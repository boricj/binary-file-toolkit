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

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.codeview.constants.CodeViewSymbolType;
import net.boricj.bft.codeview.symbols.sections.CodeViewSymbol;

/**
 * Minimal representation of the S_BUILDINFO symbol.  It only exposes the
 * {@code id} field (called "type index" by the unit tests) since
 * that's all they currently care about.
 */
public class CodeViewSymbolBuildInfo extends CodeViewSymbol {
	private final int typeIndex;

	public CodeViewSymbolBuildInfo(ByteInputStream bis) throws IOException {
		this(bis.readInt());
	}

	public CodeViewSymbolBuildInfo(int typeIndex) {
		super(CodeViewSymbolType.S_BUILDINFO);

		this.typeIndex = typeIndex;
	}

	public int getTypeIndex() {
		return typeIndex;
	}

	@Override
	protected void write(ByteOutputStream bos) throws IOException {
		bos.writeInt(typeIndex);
	}
}
