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
 * Known DWARF DIE tags used by the current fixtures.
 */
public enum DwarfTag {
	/** Array type DIE. */
	DW_TAG_array_type(0x01),
	/** Formal parameter DIE. */
	DW_TAG_formal_parameter(0x05),
	/** Lexical block DIE. */
	DW_TAG_lexical_block(0x0b),
	/** Structure/union member DIE. */
	DW_TAG_member(0x0d),
	/** Pointer type DIE. */
	DW_TAG_pointer_type(0x0f),
	/** Compilation-unit root DIE. */
	DW_TAG_compile_unit(0x11),
	/** Structure type DIE. */
	DW_TAG_structure_type(0x13),
	/** Subroutine type DIE. */
	DW_TAG_subroutine_type(0x15),
	/** Union type DIE. */
	DW_TAG_union_type(0x17),
	/** Unspecified parameters marker DIE. */
	DW_TAG_unspecified_parameters(0x18),
	/** Subrange type DIE. */
	DW_TAG_subrange_type(0x21),
	/** Const-qualified type DIE. */
	DW_TAG_const_type(0x26),
	/** Base type DIE. */
	DW_TAG_base_type(0x24),
	/** Subprogram DIE. */
	DW_TAG_subprogram(0x2e),
	/** Variable DIE. */
	DW_TAG_variable(0x34),
	;

	private final int value;

	DwarfTag(int value) {
		this.value = value;
	}

	/**
	 * Returns the numeric tag code.
	 *
	 * @return tag value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Returns the matching known tag or null when it is not modeled yet.
	 *
	 * @param value numeric tag code
	 * @return matching tag or null
	 */
	public static DwarfTag valueFromOrNull(long value) {
		for (DwarfTag tag : values()) {
			if (tag.getValue() == value) {
				return tag;
			}
		}
		return null;
	}
}
