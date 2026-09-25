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
package net.boricj.bft.codeview.constants;

/**
 * CodeView processor identifiers used by compile metadata.
 */
public enum CodeViewProcessor {
	/** Intel 8080. */
	CV_CFL_8080((byte) 0x00, "8080"),
	/** Intel 8086. */
	CV_CFL_8086((byte) 0x01, "8086"),
	/** Intel 80286. */
	CV_CFL_80286((byte) 0x02, "80286"),
	/** Intel 80386. */
	CV_CFL_80386((byte) 0x03, "80386"),
	/** Intel 80486. */
	CV_CFL_80486((byte) 0x04, "80486"),
	/** Intel Pentium. */
	CV_CFL_PENTIUM((byte) 0x05, "Pentium"),
	/** Intel Pentium Pro/Pentium II. */
	CV_CFL_PENTIUMII((byte) 0x06, "Pentium Pro/Pentium II"),
	/** Intel Pentium III. */
	CV_CFL_PENTIUMIII((byte) 0x07, "Pentium III"),

	/** Generic MIPS. */
	CV_CFL_MIPS((byte) 0x10, "MIPS (Generic)"),
	/** MIPS16. */
	CV_CFL_MIPS16((byte) 0x11, "MIPS16"),
	/** MIPS32. */
	CV_CFL_MIPS32((byte) 0x12, "MIPS32"),
	/** MIPS64. */
	CV_CFL_MIPS64((byte) 0x13, "MIPS64"),
	/** MIPS I. */
	CV_CFL_MIPSI((byte) 0x14, "MIPS I"),
	/** MIPS II. */
	CV_CFL_MIPSII((byte) 0x15, "MIPS II"),
	/** MIPS III. */
	CV_CFL_MIPSIII((byte) 0x16, "MIPS III"),
	/** MIPS IV. */
	CV_CFL_MIPSIV((byte) 0x17, "MIPS IV"),
	/** MIPS V. */
	CV_CFL_MIPSV((byte) 0x18, "MIPS V"),

	/** Motorola 68000. */
	CV_CFL_M68000((byte) 0x20, "M68000"),
	/** Motorola 68010. */
	CV_CFL_M68010((byte) 0x21, "M68010"),
	/** Motorola 68020. */
	CV_CFL_M68020((byte) 0x22, "M68020"),
	/** Motorola 68030. */
	CV_CFL_M68030((byte) 0x23, "M68030"),
	/** Motorola 68040. */
	CV_CFL_M68040((byte) 0x24, "M68040"),

	/** Alpha 21064. */
	CV_CFL_ALPHA_21064((byte) 0x30, "Alpha 21064"),
	/** Alpha 21164. */
	CV_CFL_ALPHA_21164((byte) 0x31, "Alpha 21164"),
	/** Alpha 21164A. */
	CV_CFL_ALPHA_21164A((byte) 0x32, "Alpha 21164A"),
	/** Alpha 21264. */
	CV_CFL_ALPHA_21264((byte) 0x33, "Alpha 21264"),
	/** Alpha 21364. */
	CV_CFL_ALPHA_21364((byte) 0x34, "Alpha 21364"),

	/** PowerPC 601. */
	CV_CFL_PPC601((byte) 0x40, "PPC 601"),
	/** PowerPC 603. */
	CV_CFL_PPC603((byte) 0x41, "PPC 603"),
	/** PowerPC 604. */
	CV_CFL_PPC604((byte) 0x42, "PPC 604"),
	/** PowerPC 620. */
	CV_CFL_PPC620((byte) 0x43, "PPC 620"),
	/** PowerPC with floating point support. */
	CV_CFL_PPCFP((byte) 0x44, "PPC w/FP"),
	/** Big-endian PowerPC. */
	CV_CFL_PPCBE((byte) 0x45, "PPC (Big Endian)"),

	/** SH3. */
	CV_CFL_SH3((byte) 0x50, "SH3"),
	/** SH3E. */
	CV_CFL_SH3E((byte) 0x51, "SH3E"),
	/** SH3 DSP. */
	CV_CFL_SH3DSP((byte) 0x52, "SH3DSP"),
	/** SH4. */
	CV_CFL_SH4((byte) 0x53, "SH4"),
	/** SHmedia. */
	CV_CFL_SHMEDIA((byte) 0x54, "SHmedia"),

	/** ARMv3 (Windows CE). */
	CV_CFL_ARM3((byte) 0x60, "ARMv3 (CE)"),
	/** ARMv4 (Windows CE). */
	CV_CFL_ARM4((byte) 0x61, "ARMv4 (CE)"),
	/** ARMv4T (Windows CE). */
	CV_CFL_ARM4T((byte) 0x62, "ARMv4T (CE)"),
	/** ARMv5 (Windows CE). */
	CV_CFL_ARM5((byte) 0x63, "ARMv5 (CE)"),
	/** ARMv5T (Windows CE). */
	CV_CFL_ARM5T((byte) 0x64, "ARMv5T (CE)"),
	/** ARMv6 (Windows CE). */
	CV_CFL_ARM6((byte) 0x65, "ARMv6 (CE)"),
	/** ARM XMAC extension (Windows CE). */
	CV_CFL_ARM_XMAC((byte) 0x66, "ARM (XMAC) (CE)"),
	/** ARM WMMX extension (Windows CE). */
	CV_CFL_ARM_WMMX((byte) 0x67, "ARM (WMMX) (CE)"),
	/** ARMv7 (Windows CE). */
	CV_CFL_ARM7((byte) 0x68, "ARMv7 (CE)"),

	/** Omni architecture. */
	CV_CFL_OMNI((byte) 0x70, "Omni"),

	/** Itanium generation 1. */
	CV_CFL_IA64_1((byte) 0x80, "Itanium"),
	/** Itanium generation 2 (McKinley). */
	CV_CFL_IA64_2((byte) 0x81, "Itanium (byte) McKinley)"),

	/** Common Execution Environment. */
	CV_CFL_CEE((byte) 0x90, "CEE"),

	/** AM33. */
	CV_CFL_AM33((byte) 0xa0, "AM33"),

	/** M32R. */
	CV_CFL_M32R((byte) 0xb0, "M32R"),

	/** TriCore. */
	CV_CFL_TRICORE((byte) 0xc0, "TriCore"),

	/** AMD64 / x86-64. */
	CV_CFL_AMD64((byte) 0xd0, "x64"),

	/** EFI byte code. */
	CV_CFL_EBC((byte) 0xe0, "EBC"),

	/** Thumb (Windows CE). */
	CV_CFL_THUMB((byte) 0xf0, "Thumb (byte) CE)"),
	/** ARM NT. */
	CV_CFL_ARMNT((byte) 0xf4, "ARM"),
	/** ARM64. */
	CV_CFL_ARM64((byte) 0xf5, "ARM64");

	private final byte value;
	private final String name;

	CodeViewProcessor(byte value, String name) {
		this.value = value;
		this.name = name;
	}

	/**
	 * Returns the raw CodeView processor identifier.
	 *
	 * @return raw encoded processor identifier
	 */
	public byte getValue() {
		return value;
	}

	/**
	 * Returns a human-readable processor name.
	 *
	 * @return display name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Resolves a raw processor identifier.
	 *
	 * @param value raw encoded processor identifier
	 * @return resolved processor enum constant
	 * @throws IllegalArgumentException if the identifier is unknown
	 */
	public static CodeViewProcessor valueFrom(byte value) {
		for (CodeViewProcessor processor : values()) {
			if (processor.getValue() == value) {
				return processor;
			}
		}

		throw new IllegalArgumentException();
	}
}
