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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.IndirectList;
import net.boricj.bft.Writable;
import net.boricj.bft.coff.CoffSymbolTable.CoffSymbol;
import net.boricj.bft.coff.constants.CoffStorageClass;

import static net.boricj.bft.Utils.decodeNullTerminatedString;
import static net.boricj.bft.Utils.roundUp;

public class CoffSymbolTable implements IndirectList<CoffSymbol>, Writable {
	public static final int RECORD_LENGTH = 18;

	public class CoffSymbol {
		public static final short IMAGE_SYM_UNDEFINED = 0;
		public static final short IMAGE_SYM_ABSOLUTE = -1;
		public static final short IMAGE_SYM_DEBUG = -2;

		private final String name;
		private final int value;
		private final short sectionNumber;
		private final short type;
		private final CoffStorageClass storageClass;

		public CoffSymbol(String name, int value, short sectionNumber, short type, CoffStorageClass storageClass) {
			this.name = name;
			this.value = value;
			this.sectionNumber = sectionNumber;
			this.type = type;
			this.storageClass = storageClass;
		}

		public CoffSymbol(
				String name,
				int value,
				short sectionNumber,
				short type,
				CoffStorageClass storageClass,
				byte numberOfAuxSymbols,
				DataInput dataInput)
				throws IOException {
			this(name, value, sectionNumber, type, storageClass);

			if (numberOfAuxSymbols != 0) {
				throw new RuntimeException("unhandled auxiliary symbol records");
			}
		}

		public String getName() {
			return name;
		}

		public int getValue() {
			return value;
		}

		public short getSectionNumber() {
			return sectionNumber;
		}

		public short getType() {
			return type;
		}

		public CoffStorageClass getStorageClass() {
			return storageClass;
		}

		public byte getNumberOfAuxSymbols() {
			return 0;
		}

		protected void writeAuxiliarySymbolRecord(DataOutput dataOutput) throws IOException {}
	}

	public class CoffSymbolFile extends CoffSymbol {
		private static final Charset CHARSET = StandardCharsets.UTF_8;

		private final String fileName;

		protected CoffSymbolFile(String name, String fileName) {
			super(name, 0, IMAGE_SYM_DEBUG, (short) 0, CoffStorageClass.IMAGE_SYM_CLASS_FILE);

			this.fileName = fileName;
		}

		public CoffSymbolFile(
				String name,
				int value,
				short sectionNumber,
				short type,
				CoffStorageClass storageClass,
				byte numberOfAuxSymbols,
				DataInput dataInput)
				throws IOException {
			super(name, value, sectionNumber, type, storageClass);

			byte[] aux = new byte[numberOfAuxSymbols * RECORD_LENGTH];
			dataInput.readFully(aux);

			this.fileName = decodeNullTerminatedString(aux, CHARSET);
		}

		@Override
		public CoffStorageClass getStorageClass() {
			return CoffStorageClass.IMAGE_SYM_CLASS_FILE;
		}

		public String getFileName() {
			return fileName;
		}

		@Override
		public byte getNumberOfAuxSymbols() {
			return (byte) (roundUp(fileName.getBytes(CHARSET).length, RECORD_LENGTH) / RECORD_LENGTH);
		}

		@Override
		protected void writeAuxiliarySymbolRecord(DataOutput dataOutput) throws IOException {
			byte[] aux = fileName.getBytes(CHARSET);
			if (aux.length % RECORD_LENGTH != 0) {
				aux = Arrays.copyOf(aux, roundUp(aux.length, RECORD_LENGTH));
			}

			dataOutput.write(aux);
		}
	}

	public class CoffSymbolSection extends CoffSymbol {
		private final CoffSection section;

		public CoffSymbolSection(CoffSection section) {
			super(
					section.getName(),
					0,
					(short) coff.getSections().indexOf(section),
					(short) 0,
					CoffStorageClass.IMAGE_SYM_CLASS_STATIC);

			this.section = section;
		}

