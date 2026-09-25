/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.boricj.bft.image.elf;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import net.boricj.bft.elf.ElfFile;
import net.boricj.bft.elf.ElfSection;
import net.boricj.bft.elf.ElfSectionFlags;
import net.boricj.bft.elf.ElfSectionTable;
import net.boricj.bft.elf.constants.ElfSymbolType;
import net.boricj.bft.elf.sections.ElfNoBits;
import net.boricj.bft.elf.sections.ElfNullSection;
import net.boricj.bft.elf.sections.ElfProgBits;
import net.boricj.bft.elf.sections.ElfRelTable;
import net.boricj.bft.elf.sections.ElfRelaTable;
import net.boricj.bft.elf.sections.ElfStringTable;
import net.boricj.bft.elf.sections.ElfSymbolTable;
import net.boricj.bft.elf.sections.ElfSymbolTable.ElfSymbol;
import net.boricj.bft.image.ImageExporter;
import net.boricj.bft.image.ImageFile;
import net.boricj.bft.image.ImageSection;
import net.boricj.bft.image.ImageSectionLinktimeFlags;
import net.boricj.bft.image.ImageSectionRuntimeFlags;
import net.boricj.bft.image.ImageSectionType;
import net.boricj.bft.image.ImageSymbol;
import net.boricj.bft.image.RelocationFieldCodec;
import net.boricj.bft.image.RelocationFieldCursor;

/**
 * Exports semantic image objects into native ELF objects.
 */
public final class ElfExporter implements ImageExporter<ElfFile> {
	private final ImageFile image;
	private final ElfFile.Builder builder;
	private final ElfRelocationCatalogRegistry relocationCatalogRegistry;

	/**
	 * Creates an exporter from a semantic image to ELF.
	 *
	 * @param image semantic image to export
	 * @param builder preconfigured ELF builder containing target-format parameters
	 * @param relocationCatalogRegistry relocation catalog registry used for machine-specific mapping
	 */
	public ElfExporter(
			ImageFile image, ElfFile.Builder builder, ElfRelocationCatalogRegistry relocationCatalogRegistry) {
		this.image = Objects.requireNonNull(image, "image");
		this.builder = Objects.requireNonNull(builder, "builder");
		this.relocationCatalogRegistry = Objects.requireNonNull(relocationCatalogRegistry, "relocationCatalogRegistry");
	}

	@Override
	public ElfFile exportFile(Consumer<String> logSink) {
		ElfFile elf = this.builder.build();
		validateExportPreconditions(elf);

		ElfSectionTable sectionTable = elf.addSectionTable();
		Map<ImageSection, ElfSection> sectionMap = new IdentityHashMap<>();
		Map<ImageSymbol, ElfSymbol> symbolMap = new IdentityHashMap<>();
		sectionTable.add(new ElfNullSection(elf));

		addContentSections(elf, sectionTable, sectionMap, logSink);

		ElfStringTable strtab = new ElfStringTable(elf, ".strtab");
		ElfSymbolTable symtab = new ElfSymbolTable(elf, ".symtab", strtab);
		populateStringTable(strtab);
		populateSymbolTable(symtab, sectionMap, symbolMap);
		sectionTable.add(symtab);
		sectionTable.add(strtab);

		ElfRelocationCatalog catalog =
				this.relocationCatalogRegistry.catalogFor(elf.getHeader().getMachine());
		if (catalog == null) {
			throw new IllegalArgumentException(String.format(
					"Unsupported ELF machine for relocation catalog: %s",
					elf.getHeader().getMachine()));
		}
		ElfRelocationTableKind tableKind = catalog.relocationTableKind();
		if (tableKind == ElfRelocationTableKind.RELA) {
			addRelaSections(elf, sectionTable, symtab, sectionMap, symbolMap);
		} else {
			addRelSections(elf, sectionTable, symtab, sectionMap, symbolMap);
		}

		ElfStringTable shstrtab = new ElfStringTable(elf, ".shstrtab");
		sectionTable.add(shstrtab);
		elf.getHeader().setShStr(shstrtab);
		shstrtab.add("");
		for (ElfSection section : sectionTable) {
			if (section instanceof ElfNullSection) {
				continue;
			}
			shstrtab.add(section.getName());
		}

		return elf;
	}

	private void addContentSections(
			ElfFile elf,
			ElfSectionTable sectionTable,
			Map<ImageSection, ElfSection> sectionMap,
			Consumer<String> logSink) {
		for (ImageSection imageSection : this.image.sections()) {
			byte[] contents = imageSection.getBytes().clone();
			long alignment = imageSection.getAlignment();
			ElfSectionFlags flags = mapSectionFlags(imageSection, logSink);
			ElfSection elfSection;
			if (imageSection.getType() == ImageSectionType.UNINITIALIZED
					|| ((contents.length == 0)
							&& (imageSection.getLogicalSize() > 0)
							&& imageSection.getType() != ImageSectionType.UNALLOCATED)) {
				long noBitsSize = Math.max(imageSection.getLogicalSize(), contents.length);
				elfSection = new ElfNoBits(elf, imageSection.getName(), flags, alignment, noBitsSize);
			} else {
				elfSection = new ElfProgBits(elf, imageSection.getName(), flags, alignment, contents);
			}
			sectionTable.add(elfSection);
			sectionMap.put(imageSection, elfSection);
		}
	}

