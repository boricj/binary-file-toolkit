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

public class CoffHeader implements Writable {
	private final CoffFile coff;

	private final CoffMachine machine;
	private int timeDateStamp;
	private short characteristics;

	protected CoffHeader(CoffFile coff, CoffFile.Builder builder) {
		this.coff = coff;
		this.machine = builder.getMachine();
		this.timeDateStamp = builder.getTimeDateStamp();
		this.characteristics = builder.getCharacteristics();
	}

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

	public CoffMachine getMachine() {
		return machine;
	}

	public int getTimeDateStamp() {
		return timeDateStamp;
	}

	public void setTimeDateStamp(int timeDateStamp) {
		this.timeDateStamp = timeDateStamp;
	}

	protected ByteOutputStream wrap(OutputStream outputStream) {
		return ByteOutputStream.asLittleEndian(outputStream);
	}

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
}
