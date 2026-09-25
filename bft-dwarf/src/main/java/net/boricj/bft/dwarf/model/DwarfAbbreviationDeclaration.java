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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.boricj.bft.dwarf.constants.DwarfTag;

/**
 * A DWARF abbreviation declaration from .debug_abbrev.
 */
public final class DwarfAbbreviationDeclaration {
	private final long code;
	private final long tagCode;
	private final DwarfTag tag;
	private final boolean hasChildren;
	private final List<DwarfAbbreviationAttribute> attributes;

	/**
	 * Creates a DWARF abbreviation declaration.
	 *
	 * @param code abbreviation code
	 * @param tagCode DW_TAG_* numeric code
	 * @param tag decoded DWARF tag, or null when unknown
	 * @param hasChildren whether this declaration has child DIEs
	 * @param attributes attribute specifications
	 */
	public DwarfAbbreviationDeclaration(
			long code, long tagCode, DwarfTag tag, boolean hasChildren, List<DwarfAbbreviationAttribute> attributes) {
		this.code = code;
		this.tagCode = tagCode;
		this.tag = tag;
		this.hasChildren = hasChildren;
		this.attributes = Collections.unmodifiableList(new ArrayList<>(attributes));
	}

	/**
	 * Returns the abbreviation code.
	 *
	 * @return abbreviation code
	 */
	public long getCode() {
		return code;
	}

	/**
	 * Returns the DW_TAG_* numeric code.
	 *
	 * @return tag code
	 */
	public long getTagCode() {
		return tagCode;
	}

	/**
	 * Returns the known tag enum, when available.
	 *
	 * @return known tag enum or null
	 */
	public DwarfTag getTag() {
		return tag;
	}

	/**
	 * Returns whether DIEs of this abbreviation have children.
	 *
	 * @return true when children are present
	 */
	public boolean hasChildren() {
		return hasChildren;
	}

	/**
	 * Returns the ordered list of attribute specifications.
	 *
	 * @return immutable attribute list
	 */
	public List<DwarfAbbreviationAttribute> getAttributes() {
		return attributes;
	}
}
