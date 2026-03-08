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

import java.util.Objects;

import net.boricj.bft.Writable;

/**
 * Base class for ELF sections.
 * Sections contain code, data, symbols, relocations, and other information.
 */
public abstract class ElfSection implements Writable {
	/** Undefined section index. */
	public static final int SHN_UNDEF = 0x0000;
	/** Start of reserved section indices. */
	public static final int SHN_LORESERVE = 0xff00;
	/** Extended section index. */
	public static final int SHN_XINDEX = 0xffff;
	/** Absolute symbols, not affected by relocation. */
	public static final int SHN_ABS = 0xfff1;
	/** Common symbols. */
	public static final int SHN_COMMON = 0xfff2;

	private final ElfFile elf;
	private String sh_name;
	private final ElfSectionFlags sh_flags;
	private long sh_address;
	private long sh_offset;
	private final long sh_addralign;
	private final long sh_entsize;

	/**
	 * Creates a new ELF section.
	 *
	 * @param elf parent ELF file
	 * @param name section name
	 * @param flags section flags
	 * @param address virtual address in memory
	 * @param offset file offset
	 * @param addralign address alignment constraint
	 * @param entsize entry size for sections with fixed-size entries
	 */
	public ElfSection(
			ElfFile elf, String name, ElfSectionFlags flags, long address, long offset, long addralign, long entsize) {
		Objects.requireNonNull(elf);
		Objects.requireNonNull(name);
		Objects.requireNonNull(flags);

		this.elf = elf;
		this.sh_name = name;
		this.sh_flags = flags;
		this.sh_address = address;
		this.sh_offset = offset;
		this.sh_addralign = addralign;
		this.sh_entsize = entsize;
	}

	/**
	 * Returns the ELF file this section belongs to.
	 *
	 * @return the parent ELF file
	 */
	public ElfFile getElfFile() {
		return elf;
	}

	/**
	 * Returns the section name.
	 *
	 * @return the section name
	 */
	public String getName() {
		return sh_name;
	}

	/**
	 * Sets the section name.
	 *
	 * @param name new section name
	 */
	public void setName(String name) {
		Objects.requireNonNull(name);

		sh_name = name;
	}

	/**
	 * Returns the section type.
	 *
	 * @return the section type constant
	 */
	public abstract int getType();

	/**
	 * Returns the section flags.
	 *
	 * @return the section flags
	 */
	public ElfSectionFlags getFlags() {
		return sh_flags;
	}

	/**
	 * Returns the section address in virtual memory.
	 *
	 * @return the virtual address
	 */
	public long getAddress() {
		return sh_address;
	}

	/**
	 * Sets the section address in virtual memory.
	 *
	 * @param address new address
	 */
	public void setAddress(long address) {
		sh_address = address;
	}

	@Override
	public long getOffset() {
		return sh_offset;
	}

	/**
	 * Sets the file offset for this section.
	 *
	 * @param offset new file offset
	 */
	public void setOffset(long offset) {
		sh_offset = offset;
	}

	/**
	 * Returns the section link index.
	 *
	 * @return the link index, or 0 if not applicable
	 */
	public int getLink() {
		return 0;
	}

	/**
	 * Returns the section info field.
	 *
	 * @return the info field value, or 0 if not applicable
	 */
	public int getInfo() {
		return 0;
	}

	/**
	 * Returns the address alignment requirement for this section.
	 *
	 * @return the alignment constraint
	 */
	public long getAddrAlign() {
		return sh_addralign;
	}

	/**
	 * Returns the entry size for sections with fixed-size entries.
	 *
	 * @return the entry size, or 0 if not applicable
	 */
	public long getEntSize() {
		return sh_entsize;
	}

	/**
	 * Returns the size of this section in bytes.
	 *
	 * @return the section size
	 */
	public long getSize() {
		return getLength();
	}

	@Override
	public String toString() {
		String className = getClass().getSimpleName();

		if (sh_name != null && !sh_name.isEmpty()) {
			return String.format("%s [%s]", className, sh_name);
		}

		return className;
	}
}
