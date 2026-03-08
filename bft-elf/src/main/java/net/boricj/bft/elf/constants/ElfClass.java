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
 * ELF class indicating the bit width of the file.
 */
public enum ElfClass {
	/** Invalid class. */
	ELFCLASSNONE((byte) 0),
	/** 32-bit file. */
	ELFCLASS32((byte) 1),
	/** 64-bit file. */
	ELFCLASS64((byte) 2),
	;

	private final byte value;

	ElfClass(byte value) {
		this.value = value;
	}

	/**
	 * Returns the byte value of this ELF class.
	 *
	 * @return the byte value
	 */
	public byte getValue() {
		return value;
	}

	/**
	 * Returns the ELF class constant for the given byte value.
	 *
	 * @param value the byte value to look up
	 * @return the matching ELF class
	 * @throws IllegalArgumentException if the value is invalid
	 */
	public static ElfClass valueFrom(byte value) {
		for (ElfClass class_ : values()) {
			if (class_.getValue() == value) {
				return class_;
			}
		}

		throw new IllegalArgumentException();
	}
}
