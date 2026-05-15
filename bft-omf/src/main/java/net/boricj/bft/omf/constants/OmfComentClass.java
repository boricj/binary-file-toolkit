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
package net.boricj.bft.omf.constants;

/**
 * Enumeration of OMF comment record classes.
 *
 * <p>Each comment class identifies the purpose and structure of the comment data.
 */
public enum OmfComentClass {
	/** Translator (compiler/assembler) identification. */
	TRANSLATOR((byte) 0x00),
	/** Weak external symbol definitions. */
	WEAK_EXTERNALS((byte) 0xA1),
	/** Library search metadata. */
	LIBRARY_SEARCH((byte) 0xE8),
	/** Compiler dependency information. */
	COMPILER((byte) 0xE9);

	private final byte value;

	OmfComentClass(byte value) {
		this.value = value;
	}

	/**
	 * Returns the comment class byte value.
	 *
	 * @return the byte value
	 */
	public byte getValue() {
		return value;
	}

	/**
	 * Looks up a comment class by its byte value.
	 *
	 * @param value the comment class byte
	 * @return the corresponding comment class
	 * @throws IllegalArgumentException if the value is not recognized
	 */
	public static OmfComentClass valueFrom(byte value) {
		for (OmfComentClass commentClass : values()) {
			if (commentClass.value == value) {
				return commentClass;
			}
		}
		throw new IllegalArgumentException("Unsupported OMF comment class: 0x" + String.format("%02X", value & 0xFF));
	}
}
