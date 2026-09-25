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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.dwarf.constants.DwarfAttributeName;
import net.boricj.bft.dwarf.constants.DwarfForm;
import net.boricj.bft.dwarf.constants.DwarfTag;
import net.boricj.bft.dwarf.constants.DwarfUnitType;
import net.boricj.bft.dwarf.constants.DwarfVersion;
import net.boricj.bft.dwarf.model.DwarfAbbreviationAttribute;
import net.boricj.bft.dwarf.model.DwarfAbbreviationDeclaration;
import net.boricj.bft.dwarf.model.DwarfAbbreviationTable;
import net.boricj.bft.dwarf.model.DwarfAddressValue;
import net.boricj.bft.dwarf.model.DwarfBlockValue;
import net.boricj.bft.dwarf.model.DwarfCompilationUnit;
import net.boricj.bft.dwarf.model.DwarfCompilationUnitHeader;
import net.boricj.bft.dwarf.model.DwarfDie;
import net.boricj.bft.dwarf.model.DwarfDieAttribute;
import net.boricj.bft.dwarf.model.DwarfFlagValue;
import net.boricj.bft.dwarf.model.DwarfReferenceValue;
import net.boricj.bft.dwarf.model.DwarfStringReferenceValue;
import net.boricj.bft.dwarf.model.DwarfStringValue;
import net.boricj.bft.dwarf.model.DwarfUnsignedValue;
import net.boricj.bft.dwarf.model.DwarfValue;

/**
 * Semantic .debug_info representation with exact reserialization support for the modeled forms.
 */
public final class DwarfDebugInfo {
	private final ByteOrder byteOrder;
	private final DwarfAbbreviationTable abbreviationTable;
	private final DwarfStringTable debugStrings;
	private final DwarfStringTable debugLineStrings;
	private final List<DwarfCompilationUnit> compilationUnits;

