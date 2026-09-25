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
package net.boricj.bft.ctf;

import java.io.IOException;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;

/**
 * Represents the CTF dictionary header.
 * Contains magic, version, flags, parent/compilation unit info, and section offsets.
 * Structure (per CTF spec v3):
 * - cth_magic (2), cth_version (1), cth_flags (1)
 * - cth_parlabel (4), cth_parname (4), cth_cuname (4)
 * - cth_lbloff (4), cth_objtoff (4), cth_funcoff (4)
 * - cth_objtidxoff (4), cth_funcidxoff (4), cth_varoff (4)
 * - cth_typeoff (4), cth_stroff (4), cth_strlen (4)
 * Total: 52 bytes
 */
public class CTFHeader {
	private static final int MAGIC = 0xdff2;
	// file-wide flags (ctp_flags in the preamble)
	/** Dictionary payload is zlib-compressed after the fixed header. */
	public static final int FLAG_COMPRESS = 0x1; // compressed with zlib
	/** Function info subsection uses the newer function-info encoding. */
	public static final int FLAG_NEWFUNCINFO = 0x2; // new-format function info section

	private int magic; // 0x00 (2 bytes)
	private byte version; // 0x02 (1 byte)
	private byte flags; // 0x03 (1 byte)
	private int parLabel; // 0x04 (4 bytes) - parent label
	private int parName; // 0x08 (4 bytes) - parent name offset
	private int cuName; // 0x0c (4 bytes) - compilation unit name offset
	private int lblOffset; // 0x10 (4 bytes) - label section offset
	private int objOffset; // 0x14 (4 bytes) - object section offset
	private int funcOffset; // 0x18 (4 bytes) - function info section offset
	private int objIndexOffset; // 0x1c (4 bytes) - object index section offset
	private int funcIndexOffset; // 0x20 (4 bytes) - function index section offset
	private int varOffset; // 0x24 (4 bytes) - variable section offset
	private int typeOffset; // 0x28 (4 bytes) - type section offset
	private int strOffset; // 0x2c (4 bytes) - string section offset
	private int strSize; // 0x30 (4 bytes) - string section size

	// Derived sizes from section offsets.
	private int lblSize;
	private int objSize;
	private int funcInfoSize;
	private int objIndexSize;
	private int funcIndexSize;
	private int funcSize;
	private int varSize;
	private int typeSize;

	/**
	 * Creates an empty header model.
	 */
	public CTFHeader() {}

	/**
	 * Parses a CTF header from the current stream position.
	 *
	 * @param stream input stream positioned at the start of the 52-byte CTF header
	 * @return parsed CTF header
	 * @throws IOException if the header cannot be read
	 * @throws CTFException if magic or section layout is invalid
	 */
	public static CTFHeader parse(ByteInputStream stream) throws IOException, CTFException {
		CTFHeader header = new CTFHeader();
		header.magic = stream.readUnsignedShort();
		if (header.magic != MAGIC) {
			throw new CTFException("Invalid CTF magic: 0x" + Integer.toHexString(header.magic));
		}

		header.version = stream.readByte();
		header.flags = stream.readByte();
		header.parLabel = stream.readInt();
		header.parName = stream.readInt();
		header.cuName = stream.readInt();
		header.lblOffset = stream.readInt();
		header.objOffset = stream.readInt();
		header.funcOffset = stream.readInt();
		header.objIndexOffset = stream.readInt();
		header.funcIndexOffset = stream.readInt();
		header.varOffset = stream.readInt();
		header.typeOffset = stream.readInt();
		header.strOffset = stream.readInt();
		header.strSize = stream.readInt();

		if (!(header.lblOffset <= header.objOffset
				&& header.objOffset <= header.funcOffset
				&& header.funcOffset <= header.objIndexOffset
				&& header.objIndexOffset <= header.funcIndexOffset
				&& header.funcIndexOffset <= header.varOffset
				&& header.varOffset <= header.typeOffset
				&& header.typeOffset <= header.strOffset)) {
			throw new CTFException("Invalid CTF section ordering in header");
		}

		header.lblSize = header.objOffset - header.lblOffset;
		header.objSize = header.funcOffset - header.objOffset;
		header.funcInfoSize = header.objIndexOffset - header.funcOffset;
		header.objIndexSize = header.funcIndexOffset - header.objIndexOffset;
		header.funcIndexSize = header.varOffset - header.funcIndexOffset;
		header.funcSize = header.varOffset - header.funcOffset;
		header.varSize = header.typeOffset - header.varOffset;
		header.typeSize = header.strOffset - header.typeOffset;

		return header;
	}

