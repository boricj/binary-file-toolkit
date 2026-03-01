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
 * Parser for the S_BPREL32 symbol which describes a BP-relative variable.
 */
public class CodeViewSymbolBpRel32 extends CodeViewSymbol {
	private final int bpOffset;
	private final int typeIndex;
	private final String name;

	public CodeViewSymbolBpRel32(ByteInputStream bis) throws IOException {
		this(bis.readInt(), bis.readInt(), bis.readNullTerminatedString(StandardCharsets.UTF_8));
	}

	public CodeViewSymbolBpRel32(int bpOffset, int typeIndex, String name) {
		super(CodeViewSymbolType.S_BPREL32);

		this.bpOffset = bpOffset;
		this.typeIndex = typeIndex;
		this.name = name;
	}

	public int getBpOffset() {
		return bpOffset;
	}

	public int getTypeIndex() {
		return typeIndex;
	}

	public String getName() {
		return name;
	}

	@Override
	protected void write(ByteOutputStream bos) throws IOException {
		bos.writeInt(bpOffset);
		bos.writeInt(typeIndex);
		bos.writeNullTerminatedString(name, StandardCharsets.UTF_8);
	}
}
