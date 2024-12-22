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

import net.boricj.bft.elf.ElfSectionFlags;

public enum ElfMachine {
	EM_NONE((short) 0, "None", ElfSectionFlags.class, null),
	;

	private final short value;
	private final String name;
	private final Class<? extends ElfSectionFlags> sectionFlags;
	private final Class<? extends ElfRelocationType> relocationType;

	ElfMachine(
			short value,
			String name,
			Class<? extends ElfSectionFlags> sectionFlags,
			Class<? extends ElfRelocationType> relocationType) {
		this.value = value;
		this.name = name;
		this.sectionFlags = sectionFlags;
		this.relocationType = relocationType;
	}

	public short getValue() {
		return value;
	}

	public String getName() {
		return name;
	}

	public Class<? extends ElfSectionFlags> getSectionFlagsClass() {
		return sectionFlags;
	}

	public Class<? extends ElfRelocationType> getRelocationTypeClass() {
		return relocationType;
	}

	public static ElfMachine valueFrom(short value) {
		for (ElfMachine machine : values()) {
			if (machine.getValue() == value) {
				return machine;
			}
		}

		throw new IllegalArgumentException();
	}
}
