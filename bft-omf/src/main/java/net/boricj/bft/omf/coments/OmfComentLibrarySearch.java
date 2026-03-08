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
package net.boricj.bft.omf.coments;

import java.io.IOException;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.omf.OmfComent;
import net.boricj.bft.omf.constants.OmfComentClass;

/**
 * LIBRARY_SEARCH comment (class 0xE8) - Contains source file metadata and timestamps.
 * Structure: 4-byte timestamp + optional 2-byte additional data (often zeros)
 */
public class OmfComentLibrarySearch extends OmfComent {
	private final long timestamp;
	private final byte[] additionalData;

	/**
	 * Parses a LIBRARY_SEARCH comment from the input stream.
	 *
	 * @param bis the input stream
	 * @throws IOException if an I/O error occurs
	 */
	public OmfComentLibrarySearch(ByteInputStream bis) throws IOException {
		super(OmfComentClass.LIBRARY_SEARCH);

		byte[] allData = bis.readAllBytes();
		if (allData.length < 4) {
			throw new IOException("LIBRARY_SEARCH data too short (minimum 4 bytes for timestamp)");
		}

		ByteInputStream dataBis = ByteInputStream.asLittleEndian(allData);
		this.timestamp = dataBis.readInt() & 0xFFFFFFFFL;
		this.additionalData = dataBis.readAllBytes();
	}

	/**
	 * Creates a new LIBRARY_SEARCH comment.
	 *
	 * @param timestamp the timestamp value
	 * @param additionalData additional data bytes
	 */
	public OmfComentLibrarySearch(long timestamp, byte[] additionalData) {
		super(OmfComentClass.LIBRARY_SEARCH);
		this.timestamp = timestamp;
		this.additionalData = additionalData != null ? additionalData.clone() : new byte[0];
	}

	/**
	 * Creates a new LIBRARY_SEARCH comment with no additional data.
	 *
	 * @param timestamp the timestamp value
	 */
	public OmfComentLibrarySearch(long timestamp) {
		this(timestamp, new byte[0]);
	}

	/**
	 * Returns the timestamp value.
	 *
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Returns the additional data bytes.
	 *
	 * @return the additional data bytes
	 */
	public byte[] getAdditionalData() {
		return additionalData.clone();
	}

	@Override
	public void write(ByteOutputStream bos) throws IOException {
		bos.writeInt((int) timestamp);
		bos.write(additionalData);
	}
}
