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
 * ELF data encoding indicating the byte order (endianness) of the file.
 */
public enum ElfData {
	/** Invalid data encoding. */
	ELFDATANONE((byte) 0),
	/** Little-endian (LSB) data encoding. */
	ELFDATA2LSB((byte) 1),
	/** Big-endian (MSB) data encoding. */
	ELFDATA2MSB((byte) 2),
	;

	private final byte value;

	ElfData(byte value) {
		this.value = value;
	}

	/**
	 * Returns the byte value of this data encoding.
	 *
	 * @return the byte value
	 */
	public byte getValue() {
		return value;
	}

	/**
	 * Returns the data encoding constant for the given byte value.
	 *
	 * @param value the byte value to look up
	 * @return the matching data encoding
	 * @throws IllegalArgumentException if the value is invalid
	 */
	public static ElfData valueFrom(byte value) {
		for (ElfData data : values()) {
			if (data.getValue() == value) {
				return data;
			}
		}

		throw new IllegalArgumentException();
	}
}
