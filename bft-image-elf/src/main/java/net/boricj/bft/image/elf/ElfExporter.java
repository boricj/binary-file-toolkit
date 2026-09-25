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
import net.boricj.bft.elf.constants.ElfSymbolBinding;
import net.boricj.bft.elf.constants.ElfSymbolType;
import net.boricj.bft.elf.constants.ElfSymbolVisibility;
import net.boricj.bft.elf.constants.ElfType;
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
import net.boricj.bft.image.ImageSymbol;

/**
 * Exports semantic image objects into native ELF objects.
 */
public final class ElfExporter implements ImageExporter<ElfFile> {
	private final ImageFile image;
	private final ElfFile.Builder builder;

	/**
	 * Creates an exporter from a semantic image to ELF.
	 *
	 * @param image semantic image to export
	 * @param builder preconfigured ELF builder containing target-format parameters
	 */
	public ElfExporter(ImageFile image, ElfFile.Builder builder) {
		this.image = Objects.requireNonNull(image, "image");
		this.builder = Objects.requireNonNull(builder, "builder");
	}

	@Override
	public ElfFile exportFile(Consumer<String> logSink) {
		ElfFile elf = this.builder.build();
		validateExportPreconditions(elf);

		ElfSectionTable sectionTable = elf.addSectionTable();
		Map<ImageSection, ElfSection> sectionMap = new IdentityHashMap<>();
		Map<ImageSymbol, ElfSymbol> symbolMap = new IdentityHashMap<>();
		sectionTable.add(new ElfNullSection(elf));

		addContentSections(elf, sectionTable, sectionMap);

		ElfStringTable strtab = new ElfStringTable(elf, ".strtab");
		ElfSymbolTable symtab = new ElfSymbolTable(elf, ".symtab", strtab);
		populateStringTable(strtab);
		populateSymbolTable(symtab, sectionMap, symbolMap);
		sectionTable.add(symtab);
		sectionTable.add(strtab);

		boolean explicitAddends =
				ElfRelocationCatalog.usesExplicitAddends(elf.getHeader().getMachine());
		if (explicitAddends) {
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
			ElfFile elf, ElfSectionTable sectionTable, Map<ImageSection, ElfSection> sectionMap) {
		for (ImageSection imageSection : this.image.sections()) {
			byte[] contents = imageSection.getContents();
			ElfSection elfSection;
			if (imageSection.getLogicalSize() > contents.length) {
				elfSection = new ElfNoBits(
						elf, imageSection.getName(), new ElfSectionFlags(), 1, imageSection.getLogicalSize());
			} else {
				elfSection = new ElfProgBits(elf, imageSection.getName(), new ElfSectionFlags(), 1, contents);
			}
			sectionTable.add(elfSection);
			sectionMap.put(imageSection, elfSection);
		}
	}

	private void addRelSections(
			ElfFile elf,
			ElfSectionTable sectionTable,
			ElfSymbolTable symtab,
			Map<ImageSection, ElfSection> sectionMap,
			Map<ImageSymbol, ElfSymbol> symbolMap) {
		for (ImageSection imageSection : this.image.sections()) {
			if (imageSection.relocations().isEmpty()) {
				continue;
			}

			ElfSection elfSection = sectionMap.get(imageSection);
			ElfRelTable relTable = new ElfRelTable(elf, ".rel" + imageSection.getName(), symtab, elfSection);
			for (var relocation : imageSection.relocations()) {
				for (var gang : relocation.gangs()) {
					var type = ElfRelocationCatalog.typeFromDescriptor(
							elf.getHeader().getMachine(), relocation.getOperation(), gang.getFieldCodec());
					for (var entry : gang.entries()) {
						relTable.add(entry.getOffset(), symbolMap.get(relocation.getTarget()), type);
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
		for (ImageSection imageSection : this.image.sections()) {
			if (imageSection.relocations().isEmpty()) {
				continue;
			}

			ElfSection elfSection = sectionMap.get(imageSection);
			ElfRelaTable relaTable = new ElfRelaTable(elf, ".rela" + imageSection.getName(), symtab, elfSection);
			for (var relocation : imageSection.relocations()) {
				for (var gang : relocation.gangs()) {
					var type = ElfRelocationCatalog.typeFromDescriptor(
							elf.getHeader().getMachine(), relocation.getOperation(), gang.getFieldCodec());
					for (var entry : gang.entries()) {
						relaTable.add(
								entry.getOffset(), symbolMap.get(relocation.getTarget()), type, entry.getAddend());
					}
				}
			}
			sectionTable.add(relaTable);
		}
	}

	private void validateExportPreconditions(ElfFile elf) {
		if (elf.getHeader().getType() != mapType(this.image.getKind())) {
			throw new IllegalArgumentException("ELF type mismatch between image kind and builder configuration");
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
						mapVisibility(symbol.getVisibility()),
						mapBinding(symbol.getBinding()),
						(short) ElfSection.SHN_ABS);
			} else if (symbol.getType() == ImageSymbol.Type.SECTION) {
				elfSymbol = symtab.addSection(sectionMap.get(symbol.getSection()));
			} else if (symbol.getSection() == null) {
				elfSymbol = symtab.add(
						symbol.getName(),
						symbol.getOffset(),
						symbol.getSize(),
						mapType(symbol.getType()),
						mapVisibility(symbol.getVisibility()),
						mapBinding(symbol.getBinding()),
						(short) ElfSection.SHN_UNDEF);
			} else {
				elfSymbol = symtab.add(
						symbol.getName(),
						symbol.getOffset(),
						symbol.getSize(),
						mapType(symbol.getType()),
						mapVisibility(symbol.getVisibility()),
						mapBinding(symbol.getBinding()),
						sectionMap.get(symbol.getSection()));
			}

			symbolMap.put(symbol, elfSymbol);
		}
	}

	private static ElfType mapType(ImageFile.Kind kind) {
		return switch (kind) {
			case OBJECT -> ElfType.ET_REL;
			case EXECUTABLE -> ElfType.ET_EXEC;
			case DYNAMIC_LIBRARY -> ElfType.ET_DYN;
		};
	}

	private static ElfSymbolType mapType(ImageSymbol.Type type) {
		return switch (type) {
			case NOTYPE -> ElfSymbolType.STT_NOTYPE;
			case OBJECT -> ElfSymbolType.STT_OBJECT;
			case FUNCTION -> ElfSymbolType.STT_FUNC;
			case SECTION -> ElfSymbolType.STT_SECTION;
			case FILE -> ElfSymbolType.STT_FILE;
			case TLS -> ElfSymbolType.STT_TLS;
		};
	}

	private static ElfSymbolVisibility mapVisibility(ImageSymbol.Visibility visibility) {
		return switch (visibility) {
			case DEFAULT -> ElfSymbolVisibility.STV_DEFAULT;
			case INTERNAL -> ElfSymbolVisibility.STV_INTERNAL;
			case HIDDEN -> ElfSymbolVisibility.STV_HIDDEN;
			case PROTECTED -> ElfSymbolVisibility.STV_PROTECTED;
		};
	}

	private static ElfSymbolBinding mapBinding(ImageSymbol.Binding binding) {
		return switch (binding) {
			case LOCAL -> ElfSymbolBinding.STB_LOCAL;
			case GLOBAL -> ElfSymbolBinding.STB_GLOBAL;
			case WEAK -> ElfSymbolBinding.STB_WEAK;
		};
	}
}
