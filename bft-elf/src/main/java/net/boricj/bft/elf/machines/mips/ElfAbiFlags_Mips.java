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

/**
 * MIPS ABI flags section containing architecture-specific ABI information.
 * This section describes the MIPS ISA level, register sizes, floating-point ABI, and other flags.
 */
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

	/**
	 * Creates a new MIPS ABI flags section.
	 *
	 * @param elf the parent ELF file
	 * @param name section name
	 * @param flags section flags
	 * @param addralign address alignment
	 * @param bytes initial content (unused)
	 */
	public ElfAbiFlags_Mips(ElfFile elf, String name, ElfSectionFlags flags, long addralign, byte[] bytes) {
		this(elf, name, flags, 0, 0, addralign, 0);
	}

	/**
	 * Creates a new MIPS ABI flags section with entry size.
	 *
	 * @param elf the parent ELF file
	 * @param name section name
	 * @param flags section flags
	 * @param addralign address alignment
	 * @param entsize entry size
	 * @param bytes initial content (unused)
	 */
	public ElfAbiFlags_Mips(
			ElfFile elf, String name, ElfSectionFlags flags, long addralign, long entsize, byte[] bytes) {
		this(elf, name, flags, 0, 0, addralign, entsize);
	}

	/**
	 * Creates a new MIPS ABI flags section with full parameters.
	 *
	 * @param elf the parent ELF file
	 * @param name section name
	 * @param flags section flags
	 * @param addr virtual address
	 * @param offset file offset
	 * @param addralign address alignment
	 * @param entsize entry size
	 */
	public ElfAbiFlags_Mips(
			ElfFile elf, String name, ElfSectionFlags flags, long addr, long offset, long addralign, long entsize) {
		super(elf, name, flags, addr, offset, addralign, entsize);
	}

	/**
	 * Parses a MIPS ABI flags section from an input stream.
	 *
	 * @param elf the parent ELF file
	 * @param parser parser containing input stream
	 * @param flags section flags
	 * @param addr virtual address
	 * @param offset file offset
	 * @param size section size
	 * @param link link to another section
	 * @param info additional section information
	 * @param addralign address alignment
	 * @param entsize entry size
	 * @throws IOException if reading fails
	 */
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

	/**
	 * Returns the ABI flags version.
	 *
	 * @return version field
	 */
	public short getVersion() {
		return version;
	}

	/**
	 * Sets the ABI flags version.
	 *
	 * @param version version field value
	 */
	public void setVersion(short version) {
		this.version = version;
	}

	/**
	 * Returns the ISA level.
	 *
	 * @return ISA level
	 */
	public byte getIsaLevel() {
		return isa_level;
	}

	/**
	 * Sets the ISA level.
	 *
	 * @param isa_level ISA level value
	 */
	public void setIsaLevel(byte isa_level) {
		this.isa_level = isa_level;
	}

	/**
	 * Returns the ISA revision.
	 *
	 * @return ISA revision
	 */
	public byte getIsaRev() {
		return isa_rev;
	}

	/**
	 * Sets the ISA revision.
	 *
	 * @param isa_rev ISA revision value
	 */
	public void setIsaRev(byte isa_rev) {
		this.isa_rev = isa_rev;
	}

	/**
	 * Returns the size of general purpose registers.
	 *
	 * @return GPR size
	 */
	public byte getGprSize() {
		return gpr_size;
	}

	/**
	 * Sets the size of general purpose registers.
	 *
	 * @param gpr_size GPR size value
	 */
	public void setGprSize(byte gpr_size) {
		this.gpr_size = gpr_size;
	}

	/**
	 * Returns the size of coprocessor 1 registers.
	 *
	 * @return CPR1 size
	 */
	public byte getCpr1Size() {
		return cpr1_size;
	}

	/**
	 * Sets the size of coprocessor 1 registers.
	 *
	 * @param cpr1_size CPR1 size value
	 */
	public void setCpr1Size(byte cpr1_size) {
		this.cpr1_size = cpr1_size;
	}

	/**
	 * Returns the size of coprocessor 2 registers.
	 *
	 * @return CPR2 size
	 */
	public byte getCpr2Size() {
		return cpr2_size;
	}

	/**
	 * Sets the size of coprocessor 2 registers.
	 *
	 * @param cpr2_size CPR2 size value
	 */
	public void setCpr2Size(byte cpr2_size) {
		this.cpr2_size = cpr2_size;
	}

	/**
	 * Returns the floating-point ABI.
	 *
	 * @return FP ABI value
	 */
	public byte getFpAbi() {
		return fp_abi;
	}

	/**
	 * Sets the floating-point ABI.
	 *
	 * @param fp_abi FP ABI value
	 */
	public void setFpAbi(byte fp_abi) {
		this.fp_abi = fp_abi;
	}

	/**
	 * Returns the ISA extension.
	 *
	 * @return ISA extension value
	 */
	public int getIsaExt() {
		return isa_ext;
	}

	/**
	 * Sets the ISA extension.
	 *
	 * @param isa_ext ISA extension value
	 */
	public void setIsaExt(int isa_ext) {
		this.isa_ext = isa_ext;
	}

	/**
	 * Returns the Application Specific Extensions (ASEs) flags.
	 *
	 * @return ASEs flags
	 */
	public int getAses() {
		return ases;
	}

	/**
	 * Sets the Application Specific Extensions (ASEs) flags.
	 *
	 * @param ases ASEs flags value
	 */
	public void setAses(int ases) {
		this.ases = ases;
	}

	/**
	 * Returns the first set of processor-specific flags.
	 *
	 * @return flags1 value
	 */
	public int getFlags1() {
		return flags1;
	}

	/**
	 * Sets the first set of processor-specific flags.
	 *
	 * @param flags1 flags1 value
	 */
	public void setFlags1(int flags1) {
		this.flags1 = flags1;
	}

	/**
	 * Returns the second set of processor-specific flags.
	 *
	 * @return flags2 value
	 */
	public int getFlags2() {
		return flags2;
	}

	/**
	 * Sets the second set of processor-specific flags.
	 *
	 * @param flags2 flags2 value
	 */
	public void setFlags2(int flags2) {
		this.flags2 = flags2;
	}
}
