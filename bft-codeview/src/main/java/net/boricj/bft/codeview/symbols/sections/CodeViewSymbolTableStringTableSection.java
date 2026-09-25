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
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.codeview.CodeViewFile.Parser;
import net.boricj.bft.codeview.constants.CodeViewSymbolTableSectionType;
import net.boricj.bft.codeview.symbols.CodeViewSymbolTableSection;
import net.boricj.bft.codeview.tables.CodeViewSymbolTable;

/**
 * {@code DEBUG_S_STRINGTABLE} subsection storing offset-addressable UTF-8 strings.
 */
public class CodeViewSymbolTableStringTableSection extends CodeViewSymbolTableSection {
	private final SortedMap<Integer, String> stringsByOffset = new TreeMap<>();

	/**
	 * Parses a string-table subsection.
	 *
	 * @param parser parser settings
	 * @param table owning symbol table
	 * @param bis input stream positioned at subsection payload
	 * @throws IOException if parsing fails
	 */
	public CodeViewSymbolTableStringTableSection(Parser parser, CodeViewSymbolTable table, ByteInputStream bis)
			throws IOException {
		super(CodeViewSymbolTableSectionType.DEBUG_S_STRINGTABLE);

		// Parse strings from raw data
		int offset = 0;
		for (offset = 0; bis.available() > 0; offset = bis.getCount()) {
			String str = bis.readNullTerminatedString(StandardCharsets.UTF_8);
			stringsByOffset.put(offset, str);
		}
	}

	@Override
	public void write(ByteOutputStream bos) throws IOException {
		for (Map.Entry<Integer, String> entry : stringsByOffset.entrySet()) {
			bos.writeNullTerminatedString(entry.getValue(), StandardCharsets.UTF_8);
		}
	}

	/**
	 * Returns the string stored at a given byte offset.
	 *
	 * @param offset byte offset within subsection payload
	 * @return decoded string, or {@code null} if the offset is unknown
	 */
	public String getStringAtOffset(int offset) {
		return stringsByOffset.get(offset);
	}

	/**
	 * Returns all parsed strings keyed by payload offset.
	 *
	 * @return mutable sorted map of offsets to strings
	 */
	public Map<Integer, String> getStringsByOffset() {
		return stringsByOffset;
	}
}
