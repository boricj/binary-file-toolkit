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
import java.util.Objects;

import net.boricj.bft.dwarf.constants.DwarfTag;

/**
 * Parsed DWARF debugging information entry.
 */
public final class DwarfDie {
	private final long offset;
	private final long abbreviationCode;
	private final long rawTagCode;
	private final DwarfTag tag;
	private final List<DwarfDieAttribute> attributes;
	private final List<DwarfDie> children;

	/**
	 * Creates a parsed DIE.
	 *
	 * @param offset unit-relative DIE offset
	 * @param abbreviationCode abbreviation code used to instantiate this DIE
	 * @param rawTagCode numeric DW_TAG_* value
	 * @param tag known tag enum or null
	 * @param attributes ordered attributes
	 * @param children ordered child DIEs
	 */
	public DwarfDie(
			long offset,
			long abbreviationCode,
			long rawTagCode,
			DwarfTag tag,
			List<DwarfDieAttribute> attributes,
			List<DwarfDie> children) {
		this.offset = offset;
		this.abbreviationCode = abbreviationCode;
		this.rawTagCode = rawTagCode;
		this.tag = tag;
		this.attributes = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(attributes)));
		this.children = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(children)));
	}

	/**
	 * Returns the DIE offset relative to its compilation unit.
	 *
	 * @return unit-relative DIE offset
	 */
	public long getOffset() {
		return offset;
	}

	/**
	 * Returns the abbreviation code that describes this DIE layout.
	 *
	 * @return abbreviation code
	 */
	public long getAbbreviationCode() {
		return abbreviationCode;
	}

	/**
	 * Returns the raw numeric tag code read from the abbreviation table.
	 *
	 * @return raw DW_TAG_* value
	 */
	public long getRawTagCode() {
		return rawTagCode;
	}

	/**
	 * Returns the decoded tag enum, or null when unknown.
	 *
	 * @return decoded tag enum or null
	 */
	public DwarfTag getTag() {
		return tag;
	}

	/**
	 * Returns attributes in declaration order.
	 *
	 * @return immutable list of DIE attributes
	 */
	public List<DwarfDieAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * Returns child DIEs in encounter order.
	 *
	 * @return immutable list of child DIEs
	 */
	public List<DwarfDie> getChildren() {
		return children;
	}
}
