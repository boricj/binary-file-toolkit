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
package net.boricj.bft.elf.machines.amd64;

import net.boricj.bft.elf.constants.ElfMachine;
import net.boricj.bft.elf.constants.ElfRelocationType;

public enum ElfRelocationType_amd64 implements ElfRelocationType {
	R_X86_64_NONE(0),
	R_X86_64_64(1),
	R_X86_64_PC32(2),
	R_X86_64_GOT32(3),
	R_X86_64_PLT32(4),
	R_X86_64_COPY(5),
	R_X86_64_GLOB_DAT(6),
	R_X86_64_JUMP_SLOT(7),
	R_X86_64_RELATIVE(8),
	R_X86_64_GOTPCREL(9),
	R_X86_64_32(10),
	R_X86_64_32S(11),
	R_X86_64_16(12),
	R_X86_64_PC16(13),
	R_X86_64_8(14),
	R_X86_64_PC8(15),
	R_X86_64_DTPMOD64(16),
	R_X86_64_DTPOFF64(17),
	R_X86_64_TPOFF64(18),
	R_X86_64_TLSGD(19),
	R_X86_64_TLSLD(20),
	R_X86_64_DTPOFF32(21),
	R_X86_64_GOTTPOFF(22),
	R_X86_64_TPOFF32(23),
	R_X86_64_PC64(24),
	R_X86_64_GOTOFF64(25),
	R_X86_64_GOTPC32(26),
	R_X86_64_SIZE32(32),
	R_X86_64_SIZE64(33),
	R_X86_64_GOTPC32_TLSDESC(34),
	R_X86_64_TLSDESC_CALL(35),
	R_X86_64_TLSDESC(36),
	R_X86_64_IRELATIVE(37),
	;

	private final int value;

	private ElfRelocationType_amd64(int value) {
		this.value = value;
	}

	public ElfMachine getMachine() {
		return ElfMachine.EM_X86_64;
	}

	public int getValue() {
		return value;
	}

	public static ElfRelocationType_amd64 valueFrom(int value) {
		for (ElfRelocationType_amd64 type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}

		throw new IllegalArgumentException();
	}
}
