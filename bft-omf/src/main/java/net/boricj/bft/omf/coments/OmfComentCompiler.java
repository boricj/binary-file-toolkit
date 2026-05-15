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
import java.nio.charset.StandardCharsets;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.omf.OmfComent;
import net.boricj.bft.omf.constants.OmfComentClass;

/**
 * COMPILER comment (class 0xE9) - Contains dependency file information or end-of-dependencies marker.
 * Empty data indicates an end-of-dependencies marker.
 *
 * Dependency format: timestamp (4 bytes) + Pascal string (length byte + filename)
 */
public class OmfComentCompiler extends OmfComent {
	private final Long timestamp; // null for end marker
	private final String filename; // null for end marker

	/**
	 * Parses a COMPILER comment from the input stream.
	 *
	 * @param bis the input stream
	 * @throws IOException if an I/O error occurs
	 */
	public OmfComentCompiler(ByteInputStream bis) throws IOException {
		super(OmfComentClass.COMPILER);

		byte[] dataBytes = bis.readAllBytes();

		// Parse dependency data if present
		if (dataBytes.length == 0) {
			// End marker
			this.timestamp = null;
			this.filename = null;
		} else {
			// Dependency file: timestamp (4 bytes) + string
			ByteInputStream dataBis = ByteInputStream.asLittleEndian(dataBytes);
			this.timestamp = dataBis.readInt() & 0xFFFFFFFFL;
			this.filename = dataBis.readByteLengthString(StandardCharsets.US_ASCII);
		}
	}

	/**
	 * Creates a COMPILER comment as an end-of-dependencies marker.
	 */
	public OmfComentCompiler() {
		super(OmfComentClass.COMPILER);
		this.timestamp = null;
		this.filename = null;
	}

	/**
	 * Creates a COMPILER comment for a dependency file.
	 *
	 * @param timestamp the file timestamp
	 * @param filename the dependency filename
	 */
	public OmfComentCompiler(long timestamp, String filename) {
		super(OmfComentClass.COMPILER);
		this.timestamp = timestamp;
		this.filename = filename == null ? "" : filename;
	}

	/**
	 * Returns true if this is an end-of-dependencies marker.
	 *
	 * @return true if empty data
	 */
	public boolean isEmpty() {
		return timestamp == null;
	}

	/**
	 * Returns the timestamp for dependency records.
	 *
	 * @return the timestamp, or null if this is an end marker
	 */
	public Long getTimestamp() {
		return timestamp;
	}

	/**
	 * Returns the filename for dependency records.
	 *
	 * @return the filename, or null if this is an end marker
	 */
	public String getFilename() {
		return filename;
	}

	@Override
	public void write(ByteOutputStream bos) throws IOException {
		if (timestamp != null) {
			bos.writeInt(timestamp.intValue());
			bos.writeByteLengthString(filename, StandardCharsets.US_ASCII);
		}
		// Empty for end marker
	}
}
