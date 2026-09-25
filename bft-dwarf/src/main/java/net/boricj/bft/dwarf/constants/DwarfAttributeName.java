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
 * Known DWARF attribute names used by the current fixtures.
 */
public enum DwarfAttributeName {
	/** DWARF sibling DIE reference attribute. */
	DW_AT_sibling(0x01),
	/** DWARF location description attribute. */
	DW_AT_location(0x02),
	/** DWARF name string attribute. */
	DW_AT_name(0x03),
	/** DWARF byte-size attribute. */
	DW_AT_byte_size(0x0b),
	/** DWARF statement list offset attribute. */
	DW_AT_stmt_list(0x10),
	/** DWARF low-PC attribute. */
	DW_AT_low_pc(0x11),
	/** DWARF high-PC attribute. */
	DW_AT_high_pc(0x12),
	/** DWARF source-language attribute. */
	DW_AT_language(0x13),
	/** DWARF compilation directory attribute. */
	DW_AT_comp_dir(0x1b),
	/** DWARF upper-bound attribute. */
	DW_AT_upper_bound(0x2f),
	/** DWARF producer string attribute. */
	DW_AT_producer(0x25),
	/** DWARF prototyped-flag attribute. */
	DW_AT_prototyped(0x27),
	/** DWARF count attribute. */
	DW_AT_count(0x37),
	/** DWARF lower-bound attribute. */
	DW_AT_lower_bound(0x22),
	/** DWARF declaration-column attribute. */
	DW_AT_decl_column(0x39),
	/** DWARF declaration-file attribute. */
	DW_AT_decl_file(0x3a),
	/** DWARF declaration-line attribute. */
	DW_AT_decl_line(0x3b),
	/** DWARF data-member-location attribute. */
	DW_AT_data_member_location(0x38),
	/** DWARF declaration marker attribute. */
	DW_AT_declaration(0x3c),
	/** DWARF encoding attribute. */
	DW_AT_encoding(0x3e),
	/** DWARF external-visibility flag attribute. */
	DW_AT_external(0x3f),
	/** DWARF frame-base attribute. */
	DW_AT_frame_base(0x40),
	/** DWARF type-reference attribute. */
	DW_AT_type(0x49),
	/** DWARF call-all-calls attribute. */
	DW_AT_call_all_calls(0x7a),
	/** DWARF call-all-tail-calls attribute. */
	DW_AT_call_all_tail_calls(0x7c),
	/** GNU extension attribute indicating all call sites are present. */
	DW_AT_GNU_all_call_sites(0x2117),
	/** GNU extension attribute indicating all tail-call sites are present. */
	DW_AT_GNU_all_tail_call_sites(0x2118),
	;

	private final int value;

	DwarfAttributeName(int value) {
		this.value = value;
	}

	/**
	 * Returns the numeric attribute code.
	 *
	 * @return attribute value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Returns the matching known attribute or null when not modeled yet.
	 *
	 * @param value numeric attribute code
	 * @return matching attribute or null
	 */
	public static DwarfAttributeName valueFromOrNull(long value) {
		for (DwarfAttributeName attribute : values()) {
			if (attribute.getValue() == value) {
				return attribute;
			}
		}
		return null;
	}
}
