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
package net.boricj.bft.image.coff;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import net.boricj.bft.coff.CoffFile;
import net.boricj.bft.coff.CoffRelocationTable;
import net.boricj.bft.coff.CoffSection;
import net.boricj.bft.coff.CoffSectionTable;
import net.boricj.bft.coff.CoffSymbolTable;
import net.boricj.bft.coff.CoffSymbolTable.CoffSymbol;
import net.boricj.bft.coff.CoffSymbolTable.CoffSymbolFunction;
import net.boricj.bft.coff.constants.CoffMachine;
import net.boricj.bft.coff.constants.CoffStorageClass;
import net.boricj.bft.coff.machines.amd64.CoffRelocationType_amd64;
import net.boricj.bft.coff.sections.CoffBytes;
import net.boricj.bft.coff.sections.CoffUninitialized;
import net.boricj.bft.image.ImageFile;
import net.boricj.bft.image.ImageImporter;
import net.boricj.bft.image.ImageSection;
import net.boricj.bft.image.ImageSectionLinktimeFlags;
import net.boricj.bft.image.ImageSectionRuntimeFlags;
import net.boricj.bft.image.ImageSectionType;
import net.boricj.bft.image.ImageSymbol;
import net.boricj.bft.image.RelocationFieldCursor;

/**
 * Imports native COFF object files into semantic images.
 */
public final class CoffImporter implements ImageImporter {
	private final CoffFile coff;
	private final CoffRelocationCatalogRegistry relocationCatalogRegistry;
	private final CoffMachine machine;

	/**
	 * Creates an importer for a native COFF object file.
	 *
	 * @param coff the COFF file to import into the semantic image model
	 * @param relocationCatalogRegistry relocation catalog registry used for machine-specific mapping
	 */
	public CoffImporter(CoffFile coff, CoffRelocationCatalogRegistry relocationCatalogRegistry) {
		this.coff = Objects.requireNonNull(coff, "coff");
		this.relocationCatalogRegistry = Objects.requireNonNull(relocationCatalogRegistry, "relocationCatalogRegistry");
		this.machine = coff.getHeader().getMachine();
	}

	/**
	 * Returns the machine architecture encoded by the imported COFF object.
	 *
	 * @return the COFF target machine type
	 */
	public CoffMachine machine() {
		return this.machine;
	}

