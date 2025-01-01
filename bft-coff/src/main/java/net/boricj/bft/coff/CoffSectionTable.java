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
package net.boricj.bft.coff;

import java.io.DataInput;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Stream;

import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.IndirectList;
import net.boricj.bft.Writable;
import net.boricj.bft.coff.constants.CoffSectionFlags;
import net.boricj.bft.coff.sections.CoffBytes;

import static net.boricj.bft.coff.CoffRelocationTable.EXTENDED_RELOCATIONS_COUNT;

public class CoffSectionTable implements IndirectList<CoffSection>, Writable {
	public static final int RECORD_LENGTH = 40;

	private final CoffFile coff;
	private final List<CoffSection> table = new ArrayList<>();
	private final Map<CoffSection, Integer> reverseLookup = new IdentityHashMap<>();

	protected CoffSectionTable(CoffFile coff, CoffFile.Builder builder) {
		this.coff = coff;
	}

	protected CoffSectionTable(CoffFile coff, CoffFile.Parser parser) {
		this.coff = coff;
		this.table.addAll(Stream.generate(() -> (CoffSection) null)
				.limit(parser.numberOfSections)
				.toList());
	}

	@Override
	public long getOffset() {
		CoffHeader header = coff.getHeader();
		return header.getOffset() + header.getLength();
	}

	@Override
	public long getLength() {
		return size() * RECORD_LENGTH;
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		CoffStringTable stringTable = coff.getStrings();
		ByteOutputStream dataOutput = coff.wrap(outputStream);

		for (CoffSection section : table) {
			CoffRelocationTable relocationTable = section.getRelocations();
			int numberOfRelocations = relocationTable.size();
			int characteristics = section.getCharacteristics().getValue();
			if (numberOfRelocations >= EXTENDED_RELOCATIONS_COUNT) {
				numberOfRelocations = EXTENDED_RELOCATIONS_COUNT;
				characteristics |= CoffSectionFlags.IMAGE_SCN_LNK_NRELOC_OVFL;
			}

			dataOutput.write(stringTable.encodeSecName(section.getName()));
			dataOutput.writeInt(section.getPhysicalAddress());
			dataOutput.writeInt(section.getVirtualAddress());
			dataOutput.writeInt((int) section.getLength());
			dataOutput.writeInt((int) section.getOffset());
			dataOutput.writeInt((int) relocationTable.getOffset());
			dataOutput.writeInt(0); // PointerToLinenumbers
			dataOutput.writeShort(numberOfRelocations);
			dataOutput.writeShort(0); // NumberOfLinenumbers
			dataOutput.writeInt(characteristics);
		}
	}

	public CoffSection get(int index, CoffFile.Parser parser) throws IOException {
		index -= 1;
		CoffSection section = table.get(index);

		if (section == null) {
			FileInputStream fis = parser.getFileInputStream();

			fis.getChannel().position(getOffset() + index * RECORD_LENGTH);
			DataInput dataInput = coff.wrap(fis);

			byte[] nameBytes = new byte[8];
			dataInput.readFully(nameBytes);
			String name = coff.getStrings().decodeSecName(nameBytes);
			int physicalAddress = dataInput.readInt();
			int virtualAddress = dataInput.readInt();
			int sizeOfRawData = dataInput.readInt();
			int pointerToRawData = dataInput.readInt();
			int pointerToRelocations = dataInput.readInt();
			int pointerToLineNumbers = dataInput.readInt();
			int numberOfRelocations = dataInput.readUnsignedShort();
			short numberOfLinenumbers = dataInput.readShort();
			CoffSectionFlags characteristics = new CoffSectionFlags(dataInput.readInt());

			section = new CoffBytes(
					coff,
					parser,
					name,
					physicalAddress,
					virtualAddress,
					sizeOfRawData,
					pointerToRawData,
					pointerToRelocations,
					pointerToLineNumbers,
					numberOfRelocations,
					numberOfLinenumbers,
					characteristics);
			table.set(index, section);
		}

		return section;
	}

	@Override
	public boolean add(CoffSection section) {
		Objects.requireNonNull(section);

		if (section.getCoffFile() != coff) {
			throw new NoSuchElementException("section doesn't belong to this COFF file");
		}
		if (contains(section)) {
			throw new IllegalStateException("section is already present inside section table");
		}

		reverseLookup.put(section, size() + 1);
		return table.add(section);
	}

	@Override
	public CoffSection get(int index) {
		return table.get(index - 1);
	}

	@Override
	public boolean contains(Object object) {
		return reverseLookup.containsKey(object);
	}

	@Override
	public int indexOf(Object object) {
		return reverseLookup.getOrDefault(object, -1);
	}

	@Override
	public List<CoffSection> getElements() {
		return Collections.unmodifiableList(table);
	}
	}
}