		public CoffSymbolSection(
				String name,
				int value,
				short sectionNumber,
				short type,
				CoffStorageClass storageClass,
				byte numberOfAuxSymbols,
				DataInput dataInput)
				throws IOException {
			super(name, value, sectionNumber, type, storageClass);

			this.section = coff.getSections().get(sectionNumber);

			byte[] aux = new byte[RECORD_LENGTH];
			dataInput.readFully(aux);
		}

		@Override
		public byte getNumberOfAuxSymbols() {
			return 1;
		}

		@Override
		protected void writeAuxiliarySymbolRecord(DataOutput dataOutput) throws IOException {
			CoffRelocationTable relocationTable = section.getRelocations();
			int numberOfRelocations = relocationTable.size();
			if (numberOfRelocations > 0xFFFF) {
				numberOfRelocations = 0xFFFF;
			}

			dataOutput.writeInt((int) section.getLength());
			dataOutput.writeShort(numberOfRelocations);
			dataOutput.writeShort(0); // NumberOfLinenumbers
			dataOutput.writeInt(0); // CheckSum
			dataOutput.writeShort(0); // Number
			dataOutput.writeByte(0); // Selection
			dataOutput.write(new byte[3]); // Unused
		}
	}

	public class CoffSymbolFunction extends CoffSymbol {
		public CoffSymbolFunction(
				String name,
				int value,
				short sectionNumber,
				short type,
				CoffStorageClass storageClass,
				byte numberOfAuxSymbols,
				DataInput dataInput)
				throws IOException {
			super(name, value, sectionNumber, type, storageClass);

			byte[] aux = new byte[RECORD_LENGTH];
			dataInput.readFully(aux);
		}

		@Override
		public byte getNumberOfAuxSymbols() {
			return 1;
		}

		@Override
		protected void writeAuxiliarySymbolRecord(DataOutput dataOutput) throws IOException {
			dataOutput.write(new byte[RECORD_LENGTH]);
		}
	}

	private final CoffFile coff;
	private final List<CoffSymbol> symbols = new ArrayList<>();
	private final List<CoffSymbol> lookup = new ArrayList<>();
	private final Map<CoffSymbol, Integer> reverseLookup = new IdentityHashMap<>();
	private int pointerToSymbolTable;

	protected CoffSymbolTable(CoffFile coff, CoffFile.Builder builder) {
		this.coff = coff;
	}

	protected CoffSymbolTable(CoffFile coff, CoffFile.Parser parser) throws IOException {
		this.coff = coff;
		this.pointerToSymbolTable = parser.pointerToSymbolTable;

		CoffStringTable stringTable = coff.getStrings();
		FileInputStream fis = parser.getFileInputStream();
		fis.getChannel().position(pointerToSymbolTable);
		DataInput dataInput = coff.wrap(fis);

		for (int idx = 0; idx < parser.numberOfSymbols; idx++) {
			byte[] nameBytes = new byte[8];
			dataInput.readFully(nameBytes);
			String name = stringTable.decodeSymName(nameBytes);
			int value = dataInput.readInt();
			short sectionNumber = dataInput.readShort();
			short type = dataInput.readShort();
			CoffStorageClass storageClass = CoffStorageClass.valueFrom(dataInput.readByte());
			byte numberOfAuxSymbols = dataInput.readByte();

			Class<? extends CoffSymbol> symbolClass;
			switch (storageClass) {
				case IMAGE_SYM_CLASS_FILE:
					symbolClass = CoffSymbolFile.class;
					break;

				case IMAGE_SYM_CLASS_EXTERNAL:
					if (numberOfAuxSymbols == 1 && type == 0x20) {
						symbolClass = CoffSymbolFunction.class;
					} else {
						symbolClass = CoffSymbol.class;
					}
					break;

				case IMAGE_SYM_CLASS_STATIC:
					if (numberOfAuxSymbols == 1) {
						symbolClass = CoffSymbolSection.class;
					} else {
						symbolClass = CoffSymbol.class;
					}
					break;

				default:
					symbolClass = CoffSymbol.class;
					break;
			}

			Constructor<? extends CoffSymbol> constructor;
			CoffSymbol symbol;
			try {
				constructor = symbolClass.getConstructor(
						CoffSymbolTable.class,
						String.class,
						Integer.TYPE,
						Short.TYPE,
						Short.TYPE,
						CoffStorageClass.class,
						Byte.TYPE,
						DataInput.class);
				symbol = constructor.newInstance(
						this, name, value, sectionNumber, type, storageClass, numberOfAuxSymbols, dataInput);
			} catch (NoSuchMethodException
					| SecurityException
					| InstantiationException
					| IllegalAccessException
					| IllegalArgumentException
					| InvocationTargetException ex) {
				throw new RuntimeException(ex);
			}

			idx = add(symbol, this);
			idx += symbol.getNumberOfAuxSymbols();
		}
	}

