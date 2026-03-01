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
package net.boricj.bft.codeview.symbols;

import java.io.IOException;

import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.codeview.constants.CodeViewSymbolTableSectionType;

public abstract class CodeViewSymbolTableSection {
	private final CodeViewSymbolTableSectionType type;

	public CodeViewSymbolTableSection(CodeViewSymbolTableSectionType type) {
		this.type = type;
	}

	public CodeViewSymbolTableSectionType getType() {
		return type;
	}

	public abstract void write(ByteOutputStream bos) throws IOException;
}
