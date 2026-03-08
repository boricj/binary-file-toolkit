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

/**
 * ELF null section (SHT_NULL).
 * Represents an inactive section with no content, used as the first entry in the section table.
 */
public class ElfNullSection extends ElfSection {
	/**
	 * Creates a null section with default parameters.
	 *
	 * @param elf the parent ELF file
	 */
	public ElfNullSection(ElfFile elf) {
		this(elf, "", new ElfSectionFlags(), 0, 0);
	}

	/**
	 * Creates a null section with specified parameters.
	 *
	 * @param elf the parent ELF file
	 * @param name section name
	 * @param flags section flags
	 * @param addralign address alignment
	 * @param entsize entry size
	 */
	public ElfNullSection(ElfFile elf, String name, ElfSectionFlags flags, long addralign, long entsize) {
		super(elf, name, flags, 0, 0, addralign, 0);
	}

	/**
	 * Parses a null section from an input stream.
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
	 */
	public ElfNullSection(
			ElfFile elf,
			ElfFile.Parser parser,
			ElfSectionFlags flags,
			long addr,
			long offset,
			long size,
			int link,
			int info,
			long addralign,
			long entsize) {
		super(elf, "", flags, addr, offset, addralign, entsize);
	}

	@Override
	public int getType() {
		return ElfSectionType.SHT_NULL.getValue();
	}

	@Override
	public long getLength() {
		return 0;
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		// No operation.
	}
}
