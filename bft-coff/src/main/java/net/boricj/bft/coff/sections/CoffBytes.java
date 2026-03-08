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
package net.boricj.bft.coff.sections;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import net.boricj.bft.coff.CoffFile;
import net.boricj.bft.coff.CoffRelocationTable;
import net.boricj.bft.coff.CoffSection;
import net.boricj.bft.coff.constants.CoffSectionFlags;

/**
 * A COFF section containing raw byte data.
 * Represents sections with initialized data such as code or data segments.
 */
public class CoffBytes extends CoffSection {
	private final byte[] bytes;

	/**
	 * Creates a bytes section with in-memory data.
	 *
	 * @param coff parent COFF file
	 * @param name section name
	 * @param characteristics section characteristics flags
	 * @param bytes section data bytes
	 */
	public CoffBytes(CoffFile coff, String name, CoffSectionFlags characteristics, byte[] bytes) {
		super(coff, name, 0, 0, 0, characteristics);
		Objects.requireNonNull(bytes);

		this.relocationTable = new CoffRelocationTable(coff, this);
		this.bytes = bytes;
	}

	/**
	 * Creates a bytes section by reading from a COFF parser.
	 *
	 * @param coff parent COFF file
	 * @param parser COFF file parser
	 * @param name section name
	 * @param physicalAddress section physical address
	 * @param virtualAddress section virtual address
	 * @param sizeOfRawData section size in bytes
	 * @param pointerToRawData file offset to section bytes
	 * @param pointerToRelocations file offset to relocations
	 * @param pointerToLineNumbers file offset to line numbers
	 * @param numberOfRelocations relocation count
	 * @param numberOfLinenumbers line number count
	 * @param characteristics section characteristics flags
	 * @throws IOException if an I/O error occurs while reading section data
	 */
	public CoffBytes(
			CoffFile coff,
			CoffFile.Parser parser,
			String name,
			int physicalAddress,
			int virtualAddress,
			int sizeOfRawData,
			int pointerToRawData,
			int pointerToRelocations,
			int pointerToLineNumbers,
			int numberOfRelocations,
			short numberOfLinenumbers,
			CoffSectionFlags characteristics)
			throws IOException {
		super(coff, name, physicalAddress, virtualAddress, pointerToRawData, characteristics);

		this.bytes = new byte[(int) sizeOfRawData];

		FileInputStream fis = parser.getFileInputStream();
		fis.getChannel().position(pointerToRawData);
		fis.readNBytes(bytes, 0, sizeOfRawData);

		this.relocationTable = new CoffRelocationTable(coff, parser, this, pointerToRelocations, numberOfRelocations);
	}

	/**
	 * Returns the raw byte data contained in this section.
	 *
	 * @return section data bytes
	 */
	public byte[] getBytes() {
		return bytes;
	}

	@Override
	public long getLength() {
		return bytes.length;
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		outputStream.write(bytes);
	}
}
