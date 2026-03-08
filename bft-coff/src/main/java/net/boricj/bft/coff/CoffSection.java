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
package net.boricj.bft.coff;

import java.util.Objects;

import net.boricj.bft.Writable;
import net.boricj.bft.coff.constants.CoffSectionFlags;

/** Base class for COFF sections. */
public abstract class CoffSection implements Writable {
	private final CoffFile coff;
	private final String name;
	private int physicalAddress;
	private int virtualAddress;
	private int pointerToRawData;
	private CoffSectionFlags characteristics;
	/** Relocation table associated with this section. */
	protected CoffRelocationTable relocationTable;

	/**
	 * Creates a COFF section.
	 *
	 * @param coff parent COFF file
	 * @param name section name
	 * @param physicalAddress section physical address
	 * @param virtualAddress section virtual address
	 * @param pointerToRawData file offset of section data
	 * @param characteristics section characteristics flags
	 */
	public CoffSection(
			CoffFile coff,
			String name,
			int physicalAddress,
			int virtualAddress,
			int pointerToRawData,
			CoffSectionFlags characteristics) {
		Objects.requireNonNull(coff);
		Objects.requireNonNull(name);
		Objects.requireNonNull(characteristics);

		this.coff = coff;
		this.name = name;
		this.physicalAddress = physicalAddress;
		this.virtualAddress = virtualAddress;
		this.pointerToRawData = pointerToRawData;
		this.characteristics = characteristics;
	}

	/**
	 * Returns the COFF file this section belongs to.
	 *
	 * @return the parent COFF file
	 */
	public CoffFile getCoffFile() {
		return coff;
	}

	/**
	 * Returns the section name.
	 *
	 * @return the section name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the virtual size of this section.
	 *
	 * @return the virtual size
	 */
	public int getVirtualSize() {
		return (int) getLength();
	}

	/**
	 * Returns the physical address of this section.
	 *
	 * @return the physical address
	 */
	public int getPhysicalAddress() {
		return physicalAddress;
	}

	/**
	 * Sets the physical address of this section.
	 *
	 * @param physicalAddress new physical address
	 */
	public void setPhysicalAddress(int physicalAddress) {
		this.physicalAddress = physicalAddress;
	}

	/**
	 * Returns the virtual address of this section.
	 *
	 * @return the virtual address
	 */
	public int getVirtualAddress() {
		return virtualAddress;
	}

	/**
	 * Sets the virtual address of this section.
	 *
	 * @param virtualAddress new virtual address
	 */
	public void setVirtualAddress(int virtualAddress) {
		this.virtualAddress = virtualAddress;
	}

	@Override
	public long getOffset() {
		return pointerToRawData;
	}

	/**
	 * Sets the file offset for this section's raw data.
	 *
	 * @param pointerToRawData new file offset
	 */
	public void setOffset(int pointerToRawData) {
		this.pointerToRawData = pointerToRawData;
	}

	/**
	 * Returns the section characteristics flags.
	 *
	 * @return the characteristics flags
	 */
	public CoffSectionFlags getCharacteristics() {
		return characteristics;
	}

	/**
	 * Sets the section characteristics flags.
	 *
	 * @param characteristics new flags
	 */
	public void setCharacteristics(CoffSectionFlags characteristics) {
		this.characteristics = characteristics;
	}

	/**
	 * Returns the relocation table for this section.
	 *
	 * @return the relocation table
	 */
	public CoffRelocationTable getRelocations() {
		return relocationTable;
	}

	@Override
	public String toString() {
		String className = getClass().getSimpleName();

		if (name != null && !name.isEmpty()) {
			return String.format("%s [%s]", className, name);
		}

		return className;
	}
}
