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
package net.boricj.bft.codeview.tables;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.IndirectList;
import net.boricj.bft.codeview.CodeViewFile;
import net.boricj.bft.codeview.CodeViewFile.Parser;
import net.boricj.bft.codeview.CodeViewTable;
import net.boricj.bft.codeview.constants.CodeViewSignature;
import net.boricj.bft.codeview.constants.CodeViewSymbolTableSectionType;
import net.boricj.bft.codeview.symbols.CodeViewSymbolTableSection;
import net.boricj.bft.codeview.symbols.sections.CodeViewSymbolTableFileChecksumsSection;
import net.boricj.bft.codeview.symbols.sections.CodeViewSymbolTableFrameDataSection;
import net.boricj.bft.codeview.symbols.sections.CodeViewSymbolTableLinesSection;
import net.boricj.bft.codeview.symbols.sections.CodeViewSymbolTableStringTableSection;
import net.boricj.bft.codeview.symbols.sections.CodeViewSymbolTableSymbolSection;

public class CodeViewSymbolTable extends CodeViewTable implements IndirectList<CodeViewSymbolTableSection> {
	private class CodeViewSymbolTableUnknownSection extends CodeViewSymbolTableSection {
		private final byte[] data;

		public CodeViewSymbolTableUnknownSection(CodeViewSymbolTableSectionType type, byte[] data) throws IOException {
			super(type);

			this.data = data;
		}

		public CodeViewSymbolTableUnknownSection(CodeViewSymbolTableSectionType type, ByteInputStream bis)
				throws IOException {
			this(type, bis.readAllBytes());
		}

		@Override
		public void write(ByteOutputStream bos) throws IOException {
			bos.write(data);
		}
	}

	private final List<CodeViewSymbolTableSection> table = new ArrayList<>();

	protected CodeViewSymbolTable(CodeViewFile codeView, String name, CodeViewSignature signature) {
		super(codeView, name, signature);
	}

	public CodeViewSymbolTable(Parser parser, CodeViewFile codeView, String name, ByteInputStream bis)
			throws IOException {
		super(codeView, name, CodeViewSignature.valueFrom(bis.readInt()));

		while (bis.available() > 0) {
			bis.alignTo(4);

			CodeViewSymbolTableSectionType type = CodeViewSymbolTableSectionType.valueFrom(bis.readInt());
			int length = bis.readInt();

			ByteInputStream sectionBis = bis.slice(length);

			switch (type) {
				case DEBUG_S_SYMBOLS:
					table.add(new CodeViewSymbolTableSymbolSection(parser, this, sectionBis));
					break;
				case DEBUG_S_LINES:
					table.add(new CodeViewSymbolTableLinesSection(parser, this, sectionBis));
					break;
				case DEBUG_S_FILECHKSMS:
					table.add(new CodeViewSymbolTableFileChecksumsSection(parser, this, sectionBis));
					break;
				case DEBUG_S_STRINGTABLE:
					table.add(new CodeViewSymbolTableStringTableSection(parser, this, sectionBis));
					break;
				case DEBUG_S_FRAMEDATA:
					table.add(new CodeViewSymbolTableFrameDataSection(parser, this, sectionBis));
					break;

				default:
					if (!parser.areUnknownSectionsAllowed()) {
						throw new IOException("Unknown symbol table section type: " + type);
					}

					table.add(new CodeViewSymbolTableUnknownSection(type, sectionBis));
					break;
			}
		}
	}

	@Override
	public List<CodeViewSymbolTableSection> getElements() {
		return Collections.unmodifiableList(table);
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		ByteOutputStream bos = ByteOutputStream.asLittleEndian(outputStream);
		bos.writeInt(getSignature().getValue());

		for (CodeViewSymbolTableSection section : table) {
			bos.alignTo(4);

			ByteArrayOutputStream sectionBaos = new ByteArrayOutputStream();
			section.write(ByteOutputStream.asLittleEndian(sectionBaos));
			byte[] sectionData = sectionBaos.toByteArray();

			bos.writeInt(section.getType().getValue());
			bos.writeInt(sectionData.length);
			bos.write(sectionData);
		}
	}
}
