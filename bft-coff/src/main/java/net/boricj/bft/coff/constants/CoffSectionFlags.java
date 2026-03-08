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
package net.boricj.bft.coff.constants;

/**
 * Mutable builder-style wrapper around COFF section flags.
 */
public class CoffSectionFlags {
	/** Section should not be padded to the next boundary. */
	public static final int IMAGE_SCN_TYPE_NO_PAD = 0x00000008;
	/** Section contains executable code. */
	public static final int IMAGE_SCN_CNT_CODE = 0x00000020;
	/** Section contains initialized data. */
	public static final int IMAGE_SCN_CNT_INITIALIZED_DATA = 0x00000040;
	/** Section contains uninitialized data. */
	public static final int IMAGE_SCN_CNT_UNINITIALIZED_DATA = 0x00000080;
	/** Reserved link flag. */
	public static final int IMAGE_SCN_LNK_OTHER = 0x00000100;
	/** Section contains comments or other information. */
	public static final int IMAGE_SCN_LNK_INFO = 0x00000200;
	/** Section contents should not become part of the image. */
	public static final int IMAGE_SCN_LNK_REMOVE = 0x00000800;
	/** Section contents are COMDAT data. */
	public static final int IMAGE_SCN_LNK_COMDAT = 0x00001000;
	/** Section contents can be accessed relative to GP. */
	public static final int IMAGE_SCN_GPREL = 0x00008000;
	/** Reserved memory flag alias. */
	public static final int IMAGE_SCN_MEM_PURGEABLE = 0x00020000;
	/** Reserved memory flag alias. */
	public static final int IMAGE_SCN_MEM_16BIT = 0x00020000;
	/** Reserved memory flag alias. */
	public static final int IMAGE_SCN_MEM_LOCKED = 0x00040000;
	/** Reserved memory flag alias. */
	public static final int IMAGE_SCN_MEM_PRELOAD = 0x00080000;
	/** Section alignment is 1 byte. */
	public static final int IMAGE_SCN_ALIGN_1BYTES = 0x00100000;
	/** Section alignment is 2 bytes. */
	public static final int IMAGE_SCN_ALIGN_2BYTES = 0x00200000;
	/** Section alignment is 4 bytes. */
	public static final int IMAGE_SCN_ALIGN_4BYTES = 0x00300000;
	/** Section alignment is 8 bytes. */
	public static final int IMAGE_SCN_ALIGN_8BYTES = 0x00400000;
	/** Section alignment is 16 bytes. */
	public static final int IMAGE_SCN_ALIGN_16BYTES = 0x00500000;
	/** Section alignment is 32 bytes. */
	public static final int IMAGE_SCN_ALIGN_32BYTES = 0x00600000;
	/** Section alignment is 64 bytes. */
	public static final int IMAGE_SCN_ALIGN_64BYTES = 0x00700000;
	/** Section alignment is 128 bytes. */
	public static final int IMAGE_SCN_ALIGN_128BYTES = 0x00800000;
	/** Section alignment is 256 bytes. */
	public static final int IMAGE_SCN_ALIGN_256BYTES = 0x00900000;
	/** Section alignment is 512 bytes. */
	public static final int IMAGE_SCN_ALIGN_512BYTES = 0x00A00000;
	/** Section alignment is 1024 bytes. */
	public static final int IMAGE_SCN_ALIGN_1024BYTES = 0x00B00000;
	/** Section alignment is 2048 bytes. */
	public static final int IMAGE_SCN_ALIGN_2048BYTES = 0x00C00000;
	/** Section alignment is 4096 bytes. */
	public static final int IMAGE_SCN_ALIGN_4096BYTES = 0x00D00000;
	/** Section alignment is 8192 bytes. */
	public static final int IMAGE_SCN_ALIGN_8192BYTES = 0x00E00000;
	/** Section has more than 65535 relocations. */
	public static final int IMAGE_SCN_LNK_NRELOC_OVFL = 0x01000000;
	/** Section can be discarded as needed. */
	public static final int IMAGE_SCN_MEM_DISCARDABLE = 0x02000000;
	/** Section should not be cached. */
	public static final int IMAGE_SCN_MEM_NOT_CACHED = 0x04000000;
	/** Section should not be paged. */
	public static final int IMAGE_SCN_MEM_NOT_PAGED = 0x08000000;
	/** Section can be shared in memory. */
	public static final int IMAGE_SCN_MEM_SHARED = 0x10000000;
	/** Section is executable. */
	public static final int IMAGE_SCN_MEM_EXECUTE = 0x20000000;
	/** Section is readable. */
	public static final int IMAGE_SCN_MEM_READ = 0x40000000;
	/** Section is writable. */
	public static final int IMAGE_SCN_MEM_WRITE = 0x80000000;

