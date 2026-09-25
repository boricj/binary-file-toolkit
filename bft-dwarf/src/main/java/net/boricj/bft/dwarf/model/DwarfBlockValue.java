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

import java.util.Arrays;

/**
 * Raw block or expression payload.
 *
 * @param bytes block contents without the length prefix
 */
public record DwarfBlockValue(byte[] bytes) implements DwarfValue {
	/**
	 * Creates an immutable block value by copying the provided byte array.
	 *
	 * @param bytes block payload bytes
	 */
	public DwarfBlockValue {
		bytes = Arrays.copyOf(bytes, bytes.length);
	}

	/**
	 * Returns a defensive copy of the block payload.
	 *
	 * @return block payload bytes
	 */
	@Override
	public byte[] bytes() {
		return Arrays.copyOf(bytes, bytes.length);
	}
}
