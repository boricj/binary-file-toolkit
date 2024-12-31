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

public class CoffSectionFlags {
	public static final int IMAGE_SCN_TYPE_NO_PAD = 0x00000008;
	public static final int IMAGE_SCN_CNT_CODE = 0x00000020;
	public static final int IMAGE_SCN_CNT_INITIALIZED_DATA = 0x00000040;
	public static final int IMAGE_SCN_CNT_UNINITIALIZED_DATA = 0x00000080;
	public static final int IMAGE_SCN_LNK_OTHER = 0x00000100;
	public static final int IMAGE_SCN_LNK_INFO = 0x00000200;
	public static final int IMAGE_SCN_LNK_REMOVE = 0x00000800;
	public static final int IMAGE_SCN_LNK_COMDAT = 0x00001000;
	public static final int IMAGE_SCN_GPREL = 0x00008000;
	public static final int IMAGE_SCN_MEM_PURGEABLE = 0x00020000;
	public static final int IMAGE_SCN_MEM_16BIT = 0x00020000;
	public static final int IMAGE_SCN_MEM_LOCKED = 0x00040000;
	public static final int IMAGE_SCN_MEM_PRELOAD = 0x00080000;
	public static final int IMAGE_SCN_ALIGN_1BYTES = 0x00100000;
	public static final int IMAGE_SCN_ALIGN_2BYTES = 0x00200000;
	public static final int IMAGE_SCN_ALIGN_4BYTES = 0x00300000;
	public static final int IMAGE_SCN_ALIGN_8BYTES = 0x00400000;
	public static final int IMAGE_SCN_ALIGN_16BYTES = 0x00500000;
	public static final int IMAGE_SCN_ALIGN_32BYTES = 0x00600000;
	public static final int IMAGE_SCN_ALIGN_64BYTES = 0x00700000;
	public static final int IMAGE_SCN_ALIGN_128BYTES = 0x00800000;
	public static final int IMAGE_SCN_ALIGN_256BYTES = 0x00900000;
	public static final int IMAGE_SCN_ALIGN_512BYTES = 0x00A00000;
	public static final int IMAGE_SCN_ALIGN_1024BYTES = 0x00B00000;
	public static final int IMAGE_SCN_ALIGN_2048BYTES = 0x00C00000;
	public static final int IMAGE_SCN_ALIGN_4096BYTES = 0x00D00000;
	public static final int IMAGE_SCN_ALIGN_8192BYTES = 0x00E00000;
	public static final int IMAGE_SCN_LNK_NRELOC_OVFL = 0x01000000;
	public static final int IMAGE_SCN_MEM_DISCARDABLE = 0x02000000;
	public static final int IMAGE_SCN_MEM_NOT_CACHED = 0x04000000;
	public static final int IMAGE_SCN_MEM_NOT_PAGED = 0x08000000;
	public static final int IMAGE_SCN_MEM_SHARED = 0x10000000;
	public static final int IMAGE_SCN_MEM_EXECUTE = 0x20000000;
	public static final int IMAGE_SCN_MEM_READ = 0x40000000;
	public static final int IMAGE_SCN_MEM_WRITE = 0x80000000;

	private static final int IMAGE_SCN_ALIGN_MASK = 0x00F00000;

	private int value;

	public CoffSectionFlags() {
		this.value = 0;
	}

