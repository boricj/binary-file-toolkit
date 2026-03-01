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
 * Marker symbol indicating the end of a procedure ID scope.
 *
 * <p>The S_PROC_ID_END record carries no additional payload; it merely
 * signals that a previously started procedure (such as S_GPROC32_ID) has
 * concluded.  For round‑trip purposes we write nothing beyond the type code.
 */
public class CodeViewSymbolProcIdEnd extends CodeViewSymbol {
	public CodeViewSymbolProcIdEnd(ByteInputStream bis) throws IOException {
		super(CodeViewSymbolType.S_PROC_ID_END);
		// no further data to consume
	}

	@Override
	protected void write(ByteOutputStream bos) throws IOException {
		// nothing to write
	}
}