	private static final int IMAGE_SCN_ALIGN_MASK = 0x00F00000;

	private int value;

	/**
	 * Creates flags with all bits cleared.
	 */
	public CoffSectionFlags() {
		this.value = 0;
	}

	/**
	 * Creates flags with an initial encoded value.
	 *
	 * @param value initial section flags value
	 */
	public CoffSectionFlags(int value) {
		this.value = value;
	}

	/**
	 * Gets the encoded flags value.
	 *
	 * @return encoded section flags
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Sets {@link #IMAGE_SCN_TYPE_NO_PAD}.
	 *
	 * @return this flags instance
	 */
	public CoffSectionFlags typeNoPad() {
		value |= IMAGE_SCN_TYPE_NO_PAD;
		return this;
	}

	/**
	 * Sets {@link #IMAGE_SCN_CNT_CODE}.
	 *
	 * @return this flags instance
	 */
	public CoffSectionFlags cntCode() {
		value |= IMAGE_SCN_CNT_CODE;
		return this;
	}

	/**
	 * Sets {@link #IMAGE_SCN_CNT_INITIALIZED_DATA}.
	 *
	 * @return this flags instance
	 */
	public CoffSectionFlags cntInitializedData() {
		value |= IMAGE_SCN_CNT_INITIALIZED_DATA;
		return this;
	}

	/**
	 * Sets {@link #IMAGE_SCN_CNT_UNINITIALIZED_DATA}.
	 *
	 * @return this flags instance
	 */
	public CoffSectionFlags cntUninitializedData() {
		value |= IMAGE_SCN_CNT_UNINITIALIZED_DATA;
		return this;
	}

	/**
	 * Sets {@link #IMAGE_SCN_LNK_OTHER}.
	 *
	 * @return this flags instance
	 */
	public CoffSectionFlags lnkOther() {
		value |= IMAGE_SCN_LNK_OTHER;
		return this;
	}

	/**
	 * Sets {@link #IMAGE_SCN_LNK_INFO}.
	 *
	 * @return this flags instance
	 */
	public CoffSectionFlags lnkInfo() {
		value |= IMAGE_SCN_LNK_INFO;
		return this;
	}

	/**
	 * Sets {@link #IMAGE_SCN_LNK_REMOVE}.
	 *
	 * @return this flags instance
	 */
	public CoffSectionFlags lnkRemove() {
		value |= IMAGE_SCN_LNK_REMOVE;
		return this;
	}

	/**
	 * Sets {@link #IMAGE_SCN_LNK_COMDAT}.
	 *
	 * @return this flags instance
	 */
	public CoffSectionFlags lnkComdat() {
		value |= IMAGE_SCN_LNK_COMDAT;
		return this;
	}

	/**
	 * Sets {@link #IMAGE_SCN_GPREL}.
	 *
	 * @return this flags instance
	 */
	public CoffSectionFlags gprel() {
		value |= IMAGE_SCN_GPREL;
		return this;
	}

	/**
	 * Sets {@link #IMAGE_SCN_MEM_PURGEABLE}.
	 *
	 * @return this flags instance
	 */
	public CoffSectionFlags memPurgeable() {
		value |= IMAGE_SCN_MEM_PURGEABLE;
		return this;
	}

	/**
	 * Sets {@link #IMAGE_SCN_MEM_16BIT}.
	 *
	 * @return this flags instance
	 */
	public CoffSectionFlags mem16bit() {
		value |= IMAGE_SCN_MEM_16BIT;
		return this;
	}

	/**
	 * Sets {@link #IMAGE_SCN_MEM_LOCKED}.
	 *
	 * @return this flags instance
	 */
	public CoffSectionFlags memLocked() {
		value |= IMAGE_SCN_MEM_LOCKED;
		return this;
	}

