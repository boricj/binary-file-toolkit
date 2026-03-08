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
 * ELF OS/ABI identification indicating the target operating system and ABI.
 */
public enum ElfOsAbi {
	/** UNIX System V ABI. */
	ELFOSABI_NONE((byte) 0),
	;

	private final byte value;

	ElfOsAbi(byte value) {
		this.value = value;
	}

	/**
	 * Returns the byte value of this OS/ABI.
	 *
	 * @return the byte value
	 */
	public byte getValue() {
		return value;
	}

	/**
	 * Returns the OS/ABI constant for the given byte value.
	 *
	 * @param value the byte value to look up
	 * @return the matching OS/ABI
	 * @throws IllegalArgumentException if the value is invalid
	 */
	public static ElfOsAbi valueFrom(byte value) {
		for (ElfOsAbi osAbi : values()) {
			if (osAbi.getValue() == value) {
				return osAbi;
			}
		}

		throw new IllegalArgumentException();
	}
}