	@Override
	public ImageFile importImage(Consumer<String> logSink) {
		ImageFile image = new ImageFile(ImageFile.Kind.OBJECT);
		Map<CoffSection, ImageSection> sectionMap = new IdentityHashMap<>();
		Map<CoffSymbol, ImageSymbol> symbolMap = new IdentityHashMap<>();
		CoffSectionTable sectionTable = this.coff.getSections();
		for (CoffSection section : sectionTable) {
			ImageSection imageSection = image.sections().create(section.getName());
			populateSectionMetadata(imageSection, section);
			if (section instanceof CoffBytes bytes) {
				imageSection.setBytes(bytes.getBytes());
			} else if (section instanceof CoffUninitialized) {
				imageSection.setBytes(new byte[0]);
				imageSection.setLogicalSize(section.getVirtualSize());
			}
			sectionMap.put(section, imageSection);
		}

		CoffSymbolTable symbolTable = this.coff.getSymbols();
		for (CoffSymbol symbol : symbolTable) {
			if (symbol.getStorageClass() == CoffStorageClass.IMAGE_SYM_CLASS_NULL) {
				continue;
			}
			if (symbol.getStorageClass() == CoffStorageClass.IMAGE_SYM_CLASS_STATIC
					&& symbol.getNumberOfAuxSymbols() == 1) {
				ImageSection definingSection = null;
				if (symbol.getSectionNumber() > 0 && symbol.getSectionNumber() <= sectionTable.size()) {
					definingSection = sectionMap.get(sectionTable.get(symbol.getSectionNumber()));
				}
				ImageSymbol imageSymbol = image.symbols()
						.create(
								"",
								definingSection,
								0,
								0,
								ImageSymbol.Type.SECTION,
								ImageSymbol.Visibility.DEFAULT,
								ImageSymbol.Binding.LOCAL);
				symbolMap.put(symbol, imageSymbol);
				continue;
			}
			ImageSection definingSection = null;
			if (symbol.getSectionNumber() > 0 && symbol.getSectionNumber() <= sectionTable.size()) {
				definingSection = sectionMap.get(sectionTable.get(symbol.getSectionNumber()));
			}
			ImageSymbol imageSymbol = image.symbols()
					.create(
							symbol.getName(),
							definingSection,
							Integer.toUnsignedLong(symbol.getValue()),
							extractSymbolSize(symbol),
							CoffSymbolMappings.mapCoffTypeToImageType(symbol),
							ImageSymbol.Visibility.DEFAULT,
							CoffSymbolMappings.mapCoffStorageClassToImageBinding(symbol),
							CoffSymbolMappings.mapCoffStorageClassToImageRole(symbol));
			symbolMap.put(symbol, imageSymbol);
		}

		for (CoffSection section : sectionTable) {
			ImageSection imageSection = sectionMap.get(section);
			CoffRelocationTable relocations = section.getRelocations();
			CoffRelocationCatalog relocationCatalog = this.relocationCatalogRegistry.catalogFor(this.machine);
			if (relocationCatalog == null) {
				throw new IllegalArgumentException(String.format("Unsupported COFF machine: %s", this.machine));
			}
			for (var relocation : relocations) {
				ImageSymbol target = symbolMap.get(symbolTable.get(relocation.getSymbolTableIndex()));
				if (target == null) {
					continue;
				}
				var descriptor = relocationCatalog.descriptorFromType(relocation.getType());
				if (descriptor == null) {
					throw new IllegalArgumentException(String.format(
							"Unsupported COFF relocation type %s for machine %s", relocation.getType(), this.machine));
				}
				byte[] sourceBytes = imageSection.getBytes();
				byte[] workingBytes = Arrays.copyOf(sourceBytes, sourceBytes.length);
				var cursor = new RelocationFieldCursor(
						descriptor.fieldCodec(), Integer.toUnsignedLong(relocation.getVirtualAddress()));
				long groupAddend = cursor.read(workingBytes);
				cursor.mask(workingBytes).apply(workingBytes);
				imageSection.setBytes(workingBytes);
				long localAddend = 0L;
				if (this.machine == CoffMachine.IMAGE_FILE_MACHINE_AMD64
						&& descriptor.operation() == net.boricj.bft.image.RelocationOperation.PC_RELATIVE
						&& descriptor.fieldCodec() == net.boricj.bft.image.relocs.priv.ByteFieldCodec.S32LE) {
					var type = relocation.getType();
					if (type == CoffRelocationType_amd64.IMAGE_REL_AMD64_REL32_1) {
						localAddend = 1L;
					} else if (type == CoffRelocationType_amd64.IMAGE_REL_AMD64_REL32_2) {
						localAddend = 2L;
					} else if (type == CoffRelocationType_amd64.IMAGE_REL_AMD64_REL32_3) {
						localAddend = 3L;
					} else if (type == CoffRelocationType_amd64.IMAGE_REL_AMD64_REL32_4) {
						localAddend = 4L;
					} else if (type == CoffRelocationType_amd64.IMAGE_REL_AMD64_REL32_5) {
						localAddend = 5L;
					}
				}
				var relocationGroup = imageSection.relocations().create(descriptor.operation(), target, groupAddend);
				relocationGroup
						.gangs()
						.create(descriptor.fieldCodec())
						.entries()
						.create(Integer.toUnsignedLong(relocation.getVirtualAddress()), localAddend);
			}
		}

		return image;
	}

	private static void populateSectionMetadata(ImageSection imageSection, CoffSection section) {
		var flags = section.getCharacteristics();
		imageSection.setAlignment(flags.getAlignBytes());
		if (flags.isCntUninitializedData()) {
			imageSection.setType(ImageSectionType.UNINITIALIZED);
		} else if (flags.isCntCode() || flags.isCntInitializedData()) {
			imageSection.setType(ImageSectionType.INITIALIZED);
		} else {
			imageSection.setType(ImageSectionType.UNALLOCATED);
		}
		if (flags.isLnkInfo()) {
			imageSection.addLinktimeFlag(ImageSectionLinktimeFlags.INFO);
		}
		if (flags.isLnkRemove()) {
			imageSection.addLinktimeFlag(ImageSectionLinktimeFlags.REMOVE);
		}
		if (flags.isMemDiscardable()) {
			imageSection.addLinktimeFlag(ImageSectionLinktimeFlags.DISCARDABLE);
		}
		if (flags.isMemRead()) {
			imageSection.addRuntimeFlag(ImageSectionRuntimeFlags.READ);
		}
		if (flags.isMemWrite()) {
			imageSection.addRuntimeFlag(ImageSectionRuntimeFlags.WRITE);
		}
		if (flags.isMemExecute()) {
			imageSection.addRuntimeFlag(ImageSectionRuntimeFlags.EXECUTE);
		}
		if (imageSection.getType() != ImageSectionType.UNALLOCATED
				&& !flags.isMemRead()
				&& !flags.isMemWrite()
				&& !flags.isMemExecute()) {
			// COFF frequently omits explicit read permission for allocatable content sections.
			imageSection.addRuntimeFlag(ImageSectionRuntimeFlags.READ);
		}
	}

	private static long extractSymbolSize(CoffSymbol symbol) {
		if (symbol instanceof CoffSymbolFunction functionSymbol) {
			return Integer.toUnsignedLong(functionSymbol.getTotalSize());
		}
		return 0L;
	}
}
