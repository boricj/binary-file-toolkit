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

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.dwarf.constants.DwarfAttributeName;
import net.boricj.bft.dwarf.constants.DwarfForm;
import net.boricj.bft.dwarf.constants.DwarfTag;
import net.boricj.bft.dwarf.constants.DwarfVersion;
import net.boricj.bft.dwarf.model.DwarfAbbreviationAttribute;
import net.boricj.bft.dwarf.model.DwarfAbbreviationDeclaration;
import net.boricj.bft.dwarf.model.DwarfAbbreviationTable;

/**
 * Parser for DWARF abbreviation declarations from .debug_abbrev.
 */
public final class DwarfAbbreviationParser {
	private DwarfAbbreviationParser() {
		// Utility class - prevent instantiation
	}

	/**
	 * Parses the first abbreviation table from .debug_abbrev bytes.
	 *
	 * <p>The first table ends at an abbreviation code equal to 0.
	 *
	 * @param debugAbbrev .debug_abbrev bytes
	 * @param version DWARF version to enforce form legality
	 * @param byteOrder endianness of enclosing object format
	 * @return parsed abbreviation table
	 * @throws IOException if parsing fails
	 */
	public static DwarfAbbreviationTable parseFirstTable(byte[] debugAbbrev, DwarfVersion version, ByteOrder byteOrder)
			throws IOException {
		Objects.requireNonNull(debugAbbrev);
		Objects.requireNonNull(version);
		Objects.requireNonNull(byteOrder);

		ByteInputStream bis = new ByteInputStream(new ByteArrayInputStream(debugAbbrev), byteOrder);
		Map<Long, DwarfAbbreviationDeclaration> declarationsByCode = new LinkedHashMap<>();

		while (bis.available() > 0) {
			long code = Leb128Utils.readUleb128(bis);
			if (code == 0) {
				break;
			}

			long tagCode = Leb128Utils.readUleb128(bis);
			DwarfTag tag = DwarfTag.valueFromOrNull(tagCode);
			int hasChildrenByte = bis.readUnsignedByte();
			if (hasChildrenByte != 0 && hasChildrenByte != 1) {
				throw new EOFException("Invalid children flag in abbreviation entry: " + hasChildrenByte);
			}

			List<DwarfAbbreviationAttribute> attributes = new ArrayList<>();
			while (true) {
				long attributeCode = Leb128Utils.readUleb128(bis);
				long formCode = Leb128Utils.readUleb128(bis);
				if (attributeCode == 0 && formCode == 0) {
					break;
				}

				DwarfForm form = DwarfForm.valueFrom((int) formCode);
				DwarfVersionRules.requireSupportedForm(version, form);
				DwarfAttributeName attributeName = DwarfAttributeName.valueFromOrNull(attributeCode);

				Long implicitConstValue = null;
				if (form == DwarfForm.DW_FORM_implicit_const) {
					implicitConstValue = Leb128Utils.readSleb128(bis);
				}

				attributes.add(new DwarfAbbreviationAttribute(attributeCode, attributeName, form, implicitConstValue));
			}

			declarationsByCode.put(
					code, new DwarfAbbreviationDeclaration(code, tagCode, tag, hasChildrenByte == 1, attributes));
		}

		return new DwarfAbbreviationTable(declarationsByCode);
	}
}
