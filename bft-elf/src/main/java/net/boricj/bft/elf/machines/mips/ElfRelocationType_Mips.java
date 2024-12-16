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

public enum ElfRelocationType_Mips implements ElfRelocationType {
	R_MIPS_NONE(0),
	R_MIPS_16(1),
	R_MIPS_32(2),
	R_MIPS_REL32(3),
	R_MIPS_26(4),
	R_MIPS_HI16(5),
	R_MIPS_LO16(6),
	R_MIPS_GPREL16(7),
	R_MIPS_LITERAL(8),
	R_MIPS_GOT16(9),
	R_MIPS_PC16(10),
	R_MIPS_CALL16(11),
	R_MIPS_GPREL32(12),
	R_MIPS_UNUSED1(13),
	R_MIPS_UNUSED2(14),
	R_MIPS_UNUSED3(15),
	R_MIPS_SHIFT5(16),
	R_MIPS_SHIFT6(17),
	R_MIPS_64(18),
	R_MIPS_GOT_DISP(19),
	R_MIPS_GOT_PAGE(20),
	R_MIPS_GOT_OFST(21),
	R_MIPS_GOT_HI16(22),
	R_MIPS_GOT_LO16(23),
	R_MIPS_SUB(24),
	R_MIPS_INSERT_A(25),
	R_MIPS_INSERT_B(26),
	R_MIPS_DELETE(27),
	R_MIPS_HIGHER(28),
	R_MIPS_HIGHEST(29),
	R_MIPS_CALL_HI16(30),
	R_MIPS_CALL_LO16(31),
	R_MIPS_SCN_DISP(32),
	R_MIPS_REL16(33),
	R_MIPS_ADD_IMMEDIATE(34),
	R_MIPS_PJUMP(35),
	R_MIPS_RELGOT(36),
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

	public static ElfRelocationType_Mips valueFrom(int value) {
		for (ElfRelocationType_Mips type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}

		throw new IllegalArgumentException();
	}
}
