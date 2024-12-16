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
package net.boricj.bft.elf.machines.mips;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.boricj.bft.elf.ElfFile;
import net.boricj.bft.elf.ElfSection;
import net.boricj.bft.elf.ElfSectionFlags;
import net.boricj.bft.elf.constants.ElfSectionType;

public class ElfAbiFlags_Mips extends ElfSection {
	private short version;
	private byte isa_level;
	private byte isa_rev;
	private byte gpr_size;
	private byte cpr1_size;
	private byte cpr2_size;
	private byte fp_abi;
	private int isa_ext;
	private int ases;
	private int flags1;
	private int flags2;

	public ElfAbiFlags_Mips(ElfFile elf, String name, ElfSectionFlags flags, long addralign, byte[] bytes) {
		this(elf, name, flags, 0, 0, addralign, 0);
	}

	public ElfAbiFlags_Mips(
			ElfFile elf, String name, ElfSectionFlags flags, long addralign, long entsize, byte[] bytes) {
		this(elf, name, flags, 0, 0, addralign, entsize);
	}

	public ElfAbiFlags_Mips(
			ElfFile elf, String name, ElfSectionFlags flags, long addr, long offset, long addralign, long entsize) {
		super(elf, name, flags, addr, offset, addralign, entsize);
	}

	public ElfAbiFlags_Mips(
			ElfFile elf,
			ElfFile.Parser parser,
			ElfSectionFlags flags,
			long addr,
			long offset,
			long size,
			int link,
			int info,
			long addralign,
			long entsize)
			throws IOException {
		super(elf, "", flags, addr, offset, addralign, entsize);

		FileInputStream fis = parser.getFileInputStream();

		fis.getChannel().position(offset);
		DataInput dataInput = elf.wrap(fis);

		setVersion(dataInput.readShort());
		setIsaLevel(dataInput.readByte());
		setIsaRev(dataInput.readByte());
		setGprSize(dataInput.readByte());
		setCpr1Size(dataInput.readByte());
		setCpr2Size(dataInput.readByte());
		setFpAbi(dataInput.readByte());
		setIsaExt(dataInput.readInt());
		setAses(dataInput.readInt());
		setFlags1(dataInput.readInt());
		setFlags2(dataInput.readInt());
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		DataOutput dataOutput = getElfFile().wrap(outputStream);

		dataOutput.writeShort(version);
		dataOutput.writeByte(isa_level);
		dataOutput.writeByte(isa_rev);
		dataOutput.writeByte(gpr_size);
		dataOutput.writeByte(cpr1_size);
		dataOutput.writeByte(cpr2_size);
		dataOutput.writeByte(fp_abi);
		dataOutput.writeInt(isa_ext);
		dataOutput.writeInt(ases);
		dataOutput.writeInt(flags1);
		dataOutput.writeInt(flags2);
	}

	@Override
	public long getLength() {
		return 24;
	}

	@Override
	public int getType() {
		return ElfSectionType.SHT_MIPS_ABIFLAGS.getValue();
	}

	public short getVersion() {
		return version;
	}

	public void setVersion(short version) {
		this.version = version;
	}

	public byte getIsaLevel() {
		return isa_level;
	}

	public void setIsaLevel(byte isa_level) {
		this.isa_level = isa_level;
	}

	public byte getIsaRev() {
		return isa_rev;
	}

	public void setIsaRev(byte isa_rev) {
		this.isa_rev = isa_rev;
	}

	public byte getGprSize() {
		return gpr_size;
	}

	public void setGprSize(byte gpr_size) {
		this.gpr_size = gpr_size;
	}

	public byte getCpr1Size() {
		return cpr1_size;
	}

	public void setCpr1Size(byte cpr1_size) {
		this.cpr1_size = cpr1_size;
	}

	public byte getCpr2Size() {
		return cpr2_size;
	}

	public void setCpr2Size(byte cpr2_size) {
		this.cpr2_size = cpr2_size;
	}

	public byte getFpAbi() {
		return fp_abi;
	}

	public void setFpAbi(byte fp_abi) {
		this.fp_abi = fp_abi;
	}

	public int getIsaExt() {
		return isa_ext;
	}

	public void setIsaExt(int isa_ext) {
		this.isa_ext = isa_ext;
	}

	public int getAses() {
		return ases;
	}

	public void setAses(int ases) {
		this.ases = ases;
	}

	public int getFlags1() {
		return flags1;
	}

	public void setFlags1(int flags1) {
		this.flags1 = flags1;
	}

	public int getFlags2() {
		return flags2;
	}

	public void setFlags2(int flags2) {
		this.flags2 = flags2;
	}
}
