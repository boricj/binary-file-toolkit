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

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Objects;

import net.boricj.bft.dwarf.constants.DwarfVersion;
import net.boricj.bft.dwarf.model.DwarfAbbreviationTable;

/**
 * Aggregates the modeled DWARF debug sections for one artifact.
 */
public final class DwarfFile {
	private final ByteOrder byteOrder;
	private final DwarfAranges debugAranges;
	private final DwarfStringTable debugStrings;
	private final DwarfStringTable debugLineStrings;
	private final DwarfLineInfo debugLine;
	private final DwarfDebugInfo debugInfo;

	/**
	 * Creates a DWARF file view from already parsed section models.
	 *
	 * @param byteOrder shared byte order for all sections
	 * @param debugAranges .debug_aranges section model
	 * @param debugStrings .debug_str section model, or null
	 * @param debugLineStrings .debug_line_str section model, or null
	 * @param debugLine .debug_line section model
	 * @param debugInfo .debug_info section model
	 */
	public DwarfFile(
			ByteOrder byteOrder,
			DwarfAranges debugAranges,
			DwarfStringTable debugStrings,
			DwarfStringTable debugLineStrings,
			DwarfLineInfo debugLine,
			DwarfDebugInfo debugInfo) {
		this.byteOrder = Objects.requireNonNull(byteOrder);
		this.debugAranges = Objects.requireNonNull(debugAranges);
		this.debugStrings = debugStrings;
		this.debugLineStrings = debugLineStrings;
		this.debugLine = Objects.requireNonNull(debugLine);
		this.debugInfo = Objects.requireNonNull(debugInfo);
		for (var unit : debugInfo.getCompilationUnits()) {
			DwarfAbbreviationValidator.validateSupportedIn(
					debugInfo.getAbbreviationTable(), unit.getHeader().version());
		}
	}

	/**
	 * Returns the byte order used by all sections.
	 *
	 * @return DWARF byte order
	 */
	public ByteOrder getByteOrder() {
		return byteOrder;
	}

	/**
	 * Returns the optional .debug_str table.
	 *
	 * @return .debug_str table or null
	 */
	public DwarfStringTable getDebugStrings() {
		return debugStrings;
	}

	/**
	 * Returns the .debug_aranges section model.
	 *
	 * @return .debug_aranges model
	 */
	public DwarfAranges getDebugAranges() {
		return debugAranges;
	}

	/**
	 * Returns the optional .debug_line_str table.
	 *
	 * @return .debug_line_str table or null
	 */
	public DwarfStringTable getDebugLineStrings() {
		return debugLineStrings;
	}

	/**
	 * Returns the .debug_line section model.
	 *
	 * @return .debug_line model
	 */
	public DwarfLineInfo getDebugLine() {
		return debugLine;
	}

	/**
	 * Returns the .debug_info section model.
	 *
	 * @return .debug_info model
	 */
	public DwarfDebugInfo getDebugInfo() {
		return debugInfo;
	}

	/**
	 * Serializes .debug_abbrev bytes from the abbreviation table used by .debug_info.
	 *
	 * @return serialized .debug_abbrev bytes
	 * @throws IOException if serialization fails
	 */
	public byte[] writeDebugAbbrev() throws IOException {
		return debugInfo.getAbbreviationTable().toByteArray();
	}

	/**
	 * Serializes the .debug_aranges section.
	 *
	 * @return serialized .debug_aranges bytes
	 * @throws IOException if serialization fails
	 */
	public byte[] writeDebugAranges() throws IOException {
		return debugAranges.toByteArray();
	}

	/**
	 * Serializes the .debug_info section.
	 *
	 * @return serialized .debug_info bytes
	 * @throws IOException if serialization fails
	 */
	public byte[] writeDebugInfo() throws IOException {
		return debugInfo.toByteArray();
	}

	/**
	 * Serializes the optional .debug_str section.
	 *
	 * @return serialized .debug_str bytes, or an empty array when absent
	 * @throws IOException if serialization fails
	 */
	public byte[] writeDebugStr() throws IOException {
		return debugStrings != null ? debugStrings.toByteArray() : new byte[0];
	}

