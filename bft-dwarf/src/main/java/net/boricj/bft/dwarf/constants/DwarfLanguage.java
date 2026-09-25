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
 * Known DWARF language codes used by the current fixtures.
 */
public enum DwarfLanguage {
	/** C99 language dialect. */
	DW_LANG_C99(0x000c),
	/** C11 language dialect. */
	DW_LANG_C11(0x001d),
	;

	private final int value;

	DwarfLanguage(int value) {
		this.value = value;
	}

	/**
	 * Returns the numeric language code.
	 *
	 * @return language code
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Returns the matching known language or null when not modeled yet.
	 *
	 * @param value numeric language code
	 * @return matching language or null
	 */
	public static DwarfLanguage valueFromOrNull(long value) {
		for (DwarfLanguage language : values()) {
			if (language.getValue() == value) {
				return language;
			}
		}
		return null;
	}
}
