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
package net.boricj.bft.elf;

public class ElfSectionFlags {
	private static final long SHF_WRITE = 0x1;
	private static final long SHF_ALLOC = 0x2;
	private static final long SHF_EXECINSTR = 0x4;
	private static final long SHF_MERGE = 0x10;
	private static final long SHF_STRINGS = 0x20;
	private static final long SHF_INFO_LINK = 0x40;
	private static final long SHF_GROUP = 0x200;

	protected long value;

	public ElfSectionFlags() {
		this.value = 0;
	}

	public ElfSectionFlags(long flags) {
		this();

		if ((flags & SHF_WRITE) != 0) {
			write();
			flags &= ~SHF_WRITE;
		}
		if ((flags & SHF_ALLOC) != 0) {
			alloc();
			flags &= ~SHF_ALLOC;
		}
		if ((flags & SHF_EXECINSTR) != 0) {
			execInstr();
			flags &= ~SHF_EXECINSTR;
		}
		if ((flags & SHF_MERGE) != 0) {
			merge();
			flags &= ~SHF_MERGE;
		}
		if ((flags & SHF_STRINGS) != 0) {
			strings();
			flags &= ~SHF_STRINGS;
		}
		if ((flags & SHF_INFO_LINK) != 0) {
			infoLink();
			flags &= ~SHF_INFO_LINK;
		}
		if ((flags & SHF_GROUP) != 0) {
			group();
			flags &= ~SHF_GROUP;
		}

		if (flags != 0) {
			throw new IllegalArgumentException();
		}
	}

	public long getValue() {
		return value;
	}

	public ElfSectionFlags write() {
		value |= SHF_WRITE;
		return this;
	}

	public ElfSectionFlags alloc() {
		value |= SHF_ALLOC;
		return this;
	}

	public ElfSectionFlags execInstr() {
		value |= SHF_EXECINSTR;
		return this;
	}

	public ElfSectionFlags merge() {
		value |= SHF_MERGE;
		return this;
	}

	public ElfSectionFlags strings() {
		value |= SHF_STRINGS;
		return this;
	}

	public ElfSectionFlags infoLink() {
		value |= SHF_INFO_LINK;
		return this;
	}

	public ElfSectionFlags group() {
		value |= SHF_GROUP;
		return this;
	}

	public boolean isWrite() {
		return (value & SHF_WRITE) != 0;
	}

	public boolean isAlloc() {
		return (value & SHF_ALLOC) != 0;
	}

	public boolean isExecInstr() {
		return (value & SHF_WRITE) != 0;
	}

	public boolean isMerge() {
		return (value & SHF_MERGE) != 0;
	}

	public boolean isStrings() {
		return (value & SHF_STRINGS) != 0;
	}

	public boolean isInfoLink() {
		return (value & SHF_INFO_LINK) != 0;
	}

	public boolean isGroup() {
		return (value & SHF_GROUP) != 0;
	}
}
