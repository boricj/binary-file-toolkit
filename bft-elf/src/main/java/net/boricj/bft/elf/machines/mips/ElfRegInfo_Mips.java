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

public class ElfRegInfo_Mips extends ElfSection {
	private int ri_gprmask;
	private int[] ri_cprmask = new int[4];
	private int ri_gp_value;

	public ElfRegInfo_Mips(ElfFile elf, String name, ElfSectionFlags flags, long addralign, byte[] bytes) {
		this(elf, name, flags, 0, 0, addralign, 0);
	}

	public ElfRegInfo_Mips(
			ElfFile elf, String name, ElfSectionFlags flags, long addralign, long entsize, byte[] bytes) {
		this(elf, name, flags, 0, 0, addralign, entsize);
	}

	public ElfRegInfo_Mips(
			ElfFile elf, String name, ElfSectionFlags flags, long addr, long offset, long addralign, long entsize) {
		super(elf, name, flags, addr, offset, addralign, entsize);
	}

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

	public int getGprMask() {
		return ri_gprmask;
	}

	public void setGprMask(int ri_gprmask) {
		this.ri_gprmask = ri_gprmask;
	}

	public int getCprMask(int index) {
		return ri_cprmask[index];
	}

	public void setCprMask(int index, int ri_gprmask) {
		this.ri_cprmask[index] = ri_gprmask;
	}

	public int getGpValue() {
		return ri_gprmask;
	}

	public void setGpValue(int ri_gp_value) {
		this.ri_gp_value = ri_gp_value;
	}
}
