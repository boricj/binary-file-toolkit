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
package net.boricj.bft.codeview;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.IndirectList;
import net.boricj.bft.codeview.constants.CodeViewSectionNames;
import net.boricj.bft.codeview.constants.CodeViewSignature;
import net.boricj.bft.codeview.tables.CodeViewSymbolTable;
import net.boricj.bft.codeview.tables.CodeViewTypeTable;

/**
 * In-memory representation of parsed CodeView sections.
 */
public class CodeViewFile implements IndirectList<CodeViewTable> {

	private static class CodeViewUnknownTable extends CodeViewTable {
		private final byte[] data;

		public CodeViewUnknownTable(CodeViewFile codeView, String name, ByteInputStream bis) throws IOException {
			super(codeView, name, CodeViewSignature.valueFrom(bis.readInt()));

			// TODO: implement parsing of type records.
			this.data = bis.readAllBytes();
		}

		@Override
		public void write(OutputStream outputStream) throws IOException {
			ByteOutputStream baos = ByteOutputStream.asLittleEndian(outputStream);

			baos.writeInt(getSignature().getValue());
			baos.write(data);
		}
	}

	private final List<CodeViewTable> table = new ArrayList<>();

	/**
	 * Builder/parser for constructing {@link CodeViewFile} instances from section blobs.
	 */
	public static class Parser {
		private final Map<String, byte[]> sectionsData = new HashMap<>();
		private boolean allowUnknownSections = false;
		private boolean allowUnknownSymbols = false;

		/**
		 * Creates an empty parser configuration.
		 */
		public Parser() {}

		/**
		 * Returns a mutable section map keyed by section name.
		 *
		 * @return mutable map of section payloads to parse
		 */
		public Map<String, byte[]> getSectionsData() {
			return sectionsData;
		}

		/**
		 * Adds or replaces a section payload.
		 *
		 * @param name CodeView section name
		 * @param data section bytes including the section signature field
		 * @return this parser for fluent configuration
		 */
		public Parser addSection(String name, byte[] data) {
			sectionsData.put(name, data);
			return this;
		}

		/**
		 * Configures whether unknown section names are accepted.
		 *
		 * @param allow {@code true} to preserve unknown sections, {@code false} to reject them
		 * @return this parser for fluent configuration
		 */
		public Parser allowUnknownSections(boolean allow) {
			this.allowUnknownSections = allow;
			return this;
		}

		/**
		 * Configures whether unknown symbols inside known sections are accepted.
		 *
		 * @param allow {@code true} to preserve unknown symbols, {@code false} to reject them
		 * @return this parser for fluent configuration
		 */
		public Parser allowUnknownSymbols(boolean allow) {
			this.allowUnknownSymbols = allow;
			return this;
		}

		/**
		 * Parses the configured sections into a {@link CodeViewFile}.
		 *
		 * @return parsed CodeView file
		 * @throws IOException if parsing fails
		 */
		public CodeViewFile parse() throws IOException {
			return new CodeViewFile(this);
		}

		/**
		 * Returns whether unknown section names are accepted.
		 *
		 * @return {@code true} when unknown sections are allowed
		 */
		public boolean areUnknownSectionsAllowed() {
			return allowUnknownSections;
		}

		/**
		 * Returns whether unknown symbols inside known sections are accepted.
		 *
		 * @return {@code true} when unknown symbols are allowed
		 */
		public boolean areUnknownSymbolsAllowed() {
			return allowUnknownSymbols;
		}
	}

	/**
	 * Creates an empty CodeView file.
	 */
	public CodeViewFile() {}

	private CodeViewFile(Parser parser) throws IOException {
		for (Map.Entry<String, byte[]> entry : parser.getSectionsData().entrySet()) {
			String name = entry.getKey();

			ByteInputStream bis = ByteInputStream.asLittleEndian(entry.getValue());

			switch (name) {
				case CodeViewSectionNames._DEBUG_S -> table.add(new CodeViewSymbolTable(parser, this, name, bis));
				case CodeViewSectionNames._DEBUG_T -> table.add(new CodeViewTypeTable(parser, this, name, bis));
				default -> {
					if (!parser.areUnknownSectionsAllowed()) {
						throw new IOException("Unknown CodeView section: " + name);
					}
					table.add(new CodeViewUnknownTable(this, name, bis));
				}
			}
		}
	}

	@Override
	public List<CodeViewTable> getElements() {
		return Collections.unmodifiableList(table);
	}
}