	/**
	 * Serializes the optional .debug_line_str section.
	 *
	 * @return serialized .debug_line_str bytes, or an empty array when absent
	 * @throws IOException if serialization fails
	 */
	public byte[] writeDebugLineStr() throws IOException {
		return debugLineStrings != null ? debugLineStrings.toByteArray() : new byte[0];
	}

	/**
	 * Serializes the .debug_line section.
	 *
	 * @return serialized .debug_line bytes
	 * @throws IOException if serialization fails
	 */
	public byte[] writeDebugLine() throws IOException {
		return debugLine.toByteArray();
	}

	/**
	 * Fluent builder for assembling a {@link DwarfFile} from individual section models.
	 */
	public static class Builder {
		private final ByteOrder byteOrder;
		private DwarfAranges debugAranges;
		private DwarfStringTable debugStrings;
		private DwarfStringTable debugLineStrings;
		private DwarfLineInfo debugLine;
		private DwarfDebugInfo debugInfo;

		/**
		 * Creates a builder for the provided DWARF byte order.
		 *
		 * @param byteOrder DWARF byte order shared by all sections
		 */
		public Builder(ByteOrder byteOrder) {
			this.byteOrder = Objects.requireNonNull(byteOrder);
		}

		/**
		 * Sets the .debug_aranges section model.
		 *
		 * @param debugAranges .debug_aranges model
		 * @return this builder
		 */
		public Builder setDebugAranges(DwarfAranges debugAranges) {
			this.debugAranges = debugAranges;
			return this;
		}

		/**
		 * Sets the optional .debug_str section model.
		 *
		 * @param debugStrings .debug_str model or null
		 * @return this builder
		 */
		public Builder setDebugStrings(DwarfStringTable debugStrings) {
			this.debugStrings = debugStrings;
			return this;
		}

		/**
		 * Sets the optional .debug_line_str section model.
		 *
		 * @param debugLineStrings .debug_line_str model or null
		 * @return this builder
		 */
		public Builder setDebugLineStrings(DwarfStringTable debugLineStrings) {
			this.debugLineStrings = debugLineStrings;
			return this;
		}

		/**
		 * Sets the .debug_line section model.
		 *
		 * @param debugLine .debug_line model
		 * @return this builder
		 */
		public Builder setDebugLine(DwarfLineInfo debugLine) {
			this.debugLine = debugLine;
			return this;
		}

		/**
		 * Sets the .debug_info section model.
		 *
		 * @param debugInfo .debug_info model
		 * @return this builder
		 */
		public Builder setDebugInfo(DwarfDebugInfo debugInfo) {
			this.debugInfo = debugInfo;
			return this;
		}

		/**
		 * Builds a validated DWARF file aggregate.
		 *
		 * @return assembled DWARF file
		 */
		public DwarfFile build() {
			if (debugInfo == null) {
				throw new IllegalStateException("debugInfo must be provided");
			}
			if (debugAranges == null) {
				throw new IllegalStateException("debugAranges must be provided");
			}
			if (debugLine == null) {
				throw new IllegalStateException("debugLine must be provided");
			}
			return new DwarfFile(byteOrder, debugAranges, debugStrings, debugLineStrings, debugLine, debugInfo);
		}
	}

	/**
	 * Parser that reconstructs a {@link DwarfFile} from raw DWARF section bytes.
	 */
	public static class Parser {
		private final ByteOrder byteOrder;
		private byte[] debugAranges;
		private final byte[] debugAbbrev;
		private final byte[] debugInfo;
		private byte[] debugStr;
		private byte[] debugLineStr;
		private byte[] debugLine;

