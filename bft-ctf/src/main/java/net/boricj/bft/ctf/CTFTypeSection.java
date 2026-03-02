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

	public void parse(byte[] fullData, CTFHeader header, CTFStringSection strings)
			throws IOException, CTFException {
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
        System.out.printf("[TYPETEST DEBUG] Type section stream available: %d%n", typeStream.available());
        while (typeStream.available() > 0) {
            System.out.printf("[TYPETEST DEBUG] Attempting to parse typeId: %d, available bytes: %d%n", typeId, typeStream.available());
            CTFType type = CTFType.parse(typeStream, typeId, strings);
            if (type != null) {
                types.add(type);
                typeId++;
            } else {
                // If CTFType.parse returns null, it means there aren't enough bytes for a full type
                // We need to break to avoid an infinite loop
                System.err.printf("[TYPETEST ERROR] CTFType.parse returned null for typeId %d, available bytes: %d. Breaking loop.%n", typeId, typeStream.available());
                break;
            }
        }
	}

	public void write(ByteOutputStream out) throws IOException {
		for (CTFType t : types) {
			out.write(t.write());
		}
	}

	public List<CTFType> getTypes() {
		return types;
	}

	public CTFType getType(int typeId) {
		if (typeId < 1 || typeId > types.size()) {
			return null;
		}
		return types.get(typeId - 1);
	}

    // no rawData accessors any more
}
