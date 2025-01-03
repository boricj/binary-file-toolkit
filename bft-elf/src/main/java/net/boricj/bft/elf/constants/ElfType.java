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
package net.boricj.bft.elf.constants;

public enum ElfType {
	ET_NONE((short) 0),
	ET_REL((short) 1),
	ET_EXEC((short) 2),
	ET_DYN((short) 3),
	ET_CORE((short) 4),
	;

	private final short value;

	ElfType(short value) {
		this.value = value;
	}

	public short getValue() {
		return value;
	}

	public static ElfType valueFrom(short value) {
		for (ElfType type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}

		throw new IllegalArgumentException();
	}
}
