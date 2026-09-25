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

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import net.boricj.bft.elf.ElfFile;
import net.boricj.bft.elf.ElfHeader;
import net.boricj.bft.elf.ElfSection;
import net.boricj.bft.elf.ElfSectionTable;
import net.boricj.bft.elf.constants.ElfClass;
import net.boricj.bft.elf.constants.ElfData;
import net.boricj.bft.elf.constants.ElfMachine;
import net.boricj.bft.elf.constants.ElfOsAbi;
import net.boricj.bft.elf.constants.ElfSymbolBinding;
import net.boricj.bft.elf.constants.ElfSymbolType;
import net.boricj.bft.elf.constants.ElfType;
import net.boricj.bft.elf.sections.ElfNoBits;
import net.boricj.bft.elf.sections.ElfNullSection;
import net.boricj.bft.elf.sections.ElfProgBits;
import net.boricj.bft.elf.sections.ElfRelTable;
import net.boricj.bft.elf.sections.ElfRelTable.ElfRel;
import net.boricj.bft.elf.sections.ElfRelaTable;
import net.boricj.bft.elf.sections.ElfRelaTable.ElfRela;
import net.boricj.bft.elf.sections.ElfStringTable;
import net.boricj.bft.elf.sections.ElfSymbolTable;
import net.boricj.bft.elf.sections.ElfSymbolTable.ElfSymbol;
import net.boricj.bft.image.ImageFile;
import net.boricj.bft.image.ImageImporter;
import net.boricj.bft.image.ImageSection;
import net.boricj.bft.image.ImageSectionRuntimeFlags;
import net.boricj.bft.image.ImageSectionType;
import net.boricj.bft.image.ImageSymbol;
import net.boricj.bft.image.RelocationFieldCursor;

/**
 * Imports native ELF objects into semantic image objects.
 */
public final class ElfImporter implements ImageImporter {
	private final ElfFile elf;
	private final ElfRelocationCatalogRegistry relocationCatalogRegistry;
	private final ElfClass elfClass;
	private final ElfData elfData;
	private final ElfOsAbi osAbi;
	private final ElfMachine machine;

	/**
	 * Creates an ELF importer for a parsed ELF file.
	 *
	 * @param elf source ELF file
	 * @param relocationCatalogRegistry relocation catalog registry used for machine-specific mapping
	 */
	public ElfImporter(ElfFile elf, ElfRelocationCatalogRegistry relocationCatalogRegistry) {
		this.elf = Objects.requireNonNull(elf, "elf");
		this.relocationCatalogRegistry = Objects.requireNonNull(relocationCatalogRegistry, "relocationCatalogRegistry");
		ElfHeader header = elf.getHeader();
		this.elfClass = header.getIdentClass();
		this.elfData = header.getIdentData();
		this.osAbi = header.getIdentOsAbi();
		this.machine = header.getMachine();
	}

	/**
	 * Returns ELF class (32/64-bit).
	 *
	 * @return ELF class
	 */
	public ElfClass elfClass() {
		return this.elfClass;
	}

	/**
	 * Returns ELF data encoding.
	 *
	 * @return ELF data encoding
	 */
	public ElfData elfData() {
		return this.elfData;
	}

	/**
	 * Returns ELF OS ABI.
	 *
	 * @return ELF OS ABI
	 */
	public ElfOsAbi osAbi() {
		return this.osAbi;
	}

	/**
	 * Returns ELF target machine.
	 *
	 * @return ELF machine
	 */
	public ElfMachine machine() {
		return this.machine;
	}

