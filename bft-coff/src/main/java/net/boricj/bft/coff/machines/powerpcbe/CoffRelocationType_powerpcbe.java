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
package net.boricj.bft.coff.machines.powerpcbe;

import net.boricj.bft.coff.constants.CoffMachine;
import net.boricj.bft.coff.constants.CoffRelocationType;

/**
 * POWERPCBE COFF relocation types.
 */
public enum CoffRelocationType_powerpcbe implements CoffRelocationType {
	/** No relocation required. */
	IMAGE_REL_PPC_ABSOLUTE((short) 0x0000),
	/** The target's 64-bit virtual address. */
	IMAGE_REL_PPC_ADDR64((short) 0x0001),
	/** The target's 32-bit virtual address. */
	IMAGE_REL_PPC_ADDR32((short) 0x0002),
	/** The low 24 bits of the target's virtual address. */
	IMAGE_REL_PPC_ADDR24((short) 0x0003),
	/** The low 16 bits of the target's virtual address. */
	IMAGE_REL_PPC_ADDR16((short) 0x0004),
	/** The low 14 bits of the target's virtual address. */
	IMAGE_REL_PPC_ADDR14((short) 0x0005),
	/** A 24-bit PC-relative offset to the target. */
	IMAGE_REL_PPC_REL24((short) 0x0006),
	/** A 14-bit PC-relative offset to the target. */
	IMAGE_REL_PPC_REL14((short) 0x0007),
	/** TOC-relative 16-bit offset. */
	IMAGE_REL_PPC_TOCREL16((short) 0x0008),
	/** TOC-relative 14-bit offset. */
	IMAGE_REL_PPC_TOCREL14((short) 0x0009),
	/** The target's 32-bit RVA. */
	IMAGE_REL_PPC_ADDR32NB((short) 0x000A),
	/** The 32-bit offset of the target from the section start. */
	IMAGE_REL_PPC_SECREL((short) 0x000B),
	/** The 16-bit section index that contains the target. */
	IMAGE_REL_PPC_SECTION((short) 0x000C),
	/** Not used in modern toolchains. */
	IMAGE_REL_PPC_IFGLUE((short) 0x000D),
	/** Not used in modern toolchains. */
	IMAGE_REL_PPC_IMGLUE((short) 0x000E),
	/** The 16-bit section-relative offset of the target. */
	IMAGE_REL_PPC_SECREL16((short) 0x000F),
	/** High 16 bits of the target's 32-bit address. */
	IMAGE_REL_PPC_REFHI((short) 0x0010),
	/** Low 16 bits of the target's address. */
	IMAGE_REL_PPC_REFLO((short) 0x0011),
	/** Pair relocation that follows REFHI or SECRELHI. */
	IMAGE_REL_PPC_PAIR((short) 0x0012),
	/** Low 16 bits of the section-relative target offset. */
	IMAGE_REL_PPC_SECRELLO((short) 0x0013),
	/** High 16 bits of the section-relative target offset. */
	IMAGE_REL_PPC_SECRELHI((short) 0x0014),
	/** 16-bit signed displacement relative to GP register. */
	IMAGE_REL_PPC_GPREL((short) 0x0015),
	/** CLR token relocation. */
	IMAGE_REL_PPC_TOKEN((short) 0x0016),
	;

	private final short value;

	CoffRelocationType_powerpcbe(short value) {
		this.value = value;
	}

	/**
	 * Gets the COFF machine this relocation type applies to.
	 *
	 * @return the POWERPCBE COFF machine value
	 */
	@Override
	public CoffMachine getMachine() {
		return CoffMachine.IMAGE_FILE_MACHINE_POWERPCBE;
	}

	/**
	 * Gets the numeric relocation type value.
	 *
	 * @return the relocation type value
	 */
	@Override
	public short getValue() {
		return value;
	}

	/**
	 * Resolves a relocation type from its encoded value.
	 *
	 * @param value encoded relocation type value
	 * @return matching relocation type
	 */
	public static CoffRelocationType_powerpcbe valueFrom(short value) {
		for (CoffRelocationType_powerpcbe type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}

		throw new IllegalArgumentException();
	}
}