	/**
	 * Serialize this header back out. Offsets and sizes must have been set
	 * appropriately before calling.
	 *
	 * @param stream destination stream
	 * @throws IOException if writing fails
	 */
	public void write(ByteOutputStream stream) throws IOException {
		stream.writeShort((short) magic);
		stream.writeByte(version);
		stream.writeByte(flags);
		stream.writeInt(parLabel);
		stream.writeInt(parName);
		stream.writeInt(cuName);
		stream.writeInt(lblOffset);
		stream.writeInt(objOffset);
		stream.writeInt(funcOffset);
		stream.writeInt(objIndexOffset);
		stream.writeInt(funcIndexOffset);
		stream.writeInt(varOffset);
		stream.writeInt(typeOffset);
		stream.writeInt(strOffset);
		stream.writeInt(strSize);
	}

	/**
	 * Returns object-index subsection offset.
	 *
	 * @return object-index subsection offset relative to end of header
	 */
	public int getObjIndexOffset() {
		return objIndexOffset;
	}

	/**
	 * Sets object-index subsection offset.
	 *
	 * @param objIndexOffset object-index subsection offset relative to end of header
	 */
	public void setObjIndexOffset(int objIndexOffset) {
		this.objIndexOffset = objIndexOffset;
	}

	/**
	 * Returns function-index subsection offset.
	 *
	 * @return function-index subsection offset relative to end of header
	 */
	public int getFuncIndexOffset() {
		return funcIndexOffset;
	}

	/**
	 * Sets function-index subsection offset.
	 *
	 * @param funcIndexOffset function-index subsection offset relative to end of header
	 */
	public void setFuncIndexOffset(int funcIndexOffset) {
		this.funcIndexOffset = funcIndexOffset;
	}

	/**
	 * Returns variable subsection offset.
	 *
	 * @return variable subsection offset relative to end of header
	 */
	public int getVarOffset() {
		return varOffset;
	}

	/**
	 * Sets variable subsection offset.
	 *
	 * @param varOffset variable subsection offset relative to end of header
	 */
	public void setVarOffset(int varOffset) {
		this.varOffset = varOffset;
	}

	/**
	 * Returns variable subsection size.
	 *
	 * @return variable subsection size in bytes
	 */
	public int getVarSize() {
		return varSize;
	}

	/**
	 * Sets variable subsection size.
	 *
	 * @param varSize variable subsection size in bytes
	 */
	public void setVarSize(int varSize) {
		this.varSize = varSize;
	}

	/**
	 * Serializes this header to a 52-byte array.
	 *
	 * @return encoded header bytes
	 * @throws IOException if writing fails
	 */
	public byte[] toBytes() throws IOException {
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		ByteOutputStream out = ByteOutputStream.asLittleEndian(baos);
		write(out);
		return baos.toByteArray();
	}

	// Getters and setters
	/**
	 * Returns CTF magic value.
	 *
	 * @return CTF magic value
	 */
	public int getMagic() {
		return magic;
	}

	/**
	 * Sets CTF magic value.
	 *
	 * @param magic CTF magic value
	 */
	public void setMagic(int magic) {
		this.magic = magic;
	}

	/**
	 * Returns CTF version.
	 *
	 * @return CTF version byte
	 */
	public byte getVersion() {
		return version;
	}

	/**
	 * Sets CTF version.
	 *
	 * @param version CTF version byte
	 */
	public void setVersion(byte version) {
		this.version = version;
	}

	/**
	 * Returns file-level flags.
	 *
	 * @return bitmask of {@code FLAG_*} values
	 */
	public byte getFlags() {
		return flags;
	}

	/**
	 * Sets file-level flags.
	 *
	 * @param flags bitmask of {@code FLAG_*} values
	 */
	public void setFlags(byte flags) {
		this.flags = flags;
	}

	/**
	 * Returns parent label string-table offset.
	 *
	 * @return parent label string-table offset
	 */
	public int getParLabel() {
		return parLabel;
	}

	/**
	 * Sets parent label string-table offset.
	 *
	 * @param parLabel parent label string-table offset
	 */
	public void setParLabel(int parLabel) {
		this.parLabel = parLabel;
	}

	/**
	 * Returns parent name string-table offset.
	 *
	 * @return parent name string-table offset
	 */
	public int getParName() {
		return parName;
	}

	/**
	 * Sets parent name string-table offset.
	 *
	 * @param parName parent name string-table offset
	 */
	public void setParName(int parName) {
		this.parName = parName;
	}

	/**
	 * Returns compilation-unit name string-table offset.
	 *
	 * @return compilation-unit name string-table offset
	 */
	public int getCuName() {
		return cuName;
	}

	/**
	 * Sets compilation-unit name string-table offset.
	 *
	 * @param cuName compilation-unit name string-table offset
	 */
	public void setCuName(int cuName) {
		this.cuName = cuName;
	}

	/**
	 * Returns label subsection offset.
	 *
	 * @return label subsection offset relative to end of header
	 */
	public int getLblOffset() {
		return lblOffset;
	}

