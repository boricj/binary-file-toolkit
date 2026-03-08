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
import java.io.InputStream;
import java.io.OutputStream;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.Writable;
import net.boricj.bft.coff.constants.CoffMachine;

/**
 * COFF file header containing machine type, timestamp, and characteristics.
 * The header appears at the start of a COFF object file.
 */
public class CoffHeader implements Writable {
	private final CoffFile coff;

	private final CoffMachine machine;
	private int timeDateStamp;
	private short characteristics;

	/**
	 * Constructs a COFF header from a builder.
	 *
	 * @param coff the parent COFF file
	 * @param builder the builder containing header configuration
	 */
	protected CoffHeader(CoffFile coff, CoffFile.Builder builder) {
		this.coff = coff;
		this.machine = builder.getMachine();
		this.timeDateStamp = builder.getTimeDateStamp();
		this.characteristics = builder.getCharacteristics();
	}

	/**
	 * Constructs a COFF header by parsing from a parser.
	 *
	 * @param coff the parent COFF file
	 * @param parser the parser containing the input stream
	 * @throws IOException if an I/O error occurs during parsing
	 */
	protected CoffHeader(CoffFile coff, CoffFile.Parser parser) throws IOException {
		this.coff = coff;

		FileInputStream fis = parser.getFileInputStream();
		DataInput dataInput = wrap(fis);

		this.machine = CoffMachine.valueFrom(dataInput.readShort());
		parser.numberOfSections = dataInput.readShort();
		this.timeDateStamp = dataInput.readInt();
		parser.pointerToSymbolTable = dataInput.readInt();
		parser.numberOfSymbols = dataInput.readInt();
		dataInput.readShort(); // SizeOfOptionalHeader
		this.characteristics = dataInput.readShort();
	}

	/**
	 * Returns the machine type for this COFF file.
	 *
	 * @return the machine type
	 */
	public CoffMachine getMachine() {
		return machine;
	}

	/**
	 * Returns the timestamp value stored in the header.
	 *
	 * @return the timestamp value
	 */
	public int getTimeDateStamp() {
		return timeDateStamp;
	}

	/**
	 * Sets the timestamp value in the header.
	 *
	 * @param timeDateStamp the new timestamp value
	 */
	public void setTimeDateStamp(int timeDateStamp) {
		this.timeDateStamp = timeDateStamp;
	}

	/**
	 * Wraps an output stream with little-endian byte order.
	 *
	 * @param outputStream the output stream to wrap
	 * @return wrapped output stream
	 */
	protected ByteOutputStream wrap(OutputStream outputStream) {
		return ByteOutputStream.asLittleEndian(outputStream);
	}

	/**
	 * Wraps an input stream with little-endian byte order.
	 *
	 * @param inputStream the input stream to wrap
	 * @return wrapped input stream
	 */
	protected ByteInputStream wrap(InputStream inputStream) {
		return ByteInputStream.asLittleEndian(inputStream);
	}

	@Override
	public long getOffset() {
		return 0;
	}

	@Override
	public long getLength() {
		return 20;
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		CoffSectionTable sectionTable = coff.getSections();
		CoffSymbolTable symbolTable = coff.getSymbols();

		DataOutput dataOutput = coff.wrap(outputStream);
		dataOutput.writeShort(machine.getValue());
		dataOutput.writeShort(sectionTable.size());
		dataOutput.writeInt(timeDateStamp);
		dataOutput.writeInt((int) symbolTable.getOffset());
		dataOutput.writeInt((int) (symbolTable.getLength() / CoffSymbolTable.RECORD_LENGTH));
		dataOutput.writeShort(0); // SizeOfOptionalHeader
		dataOutput.writeShort(characteristics);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
