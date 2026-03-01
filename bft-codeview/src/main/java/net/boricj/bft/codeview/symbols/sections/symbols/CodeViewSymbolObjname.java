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

public class CodeViewSymbolObjname extends CodeViewSymbol {
	private final int signature;
	private final String name;

	public CodeViewSymbolObjname(ByteInputStream bis) throws IOException {
		this(bis.readInt(), bis.readNullTerminatedString(StandardCharsets.UTF_8));
	}

	public CodeViewSymbolObjname(int signature, String name) {
		super(CodeViewSymbolType.S_OBJNAME);

		this.signature = signature;
		this.name = name;
	}

	public int getSignature() {
		return signature;
	}

	public String getName() {
		return name;
	}

	@Override
	protected void write(ByteOutputStream bos) throws IOException {
		bos.writeInt(signature);
		bos.writeNullTerminatedString(name, StandardCharsets.UTF_8);
	}
}
