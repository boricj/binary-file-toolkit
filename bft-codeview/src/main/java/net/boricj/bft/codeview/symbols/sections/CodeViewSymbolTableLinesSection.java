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
import net.boricj.bft.codeview.CodeViewFile.Parser;
import net.boricj.bft.codeview.constants.CodeViewSymbolTableSectionType;
import net.boricj.bft.codeview.symbols.CodeViewSymbolTableSection;
import net.boricj.bft.codeview.symbols.sections.lines.CodeViewFileBlock;
import net.boricj.bft.codeview.symbols.sections.lines.CodeViewLineEntry;
import net.boricj.bft.codeview.tables.CodeViewSymbolTable;

public class CodeViewSymbolTableLinesSection extends CodeViewSymbolTableSection {
	private final int offCon;
	private final int segCon;
	private final int flags;
	private int cbCon;
	private final List<CodeViewFileBlock> fileBlocks;

	public CodeViewSymbolTableLinesSection(Parser parser, CodeViewSymbolTable table, ByteInputStream bis)
			throws IOException {
		super(CodeViewSymbolTableSectionType.DEBUG_S_LINES);

		offCon = bis.readInt();
		segCon = bis.readShort() & 0xFFFF;
		flags = bis.readShort() & 0xFFFF;
		cbCon = bis.readInt();

		// parse file blocks directly from the slice
		ByteInputStream dataBis = bis;
		List<CodeViewFileBlock> blocks = new ArrayList<>();
		boolean fHasColumn = (flags & 0x0001) != 0;

		while (dataBis.available() > 0) {
			int offFile = dataBis.readInt();
			int nLines = dataBis.readInt();
			int cbBlock = dataBis.readInt();
			System.out.printf(
					"[DEBUG LINES PARSE DETAIL] Raw: offFile=0x%08x nLines=0x%08x cbBlock=0x%08x\n",
					offFile, nLines, cbBlock);

			List<CodeViewLineEntry> lines = new ArrayList<>();
			for (int i = 0; i < nLines && dataBis.available() >= 8; i++) {
				int lineInfo = dataBis.readInt();
				int offset = dataBis.readInt();

				int lineNumber = lineInfo & 0xFFFFFF;
				boolean fStartStatement = (lineInfo & 0x80000000) != 0;

				lines.add(new CodeViewLineEntry(offset, lineNumber, fStartStatement));
			}

			// Skip column data if present and remaining padding
			int bytesConsumed = 12 + (lines.size() * 8);
			if (fHasColumn) {
				for (int i = 0; i < lines.size() && dataBis.available() >= 4; i++) {
					dataBis.readShort();
					dataBis.readShort();
					bytesConsumed += 4;
				}
			}

			// Skip remaining padding
			while (bytesConsumed < cbBlock && dataBis.available() > 0) {
				dataBis.readByte();
				bytesConsumed++;
			}

			blocks.add(new CodeViewFileBlock(offFile, lines));
			System.out.printf(
					"[DEBUG LINES PARSE] FileBlock: offFile=%d, nLines=%d, cbBlock=%d\n", offFile, nLines, cbBlock);
		}

		this.fileBlocks = Collections.unmodifiableList(blocks);
	}

	@Override
	public void write(ByteOutputStream bos) throws IOException {
		// rebuild data section from parsed blocks
		ByteArrayOutputStream dataBaos = new ByteArrayOutputStream();
		ByteOutputStream dataBos = ByteOutputStream.asLittleEndian(dataBaos);
		boolean fHasColumn = (flags & 0x0001) != 0;

		for (CodeViewFileBlock fb : fileBlocks) {
			ByteArrayOutputStream fbBaos = new ByteArrayOutputStream();
			ByteOutputStream fbBos = ByteOutputStream.asLittleEndian(fbBaos);

			fbBos.writeInt(fb.getOffFile());
			fbBos.writeInt(fb.getLines().size());
			// placeholder for cbBlock
			fbBos.writeInt(0);

			for (CodeViewLineEntry le : fb.getLines()) {
				int lineInfo = le.getLineNumber() & 0xFFFFFF;
				if (le.isFStartStatement()) {
					lineInfo |= 0x80000000;
				}
				fbBos.writeInt(lineInfo);
				fbBos.writeInt(le.getOffset());
			}

			if (fHasColumn) {
				for (int i = 0; i < fb.getLines().size(); i++) {
					fbBos.writeShort((short) 0);
					fbBos.writeShort((short) 0);
				}
			}

			// calculate cbBlock and patch
			byte[] fbBytes = fbBaos.toByteArray();
			// cbBlock includes the 12-byte header (offFile + nLines + cbBlock)
			int cbBlock = fbBytes.length;
			System.out.printf(
					"[DEBUG LINES WRITE] FileBlock: offFile=%d, nLines=%d, cbBlock=%d (total bytes=%d)\n",
					fb.getOffFile(), fb.getLines().size(), cbBlock, fbBytes.length);
			// patch at offset 8 (after offFile and nLines)
			fbBytes[8] = (byte) (cbBlock & 0xFF);
			fbBytes[9] = (byte) ((cbBlock >> 8) & 0xFF);
			fbBytes[10] = (byte) ((cbBlock >> 16) & 0xFF);
			fbBytes[11] = (byte) ((cbBlock >> 24) & 0xFF);

			dataBos.write(fbBytes);
		}

		byte[] dataBytes = dataBaos.toByteArray();
		// do not modify cbCon; it reflects the original declared size

		bos.writeInt(offCon);
		bos.writeShort((short) segCon);
		bos.writeShort((short) flags);
		System.out.printf(
				"[DEBUG LINES WRITE HEADER] offCon=0x%08x segCon=%d flags=0x%04x cbCon=%d\n",
				offCon, segCon, flags, cbCon);
		bos.writeInt(cbCon);
		bos.write(dataBytes);
	}

	public int getOffsetCon() {
		return offCon;
	}

	public int getSegmentCon() {
		return segCon;
	}

	public int getFlags() {
		return flags;
	}

	public int getCbCon() {
		return cbCon;
	}

	public List<CodeViewFileBlock> getFileBlocks() {
		return fileBlocks;
	}
}
