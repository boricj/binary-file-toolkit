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
package net.boricj.bft.elf.constants;

/**
 * ELF symbol types indicating what kind of entity a symbol represents.
 */
public enum ElfSymbolType {
	/** Symbol type is not specified. */
	STT_NOTYPE((byte) 0),
	/** Symbol is a data object. */
	STT_OBJECT((byte) 1),
	/** Symbol is a function or executable code. */
	STT_FUNC((byte) 2),
	/** Symbol is a section. */
	STT_SECTION((byte) 3),
	/** Symbol is a file name. */
	STT_FILE((byte) 4),
	/** Symbol is a thread-local storage entity. */
	STT_TLS((byte) 6),
	;

	private final byte value;

	private ElfSymbolType(byte value) {
		this.value = value;
	}

	/**
	 * Returns the byte value of this symbol type.
	 *
	 * @return the byte value
	 */
	public byte getValue() {
		return value;
	}

	/**
	 * Returns the symbol type constant for the given byte value.
	 *
	 * @param value the byte value to look up
	 * @return the matching symbol type
	 * @throws IllegalArgumentException if the value is invalid
	 */
	public static ElfSymbolType valueFrom(byte value) {
		for (ElfSymbolType type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}

		throw new IllegalArgumentException();
	}
}
