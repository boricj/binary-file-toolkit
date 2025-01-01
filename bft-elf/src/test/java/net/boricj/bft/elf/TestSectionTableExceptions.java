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

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import net.boricj.bft.elf.sections.ElfNoBits;
import net.boricj.bft.elf.sections.ElfNullSection;

import static net.boricj.bft.elf.constants.ElfClass.ELFCLASSNONE;
import static net.boricj.bft.elf.constants.ElfData.ELFDATANONE;
import static net.boricj.bft.elf.constants.ElfMachine.EM_NONE;
import static net.boricj.bft.elf.constants.ElfOsAbi.ELFOSABI_NONE;
import static net.boricj.bft.elf.constants.ElfSectionNames._BSS;
import static net.boricj.bft.elf.constants.ElfType.ET_NONE;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestSectionTableExceptions {
	@Test
	public void testBadElfFile() {
		ElfFile elf = new ElfFile.Builder(ELFCLASSNONE, ELFDATANONE, ELFOSABI_NONE, ET_NONE, EM_NONE).build();
		ElfFile otherElf = new ElfFile.Builder(ELFCLASSNONE, ELFDATANONE, ELFOSABI_NONE, ET_NONE, EM_NONE).build();
		ElfSectionTable sectionTable = elf.addSectionTable();
		ElfSection section = new ElfNullSection(otherElf);
		assertThrows(NoSuchElementException.class, () -> sectionTable.add(section));
	}

	@Test
	public void testBadFirstSection() {
		ElfFile elf = new ElfFile.Builder(ELFCLASSNONE, ELFDATANONE, ELFOSABI_NONE, ET_NONE, EM_NONE).build();
		ElfSectionTable sectionTable = elf.addSectionTable();
		ElfSection section = new ElfNoBits(elf, _BSS, new ElfSectionFlags(), 1, 4);
		assertThrows(IllegalArgumentException.class, () -> sectionTable.add(section));
	}

	@Test
	public void testDoubleAdd() {
		ElfFile elf = new ElfFile.Builder(ELFCLASSNONE, ELFDATANONE, ELFOSABI_NONE, ET_NONE, EM_NONE).build();
		ElfSectionTable sectionTable = elf.addSectionTable();
		ElfSection section = new ElfNullSection(elf);
		sectionTable.add(section);
		assertThrows(IllegalStateException.class, () -> sectionTable.add(section));
	}
}
