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

/**
 * Intel 386 relocation types.
 * These constants define the relocation mechanisms specific to the i386 architecture.
 */
public enum ElfRelocationType_i386 implements ElfRelocationType {
	/** No relocation. */
	R_386_NONE(0),
	/** Direct 32-bit. */
	R_386_32(1),
	/** PC relative 32-bit. */
	R_386_PC32(2),
	/** 32-bit GOT entry. */
	R_386_GOT32(3),
	/** 32-bit PLT address. */
	R_386_PLT32(4),
	/** Copy symbol at runtime. */
	R_386_COPY(5),
	/** Create GOT entry. */
	R_386_GLOB_DAT(6),
	/** Create PLT entry. */
	R_386_JMP_SLOT(7),
	/** Adjust by program base. */
	R_386_RELATIVE(8),
	/** 32-bit offset to GOT. */
	R_386_GOTOFF(9),
	/** 32-bit PC relative offset to GOT. */
	R_386_GOTPC(10),
	/** 32-bit PLT address. */
	R_386_32PLT(11),
	/** Negative offset in static TLS block. */
	R_386_TLS_TPOFF(14),
	/** Absolute address of GOT entry for static TLS block offset. */
	R_386_TLS_IE(15),
	/** GOT entry for static TLS block offset. */
	R_386_TLS_GOTIE(16),
	/** Offset relative to static TLS block. */
	R_386_TLS_LE(17),
	/** Direct 32-bit for GNU version of general dynamic thread local data. */
	R_386_TLS_GD(18),
	/** Direct 32-bit for GNU version of local dynamic thread local data in LE code. */
	R_386_TLS_LDM(19),
	/** Direct 16-bit zero-extended. */
	R_386_16(20),
	/** 16-bit PC relative. */
	R_386_PC16(21),
	/** Direct 8-bit sign-extended. */
	R_386_8(22),
	/** 8-bit PC relative. */
	R_386_PC8(23),
	/** Direct 32-bit for general dynamic thread local data. */
	R_386_TLS_GD_32(24),
	/** Tag for pushl in GD TLS code. */
	R_386_TLS_GD_PUSH(25),
	/** Relocation for call to __tls_get_addr(). */
	R_386_TLS_GD_CALL(26),
	/** Tag for popl in GD TLS code. */
	R_386_TLS_GD_POP(27),
	/** Direct 32-bit for local dynamic thread local data in LE code. */
	R_386_TLS_LDM_32(28),
	/** Tag for pushl in LDM TLS code. */
	R_386_TLS_LDM_PUSH(29),
	/** Relocation for call to __tls_get_addr() in LDM code. */
	R_386_TLS_LDM_CALL(30),
	/** Tag for popl in LDM TLS code. */
	R_386_TLS_LDM_POP(31),
	/** Offset relative to TLS block. */
	R_386_TLS_LDO_32(32),
	/** GOT entry for negated static TLS block offset. */
	R_386_TLS_IE_32(33),
	/** Negated offset relative to static TLS block. */
	R_386_TLS_LE_32(34),
	/** ID of module containing symbol. */
	R_386_TLS_DTPMOD32(35),
	/** Offset in TLS block. */
	R_386_TLS_DTPOFF32(36),
	/** Negated offset in static TLS block. */
	R_386_TLS_TPOFF32(37),
	/** 32-bit symbol size. */
	R_386_SIZE32(38),
	/** GOT offset for TLS descriptor. */
	R_386_TLS_GOTDESC(39),
	/** Marker of call through TLS descriptor. */
	R_386_TLS_DESC_CALL(40),
	/** TLS descriptor containing pointer and index. */
	R_386_TLS_DESC(41),
	/** Adjust indirectly by program base. */
	R_386_IRELATIVE(42),
	/** Load from 32-bit GOT entry, relaxable. */
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

	/**
	 * Resolves a relocation type from its numeric value.
	 *
	 * @param value the numeric relocation type
	 * @return the corresponding relocation type
	 * @throws IllegalArgumentException if the value is not recognized
	 */
	public static ElfRelocationType_i386 valueFrom(int value) {
		for (ElfRelocationType_i386 type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}

		throw new IllegalArgumentException();
	}
}
