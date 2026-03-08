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
 * ELF version field indicating the ELF format version.
 */
public enum ElfVersion {
	/** Invalid version. */
	EV_NONE((byte) 0),
	/** Current ELF format version. */
	EV_CURRENT((byte) 1),
	;

	private final byte value;

	ElfVersion(byte value) {
		this.value = value;
	}

	/**
	 * Returns the byte value of this version.
	 *
	 * @return the byte value
	 */
	public byte getValue() {
		return value;
	}

	/**
	 * Returns the version constant for the given byte value.
	 *
	 * @param value the byte value to look up
	 * @return the matching version
	 * @throws IllegalArgumentException if the value is invalid
	 */
	public static ElfVersion valueFrom(byte value) {
		for (ElfVersion version : values()) {
			if (version.getValue() == value) {
				return version;
			}
		}

		throw new IllegalArgumentException();
	}
}
