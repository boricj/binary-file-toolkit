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

public abstract class CoffSection implements Writable {
	private final CoffFile coff;
	private final String name;
	private int physicalAddress;
	private int virtualAddress;
	private int pointerToRawData;
	private CoffSectionFlags characteristics;
	protected CoffRelocationTable relocationTable;

	public CoffSection(
			CoffFile coff,
			String name,
			int physicalAddress,
			int virtualAddress,
			int pointerToRawData,
			CoffSectionFlags characteristics) {
		Objects.requireNonNull(coff);

		this.coff = coff;
		this.name = name;
		this.physicalAddress = physicalAddress;
		this.virtualAddress = virtualAddress;
		this.pointerToRawData = pointerToRawData;
		this.characteristics = characteristics;
	}

	public CoffFile getCoffFile() {
		return coff;
	}

	public String getName() {
		return name;
	}

	public int getVirtualSize() {
		return (int) getLength();
	}

	public int getPhysicalAddress() {
		return physicalAddress;
	}

	public void setPhysicalAddress(int physicalAddress) {
		this.physicalAddress = physicalAddress;
	}

	public int getVirtualAddress() {
		return virtualAddress;
	}

	public void setVirtualAddress(int virtualAddress) {
		this.virtualAddress = virtualAddress;
	}

	@Override
	public long getOffset() {
		return pointerToRawData;
	}

	public void setOffset(int pointerToRawData) {
		this.pointerToRawData = pointerToRawData;
	}

	public CoffSectionFlags getCharacteristics() {
		return characteristics;
	}

	public void setCharacteristics(CoffSectionFlags characteristics) {
		this.characteristics = characteristics;
	}

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