	/**
	 * Sets {@link #IMAGE_SCN_MEM_PRELOAD}.
	 *
	 * @return this flags instance
	 */
	public CoffSectionFlags memPreload() {
		value |= IMAGE_SCN_MEM_PRELOAD;
		return this;
	}

	/**
	 * Sets the section alignment bits.
	 *
	 * @param align alignment in bytes (0, 1, 2, 4, ..., 8192)
	 * @return this flags instance
	 */
	public CoffSectionFlags alignBytes(int align) {
		switch (align) {
			case 0:
				value = (value & ~IMAGE_SCN_ALIGN_MASK);
				break;

			case 1:
				value = (value & ~IMAGE_SCN_ALIGN_MASK) | IMAGE_SCN_ALIGN_1BYTES;
				break;

			case 2:
				value = (value & ~IMAGE_SCN_ALIGN_MASK) | IMAGE_SCN_ALIGN_2BYTES;
				break;

			case 4:
				value = (value & ~IMAGE_SCN_ALIGN_MASK) | IMAGE_SCN_ALIGN_4BYTES;
				break;

			case 8:
				value = (value & ~IMAGE_SCN_ALIGN_MASK) | IMAGE_SCN_ALIGN_8BYTES;
				break;

			case 16:
				value = (value & ~IMAGE_SCN_ALIGN_MASK) | IMAGE_SCN_ALIGN_16BYTES;
				break;

			case 32:
				value = (value & ~IMAGE_SCN_ALIGN_MASK) | IMAGE_SCN_ALIGN_32BYTES;
				break;

			case 64:
				value = (value & ~IMAGE_SCN_ALIGN_MASK) | IMAGE_SCN_ALIGN_64BYTES;
				break;

			case 128:
				value = (value & ~IMAGE_SCN_ALIGN_MASK) | IMAGE_SCN_ALIGN_128BYTES;
				break;

			case 256:
				value = (value & ~IMAGE_SCN_ALIGN_MASK) | IMAGE_SCN_ALIGN_256BYTES;
				break;

			case 512:
				value = (value & ~IMAGE_SCN_ALIGN_MASK) | IMAGE_SCN_ALIGN_512BYTES;
				break;

			case 1024:
				value = (value & ~IMAGE_SCN_ALIGN_MASK) | IMAGE_SCN_ALIGN_1024BYTES;
				break;

			case 2048:
				value = (value & ~IMAGE_SCN_ALIGN_MASK) | IMAGE_SCN_ALIGN_2048BYTES;
				break;

			case 4096:
				value = (value & ~IMAGE_SCN_ALIGN_MASK) | IMAGE_SCN_ALIGN_4096BYTES;
				break;

			case 8192:
				value = (value & ~IMAGE_SCN_ALIGN_MASK) | IMAGE_SCN_ALIGN_8192BYTES;
				break;

			default:
				throw new IllegalArgumentException();
		}

		return this;
	}

	/**
	 * Sets {@link #IMAGE_SCN_MEM_DISCARDABLE}.
	 *
	 * @return this flags instance
	 */
	public CoffSectionFlags memDiscardable() {
		value |= IMAGE_SCN_MEM_DISCARDABLE;
		return this;
	}

	/**
	 * Sets {@link #IMAGE_SCN_MEM_NOT_CACHED}.
	 *
	 * @return this flags instance
	 */
	public CoffSectionFlags memNotCached() {
		value |= IMAGE_SCN_MEM_NOT_CACHED;
		return this;
	}

	/**
	 * Sets {@link #IMAGE_SCN_MEM_NOT_PAGED}.
	 *
	 * @return this flags instance
	 */
	public CoffSectionFlags memNotPaged() {
		value |= IMAGE_SCN_MEM_NOT_PAGED;
		return this;
	}

	/**
	 * Sets {@link #IMAGE_SCN_MEM_SHARED}.
	 *
	 * @return this flags instance
	 */
	public CoffSectionFlags memShared() {
		value |= IMAGE_SCN_MEM_SHARED;
		return this;
	}

	/**
	 * Sets {@link #IMAGE_SCN_MEM_EXECUTE}.
	 *
	 * @return this flags instance
	 */
	public CoffSectionFlags memExecute() {
		value |= IMAGE_SCN_MEM_EXECUTE;
		return this;
	}

