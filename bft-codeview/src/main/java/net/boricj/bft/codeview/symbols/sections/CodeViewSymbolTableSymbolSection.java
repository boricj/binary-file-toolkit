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
package net.boricj.bft.codeview.symbols.sections;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.IndirectList;
import net.boricj.bft.codeview.CodeViewFile.Parser;
import net.boricj.bft.codeview.constants.CodeViewSymbolTableSectionType;
import net.boricj.bft.codeview.constants.CodeViewSymbolType;
import net.boricj.bft.codeview.symbols.CodeViewSymbolTableSection;
import net.boricj.bft.codeview.symbols.sections.symbols.CodeViewSymbolBpRel32;
import net.boricj.bft.codeview.symbols.sections.symbols.CodeViewSymbolBuildInfo;
import net.boricj.bft.codeview.symbols.sections.symbols.CodeViewSymbolCompile3;
import net.boricj.bft.codeview.symbols.sections.symbols.CodeViewSymbolFrameProc;
import net.boricj.bft.codeview.symbols.sections.symbols.CodeViewSymbolObjname;
import net.boricj.bft.codeview.symbols.sections.symbols.CodeViewSymbolProcIdEnd;
import net.boricj.bft.codeview.symbols.sections.symbols.CodeViewSymbolProcSym32;
import net.boricj.bft.codeview.tables.CodeViewSymbolTable;

public class CodeViewSymbolTableSymbolSection extends CodeViewSymbolTableSection
		implements IndirectList<CodeViewSymbol> {
	private static class CodeViewSymbolUnknown extends CodeViewSymbol {
		private final byte[] data;

		public CodeViewSymbolUnknown(CodeViewSymbolType type, byte[] data) {
			super(type);

			this.data = data;
		}

		public CodeViewSymbolUnknown(CodeViewSymbolType type, ByteInputStream bis) throws IOException {
			this(type, bis.readAllBytes());
		}

		@Override
		protected void write(ByteOutputStream bos) throws IOException {
			bos.write(data);
		}
	}

	private final List<CodeViewSymbol> table = new ArrayList<>();

	public CodeViewSymbolTableSymbolSection(
			Parser parser, CodeViewSymbolTable codeViewSymbolsTable, ByteInputStream bis) throws IOException {
		super(CodeViewSymbolTableSectionType.DEBUG_S_SYMBOLS);

		while (bis.available() >= 4) {
			short length = bis.readShort();
			if (length < 2) {
				break;
			}

			CodeViewSymbolType symbolType = CodeViewSymbolType.valueFrom(bis.readShort());

			// The symbol length includes the symbol type field, but not the length field.
			// At this point, the remaining bytes to read for this record is length - 2.
			ByteInputStream symbolBis = bis.slice(length - 2);

			// Parse symbol records based on type.
			switch (symbolType) {
				case S_BPREL32:
					table.add(new CodeViewSymbolBpRel32(symbolBis));
					break;

				case S_BUILDINFO:
					table.add(new CodeViewSymbolBuildInfo(symbolBis));
					break;

				case S_COMPILE3:
					table.add(new CodeViewSymbolCompile3(symbolBis));
					break;

				case S_FRAMEPROC:
					table.add(new CodeViewSymbolFrameProc(symbolBis));
					break;

				case S_OBJNAME:
					table.add(new CodeViewSymbolObjname(symbolBis));
					break;

				case S_PROC_ID_END:
					table.add(new CodeViewSymbolProcIdEnd(symbolBis));
					break;

				case S_GPROC32_ID:
					table.add(new CodeViewSymbolProcSym32(symbolBis));
					break;

				default:
					if (!parser.areUnknownSymbolsAllowed()) {
						throw new IOException("Unknown symbol type: " + symbolType);
					}

					table.add(new CodeViewSymbolUnknown(symbolType, symbolBis));
					break;
			}
		}
	}

	@Override
	public List<CodeViewSymbol> getElements() {
		return Collections.unmodifiableList(table);
	}

	@Override
	public void write(ByteOutputStream bos) throws IOException {
		for (CodeViewSymbol symbol : table) {
			ByteArrayOutputStream symbolBaos = new ByteArrayOutputStream();
			ByteOutputStream symbolBos = ByteOutputStream.asLittleEndian(symbolBaos);

			symbol.write(symbolBos);
			byte[] symbolBytes = symbolBaos.toByteArray();
			int recordLength = 2 + symbolBytes.length;

			bos.writeShort(recordLength);
			bos.writeShort(symbol.getType().getValue());
			bos.write(symbolBytes);
		}
	}
}
