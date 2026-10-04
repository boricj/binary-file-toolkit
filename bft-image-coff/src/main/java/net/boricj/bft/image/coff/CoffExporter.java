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
import net.boricj.bft.coff.CoffSectionTable;
import net.boricj.bft.coff.CoffSymbolTable;
import net.boricj.bft.coff.CoffSymbolTable.CoffSymbol;
import net.boricj.bft.coff.constants.CoffSectionFlags;
import net.boricj.bft.coff.constants.CoffStorageClass;
import net.boricj.bft.coff.sections.CoffBytes;
import net.boricj.bft.coff.sections.CoffUninitialized;
import net.boricj.bft.image.ImageExporter;
import net.boricj.bft.image.ImageFile;
import net.boricj.bft.image.ImageSection;
import net.boricj.bft.image.ImageSectionLinktimeFlags;
import net.boricj.bft.image.ImageSectionRuntimeFlags;
import net.boricj.bft.image.ImageSymbol;

/**
 * Exports semantic images into native COFF object files.
 */
public final class CoffExporter implements ImageExporter<CoffFile> {
	private final ImageFile image;
	private final CoffFile.Builder builder;

	/**
	 * Creates an exporter that serializes a semantic image into a native COFF object.
	 *
	 * @param image the semantic image to export
	 * @param builder the COFF file builder that receives the generated object content
	 */
	public CoffExporter(ImageFile image, CoffFile.Builder builder) {
		this.image = Objects.requireNonNull(image, "image");
		this.builder = Objects.requireNonNull(builder, "builder");
	}

	@Override
	public CoffFile exportFile(Consumer<String> logSink) {
		CoffFile coff = builder.build();
		CoffSectionTable sections = coff.getSections();
		Map<ImageSection, net.boricj.bft.coff.CoffSection> sectionMap = new IdentityHashMap<>();
		Map<ImageSymbol, CoffSymbol> symbolMap = new IdentityHashMap<>();

		for (ImageSection imageSection : this.image.sections()) {
			CoffSectionFlags flags = mapSectionFlags(imageSection);
			applyDataKindFlags(imageSection, flags);
			net.boricj.bft.coff.CoffSection section =
					new CoffBytes(coff, imageSection.getName(), flags, imageSection.getContents());
			if ((imageSection.getContents().length == 0) && (imageSection.getLogicalSize() > 0)) {
				section =
						new CoffUninitialized(coff, imageSection.getName(), flags, (int) imageSection.getLogicalSize());
			}
			sections.add(section);
			sectionMap.put(imageSection, section);
		}

		CoffSymbolTable symbols = coff.getSymbols();
		for (ImageSymbol imageSymbol : this.image.symbols()) {
			CoffSymbol symbol;
			if (imageSymbol.getType() == ImageSymbol.Type.FILE) {
				symbol = symbols.addFile(imageSymbol.getName(), imageSymbol.getName());
			} else if (imageSymbol.getType() == ImageSymbol.Type.SECTION) {
				symbol = symbols.addSection(sectionMap.get(imageSymbol.getSection()));
			} else if (imageSymbol.getSection() == null) {
				if (imageSymbol.getBinding() == ImageSymbol.Binding.LOCAL) {
					symbol = symbols.addAbsolute(imageSymbol.getName(), (int) imageSymbol.getOffset());
				} else {
					symbol = symbols.addUndefined(imageSymbol.getName(), mapType(imageSymbol.getType()));
				}
			} else {
				symbol = symbols.addSymbol(
						imageSymbol.getName(),
						(int) imageSymbol.getOffset(),
						sectionMap.get(imageSymbol.getSection()),
						mapType(imageSymbol.getType()),
						CoffStorageClass.IMAGE_SYM_CLASS_EXTERNAL);
			}
			symbolMap.put(imageSymbol, symbol);
		}

		for (ImageSection imageSection : this.image.sections()) {
			var section = sectionMap.get(imageSection);
			for (var group : imageSection.relocations()) {
				var type = CoffRelocationCatalog.typeFromDescriptor(
						coff.getHeader().getMachine(),
						group.getOperation(),
						group.gangs().get(0).getFieldCodec());
				for (var gang : group.gangs()) {
					for (var entry : gang.entries()) {
						section.getRelocations().add((int) entry.getOffset(), symbolMap.get(group.getTarget()), type);
					}
				}
			}
		}

		return coff;
	}

	private static byte mapType(ImageSymbol.Type type) {
		return switch (type) {
			case NOTYPE -> 0x00;
			case OBJECT -> 0x01;
			case FUNCTION -> 0x20;
			case SECTION -> 0x00;
			case FILE -> 0x00;
			case TLS -> 0x00;
		};
	}

	private static CoffSectionFlags mapSectionFlags(ImageSection section) {
		CoffSectionFlags flags = new CoffSectionFlags();
		if (section.hasRuntimeFlag(ImageSectionRuntimeFlags.EXECUTE)) {
			flags.memExecute();
		}
		if (section.hasRuntimeFlag(ImageSectionRuntimeFlags.READ)) {
			flags.memRead();
		}
		if (section.hasRuntimeFlag(ImageSectionRuntimeFlags.WRITE)) {
			flags.memWrite();
		}
		if (section.getAlignment() > 0) {
			flags.alignBytes((int) section.getAlignment());
		}
		return flags;
	}

	private static void applyDataKindFlags(ImageSection section, CoffSectionFlags flags) {
		if (section.hasLinktimeFlag(ImageSectionLinktimeFlags.CODE)) {
			flags.cntCode();
			return;
		}

		if ((section.getContents().length == 0) && (section.getLogicalSize() > 0)) {
			flags.cntUninitializedData();
			return;
		}

		if (section.getContents().length > 0) {
			flags.cntInitializedData();
		}
	}
}