	/**
	 * Sets {@link #IMAGE_SCN_MEM_READ}.
	 *
	 * @return this flags instance
	 */
	public CoffSectionFlags memRead() {
		value |= IMAGE_SCN_MEM_READ;
		return this;
	}

	/**
	 * Sets {@link #IMAGE_SCN_MEM_WRITE}.
	 *
	 * @return this flags instance
	 */
	public CoffSectionFlags memWrite() {
		value |= IMAGE_SCN_MEM_WRITE;
		return this;
	}

	/**
	 * Checks whether {@link #IMAGE_SCN_TYPE_NO_PAD} is set.
	 *
	 * @return {@code true} if set, {@code false} otherwise
	 */
	public boolean isTypeNoPad() {
		return (value & IMAGE_SCN_TYPE_NO_PAD) != 0;
	}

	/**
	 * Checks whether {@link #IMAGE_SCN_CNT_CODE} is set.
	 *
	 * @return {@code true} if set, {@code false} otherwise
	 */
	public boolean isCntCode() {
		return (value & IMAGE_SCN_CNT_CODE) != 0;
	}

	/**
	 * Checks whether {@link #IMAGE_SCN_CNT_INITIALIZED_DATA} is set.
	 *
	 * @return {@code true} if set, {@code false} otherwise
	 */
	public boolean isCntInitializedData() {
		return (value & IMAGE_SCN_CNT_INITIALIZED_DATA) != 0;
	}

	/**
	 * Checks whether {@link #IMAGE_SCN_CNT_UNINITIALIZED_DATA} is set.
	 *
	 * @return {@code true} if set, {@code false} otherwise
	 */
	public boolean isCntUninitializedData() {
		return (value & IMAGE_SCN_CNT_UNINITIALIZED_DATA) != 0;
	}

	/**
	 * Checks whether {@link #IMAGE_SCN_LNK_OTHER} is set.
	 *
	 * @return {@code true} if set, {@code false} otherwise
	 */
	public boolean isLnkOther() {
		return (value & IMAGE_SCN_LNK_OTHER) != 0;
	}

	/**
	 * Checks whether {@link #IMAGE_SCN_LNK_INFO} is set.
	 *
	 * @return {@code true} if set, {@code false} otherwise
	 */
	public boolean isLnkInfo() {
		return (value & IMAGE_SCN_LNK_INFO) != 0;
	}

	/**
	 * Checks whether {@link #IMAGE_SCN_LNK_REMOVE} is set.
	 *
	 * @return {@code true} if set, {@code false} otherwise
	 */
	public boolean isLnkRemove() {
		return (value & IMAGE_SCN_LNK_REMOVE) != 0;
	}

	/**
	 * Checks whether {@link #IMAGE_SCN_LNK_COMDAT} is set.
	 *
	 * @return {@code true} if set, {@code false} otherwise
	 */
	public boolean isLnkComdat() {
		return (value & IMAGE_SCN_LNK_COMDAT) != 0;
	}

	/**
	 * Checks whether {@link #IMAGE_SCN_GPREL} is set.
	 *
	 * @return {@code true} if set, {@code false} otherwise
	 */
	public boolean isGprel() {
		return (value & IMAGE_SCN_GPREL) != 0;
	}

	/**
	 * Checks whether {@link #IMAGE_SCN_MEM_PURGEABLE} is set.
	 *
	 * @return {@code true} if set, {@code false} otherwise
	 */
	public boolean isMemPurgeable() {
		return (value & IMAGE_SCN_MEM_PURGEABLE) != 0;
	}

	/**
	 * Checks whether {@link #IMAGE_SCN_MEM_16BIT} is set.
	 *
	 * @return {@code true} if set, {@code false} otherwise
	 */
	public boolean isMem16bit() {
		return (value & IMAGE_SCN_MEM_16BIT) != 0;
	}

	/**
	 * Checks whether {@link #IMAGE_SCN_MEM_LOCKED} is set.
	 *
	 * @return {@code true} if set, {@code false} otherwise
	 */
	public boolean isMemLocked() {
		return (value & IMAGE_SCN_MEM_LOCKED) != 0;
	}

