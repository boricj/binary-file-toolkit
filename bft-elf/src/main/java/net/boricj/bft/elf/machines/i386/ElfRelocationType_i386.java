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
package net.boricj.bft.elf.machines.i386;

import net.boricj.bft.elf.constants.ElfMachine;
import net.boricj.bft.elf.constants.ElfRelocationType;

public enum ElfRelocationType_i386 implements ElfRelocationType {
	R_386_NONE(0),
	R_386_32(1),
	R_386_PC32(2),
	R_386_GOT32(3),
	R_386_PLT32(4),
	R_386_COPY(5),
	R_386_GLOB_DAT(6),
	R_386_JMP_SLOT(7),
	R_386_RELATIVE(8),
	R_386_GOTOFF(9),
	R_386_GOTPC(10),
	R_386_32PLT(11),
	R_386_TLS_TPOFF(14),
	R_386_TLS_IE(15),
	R_386_TLS_GOTIE(16),
	R_386_TLS_LE(17),
	R_386_TLS_GD(18),
	R_386_TLS_LDM(19),
	R_386_16(20),
	R_386_PC16(21),
	R_386_8(22),
	R_386_PC8(23),
	R_386_TLS_GD_32(24),
	R_386_TLS_GD_PUSH(25),
	R_386_TLS_GD_CALL(26),
	R_386_TLS_GD_POP(27),
	R_386_TLS_LDM_32(28),
	R_386_TLS_LDM_PUSH(29),
	R_386_TLS_LDM_CALL(30),
	R_386_TLS_LDM_POP(31),
	R_386_TLS_LDO_32(32),
	R_386_TLS_IE_32(33),
	R_386_TLS_LE_32(34),
	R_386_TLS_DTPMOD32(35),
	R_386_TLS_DTPOFF32(36),
	R_386_TLS_TPOFF32(37),
	R_386_SIZE32(38),
	R_386_TLS_GOTDESC(39),
	R_386_TLS_DESC_CALL(40),
	R_386_TLS_DESC(41),
	R_386_IRELATIVE(42),
	R_386_GOT32X(43),
	;

	private final int value;

	private ElfRelocationType_i386(int value) {
		this.value = value;
	}

	public ElfMachine getMachine() {
		return ElfMachine.EM_386;
	}

	public int getValue() {
		return value;
	}

	public static ElfRelocationType_i386 valueFrom(int value) {
		for (ElfRelocationType_i386 type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}

		throw new IllegalArgumentException();
	}
}
