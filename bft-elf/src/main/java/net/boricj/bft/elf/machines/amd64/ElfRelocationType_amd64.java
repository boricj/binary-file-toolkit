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

/**
 * AMD64 (x86-64) relocation types.
 * These constants define the relocation mechanisms specific to the x86-64 architecture.
 */
public enum ElfRelocationType_amd64 implements ElfRelocationType {
	/** No relocation. */
	R_X86_64_NONE(0),
	/** Direct 64-bit. */
	R_X86_64_64(1),
	/** PC relative 32-bit signed. */
	R_X86_64_PC32(2),
	/** 32-bit GOT entry. */
	R_X86_64_GOT32(3),
	/** 32-bit PLT address. */
	R_X86_64_PLT32(4),
	/** Copy symbol at runtime. */
	R_X86_64_COPY(5),
	/** Create GOT entry. */
	R_X86_64_GLOB_DAT(6),
	/** Create PLT entry. */
	R_X86_64_JUMP_SLOT(7),
	/** Adjust by program base. */
	R_X86_64_RELATIVE(8),
	/** 32-bit signed PC relative offset to GOT. */
	R_X86_64_GOTPCREL(9),
	/** Direct 32-bit zero-extended. */
	R_X86_64_32(10),
	/** Direct 32-bit sign-extended. */
	R_X86_64_32S(11),
	/** Direct 16-bit zero-extended. */
	R_X86_64_16(12),
	/** 16-bit sign-extended PC relative. */
	R_X86_64_PC16(13),
	/** Direct 8-bit sign-extended. */
	R_X86_64_8(14),
	/** 8-bit sign-extended PC relative. */
	R_X86_64_PC8(15),
	/** ID of module containing symbol. */
	R_X86_64_DTPMOD64(16),
	/** Offset in module's TLS block. */
	R_X86_64_DTPOFF64(17),
	/** Offset in initial TLS block. */
	R_X86_64_TPOFF64(18),
	/** 32-bit signed PC relative offset to two GOT entries for GD symbol. */
	R_X86_64_TLSGD(19),
	/** 32-bit signed PC relative offset to two GOT entries for LD symbol. */
	R_X86_64_TLSLD(20),
	/** Offset in TLS block. */
	R_X86_64_DTPOFF32(21),
	/** 32-bit signed PC relative offset to GOT entry for IE symbol. */
	R_X86_64_GOTTPOFF(22),
	/** Offset in initial TLS block. */
	R_X86_64_TPOFF32(23),
	/** PC relative 64-bit. */
	R_X86_64_PC64(24),
	/** 64-bit offset to GOT. */
	R_X86_64_GOTOFF64(25),
	/** 32-bit signed PC relative offset to GOT. */
	R_X86_64_GOTPC32(26),
	/** Size of symbol plus 32-bit addend. */
	R_X86_64_SIZE32(32),
	/** Size of symbol plus 64-bit addend. */
	R_X86_64_SIZE64(33),
	/** 32-bit signed PC relative offset to TLS descriptor. */
	R_X86_64_GOTPC32_TLSDESC(34),
	/** TLS descriptor call. */
	R_X86_64_TLSDESC_CALL(35),
	/** TLS descriptor. */
	R_X86_64_TLSDESC(36),
	/** Adjust indirectly by program base. */
	R_X86_64_IRELATIVE(37),
	/** 64-bit adjust by program base. */
	R_X86_64_RELATIVE64(38),
	/** PC relative 32-bit signed with BND prefix (deprecated). */
	R_X86_64_PC32_BND(39), // Deprecated.
	/** 32-bit PLT address with BND prefix (deprecated). */
	R_X86_64_PLT32_BND(40), // Deprecated.
	/** Load from 32-bit signed PC relative offset to GOT entry without REX prefix. */
	R_X86_64_GOTPCRELX(41),
	/** Load from 32-bit signed PC relative offset to GOT entry with REX prefix. */
	R_X86_64_REX_GOTPCRELX(42),
	/** GOTPCRELX relaxation with 4-byte displacement. */
	R_X86_64_CODE_4_GOTPCRELX(43),
	/** GOTTPOFF relaxation with 4-byte displacement. */
	R_X86_64_CODE_4_GOTTPOFF(44),
	/** GOTPC32_TLSDESC relaxation with 4-byte displacement. */
	R_X86_64_CODE_4_GOTPC32_TLSDESC(45),
	/** GOTPCRELX relaxation with 5-byte displacement. */
	R_X86_64_CODE_5_GOTPCRELX(46),
	/** GOTTPOFF relaxation with 5-byte displacement. */
	R_X86_64_CODE_5_GOTTPOFF(47),
	/** GOTPC32_TLSDESC relaxation with 5-byte displacement. */
	R_X86_64_CODE_5_GOTPC32_TLSDESC(48),
	/** GOTPCRELX relaxation with 6-byte displacement. */
	R_X86_64_CODE_6_GOTPCRELX(49),
	/** GOTTPOFF relaxation with 6-byte displacement. */
	R_X86_64_CODE_6_GOTTPOFF(50),
	/** GOTPC32_TLSDESC relaxation with 6-byte displacement. */
	R_X86_64_CODE_6_GOTPC32_TLSDESC(51),
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

	/**
	 * Resolves a relocation type from its numeric value.
	 *
	 * @param value the numeric relocation type
	 * @return the corresponding relocation type
	 * @throws IllegalArgumentException if the value is not recognized
	 */
	public static ElfRelocationType_amd64 valueFrom(int value) {
		for (ElfRelocationType_amd64 type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}

		throw new IllegalArgumentException();
	}
}
