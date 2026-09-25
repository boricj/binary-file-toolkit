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
package net.boricj.bft.image.elf;

import org.junit.jupiter.api.Test;

import net.boricj.bft.elf.constants.ElfType;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElfImporterSymbolTablePolicyTest {
	@Test
	void importsAllSymbolTablesForRelocatableObjects() {
		assertTrue(ElfImporter.shouldImportSymbolTable(ElfType.ET_REL, ".symtab", true));
		assertTrue(ElfImporter.shouldImportSymbolTable(ElfType.ET_REL, ".dynsym", true));
		assertTrue(ElfImporter.shouldImportSymbolTable(ElfType.ET_REL, ".unknown", false));
	}

	@Test
	void prefersSymtabForExecutableOrSharedObjectsWhenAvailable() {
		assertTrue(ElfImporter.shouldImportSymbolTable(ElfType.ET_EXEC, ".symtab", true));
		assertFalse(ElfImporter.shouldImportSymbolTable(ElfType.ET_EXEC, ".dynsym", true));
		assertTrue(ElfImporter.shouldImportSymbolTable(ElfType.ET_DYN, ".symtab", true));
		assertFalse(ElfImporter.shouldImportSymbolTable(ElfType.ET_DYN, ".dynsym", true));
	}

	@Test
	void fallsBackToDynsymWhenSymtabIsUnavailable() {
		assertFalse(ElfImporter.shouldImportSymbolTable(ElfType.ET_EXEC, ".symtab", false));
		assertTrue(ElfImporter.shouldImportSymbolTable(ElfType.ET_EXEC, ".dynsym", false));
		assertFalse(ElfImporter.shouldImportSymbolTable(ElfType.ET_DYN, ".symtab", false));
		assertTrue(ElfImporter.shouldImportSymbolTable(ElfType.ET_DYN, ".dynsym", false));
	}
}
