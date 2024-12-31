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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.coff.constants.CoffMachine;

public class CoffFile {
	private final CoffHeader header;
	private final CoffSectionTable sections;
	private final CoffSymbolTable symbols;
	private final CoffStringTable strings;

	public static class Builder {
		private final CoffMachine machine;
		private int timeDateStamp;
		private short characteristics;
		private Charset stringTableCharset = CoffStringTable.DEFAULT_CHARSET;

		public Builder(CoffMachine machine) {
			Objects.requireNonNull(machine);

			this.machine = machine;
		}

		public CoffMachine getMachine() {
			return machine;
		}

		public int getTimeDateStamp() {
			return timeDateStamp;
		}

		public Builder setTimeDateStamp(int timeDateStamp) {
			this.timeDateStamp = timeDateStamp;

			return this;
		}

		public short getCharacteristics() {
			return characteristics;
		}

		public Builder setCharacteristics(short characteristics) {
			this.characteristics = characteristics;

			return this;
		}

		public Charset getStringTableCharset() {
			return stringTableCharset;
		}

		public void setStringTableCharset(Charset stringTableCharset) {
			Objects.requireNonNull(stringTableCharset);

			this.stringTableCharset = stringTableCharset;
		}

		public CoffFile build() {
			return new CoffFile(this);
		}
	}

	public static class Parser {
		private final FileInputStream fis;
		public Charset charset = StandardCharsets.UTF_8;

		public short numberOfSections;
		public int pointerToSymbolTable;
		public int numberOfSymbols;

		public Parser(FileInputStream fis) {
			Objects.requireNonNull(fis);

			this.fis = fis;
		}

		public FileInputStream getFileInputStream() {
			return fis;
		}

		public CoffFile parse() throws IOException {
			return new CoffFile(this);
		}
	}

	protected CoffFile(Builder builder) {
		this.header = new CoffHeader(this, builder);
		this.sections = new CoffSectionTable(this, builder);
		this.strings = new CoffStringTable(this, builder);
		this.symbols = new CoffSymbolTable(this, builder);
	}

	protected CoffFile(Parser parser) throws IOException {
		this.header = new CoffHeader(this, parser);
		this.sections = new CoffSectionTable(this, parser);
		this.strings = new CoffStringTable(this, parser);

		// Ensure all sections are instanciated.
		for (short idx = 1; idx <= parser.numberOfSections; idx++) {
			this.sections.get(idx, parser);
		}

		this.symbols = new CoffSymbolTable(this, parser);
	}

	public CoffHeader getHeader() {
		return header;
	}

	public CoffSectionTable getSections() {
		return sections;
	}

	public CoffSymbolTable getSymbols() {
		return symbols;
	}

	public CoffStringTable getStrings() {
		return strings;
	}

	public ByteOutputStream wrap(OutputStream outputStream) {
		return header.wrap(outputStream);
	}

	public ByteInputStream wrap(InputStream inputStream) {
		return header.wrap(inputStream);
	}
}