	@Override
	public ImageFile importImage(Consumer<String> logSink) {
		ImageFile image = new ImageFile(mapKind(this.elf.getHeader().getType()));
		Map<ElfSection, ImageSection> sectionMap = new IdentityHashMap<>();
		Map<ElfSymbol, ImageSymbol> symbolMap = new IdentityHashMap<>();

		ElfSectionTable sectionTable = this.elf.getSections();
		if (sectionTable != null) {
			boolean hasNamedSymtab = false;
			for (ElfSection section : sectionTable) {
				if (section instanceof ElfSymbolTable symbolTable && ".symtab".equals(symbolTable.getName())) {
					hasNamedSymtab = true;
					break;
				}
			}

			for (ElfSection section : sectionTable) {
				if (section instanceof ElfNullSection) {
					continue;
				}

				if (section instanceof ElfProgBits progbits) {
					ImageSection imageSection = image.sections().create(section.getName());
					imageSection.setBytes(progbits.getBytes());
					populateSectionMetadata(imageSection, section);
					sectionMap.put(section, imageSection);
					continue;
				}

				if (section instanceof ElfNoBits noBits) {
					ImageSection imageSection = image.sections().create(section.getName());
					imageSection.setBytes(new byte[0]);
					imageSection.setLogicalSize(noBits.getSize());
					populateSectionMetadata(imageSection, section);
					sectionMap.put(section, imageSection);
					continue;
				}

				if (section instanceof ElfSymbolTable
						|| section instanceof ElfStringTable
						|| section instanceof ElfRelTable
						|| section instanceof ElfRelaTable) {
					continue;
				}

				logSink.accept(String.format(
						"Skipped unsupported ELF section '%s' of type 0x%x", section.getName(), section.getType()));
			}

			for (ElfSection section : sectionTable) {
				if (!(section instanceof ElfSymbolTable symbolTable)) {
					continue;
				}
				if (!shouldImportSymbolTable(this.elf.getHeader().getType(), symbolTable.getName(), hasNamedSymtab)) {
					continue;
				}

				for (ElfSymbol symbol : symbolTable) {
					if (isNullSymbol(symbol)) {
						continue;
					}

					ImageSection imageSection = mapSymbolSection(symbol, sectionTable, sectionMap);
					if (symbol.getType() == ElfSymbolType.STT_SECTION && imageSection == null) {
						logSink.accept(String.format(
								"Skipped section symbol for unsupported section index %d",
								Short.toUnsignedInt(symbol.getIndex())));
						continue;
					}

					ImageSymbol imageSymbol = image.symbols()
							.create(
									symbol.getName(),
									imageSection,
									symbol.getValue(),
									symbol.getSize(),
									ElfSymbolMappings.mapElfTypeToImageType(symbol.getType()),
									ElfSymbolMappings.mapElfVisibilityToImageVisibility(symbol.getVisibility()),
									ElfSymbolMappings.mapElfBindingToImageBinding(symbol.getBinding()));
					symbolMap.put(symbol, imageSymbol);
				}
			}

			for (ElfSection section : sectionTable) {
				if (section instanceof ElfRelTable relTable) {
					importRelocations(relTable, sectionMap, symbolMap, logSink);
				} else if (section instanceof ElfRelaTable relaTable) {
					importRelocations(relaTable, sectionMap, symbolMap, logSink);
				}
			}
		}

		return image;
	}

	static boolean shouldImportSymbolTable(ElfType fileType, String tableName, boolean hasNamedSymtab) {
		if (fileType == ElfType.ET_REL) {
			return true;
		}
		if (fileType == ElfType.ET_EXEC || fileType == ElfType.ET_DYN) {
			if (hasNamedSymtab) {
				return ".symtab".equals(tableName);
			}
			return ".dynsym".equals(tableName);
		}
		return false;
	}

	private static void populateSectionMetadata(ImageSection imageSection, ElfSection section) {
		long alignment = section.getAddrAlign();
		imageSection.setAlignment(alignment);
		var flags = section.getFlags();
		if (flags.isAlloc()) {
			if (section instanceof ElfNoBits) {
				imageSection.setType(ImageSectionType.UNINITIALIZED);
			} else if (section instanceof ElfProgBits) {
				imageSection.setType(ImageSectionType.INITIALIZED);
			} else {
				imageSection.setType(ImageSectionType.INITIALIZED);
			}
		} else {
			imageSection.setType(ImageSectionType.UNALLOCATED);
		}
		if (flags.isAlloc()) {
			imageSection.addRuntimeFlag(ImageSectionRuntimeFlags.READ);
		}
		if (flags.isWrite()) {
			imageSection.addRuntimeFlag(ImageSectionRuntimeFlags.WRITE);
		}
		if (flags.isExecInstr()) {
			imageSection.addRuntimeFlag(ImageSectionRuntimeFlags.EXECUTE);
		}
	}