		/**
		 * Creates a parser with required .debug_abbrev and .debug_info payloads.
		 *
		 * @param byteOrder byte order used by encoded DWARF sections
		 * @param debugAbbrev required .debug_abbrev bytes
		 * @param debugInfo required .debug_info bytes
		 */
		public Parser(ByteOrder byteOrder, byte[] debugAbbrev, byte[] debugInfo) {
			this.byteOrder = Objects.requireNonNull(byteOrder);
			this.debugAbbrev = Objects.requireNonNull(debugAbbrev);
			this.debugInfo = Objects.requireNonNull(debugInfo);
		}

		/**
		 * Supplies optional .debug_aranges bytes.
		 *
		 * @param debugAranges .debug_aranges bytes
		 * @return this parser
		 */
		public Parser setDebugAranges(byte[] debugAranges) {
			this.debugAranges = debugAranges;
			return this;
		}

		/**
		 * Supplies optional .debug_str bytes.
		 *
		 * @param debugStr .debug_str bytes
		 * @return this parser
		 */
		public Parser setDebugStr(byte[] debugStr) {
			this.debugStr = debugStr;
			return this;
		}

		/**
		 * Supplies optional .debug_line_str bytes.
		 *
		 * @param debugLineStr .debug_line_str bytes
		 * @return this parser
		 */
		public Parser setDebugLineStr(byte[] debugLineStr) {
			this.debugLineStr = debugLineStr;
			return this;
		}

		/**
		 * Supplies optional .debug_line bytes.
		 *
		 * @param debugLine .debug_line bytes
		 * @return this parser
		 */
		public Parser setDebugLine(byte[] debugLine) {
			this.debugLine = debugLine;
			return this;
		}

		/**
		 * Parses configured sections into a validated {@link DwarfFile}.
		 *
		 * @return parsed DWARF file model
		 * @throws IOException if any section cannot be parsed
		 */
		public DwarfFile parse() throws IOException {
			java.util.List<DwarfVersion> versions = DwarfInfoProbe.readCompilationUnitVersions(debugInfo, byteOrder);
			if (versions.isEmpty()) {
				throw new IllegalArgumentException(".debug_info does not contain any compilation units");
			}
			DwarfVersion version = versions.getFirst();
			boolean mixedVersions = versions.stream().anyMatch(unitVersion -> unitVersion != version);
			if (mixedVersions) {
				throw new IllegalArgumentException(
						"Compilation units using different DWARF versions are not supported in one DwarfFile");
			}
			DwarfAranges aranges = debugAranges != null
					? DwarfAranges.parse(debugAranges, byteOrder)
					: new DwarfAranges(byteOrder, java.util.List.of());
			DwarfStringTable debugStrings = debugStr != null ? DwarfStringTable.parse(debugStr) : null;
			DwarfStringTable debugLineStrings = debugLineStr != null ? DwarfStringTable.parse(debugLineStr) : null;
			DwarfAbbreviationTable abbreviationTable =
					DwarfAbbreviationParser.parseFirstTable(debugAbbrev, version, byteOrder);
			DwarfDebugInfo info =
					DwarfDebugInfo.parse(debugInfo, byteOrder, abbreviationTable, debugStrings, debugLineStrings);
			int lineAddressSize = inferLineAddressSize(info);
			DwarfLineInfo line = debugLine != null
					? DwarfLineInfo.parse(debugLine, byteOrder, debugLineStrings, lineAddressSize)
					: new DwarfLineInfo(byteOrder, java.util.List.of());
			return new DwarfFile(byteOrder, aranges, debugStrings, debugLineStrings, line, info);
		}

		private static int inferLineAddressSize(DwarfDebugInfo debugInfo) {
			if (debugInfo.getCompilationUnits().isEmpty()) {
				throw new IllegalArgumentException(".debug_info does not contain any compilation units");
			}
			int addressSize =
					debugInfo.getCompilationUnits().getFirst().getHeader().addressSize();
			boolean mixedAddressSizes = debugInfo.getCompilationUnits().stream()
					.anyMatch(unit -> unit.getHeader().addressSize() != addressSize);
			if (mixedAddressSizes) {
				throw new IllegalArgumentException(
						"Compilation units using different address sizes are not supported in one DwarfFile");
			}
			return addressSize;
		}
	}
}
