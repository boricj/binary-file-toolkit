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
package net.boricj.bft.elf.machines.mips;

import net.boricj.bft.elf.ElfSectionFlags;

public class ElfSectionFlags_Mips extends ElfSectionFlags {
	private static final long SHF_MIPS_GPREL = 0x10000000;

	private static final long BITMASK = SHF_MIPS_GPREL;

	public ElfSectionFlags_Mips() {
		super();
	}

	public ElfSectionFlags_Mips(long flags) {
		super(flags & ~BITMASK);

		if ((flags & SHF_MIPS_GPREL) != 0) {
			mipsGpRel();
			flags &= ~SHF_MIPS_GPREL;
		}
	}

	public ElfSectionFlags_Mips mipsGpRel() {
		value |= SHF_MIPS_GPREL;
		return this;
	}

	public boolean isMipsGpRel() {
		return (value & SHF_MIPS_GPREL) != 0;
	}
}
