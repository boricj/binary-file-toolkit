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

public enum CoffRelocationType_amd64 implements CoffRelocationType {
	IMAGE_REL_AMD64_ABSOLUTE((short) 0x0000),
	IMAGE_REL_AMD64_ADDR64((short) 0x0001),
	IMAGE_REL_AMD64_ADDR32((short) 0x0002),
	IMAGE_REL_AMD64_ADDR32NB((short) 0x0003),
	IMAGE_REL_AMD64_REL32((short) 0x0004),
	IMAGE_REL_AMD64_REL32_1((short) 0x0005),
	IMAGE_REL_AMD64_REL32_2((short) 0x0006),
	IMAGE_REL_AMD64_REL32_3((short) 0x0007),
	IMAGE_REL_AMD64_REL32_4((short) 0x0008),
	IMAGE_REL_AMD64_REL32_5((short) 0x0009),
	IMAGE_REL_AMD64_SECTION((short) 0x000A),
	IMAGE_REL_AMD64_SECREL((short) 0x000B),
	IMAGE_REL_AMD64_SECREL7((short) 0x000C),
	IMAGE_REL_AMD64_TOKEN((short) 0x000D),
	IMAGE_REL_AMD64_SREL32((short) 0x000E),
	IMAGE_REL_AMD64_PAIR((short) 0x000F),
	IMAGE_REL_AMD64_SSPAN32((short) 0x0010),
	;

	private final short value;

	CoffRelocationType_amd64(short value) {
		this.value = value;
	}

	@Override
	public CoffMachine getMachine() {
		return CoffMachine.IMAGE_FILE_MACHINE_AMD64;
	}

	@Override
	public short getValue() {
		return value;
	}

	public static CoffRelocationType_amd64 valueFrom(short value) {
		for (CoffRelocationType_amd64 type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}

		throw new IllegalArgumentException();
	}
}