	private static ElfSectionFlags mapSectionFlags(ImageSection section, Consumer<String> logSink) {
		ElfSectionFlags flags = new ElfSectionFlags();
		java.util.List<String> losses = new java.util.ArrayList<>();

		if (section.hasRuntimeFlag(ImageSectionRuntimeFlags.WRITE)) {
			flags.write();
		}
		boolean isAllocated = section.getType() != ImageSectionType.UNALLOCATED
				|| section.hasRuntimeFlag(ImageSectionRuntimeFlags.READ);
		if (isAllocated) {
			flags.alloc();
		}
		if (section.hasRuntimeFlag(ImageSectionRuntimeFlags.EXECUTE)) {
			flags.execInstr();
		}

		if (section.hasLinktimeFlag(ImageSectionLinktimeFlags.INFO)) {
			losses.add("INFO");
		}
		if (section.hasLinktimeFlag(ImageSectionLinktimeFlags.REMOVE)) {
			losses.add("REMOVE");
		}
		if (section.hasLinktimeFlag(ImageSectionLinktimeFlags.DISCARDABLE)) {
			losses.add("DISCARDABLE");
		}
		if (section.hasLinktimeFlag(ImageSectionLinktimeFlags.COMMENT)) {
			losses.add("COMMENT");
		}

		if (logSink != null && !losses.isEmpty()) {
			logSink.accept(String.format(
					"ELF export for section '%s' drops non-portable section metadata [%s]; exported flags are %s.",
					section.getName(), String.join(",", losses), flags));
		}
		return flags;
	}

	private void addRelSections(
			ElfFile elf,
			ElfSectionTable sectionTable,
			ElfSymbolTable symtab,
			Map<ImageSection, ElfSection> sectionMap,
			Map<ImageSymbol, ElfSymbol> symbolMap) {
		ElfRelocationCatalog relocationCatalog =
				this.relocationCatalogRegistry.catalogFor(elf.getHeader().getMachine());
		if (relocationCatalog == null) {
			throw new IllegalArgumentException(String.format(
					"Unsupported ELF machine for relocation catalog: %s",
					elf.getHeader().getMachine()));
		}
		for (ImageSection imageSection : this.image.sections()) {
			if (imageSection.relocations().isEmpty()) {
				continue;
			}

			ElfSection elfSection = sectionMap.get(imageSection);
			ElfRelTable relTable = new ElfRelTable(elf, ".rel" + imageSection.getName(), symtab, elfSection);
			byte[] emittedSectionBytes = null;
			if (elfSection instanceof ElfProgBits elfProgBits) {
				emittedSectionBytes = elfProgBits.getBytes();
			}
			for (var relocation : imageSection.relocations()) {
				var targetSymbol = symbolMap.get(relocation.getTarget());
				if (targetSymbol == null) {
					throw new IllegalStateException("missing exported symbol for relocation target");
				}
				for (var gang : relocation.gangs()) {
					for (var entry : gang.entries()) {
						var type = relocationCatalog.typeFromEntry(entry);
						if (type == null) {
							throw new IllegalArgumentException(String.format(
									"No ELF relocation type mapping for operation '%s' and codec '%s' on machine %s",
									entry.getOperation(),
									entry.getFieldCodec(),
									elf.getHeader().getMachine()));
						}
						if (emittedSectionBytes == null) {
							throw new IllegalArgumentException(String.format(
									"Cannot emit REL relocation for non-PROGBITS section '%s'",
									imageSection.getName()));
						}
						gang.getFieldCodec().write(emittedSectionBytes, (int) entry.getOffset(), entry.getAddend());
						relTable.add(entry.getOffset(), targetSymbol, type);
					}
				}
			}
			sectionTable.add(relTable);
		}
	}

	private void addRelaSections(
			ElfFile elf,
			ElfSectionTable sectionTable,
			ElfSymbolTable symtab,
			Map<ImageSection, ElfSection> sectionMap,
			Map<ImageSymbol, ElfSymbol> symbolMap) {
		ElfRelocationCatalog relocationCatalog =
				this.relocationCatalogRegistry.catalogFor(elf.getHeader().getMachine());
		if (relocationCatalog == null) {
			throw new IllegalArgumentException(String.format(
					"Unsupported ELF machine for relocation catalog: %s",
					elf.getHeader().getMachine()));
		}
		for (ImageSection imageSection : this.image.sections()) {
			if (imageSection.relocations().isEmpty()) {
				continue;
			}

			ElfSection elfSection = sectionMap.get(imageSection);
			ElfRelaTable relaTable = new ElfRelaTable(elf, ".rela" + imageSection.getName(), symtab, elfSection);
			for (var relocation : imageSection.relocations()) {
				var targetSymbol = symbolMap.get(relocation.getTarget());
				if (targetSymbol == null) {
					throw new IllegalStateException("missing exported symbol for relocation target");
				}
				for (var gang : relocation.gangs()) {
					for (var entry : gang.entries()) {
						var type = relocationCatalog.typeFromEntry(entry);
						if (type == null) {
							throw new IllegalArgumentException(String.format(
									"No ELF relocation type mapping for operation '%s' and codec '%s' on machine %s",
									entry.getOperation(),
									entry.getFieldCodec(),
									elf.getHeader().getMachine()));
						}
						relaTable.add(entry.getOffset(), targetSymbol, type, entry.getAddend());
					}
				}
			}
			sectionTable.add(relaTable);
		}
	}

