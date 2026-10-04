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
import net.boricj.bft.coff.constants.CoffMachine;
import net.boricj.bft.coff.constants.CoffRelocationType;
import net.boricj.bft.coff.constants.CoffSectionFlags;
import net.boricj.bft.coff.sections.CoffBytes;
import net.boricj.bft.coff.sections.CoffUninitialized;
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
 * Exports semantic images into native COFF object files.
 */
public final class CoffExporter implements ImageExporter<CoffFile> {
	private final ImageFile image;
	private final CoffFile.Builder builder;
	private final CoffRelocationCatalogRegistry relocationCatalogRegistry;

	/**
	 * Creates an exporter that serializes a semantic image into a native COFF object.
	 *
	 * @param image the semantic image to export
	 * @param builder the COFF file builder that receives the generated object content
	 * @param relocationCatalogRegistry relocation catalog registry used for machine-specific mapping
	 */
	public CoffExporter(
			ImageFile image, CoffFile.Builder builder, CoffRelocationCatalogRegistry relocationCatalogRegistry) {
		this.image = Objects.requireNonNull(image, "image");
		this.builder = Objects.requireNonNull(builder, "builder");
		this.relocationCatalogRegistry = Objects.requireNonNull(relocationCatalogRegistry, "relocationCatalogRegistry");
	}

	@Override
	public CoffFile exportFile(Consumer<String> logSink) {
		CoffFile coff = builder.build();
		validateExportPreconditions(coff);
		CoffSectionTable sections = coff.getSections();
		Map<ImageSection, net.boricj.bft.coff.CoffSection> sectionMap = new IdentityHashMap<>();
		Map<ImageSection, byte[]> emittedSectionBytes = new IdentityHashMap<>();
		Map<ImageSymbol, CoffSymbol> symbolMap = new IdentityHashMap<>();

		for (ImageSection imageSection : this.image.sections()) {
			CoffSectionFlags flags = mapSectionFlags(imageSection, logSink);
			applyDataKindFlags(imageSection, flags);
			byte[] sectionBytes = imageSection.getBytes().clone();
			net.boricj.bft.coff.CoffSection section = new CoffBytes(coff, imageSection.getName(), flags, sectionBytes);
			if ((imageSection.getBytes().length == 0) && (imageSection.getLogicalSize() > 0)) {
				section =
						new CoffUninitialized(coff, imageSection.getName(), flags, (int) imageSection.getLogicalSize());
			} else {
				emittedSectionBytes.put(imageSection, sectionBytes);
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
			} else if (imageSymbol.getType() == ImageSymbol.Type.FUNCTION
					&& imageSymbol.getSection() != null
					&& imageSymbol.getSize() > 0) {
				symbol = symbols.addFunction(
						imageSymbol.getName(),
						(int) imageSymbol.getOffset(),
						sectionMap.get(imageSymbol.getSection()),
						CoffSymbolMappings.mapStorageClass(imageSymbol),
						coffFunctionSize(imageSymbol));
			} else if (imageSymbol.getSection() == null) {
				if (imageSymbol.getBinding() == ImageSymbol.Binding.LOCAL) {
					symbol = symbols.addAbsolute(imageSymbol.getName(), (int) imageSymbol.getOffset());
				} else {
					symbol = symbols.addUndefined(
							imageSymbol.getName(), CoffSymbolMappings.mapImageTypeToCoffType(imageSymbol.getType()));
				}
			} else {
				symbol = symbols.addSymbol(
						imageSymbol.getName(),
						(int) imageSymbol.getOffset(),
						sectionMap.get(imageSymbol.getSection()),
						CoffSymbolMappings.mapImageTypeToCoffType(imageSymbol.getType()),
						CoffSymbolMappings.mapStorageClass(imageSymbol));
			}
			symbolMap.put(imageSymbol, symbol);
		}

		for (ImageSection imageSection : this.image.sections()) {
			var section = sectionMap.get(imageSection);
			byte[] emittedBytes = emittedSectionBytes.get(imageSection);
			for (var group : imageSection.relocations()) {
				var targetSymbol = symbolMap.get(group.getTarget());
				if (targetSymbol == null) {
					throw new IllegalStateException("missing exported symbol for relocation target");
				}
				for (var gang : group.gangs()) {
					for (var entry : gang.entries()) {
						if (emittedBytes != null) {
							long inlineAddend = inlineAddendForEmission(
									coff.getHeader().getMachine(),
									group.getOperation(),
									gang.getFieldCodec(),
									group.getAddend(),
									entry.getLocalAddend(),
									entry.getAddend(),
									imageSection,
									entry,
									logSink);
							gang.getFieldCodec().write(emittedBytes, (int) entry.getOffset(), inlineAddend);
						}
						var type = mapRelocationType(coff.getHeader().getMachine(), entry);
						section.getRelocations().add((int) entry.getOffset(), targetSymbol, type);
					}
				}
			}
		}

		return coff;
	}

	private static long inlineAddendForEmission(
			CoffMachine machine,
			net.boricj.bft.image.RelocationOperation operation,
			net.boricj.bft.image.RelocationFieldCodec fieldCodec,
			long groupAddend,
			long localAddend,
			long effectiveAddend,
			ImageSection imageSection,
			net.boricj.bft.image.ImageRelocationEntry entry,
			Consumer<String> logSink) {
		boolean isAmd64Rel32Family = machine == CoffMachine.IMAGE_FILE_MACHINE_AMD64
				&& operation == net.boricj.bft.image.RelocationOperation.PC_RELATIVE
				&& fieldCodec == net.boricj.bft.image.relocs.priv.ByteFieldCodec.S32LE;
		if (!isAmd64Rel32Family) {
			return effectiveAddend;
		}
		if (localAddend >= 0 && localAddend <= 5) {
			return groupAddend;
		}
		if (logSink != null) {
			logSink.accept(String.format(
					"AMD64 REL32 local addend %d at section '%s' offset 0x%x is not representable as REL32_n; using REL32 fallback and emitting effective inline addend.",
					localAddend, imageSection.getName(), entry.getOffset()));
		}
		return effectiveAddend;
	}

