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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.dwarf.constants.DwarfForm;
import net.boricj.bft.dwarf.constants.DwarfLineContentType;
import net.boricj.bft.dwarf.constants.DwarfVersion;
import net.boricj.bft.dwarf.model.DwarfBlockValue;
import net.boricj.bft.dwarf.model.DwarfStringReferenceValue;
import net.boricj.bft.dwarf.model.DwarfStringValue;
import net.boricj.bft.dwarf.model.DwarfUnsignedValue;
import net.boricj.bft.dwarf.model.DwarfValue;

/**
 * Semantic representation of a DWARF .debug_line section with exact reserialization support.
 */
public final class DwarfLineInfo {
	private final ByteOrder byteOrder;
	private final List<DwarfLineUnit> units;

	/**
	 * Creates a semantic .debug_line model.
	 *
	 * @param byteOrder section byte order
	 * @param units parsed line-table units in section order
	 */
	public DwarfLineInfo(ByteOrder byteOrder, List<DwarfLineUnit> units) {
		this.byteOrder = Objects.requireNonNull(byteOrder);
		this.units = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(units)));
	}

	/**
	 * Returns byte order used for line-table encoding.
	 *
	 * @return section byte order
	 */
	public ByteOrder getByteOrder() {
		return byteOrder;
	}

	/**
	 * Returns parsed line-table units.
	 *
	 * @return immutable list of line-table units
	 */
	public List<DwarfLineUnit> getUnits() {
		return units;
	}

	/**
	 * Parses .debug_line bytes.
	 *
	 * @param bytes section bytes
	 * @param byteOrder section byte order
	 * @param debugLineStrings optional .debug_line_str table for DW_FORM_line_strp values
	 * @return parsed line-table model
	 * @throws IOException if parsing fails
	 */
	public static DwarfLineInfo parse(byte[] bytes, ByteOrder byteOrder, DwarfStringTable debugLineStrings)
			throws IOException {
		return parse(bytes, byteOrder, debugLineStrings, 0);
	}

	/**
	 * Parses .debug_line bytes with an explicit fallback address size for pre-DWARF5 units.
	 *
	 * @param bytes section bytes
	 * @param byteOrder section byte order
	 * @param debugLineStrings optional .debug_line_str table for DW_FORM_line_strp values
	 * @param defaultAddressSize address size used when units do not encode one explicitly
	 * @return parsed line-table model
	 * @throws IOException if parsing fails
	 */
	public static DwarfLineInfo parse(
			byte[] bytes, ByteOrder byteOrder, DwarfStringTable debugLineStrings, int defaultAddressSize)
			throws IOException {
		Objects.requireNonNull(bytes);
		Objects.requireNonNull(byteOrder);

		ByteInputStream bis = new ByteInputStream(new ByteArrayInputStream(bytes), byteOrder);
		List<DwarfLineUnit> units = new ArrayList<>();
		while (bis.available() > 0) {
			long sectionOffset = bis.getCount();
			long initialLength = Integer.toUnsignedLong(bis.readInt());
			boolean dwarf64 = initialLength == 0xffff_ffffL;
			long unitLength = dwarf64 ? bis.readLong() : initialLength;
			byte[] unitBytes = new byte[requireIntLength(unitLength, ".debug_line unit length")];
			bis.readFully(unitBytes);
			units.add(parseUnit(sectionOffset, unitBytes, byteOrder, dwarf64, debugLineStrings, defaultAddressSize));
		}
		return new DwarfLineInfo(byteOrder, units);
	}

	private static DwarfLineUnit parseUnit(
			long sectionOffset,
			byte[] unitBytes,
			ByteOrder byteOrder,
			boolean dwarf64,
			DwarfStringTable debugLineStrings,
			int defaultAddressSize)
			throws IOException {
		ByteInputStream bis = new ByteInputStream(new ByteArrayInputStream(unitBytes), byteOrder);
		DwarfVersion version = DwarfVersion.valueFrom(bis.readUnsignedShort());
		int addressSize = 0;
		int segmentSelectorSize = 0;
		if (version.isAtLeast(DwarfVersion.DWARF5)) {
			addressSize = bis.readUnsignedByte();
			segmentSelectorSize = bis.readUnsignedByte();
		}
		long prologueLength = dwarf64 ? bis.readLong() : Integer.toUnsignedLong(bis.readInt());
		byte[] prologueBytes = new byte[requireIntLength(prologueLength, ".debug_line prologue length")];
		bis.readFully(prologueBytes);
		ByteInputStream prologue = new ByteInputStream(new ByteArrayInputStream(prologueBytes), byteOrder);

		int minimumInstructionLength = prologue.readUnsignedByte();
		int maximumOperationsPerInstruction = version.isAtLeast(DwarfVersion.DWARF4) ? prologue.readUnsignedByte() : 1;
		boolean defaultIsStatement = prologue.readUnsignedByte() != 0;
		int lineBase = prologue.readByte();
		int lineRange = prologue.readUnsignedByte();
		int opcodeBase = prologue.readUnsignedByte();
		List<Integer> standardOpcodeLengths = new ArrayList<>();
		for (int index = 1; index < opcodeBase; index++) {
			standardOpcodeLengths.add(prologue.readUnsignedByte());
		}

		List<DwarfLineEntryFormat> directoryFormats;
		List<DwarfLineDirectoryEntry> directories;
		List<DwarfLineEntryFormat> fileFormats;
		List<DwarfLineFileEntry> files;
		if (version.isAtLeast(DwarfVersion.DWARF5)) {
			directoryFormats = parseEntryFormats(prologue);
			directories = parseDirectoryEntries(prologue, directoryFormats, debugLineStrings, dwarf64);
			fileFormats = parseEntryFormats(prologue);
			files = parseFileEntries(prologue, fileFormats, debugLineStrings, dwarf64);
		} else {
			directoryFormats = List.of();
			directories = parseLegacyDirectories(prologue);
			fileFormats = List.of();
			files = parseLegacyFiles(prologue);
		}

		int effectiveAddressSize = resolveAddressSize(version, addressSize, defaultAddressSize);

		byte[] instructionBytes = new byte[bis.available()];
		bis.readFully(instructionBytes);
		List<DwarfLineInstruction> instructions =
				parseInstructions(instructionBytes, byteOrder, effectiveAddressSize, opcodeBase);
		DwarfLineHeader header = new DwarfLineHeader(
				unitBytes.length,
				dwarf64,
				version,
				effectiveAddressSize,
				segmentSelectorSize,
				prologueLength,
				minimumInstructionLength,
				maximumOperationsPerInstruction,
				defaultIsStatement,
				lineBase,
				lineRange,
				opcodeBase,
				standardOpcodeLengths,
				directoryFormats,
				directories,
				fileFormats,
				files);
		List<DwarfLineRow> rows = decodeRows(header, instructions);
		return new DwarfLineUnit(sectionOffset, header, instructions, rows);
	}

	private static List<DwarfLineEntryFormat> parseEntryFormats(ByteInputStream prologue) throws IOException {
		int count = prologue.readUnsignedByte();
		List<DwarfLineEntryFormat> formats = new ArrayList<>();
		java.util.Set<Long> seenContentTypeCodes = new java.util.HashSet<>();
		for (int index = 0; index < count; index++) {
			long contentTypeCode = Leb128Utils.readUleb128(prologue);
			if (!seenContentTypeCodes.add(contentTypeCode)) {
				throw new IllegalArgumentException("Malformed line-table entry formats: duplicate content type code 0x"
						+ Long.toHexString(contentTypeCode));
			}
			long formCode = Leb128Utils.readUleb128(prologue);
			formats.add(new DwarfLineEntryFormat(
					contentTypeCode, DwarfLineContentType.valueFromOrNull(contentTypeCode), DwarfForm.valueFrom((int)
							formCode)));
		}
		return formats;
	}

	private static List<DwarfLineDirectoryEntry> parseDirectoryEntries(
			ByteInputStream prologue,
			List<DwarfLineEntryFormat> formats,
			DwarfStringTable debugLineStrings,
			boolean dwarf64)
			throws IOException {
		long count = Leb128Utils.readUleb128(prologue);
		List<DwarfLineDirectoryEntry> entries = new ArrayList<>();
		for (int index = 0; index < count; index++) {
			ParsedEntryValues values = parseEntryValues(prologue, formats, debugLineStrings, dwarf64);
			entries.add(new DwarfLineDirectoryEntry(values.values(), values.valuesByContentTypeCode()));
		}
		return entries;
	}

	private static List<DwarfLineFileEntry> parseFileEntries(
			ByteInputStream prologue,
			List<DwarfLineEntryFormat> formats,
			DwarfStringTable debugLineStrings,
			boolean dwarf64)
			throws IOException {
		long count = Leb128Utils.readUleb128(prologue);
		List<DwarfLineFileEntry> entries = new ArrayList<>();
		for (int index = 0; index < count; index++) {
			ParsedEntryValues values = parseEntryValues(prologue, formats, debugLineStrings, dwarf64);
			entries.add(new DwarfLineFileEntry(values.values(), values.valuesByContentTypeCode()));
		}
		return entries;
	}

	private static ParsedEntryValues parseEntryValues(
			ByteInputStream prologue,
			List<DwarfLineEntryFormat> formats,
			DwarfStringTable debugLineStrings,
			boolean dwarf64)
			throws IOException {
		Map<DwarfLineContentType, DwarfValue> values = new LinkedHashMap<>();
		Map<Long, DwarfValue> valuesByContentTypeCode = new LinkedHashMap<>();
		for (DwarfLineEntryFormat format : formats) {
			DwarfValue value =
					switch (format.form()) {
						case DW_FORM_line_strp -> {
							if (debugLineStrings == null) {
								throw new IllegalArgumentException(
										"DW_FORM_line_strp requires a .debug_line_str table");
							}
							long offset = dwarf64 ? prologue.readLong() : Integer.toUnsignedLong(prologue.readInt());
							yield new DwarfStringReferenceValue(offset, debugLineStrings.get(offset), true);
						}
						case DW_FORM_string -> new DwarfStringValue(
								prologue.readNullTerminatedString(StandardCharsets.UTF_8));
						case DW_FORM_udata -> new DwarfUnsignedValue(Leb128Utils.readUleb128(prologue));
						case DW_FORM_data1 -> new DwarfUnsignedValue(prologue.readUnsignedByte());
						case DW_FORM_data2 -> new DwarfUnsignedValue(prologue.readUnsignedShort());
						case DW_FORM_data4 -> new DwarfUnsignedValue(Integer.toUnsignedLong(prologue.readInt()));
						case DW_FORM_data8 -> new DwarfUnsignedValue(prologue.readLong());
						case DW_FORM_data16 -> new DwarfBlockValue(readBytes(prologue, 16));
						default -> throw new IllegalArgumentException(
								"Unsupported line-table entry form: " + format.form());
					};
			valuesByContentTypeCode.put(format.contentTypeCode(), value);
			if (format.contentType() != null) {
				values.put(format.contentType(), value);
			}
		}
		return new ParsedEntryValues(values, valuesByContentTypeCode);
	}

	private static List<DwarfLineDirectoryEntry> parseLegacyDirectories(ByteInputStream prologue) throws IOException {
		List<DwarfLineDirectoryEntry> directories = new ArrayList<>();
		while (true) {
			String value = prologue.readNullTerminatedString(StandardCharsets.UTF_8);
			if (value.isEmpty()) {
				break;
			}
			directories.add(new DwarfLineDirectoryEntry(
					Map.of(DwarfLineContentType.DW_LNCT_path, new DwarfStringValue(value))));
		}
		return directories;
	}

	private static List<DwarfLineFileEntry> parseLegacyFiles(ByteInputStream prologue) throws IOException {
		List<DwarfLineFileEntry> files = new ArrayList<>();
		while (true) {
			String name = prologue.readNullTerminatedString(StandardCharsets.UTF_8);
			if (name.isEmpty()) {
				break;
			}
			long directoryIndex = Leb128Utils.readUleb128(prologue);
			long timestamp = Leb128Utils.readUleb128(prologue);
			long size = Leb128Utils.readUleb128(prologue);
			Map<DwarfLineContentType, DwarfValue> values = new LinkedHashMap<>();
			values.put(DwarfLineContentType.DW_LNCT_path, new DwarfStringValue(name));
			values.put(DwarfLineContentType.DW_LNCT_directory_index, new DwarfUnsignedValue(directoryIndex));
			values.put(DwarfLineContentType.DW_LNCT_timestamp, new DwarfUnsignedValue(timestamp));
			values.put(DwarfLineContentType.DW_LNCT_size, new DwarfUnsignedValue(size));
			files.add(new DwarfLineFileEntry(values));
		}
		return files;
	}

	private static List<DwarfLineInstruction> parseInstructions(
			byte[] bytes, ByteOrder byteOrder, int addressSize, int opcodeBase) throws IOException {
		ByteInputStream bis = new ByteInputStream(new ByteArrayInputStream(bytes), byteOrder);
		List<DwarfLineInstruction> instructions = new ArrayList<>();
		while (bis.available() > 0) {
			int opcode = bis.readUnsignedByte();
			if (opcode == 0) {
				long length = Leb128Utils.readUleb128(bis);
				if (length <= 0) {
					throw new IllegalArgumentException("Extended line opcode must include a sub-opcode byte");
				}
				int payloadLength = requireIntLength(length - 1, "extended line opcode payload length");
				int subOpcode = bis.readUnsignedByte();
				byte[] payload = readBytes(bis, payloadLength);
				instructions.add(parseExtendedInstruction(subOpcode, payload, byteOrder, addressSize));
			} else {
				instructions.add(parseNonExtendedInstruction(opcode, opcodeBase, bis));
			}
		}
		return instructions;
	}

	private static DwarfLineInstruction parseExtendedInstruction(
			int subOpcode, byte[] payload, ByteOrder byteOrder, int addressSize) throws IOException {
		ByteInputStream bis = new ByteInputStream(new ByteArrayInputStream(payload), byteOrder);
		return switch (subOpcode) {
			case 1 -> new DwarfLineEndSequence();
			case 2 -> new DwarfLineSetAddress(readAddress(bis, addressSize));
			case 4 -> new DwarfLineSetDiscriminator(Leb128Utils.readUleb128(bis));
			default -> new DwarfLineUnknownExtendedInstruction(subOpcode, payload);
		};
	}

	private static DwarfLineInstruction parseNonExtendedInstruction(int opcode, int opcodeBase, ByteInputStream bis)
			throws IOException {
		if (opcode >= opcodeBase) {
			return new DwarfLineSpecialOpcode(opcode);
		}
		return switch (opcode) {
			case 1 -> new DwarfLineCopy();
			case 2 -> new DwarfLineAdvancePc(Leb128Utils.readUleb128(bis));
			case 3 -> new DwarfLineAdvanceLine(Leb128Utils.readSleb128(bis));
			case 4 -> new DwarfLineSetFile(Leb128Utils.readUleb128(bis));
			case 5 -> new DwarfLineSetColumn(Leb128Utils.readUleb128(bis));
			case 6 -> new DwarfLineNegateStatement();
			case 7 -> new DwarfLineSetBasicBlock();
			case 8 -> new DwarfLineConstAddPc();
			case 9 -> new DwarfLineFixedAdvancePc(bis.readUnsignedShort());
			case 10 -> new DwarfLineSetPrologueEnd();
			case 11 -> new DwarfLineSetEpilogueBegin();
			case 12 -> new DwarfLineSetIsa(Leb128Utils.readUleb128(bis));
			default -> throw new IllegalArgumentException("Unsupported standard line opcode: " + opcode);
		};
	}

	private static List<DwarfLineRow> decodeRows(DwarfLineHeader header, List<DwarfLineInstruction> instructions) {
		if (header.maximumOperationsPerInstruction() != 1) {
			throw new IllegalArgumentException(
					"Line tables with maximum_operations_per_instruction != 1 are not supported yet");
		}
		if (header.lineRange() == 0) {
			throw new IllegalArgumentException("Malformed line-table header: line_range must be non-zero");
		}
		List<DwarfLineRow> rows = new ArrayList<>();
		DwarfLineState state = new DwarfLineState(header.defaultIsStatement());
		for (DwarfLineInstruction instruction : instructions) {
			instruction.apply(state, header, rows);
		}
		return Collections.unmodifiableList(rows);
	}

	/**
	 * Serializes this .debug_line model to raw bytes.
	 *
	 * @return serialized .debug_line bytes
	 * @throws IOException if serialization fails
	 */
	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		write(baos);
		return baos.toByteArray();
	}

	/**
	 * Writes this .debug_line model to the provided output stream.
	 *
	 * @param outputStream destination stream
	 * @throws IOException if writing fails
	 */
	public void write(OutputStream outputStream) throws IOException {
		ByteOutputStream bos = new ByteOutputStream(outputStream, byteOrder);
		for (DwarfLineUnit unit : units) {
			byte[] body = unit.toUnitBody(byteOrder);
			if (unit.header().dwarf64()) {
				bos.writeInt(0xffff_ffff);
				bos.writeLong(body.length);
			} else {
				bos.writeInt(body.length);
			}
			bos.write(body);
		}
	}

	private static byte[] readBytes(ByteInputStream bis, int length) throws IOException {
		byte[] bytes = new byte[length];
		bis.readFully(bytes);
		return bytes;
	}

	private static long readAddress(ByteInputStream bis, int addressSize) throws IOException {
		return switch (addressSize) {
			case 1 -> bis.readUnsignedByte();
			case 2 -> bis.readUnsignedShort();
			case 4 -> Integer.toUnsignedLong(bis.readInt());
			case 8 -> bis.readLong();
			default -> throw new IllegalArgumentException("Unsupported DWARF address size: " + addressSize);
		};
	}

	private static int resolveAddressSize(DwarfVersion version, int encodedAddressSize, int defaultAddressSize) {
		if (version.isAtLeast(DwarfVersion.DWARF5)) {
			return encodedAddressSize;
		}
		if (defaultAddressSize <= 0) {
			throw new IllegalArgumentException(
					"A default address size is required to parse pre-DWARF5 .debug_line units");
		}
		return defaultAddressSize;
	}

	/**
	 * One line-table unit from .debug_line.
	 *
	 * @param sectionOffset section-relative offset of the unit
	 * @param header parsed unit header and prologue metadata
	 * @param instructions decoded opcode stream in encounter order
	 * @param rows decoded logical line rows derived from the instruction stream
	 */
	public record DwarfLineUnit(
			long sectionOffset,
			DwarfLineHeader header,
			List<DwarfLineInstruction> instructions,
			List<DwarfLineRow> rows) {
		/**
		 * Creates an immutable line-table unit snapshot.
		 *
		 * @param sectionOffset section-relative offset of the unit
		 * @param header parsed unit header
		 * @param instructions decoded opcode stream
		 * @param rows decoded line rows
		 */
		public DwarfLineUnit {
			instructions = Collections.unmodifiableList(new ArrayList<>(instructions));
			rows = Collections.unmodifiableList(new ArrayList<>(rows));
		}

		byte[] toUnitBody(ByteOrder byteOrder) throws IOException {
			ByteArrayOutputStream prologueBaos = new ByteArrayOutputStream();
			ByteOutputStream prologueBos = new ByteOutputStream(prologueBaos, byteOrder);
			prologueBos.writeByte(header.minimumInstructionLength());
			if (header.version().isAtLeast(DwarfVersion.DWARF4)) {
				prologueBos.writeByte(header.maximumOperationsPerInstruction());
			}
			prologueBos.writeByte(header.defaultIsStatement() ? 1 : 0);
			prologueBos.writeByte(header.lineBase());
			prologueBos.writeByte(header.lineRange());
			prologueBos.writeByte(header.opcodeBase());
			for (int length : header.standardOpcodeLengths()) {
				prologueBos.writeByte(length);
			}
			if (header.version().isAtLeast(DwarfVersion.DWARF5)) {
				writeFormats(prologueBos, header.directoryFormats());
				Leb128Utils.writeUleb128(prologueBos, header.directories().size());
				for (DwarfLineDirectoryEntry entry : header.directories()) {
					writeEntry(
							prologueBos, header.directoryFormats(), entry.valuesByContentTypeCode(), header.dwarf64());
				}
				writeFormats(prologueBos, header.fileFormats());
				Leb128Utils.writeUleb128(prologueBos, header.files().size());
				for (DwarfLineFileEntry entry : header.files()) {
					writeEntry(prologueBos, header.fileFormats(), entry.valuesByContentTypeCode(), header.dwarf64());
				}
			} else {
				for (DwarfLineDirectoryEntry directory : header.directories()) {
					DwarfValue value = directory.values().get(DwarfLineContentType.DW_LNCT_path);
					prologueBos.writeNullTerminatedString(pathString(value), StandardCharsets.UTF_8);
				}
				prologueBos.writeByte(0);
				for (DwarfLineFileEntry file : header.files()) {
					prologueBos.writeNullTerminatedString(
							pathString(file.values().get(DwarfLineContentType.DW_LNCT_path)), StandardCharsets.UTF_8);
					Leb128Utils.writeUleb128(
							prologueBos,
							unsigned(file.values()
									.getOrDefault(
											DwarfLineContentType.DW_LNCT_directory_index, new DwarfUnsignedValue(0))));
					Leb128Utils.writeUleb128(
							prologueBos,
							unsigned(file.values()
									.getOrDefault(DwarfLineContentType.DW_LNCT_timestamp, new DwarfUnsignedValue(0))));
					Leb128Utils.writeUleb128(
							prologueBos,
							unsigned(file.values()
									.getOrDefault(DwarfLineContentType.DW_LNCT_size, new DwarfUnsignedValue(0))));
				}
				prologueBos.writeByte(0);
			}

			byte[] prologueBytes = prologueBaos.toByteArray();
			ByteArrayOutputStream unitBaos = new ByteArrayOutputStream();
			ByteOutputStream unitBos = new ByteOutputStream(unitBaos, byteOrder);
			unitBos.writeShort(header.version().getValue());
			if (header.version().isAtLeast(DwarfVersion.DWARF5)) {
				unitBos.writeByte(header.addressSize());
				unitBos.writeByte(header.segmentSelectorSize());
			}
			if (header.dwarf64()) {
				unitBos.writeLong(prologueBytes.length);
			} else {
				unitBos.writeInt(prologueBytes.length);
			}
			unitBos.write(prologueBytes);
			for (DwarfLineInstruction instruction : instructions) {
				instruction.write(unitBos, byteOrder, header.addressSize());
			}
			return unitBaos.toByteArray();
		}

		private static void writeFormats(ByteOutputStream bos, List<DwarfLineEntryFormat> formats) throws IOException {
			if (formats.size() > 0xff) {
				throw new IllegalArgumentException("Line-table entry format count does not fit in 1 byte");
			}
			bos.writeByte(formats.size());
			for (DwarfLineEntryFormat format : formats) {
				Leb128Utils.writeUleb128(bos, format.contentTypeCode());
				Leb128Utils.writeUleb128(bos, format.form().getValue());
			}
		}

		private static void writeEntry(
				ByteOutputStream bos,
				List<DwarfLineEntryFormat> formats,
				Map<Long, DwarfValue> valuesByContentTypeCode,
				boolean dwarf64)
				throws IOException {
			for (DwarfLineEntryFormat format : formats) {
				DwarfValue value = valuesByContentTypeCode.get(format.contentTypeCode());
				if (value == null) {
					throw new IllegalArgumentException("Missing line-table value for content type code 0x"
							+ Long.toHexString(format.contentTypeCode()));
				}
				switch (format.form()) {
					case DW_FORM_line_strp -> {
						long offset = ((DwarfStringReferenceValue) value).offset();
						if (dwarf64) {
							bos.writeLong(offset);
						} else {
							bos.writeInt(requireUnsignedInt(offset, "DW_FORM_line_strp offset"));
						}
					}
					case DW_FORM_string -> bos.writeNullTerminatedString(pathString(value), StandardCharsets.UTF_8);
					case DW_FORM_udata -> Leb128Utils.writeUleb128(bos, unsigned(value));
					case DW_FORM_data1 -> bos.writeByte(
							requireUnsignedByte(unsigned(value), format.form().name()));
					case DW_FORM_data2 -> bos.writeShort(
							requireUnsignedShort(unsigned(value), format.form().name()));
					case DW_FORM_data4 -> bos.writeInt(
							requireUnsignedInt(unsigned(value), format.form().name()));
					case DW_FORM_data8 -> bos.writeLong(unsigned(value));
					case DW_FORM_data16 -> {
						byte[] bytes = ((DwarfBlockValue) value).bytes();
						if (bytes.length != 16) {
							throw new IllegalArgumentException("DW_FORM_data16 value must be exactly 16 bytes");
						}
						bos.write(bytes);
					}
					default -> throw new IllegalArgumentException(
							"Unsupported line-table entry form: " + format.form());
				}
			}
		}
	}

	/**
	 * Parsed line-table unit header and prologue metadata.
	 *
	 * @param unitLength payload length excluding the initial length field encoding
	 * @param dwarf64 true when DWARF64 initial-length encoding is used
	 * @param version DWARF version for this unit
	 * @param addressSize machine address size used by address-bearing opcodes
	 * @param segmentSelectorSize optional segment selector size
	 * @param prologueLength encoded prologue length
	 * @param minimumInstructionLength minimum instruction length in address units
	 * @param maximumOperationsPerInstruction maximum operations per instruction
	 * @param defaultIsStatement initial is_stmt state
	 * @param lineBase signed line-base delta
	 * @param lineRange line-range divisor for special opcodes
	 * @param opcodeBase first special-opcode value
	 * @param standardOpcodeLengths operand counts for standard opcodes
	 * @param directoryFormats v5 directory entry format descriptors
	 * @param directories decoded directory entries
	 * @param fileFormats v5 file entry format descriptors
	 * @param files decoded file entries
	 */
	public record DwarfLineHeader(
			long unitLength,
			boolean dwarf64,
			DwarfVersion version,
			int addressSize,
			int segmentSelectorSize,
			long prologueLength,
			int minimumInstructionLength,
			int maximumOperationsPerInstruction,
			boolean defaultIsStatement,
			int lineBase,
			int lineRange,
			int opcodeBase,
			List<Integer> standardOpcodeLengths,
			List<DwarfLineEntryFormat> directoryFormats,
			List<DwarfLineDirectoryEntry> directories,
			List<DwarfLineEntryFormat> fileFormats,
			List<DwarfLineFileEntry> files) {
		/**
		 * Creates an immutable line-table header.
		 *
		 * @param unitLength payload length excluding the initial length field encoding
		 * @param dwarf64 true when DWARF64 initial-length encoding is used
		 * @param version DWARF version for this unit
		 * @param addressSize machine address size used by address-bearing opcodes
		 * @param segmentSelectorSize optional segment selector size
		 * @param prologueLength encoded prologue length
		 * @param minimumInstructionLength minimum instruction length in address units
		 * @param maximumOperationsPerInstruction maximum operations per instruction
		 * @param defaultIsStatement initial is_stmt state
		 * @param lineBase signed line-base delta
		 * @param lineRange line-range divisor for special opcodes
		 * @param opcodeBase first special-opcode value
		 * @param standardOpcodeLengths operand counts for standard opcodes
		 * @param directoryFormats v5 directory entry format descriptors
		 * @param directories decoded directory entries
		 * @param fileFormats v5 file entry format descriptors
		 * @param files decoded file entries
		 */
		public DwarfLineHeader {
			standardOpcodeLengths = Collections.unmodifiableList(new ArrayList<>(standardOpcodeLengths));
			directoryFormats = Collections.unmodifiableList(new ArrayList<>(directoryFormats));
			directories = Collections.unmodifiableList(new ArrayList<>(directories));
			fileFormats = Collections.unmodifiableList(new ArrayList<>(fileFormats));
			files = Collections.unmodifiableList(new ArrayList<>(files));
		}
	}

	/**
	 * Content-type/form descriptor used by DWARF5 directory and file entry tables.
	 *
	 * @param contentTypeCode raw DW_LNCT_* numeric code
	 * @param contentType decoded DW_LNCT_* enum, or null when unknown
	 * @param form DW_FORM_* used to encode entry values for this content type
	 */
	public record DwarfLineEntryFormat(long contentTypeCode, DwarfLineContentType contentType, DwarfForm form) {}

	/**
	 * One directory-table entry.
	 *
	 * @param values values keyed by known content-type enums
	 * @param valuesByContentTypeCode values keyed by raw content-type codes
	 */
	public record DwarfLineDirectoryEntry(
			Map<DwarfLineContentType, DwarfValue> values, Map<Long, DwarfValue> valuesByContentTypeCode) {
		/**
		 * Creates a directory entry from known typed values.
		 *
		 * @param values values keyed by known content-type enums
		 */
		public DwarfLineDirectoryEntry(Map<DwarfLineContentType, DwarfValue> values) {
			this(values, mapKnownValuesByContentTypeCode(values));
		}

		/**
		 * Creates an immutable directory entry.
		 *
		 * @param values values keyed by known content-type enums
		 * @param valuesByContentTypeCode values keyed by raw content-type codes
		 */
		public DwarfLineDirectoryEntry {
			values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
			valuesByContentTypeCode = Collections.unmodifiableMap(new LinkedHashMap<>(valuesByContentTypeCode));
		}
	}

	/**
	 * One file-table entry.
	 *
	 * @param values values keyed by known content-type enums
	 * @param valuesByContentTypeCode values keyed by raw content-type codes
	 */
	public record DwarfLineFileEntry(
			Map<DwarfLineContentType, DwarfValue> values, Map<Long, DwarfValue> valuesByContentTypeCode) {
		/**
		 * Creates a file entry from known typed values.
		 *
		 * @param values values keyed by known content-type enums
		 */
		public DwarfLineFileEntry(Map<DwarfLineContentType, DwarfValue> values) {
			this(values, mapKnownValuesByContentTypeCode(values));
		}

		/**
		 * Creates an immutable file entry.
		 *
		 * @param values values keyed by known content-type enums
		 * @param valuesByContentTypeCode values keyed by raw content-type codes
		 */
		public DwarfLineFileEntry {
			values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
			valuesByContentTypeCode = Collections.unmodifiableMap(new LinkedHashMap<>(valuesByContentTypeCode));
		}
	}

	private record ParsedEntryValues(
			Map<DwarfLineContentType, DwarfValue> values, Map<Long, DwarfValue> valuesByContentTypeCode) {}

	/**
	 * One decoded line-table row emitted by the line-state machine.
	 *
	 * @param address machine address for this row
	 * @param file one-based file index
	 * @param line source line number
	 * @param column source column number
	 * @param isStatement statement marker state
	 * @param basicBlock basic-block start marker
	 * @param endSequence end-of-sequence marker
	 * @param prologueEnd function prologue end marker
	 * @param epilogueBegin function epilogue start marker
	 * @param isa instruction-set architecture discriminator
	 * @param discriminator path discriminator value
	 */
	public record DwarfLineRow(
			long address,
			long file,
			long line,
			long column,
			boolean isStatement,
			boolean basicBlock,
			boolean endSequence,
			boolean prologueEnd,
			boolean epilogueBegin,
			long isa,
			long discriminator) {}

	private static final class DwarfLineState {
		long address;
		long file = 1;
		long line = 1;
		long column;
		boolean isStatement;
		boolean basicBlock;
		boolean endSequence;
		boolean prologueEnd;
		boolean epilogueBegin;
		long isa;
		long discriminator;

		DwarfLineState(boolean defaultIsStatement) {
			this.isStatement = defaultIsStatement;
		}

		void reset(boolean defaultIsStatement) {
			address = 0;
			file = 1;
			line = 1;
			column = 0;
			isStatement = defaultIsStatement;
			basicBlock = false;
			endSequence = false;
			prologueEnd = false;
			epilogueBegin = false;
			isa = 0;
			discriminator = 0;
		}

		DwarfLineRow row() {
			return new DwarfLineRow(
					address,
					file,
					line,
					column,
					isStatement,
					basicBlock,
					endSequence,
					prologueEnd,
					epilogueBegin,
					isa,
					discriminator);
		}

		void clearRowFlags() {
			basicBlock = false;
			prologueEnd = false;
			epilogueBegin = false;
			discriminator = 0;
		}
	}

	/**
	 * Common contract for decoded line-program instructions.
	 */
	public sealed interface DwarfLineInstruction
			permits DwarfLineCopy,
					DwarfLineAdvancePc,
					DwarfLineAdvanceLine,
					DwarfLineSetFile,
					DwarfLineSetColumn,
					DwarfLineNegateStatement,
					DwarfLineSetBasicBlock,
					DwarfLineConstAddPc,
					DwarfLineFixedAdvancePc,
					DwarfLineSetPrologueEnd,
					DwarfLineSetEpilogueBegin,
					DwarfLineSetIsa,
					DwarfLineSetAddress,
					DwarfLineSetDiscriminator,
					DwarfLineEndSequence,
					DwarfLineSpecialOpcode,
					DwarfLineUnknownExtendedInstruction {
		/**
		 * Writes this instruction using canonical DWARF line-program encoding.
		 *
		 * @param bos destination output stream
		 * @param byteOrder active byte order
		 * @param addressSize address size used by address-bearing instructions
		 * @throws IOException if writing fails
		 */
		void write(ByteOutputStream bos, ByteOrder byteOrder, int addressSize) throws IOException;

		/**
		 * Applies this instruction to line-state and appends rows when the instruction emits one.
		 *
		 * @param state mutable line-state machine
		 * @param header line-table header driving opcode semantics
		 * @param rows output row list
		 */
		void apply(DwarfLineState state, DwarfLineHeader header, List<DwarfLineRow> rows);
	}

	/** Standard opcode DW_LNS_copy. */
	public record DwarfLineCopy() implements DwarfLineInstruction {
		@Override
		public void write(ByteOutputStream bos, ByteOrder byteOrder, int addressSize) throws IOException {
			bos.writeByte(1);
		}

		@Override
		public void apply(DwarfLineState state, DwarfLineHeader header, List<DwarfLineRow> rows) {
			rows.add(state.row());
			state.clearRowFlags();
		}
	}

	/**
	 * Standard opcode DW_LNS_advance_pc.
	 *
	 * @param operand address advance in minimum-instruction-length units
	 */
	public record DwarfLineAdvancePc(long operand) implements DwarfLineInstruction {
		@Override
		public void write(ByteOutputStream bos, ByteOrder byteOrder, int addressSize) throws IOException {
			bos.writeByte(2);
			Leb128Utils.writeUleb128(bos, operand);
		}

		@Override
		public void apply(DwarfLineState state, DwarfLineHeader header, List<DwarfLineRow> rows) {
			state.address += operand * header.minimumInstructionLength();
		}
	}

	/**
	 * Standard opcode DW_LNS_advance_line.
	 *
	 * @param operand signed line delta
	 */
	public record DwarfLineAdvanceLine(long operand) implements DwarfLineInstruction {
		@Override
		public void write(ByteOutputStream bos, ByteOrder byteOrder, int addressSize) throws IOException {
			bos.writeByte(3);
			Leb128Utils.writeSleb128(bos, operand);
		}

		@Override
		public void apply(DwarfLineState state, DwarfLineHeader header, List<DwarfLineRow> rows) {
			state.line += operand;
		}
	}

	/**
	 * Standard opcode DW_LNS_set_file.
	 *
	 * @param operand new file index
	 */
	public record DwarfLineSetFile(long operand) implements DwarfLineInstruction {
		@Override
		public void write(ByteOutputStream bos, ByteOrder byteOrder, int addressSize) throws IOException {
			bos.writeByte(4);
			Leb128Utils.writeUleb128(bos, operand);
		}

		@Override
		public void apply(DwarfLineState state, DwarfLineHeader header, List<DwarfLineRow> rows) {
			state.file = operand;
		}
	}

	/**
	 * Standard opcode DW_LNS_set_column.
	 *
	 * @param operand new source column number
	 */
	public record DwarfLineSetColumn(long operand) implements DwarfLineInstruction {
		@Override
		public void write(ByteOutputStream bos, ByteOrder byteOrder, int addressSize) throws IOException {
			bos.writeByte(5);
			Leb128Utils.writeUleb128(bos, operand);
		}

		@Override
		public void apply(DwarfLineState state, DwarfLineHeader header, List<DwarfLineRow> rows) {
			state.column = operand;
		}
	}

	/** Standard opcode DW_LNS_negate_stmt. */
	public record DwarfLineNegateStatement() implements DwarfLineInstruction {
		@Override
		public void write(ByteOutputStream bos, ByteOrder byteOrder, int addressSize) throws IOException {
			bos.writeByte(6);
		}

		@Override
		public void apply(DwarfLineState state, DwarfLineHeader header, List<DwarfLineRow> rows) {
			state.isStatement = !state.isStatement;
		}
	}

	/** Standard opcode DW_LNS_set_basic_block. */
	public record DwarfLineSetBasicBlock() implements DwarfLineInstruction {
		@Override
		public void write(ByteOutputStream bos, ByteOrder byteOrder, int addressSize) throws IOException {
			bos.writeByte(7);
		}

		@Override
		public void apply(DwarfLineState state, DwarfLineHeader header, List<DwarfLineRow> rows) {
			state.basicBlock = true;
		}
	}

	/** Standard opcode DW_LNS_const_add_pc. */
	public record DwarfLineConstAddPc() implements DwarfLineInstruction {
		@Override
		public void write(ByteOutputStream bos, ByteOrder byteOrder, int addressSize) throws IOException {
			bos.writeByte(8);
		}

		@Override
		public void apply(DwarfLineState state, DwarfLineHeader header, List<DwarfLineRow> rows) {
			int adjusted = 255 - header.opcodeBase();
			state.address += (adjusted / header.lineRange()) * header.minimumInstructionLength();
		}
	}

	/**
	 * Standard opcode DW_LNS_fixed_advance_pc.
	 *
	 * @param operand absolute address delta in target-address units
	 */
	public record DwarfLineFixedAdvancePc(int operand) implements DwarfLineInstruction {
		@Override
		public void write(ByteOutputStream bos, ByteOrder byteOrder, int addressSize) throws IOException {
			bos.writeByte(9);
			bos.writeShort(requireUnsignedShort(operand, "DW_LNS_fixed_advance_pc operand"));
		}

		@Override
		public void apply(DwarfLineState state, DwarfLineHeader header, List<DwarfLineRow> rows) {
			state.address += operand;
		}
	}

	/** Standard opcode DW_LNS_set_prologue_end. */
	public record DwarfLineSetPrologueEnd() implements DwarfLineInstruction {
		@Override
		public void write(ByteOutputStream bos, ByteOrder byteOrder, int addressSize) throws IOException {
			bos.writeByte(10);
		}

		@Override
		public void apply(DwarfLineState state, DwarfLineHeader header, List<DwarfLineRow> rows) {
			state.prologueEnd = true;
		}
	}

	/** Standard opcode DW_LNS_set_epilogue_begin. */
	public record DwarfLineSetEpilogueBegin() implements DwarfLineInstruction {
		@Override
		public void write(ByteOutputStream bos, ByteOrder byteOrder, int addressSize) throws IOException {
			bos.writeByte(11);
		}

		@Override
		public void apply(DwarfLineState state, DwarfLineHeader header, List<DwarfLineRow> rows) {
			state.epilogueBegin = true;
		}
	}

	/**
	 * Standard opcode DW_LNS_set_isa.
	 *
	 * @param operand ISA discriminator value
	 */
	public record DwarfLineSetIsa(long operand) implements DwarfLineInstruction {
		@Override
		public void write(ByteOutputStream bos, ByteOrder byteOrder, int addressSize) throws IOException {
			bos.writeByte(12);
			Leb128Utils.writeUleb128(bos, operand);
		}

		@Override
		public void apply(DwarfLineState state, DwarfLineHeader header, List<DwarfLineRow> rows) {
			state.isa = operand;
		}
	}

	/**
	 * Extended opcode DW_LNE_set_address.
	 *
	 * @param address absolute machine address
	 */
	public record DwarfLineSetAddress(long address) implements DwarfLineInstruction {
		@Override
		public void write(ByteOutputStream bos, ByteOrder byteOrder, int addressSize) throws IOException {
			bos.writeByte(0);
			Leb128Utils.writeUleb128(bos, 1L + addressSize);
			bos.writeByte(2);
			switch (addressSize) {
				case 1 -> bos.writeByte(requireUnsignedByte(address, "DW_LNE_set_address"));
				case 2 -> bos.writeShort(requireUnsignedShort(address, "DW_LNE_set_address"));
				case 4 -> bos.writeInt(requireUnsignedInt(address, "DW_LNE_set_address"));
				case 8 -> bos.writeLong(address);
				default -> throw new IllegalArgumentException("Unsupported DWARF address size: " + addressSize);
			}
		}

		@Override
		public void apply(DwarfLineState state, DwarfLineHeader header, List<DwarfLineRow> rows) {
			state.address = address;
		}
	}

	/**
	 * Extended opcode DW_LNE_set_discriminator.
	 *
	 * @param discriminator path discriminator value
	 */
	public record DwarfLineSetDiscriminator(long discriminator) implements DwarfLineInstruction {
		@Override
		public void write(ByteOutputStream bos, ByteOrder byteOrder, int addressSize) throws IOException {
			ByteArrayOutputStream payloadBaos = new ByteArrayOutputStream();
			ByteOutputStream payloadBos = ByteOutputStream.asLittleEndian(payloadBaos);
			Leb128Utils.writeUleb128(payloadBos, discriminator);
			byte[] payload = payloadBaos.toByteArray();
			bos.writeByte(0);
			Leb128Utils.writeUleb128(bos, payload.length + 1L);
			bos.writeByte(4);
			bos.write(payload);
		}

		@Override
		public void apply(DwarfLineState state, DwarfLineHeader header, List<DwarfLineRow> rows) {
			state.discriminator = discriminator;
		}
	}

	/** Extended opcode DW_LNE_end_sequence. */
	public record DwarfLineEndSequence() implements DwarfLineInstruction {
		@Override
		public void write(ByteOutputStream bos, ByteOrder byteOrder, int addressSize) throws IOException {
			bos.writeByte(0);
			Leb128Utils.writeUleb128(bos, 1);
			bos.writeByte(1);
		}

		@Override
		public void apply(DwarfLineState state, DwarfLineHeader header, List<DwarfLineRow> rows) {
			state.endSequence = true;
			rows.add(state.row());
			state.reset(header.defaultIsStatement());
		}
	}

	/**
	 * Encoded special opcode in the range [opcode_base, 255].
	 *
	 * @param opcode raw opcode byte value
	 */
	public record DwarfLineSpecialOpcode(int opcode) implements DwarfLineInstruction {
		@Override
		public void write(ByteOutputStream bos, ByteOrder byteOrder, int addressSize) throws IOException {
			bos.writeByte(requireUnsignedByte(opcode, "special line opcode"));
		}

		@Override
		public void apply(DwarfLineState state, DwarfLineHeader header, List<DwarfLineRow> rows) {
			int adjusted = opcode - header.opcodeBase();
			state.address += (adjusted / header.lineRange()) * header.minimumInstructionLength();
			state.line += header.lineBase() + (adjusted % header.lineRange());
			rows.add(state.row());
			state.clearRowFlags();
		}
	}

	/**
	 * Unknown extended opcode preserved as raw payload bytes.
	 *
	 * @param subOpcode raw extended sub-opcode value
	 * @param payload raw extended payload bytes, excluding the sub-opcode byte
	 */
	public record DwarfLineUnknownExtendedInstruction(int subOpcode, byte[] payload) implements DwarfLineInstruction {
		/**
		 * Creates an immutable unknown extended instruction.
		 *
		 * @param subOpcode raw extended sub-opcode value
		 * @param payload raw extended payload bytes, excluding the sub-opcode byte
		 */
		public DwarfLineUnknownExtendedInstruction {
			payload = Arrays.copyOf(payload, payload.length);
		}

		/**
		 * Returns a defensive copy of the raw extended payload bytes.
		 *
		 * @return copied payload bytes
		 */
		@Override
		public byte[] payload() {
			return Arrays.copyOf(payload, payload.length);
		}

		@Override
		public void write(ByteOutputStream bos, ByteOrder byteOrder, int addressSize) throws IOException {
			bos.writeByte(0);
			Leb128Utils.writeUleb128(bos, payload.length + 1L);
			bos.writeByte(subOpcode);
			bos.write(payload);
		}

		@Override
		public void apply(DwarfLineState state, DwarfLineHeader header, List<DwarfLineRow> rows) {
			throw new IllegalArgumentException("Unsupported extended line opcode during decoding: " + subOpcode);
		}
	}

	private static String pathString(DwarfValue value) {
		if (value instanceof DwarfStringReferenceValue stringReferenceValue) {
			return stringReferenceValue.value();
		}
		if (value instanceof DwarfStringValue stringValue) {
			return stringValue.value();
		}
		throw new IllegalArgumentException("Unsupported path value: " + value);
	}

	private static long unsigned(DwarfValue value) {
		return ((DwarfUnsignedValue) value).value();
	}

	private static int requireUnsignedByte(long value, String fieldName) {
		if (value < 0 || value > 0xffL) {
			throw new IllegalArgumentException(fieldName + " does not fit in 1 byte");
		}
		return (int) value;
	}

	private static int requireUnsignedShort(long value, String fieldName) {
		if (value < 0 || value > 0xffffL) {
			throw new IllegalArgumentException(fieldName + " does not fit in 2 bytes");
		}
		return (int) value;
	}

	private static int requireUnsignedInt(long value, String fieldName) {
		if (value < 0 || value > 0xffff_ffffL) {
			throw new IllegalArgumentException(fieldName + " does not fit in 4 bytes");
		}
		return (int) value;
	}

	private static int requireIntLength(long value, String fieldName) {
		if (value < 0 || value > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(fieldName + " does not fit in a Java array length");
		}
		return (int) value;
	}

	private static Map<Long, DwarfValue> mapKnownValuesByContentTypeCode(Map<DwarfLineContentType, DwarfValue> values) {
		Map<Long, DwarfValue> valuesByCode = new LinkedHashMap<>();
		for (Map.Entry<DwarfLineContentType, DwarfValue> entry : values.entrySet()) {
			valuesByCode.put((long) entry.getKey().getValue(), entry.getValue());
		}
		return valuesByCode;
	}
}
