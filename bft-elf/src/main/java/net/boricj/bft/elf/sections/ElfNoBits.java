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
package net.boricj.bft.elf.sections;

import java.io.IOException;
import java.io.OutputStream;

import net.boricj.bft.elf.ElfFile;
import net.boricj.bft.elf.ElfSection;
import net.boricj.bft.elf.ElfSectionFlags;
import net.boricj.bft.elf.constants.ElfSectionType;

public class ElfNoBits extends ElfSection {
	private final long size;

	public ElfNoBits(ElfFile elf, String name, ElfSectionFlags flags, long addralign, long size) {
		this(elf, name, flags, 0, 0, addralign, 0, size);
	}

	public ElfNoBits(
			ElfFile elf,
			String name,
			ElfSectionFlags flags,
			long address,
			long offset,
			long addralign,
			long entsize,
			long size) {
		super(elf, name, flags, address, offset, addralign, entsize);

		this.size = size;
	}

	public ElfNoBits(
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

		this.size = size;
	}

	@Override
	public int getType() {
		return ElfSectionType.SHT_NOBITS.getValue();
	}

	@Override
	public long getLength() {
		return 0;
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		// No operation.
	}
}
