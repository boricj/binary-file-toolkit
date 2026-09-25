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
package net.boricj.bft.dwarf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.boricj.bft.dwarf.constants.DwarfForm;
import net.boricj.bft.dwarf.constants.DwarfVersion;

public class DwarfVersionRulesTest {
	@Test
	public void testRejectsDwarf4FormInDwarf2() {
		Assertions.assertThrows(
				IllegalArgumentException.class,
				() -> DwarfVersionRules.requireSupportedForm(DwarfVersion.DWARF2, DwarfForm.DW_FORM_exprloc));
	}

	@Test
	public void testRejectsDwarf5FormInDwarf4() {
		Assertions.assertThrows(
				IllegalArgumentException.class,
				() -> DwarfVersionRules.requireSupportedForm(DwarfVersion.DWARF4, DwarfForm.DW_FORM_strx));
	}

	@Test
	public void testAcceptsDwarf2BaseFormsInAllSupportedVersions() {
		for (DwarfVersion version : DwarfVersion.values()) {
			DwarfVersionRules.requireSupportedForm(version, DwarfForm.DW_FORM_data4);
		}
	}
}
