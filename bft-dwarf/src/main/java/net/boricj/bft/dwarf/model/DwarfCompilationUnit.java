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

/**
 * One parsed compilation unit from .debug_info.
 */
public final class DwarfCompilationUnit {
	private final long sectionOffset;
	private final DwarfCompilationUnitHeader header;
	private final List<DwarfDie> dies;

	/**
	 * Creates a compilation unit.
	 *
	 * @param sectionOffset section-relative offset of the unit header
	 * @param header parsed header
	 * @param dies ordered top-level DIEs in this unit
	 */
	public DwarfCompilationUnit(long sectionOffset, DwarfCompilationUnitHeader header, List<DwarfDie> dies) {
		this.sectionOffset = sectionOffset;
		this.header = Objects.requireNonNull(header);
		this.dies = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(dies)));
	}

	/**
	 * Returns the unit header offset within the .debug_info section.
	 *
	 * @return section-relative compilation-unit offset
	 */
	public long getSectionOffset() {
		return sectionOffset;
	}

	/**
	 * Returns the parsed compilation-unit header.
	 *
	 * @return compilation-unit header
	 */
	public DwarfCompilationUnitHeader getHeader() {
		return header;
	}

	/**
	 * Returns top-level DIEs in encounter order.
	 *
	 * @return immutable list of top-level DIEs
	 */
	public List<DwarfDie> getDies() {
		return dies;
	}
}
