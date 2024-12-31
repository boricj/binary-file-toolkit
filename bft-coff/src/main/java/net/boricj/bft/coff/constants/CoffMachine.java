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

import net.boricj.bft.coff.machines.i386.CoffRelocationType_i386;

public enum CoffMachine {
	IMAGE_FILE_MACHINE_UNKNOWN((short) 0x0, null),
	;

	private final short value;
	private final Class<? extends CoffRelocationType> relocationType;

	CoffMachine(short value, Class<? extends CoffRelocationType> relocationType) {
		this.value = value;
		this.relocationType = relocationType;
	}

	public short getValue() {
		return value;
	}

	public Class<? extends CoffRelocationType> getRelocationTypeClass() {
		return relocationType;
	}

	public static CoffMachine valueFrom(short value) {
		for (CoffMachine machine : values()) {
			if (machine.getValue() == value) {
				return machine;
			}
		}

		throw new IllegalArgumentException();
	}
}