	/**
	 * Checks whether {@link #IMAGE_SCN_MEM_PRELOAD} is set.
	 *
	 * @return {@code true} if set, {@code false} otherwise
	 */
	public boolean isMemPreload() {
		return (value & IMAGE_SCN_MEM_PRELOAD) != 0;
	}

	/**
	 * Gets the decoded section alignment in bytes.
	 *
	 * @return alignment in bytes, or 0 when no alignment flag is set
	 */
	public int getAlignBytes() {
		switch (value & IMAGE_SCN_ALIGN_MASK) {
			case 0:
				return 0;
			case IMAGE_SCN_ALIGN_1BYTES:
				return 1;
			case IMAGE_SCN_ALIGN_2BYTES:
				return 2;
			case IMAGE_SCN_ALIGN_4BYTES:
				return 4;
			case IMAGE_SCN_ALIGN_8BYTES:
				return 8;
			case IMAGE_SCN_ALIGN_16BYTES:
				return 16;
			case IMAGE_SCN_ALIGN_32BYTES:
				return 32;
			case IMAGE_SCN_ALIGN_64BYTES:
				return 64;
			case IMAGE_SCN_ALIGN_128BYTES:
				return 128;
			case IMAGE_SCN_ALIGN_256BYTES:
				return 256;
			case IMAGE_SCN_ALIGN_512BYTES:
				return 512;
			case IMAGE_SCN_ALIGN_1024BYTES:
				return 1024;
			case IMAGE_SCN_ALIGN_2048BYTES:
				return 2048;
			case IMAGE_SCN_ALIGN_4096BYTES:
				return 4096;
			case IMAGE_SCN_ALIGN_8192BYTES:
				return 8192;
			default:
				throw new RuntimeException();
		}
	}

	/**
	 * Checks whether {@link #IMAGE_SCN_LNK_NRELOC_OVFL} is set.
	 *
	 * @return {@code true} if set, {@code false} otherwise
	 */
	public boolean hasLnkNrelocOvfl() {
		return (value & IMAGE_SCN_LNK_NRELOC_OVFL) != 0;
	}

	/**
	 * Checks whether {@link #IMAGE_SCN_MEM_DISCARDABLE} is set.
	 *
	 * @return {@code true} if set, {@code false} otherwise
	 */
	public boolean isMemDiscardable() {
		return (value & IMAGE_SCN_MEM_DISCARDABLE) != 0;
	}

	/**
	 * Checks whether {@link #IMAGE_SCN_MEM_NOT_CACHED} is set.
	 *
	 * @return {@code true} if set, {@code false} otherwise
	 */
	public boolean isMemNotCached() {
		return (value & IMAGE_SCN_MEM_NOT_CACHED) != 0;
	}

	/**
	 * Checks whether {@link #IMAGE_SCN_MEM_NOT_PAGED} is set.
	 *
	 * @return {@code true} if set, {@code false} otherwise
	 */
	public boolean isMemNotPaged() {
		return (value & IMAGE_SCN_MEM_NOT_PAGED) != 0;
	}

	/**
	 * Checks whether {@link #IMAGE_SCN_MEM_SHARED} is set.
	 *
	 * @return {@code true} if set, {@code false} otherwise
	 */
	public boolean isMemShared() {
		return (value & IMAGE_SCN_MEM_SHARED) != 0;
	}

	/**
	 * Checks whether {@link #IMAGE_SCN_MEM_EXECUTE} is set.
	 *
	 * @return {@code true} if set, {@code false} otherwise
	 */
	public boolean isMemExecute() {
		return (value & IMAGE_SCN_MEM_EXECUTE) != 0;
	}

	/**
	 * Checks whether {@link #IMAGE_SCN_MEM_READ} is set.
	 *
	 * @return {@code true} if set, {@code false} otherwise
	 */
	public boolean isMemRead() {
		return (value & IMAGE_SCN_MEM_READ) != 0;
	}

	/**
	 * Checks whether {@link #IMAGE_SCN_MEM_WRITE} is set.
	 *
	 * @return {@code true} if set, {@code false} otherwise
	 */
	public boolean isMemWrite() {
		return (value & IMAGE_SCN_MEM_WRITE) != 0;
	}
}
