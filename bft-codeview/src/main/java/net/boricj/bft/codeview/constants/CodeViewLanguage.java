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
 * CodeView language identifiers used in compile flags and symbol metadata.
 */
public enum CodeViewLanguage {
	/** C language. */
	CV_CFL_C((byte) 0x00, "C"),
	/** C++ language. */
	CV_CFL_CXX((byte) 0x01, "C++"),
	/** FORTRAN language. */
	CV_CFL_FORTRAN((byte) 0x02, "FORTRAN"),
	/** MASM language. */
	CV_CFL_MASM((byte) 0x03, "MASM"),
	/** Pascal language. */
	CV_CFL_PASCAL((byte) 0x04, "Pascal"),
	/** Basic language. */
	CV_CFL_BASIC((byte) 0x05, "Basic"),
	/** COBOL language. */
	CV_CFL_COBOL((byte) 0x06, "COBOL"),
	/** Linker-generated language marker. */
	CV_CFL_LINK((byte) 0x07, "LINK"),
	/** Resource conversion tool marker. */
	CV_CFL_CVTRES((byte) 0x08, "CVTRES"),
	/** Profile-guided database conversion tool marker. */
	CV_CFL_CVTPGD((byte) 0x09, "CVTPGD"),
	/** C# language. */
	CV_CFL_CSHARP((byte) 0x0a, "C#"),
	/** Visual Basic language. */
	CV_CFL_VB((byte) 0x0b, "Visual Basic"),
	/** IL assembler language. */
	CV_CFL_ILASM((byte) 0x0c, "ILASM"),
	/** Java language. */
	CV_CFL_JAVA((byte) 0x0d, "Java"),
	/** JScript language. */
	CV_CFL_JSCRIPT((byte) 0x0e, "JScript"),
	/** Microsoft intermediate language marker. */
	CV_CFL_MSIL((byte) 0x0f, "MSIL"),
	/** HLSL language. */
	CV_CFL_HLSL((byte) 0x10, "HLSL");

	private final byte value;
	private final String name;

	CodeViewLanguage(byte value, String name) {
		this.value = value;
		this.name = name;
	}

	/**
	 * Returns the raw CodeView language identifier.
	 *
	 * @return raw encoded language identifier
	 */
	public byte getValue() {
		return value;
	}

	/**
	 * Returns a human-readable language name.
	 *
	 * @return display name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Resolves a raw language identifier.
	 *
	 * @param value raw encoded language identifier
	 * @return resolved language enum constant
	 * @throws IllegalArgumentException if the identifier is unknown
	 */
	public static CodeViewLanguage valueFrom(byte value) {
		for (CodeViewLanguage language : values()) {
			if (language.getValue() == value) {
				return language;
			}
		}

		throw new IllegalArgumentException();
	}
}
