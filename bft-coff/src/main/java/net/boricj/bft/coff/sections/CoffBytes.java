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

import net.boricj.bft.coff.CoffFile;
import net.boricj.bft.coff.CoffRelocationTable;
import net.boricj.bft.coff.CoffSection;
import net.boricj.bft.coff.constants.CoffSectionFlags;

public class CoffBytes extends CoffSection {
	private final byte[] bytes;

	public CoffBytes(CoffFile coff, String name, CoffSectionFlags characteristics, byte[] bytes) {
		super(coff, name, 0, 0, 0, characteristics);

		this.relocationTable = new CoffRelocationTable(coff, this);
		this.bytes = bytes;
	}

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
