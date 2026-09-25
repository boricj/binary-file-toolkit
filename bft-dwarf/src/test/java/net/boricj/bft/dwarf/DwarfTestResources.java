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
package net.boricj.bft.dwarf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteOrder;
import java.util.Objects;

import net.boricj.bft.dwarf.constants.DwarfVersion;
import net.boricj.bft.dwarf.model.DwarfAbbreviationTable;
import net.boricj.bft.elf.ElfFile;
import net.boricj.bft.elf.ElfSection;
import net.boricj.bft.elf.sections.ElfProgBits;

final class DwarfTestResources {
	enum FixtureTarget {
		X86_64_LINUX_GNU("x86_64-linux-gnu"),
		MIPS_LINUX_GNU("mips-linux-gnu");

		private final String suffix;

		FixtureTarget(String suffix) {
			this.suffix = suffix;
		}

		String suffix() {
			return suffix;
		}
	}

	private DwarfTestResources() {
		// Utility class - prevent instantiation
	}

	static DwarfFixture fixture(FixtureTarget target, int version) throws IOException {
		DwarfVersion dwarfVersion = DwarfVersion.valueFrom(version);
		ElfFile elfFile = loadElf(target, version);
		ByteOrder byteOrder =
				switch (elfFile.getHeader().getIdentData()) {
					case ELFDATA2LSB -> ByteOrder.LITTLE_ENDIAN;
					case ELFDATA2MSB -> ByteOrder.BIG_ENDIAN;
					default -> throw new IllegalStateException("Unsupported ELF data encoding");
				};
		byte[] debugAranges = getRequiredSectionBytes(elfFile, ".debug_aranges");
		byte[] debugAbbrev = getRequiredSectionBytes(elfFile, ".debug_abbrev");
		byte[] debugInfo = getRequiredSectionBytes(elfFile, ".debug_info");
		byte[] debugLine = getRequiredSectionBytes(elfFile, ".debug_line");
		byte[] debugStr = getRequiredSectionBytes(elfFile, ".debug_str");
		byte[] debugLineStr = version >= 5 ? getOptionalSectionBytes(elfFile, ".debug_line_str") : null;

		DwarfAbbreviationTable abbreviationTable =
				DwarfAbbreviationParser.parseFirstTable(debugAbbrev, dwarfVersion, byteOrder);
		DwarfStringTable debugStrings = DwarfStringTable.parse(debugStr);
		DwarfStringTable debugLineStrings = debugLineStr != null ? DwarfStringTable.parse(debugLineStr) : null;
		DwarfDebugInfo debugInfoSection =
				DwarfDebugInfo.parse(debugInfo, byteOrder, abbreviationTable, debugStrings, debugLineStrings);
		DwarfLineInfo debugLineSection =
				DwarfLineInfo.parse(debugLine, byteOrder, debugLineStrings, commonAddressSize(debugInfoSection));
		DwarfAranges debugArangesSection = DwarfAranges.parse(debugAranges, byteOrder);

		return new DwarfFixture(
				target,
				byteOrder,
				dwarfVersion,
				debugAranges,
				debugAbbrev,
				debugInfo,
				debugLine,
				debugStr,
				debugLineStr,
				abbreviationTable,
				debugArangesSection,
				debugStrings,
				debugLineStrings,
				debugInfoSection,
				debugLineSection);
	}

	private static int commonAddressSize(DwarfDebugInfo debugInfo) {
		int addressSize = debugInfo.getCompilationUnits().getFirst().getHeader().addressSize();
		boolean mixedAddressSizes = debugInfo.getCompilationUnits().stream()
				.anyMatch(unit -> unit.getHeader().addressSize() != addressSize);
		if (mixedAddressSizes) {
			throw new IllegalArgumentException("Fixture compilation units use different address sizes");
		}
		return addressSize;
	}

	private static ElfFile loadElf(FixtureTarget target, int version) throws IOException {
		String resourcePath = "/net/boricj/bft/dwarf/ascii-table-" + target.suffix() + "-dwarf" + version + ".elf";
		try {
			File file = new File(Objects.requireNonNull(DwarfTestResources.class.getResource(resourcePath))
					.toURI());
			try (FileInputStream inputStream = new FileInputStream(file)) {
				return new ElfFile.Parser(inputStream)
						.setIgnoreSectionErrors(true)
						.parse();
			}
		} catch (URISyntaxException ex) {
			throw new IOException("Missing resource: " + resourcePath, ex);
		}
	}

	private static byte[] getRequiredSectionBytes(ElfFile elfFile, String sectionName) {
		byte[] bytes = getOptionalSectionBytes(elfFile, sectionName);
		if (bytes == null) {
			throw new IllegalStateException("Missing section: " + sectionName);
		}
		return bytes;
	}

	private static byte[] getOptionalSectionBytes(ElfFile elfFile, String sectionName) {
		ElfSection section = elfFile.getSections().stream()
				.filter(Objects::nonNull)
				.filter(s -> sectionName.equals(s.getName()))
				.findFirst()
				.orElse(null);
		if (section == null) {
			return null;
		}
		if (!(section instanceof ElfProgBits)) {
			throw new IllegalStateException("Section is not SHT_PROGBITS: " + sectionName);
		}
		return ((ElfProgBits) section).getBytes();
	}

	record DwarfFixture(
			FixtureTarget target,
			ByteOrder byteOrder,
			DwarfVersion version,
			byte[] debugArangesBytes,
			byte[] debugAbbrevBytes,
			byte[] debugInfoBytes,
			byte[] debugLineBytes,
			byte[] debugStrBytes,
			byte[] debugLineStrBytes,
			DwarfAbbreviationTable abbreviationTable,
			DwarfAranges debugAranges,
			DwarfStringTable debugStrings,
			DwarfStringTable debugLineStrings,
			DwarfDebugInfo debugInfo,
			DwarfLineInfo debugLine) {
		DwarfFixture {
			Objects.requireNonNull(target);
			Objects.requireNonNull(byteOrder);
			Objects.requireNonNull(version);
			Objects.requireNonNull(debugArangesBytes);
			Objects.requireNonNull(debugAbbrevBytes);
			Objects.requireNonNull(debugInfoBytes);
			Objects.requireNonNull(debugLineBytes);
			Objects.requireNonNull(debugStrBytes);
			Objects.requireNonNull(abbreviationTable);
			Objects.requireNonNull(debugAranges);
			Objects.requireNonNull(debugStrings);
			Objects.requireNonNull(debugInfo);
		}
	}
}