	/**
	 * Creates a semantic .debug_info section.
	 *
	 * @param byteOrder section byte order
	 * @param abbreviationTable abbreviation table used by the units
	 * @param debugStrings .debug_str contents, or null
	 * @param debugLineStrings .debug_line_str contents, or null
	 * @param compilationUnits compilation units in encounter order
	 */
	public DwarfDebugInfo(
			ByteOrder byteOrder,
			DwarfAbbreviationTable abbreviationTable,
			DwarfStringTable debugStrings,
			DwarfStringTable debugLineStrings,
			List<DwarfCompilationUnit> compilationUnits) {
		this.byteOrder = Objects.requireNonNull(byteOrder);
		this.abbreviationTable = Objects.requireNonNull(abbreviationTable);
		this.debugStrings = debugStrings;
		this.debugLineStrings = debugLineStrings;
		this.compilationUnits = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(compilationUnits)));
	}

	/**
	 * Returns the byte order used for all numeric encodings in this debug info.
	 *
	 * @return DWARF byte order
	 */
	public ByteOrder getByteOrder() {
		return byteOrder;
	}

	/**
	 * Returns abbreviation declarations referenced by compilation units.
	 *
	 * @return abbreviation table
	 */
	public DwarfAbbreviationTable getAbbreviationTable() {
		return abbreviationTable;
	}

	/**
	 * Returns optional .debug_str table used by DW_FORM_strp attributes.
	 *
	 * @return string table or null
	 */
	public DwarfStringTable getDebugStrings() {
		return debugStrings;
	}

	/**
	 * Returns optional .debug_line_str table used by DW_FORM_line_strp attributes.
	 *
	 * @return line-string table or null
	 */
	public DwarfStringTable getDebugLineStrings() {
		return debugLineStrings;
	}

	/**
	 * Returns parsed compilation units in section order.
	 *
	 * @return immutable list of compilation units
	 */
	public List<DwarfCompilationUnit> getCompilationUnits() {
		return compilationUnits;
	}

	/**
	 * Parses .debug_info bytes using the supplied semantic context.
	 *
	 * @param bytes .debug_info bytes
	 * @param byteOrder section byte order
	 * @param abbreviationTable abbreviation table for the units
	 * @param debugStrings .debug_str contents, or null
	 * @param debugLineStrings .debug_line_str contents, or null
	 * @return parsed semantic representation
	 * @throws IOException if parsing fails
	 */
	public static DwarfDebugInfo parse(
			byte[] bytes,
			ByteOrder byteOrder,
			DwarfAbbreviationTable abbreviationTable,
			DwarfStringTable debugStrings,
			DwarfStringTable debugLineStrings)
			throws IOException {
		Objects.requireNonNull(bytes);
		Objects.requireNonNull(byteOrder);
		Objects.requireNonNull(abbreviationTable);

		ByteInputStream bis = new ByteInputStream(new ByteArrayInputStream(bytes), byteOrder);
		List<DwarfCompilationUnit> units = new ArrayList<>();

		while (bis.available() > 0) {
			long sectionOffset = bis.getCount();
			long initialLength = Integer.toUnsignedLong(bis.readInt());
			boolean dwarf64 = initialLength == 0xffff_ffffL;
			long unitLength = dwarf64 ? bis.readLong() : initialLength;
			byte[] unitBytes = new byte[requireIntLength(unitLength, ".debug_info unit length")];
			bis.readFully(unitBytes);

			ByteInputStream unitStream = new ByteInputStream(new ByteArrayInputStream(unitBytes), byteOrder);
			DwarfVersion version = DwarfVersion.valueFrom(unitStream.readUnsignedShort());
			DwarfUnitType unitType = null;
			int addressSize;
			long abbreviationOffset;
			if (version.isAtLeast(DwarfVersion.DWARF5)) {
				unitType = DwarfUnitType.valueFromOrNull(unitStream.readUnsignedByte());
				addressSize = unitStream.readUnsignedByte();
				abbreviationOffset = readOffset(unitStream, dwarf64);
			} else {
				abbreviationOffset = readOffset(unitStream, dwarf64);
				addressSize = unitStream.readUnsignedByte();
			}

			if (abbreviationOffset != 0) {
				throw new IllegalArgumentException(
						"Non-zero abbreviation offsets are not supported yet: " + abbreviationOffset);
			}

			DwarfCompilationUnitHeader header = new DwarfCompilationUnitHeader(
					unitLength, dwarf64, version, unitType, abbreviationOffset, addressSize);
			List<DwarfDie> topLevelDies = new ArrayList<>();
			while (unitStream.available() > 0) {
				DwarfDie die = parseDie(unitStream, header, abbreviationTable, debugStrings, debugLineStrings);
				if (die == null) {
					break;
				}
				topLevelDies.add(die);
			}

			units.add(new DwarfCompilationUnit(sectionOffset, header, topLevelDies));
		}

		return new DwarfDebugInfo(byteOrder, abbreviationTable, debugStrings, debugLineStrings, units);
	}

	private static DwarfDie parseDie(
			ByteInputStream unitStream,
			DwarfCompilationUnitHeader header,
			DwarfAbbreviationTable abbreviationTable,
			DwarfStringTable debugStrings,
			DwarfStringTable debugLineStrings)
			throws IOException {
		long offset = unitStream.getCount();
		long abbreviationCode = Leb128Utils.readUleb128(unitStream);
		if (abbreviationCode == 0) {
			return null;
		}

		Map<Long, DwarfAbbreviationDeclaration> declarations = abbreviationTable.getDeclarationsByCode();
		DwarfAbbreviationDeclaration declaration = declarations.get(abbreviationCode);
		if (declaration == null) {
			throw new IllegalArgumentException("Unknown abbreviation code: " + abbreviationCode);
		}

		List<DwarfDieAttribute> attributes = new ArrayList<>();
		for (DwarfAbbreviationAttribute abbreviationAttribute : declaration.getAttributes()) {
			attributes.add(new DwarfDieAttribute(
					abbreviationAttribute.getAttributeCode(),
					abbreviationAttribute.getAttributeName(),
					abbreviationAttribute.getForm(),
					parseValue(unitStream, header, abbreviationAttribute, debugStrings, debugLineStrings)));
		}

		List<DwarfDie> children = new ArrayList<>();
		if (declaration.hasChildren()) {
			while (true) {
				DwarfDie child = parseDie(unitStream, header, abbreviationTable, debugStrings, debugLineStrings);
				if (child == null) {
					break;
				}
				children.add(child);
			}
		}

		DwarfTag tag = declaration.getTag();
		return new DwarfDie(offset, abbreviationCode, declaration.getTagCode(), tag, attributes, children);
	}

	private static DwarfValue parseValue(
			ByteInputStream unitStream,
			DwarfCompilationUnitHeader header,
			DwarfAbbreviationAttribute attribute,
			DwarfStringTable debugStrings,
			DwarfStringTable debugLineStrings)
			throws IOException {
		return switch (attribute.getForm()) {
			case DW_FORM_addr -> new DwarfAddressValue(readAddress(unitStream, header.addressSize()));
			case DW_FORM_data1 -> new DwarfUnsignedValue(unitStream.readUnsignedByte());
			case DW_FORM_data2 -> new DwarfUnsignedValue(unitStream.readUnsignedShort());
			case DW_FORM_data4, DW_FORM_sec_offset -> new DwarfUnsignedValue(
					Integer.toUnsignedLong(unitStream.readInt()));
			case DW_FORM_data8 -> new DwarfUnsignedValue(unitStream.readLong());
			case DW_FORM_udata -> new DwarfUnsignedValue(Leb128Utils.readUleb128(unitStream));
			case DW_FORM_sdata -> new DwarfUnsignedValue(Leb128Utils.readSleb128(unitStream));
			case DW_FORM_string -> new DwarfStringValue(unitStream.readNullTerminatedString(StandardCharsets.UTF_8));
			case DW_FORM_strp -> {
				requireStringTable(debugStrings, DwarfForm.DW_FORM_strp);
				long offset = readOffset(unitStream, header.dwarf64());
				yield new DwarfStringReferenceValue(offset, debugStrings.get(offset), false);
			}
			case DW_FORM_line_strp -> {
				requireStringTable(debugLineStrings, DwarfForm.DW_FORM_line_strp);
				long offset = readOffset(unitStream, header.dwarf64());
				yield new DwarfStringReferenceValue(offset, debugLineStrings.get(offset), true);
			}
			case DW_FORM_ref1 -> new DwarfReferenceValue(unitStream.readUnsignedByte());
			case DW_FORM_ref2 -> new DwarfReferenceValue(unitStream.readUnsignedShort());
			case DW_FORM_ref4 -> new DwarfReferenceValue(Integer.toUnsignedLong(unitStream.readInt()));
			case DW_FORM_ref8 -> new DwarfReferenceValue(unitStream.readLong());
			case DW_FORM_ref_udata -> new DwarfReferenceValue(Leb128Utils.readUleb128(unitStream));
			case DW_FORM_ref_addr -> new DwarfReferenceValue(
					readRefAddress(unitStream, header.version(), header.addressSize(), header.dwarf64()));
			case DW_FORM_flag -> new DwarfFlagValue(unitStream.readUnsignedByte() != 0, false);
			case DW_FORM_flag_present -> new DwarfFlagValue(true, true);
			case DW_FORM_block1 -> new DwarfBlockValue(readBlock(unitStream, unitStream.readUnsignedByte()));
			case DW_FORM_block2 -> new DwarfBlockValue(readBlock(unitStream, unitStream.readUnsignedShort()));
			case DW_FORM_block4 -> new DwarfBlockValue(
					readBlock(unitStream, Integer.toUnsignedLong(unitStream.readInt())));
			case DW_FORM_block, DW_FORM_exprloc -> new DwarfBlockValue(
					readBlock(unitStream, Leb128Utils.readUleb128(unitStream)));
			case DW_FORM_implicit_const -> new DwarfUnsignedValue(attribute.getImplicitConstValue());
			case DW_FORM_indirect -> throw new IllegalArgumentException("DW_FORM_indirect is not supported yet");
			case DW_FORM_ref_sig8,
					DW_FORM_strx,
					DW_FORM_addrx,
					DW_FORM_ref_sup4,
					DW_FORM_strp_sup,
					DW_FORM_data16,
					DW_FORM_loclistx,
					DW_FORM_rnglistx,
					DW_FORM_ref_sup8,
					DW_FORM_strx1,
					DW_FORM_strx2,
					DW_FORM_strx3,
					DW_FORM_strx4,
					DW_FORM_addrx1,
					DW_FORM_addrx2,
					DW_FORM_addrx3,
					DW_FORM_addrx4 -> throw new IllegalArgumentException(
					"Form not implemented yet: " + attribute.getForm());
		};
	}

	private static byte[] readBlock(ByteInputStream unitStream, long length) throws IOException {
		byte[] bytes = new byte[requireIntLength(length, "DWARF block length")];
		unitStream.readFully(bytes);
		return bytes;
	}

	private static int requireIntLength(long value, String fieldName) {
		if (value < 0 || value > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(fieldName + " does not fit in a Java array length");
		}
		return (int) value;
	}

	private static long readAddress(ByteInputStream unitStream, int addressSize) throws IOException {
		return switch (addressSize) {
			case 1 -> unitStream.readUnsignedByte();
			case 2 -> unitStream.readUnsignedShort();
			case 4 -> Integer.toUnsignedLong(unitStream.readInt());
			case 8 -> unitStream.readLong();
			default -> throw new IllegalArgumentException("Unsupported DWARF address size: " + addressSize);
		};
	}

	private static long readOffset(ByteInputStream unitStream, boolean dwarf64) throws IOException {
		return dwarf64 ? unitStream.readLong() : Integer.toUnsignedLong(unitStream.readInt());
	}

	private static long readRefAddress(
			ByteInputStream unitStream, DwarfVersion version, int addressSize, boolean dwarf64) throws IOException {
		if (version == DwarfVersion.DWARF2) {
			return readAddress(unitStream, addressSize);
		}
		return readOffset(unitStream, dwarf64);
	}

	private static void requireStringTable(DwarfStringTable stringTable, DwarfForm form) {
		if (stringTable == null) {
			throw new IllegalArgumentException("Missing string table required for form: " + form);
		}
	}

	/**
	 * Serializes this .debug_info model to raw section bytes.
	 *
	 * @return encoded .debug_info payload
	 * @throws IOException if serialization fails
	 */
	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		write(baos);
		return baos.toByteArray();
	}

	/**
	 * Writes this .debug_info model to the provided output stream.
	 *
	 * @param outputStream destination stream
	 * @throws IOException if writing fails
	 */
	public void write(OutputStream outputStream) throws IOException {
		ByteOutputStream sectionBos = new ByteOutputStream(outputStream, byteOrder);
		for (DwarfCompilationUnit compilationUnit : compilationUnits) {
			DwarfAbbreviationValidator.validateSupportedIn(
					abbreviationTable, compilationUnit.getHeader().version());
			byte[] unitPayload = buildUnitPayload(compilationUnit);
			if (compilationUnit.getHeader().dwarf64()) {
				sectionBos.writeInt(0xffff_ffff);
				sectionBos.writeLong(unitPayload.length);
			} else {
				sectionBos.writeInt(unitPayload.length);
			}
			sectionBos.write(unitPayload);
		}
	}

	private byte[] buildUnitPayload(DwarfCompilationUnit compilationUnit) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ByteOutputStream bos = new ByteOutputStream(baos, byteOrder);
		DwarfCompilationUnitHeader header = compilationUnit.getHeader();

		bos.writeShort(header.version().getValue());
		if (header.version().isAtLeast(DwarfVersion.DWARF5)) {
			if (header.unitType() == null) {
				throw new IllegalArgumentException("DWARF5 compilation units must define a unit type");
			}
			bos.writeByte(header.unitType().getValue());
			bos.writeByte(header.addressSize());
			writeOffset(bos, header.abbreviationOffset(), header.dwarf64());
		} else {
			writeOffset(bos, header.abbreviationOffset(), header.dwarf64());
			bos.writeByte(header.addressSize());
		}

		for (DwarfDie die : compilationUnit.getDies()) {
			writeDie(bos, die, header.version(), header.addressSize(), header.dwarf64());
		}
		return baos.toByteArray();
	}

	private void writeDie(ByteOutputStream bos, DwarfDie die, DwarfVersion version, int addressSize, boolean dwarf64)
			throws IOException {
		Leb128Utils.writeUleb128(bos, die.getAbbreviationCode());
		DwarfAbbreviationDeclaration declaration =
				abbreviationTable.getDeclarationsByCode().get(die.getAbbreviationCode());
		if (declaration == null) {
			throw new IllegalArgumentException(
					"Unknown abbreviation code while writing DIE: " + die.getAbbreviationCode());
		}
		if (declaration.getAttributes().size() != die.getAttributes().size()) {
			throw new IllegalArgumentException("DIE attribute count does not match abbreviation declaration");
		}

		for (int index = 0; index < declaration.getAttributes().size(); index++) {
			DwarfAbbreviationAttribute attributeSpec =
					declaration.getAttributes().get(index);
			DwarfDieAttribute attribute = die.getAttributes().get(index);
			if (attribute.rawAttributeCode() != attributeSpec.getAttributeCode()) {
				throw new IllegalArgumentException(
						"DIE attribute at index " + index + " does not match abbreviation declaration attribute code");
			}
			if (attribute.form() != attributeSpec.getForm()) {
				throw new IllegalArgumentException(
						"DIE attribute at index " + index + " does not match abbreviation declaration form");
			}
			writeValue(
					bos,
					attributeSpec.getForm(),
					attribute.value(),
					attributeSpec.getImplicitConstValue(),
					version,
					addressSize,
					dwarf64);
		}

		if (declaration.hasChildren()) {
			for (DwarfDie child : die.getChildren()) {
				writeDie(bos, child, version, addressSize, dwarf64);
			}
			bos.writeByte(0);
		}
	}

	private void writeValue(
			ByteOutputStream bos,
			DwarfForm form,
			DwarfValue value,
			Long implicitConstValue,
			DwarfVersion version,
			int addressSize,
			boolean dwarf64)
			throws IOException {
		switch (form) {
			case DW_FORM_addr -> writeAddress(bos, ((DwarfAddressValue) value).value(), addressSize);
			case DW_FORM_data1 -> bos.writeByte(requireUnsignedByte(((DwarfUnsignedValue) value).value(), form.name()));
			case DW_FORM_data2 -> bos.writeShort(
					requireUnsignedShort(((DwarfUnsignedValue) value).value(), form.name()));
			case DW_FORM_data4, DW_FORM_sec_offset -> bos.writeInt(
					requireUnsignedInt(((DwarfUnsignedValue) value).value(), form.name()));
			case DW_FORM_data8 -> bos.writeLong(((DwarfUnsignedValue) value).value());
			case DW_FORM_udata -> Leb128Utils.writeUleb128(bos, ((DwarfUnsignedValue) value).value());
			case DW_FORM_sdata -> Leb128Utils.writeSleb128(bos, ((DwarfUnsignedValue) value).value());
			case DW_FORM_string -> bos.writeNullTerminatedString(
					((DwarfStringValue) value).value(), StandardCharsets.UTF_8);
			case DW_FORM_strp, DW_FORM_line_strp -> writeOffset(
					bos, ((DwarfStringReferenceValue) value).offset(), dwarf64);
			case DW_FORM_ref1 -> bos.writeByte(
					requireUnsignedByte(((DwarfReferenceValue) value).offset(), form.name()));
			case DW_FORM_ref2 -> bos.writeShort(
					requireUnsignedShort(((DwarfReferenceValue) value).offset(), form.name()));
			case DW_FORM_ref4 -> bos.writeInt(requireUnsignedInt(((DwarfReferenceValue) value).offset(), form.name()));
			case DW_FORM_ref8 -> bos.writeLong(((DwarfReferenceValue) value).offset());
			case DW_FORM_ref_udata -> Leb128Utils.writeUleb128(bos, ((DwarfReferenceValue) value).offset());
			case DW_FORM_ref_addr -> writeRefAddress(
					bos, ((DwarfReferenceValue) value).offset(), version, addressSize, dwarf64);
			case DW_FORM_flag -> bos.writeByte(((DwarfFlagValue) value).value() ? 1 : 0);
			case DW_FORM_flag_present -> {}
			case DW_FORM_block1 -> writeSizedBlock(bos, ((DwarfBlockValue) value).bytes(), 1);
			case DW_FORM_block2 -> writeSizedBlock(bos, ((DwarfBlockValue) value).bytes(), 2);
			case DW_FORM_block4 -> writeSizedBlock(bos, ((DwarfBlockValue) value).bytes(), 4);
			case DW_FORM_block, DW_FORM_exprloc -> {
				byte[] blockBytes = ((DwarfBlockValue) value).bytes();
				Leb128Utils.writeUleb128(bos, blockBytes.length);
				bos.write(blockBytes);
			}
			case DW_FORM_implicit_const -> {
				if (implicitConstValue == null) {
					throw new IllegalArgumentException("Missing implicit const value in abbreviation spec");
				}
			}
			case DW_FORM_indirect,
					DW_FORM_ref_sig8,
					DW_FORM_strx,
					DW_FORM_addrx,
					DW_FORM_ref_sup4,
					DW_FORM_strp_sup,
					DW_FORM_data16,
					DW_FORM_loclistx,
					DW_FORM_rnglistx,
					DW_FORM_ref_sup8,
					DW_FORM_strx1,
					DW_FORM_strx2,
					DW_FORM_strx3,
					DW_FORM_strx4,
					DW_FORM_addrx1,
					DW_FORM_addrx2,
					DW_FORM_addrx3,
					DW_FORM_addrx4 -> throw new IllegalArgumentException("Form not implemented yet: " + form);
		}
	}

	private static void writeSizedBlock(ByteOutputStream bos, byte[] blockBytes, int lengthFieldSize)
			throws IOException {
		long maxLength =
				switch (lengthFieldSize) {
					case 1 -> 0xffL;
					case 2 -> 0xffffL;
					case 4 -> 0xffff_ffffL;
					default -> throw new IllegalArgumentException(
							"Unsupported block length field size: " + lengthFieldSize);
				};
		if (blockBytes.length > maxLength) {
			throw new IllegalArgumentException("Block length does not fit in " + lengthFieldSize + " bytes");
		}
		switch (lengthFieldSize) {
			case 1 -> bos.writeByte(blockBytes.length);
			case 2 -> bos.writeShort(blockBytes.length);
			case 4 -> bos.writeInt(blockBytes.length);
		}
		bos.write(blockBytes);
	}

	private static void writeOffset(ByteOutputStream bos, long value, boolean dwarf64) throws IOException {
		if (dwarf64) {
			bos.writeLong(value);
		} else {
			bos.writeInt(requireUnsignedInt(value, "DWARF offset"));
		}
	}

	private static void writeRefAddress(
			ByteOutputStream bos, long value, DwarfVersion version, int addressSize, boolean dwarf64)
			throws IOException {
		if (version == DwarfVersion.DWARF2) {
			writeAddress(bos, value, addressSize);
			return;
		}
		writeOffset(bos, value, dwarf64);
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

	private static void writeAddress(ByteOutputStream bos, long value, int addressSize) throws IOException {
		switch (addressSize) {
			case 1 -> bos.writeByte(requireUnsignedByte(value, "DWARF address"));
			case 2 -> bos.writeShort(requireUnsignedShort(value, "DWARF address"));
			case 4 -> bos.writeInt(requireUnsignedInt(value, "DWARF address"));
			case 8 -> bos.writeLong(value);
			default -> throw new IllegalArgumentException("Unsupported DWARF address size: " + addressSize);
		}
	}

	/**
	 * Returns the first attribute value of the requested name for the given DIE.
	 *
	 * @param die DIE to inspect
	 * @param attributeName requested attribute
	 * @return matching attribute value or null when absent
	 */
	public static DwarfValue getAttributeValue(DwarfDie die, DwarfAttributeName attributeName) {
		for (DwarfDieAttribute attribute : die.getAttributes()) {
			if (attribute.attributeName() == attributeName) {
				return attribute.value();
			}
		}
		return null;
	}
}
