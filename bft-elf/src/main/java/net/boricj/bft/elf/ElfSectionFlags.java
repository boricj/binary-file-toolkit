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

/**
 * ELF section flags indicating section attributes.
 * These flags control how sections are treated during loading and linking.
 */
public class ElfSectionFlags {
	private static final long SHF_WRITE = 0x1;
	private static final long SHF_ALLOC = 0x2;
	private static final long SHF_EXECINSTR = 0x4;
	private static final long SHF_MERGE = 0x10;
	private static final long SHF_STRINGS = 0x20;
	private static final long SHF_INFO_LINK = 0x40;
	private static final long SHF_GROUP = 0x200;

	/** The numeric section flags value. */
	protected long value;

	/**
	 * Creates a new empty section flags object.
	 */
	public ElfSectionFlags() {
		this.value = 0;
	}

	/**
	 * Creates section flags from a flags value.
	 *
	 * @param flags flags value to parse
	 */
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

	/**
	 * Returns the numeric flags value.
	 *
	 * @return the flags value
	 */
	public long getValue() {
		return value;
	}

	/**
	 * Sets the writable flag.
	 *
	 * @return this flags object
	 */
	public ElfSectionFlags write() {
		value |= SHF_WRITE;
		return this;
	}

	/**
	 * Sets the allocatable flag (occupies memory during execution).
	 *
	 * @return this flags object
	 */
	public ElfSectionFlags alloc() {
		value |= SHF_ALLOC;
		return this;
	}

	/**
	 * Sets the executable instructions flag.
	 *
	 * @return this flags object
	 */
	public ElfSectionFlags execInstr() {
		value |= SHF_EXECINSTR;
		return this;
	}

	/**
	 * Sets the merge flag (section can be merged with identical entries).
	 *
	 * @return this flags object
	 */
	public ElfSectionFlags merge() {
		value |= SHF_MERGE;
		return this;
	}

	/**
	 * Sets the strings flag (section contains null-terminated strings).
	 *
	 * @return this flags object
	 */
	public ElfSectionFlags strings() {
		value |= SHF_STRINGS;
		return this;
	}

	/**
	 * Sets the info link flag (sh_info field holds a section index).
	 *
	 * @return this flags object
	 */
	public ElfSectionFlags infoLink() {
		value |= SHF_INFO_LINK;
		return this;
	}

	/**
	 * Sets the group flag (section is member of a section group).
	 *
	 * @return this flags object
	 */
	public ElfSectionFlags group() {
		value |= SHF_GROUP;
		return this;
	}

	/**
	 * Checks if the writable flag is set.
	 *
	 * @return true if section is writable
	 */
	public boolean isWrite() {
		return (value & SHF_WRITE) != 0;
	}

	/**
	 * Checks if the allocatable flag is set.
	 *
	 * @return true if section is allocatable
	 */
	public boolean isAlloc() {
		return (value & SHF_ALLOC) != 0;
	}

	/**
	 * Checks if the executable instructions flag is set.
	 *
	 * @return true if section contains executable instructions
	 */
	public boolean isExecInstr() {
		return (value & SHF_WRITE) != 0;
	}

	/**
	 * Checks if the merge flag is set.
	 *
	 * @return true if section can be merged
	 */
	public boolean isMerge() {
		return (value & SHF_MERGE) != 0;
	}

	/**
	 * Checks if the strings flag is set.
	 *
	 * @return true if section contains null-terminated strings
	 */
	public boolean isStrings() {
		return (value & SHF_STRINGS) != 0;
	}

	/**
	 * Checks if the info link flag is set.
	 *
	 * @return true if sh_info holds a section index
	 */
	public boolean isInfoLink() {
		return (value & SHF_INFO_LINK) != 0;
	}

	/**
	 * Checks if the group flag is set.
	 *
	 * @return true if section is member of a section group
	 */
	public boolean isGroup() {
		return (value & SHF_GROUP) != 0;
	}
}
