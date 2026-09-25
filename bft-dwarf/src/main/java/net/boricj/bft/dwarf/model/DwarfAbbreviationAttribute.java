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

import net.boricj.bft.dwarf.constants.DwarfAttributeName;
import net.boricj.bft.dwarf.constants.DwarfForm;

/**
 * One attribute specification in a DWARF abbreviation declaration.
 */
public final class DwarfAbbreviationAttribute {
	private final long attributeCode;
	private final DwarfAttributeName attributeName;
	private final DwarfForm form;
	private final Long implicitConstValue;

	/**
	 * Creates a DWARF abbreviation attribute specification.
	 *
	 * @param attributeCode DW_AT_* numeric code
	 * @param attributeName decoded DWARF attribute name, or null when unknown
	 * @param form DW_FORM_* for this attribute
	 * @param implicitConstValue inline constant for DW_FORM_implicit_const, or null otherwise
	 */
	public DwarfAbbreviationAttribute(
			long attributeCode, DwarfAttributeName attributeName, DwarfForm form, Long implicitConstValue) {
		this.attributeCode = attributeCode;
		this.attributeName = attributeName;
		this.form = Objects.requireNonNull(form);
		this.implicitConstValue = implicitConstValue;
	}

	/**
	 * Returns the DW_AT_* numeric code.
	 *
	 * @return attribute code
	 */
	public long getAttributeCode() {
		return attributeCode;
	}

	/**
	 * Returns the resolved attribute enum, when known.
	 *
	 * @return known attribute enum or null
	 */
	public DwarfAttributeName getAttributeName() {
		return attributeName;
	}

	/**
	 * Returns the DW_FORM_* used by this attribute.
	 *
	 * @return form
	 */
	public DwarfForm getForm() {
		return form;
	}

	/**
	 * Returns the inline SLEB128 value for DW_FORM_implicit_const, otherwise null.
	 *
	 * @return implicit constant value or null
	 */
	public Long getImplicitConstValue() {
		return implicitConstValue;
	}
}
