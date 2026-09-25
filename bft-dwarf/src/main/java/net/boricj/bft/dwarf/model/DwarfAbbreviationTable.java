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
package net.boricj.bft.dwarf.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.dwarf.Leb128Utils;
import net.boricj.bft.dwarf.constants.DwarfForm;

/**
 * Parsed DWARF abbreviation table keyed by abbreviation code.
 */
public final class DwarfAbbreviationTable {
	private final Map<Long, DwarfAbbreviationDeclaration> declarationsByCode;

	/**
	 * Creates a table with the provided declarations.
	 *
	 * @param declarationsByCode map keyed by abbreviation code
	 */
	public DwarfAbbreviationTable(Map<Long, DwarfAbbreviationDeclaration> declarationsByCode) {
		this.declarationsByCode = Collections.unmodifiableMap(new LinkedHashMap<>(declarationsByCode));
	}

	/**
	 * Returns declarations keyed by abbreviation code.
	 *
	 * @return immutable declarations map
	 */
	public Map<Long, DwarfAbbreviationDeclaration> getDeclarationsByCode() {
		return declarationsByCode;
	}

	/**
	 * Serializes this abbreviation table into bytes.
	 *
	 * @return serialized bytes
	 * @throws IOException if writing fails
	 */
	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		write(baos);
		return baos.toByteArray();
	}

	/**
	 * Writes this abbreviation table to an output stream.
	 *
	 * @param outputStream output stream
	 * @throws IOException if writing fails
	 */
	public void write(OutputStream outputStream) throws IOException {
		ByteOutputStream bos = ByteOutputStream.asLittleEndian(outputStream);
		for (DwarfAbbreviationDeclaration declaration : declarationsByCode.values()) {
			Leb128Utils.writeUleb128(bos, declaration.getCode());
			Leb128Utils.writeUleb128(bos, declaration.getTagCode());
			bos.writeByte(declaration.hasChildren() ? 1 : 0);

			for (DwarfAbbreviationAttribute attribute : declaration.getAttributes()) {
				Leb128Utils.writeUleb128(bos, attribute.getAttributeCode());
				Leb128Utils.writeUleb128(bos, attribute.getForm().getValue());
				if (attribute.getForm() == DwarfForm.DW_FORM_implicit_const) {
					Leb128Utils.writeSleb128(bos, attribute.getImplicitConstValue());
				}
			}

			bos.writeByte(0);
			bos.writeByte(0);
		}
		bos.writeByte(0);
	}
}
