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

import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.codeview.constants.CodeViewSymbolType;

/**
 * Base class for decoded CodeView symbol records.
 */
public abstract class CodeViewSymbol {
	private final CodeViewSymbolType type;

	/**
	 * Creates a symbol with a specific CodeView symbol kind.
	 *
	 * @param type symbol record type identifier
	 */
	public CodeViewSymbol(CodeViewSymbolType type) {
		this.type = type;
	}

	/**
	 * Returns the symbol record kind.
	 *
	 * @return symbol record type identifier
	 */
	public CodeViewSymbolType getType() {
		return type;
	}

	/**
	 * Writes symbol payload bytes (excluding record length and type fields).
	 *
	 * @param bos output stream receiving encoded payload bytes
	 * @throws IOException if writing fails
	 */
	protected abstract void write(ByteOutputStream bos) throws IOException;
}
