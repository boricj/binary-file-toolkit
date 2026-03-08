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

import net.boricj.bft.elf.ElfSection;
import net.boricj.bft.elf.machines.mips.ElfAbiFlags_Mips;
import net.boricj.bft.elf.machines.mips.ElfRegInfo_Mips;
import net.boricj.bft.elf.sections.ElfGroup;
import net.boricj.bft.elf.sections.ElfNoBits;
import net.boricj.bft.elf.sections.ElfNullSection;
import net.boricj.bft.elf.sections.ElfProgBits;
import net.boricj.bft.elf.sections.ElfRelTable;
import net.boricj.bft.elf.sections.ElfRelaTable;
import net.boricj.bft.elf.sections.ElfStringTable;
import net.boricj.bft.elf.sections.ElfSymbolTable;

/**
 * ELF section types defining the contents and semantics of sections.
 */
public enum ElfSectionType {
	/** Inactive section with no associated content. */
	SHT_NULL(0, null, ElfNullSection.class),
	/** Section holding program-defined information. */
	SHT_PROGBITS(1, null, ElfProgBits.class),
	/** Symbol table. */
	SHT_SYMTAB(2, null, ElfSymbolTable.class),
	/** String table. */
	SHT_STRTAB(3, null, ElfStringTable.class),
	/** Relocation entries with explicit addends. */
	SHT_RELA(4, null, ElfRelaTable.class),
	/** Section occupying no space in the file. */
	SHT_NOBITS(8, null, ElfNoBits.class),
	/** Relocation entries without explicit addends. */
	SHT_REL(9, null, ElfRelTable.class),
	/** Section group. */
	SHT_GROUP(17, null, ElfGroup.class),

	/** MIPS register information. */
	SHT_MIPS_REGINFO(0x70000006, ElfMachine.EM_MIPS, ElfRegInfo_Mips.class),
	/** MIPS ABI flags. */
	SHT_MIPS_ABIFLAGS(0x7000002a, ElfMachine.EM_MIPS, ElfAbiFlags_Mips.class),
	;

	private final int value;
	private final ElfMachine machine;
	private final Class<? extends ElfSection> clazz;

	private ElfSectionType(int value, ElfMachine machine, Class<? extends ElfSection> clazz) {
		this.value = value;
		this.machine = machine;
		this.clazz = clazz;
	}

	/**
	 * Returns the integer value of this section type.
	 *
	 * @return the integer value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Returns the machine type this section type is specific to.
	 *
	 * @return the machine type, or null if generic
	 */
	public ElfMachine getMachine() {
		return machine;
	}

	/**
	 * Returns the section implementation class for this type.
	 *
	 * @return the section class
	 */
	public Class<? extends ElfSection> getSectionClass() {
		return clazz;
	}

	/**
	 * Returns the section type constant for the given value and machine type.
	 *
	 * @param value the integer value to look up
	 * @param machine the target machine type
	 * @return the matching section type
	 * @throws IllegalArgumentException if the value is invalid
	 */
	public static ElfSectionType valueFrom(int value, ElfMachine machine) {
		for (ElfSectionType type : values()) {
			ElfMachine typeMachine = type.getMachine();

			if (type.getValue() == value && (typeMachine == null || typeMachine == machine)) {
				return type;
			}
		}

		throw new IllegalArgumentException();
	}
}
