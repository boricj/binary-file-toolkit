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
package net.boricj.bft.ctf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;

/**
 * Represents the CTF type section containing type records.
 * Each type record has a kind (leaf type) and variable-length data.
 */
public class CTFTypeSection {
	// rawData is no longer stored; section reconstructed from objects
	private List<CTFType> types = new ArrayList<>();

	/**
	 * Creates an empty type section model.
	 */
	public CTFTypeSection() {}

	/**
	 * Parses the dictionary type section.
	 *
	 * @param fullData dictionary payload bytes after the header
	 * @param header parsed CTF header
	 * @param strings parsed string section
	 * @throws IOException if data cannot be read
	 * @throws CTFException if section bounds are invalid or records are malformed
	 */
	public void parse(byte[] fullData, CTFHeader header, CTFStringSection strings) throws IOException, CTFException {
		int offset = header.getTypeOffset();
		int size = header.getTypeSize();

		if (offset == 0 && size == 0) {
			// empty section
			return;
		}

		if (offset < 0 || size < 0 || offset + size > fullData.length) {
			throw new CTFException("Invalid type section offset or size");
		}

		byte[] data = new byte[size];
		System.arraycopy(fullData, offset, data, 0, size);

		// Parse type records
		ByteInputStream typeStream = ByteInputStream.asLittleEndian(data);
		int typeId = 0x1; // Type IDs start at 1
		while (typeStream.available() > 0) {
			CTFType type = CTFType.parse(typeStream, typeId, strings);
			if (type != null) {
				types.add(type);
				typeId++;
			} else {
				// Insufficient trailing bytes for a complete type record.
				break;
			}
		}
	}

	/**
	 * Writes all parsed type records.
	 *
	 * @param out destination stream
	 * @throws IOException if writing fails
	 */
	public void write(ByteOutputStream out) throws IOException {
		for (CTFType t : types) {
			out.write(t.write());
		}
	}

	/**
	 * Returns all parsed type records.
	 *
	 * @return mutable list of type records
	 */
	public List<CTFType> getTypes() {
		return types;
	}

	/**
	 * Looks up a type by one-based CTF type identifier.
	 *
	 * @param typeId one-based type identifier
	 * @return matching type record, or {@code null} when not found
	 */
	public CTFType getType(int typeId) {
		if (typeId < 1 || typeId > types.size()) {
			return null;
		}
		return types.get(typeId - 1);
	}

	// no rawData accessors any more
}
