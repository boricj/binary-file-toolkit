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
 * Contains magic, version, flags, parent/compilation unit info, and section offsets/sizes.
 * Structure (per CTF spec v3):
 * - cth_magic (2), cth_version (1), cth_flags (1)
 * - cth_parlabel (4), cth_parname (4), cth_cuname (4)
 * - cth_lbloff (4), cth_lbllen (4)
 * - cth_objoff (4), cth_objlen (4)
 * - cth_funcoff (4), cth_funclen (4)
 * - cth_typeoff (4), cth_typelen (4)
 * - cth_stroff (4), cth_strlen (4)
 * Total: 40 bytes
 */
public class CTFHeader {
	private static final int MAGIC = 0xdff2;
	// file-wide flags (ctp_flags in the preamble)
	public static final int FLAG_COMPRESS = 0x1;      // compressed with zlib
	public static final int FLAG_NEWFUNCINFO = 0x2;   // new-format function info section

	private int magic; // 0x00 (2 bytes)
	private byte version; // 0x02 (1 byte)
	private byte flags; // 0x03 (1 byte)
	private int parLabel; // 0x04 (4 bytes) - parent label
	private int parName; // 0x08 (4 bytes) - parent name offset
	private int cuName; // 0x0c (4 bytes) - compilation unit name offset
	private int lblOffset; // 0x10 (4 bytes) - label section offset
	private int lblSize; // 0x14 (4 bytes) - label section size
	private int objOffset; // 0x18 (4 bytes) - object section offset
	private int objSize; // 0x1c (4 bytes) - object section size
	private int funcOffset; // 0x20 (4 bytes) - function section offset
	private int funcSize; // 0x24 (4 bytes) - function section size
	private int varOffset; // 0x28 (4 bytes) - variable section offset
	private int varSize; // 0x2c (4 bytes) - variable section size
	private int typeOffset; // 0x30 (4 bytes) - type section offset
	private int typeSize; // 0x34 (4 bytes) - type section size
	private int strOffset; // 0x38 (4 bytes) - string section offset
	private int strSize; // 0x3c (4 bytes) - string section size

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
		header.lblSize = stream.readInt();
		header.objOffset = stream.readInt();
		header.objSize = stream.readInt();
		header.funcOffset = stream.readInt();
		header.funcSize = stream.readInt();
		header.varOffset = stream.readInt();
		header.varSize = stream.readInt();
		header.typeOffset = stream.readInt();
		header.typeSize = stream.readInt();
		header.strOffset = stream.readInt();
		header.strSize = stream.readInt();

		return header;
	}

	/**
	 * Serialize this header back out. Offsets and sizes must have been set
	 * appropriately before calling.
	 */
	public void write(ByteOutputStream stream) throws IOException {
		stream.writeShort((short) magic);
		stream.writeByte(version);
		stream.writeByte(flags);
		stream.writeInt(parLabel);
		stream.writeInt(parName);
		stream.writeInt(cuName);
		stream.writeInt(lblOffset);
		stream.writeInt(lblSize);
		stream.writeInt(objOffset);
		stream.writeInt(objSize);
		stream.writeInt(funcOffset);
		stream.writeInt(funcSize);
		stream.writeInt(varOffset);
		stream.writeInt(varSize);
		stream.writeInt(typeOffset);
		stream.writeInt(typeSize);
		stream.writeInt(strOffset);
		stream.writeInt(strSize);
	}

	public int getVarOffset() { return varOffset; }
	public void setVarOffset(int varOffset) { this.varOffset = varOffset; }
	public int getVarSize() { return varSize; }
	public void setVarSize(int varSize) { this.varSize = varSize; }

	public byte[] toBytes() throws IOException {
		ByteOutputStream out = new ByteOutputStream();
		write(out);
		return out.toByteArray();
	}

	// Getters and setters
	public int getMagic() {
		return magic;
	}

	public void setMagic(int magic) {
		this.magic = magic;
	}

	public byte getVersion() {
		return version;
	}

	public void setVersion(byte version) {
		this.version = version;
	}

	public byte getFlags() {
		return flags;
	}

	public void setFlags(byte flags) {
		this.flags = flags;
	}

	public int getParLabel() {
		return parLabel;
	}

	public void setParLabel(int parLabel) {
		this.parLabel = parLabel;
	}

	public int getParName() {
		return parName;
	}

	public void setParName(int parName) {
		this.parName = parName;
	}

	public int getCuName() {
		return cuName;
	}

	public void setCuName(int cuName) {
		this.cuName = cuName;
	}

	public int getLblOffset() {
		return lblOffset;
	}

	public void setLblOffset(int lblOffset) {
		this.lblOffset = lblOffset;
	}

	public int getLblSize() {
		return lblSize;
	}

	public void setLblSize(int lblSize) {
		this.lblSize = lblSize;
	}

	public int getObjOffset() {
		return objOffset;
	}

	public void setObjOffset(int objOffset) {
		this.objOffset = objOffset;
	}

	public int getObjSize() {
		return objSize;
	}

	public void setObjSize(int objSize) {
		this.objSize = objSize;
	}

	public int getFuncOffset() {
		return funcOffset;
	}

	public void setFuncOffset(int funcOffset) {
		this.funcOffset = funcOffset;
	}

	public int getFuncSize() {
		return funcSize;
	}

	public void setFuncSize(int funcSize) {
		this.funcSize = funcSize;
	}

	public int getTypeOffset() {
		return typeOffset;
	}

	public void setTypeOffset(int typeOffset) {
		this.typeOffset = typeOffset;
	}

	public int getTypeSize() {
		return typeSize;
	}

	public void setTypeSize(int typeSize) {
		this.typeSize = typeSize;
	}

	public int getStrOffset() {
		return strOffset;
	}

	public void setStrOffset(int strOffset) {
		this.strOffset = strOffset;
	}

	public int getStrSize() {
		return strSize;
	}

	public void setStrSize(int strSize) {
		this.strSize = strSize;
	}
}