	private void validateExportPreconditions(ElfFile elf) {
		if (elf.getHeader().getType() != ElfSymbolMappings.mapImageKindToElfType(this.image.getKind())) {
			throw new IllegalArgumentException("ELF type mismatch between image kind and builder configuration");
		}

		Map<ImageSection, Boolean> sections = new IdentityHashMap<>();
		for (ImageSection section : this.image.sections()) {
			sections.put(section, Boolean.TRUE);
		}

		Map<ImageSymbol, Boolean> symbols = new IdentityHashMap<>();
		for (ImageSymbol symbol : this.image.symbols()) {
			if (symbol.getSection() != null && !sections.containsKey(symbol.getSection())) {
				throw new IllegalArgumentException("symbol references section outside of image section table");
			}
			symbols.put(symbol, Boolean.TRUE);
		}

		for (ImageSection section : this.image.sections()) {
			for (var relocation : section.relocations()) {
				if (relocation.gangs().isEmpty()) {
					throw new IllegalArgumentException("relocation group must contain at least one gang");
				}
				if (relocation.getTarget() == null) {
					throw new IllegalArgumentException("ELF relocation target cannot be null");
				}
				if (!symbols.containsKey(relocation.getTarget())) {
					throw new IllegalArgumentException("relocation target must belong to image symbol table");
				}
			}
		}
	}

	static void ensureInlineFieldIsZero(
			byte[] sectionBytes, long offset, RelocationFieldCodec codec, String sectionName) {
		Objects.requireNonNull(sectionBytes, "sectionBytes");
		Objects.requireNonNull(codec, "codec");
		Objects.requireNonNull(sectionName, "sectionName");
		if (!new RelocationFieldCursor(codec, offset).isZero(sectionBytes)) {
			throw new IllegalArgumentException(String.format(
					"Non-zero inline relocation field at section '%s' offset 0x%x for codec %s; normalized bft-image requires zero inline fields before export.",
					sectionName, offset, codec));
		}
	}

	private void populateStringTable(ElfStringTable strtab) {
		strtab.add("");
		for (ImageSymbol symbol : this.image.symbols()) {
			if (!symbol.getName().isEmpty()) {
				strtab.add(symbol.getName());
			}
		}
	}

	private void populateSymbolTable(
			ElfSymbolTable symtab, Map<ImageSection, ElfSection> sectionMap, Map<ImageSymbol, ElfSymbol> symbolMap) {
		symtab.addNull();
		for (ImageSymbol symbol : this.image.symbols()) {
			ElfSymbol elfSymbol;
			if (symbol.getType() == ImageSymbol.Type.FILE) {
				elfSymbol = symtab.add(
						symbol.getName(),
						0,
						0,
						ElfSymbolType.STT_FILE,
						ElfSymbolMappings.mapImageVisibilityToElfVisibility(symbol.getVisibility()),
						ElfSymbolMappings.mapImageBindingToElfBinding(symbol.getBinding()),
						(short) ElfSection.SHN_ABS);
			} else if (symbol.getType() == ImageSymbol.Type.SECTION) {
				elfSymbol = symtab.addSection(sectionMap.get(symbol.getSection()));
			} else if (symbol.getSection() == null) {
				elfSymbol = symtab.add(
						symbol.getName(),
						symbol.getOffset(),
						symbol.getSize(),
						ElfSymbolMappings.mapImageTypeToElfType(symbol.getType()),
						ElfSymbolMappings.mapImageVisibilityToElfVisibility(symbol.getVisibility()),
						ElfSymbolMappings.mapImageBindingToElfBinding(symbol.getBinding()),
						(short) ElfSection.SHN_UNDEF);
			} else {
				elfSymbol = symtab.add(
						symbol.getName(),
						symbol.getOffset(),
						symbol.getSize(),
						ElfSymbolMappings.mapImageTypeToElfType(symbol.getType()),
						ElfSymbolMappings.mapImageVisibilityToElfVisibility(symbol.getVisibility()),
						ElfSymbolMappings.mapImageBindingToElfBinding(symbol.getBinding()),
						sectionMap.get(symbol.getSection()));
			}

			symbolMap.put(symbol, elfSymbol);
		}
	}
}
