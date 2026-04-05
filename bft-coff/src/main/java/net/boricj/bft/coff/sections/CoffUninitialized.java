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

import java.io.IOException;
import java.io.OutputStream;

import net.boricj.bft.coff.CoffFile;
import net.boricj.bft.coff.CoffRelocationTable;
import net.boricj.bft.coff.CoffSection;
import net.boricj.bft.coff.constants.CoffSectionFlags;

/**
 * A COFF section that reserves virtual space without storing raw bytes.
 */
public class CoffUninitialized extends CoffSection {
	/**
	 * Creates an uninitialized section.
	 *
	 * @param coff parent COFF file
	 * @param name section name
	 * @param characteristics section characteristics flags
	 * @param virtualSize section virtual size in bytes
	 */
	public CoffUninitialized(CoffFile coff, String name, CoffSectionFlags characteristics, int virtualSize) {
		super(coff, name, virtualSize, 0, 0, 0, characteristics);

		this.relocationTable = new CoffRelocationTable(coff, this);
	}

	/**
	 * Creates an uninitialized section by reading section metadata from a parser.
	 *
	 * @param coff parent COFF file
	 * @param parser COFF file parser
	 * @param name section name
	 * @param physicalAddress section physical address
	 * @param virtualAddress section virtual address
	 * @param sizeOfRawData section virtual size in bytes
	 * @param pointerToRawData file offset of section raw data
	 * @param pointerToRelocations file offset to relocations
	 * @param pointerToLineNumbers file offset to line numbers
	 * @param numberOfRelocations relocation count
	 * @param numberOfLinenumbers line number count
	 * @param characteristics section characteristics flags
	 * @throws IOException if an I/O error occurs while reading relocations
	 */
	public CoffUninitialized(
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
		super(coff, name, sizeOfRawData, physicalAddress, virtualAddress, pointerToRawData, characteristics);

		this.relocationTable = new CoffRelocationTable(coff, parser, this, pointerToRelocations, numberOfRelocations);
	}

	@Override
	public long getLength() {
		return 0;
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {}
}
