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
 * DWARF attribute forms.
 *
 * <p>Version availability follows the DWARF standard. The introduction version is tracked to enforce
 * that newer forms are not encoded in older DWARF artifacts.
 */
public enum DwarfForm {
	/** Fixed-width target address. */
	DW_FORM_addr(0x01, DwarfVersion.DWARF2),
	/** Block prefixed with a 2-byte length. */
	DW_FORM_block2(0x03, DwarfVersion.DWARF2),
	/** Block prefixed with a 4-byte length. */
	DW_FORM_block4(0x04, DwarfVersion.DWARF2),
	/** 2-byte unsigned constant. */
	DW_FORM_data2(0x05, DwarfVersion.DWARF2),
	/** 4-byte unsigned constant. */
	DW_FORM_data4(0x06, DwarfVersion.DWARF2),
	/** 8-byte unsigned constant. */
	DW_FORM_data8(0x07, DwarfVersion.DWARF2),
	/** Inline null-terminated UTF-8 string. */
	DW_FORM_string(0x08, DwarfVersion.DWARF2),
	/** Block prefixed with ULEB128 length. */
	DW_FORM_block(0x09, DwarfVersion.DWARF2),
	/** Block prefixed with a 1-byte length. */
	DW_FORM_block1(0x0a, DwarfVersion.DWARF2),
	/** 1-byte unsigned constant. */
	DW_FORM_data1(0x0b, DwarfVersion.DWARF2),
	/** Explicit boolean flag byte. */
	DW_FORM_flag(0x0c, DwarfVersion.DWARF2),
	/** Signed LEB128 constant. */
	DW_FORM_sdata(0x0d, DwarfVersion.DWARF2),
	/** String-table offset into .debug_str. */
	DW_FORM_strp(0x0e, DwarfVersion.DWARF2),
	/** Unsigned LEB128 constant. */
	DW_FORM_udata(0x0f, DwarfVersion.DWARF2),
	/** Cross-unit reference address. */
	DW_FORM_ref_addr(0x10, DwarfVersion.DWARF2),
	/** 1-byte local DIE reference. */
	DW_FORM_ref1(0x11, DwarfVersion.DWARF2),
	/** 2-byte local DIE reference. */
	DW_FORM_ref2(0x12, DwarfVersion.DWARF2),
	/** 4-byte local DIE reference. */
	DW_FORM_ref4(0x13, DwarfVersion.DWARF2),
	/** 8-byte local DIE reference. */
	DW_FORM_ref8(0x14, DwarfVersion.DWARF2),
	/** ULEB128 local DIE reference. */
	DW_FORM_ref_udata(0x15, DwarfVersion.DWARF2),
	/** Indirect form, encoded by a nested form code. */
	DW_FORM_indirect(0x16, DwarfVersion.DWARF2),

	/** Section-relative offset into another debug section. */
	DW_FORM_sec_offset(0x17, DwarfVersion.DWARF4),
	/** Expression location block with ULEB128 length. */
	DW_FORM_exprloc(0x18, DwarfVersion.DWARF4),
	/** Implicitly-present boolean flag (no payload bytes). */
	DW_FORM_flag_present(0x19, DwarfVersion.DWARF4),
	/** 8-byte type signature reference. */
	DW_FORM_ref_sig8(0x20, DwarfVersion.DWARF4),

	/** Indexed string reference into DWARF5 string index tables. */
	DW_FORM_strx(0x1a, DwarfVersion.DWARF5),
	/** Indexed address reference into DWARF5 address tables. */
	DW_FORM_addrx(0x1b, DwarfVersion.DWARF5),
	/** 4-byte supplementary-reference offset. */
	DW_FORM_ref_sup4(0x1c, DwarfVersion.DWARF5),
	/** Supplementary string-table offset. */
	DW_FORM_strp_sup(0x1d, DwarfVersion.DWARF5),
	/** 16-byte constant. */
	DW_FORM_data16(0x1e, DwarfVersion.DWARF5),
	/** String-table offset into .debug_line_str. */
	DW_FORM_line_strp(0x1f, DwarfVersion.DWARF5),
	/** Constant encoded in abbreviation, not in DIE payload. */
	DW_FORM_implicit_const(0x21, DwarfVersion.DWARF5),
	/** Indexed location-list entry. */
	DW_FORM_loclistx(0x22, DwarfVersion.DWARF5),
	/** Indexed range-list entry. */
	DW_FORM_rnglistx(0x23, DwarfVersion.DWARF5),
	/** 8-byte supplementary-reference offset. */
	DW_FORM_ref_sup8(0x24, DwarfVersion.DWARF5),
	/** 1-byte string index. */
	DW_FORM_strx1(0x25, DwarfVersion.DWARF5),
	/** 2-byte string index. */
	DW_FORM_strx2(0x26, DwarfVersion.DWARF5),
	/** 3-byte string index. */
	DW_FORM_strx3(0x27, DwarfVersion.DWARF5),
	/** 4-byte string index. */
	DW_FORM_strx4(0x28, DwarfVersion.DWARF5),
	/** 1-byte address index. */
	DW_FORM_addrx1(0x29, DwarfVersion.DWARF5),
	/** 2-byte address index. */
	DW_FORM_addrx2(0x2a, DwarfVersion.DWARF5),
	/** 3-byte address index. */
	DW_FORM_addrx3(0x2b, DwarfVersion.DWARF5),
	/** 4-byte address index. */
	DW_FORM_addrx4(0x2c, DwarfVersion.DWARF5),
	;

	private final int value;
	private final DwarfVersion introducedIn;

	DwarfForm(int value, DwarfVersion introducedIn) {
		this.value = value;
		this.introducedIn = introducedIn;
	}

	/**
	 * Returns the numeric form code.
	 *
	 * @return DW_FORM value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Returns the version in which this form was introduced.
	 *
	 * @return minimum supported version
	 */
	public DwarfVersion getIntroducedIn() {
		return introducedIn;
	}

	/**
	 * Returns whether this form is valid in the provided DWARF version.
	 *
	 * @param version target DWARF version
	 * @return true when supported
	 */
	public boolean isSupportedIn(DwarfVersion version) {
		return version.isAtLeast(introducedIn);
	}

	/**
	 * Looks up a DWARF form by its numeric value.
	 *
	 * @param value numeric form code
	 * @return matching form
	 * @throws IllegalArgumentException when unknown
	 */
	public static DwarfForm valueFrom(int value) {
		for (DwarfForm form : values()) {
			if (form.getValue() == value) {
				return form;
			}
		}

		throw new IllegalArgumentException("Unsupported DWARF form: 0x" + Integer.toHexString(value));
	}
}
