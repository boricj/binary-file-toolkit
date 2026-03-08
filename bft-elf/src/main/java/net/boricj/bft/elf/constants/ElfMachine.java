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
import net.boricj.bft.elf.machines.amd64.ElfRelocationType_amd64;
import net.boricj.bft.elf.machines.i386.ElfRelocationType_i386;
import net.boricj.bft.elf.machines.mips.ElfRelocationType_Mips;
import net.boricj.bft.elf.machines.mips.ElfSectionFlags_Mips;

/**
 * ELF target machine architecture types.
 */
public enum ElfMachine {
	/** No machine. */
	EM_NONE((short) 0, "None", ElfSectionFlags.class, null),
	/** Intel 80386. */
	EM_386((short) 3, "Intel 80386", ElfSectionFlags.class, ElfRelocationType_i386.class),
	/** MIPS R3000. */
	EM_MIPS((short) 8, "MIPS R3000", ElfSectionFlags_Mips.class, ElfRelocationType_Mips.class),
	/** AMD x86-64. */
	EM_X86_64((short) 62, "Advanced Micro Devices X86-64", ElfSectionFlags.class, ElfRelocationType_amd64.class),
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

	/**
	 * Returns the short value of this machine type.
	 *
	 * @return the short value
	 */
	public short getValue() {
		return value;
	}

	/**
	 * Returns the descriptive name of this machine type.
	 *
	 * @return the machine name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the section flags class for this machine type.
	 *
	 * @return the section flags class
	 */
	public Class<? extends ElfSectionFlags> getSectionFlagsClass() {
		return sectionFlags;
	}

	/**
	 * Returns the relocation type class for this machine type.
	 *
	 * @return the relocation type class, or null if not supported
	 */
	public Class<? extends ElfRelocationType> getRelocationTypeClass() {
		return relocationType;
	}

	/**
	 * Returns the machine type constant for the given short value.
	 *
	 * @param value the short value to look up
	 * @return the matching machine type
	 * @throws IllegalArgumentException if the value is invalid
	 */
	public static ElfMachine valueFrom(short value) {
		for (ElfMachine machine : values()) {
			if (machine.getValue() == value) {
				return machine;
			}
		}

		throw new IllegalArgumentException();
	}
}
