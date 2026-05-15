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
package net.boricj.bft.omf.records;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * EXTDEF subrecord entry with external name and type index.
 *
 * @param name external symbol name
 * @param typeIndex type index
 */
public record OmfSubrecordExtdef(String name, int typeIndex) {
	/**
	 * Validates the EXTDEF subrecord components.
	 */
	public OmfSubrecordExtdef {
		Objects.requireNonNull(name);

		byte[] encodedName = name.getBytes(StandardCharsets.US_ASCII);
		if (encodedName.length > 0xFF) {
			throw new IllegalArgumentException("EXTDEF symbol name too long: " + name);
		}

		if (typeIndex < 0) {
			throw new IllegalArgumentException("EXTDEF type index must be non-negative: " + typeIndex);
		}
	}
}