	@Override
	public long getOffset() {
		return pointerToSymbolTable;
	}

	public void setOffset(long offset) {
		pointerToSymbolTable = (int) offset;
	}

	@Override
	public long getLength() {
		if (symbols.isEmpty()) {
			return 0;
		}

		CoffSymbol lastSymbol = symbols.get(symbols.size() - 1);
		int lastIndex = reverseLookup.get(lastSymbol);
		return (lastIndex + lastSymbol.getNumberOfAuxSymbols() + 1) * RECORD_LENGTH;
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		ByteOutputStream dataOutput = coff.wrap(outputStream);
		CoffStringTable stringTable = coff.getStrings();

		for (CoffSymbol symbol : symbols) {
			dataOutput.write(stringTable.encodeSymName(symbol.getName()));
			dataOutput.writeInt(symbol.getValue());
			dataOutput.writeShort(symbol.getSectionNumber());
			dataOutput.writeShort(symbol.getType());
			dataOutput.writeByte(symbol.getStorageClass().getValue());
			dataOutput.writeByte(symbol.getNumberOfAuxSymbols());
			symbol.writeAuxiliarySymbolRecord(dataOutput);
		}
	}

	@Override
	public List<CoffSymbol> getElements() {
		return Collections.unmodifiableList(symbols);
	}

	public CoffSymbol addSymbol(
			String name, int offset, CoffSection section, byte type, CoffStorageClass storageClass) {
		CoffSectionTable sectionTable = coff.getSections();

		CoffSymbol symbol = new CoffSymbol(name, offset, (short) sectionTable.indexOf(section), type, storageClass);
		add(symbol, this);
		return symbol;
	}

	public CoffSymbol addUndefined(String name) {
		CoffSymbol symbol = new CoffSymbol(
				name, 0, CoffSymbol.IMAGE_SYM_UNDEFINED, (byte) 0x20, CoffStorageClass.IMAGE_SYM_CLASS_EXTERNAL);
		add(symbol, this);
		return symbol;
	}

	public CoffSymbol addAbsolute(String name, int value) {
		CoffSymbol symbol = new CoffSymbol(
				name, value, CoffSymbol.IMAGE_SYM_ABSOLUTE, (byte) 0x00, CoffStorageClass.IMAGE_SYM_CLASS_STATIC);
		add(symbol, this);
		return symbol;
	}

	public CoffSymbol addFile(String name, String fileName) {
		CoffSymbol symbol = new CoffSymbolFile(name, fileName);
		add(symbol, this);
		return symbol;
	}

	public CoffSymbol addSection(CoffSection section) {
		CoffSymbol symbol = new CoffSymbolSection(section);
		add(symbol, this);
		return symbol;
	}

	private int add(CoffSymbol symbol, CoffSymbolTable dummy) {
		int idx = lookup.size();

		reverseLookup.put(symbol, idx);
		symbols.add(symbol);
		lookup.add(symbol);
		for (int i = 0; i < symbol.getNumberOfAuxSymbols(); i++) {
			lookup.add(null);
		}

		return idx;
	}

	@Override
	public boolean contains(Object object) {
		return reverseLookup.containsKey(object);
	}

	@Override
	public CoffSymbol get(int index) {
		return lookup.get(index);
	}

	@Override
	public int indexOf(Object object) {
		return reverseLookup.getOrDefault(object, -1);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
