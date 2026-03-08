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
 * ELF object file type.
 */
public enum ElfType {
	/** No file type. */
	ET_NONE((short) 0),
	/** Relocatable file. */
	ET_REL((short) 1),
	/** Executable file. */
	ET_EXEC((short) 2),
	/** Shared object file. */
	ET_DYN((short) 3),
	/** Core file. */
	ET_CORE((short) 4),
	;

	private final short value;

	ElfType(short value) {
		this.value = value;
	}

	/**
	 * Returns the short value of this file type.
	 *
	 * @return the short value
	 */
	public short getValue() {
		return value;
	}

	/**
	 * Returns the file type constant for the given short value.
	 *
	 * @param value the short value to look up
	 * @return the matching file type
	 * @throws IllegalArgumentException if the value is invalid
	 */
	public static ElfType valueFrom(short value) {
		for (ElfType type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}

		throw new IllegalArgumentException();
	}
}
