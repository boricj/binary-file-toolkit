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
 * Represents the CTF function section.
 * Contains function info entries and function index entries.
 */
public class CTFFunctionSection {
	private List<CTFFunctionInfo> functions = new ArrayList<>();
	private List<Integer> indexes = new ArrayList<>();

	public static class CTFFunctionInfo {
		public int functionNameOffset;
		public int typeId;

		public CTFFunctionInfo(int nameOffset, int typeId) {
			this.functionNameOffset = nameOffset;
			this.typeId = typeId;
		}
	}

	public void parse(byte[] fullData, CTFHeader header, CTFStringSection strings, CTFTypeSection types)
			throws IOException, CTFException {
		int offset = header.getFuncOffset();
		int size = header.getFuncSize();

		if (offset == 0 && size == 0) {
			// empty
			return;
		}

		if (offset < 0 || size < 0 || offset + size > fullData.length) {
			throw new CTFException("Invalid function section offset or size");
		}

		byte[] data = new byte[size];
		System.arraycopy(fullData, offset, data, 0, size);

		// split the data into two halves: info records and index records
		int mid = size / 2;
		ByteInputStream infoStream = ByteInputStream.asLittleEndian(java.util.Arrays.copyOfRange(data, 0, mid));
		while (infoStream.available() >= 8) {
			int name = infoStream.readInt();
			int typeId = infoStream.readInt();
			functions.add(new CTFFunctionInfo(name, typeId));
		}

		ByteInputStream idxStream = ByteInputStream.asLittleEndian(java.util.Arrays.copyOfRange(data, mid, size));
		while (idxStream.available() >= 4) {
			indexes.add(idxStream.readInt());
		}
	}

	public void write(ByteOutputStream out) throws IOException {
		// write info records
		for (CTFFunctionInfo fi : functions) {
			out.writeInt(fi.functionNameOffset);
			out.writeInt(fi.typeId);
		}
		// write index entries
		for (int idx : indexes) {
			out.writeInt(idx);
		}
	}

	public List<CTFFunctionInfo> getFunctions() {
		return functions;
	}

	public List<Integer> getIndexes() {
		return indexes;
	}

    // Raw data is not stored any more; all information comes from parsed lists
}
