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
package net.boricj.bft.elf.machines.ppc;

import net.boricj.bft.elf.constants.ElfMachine;
import net.boricj.bft.elf.constants.ElfRelocationType;

/**
 * PPC ELF relocation types.
 * These constants define the relocation mechanisms specific to the PowerPC 32-bit architecture.
 */
public enum ElfRelocationType_ppc implements ElfRelocationType {
	/** No relocation. */
	R_PPC_NONE(0),
	/** Direct 32-bit. */
	R_PPC_ADDR32(1),
	/** Direct 24-bit. */
	R_PPC_ADDR24(2),
	/** Direct 16-bit. */
	R_PPC_ADDR16(3),
	/** Direct 32-bit with bits 0 through 15. */
	R_PPC_ADDR16_LO(4),
	/** Direct 32-bit with bits 16 through 31. */
	R_PPC_ADDR16_HI(5),
	/** Direct 32-bit with bits 16 through 31 with adjustment for the carry. */
	R_PPC_ADDR16_HA(6),
	/** Direct 14-bit. */
	R_PPC_ADDR14(7),
	/** Direct 14-bit with branch taken. */
	R_PPC_ADDR14_BRTAKEN(8),
	/** Direct 14-bit with branch not taken. */
	R_PPC_ADDR14_BRNTAKEN(9),
	/** Relative 24-bit. */
	R_PPC_REL24(10),
	/** Relative 14-bit. */
	R_PPC_REL14(11),
	/** Relative 14-bit with branch taken. */
	R_PPC_REL14_BRTAKEN(12),
	/** Relative 14-bit with branch not taken. */
	R_PPC_REL14_BRNTAKEN(13),
	/** Global Offset Table 16-bit. */
	R_PPC_GOT16(14),
	/** Global Offset Table 32-bit with bits 0 through 15. */
	R_PPC_GOT16_LO(15),
	/** Global Offset Table 32-bit with bits 16 through 31. */
	R_PPC_GOT16_HI(16),
	/** Global Offset Table 32-bit with bits 16 through 31 with adjustment for the carry. */
	R_PPC_GOT16_HA(17),
	/** Procedure Linkage Table relative 24-bit. */
	R_PPC_PLTREL24(18),
	/** Dynamic linking stuff. */
	R_PPC_COPY(19),
	/** Global data. */
	R_PPC_GLOB_DAT(20),
	/** Dynamic linking stuff. */
	R_PPC_JMP_SLOT(21),
	/** Dynamic linking stuff. */
	R_PPC_RELATIVE(22),
	/** Dynamic linking stuff. */
	R_PPC_LOCAL24PC(23),
	/** Absolute 32-bit unaligned. */
	R_PPC_UADDR32(24),
	/** Absolute 16-bit unaligned. */
	R_PPC_UADDR16(25),
	/** Relative 32-bit. */
	R_PPC_REL32(26),
	/** Procedure Linkage Table 32-bit. */
	R_PPC_PLT32(27),
	/** Procedure Linkage Table relative 32-bit. */
	R_PPC_PLTREL32(28),
	/** Procedure Linkage Table 32-bit with bits 0 through 15. */
	R_PPC_PLT16_LO(29),
	/** Procedure Linkage Table 32-bit with bits 16 through 31. */
	R_PPC_PLT16_HI(30),
	/** Procedure Linkage Table 32-bit with bits 16 through 31 with adjustment for the carry. */
	R_PPC_PLT16_HA(31),
	/** Small Data Area relative 16-bit. */
	R_PPC_SDAREL16(32),
	/** Section offset. */
	R_PPC_SECTOFF(33),
	/** Section offset low. */
	R_PPC_SECTOFF_LO(34),
	/** Section offset high. */
	R_PPC_SECTOFF_HI(35),
	/** Section offset high adjusted. */
	R_PPC_SECTOFF_HA(36),
	/** Direct 30-bit. */
	R_PPC_ADDR30(37),
	;

	private final int value;

	ElfRelocationType_ppc(int value) {
		this.value = value;
	}

	public ElfMachine getMachine() {
		return ElfMachine.EM_PPC;
	}

	public int getValue() {
		return value;
	}
}