	private void importRelocations(
			ElfRelTable relTable,
			Map<ElfSection, ImageSection> sectionMap,
			Map<ElfSymbol, ImageSymbol> symbolMap,
			Consumer<String> logSink) {
		ElfRelocationCatalog relocationCatalog = this.relocationCatalogRegistry.catalogFor(this.machine);
		if (relocationCatalog == null) {
			throw new IllegalArgumentException(String.format("Unsupported ELF machine: %s", this.machine));
		}
		ImageSection imageSection = sectionMap.get(relTable.getSection());
		if (imageSection == null) {
			logSink.accept(String.format(
					"Skipped relocations for unsupported section '%s'",
					relTable.getSection().getName()));
			return;
		}

		for (ElfRel relocation : relTable) {
			ImageSymbol target = symbolMap.get(relocation.getSymbol());
			if (target == null) {
				logSink.accept(String.format(
						"Skipped relocation at offset 0x%x due to unsupported symbol", relocation.getOffset()));
				continue;
			}
			var descriptor = relocationCatalog.descriptorFromType(relocation.getType());
			if (descriptor == null) {
				throw new IllegalArgumentException(String.format(
						"Unsupported ELF relocation type %s for machine %s", relocation.getType(), this.machine));
			}
			byte[] sourceBytes = imageSection.getBytes();
			byte[] workingBytes = Arrays.copyOf(sourceBytes, sourceBytes.length);
			var cursor = new RelocationFieldCursor(descriptor.fieldCodec(), relocation.getOffset());
			long groupAddend = cursor.read(workingBytes);
			cursor.mask(workingBytes).apply(workingBytes);
			imageSection.setBytes(workingBytes);
			var relocationGroup = imageSection.relocations().create(descriptor.operation(), target, groupAddend);
			relocationGroup.gangs().create(descriptor.fieldCodec()).entries().create(relocation.getOffset(), 0L);
		}
	}

	private void importRelocations(
			ElfRelaTable relaTable,
			Map<ElfSection, ImageSection> sectionMap,
			Map<ElfSymbol, ImageSymbol> symbolMap,
			Consumer<String> logSink) {
		ElfRelocationCatalog relocationCatalog = this.relocationCatalogRegistry.catalogFor(this.machine);
		if (relocationCatalog == null) {
			throw new IllegalArgumentException(String.format("Unsupported ELF machine: %s", this.machine));
		}
		int targetSectionIndex = relaTable.getInfo();
		if (targetSectionIndex < 0
				|| targetSectionIndex >= relaTable.getElfFile().getSections().size()) {
			logSink.accept(
					String.format("Skipped relocations for invalid target section index %d", targetSectionIndex));
			return;
		}
		ElfSection targetSection = relaTable.getElfFile().getSections().get(targetSectionIndex);
		ImageSection imageSection = sectionMap.get(targetSection);
		if (imageSection == null) {
			logSink.accept(String.format("Skipped relocations for unsupported section '%s'", targetSection.getName()));
			return;
		}

		for (ElfRela relocation : relaTable) {
			ImageSymbol target = symbolMap.get(relocation.getSymbol());
			if (target == null) {
				logSink.accept(String.format(
						"Skipped relocation at offset 0x%x due to unsupported symbol", relocation.getOffset()));
				continue;
			}
			var descriptor = relocationCatalog.descriptorFromType(relocation.getType());
			if (descriptor == null) {
				throw new IllegalArgumentException(String.format(
						"Unsupported ELF relocation type %s for machine %s", relocation.getType(), this.machine));
			}
			var relocationGroup =
					imageSection.relocations().create(descriptor.operation(), target, relocation.getAddend());
			relocationGroup.gangs().create(descriptor.fieldCodec()).entries().create(relocation.getOffset(), 0L);
		}
	}

	private static ImageSection mapSymbolSection(
			ElfSymbol symbol, ElfSectionTable sectionTable, Map<ElfSection, ImageSection> sectionMap) {
		int index = Short.toUnsignedInt(symbol.getIndex());
		if (index == ElfSection.SHN_UNDEF || index >= sectionTable.size()) {
			return null;
		}
		return sectionMap.get(sectionTable.get(index));
	}

	private static boolean isNullSymbol(ElfSymbol symbol) {
		return symbol.getName().isEmpty()
				&& symbol.getValue() == 0
				&& symbol.getSize() == 0
				&& symbol.getType() == ElfSymbolType.STT_NOTYPE
				&& symbol.getBinding() == ElfSymbolBinding.STB_LOCAL
				&& Short.toUnsignedInt(symbol.getIndex()) == ElfSection.SHN_UNDEF;
	}

	private static ImageFile.Kind mapKind(ElfType type) {
		return switch (type) {
			case ET_REL -> ImageFile.Kind.OBJECT;
			case ET_EXEC -> ImageFile.Kind.EXECUTABLE;
			case ET_DYN -> ImageFile.Kind.DYNAMIC_LIBRARY;
			default -> throw new IllegalArgumentException(String.format("Unsupported ELF type: %s", type));
		};
	}
}
