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
import java.io.DataOutput;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.boricj.bft.IndirectList;
import net.boricj.bft.Writable;
import net.boricj.bft.coff.CoffRelocationTable.CoffRel;
import net.boricj.bft.coff.CoffSymbolTable.CoffSymbol;
import net.boricj.bft.coff.constants.CoffMachine;
import net.boricj.bft.coff.constants.CoffRelocationType;

public class CoffRelocationTable implements IndirectList<CoffRel>, Writable {
	public static final int RECORD_SIZE = 10;
	public static final int EXTENDED_RELOCATIONS_COUNT = 0xFFFF;

	public class CoffRel {
		private final int virtualAddress;
		private final int symbolTableIndex;
		private final CoffRelocationType type;

		protected CoffRel(int virtualAddress, int symbolTableIndex, CoffRelocationType type) {
			Objects.requireNonNull(type);

			if (type.getMachine().getRelocationTypeClass() != type.getClass()) {
				throw new IllegalArgumentException("relocation type doesn't match COFF machine");
			}

			this.virtualAddress = virtualAddress;
			this.symbolTableIndex = symbolTableIndex;
			this.type = type;
		}

		protected void write(DataOutput dataOutput) throws IOException {
			dataOutput.writeInt(virtualAddress);
			dataOutput.writeInt(symbolTableIndex);
			dataOutput.writeShort(type.getValue());
		}

		public int getVirtualAddress() {
			return virtualAddress;
		}

		public int getSymbolTableIndex() {
			return symbolTableIndex;
		}

		public CoffRelocationType getType() {
			return type;
		}
	}

	private final CoffFile coff;
	private final CoffSection section;
	private final List<CoffRel> relocations = new ArrayList<>();
	private int pointerToRelocations;

	public CoffRelocationTable(CoffFile coff, CoffSection section) {
		this.coff = coff;
		this.section = section;
	}

	public CoffRelocationTable(
			CoffFile coff,
			CoffFile.Parser parser,
			CoffSection section,
			int pointerToRelocations,
			int numberOfRelocations)
			throws IOException {
		Objects.requireNonNull(coff);
		Objects.requireNonNull(section);

		this.coff = coff;
		this.section = section;
		this.pointerToRelocations = pointerToRelocations;

		CoffHeader header = coff.getHeader();
		CoffMachine machine = header.getMachine();
		Class<? extends CoffRelocationType> clazz = machine.getRelocationTypeClass();

		FileInputStream fis = parser.getFileInputStream();
		fis.getChannel().position(pointerToRelocations);
		DataInput dataInput = coff.wrap(fis);

		for (int idx = 0; idx < numberOfRelocations; idx++) {
			int virtualAddress = dataInput.readInt();
			int symbolTableIndex = dataInput.readInt();
			short typeValue = dataInput.readShort();

			if (idx == 0 && typeValue == 0) {
				numberOfRelocations = virtualAddress;
				continue;
			}

			CoffRelocationType type;

			try {
				type = (CoffRelocationType)
						clazz.getMethod("valueFrom", Short.TYPE).invoke(null, typeValue);
			} catch (IllegalAccessException
					| InvocationTargetException
					| NoSuchMethodException
					| SecurityException ex) {
				throw new RuntimeException(ex);
			}

			relocations.add(new CoffRel(virtualAddress, symbolTableIndex, type));
		}
	}

	public void add(int virtualAddress, CoffSymbol symbol, CoffRelocationType type) {
		CoffSymbolTable symbolTable = coff.getSymbols();
		int symbolTableIndex = symbolTable.indexOf(symbol);
		if (symbolTableIndex == -1) {
			throw new IllegalArgumentException("symbol not found inside symbol table");
		}

		relocations.add(new CoffRel(virtualAddress, symbolTableIndex, type));
	}

	public void add(int virtualAddress, int symbolTableIndex, CoffRelocationType type) {
		relocations.add(new CoffRel(virtualAddress, symbolTableIndex, type));
	}

	@Override
	public long getOffset() {
		return pointerToRelocations;
	}

	public void setOffset(long offset) {
		pointerToRelocations = (int) offset;
	}

	@Override
	public long getLength() {
		int numberOfRelocations = relocations.size();

		if (numberOfRelocations >= EXTENDED_RELOCATIONS_COUNT) {
			numberOfRelocations += 1;
		}

		return numberOfRelocations * RECORD_SIZE;
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		DataOutput dataOutput = coff.wrap(outputStream);

		int numberOfRelocations = relocations.size();
		if (relocations.size() >= EXTENDED_RELOCATIONS_COUNT) {
			dataOutput.writeInt(numberOfRelocations + 1);
			dataOutput.writeInt(0);
			dataOutput.writeShort(0);
		}

		for (CoffRel rel : relocations) {
			rel.write(dataOutput);
		}
	}

	@Override
	public List<CoffRel> getElements() {
		return Collections.unmodifiableList(relocations);
	}

	public CoffFile getCoffFile() {
		return coff;
	}

	public CoffSection getSection() {
		return section;
	}

	@Override
	public String toString() {
		String className = getClass().getSimpleName();
		String name = section.getName();

		if (name != null && !name.isEmpty()) {
			return String.format("%s [%s]", className, name);
		}

		return className;
	}
}
