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
import net.boricj.bft.elf.ElfHeader;
import net.boricj.bft.elf.ElfSection;
import net.boricj.bft.elf.ElfSectionTable;
import net.boricj.bft.elf.constants.ElfClass;
import net.boricj.bft.elf.constants.ElfData;
import net.boricj.bft.elf.constants.ElfMachine;
import net.boricj.bft.elf.constants.ElfOsAbi;
import net.boricj.bft.elf.constants.ElfSymbolBinding;
import net.boricj.bft.elf.constants.ElfSymbolType;
import net.boricj.bft.elf.constants.ElfSymbolVisibility;
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
import net.boricj.bft.image.ImageSymbol;

/**
 * Imports native ELF objects into semantic image objects.
 */
public final class ElfImporter implements ImageImporter {
	private final ElfFile elf;
	private final ElfClass elfClass;
	private final ElfData elfData;
	private final ElfOsAbi osAbi;
	private final ElfMachine machine;

	/**
	 * Creates an ELF importer for a parsed ELF file.
	 *
	 * @param elf source ELF file
	 */
	public ElfImporter(ElfFile elf) {
		this.elf = Objects.requireNonNull(elf, "elf");
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
			for (ElfSection section : sectionTable) {
				if (section instanceof ElfNullSection) {
					continue;
				}

				if (section instanceof ElfProgBits progbits) {
					ImageSection imageSection = image.sections().create(section.getName());
					imageSection.setContents(progbits.getBytes());
					sectionMap.put(section, imageSection);
					continue;
				}

				if (section instanceof ElfNoBits noBits) {
					ImageSection imageSection = image.sections().create(section.getName());
					imageSection.setContents(new byte[0]);
					imageSection.setLogicalSize(noBits.getSize());
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
									mapType(symbol.getType()),
									mapVisibility(symbol.getVisibility()),
									mapBinding(symbol.getBinding()));
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

	private void importRelocations(
			ElfRelTable relTable,
			Map<ElfSection, ImageSection> sectionMap,
			Map<ElfSymbol, ImageSymbol> symbolMap,
			Consumer<String> logSink) {
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
			var descriptor = ElfRelocationCatalog.descriptorFromType(relocation.getType());

			imageSection
					.relocations()
					.createSingleEntry(
							relocation.getOffset(), descriptor.operation(), descriptor.fieldCodec(), target, 0);
		}
	}

	private void importRelocations(
			ElfRelaTable relaTable,
			Map<ElfSection, ImageSection> sectionMap,
			Map<ElfSymbol, ImageSymbol> symbolMap,
			Consumer<String> logSink) {
		ElfSection targetSection = relaTable.getElfFile().getSections().get(relaTable.getInfo());
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
			var descriptor = ElfRelocationCatalog.descriptorFromType(relocation.getType());

			imageSection
					.relocations()
					.createSingleEntry(
							relocation.getOffset(),
							descriptor.operation(),
							descriptor.fieldCodec(),
							target,
							relocation.getAddend());
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

	private static ImageSymbol.Type mapType(ElfSymbolType type) {
		return switch (type) {
			case STT_NOTYPE -> ImageSymbol.Type.NOTYPE;
			case STT_OBJECT -> ImageSymbol.Type.OBJECT;
			case STT_FUNC -> ImageSymbol.Type.FUNCTION;
			case STT_SECTION -> ImageSymbol.Type.SECTION;
			case STT_FILE -> ImageSymbol.Type.FILE;
			case STT_TLS -> ImageSymbol.Type.TLS;
		};
	}

	private static ImageSymbol.Visibility mapVisibility(ElfSymbolVisibility visibility) {
		return switch (visibility) {
			case STV_DEFAULT -> ImageSymbol.Visibility.DEFAULT;
			case STV_INTERNAL -> ImageSymbol.Visibility.INTERNAL;
			case STV_HIDDEN -> ImageSymbol.Visibility.HIDDEN;
			case STV_PROTECTED -> ImageSymbol.Visibility.PROTECTED;
		};
	}

	private static ImageSymbol.Binding mapBinding(ElfSymbolBinding binding) {
		return switch (binding) {
			case STB_LOCAL -> ImageSymbol.Binding.LOCAL;
			case STB_GLOBAL -> ImageSymbol.Binding.GLOBAL;
			case STB_WEAK -> ImageSymbol.Binding.WEAK;
		};
	}
}
