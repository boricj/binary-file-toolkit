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
 * MIPS register usage information section.
 * Contains masks indicating which registers are used and the global pointer value.
 */
public class ElfRegInfo_Mips extends ElfSection {
	private int ri_gprmask;
	private int[] ri_cprmask = new int[4];
	private int ri_gp_value;

	/**
	 * Creates a new MIPS register info section.
	 *
	 * @param elf the parent ELF file
	 * @param name section name
	 * @param flags section flags
	 * @param addralign address alignment
	 * @param bytes initial content (unused)
	 */
	public ElfRegInfo_Mips(ElfFile elf, String name, ElfSectionFlags flags, long addralign, byte[] bytes) {
		this(elf, name, flags, 0, 0, addralign, 0);
	}

	/**
	 * Creates a new MIPS register info section with entry size.
	 *
	 * @param elf the parent ELF file
	 * @param name section name
	 * @param flags section flags
	 * @param addralign address alignment
	 * @param entsize entry size
	 * @param bytes initial content (unused)
	 */
	public ElfRegInfo_Mips(
			ElfFile elf, String name, ElfSectionFlags flags, long addralign, long entsize, byte[] bytes) {
		this(elf, name, flags, 0, 0, addralign, entsize);
	}

	/**
	 * Creates a new MIPS register info section with full parameters.
	 *
	 * @param elf the parent ELF file
	 * @param name section name
	 * @param flags section flags
	 * @param addr virtual address
	 * @param offset file offset
	 * @param addralign address alignment
	 * @param entsize entry size
	 */
	public ElfRegInfo_Mips(
			ElfFile elf, String name, ElfSectionFlags flags, long addr, long offset, long addralign, long entsize) {
		super(elf, name, flags, addr, offset, addralign, entsize);
	}

	/**
	 * Parses a MIPS register info section from an input stream.
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
	public ElfRegInfo_Mips(
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

		setGprMask(dataInput.readInt());
		setCprMask(0, dataInput.readInt());
		setCprMask(1, dataInput.readInt());
		setCprMask(2, dataInput.readInt());
		setCprMask(3, dataInput.readInt());
		setGpValue(dataInput.readInt());
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		DataOutput dataOutput = getElfFile().wrap(outputStream);

		dataOutput.writeInt(ri_gprmask);
		dataOutput.writeInt(ri_cprmask[0]);
		dataOutput.writeInt(ri_cprmask[1]);
		dataOutput.writeInt(ri_cprmask[2]);
		dataOutput.writeInt(ri_cprmask[3]);
		dataOutput.writeInt(ri_gp_value);
	}

	@Override
	public long getLength() {
		return 24;
	}

	@Override
	public int getType() {
		return ElfSectionType.SHT_MIPS_REGINFO.getValue();
	}

	/**
	 * Returns the general purpose register usage mask.
	 *
	 * @return GPR mask
	 */
	public int getGprMask() {
		return ri_gprmask;
	}

	/**
	 * Sets the general purpose register usage mask.
	 *
	 * @param ri_gprmask GPR mask value
	 */
	public void setGprMask(int ri_gprmask) {
		this.ri_gprmask = ri_gprmask;
	}

	/**
	 * Returns a coprocessor register usage mask.
	 *
	 * @param index coprocessor index (0-3)
	 * @return coprocessor register mask
	 */
	public int getCprMask(int index) {
		return ri_cprmask[index];
	}

	/**
	 * Sets a coprocessor register usage mask.
	 *
	 * @param index coprocessor index (0-3)
	 * @param ri_gprmask coprocessor register mask value
	 */
	public void setCprMask(int index, int ri_gprmask) {
		this.ri_cprmask[index] = ri_gprmask;
	}

	/**
	 * Returns the global pointer value.
	 *
	 * @return global pointer value
	 */
	public int getGpValue() {
		return ri_gprmask;
	}

	/**
	 * Sets the global pointer value.
	 *
	 * @param ri_gp_value global pointer value
	 */
	public void setGpValue(int ri_gp_value) {
		this.ri_gp_value = ri_gp_value;
	}
}
