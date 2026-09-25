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
package net.boricj.bft.dwarf.constants;

/**
 * Supported DWARF versions.
 */
public enum DwarfVersion {
	/** DWARF version 2. */
	DWARF2((short) 2),
	/** DWARF version 3. */
	DWARF3((short) 3),
	/** DWARF version 4. */
	DWARF4((short) 4),
	/** DWARF version 5. */
	DWARF5((short) 5),
	;

	private final short value;

	DwarfVersion(short value) {
		this.value = value;
	}

	/**
	 * Returns the numeric DWARF version value.
	 *
	 * @return version value
	 */
	public short getValue() {
		return value;
	}

	/**
	 * Returns whether this version is greater than or equal to the provided one.
	 *
	 * @param other reference version
	 * @return true when this version is at least {@code other}
	 */
	public boolean isAtLeast(DwarfVersion other) {
		return value >= other.value;
	}

	/**
	 * Looks up a DWARF version from its numeric value.
	 *
	 * @param value numeric version
	 * @return matching DWARF version
	 * @throws IllegalArgumentException when unsupported
	 */
	public static DwarfVersion valueFrom(int value) {
		for (DwarfVersion version : values()) {
			if (version.getValue() == value) {
				return version;
			}
		}

		throw new IllegalArgumentException("Unsupported DWARF version: " + value);
	}
}
