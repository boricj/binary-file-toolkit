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
package net.boricj.bft.dwarf.constants;

/**
 * DWARF v5 unit types.
 */
public enum DwarfUnitType {
	/** Standard compilation unit. */
	DW_UT_compile(0x01),
	;

	private final int value;

	DwarfUnitType(int value) {
		this.value = value;
	}

	/**
	 * Returns the numeric unit type code.
	 *
	 * @return unit type value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Returns the matching known unit type or null when not modeled yet.
	 *
	 * @param value numeric unit type code
	 * @return matching unit type or null
	 */
	public static DwarfUnitType valueFromOrNull(int value) {
		for (DwarfUnitType type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}
		return null;
	}
}
