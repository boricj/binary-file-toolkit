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

	public static class Parser {
		private final Map<String, byte[]> sectionsData = new HashMap<>();
		private boolean allowUnknownSections = false;
		private boolean allowUnknownSymbols = false;

		public Parser() {}

		public Map<String, byte[]> getSectionsData() {
			return sectionsData;
		}

		public Parser addSection(String name, byte[] data) {
			sectionsData.put(name, data);
			return this;
		}

		public Parser allowUnknownSections(boolean allow) {
			this.allowUnknownSections = allow;
			return this;
		}

		public Parser allowUnknownSymbols(boolean allow) {
			this.allowUnknownSymbols = allow;
			return this;
		}

		public CodeViewFile parse() throws IOException {
			return new CodeViewFile(this);
		}

		public boolean areUnknownSectionsAllowed() {
			return allowUnknownSections;
		}

		public boolean areUnknownSymbolsAllowed() {
			return allowUnknownSymbols;
		}
	}

	public CodeViewFile() {}

	private CodeViewFile(Parser parser) throws IOException {
		for (Map.Entry<String, byte[]> entry : parser.getSectionsData().entrySet()) {
			String name = entry.getKey();

			ByteInputStream bis = ByteInputStream.asLittleEndian(entry.getValue());

			switch (name) {
				case CodeViewSectionNames._DEBUG_S -> table.add(new CodeViewSymbolTable(parser, this, name, bis));
				case CodeViewSectionNames._DEBUG_T -> table.add(new CodeViewTypeTable(parser, this, name, bis));
				default -> table.add(new CodeViewUnknownTable(this, name, bis));
			}
		}
	}

	@Override
	public List<CodeViewTable> getElements() {
		return Collections.unmodifiableList(table);
	}
}
