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

public enum CodeViewProcessor {
	CV_CFL_8080((byte) 0x00, "8080"),
	CV_CFL_8086((byte) 0x01, "8086"),
	CV_CFL_80286((byte) 0x02, "80286"),
	CV_CFL_80386((byte) 0x03, "80386"),
	CV_CFL_80486((byte) 0x04, "80486"),
	CV_CFL_PENTIUM((byte) 0x05, "Pentium"),
	CV_CFL_PENTIUMII((byte) 0x06, "Pentium Pro/Pentium II"),
	CV_CFL_PENTIUMIII((byte) 0x07, "Pentium III"),

	CV_CFL_MIPS((byte) 0x10, "MIPS (Generic)"),
	CV_CFL_MIPS16((byte) 0x11, "MIPS16"),
	CV_CFL_MIPS32((byte) 0x12, "MIPS32"),
	CV_CFL_MIPS64((byte) 0x13, "MIPS64"),
	CV_CFL_MIPSI((byte) 0x14, "MIPS I"),
	CV_CFL_MIPSII((byte) 0x15, "MIPS II"),
	CV_CFL_MIPSIII((byte) 0x16, "MIPS III"),
	CV_CFL_MIPSIV((byte) 0x17, "MIPS IV"),
	CV_CFL_MIPSV((byte) 0x18, "MIPS V"),

	CV_CFL_M68000((byte) 0x20, "M68000"),
	CV_CFL_M68010((byte) 0x21, "M68010"),
	CV_CFL_M68020((byte) 0x22, "M68020"),
	CV_CFL_M68030((byte) 0x23, "M68030"),
	CV_CFL_M68040((byte) 0x24, "M68040"),

	CV_CFL_ALPHA_21064((byte) 0x30, "Alpha 21064"),
	CV_CFL_ALPHA_21164((byte) 0x31, "Alpha 21164"),
	CV_CFL_ALPHA_21164A((byte) 0x32, "Alpha 21164A"),
	CV_CFL_ALPHA_21264((byte) 0x33, "Alpha 21264"),
	CV_CFL_ALPHA_21364((byte) 0x34, "Alpha 21364"),

	CV_CFL_PPC601((byte) 0x40, "PPC 601"),
	CV_CFL_PPC603((byte) 0x41, "PPC 603"),
	CV_CFL_PPC604((byte) 0x42, "PPC 604"),
	CV_CFL_PPC620((byte) 0x43, "PPC 620"),
	CV_CFL_PPCFP((byte) 0x44, "PPC w/FP"),
	CV_CFL_PPCBE((byte) 0x45, "PPC (Big Endian)"),

	CV_CFL_SH3((byte) 0x50, "SH3"),
	CV_CFL_SH3E((byte) 0x51, "SH3E"),
	CV_CFL_SH3DSP((byte) 0x52, "SH3DSP"),
	CV_CFL_SH4((byte) 0x53, "SH4"),
	CV_CFL_SHMEDIA((byte) 0x54, "SHmedia"),

	CV_CFL_ARM3((byte) 0x60, "ARMv3 (CE)"),
	CV_CFL_ARM4((byte) 0x61, "ARMv4 (CE)"),
	CV_CFL_ARM4T((byte) 0x62, "ARMv4T (CE)"),
	CV_CFL_ARM5((byte) 0x63, "ARMv5 (CE)"),
	CV_CFL_ARM5T((byte) 0x64, "ARMv5T (CE)"),
	CV_CFL_ARM6((byte) 0x65, "ARMv6 (CE)"),
	CV_CFL_ARM_XMAC((byte) 0x66, "ARM (XMAC) (CE)"),
	CV_CFL_ARM_WMMX((byte) 0x67, "ARM (WMMX) (CE)"),
	CV_CFL_ARM7((byte) 0x68, "ARMv7 (CE)"),

	CV_CFL_OMNI((byte) 0x70, "Omni"),

	CV_CFL_IA64_1((byte) 0x80, "Itanium"),
	CV_CFL_IA64_2((byte) 0x81, "Itanium (byte) McKinley)"),

	CV_CFL_CEE((byte) 0x90, "CEE"),

	CV_CFL_AM33((byte) 0xa0, "AM33"),

	CV_CFL_M32R((byte) 0xb0, "M32R"),

	CV_CFL_TRICORE((byte) 0xc0, "TriCore"),

	CV_CFL_AMD64((byte) 0xd0, "x64"),

	CV_CFL_EBC((byte) 0xe0, "EBC"),

	CV_CFL_THUMB((byte) 0xf0, "Thumb (byte) CE)"),
	CV_CFL_ARMNT((byte) 0xf4, "ARM"),
	CV_CFL_ARM64((byte) 0xf5, "ARM64");

	private final byte value;
	private final String name;

	CodeViewProcessor(byte value, String name) {
		this.value = value;
		this.name = name;
	}

	public byte getValue() {
		return value;
	}

	public String getName() {
		return name;
	}

	public static CodeViewProcessor valueFrom(byte value) {
		for (CodeViewProcessor processor : values()) {
			if (processor.getValue() == value) {
				return processor;
			}
		}

		throw new IllegalArgumentException();
	}
}
