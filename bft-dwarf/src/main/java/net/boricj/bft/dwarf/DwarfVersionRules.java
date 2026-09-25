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

import net.boricj.bft.dwarf.constants.DwarfForm;
import net.boricj.bft.dwarf.constants.DwarfVersion;

/**
 * Version constraints for DWARF entities.
 */
public final class DwarfVersionRules {
	private DwarfVersionRules() {
		// Utility class - prevent instantiation
	}

	/**
	 * Validates that a form can be encoded in the requested DWARF version.
	 *
	 * @param version target artifact version
	 * @param form form being emitted
	 * @throws IllegalArgumentException when form is not supported in that version
	 */
	public static void requireSupportedForm(DwarfVersion version, DwarfForm form) {
		Objects.requireNonNull(version);
		Objects.requireNonNull(form);

		if (!form.isSupportedIn(version)) {
			String msg = "Form " + form + " requires DWARF "
					+ form.getIntroducedIn().getValue() + "+, cannot encode in DWARF " + version.getValue();
			throw new IllegalArgumentException(msg);
		}
	}
}
