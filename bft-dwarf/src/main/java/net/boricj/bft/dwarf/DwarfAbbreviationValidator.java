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

import java.util.Objects;

import net.boricj.bft.dwarf.constants.DwarfVersion;
import net.boricj.bft.dwarf.model.DwarfAbbreviationAttribute;
import net.boricj.bft.dwarf.model.DwarfAbbreviationDeclaration;
import net.boricj.bft.dwarf.model.DwarfAbbreviationTable;

/**
 * Validates that a DWARF abbreviation table is legal for a target DWARF version.
 */
public final class DwarfAbbreviationValidator {
	private DwarfAbbreviationValidator() {
		// Utility class - prevent instantiation
	}

	/**
	 * Verifies that all attribute forms in an abbreviation table are legal for the target version.
	 *
	 * @param table abbreviation table to validate
	 * @param version target DWARF version
	 */
	public static void validateSupportedIn(DwarfAbbreviationTable table, DwarfVersion version) {
		Objects.requireNonNull(table);
		Objects.requireNonNull(version);

		for (DwarfAbbreviationDeclaration declaration :
				table.getDeclarationsByCode().values()) {
			for (DwarfAbbreviationAttribute attribute : declaration.getAttributes()) {
				DwarfVersionRules.requireSupportedForm(version, attribute.getForm());
			}
		}
	}
}