	private CoffRelocationType mapRelocationType(CoffMachine machine, net.boricj.bft.image.ImageRelocationEntry entry) {
		CoffRelocationCatalog relocationCatalog = this.relocationCatalogRegistry.catalogFor(machine);
		if (relocationCatalog == null) {
			throw new IllegalArgumentException(String.format("Unsupported COFF machine: %s", machine));
		}
		CoffRelocationType type = relocationCatalog.typeFromEntry(entry);
		if (type == null) {
			throw new IllegalArgumentException(String.format(
					"No COFF relocation type mapping for operation '%s' and codec '%s' on machine %s",
					entry.getOperation(), entry.getFieldCodec(), machine));
		}
		return type;
	}

	private void validateExportPreconditions(CoffFile coff) {
		if (this.image.getKind() != ImageFile.Kind.OBJECT) {
			throw new IllegalArgumentException("COFF exporter only supports object images");
		}
		if (coff.getHeader().getMachine() == CoffMachine.IMAGE_FILE_MACHINE_UNKNOWN) {
			throw new IllegalArgumentException("COFF machine must be configured before export");
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
					throw new IllegalArgumentException("COFF relocation target cannot be null");
				}
				if (!symbols.containsKey(relocation.getTarget())) {
					throw new IllegalArgumentException("relocation target must belong to image symbol table");
				}
				for (var gang : relocation.gangs()) {
					for (var entry : gang.entries()) {
						long localAddend = entry.getLocalAddend();
						boolean isAmd64Rel32Family = coff.getHeader().getMachine()
										== CoffMachine.IMAGE_FILE_MACHINE_AMD64
								&& relocation.getOperation() == net.boricj.bft.image.RelocationOperation.PC_RELATIVE
								&& gang.getFieldCodec() == net.boricj.bft.image.relocs.priv.ByteFieldCodec.S32LE;
						if (!isAmd64Rel32Family && localAddend != 0) {
							throw new IllegalArgumentException("COFF exporter requires zero local entry addends");
						}
					}
				}
			}
		}
	}

	private static int coffFunctionSize(ImageSymbol symbol) {
		long size = symbol.getSize();
		if (size <= 0) {
			return 0;
		}
		if (size > 0xFFFF_FFFFL) {
			return 0xFFFF_FFFF;
		}
		return (int) size;
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

	private static CoffSectionFlags mapSectionFlags(ImageSection section, Consumer<String> logSink) {
		CoffSectionFlags flags = new CoffSectionFlags();
		java.util.List<String> losses = new java.util.ArrayList<>();
		if (section.hasRuntimeFlag(ImageSectionRuntimeFlags.EXECUTE)) {
			flags.memExecute();
		}
		if (section.hasRuntimeFlag(ImageSectionRuntimeFlags.READ)) {
			flags.memRead();
		}
		if (section.hasRuntimeFlag(ImageSectionRuntimeFlags.WRITE)) {
			flags.memWrite();
		}
		if (section.hasLinktimeFlag(ImageSectionLinktimeFlags.INFO)) {
			flags.lnkInfo();
		}
		if (section.hasLinktimeFlag(ImageSectionLinktimeFlags.REMOVE)) {
			flags.lnkRemove();
		}
		if (section.hasLinktimeFlag(ImageSectionLinktimeFlags.DISCARDABLE)) {
			flags.memDiscardable();
		}
		if (section.hasLinktimeFlag(ImageSectionLinktimeFlags.COMMENT)) {
			losses.add("COMMENT");
		}
		if (section.getType() != ImageSectionType.UNALLOCATED
				&& !section.hasRuntimeFlag(ImageSectionRuntimeFlags.READ)
				&& !section.hasRuntimeFlag(ImageSectionRuntimeFlags.WRITE)
				&& !section.hasRuntimeFlag(ImageSectionRuntimeFlags.EXECUTE)) {
			// Preserve allocatable intent in COFF even when no explicit runtime permission was set.
			flags.memRead();
		}
		if (section.getAlignment() > 0) {
			flags.alignBytes((int) section.getAlignment());
		}
		if (logSink != null && !losses.isEmpty()) {
			logSink.accept(String.format(
					"COFF export for section '%s' drops non-portable section metadata [%s]; exported flags are %s.",
					section.getName(), String.join(",", losses), flags));
		}
		return flags;
	}

	private static void applyDataKindFlags(ImageSection section, CoffSectionFlags flags) {
		if (section.hasLinktimeFlag(ImageSectionLinktimeFlags.INFO)
				|| section.hasLinktimeFlag(ImageSectionLinktimeFlags.REMOVE)) {
			return;
		}
		if (section.getType() == ImageSectionType.UNINITIALIZED) {
			flags.cntUninitializedData();
			return;
		}

		if (section.getType() == ImageSectionType.INITIALIZED) {
			if (section.hasRuntimeFlag(ImageSectionRuntimeFlags.EXECUTE)) {
				flags.cntCode();
			} else {
				flags.cntInitializedData();
			}
		}
	}
}
