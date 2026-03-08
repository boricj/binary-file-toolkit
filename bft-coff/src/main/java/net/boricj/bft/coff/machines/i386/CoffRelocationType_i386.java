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
package net.boricj.bft.coff.machines.i386;

import net.boricj.bft.coff.constants.CoffMachine;
import net.boricj.bft.coff.constants.CoffRelocationType;

/**
 * i386 COFF relocation types.
 */
public enum CoffRelocationType_i386 implements CoffRelocationType {
	/** No relocation required. */
	IMAGE_REL_I386_ABSOLUTE((short) 0x0000),
	/** Direct 16-bit relocation. */
	IMAGE_REL_I386_DIR16((short) 0x0001),
	/** Relative 16-bit relocation. */
	IMAGE_REL_I386_REL16((short) 0x0002),
	/** Direct 32-bit relocation. */
	IMAGE_REL_I386_DIR32((short) 0x0006),
	/** Direct 32-bit relocation without base. */
	IMAGE_REL_I386_DIR32NB((short) 0x0007),
	/** 16-bit section index relocation. */
	IMAGE_REL_I386_SEG12((short) 0x0009),
	/** Section index relocation. */
	IMAGE_REL_I386_SECTION((short) 0x000A),
	/** 32-bit offset from section start. */
	IMAGE_REL_I386_SECREL((short) 0x000B),
	/** CLR token relocation. */
	IMAGE_REL_I386_TOKEN((short) 0x000C),
	/** 7-bit offset from section start. */
	IMAGE_REL_I386_SECREL7((short) 0x000D),
	/** 32-bit relative relocation. */
	IMAGE_REL_I386_REL32((short) 0x0014),
	;

	private final short value;

	CoffRelocationType_i386(short value) {
		this.value = value;
	}

	/**
	 * Gets the COFF machine this relocation type applies to.
	 *
	 * @return the i386 COFF machine value
	 */
	@Override
	public CoffMachine getMachine() {
		return CoffMachine.IMAGE_FILE_MACHINE_I386;
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
	public static CoffRelocationType_i386 valueFrom(short value) {
		for (CoffRelocationType_i386 type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}

		throw new IllegalArgumentException();
	}
}
