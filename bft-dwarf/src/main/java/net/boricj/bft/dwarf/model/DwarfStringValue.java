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
package net.boricj.bft.dwarf.model;

import java.util.Objects;

/**
 * Inline string attribute value.
 *
 * @param value decoded string value
 */
public record DwarfStringValue(String value) implements DwarfValue {
	/**
	 * Creates an inline DWARF string value.
	 *
	 * @param value decoded string contents
	 */
	public DwarfStringValue {
		Objects.requireNonNull(value);
	}
}
