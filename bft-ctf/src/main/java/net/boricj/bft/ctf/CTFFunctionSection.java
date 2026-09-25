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

	/**
	 * Creates an empty function section model.
	 */
	public CTFFunctionSection() {}

	/**
	 * One function-info record in the CTF function section.
	 */
	public static class CTFFunctionInfo {
		/** Referenced function type identifier. */
		public int typeId;

		/**
		 * Creates a function-info record.
		 *
		 * @param typeId referenced function type identifier
		 */
		public CTFFunctionInfo(int typeId) {
			this.typeId = typeId;
		}
	}

	/**
	 * Parses function info and function index subsections.
	 *
	 * @param fullData dictionary payload bytes after the header
	 * @param header parsed CTF header
	 * @param strings parsed string section
	 * @param types parsed type section
	 * @throws IOException if data cannot be read
	 * @throws CTFException if section bounds are invalid
	 */
	public void parse(byte[] fullData, CTFHeader header, CTFStringSection strings, CTFTypeSection types)
			throws IOException, CTFException {
		int infoOffset = header.getFuncOffset();
		int infoSize = header.getFuncInfoSize();
		int indexOffset = header.getFuncIndexOffset();
		int indexSize = header.getFuncIndexSize();

		if (infoOffset == 0 && infoSize == 0 && indexOffset == 0 && indexSize == 0) {
			return;
		}

		if (infoOffset < 0 || infoSize < 0 || infoOffset + infoSize > fullData.length) {
			throw new CTFException("Invalid function info section offset or size");
		}
		if (indexOffset < 0 || indexSize < 0 || indexOffset + indexSize > fullData.length) {
			throw new CTFException("Invalid function index section offset or size");
		}
		if ((infoSize & 3) != 0 || (indexSize & 3) != 0) {
			throw new CTFException("Function sections must be arrays of uint32 entries");
		}

		ByteInputStream infoStream = ByteInputStream.asLittleEndian(
				java.util.Arrays.copyOfRange(fullData, infoOffset, infoOffset + infoSize));
		while (infoStream.available() >= 4) {
			functions.add(new CTFFunctionInfo(infoStream.readInt()));
		}

		ByteInputStream idxStream = ByteInputStream.asLittleEndian(
				java.util.Arrays.copyOfRange(fullData, indexOffset, indexOffset + indexSize));
		while (idxStream.available() >= 4) {
			indexes.add(idxStream.readInt());
		}
	}

	/**
	 * Writes function-info entries.
	 *
	 * @param out destination stream
	 * @throws IOException if writing fails
	 */
	public void writeInfo(ByteOutputStream out) throws IOException {
		for (CTFFunctionInfo fi : functions) {
			out.writeInt(fi.typeId);
		}
	}

	/**
	 * Writes function-index entries.
	 *
	 * @param out destination stream
	 * @throws IOException if writing fails
	 */
	public void writeIndexes(ByteOutputStream out) throws IOException {
		for (int idx : indexes) {
			out.writeInt(idx);
		}
	}

	/**
	 * Returns parsed function-info entries.
	 *
	 * @return mutable list of function-info entries
	 */
	public List<CTFFunctionInfo> getFunctions() {
		return functions;
	}

	/**
	 * Returns parsed function-index entries.
	 *
	 * @return mutable list of function indexes
	 */
	public List<Integer> getIndexes() {
		return indexes;
	}

	// Raw data is not stored any more; all information comes from parsed lists
}
