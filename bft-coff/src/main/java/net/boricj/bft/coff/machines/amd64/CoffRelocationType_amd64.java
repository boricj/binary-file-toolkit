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
package net.boricj.bft.coff.machines.amd64;

import net.boricj.bft.coff.constants.CoffMachine;
import net.boricj.bft.coff.constants.CoffRelocationType;

/**
 * AMD64 COFF relocation types.
 */
public enum CoffRelocationType_amd64 implements CoffRelocationType {
	/** No relocation required. */
	IMAGE_REL_AMD64_ABSOLUTE((short) 0x0000),
	/** The target's 64-bit virtual address. */
	IMAGE_REL_AMD64_ADDR64((short) 0x0001),
	/** The target's 32-bit virtual address. */
	IMAGE_REL_AMD64_ADDR32((short) 0x0002),
	/** The target's 32-bit RVA. */
	IMAGE_REL_AMD64_ADDR32NB((short) 0x0003),
	/** The 32-bit relative address from the byte following the relocation. */
	IMAGE_REL_AMD64_REL32((short) 0x0004),
	/** REL32 with a displacement of 1 byte. */
	IMAGE_REL_AMD64_REL32_1((short) 0x0005),
	/** REL32 with a displacement of 2 bytes. */
	IMAGE_REL_AMD64_REL32_2((short) 0x0006),
	/** REL32 with a displacement of 3 bytes. */
	IMAGE_REL_AMD64_REL32_3((short) 0x0007),
	/** REL32 with a displacement of 4 bytes. */
	IMAGE_REL_AMD64_REL32_4((short) 0x0008),
	/** REL32 with a displacement of 5 bytes. */
	IMAGE_REL_AMD64_REL32_5((short) 0x0009),
	/** Section index relocation. */
	IMAGE_REL_AMD64_SECTION((short) 0x000A),
	/** 32-bit offset from the start of section. */
	IMAGE_REL_AMD64_SECREL((short) 0x000B),
	/** 7-bit offset from the start of section. */
	IMAGE_REL_AMD64_SECREL7((short) 0x000C),
	/** CLR token relocation. */
	IMAGE_REL_AMD64_TOKEN((short) 0x000D),
	/** 32-bit signed span-dependent relocation. */
	IMAGE_REL_AMD64_SREL32((short) 0x000E),
	/** Pair relocation entry. */
	IMAGE_REL_AMD64_PAIR((short) 0x000F),
	/** 32-bit span-dependent relocation. */
	IMAGE_REL_AMD64_SSPAN32((short) 0x0010),
	;

	private final short value;

	CoffRelocationType_amd64(short value) {
		this.value = value;
	}

	/**
	 * Gets the COFF machine this relocation type applies to.
	 *
	 * @return the AMD64 COFF machine value
	 */
	@Override
	public CoffMachine getMachine() {
		return CoffMachine.IMAGE_FILE_MACHINE_AMD64;
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
	public static CoffRelocationType_amd64 valueFrom(short value) {
		for (CoffRelocationType_amd64 type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}

		throw new IllegalArgumentException();
	}
}
