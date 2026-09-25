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
 * DWARF v5 line-table content descriptors.
 */
public enum DwarfLineContentType {
	/** Path string (directory or file path). */
	DW_LNCT_path(0x1),
	/** One-based directory table index for a file entry. */
	DW_LNCT_directory_index(0x2),
	/** File timestamp metadata. */
	DW_LNCT_timestamp(0x3),
	/** File size metadata. */
	DW_LNCT_size(0x4),
	/** MD5 digest metadata. */
	DW_LNCT_MD5(0x5),
	;

	private final int value;

	DwarfLineContentType(int value) {
		this.value = value;
	}

	/**
	 * Returns numeric DW_LNCT_* code.
	 *
	 * @return content-type code
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Resolves a numeric content-type code to a known enum constant.
	 *
	 * @param value numeric DW_LNCT_* code
	 * @return known content type or null when unknown
	 */
	public static DwarfLineContentType valueFromOrNull(long value) {
		for (DwarfLineContentType type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}
		return null;
	}
}
