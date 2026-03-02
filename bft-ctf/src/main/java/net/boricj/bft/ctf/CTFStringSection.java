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
import java.util.HashMap;
import java.util.Map;
import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;

/**
 * Represents the CTF string section.
 * Contains null-terminated strings referenced throughout the CTF data.
 */
public class CTFStringSection {
    private Map<Integer, String> stringsById = new HashMap<>();
    private int sectionSize;

	public void parse(byte[] fullData, CTFHeader header) throws IOException, CTFException {
		int offset = header.getStrOffset();
		int size = header.getStrSize();

		if (offset == 0 && size == 0) {
			// empty section
			sectionSize = 0;
			return;
		}

		if (offset < 0 || size < 0 || offset + size > fullData.length) {
			throw new CTFException("Invalid string section offset or size");
		}

		sectionSize = header.getStrSize();
		byte[] data = new byte[sectionSize];
		System.arraycopy(fullData, offset, data, 0, sectionSize);

		// Parse strings from raw data
		int stringOffset = 0;
		while (stringOffset < data.length) {
			int startOffset = stringOffset;
			StringBuilder sb = new StringBuilder();
			while (stringOffset < data.length && data[stringOffset] != 0) {
				sb.append((char) (data[stringOffset] & 0xFF));
				stringOffset++;
			}
			stringsById.put(startOffset, sb.toString());
			stringOffset++; // skip null terminator
		}
	}

	public void write(ByteOutputStream out) throws IOException {
		// rebuild the string section from the stored strings and the
		// original section size.  Offsets are relative to the start of the
		// section and must match what we parsed earlier in order to round-
		// trip successfully.
        // Calculate actual required size for the string section
        int actualSize = 0;
        for (Map.Entry<Integer, String> e : stringsById.entrySet()) {
            byte[] bs = e.getValue().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            actualSize = Math.max(actualSize, e.getKey() + bs.length + 1); // +1 for null terminator
        }

        byte[] outData = new byte[actualSize];
        for (Map.Entry<Integer, String> e : stringsById.entrySet()) {
            int off = e.getKey();
            byte[] bs = e.getValue().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            System.arraycopy(bs, 0, outData, off, bs.length);
            // Null terminators are already present due to zero-initialization of outData
        }
        out.write(outData);
    }

    public String getString(int offset) {
        return stringsById.getOrDefault(offset, "");
    }

    public Map<Integer, String> getStrings() {
        return stringsById;
    }

    public void setStrings(Map<Integer, String> strings) {
        this.stringsById = strings;
    }
}
