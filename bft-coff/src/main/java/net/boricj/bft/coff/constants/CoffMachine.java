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
package net.boricj.bft.coff.constants;

import net.boricj.bft.coff.machines.amd64.CoffRelocationType_amd64;
import net.boricj.bft.coff.machines.i386.CoffRelocationType_i386;

/**
 * COFF machine identifiers.
 */
public enum CoffMachine {
	/** Unknown machine type. */
	IMAGE_FILE_MACHINE_UNKNOWN((short) 0x0, null),
	/** Intel i386 machine type. */
	IMAGE_FILE_MACHINE_I386((short) 0x14c, CoffRelocationType_i386.class),
	/** AMD64 machine type. */
	IMAGE_FILE_MACHINE_AMD64((short) 0x8664, CoffRelocationType_amd64.class),
	;

	private final short value;
	private final Class<? extends CoffRelocationType> relocationType;

	CoffMachine(short value, Class<? extends CoffRelocationType> relocationType) {
		this.value = value;
		this.relocationType = relocationType;
	}

	/**
	 * Gets the encoded COFF machine value.
	 *
	 * @return machine value
	 */
	public short getValue() {
		return value;
	}

	/**
	 * Gets the relocation type class associated with this machine.
	 *
	 * @return relocation type class, or {@code null} if unknown
	 */
	public Class<? extends CoffRelocationType> getRelocationTypeClass() {
		return relocationType;
	}

	/**
	 * Resolves a machine identifier from its encoded value.
	 *
	 * @param value encoded machine value
	 * @return matching machine identifier
	 */
	public static CoffMachine valueFrom(short value) {
		for (CoffMachine machine : values()) {
			if (machine.getValue() == value) {
				return machine;
			}
		}

		throw new IllegalArgumentException();
	}
}
