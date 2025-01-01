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
package net.boricj.bft.coff;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import net.boricj.bft.coff.constants.CoffMachine;
import net.boricj.bft.coff.constants.CoffSectionFlags;
import net.boricj.bft.coff.sections.CoffBytes;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestSectionTableExceptions {
	@Test
	public void testBadCoffFile() {
		CoffFile coff = new CoffFile.Builder(CoffMachine.IMAGE_FILE_MACHINE_UNKNOWN).build();
		CoffFile otherCoff = new CoffFile.Builder(CoffMachine.IMAGE_FILE_MACHINE_UNKNOWN).build();
		CoffSectionTable sectionTable = coff.getSections();
		CoffSection section = new CoffBytes(otherCoff, ".text", new CoffSectionFlags(), new byte[0]);
		assertThrows(NoSuchElementException.class, () -> sectionTable.add(section));
	}

	@Test
	public void testDoubleAdd() {
		CoffFile coff = new CoffFile.Builder(CoffMachine.IMAGE_FILE_MACHINE_UNKNOWN).build();
		CoffSectionTable sectionTable = coff.getSections();
		CoffSection section = new CoffBytes(coff, ".text", new CoffSectionFlags(), new byte[0]);
		sectionTable.add(section);
		assertThrows(IllegalStateException.class, () -> sectionTable.add(section));
	}
}
