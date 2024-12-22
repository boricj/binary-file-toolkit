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

public enum ElfSymbolType {
	STT_NOTYPE((byte) 0),
	STT_OBJECT((byte) 1),
	STT_FUNC((byte) 2),
	STT_SECTION((byte) 3),
	STT_FILE((byte) 4),
	STT_TLS((byte) 6),
	;

	private final byte value;

	private ElfSymbolType(byte value) {
		this.value = value;
	}

	public byte getValue() {
		return value;
	}

	public static ElfSymbolType valueFrom(byte value) {
		for (ElfSymbolType type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}

		throw new IllegalArgumentException();
	}
}