	public CoffSectionFlags(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public CoffSectionFlags typeNoPad() {
		value |= IMAGE_SCN_TYPE_NO_PAD;
		return this;
	}

	public CoffSectionFlags cntCode() {
		value |= IMAGE_SCN_CNT_CODE;
		return this;
	}

	public CoffSectionFlags cntInitializedData() {
		value |= IMAGE_SCN_CNT_INITIALIZED_DATA;
		return this;
	}

	public CoffSectionFlags cntUninitializedData() {
		value |= IMAGE_SCN_CNT_UNINITIALIZED_DATA;
		return this;
	}

	public CoffSectionFlags lnkOther() {
		value |= IMAGE_SCN_LNK_OTHER;
		return this;
	}

	public CoffSectionFlags lnkInfo() {
		value |= IMAGE_SCN_LNK_INFO;
		return this;
	}

	public CoffSectionFlags lnkRemove() {
		value |= IMAGE_SCN_LNK_REMOVE;
		return this;
	}

	public CoffSectionFlags lnkComdat() {
		value |= IMAGE_SCN_LNK_COMDAT;
		return this;
	}

	public CoffSectionFlags gprel() {
		value |= IMAGE_SCN_GPREL;
		return this;
	}

	public CoffSectionFlags memPurgeable() {
		value |= IMAGE_SCN_MEM_PURGEABLE;
		return this;
	}

	public CoffSectionFlags mem16bit() {
		value |= IMAGE_SCN_MEM_16BIT;
		return this;
	}

	public CoffSectionFlags memLocked() {
		value |= IMAGE_SCN_MEM_LOCKED;
		return this;
	}

	public CoffSectionFlags memPreload() {
		value |= IMAGE_SCN_MEM_PRELOAD;
		return this;
	}

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

	public CoffSectionFlags memDiscardable() {
		value |= IMAGE_SCN_MEM_DISCARDABLE;
		return this;
	}

	public CoffSectionFlags memNotCached() {
		value |= IMAGE_SCN_MEM_NOT_CACHED;
		return this;
	}

	public CoffSectionFlags memNotPaged() {
		value |= IMAGE_SCN_MEM_NOT_PAGED;
		return this;
	}

	public CoffSectionFlags memShared() {
		value |= IMAGE_SCN_MEM_SHARED;
		return this;
	}

	public CoffSectionFlags memExecute() {
		value |= IMAGE_SCN_MEM_EXECUTE;
		return this;
	}

	public CoffSectionFlags memRead() {
		value |= IMAGE_SCN_MEM_READ;
		return this;
	}

	public CoffSectionFlags memWrite() {
		value |= IMAGE_SCN_MEM_WRITE;
		return this;
	}

	public boolean isTypeNoPad() {
		return (value & IMAGE_SCN_TYPE_NO_PAD) != 0;
	}

	public boolean isCntCode() {
		return (value & IMAGE_SCN_CNT_CODE) != 0;
	}

	public boolean isCntInitializedData() {
		return (value & IMAGE_SCN_CNT_INITIALIZED_DATA) != 0;
	}

	public boolean isCntUninitializedData() {
		return (value & IMAGE_SCN_CNT_UNINITIALIZED_DATA) != 0;
	}

	public boolean isLnkOther() {
		return (value & IMAGE_SCN_LNK_OTHER) != 0;
	}

	public boolean isLnkInfo() {
		return (value & IMAGE_SCN_LNK_INFO) != 0;
	}

	public boolean isLnkRemove() {
		return (value & IMAGE_SCN_LNK_REMOVE) != 0;
	}

	public boolean isLnkComdat() {
		return (value & IMAGE_SCN_LNK_COMDAT) != 0;
	}

	public boolean isGprel() {
		return (value & IMAGE_SCN_GPREL) != 0;
	}

	public boolean isMemPurgeable() {
		return (value & IMAGE_SCN_MEM_PURGEABLE) != 0;
	}

	public boolean isMem16bit() {
		return (value & IMAGE_SCN_MEM_16BIT) != 0;
	}

	public boolean isMemLocked() {
		return (value & IMAGE_SCN_MEM_LOCKED) != 0;
	}

	public boolean isMemPreload() {
		return (value & IMAGE_SCN_MEM_PRELOAD) != 0;
	}

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

	public boolean hasLnkNrelocOvfl() {
		return (value & IMAGE_SCN_LNK_NRELOC_OVFL) != 0;
	}

	public boolean isMemDiscardable() {
		return (value & IMAGE_SCN_MEM_DISCARDABLE) != 0;
	}

	public boolean isMemNotCached() {
		return (value & IMAGE_SCN_MEM_NOT_CACHED) != 0;
	}

	public boolean isMemNotPaged() {
		return (value & IMAGE_SCN_MEM_NOT_PAGED) != 0;
	}

	public boolean isMemShared() {
		return (value & IMAGE_SCN_MEM_SHARED) != 0;
	}

	public boolean isMemExecute() {
		return (value & IMAGE_SCN_MEM_EXECUTE) != 0;
	}

	public boolean isMemRead() {
		return (value & IMAGE_SCN_MEM_READ) != 0;
	}

	public boolean isMemWrite() {
		return (value & IMAGE_SCN_MEM_WRITE) != 0;
	}
}
