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
import java.util.Objects;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.coff.constants.CoffMachine;

/**
 * Represents a COFF (Common Object File Format) file.
 *
 * <p>This class provides functionality to parse existing COFF files and build new ones.
 * COFF files contain a header, section table, symbol table, and string table.
 */
public class CoffFile {
	private final CoffHeader header;
	private final CoffSectionTable sections;
	private final CoffSymbolTable symbols;
	private final CoffStringTable strings;

	/**
	 * Builder for constructing COFF files programmatically.
	 */
	public static class Builder {
		private final CoffMachine machine;
		private int timeDateStamp;
		private short characteristics;
		private Charset charset = CoffStringTable.DEFAULT_CHARSET;

		/**
		 * Creates a new COFF file builder for the specified machine architecture.
		 *
		 * @param machine the target machine architecture
		 */
		public Builder(CoffMachine machine) {
			Objects.requireNonNull(machine);

			this.machine = machine;
		}

		/**
		 * Returns the COFF machine target used when building the file.
		 *
		 * @return machine target
		 */
		public CoffMachine getMachine() {
			return machine;
		}

		/**
		 * Returns the COFF timestamp value stored in the header.
		 *
		 * @return timestamp value
		 */
		public int getTimeDateStamp() {
			return timeDateStamp;
		}

		/**
		 * Sets the COFF timestamp value stored in the header.
		 *
		 * @param timeDateStamp timestamp value to write in the header
		 * @return this builder
		 */
		public Builder setTimeDateStamp(int timeDateStamp) {
			this.timeDateStamp = timeDateStamp;

			return this;
		}

		/**
		 * Returns the COFF header characteristics flags.
		 *
		 * @return characteristics flags
		 */
		public short getCharacteristics() {
			return characteristics;
		}

		/**
		 * Sets COFF header characteristics flags.
		 *
		 * @param characteristics flags value
		 * @return this builder
		 */
		public Builder setCharacteristics(short characteristics) {
			this.characteristics = characteristics;

			return this;
		}

		/**
		 * Returns the charset used by the COFF string table.
		 *
		 * @return charset used by the string table
		 */
		public Charset getCharset() {
			return charset;
		}

		/**
		 * Sets the charset used by the COFF string table.
		 *
		 * @param charset charset used for encoding and decoding strings
		 * @return this builder
		 */
		public Builder setCharset(Charset charset) {
			Objects.requireNonNull(charset);

			this.charset = charset;
			return this;
		}

		/**
		 * Builds a COFF file from this builder configuration.
		 *
		 * @return new COFF file
		 */
		public CoffFile build() {
			return new CoffFile(this);
		}
	}

	/**
	 * Parser for reading COFF files from an input stream.
	 */
	public static class Parser {
		private final FileInputStream fis;
		private Charset charset = CoffStringTable.DEFAULT_CHARSET;

		short numberOfSections;
		int pointerToSymbolTable;
		int numberOfSymbols;

		/**
		 * Creates a new COFF file parser for the specified input stream.
		 *
		 * @param fis the file input stream to parse
		 */
		public Parser(FileInputStream fis) {
			Objects.requireNonNull(fis);

			this.fis = fis;
		}

		/**
		 * Returns the input stream used for parsing.
		 *
		 * @return parser input stream
		 */
		public FileInputStream getFileInputStream() {
			return fis;
		}

		/**
		 * Returns the charset used for decoding strings in the COFF string table.
		 *
		 * @return parser charset
		 */
		public Charset getCharset() {
			return charset;
		}

		/**
		 * Sets the charset used for decoding strings in the COFF string table.
		 *
		 * @param charset the charset to use
		 * @return this parser
		 */
		public Parser setCharset(Charset charset) {
			Objects.requireNonNull(charset);

			this.charset = charset;
			return this;
		}

		/**
		 * Parses and returns a COFF file from the configured input stream.
		 *
		 * @return parsed COFF file
		 * @throws IOException if reading from the input stream fails
		 */
		public CoffFile parse() throws IOException {
			return new CoffFile(this);
		}
	}

	/**
	 * Constructs a COFF file from a builder.
	 *
	 * @param builder the builder containing the COFF file configuration
	 */
	protected CoffFile(Builder builder) {
		this.header = new CoffHeader(this, builder);
		this.sections = new CoffSectionTable(this, builder);
		this.strings = new CoffStringTable(this, builder);
		this.symbols = new CoffSymbolTable(this, builder);
	}

	/**
	 * Constructs a COFF file by parsing from a parser.
	 *
	 * @param parser the parser containing the input stream
	 * @throws IOException if an I/O error occurs during parsing
	 */
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

	/**
	 * Returns the COFF header.
	 *
	 * @return COFF header
	 */
	public CoffHeader getHeader() {
		return header;
	}

	/**
	 * Returns the COFF section table.
	 *
	 * @return COFF section table
	 */
	public CoffSectionTable getSections() {
		return sections;
	}

	/**
	 * Returns the COFF symbol table.
	 *
	 * @return COFF symbol table
	 */
	public CoffSymbolTable getSymbols() {
		return symbols;
	}

	/**
	 * Returns the COFF string table.
	 *
	 * @return COFF string table
	 */
	public CoffStringTable getStrings() {
		return strings;
	}

	/**
	 * Wraps an output stream using the byte order configured by this COFF file.
	 *
	 * @param outputStream output stream to wrap
	 * @return wrapped output stream
	 */
	public ByteOutputStream wrap(OutputStream outputStream) {
		return header.wrap(outputStream);
	}

	/**
	 * Wraps an input stream using the byte order configured by this COFF file.
	 *
	 * @param inputStream input stream to wrap
	 * @return wrapped input stream
	 */
	public ByteInputStream wrap(InputStream inputStream) {
		return header.wrap(inputStream);
	}
}
