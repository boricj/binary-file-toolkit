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

/**
 * ELF symbol binding indicating the symbol's linkage visibility and scope.
 */
public enum ElfSymbolBinding {
	/** Local symbol not visible outside the object file. */
	STB_LOCAL((byte) 0),
	/** Global symbol visible to all object files. */
	STB_GLOBAL((byte) 1),
	/** Weak symbol, lower precedence than global symbols. */
	STB_WEAK((byte) 2),
	;

	private final byte value;

	private ElfSymbolBinding(byte value) {
		this.value = value;
	}

	/**
	 * Returns the integer value of this symbol binding.
	 *
	 * @return the byte value as an integer
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Returns the symbol binding constant for the given byte value.
	 *
	 * @param value the byte value to look up
	 * @return the matching symbol binding
	 * @throws IllegalArgumentException if the value is invalid
	 */
	public static ElfSymbolBinding valueFrom(byte value) {
		for (ElfSymbolBinding binding : values()) {
			if (binding.getValue() == value) {
				return binding;
			}
		}

		throw new IllegalArgumentException();
	}
}
