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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.IndirectList;
import net.boricj.bft.codeview.CodeViewFile.Parser;
import net.boricj.bft.codeview.constants.CodeViewSymbolTableSectionType;
import net.boricj.bft.codeview.symbols.CodeViewSymbolTableSection;
import net.boricj.bft.codeview.symbols.sections.frame.CodeViewFrameData;
import net.boricj.bft.codeview.tables.CodeViewSymbolTable;

public class CodeViewSymbolTableFrameDataSection extends CodeViewSymbolTableSection
		implements IndirectList<CodeViewFrameData> {
	private final int rva;
	private final List<CodeViewFrameData> table = new ArrayList<>();

	public CodeViewSymbolTableFrameDataSection(
			Parser parser, CodeViewSymbolTable codeViewSymbolsTable, ByteInputStream bis) throws IOException {
		super(CodeViewSymbolTableSectionType.DEBUG_S_FRAMEDATA);

		this.rva = bis.readInt();

		while (bis.available() > 0) {
			CodeViewFrameData fd = new CodeViewFrameData(bis);
			table.add(fd);
		}
	}

	public int getRva() {
		return rva;
	}

	@Override
	public List<CodeViewFrameData> getElements() {
		return Collections.unmodifiableList(table);
	}

	@Override
	public void write(ByteOutputStream bos) throws IOException {
		bos.writeInt(rva);

		for (CodeViewFrameData fd : table) {
			fd.write(bos);
		}
	}
}
