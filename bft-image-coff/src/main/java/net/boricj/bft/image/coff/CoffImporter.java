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
import net.boricj.bft.coff.constants.CoffMachine;
import net.boricj.bft.coff.constants.CoffStorageClass;
import net.boricj.bft.coff.sections.CoffBytes;
import net.boricj.bft.coff.sections.CoffUninitialized;
import net.boricj.bft.image.ImageFile;
import net.boricj.bft.image.ImageImporter;
import net.boricj.bft.image.ImageSection;
import net.boricj.bft.image.ImageSectionLinktimeFlags;
import net.boricj.bft.image.ImageSectionRuntimeFlags;
import net.boricj.bft.image.ImageSymbol;

/**
 * Imports native COFF object files into semantic images.
 */
public final class CoffImporter implements ImageImporter {
	private final CoffFile coff;
	private final CoffMachine machine;

	/**
	 * Creates an importer for a native COFF object file.
	 *
	 * @param coff the COFF file to import into the semantic image model
	 */
	public CoffImporter(CoffFile coff) {
		this.coff = Objects.requireNonNull(coff, "coff");
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
				imageSection.setContents(bytes.getBytes());
			} else if (section instanceof CoffUninitialized) {
				imageSection.setContents(new byte[0]);
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
			long size = 0;
			if (definingSection != null) {
				size = definingSection.getLogicalSize() > 0
						? definingSection.getLogicalSize()
						: definingSection.getContents().length;
			}
			ImageSymbol imageSymbol = image.symbols()
					.create(
							symbol.getName(),
							definingSection,
							symbol.getValue(),
							size,
							mapType(symbol),
							ImageSymbol.Visibility.DEFAULT,
							mapBinding(symbol));
			symbolMap.put(symbol, imageSymbol);
		}

		for (CoffSection section : sectionTable) {
			ImageSection imageSection = sectionMap.get(section);
			CoffRelocationTable relocations = section.getRelocations();
			for (var relocation : relocations) {
				ImageSymbol target = symbolMap.get(symbolTable.get(relocation.getSymbolTableIndex()));
				if (target == null) {
					continue;
				}
				var descriptor = CoffRelocationCatalog.descriptorFromType(relocation.getType());
				imageSection
						.relocations()
						.createSingleEntry(
								relocation.getVirtualAddress(),
								descriptor.operation(),
								descriptor.fieldCodec(),
								target,
								0);
			}
		}

		return image;
	}

	private static void populateSectionMetadata(ImageSection imageSection, CoffSection section) {
		var flags = section.getCharacteristics();
		if (flags.getAlignBytes() > 0) {
			imageSection.setAlignment(flags.getAlignBytes());
		}
		if (flags.isCntCode()) {
			imageSection.addLinktimeFlag(ImageSectionLinktimeFlags.CODE);
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
		if (flags.isMemRead() || flags.isMemWrite() || flags.isMemExecute()) {
			imageSection.addLinktimeFlag(ImageSectionLinktimeFlags.ALLOC);
		}
	}

	private static ImageSymbol.Type mapType(CoffSymbol symbol) {
		if (symbol.getStorageClass() == CoffStorageClass.IMAGE_SYM_CLASS_FILE) {
			return ImageSymbol.Type.FILE;
		}
		if (symbol.getStorageClass() == CoffStorageClass.IMAGE_SYM_CLASS_SECTION) {
			return ImageSymbol.Type.SECTION;
		}
		if (symbol.getType() == 0x20) {
			return ImageSymbol.Type.FUNCTION;
		}
		if (symbol.getType() == 0x00) {
			return ImageSymbol.Type.NOTYPE;
		}
		return ImageSymbol.Type.OBJECT;
	}

	private static ImageSymbol.Binding mapBinding(CoffSymbol symbol) {
		if (symbol.getStorageClass() == CoffStorageClass.IMAGE_SYM_CLASS_EXTERNAL) {
			return ImageSymbol.Binding.GLOBAL;
		}
		return ImageSymbol.Binding.LOCAL;
	}
}
