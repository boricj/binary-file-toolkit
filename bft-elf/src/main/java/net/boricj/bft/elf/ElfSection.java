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

public abstract class ElfSection implements Writable {
	public static final int SHN_UNDEF = 0x0000;
	public static final int SHN_LORESERVE = 0xff00;
	public static final int SHN_XINDEX = 0xffff;
	public static final int SHN_ABS = 0xfff1;
	public static final int SHN_COMMON = 0xfff2;

	private final ElfFile elf;
	private String sh_name;
	private final ElfSectionFlags sh_flags;
	private long sh_address;
	private long sh_offset;
	private final long sh_addralign;
	private final long sh_entsize;

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

	public ElfFile getElfFile() {
		return elf;
	}

	public String getName() {
		return sh_name;
	}

	public void setName(String name) {
		Objects.requireNonNull(name);

		sh_name = name;
	}

	public abstract int getType();

	public ElfSectionFlags getFlags() {
		return sh_flags;
	}

	public long getAddress() {
		return sh_address;
	}

	public void setAddress(long address) {
		sh_address = address;
	}

	@Override
	public long getOffset() {
		return sh_offset;
	}

	public void setOffset(long offset) {
		sh_offset = offset;
	}

	public int getLink() {
		return 0;
	}

	public int getInfo() {
		return 0;
	}

	public long getAddrAlign() {
		return sh_addralign;
	}

	public long getEntSize() {
		return sh_entsize;
	}

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
