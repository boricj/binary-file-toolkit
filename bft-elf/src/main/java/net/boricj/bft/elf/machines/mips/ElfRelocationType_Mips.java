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
package net.boricj.bft.elf.machines.mips;

import net.boricj.bft.elf.constants.ElfMachine;
import net.boricj.bft.elf.constants.ElfRelocationType;

/**
 * MIPS relocation types.
 * These constants define the relocation mechanisms specific to the MIPS architecture.
 */
public enum ElfRelocationType_Mips implements ElfRelocationType {
	/** No relocation. */
	R_MIPS_NONE(0),
	/** Direct 16-bit. */
	R_MIPS_16(1),
	/** Direct 32-bit. */
	R_MIPS_32(2),
	/** PC relative 32-bit. */
	R_MIPS_REL32(3),
	/** Direct 26-bit shifted. */
	R_MIPS_26(4),
	/** High 16 bits of symbol value. */
	R_MIPS_HI16(5),
	/** Low 16 bits of symbol value. */
	R_MIPS_LO16(6),
	/** GP relative 16-bit. */
	R_MIPS_GPREL16(7),
	/** 16-bit literal entry. */
	R_MIPS_LITERAL(8),
	/** 16-bit GOT entry. */
	R_MIPS_GOT16(9),
	/** PC relative 16-bit shifted. */
	R_MIPS_PC16(10),
	/** 16-bit GOT entry for function. */
	R_MIPS_CALL16(11),
	/** GP relative 32-bit. */
	R_MIPS_GPREL32(12),
	/** Unused relocation type 1. */
	R_MIPS_UNUSED1(13),
	/** Unused relocation type 2. */
	R_MIPS_UNUSED2(14),
	/** Unused relocation type 3. */
	R_MIPS_UNUSED3(15),
	/** Direct 5-bit shift. */
	R_MIPS_SHIFT5(16),
	/** Direct 6-bit shift. */
	R_MIPS_SHIFT6(17),
	/** Direct 64-bit. */
	R_MIPS_64(18),
	/** GOT displacement. */
	R_MIPS_GOT_DISP(19),
	/** Page number of GOT page. */
	R_MIPS_GOT_PAGE(20),
	/** Offset within GOT page. */
	R_MIPS_GOT_OFST(21),
	/** High 16 bits of GOT entry. */
	R_MIPS_GOT_HI16(22),
	/** Low 16 bits of GOT entry. */
	R_MIPS_GOT_LO16(23),
	/** Subtraction. */
	R_MIPS_SUB(24),
	/** Insert "A" field instruction. */
	R_MIPS_INSERT_A(25),
	/** Insert "B" field instruction. */
	R_MIPS_INSERT_B(26),
	/** Delete field instruction. */
	R_MIPS_DELETE(27),
	/** Higher 16 bits of 48-bit value. */
	R_MIPS_HIGHER(28),
	/** Highest 16 bits of 48-bit value. */
	R_MIPS_HIGHEST(29),
	/** High 16 bits of call displacement. */
	R_MIPS_CALL_HI16(30),
	/** Low 16 bits of call displacement. */
	R_MIPS_CALL_LO16(31),
	/** Section displacement. */
	R_MIPS_SCN_DISP(32),
	/** PC relative 16-bit. */
	R_MIPS_REL16(33),
	/** Add immediate instruction relocation. */
	R_MIPS_ADD_IMMEDIATE(34),
	/** Jump relocation. */
	R_MIPS_PJUMP(35),
	/** Relative GOT relocation. */
	R_MIPS_RELGOT(36),
	/** Jump and link register optimization. */
	R_MIPS_JALR(37),
	;

	private final int value;

	private ElfRelocationType_Mips(int value) {
		this.value = value;
	}

	public ElfMachine getMachine() {
		return ElfMachine.EM_MIPS;
	}

	public int getValue() {
		return value;
	}

	/**
	 * Resolves a relocation type from its numeric value.
	 *
	 * @param value the numeric relocation type
	 * @return the corresponding relocation type
	 * @throws IllegalArgumentException if the value is not recognized
	 */
	public static ElfRelocationType_Mips valueFrom(int value) {
		for (ElfRelocationType_Mips type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}

		throw new IllegalArgumentException();
	}
}