	/**
	 * Sets label subsection offset.
	 *
	 * @param lblOffset label subsection offset relative to end of header
	 */
	public void setLblOffset(int lblOffset) {
		this.lblOffset = lblOffset;
	}

	/**
	 * Returns label subsection size.
	 *
	 * @return label subsection size in bytes
	 */
	public int getLblSize() {
		return lblSize;
	}

	/**
	 * Sets label subsection size.
	 *
	 * @param lblSize label subsection size in bytes
	 */
	public void setLblSize(int lblSize) {
		this.lblSize = lblSize;
	}

	/**
	 * Returns object subsection offset.
	 *
	 * @return object subsection offset relative to end of header
	 */
	public int getObjOffset() {
		return objOffset;
	}

	/**
	 * Sets object subsection offset.
	 *
	 * @param objOffset object subsection offset relative to end of header
	 */
	public void setObjOffset(int objOffset) {
		this.objOffset = objOffset;
	}

	/**
	 * Returns object subsection size.
	 *
	 * @return object subsection size in bytes
	 */
	public int getObjSize() {
		return objSize;
	}

	/**
	 * Sets object subsection size.
	 *
	 * @param objSize object subsection size in bytes
	 */
	public void setObjSize(int objSize) {
		this.objSize = objSize;
	}

	/**
	 * Returns function-info subsection offset.
	 *
	 * @return function-info subsection offset relative to end of header
	 */
	public int getFuncOffset() {
		return funcOffset;
	}

	/**
	 * Sets function-info subsection offset.
	 *
	 * @param funcOffset function-info subsection offset relative to end of header
	 */
	public void setFuncOffset(int funcOffset) {
		this.funcOffset = funcOffset;
	}

	/**
	 * Returns function-info subsection size.
	 *
	 * @return function-info subsection size in bytes
	 */
	public int getFuncInfoSize() {
		return funcInfoSize;
	}

	/**
	 * Sets function-info subsection size.
	 *
	 * @param funcInfoSize function-info subsection size in bytes
	 */
	public void setFuncInfoSize(int funcInfoSize) {
		this.funcInfoSize = funcInfoSize;
	}

	/**
	 * Returns object-index subsection size.
	 *
	 * @return object-index subsection size in bytes
	 */
	public int getObjIndexSize() {
		return objIndexSize;
	}

	/**
	 * Sets object-index subsection size.
	 *
	 * @param objIndexSize object-index subsection size in bytes
	 */
	public void setObjIndexSize(int objIndexSize) {
		this.objIndexSize = objIndexSize;
	}

	/**
	 * Returns function-index subsection size.
	 *
	 * @return function-index subsection size in bytes
	 */
	public int getFuncIndexSize() {
		return funcIndexSize;
	}

	/**
	 * Sets function-index subsection size.
	 *
	 * @param funcIndexSize function-index subsection size in bytes
	 */
	public void setFuncIndexSize(int funcIndexSize) {
		this.funcIndexSize = funcIndexSize;
	}

	/**
	 * Returns total function-area size.
	 *
	 * @return total function-area size in bytes
	 */
	public int getFuncSize() {
		return funcSize;
	}

	/**
	 * Sets total function-area size.
	 *
	 * @param funcSize total function-area size in bytes
	 */
	public void setFuncSize(int funcSize) {
		this.funcSize = funcSize;
	}

	/**
	 * Returns type subsection offset.
	 *
	 * @return type subsection offset relative to end of header
	 */
	public int getTypeOffset() {
		return typeOffset;
	}

	/**
	 * Sets type subsection offset.
	 *
	 * @param typeOffset type subsection offset relative to end of header
	 */
	public void setTypeOffset(int typeOffset) {
		this.typeOffset = typeOffset;
	}

	/**
	 * Returns type subsection size.
	 *
	 * @return type subsection size in bytes
	 */
	public int getTypeSize() {
		return typeSize;
	}

	/**
	 * Sets type subsection size.
	 *
	 * @param typeSize type subsection size in bytes
	 */
	public void setTypeSize(int typeSize) {
		this.typeSize = typeSize;
	}

	/**
	 * Returns string subsection offset.
	 *
	 * @return string subsection offset relative to end of header
	 */
	public int getStrOffset() {
		return strOffset;
	}

	/**
	 * Sets string subsection offset.
	 *
	 * @param strOffset string subsection offset relative to end of header
	 */
	public void setStrOffset(int strOffset) {
		this.strOffset = strOffset;
	}

	/**
	 * Returns string subsection size.
	 *
	 * @return string subsection size in bytes
	 */
	public int getStrSize() {
		return strSize;
	}

	/**
	 * Sets string subsection size.
	 *
	 * @param strSize string subsection size in bytes
	 */
	public void setStrSize(int strSize) {
		this.strSize = strSize;
	}
}
