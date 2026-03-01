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
package net.boricj.bft.codeview.symbols.sections.symbols;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.codeview.constants.CodeViewSymbolType;
import net.boricj.bft.codeview.symbols.sections.CodeViewSymbol;

/**
 * Represents a 32‑bit procedure symbol with an ID instead of a type index.
 *
 * <p>The structure follows the PROCSYM32 layout defined by the CodeView
 * specification.  For the purposes of the current unit tests we only expose
 * the integer fields verified by {@link
 * net.boricj.bft.codeview.TestHelloWorld}.
 */
public class CodeViewSymbolProcSym32 extends CodeViewSymbol {
	private final int parentPointer;
	private final int endPointer;
	private final int nextPointer;
	private final int length;
	private final int debugStart;
	private final int debugEnd;
	private final int typeIndex;
	private final int offset;
	private final short segment;
	private final String name;

	public CodeViewSymbolProcSym32(ByteInputStream bis) throws IOException {
		super(CodeViewSymbolType.S_GPROC32_ID);

		parentPointer = bis.readInt();
		endPointer = bis.readInt();
		nextPointer = bis.readInt();
		length = bis.readInt();
		debugStart = bis.readInt();
		debugEnd = bis.readInt();
		typeIndex = bis.readInt();
		offset = bis.readInt();
		segment = bis.readShort();
		name = bis.readNullTerminatedString(StandardCharsets.UTF_8);
	}

	public int getParentPointer() {
		return parentPointer;
	}

	public int getEndPointer() {
		return endPointer;
	}

	public int getNextPointer() {
		return nextPointer;
	}

	public int getLength() {
		return length;
	}

	public int getDebugStart() {
		return debugStart;
	}

	public int getDebugEnd() {
		return debugEnd;
	}

	public int getTypeIndex() {
		return typeIndex;
	}

	public int getOffset() {
		return offset;
	}

	public short getSegment() {
		return segment;
	}

	public String getName() {
		return name;
	}

	@Override
	protected void write(ByteOutputStream bos) throws IOException {
		bos.writeInt((int) parentPointer);
		bos.writeInt((int) endPointer);
		bos.writeInt((int) nextPointer);
		bos.writeInt((int) length);
		bos.writeInt((int) debugStart);
		bos.writeInt((int) debugEnd);
		bos.writeInt((int) typeIndex);
		bos.writeInt((int) offset);
		bos.writeShort(segment);
		bos.writeNullTerminatedString(name, StandardCharsets.UTF_8);
	}
}
