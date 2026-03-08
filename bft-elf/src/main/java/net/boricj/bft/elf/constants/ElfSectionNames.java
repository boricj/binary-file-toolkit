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

/**
 * Standard ELF section names.
 * These constants define commonly used section names in ELF files.
 */
public class ElfSectionNames {
	/** Uninitialized data section. */
	public static final String _BSS = ".bss";
	/** Relocation section without addends. */
	public static final String _REL = ".rel";
	/** Relocation section with addends. */
	public static final String _RELA = ".rela";
	/** Comment section. */
	public static final String _COMMENT = ".comment";
	/** Initialized data section. */
	public static final String _DATA = ".data";
	/** GNU stack note section. */
	public static final String _NOTE_GNU_STACK = ".note.GNU-stack";
	/** Read-only data section. */
	public static final String _RODATA = ".rodata";
	/** Section header string table. */
	public static final String _SHSTRTAB = ".shstrtab";
	/** String table. */
	public static final String _STRTAB = ".strtab";
	/** Symbol table. */
	public static final String _SYMTAB = ".symtab";
	/** Executable code section. */
	public static final String _TEXT = ".text";

	private ElfSectionNames() {
		throw new UnsupportedOperationException();
	}
}
