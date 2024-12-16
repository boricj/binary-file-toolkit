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

public enum ElfSectionType {
	SHT_NULL(0, null, ElfNullSection.class),
	SHT_PROGBITS(1, null, ElfProgBits.class),
	SHT_SYMTAB(2, null, ElfSymbolTable.class),
	SHT_STRTAB(3, null, ElfStringTable.class),
	SHT_RELA(4, null, ElfRelaTable.class),
	SHT_NOBITS(8, null, ElfNoBits.class),
	SHT_REL(9, null, ElfRelTable.class),
	SHT_GROUP(17, null, ElfGroup.class),

	SHT_MIPS_REGINFO(0x70000006, ElfMachine.EM_MIPS, ElfRegInfo_Mips.class),
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

	public int getValue() {
		return value;
	}

	public ElfMachine getMachine() {
		return machine;
	}

	public Class<? extends ElfSection> getSectionClass() {
		return clazz;
	}

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
