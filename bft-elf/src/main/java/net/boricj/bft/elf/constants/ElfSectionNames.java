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
package net.boricj.bft.elf.constants;

public class ElfSectionNames {
	public static final String _BSS = ".bss";
	public static final String _REL = ".rel";
	public static final String _COMMENT = ".comment";
	public static final String _DATA = ".data";
	public static final String _NOTE_GNU_STACK = ".note.GNU-stack";
	public static final String _RODATA = ".rodata";
	public static final String _SHSTRTAB = ".shstrtab";
	public static final String _STRTAB = ".strtab";
	public static final String _SYMTAB = ".symtab";
	public static final String _TEXT = ".text";

	private ElfSectionNames() {
		throw new UnsupportedOperationException();
	}
}
