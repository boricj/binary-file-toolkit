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
 * PPC64 ELF relocation types.
 * These constants define the relocation mechanisms specific to the PowerPC 64-bit architecture.
 */
public enum ElfRelocationType_ppc64 implements ElfRelocationType {
	/** No relocation. */
	R_PPC64_NONE(0),
	/** Direct 32-bit. */
	R_PPC64_ADDR32(1),
	/** Direct 24-bit. */
	R_PPC64_ADDR24(2),
	/** Direct 16-bit. */
	R_PPC64_ADDR16(3),
	/** Direct 32-bit with the low 16 bits of the addend. */
	R_PPC64_ADDR16_LO(4),
	/** Direct 32-bit with the high 16 bits of the addend. */
	R_PPC64_ADDR16_HI(5),
	/** Direct 32-bit with the high 16 bits of the addend with adjustment for the carry. */
	R_PPC64_ADDR16_HA(6),
	/** Direct 14-bit. */
	R_PPC64_ADDR14(7),
	/** Direct 14-bit with branch taken. */
	R_PPC64_ADDR14_BRTAKEN(8),
	/** Direct 14-bit with branch not taken. */
	R_PPC64_ADDR14_BRNTAKEN(9),
	/** Relative 24-bit. */
	R_PPC64_REL24(10),
	/** Relative 14-bit. */
	R_PPC64_REL14(11),
	/** Relative 14-bit with branch taken. */
	R_PPC64_REL14_BRTAKEN(12),
	/** Relative 14-bit with branch not taken. */
	R_PPC64_REL14_BRNTAKEN(13),
	/** Global Offset Table 16-bit. */
	R_PPC64_GOT16(14),
	/** Global Offset Table 32-bit with bits 0 through 15. */
	R_PPC64_GOT16_LO(15),
	/** Global Offset Table 32-bit with bits 16 through 31. */
	R_PPC64_GOT16_HI(16),
	/** Global Offset Table 32-bit with bits 16 through 31 with adjustment for the carry. */
	R_PPC64_GOT16_HA(17),
	/** Dynamic linking stuff. */
	R_PPC64_COPY(19),
	/** Global data. */
	R_PPC64_GLOB_DAT(20),
	/** Dynamic linking stuff. */
	R_PPC64_JMP_SLOT(21),
	/** Dynamic linking stuff. */
	R_PPC64_RELATIVE(22),
	/** Absolute 32-bit unaligned. */
	R_PPC64_UADDR32(24),
	/** Absolute 16-bit unaligned. */
	R_PPC64_UADDR16(25),
	/** Relative 32-bit. */
	R_PPC64_REL32(26),
	/** Procedure Linkage Table 32-bit. */
	R_PPC64_PLT32(27),
	/** Procedure Linkage Table relative 32-bit. */
	R_PPC64_PLTREL32(28),
	/** Procedure Linkage Table 16-bit with bits 0 through 15. */
	R_PPC64_PLT16_LO(29),
	/** Procedure Linkage Table 16-bit with bits 16 through 31. */
	R_PPC64_PLT16_HI(30),
	/** Procedure Linkage Table 16-bit with bits 16 through 31 with adjustment for the carry. */
	R_PPC64_PLT16_HA(31),
	/** Section offset. */
	R_PPC64_SECTOFF(33),
	/** Section offset low. */
	R_PPC64_SECTOFF_LO(34),
	/** Section offset high. */
	R_PPC64_SECTOFF_HI(35),
	/** Section offset high adjusted. */
	R_PPC64_SECTOFF_HA(36),
	/** Direct 30-bit. */
	R_PPC64_ADDR30(37),
	/** Direct 64-bit. */
	R_PPC64_ADDR64(38),
	/** Direct 64-bit with bits 32 through 47. */
	R_PPC64_ADDR16_HIGHER(39),
	/** Direct 64-bit with bits 48 through 63 with adjustment for the carry. */
	R_PPC64_ADDR16_HIGHERA(40),
	/** Direct 64-bit with bits 48 through 63. */
	R_PPC64_ADDR16_HIGHEST(41),
	/** Direct 64-bit with bits 48 through 63 with adjustment for the carry. */
	R_PPC64_ADDR16_HIGHESTA(42),
	/** Absolute 64-bit unaligned. */
	R_PPC64_UADDR64(43),
	/** Relative 64-bit. */
	R_PPC64_REL64(44),
	/** Procedure Linkage Table 64-bit. */
	R_PPC64_PLT64(45),
	/** Procedure Linkage Table relative 64-bit. */
	R_PPC64_PLTREL64(46),
	/** Table of Contents 16-bit. */
	R_PPC64_TOC16(47),
	/** Table of Contents 32-bit with bits 0 through 15. */
	R_PPC64_TOC16_LO(48),
	/** Table of Contents 32-bit with bits 16 through 31. */
	R_PPC64_TOC16_HI(49),
	/** Table of Contents 32-bit with bits 16 through 31 with adjustment for the carry. */
	R_PPC64_TOC16_HA(50),
	/** Table of Contents. */
	R_PPC64_TOC(51),
	/** Procedure Linkage Table Global Offset Table 16-bit. */
	R_PPC64_PLTGOT16(52),
	/** Procedure Linkage Table Global Offset Table 32-bit with bits 0 through 15. */
	R_PPC64_PLTGOT16_LO(53),
	/** Procedure Linkage Table Global Offset Table 32-bit with bits 16 through 31. */
	R_PPC64_PLTGOT16_HI(54),
	/** Procedure Linkage Table Global Offset Table 32-bit with bits 16 through 31 with adjustment for the carry. */
	R_PPC64_PLTGOT16_HA(55),
	;

	private final int value;

	ElfRelocationType_ppc64(int value) {
		this.value = value;
	}

	public ElfMachine getMachine() {
		return ElfMachine.EM_PPC64;
	}

	public int getValue() {
		return value;
	}
}
