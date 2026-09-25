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
package net.boricj.bft.codeview.constants;

/**
 * CodeView subsection type identifiers used inside {@code .debug$S} symbol tables.
 */
public enum CodeViewSymbolTableSectionType {
	/** Symbol records subsection. */
	DEBUG_S_SYMBOLS(0xf1),
	/** Source line mapping subsection. */
	DEBUG_S_LINES(0xf2),
	/** String table subsection. */
	DEBUG_S_STRINGTABLE(0xf3),
	/** File checksums subsection. */
	DEBUG_S_FILECHKSMS(0xf4),
	/** Frame data subsection. */
	DEBUG_S_FRAMEDATA(0xf5),
	/** Inlinee lines subsection. */
	DEBUG_S_INLINEELINES(0xf6),
	/** Cross-scope imports subsection. */
	DEBUG_S_CROSSSCOPEIMPORTS(0xf7),
	/** Cross-scope exports subsection. */
	DEBUG_S_CROSSSCOPEEXPORTS(0xf8),

	/** IL line mapping subsection. */
	DEBUG_S_IL_LINES(0xf9),
	/** Function metadata token map subsection. */
	DEBUG_S_FUNC_MDTOKEN_MAP(0xfa),
	/** Type metadata token map subsection. */
	DEBUG_S_TYPE_MDTOKEN_MAP(0xfb),
	/** Merged assembly input subsection. */
	DEBUG_S_MERGED_ASSEMBLYINPUT(0xfc),

	/** COFF symbol RVA map subsection. */
	DEBUG_S_COFF_SYMBOL_RVA(0xfd);

	private final int value;

	CodeViewSymbolTableSectionType(int value) {
		this.value = value;
	}

	/**
	 * Returns raw subsection type identifier.
	 *
	 * @return raw subsection type identifier
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Resolves a raw subsection type identifier.
	 *
	 * @param value raw subsection type identifier
	 * @return resolved subsection type
	 * @throws IllegalArgumentException if {@code value} is unknown
	 */
	public static CodeViewSymbolTableSectionType valueFrom(int value) {
		for (CodeViewSymbolTableSectionType subsection : values()) {
			if (subsection.getValue() == value) {
				return subsection;
			}
		}

		throw new IllegalArgumentException();
	}
}
