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
package net.boricj.bft.codeview.symbols.sections.lines;

/**
 * Line mapping entry from a CodeView line table subsection.
 */
public class CodeViewLineEntry {
	private final int offset;
	private final int lineNumber;
	private final boolean fStartStatement;

	/**
	 * Creates a line mapping entry.
	 *
	 * @param offset code offset relative to the containing contribution
	 * @param lineNumber source line number
	 * @param fStartStatement whether this entry marks the start of a statement
	 */
	public CodeViewLineEntry(int offset, int lineNumber, boolean fStartStatement) {
		this.offset = offset;
		this.lineNumber = lineNumber;
		this.fStartStatement = fStartStatement;
	}

	/**
	 * Returns the code offset relative to the contribution base.
	 *
	 * @return code offset
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Returns the source line number.
	 *
	 * @return source line number
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	/**
	 * Returns whether this entry starts a statement.
	 *
	 * @return {@code true} if this line entry marks a statement boundary
	 */
	public boolean isFStartStatement() {
		return fStartStatement;
	}
}
