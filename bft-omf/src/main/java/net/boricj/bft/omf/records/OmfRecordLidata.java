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
package net.boricj.bft.omf.records;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.omf.OmfFile;
import net.boricj.bft.omf.OmfRecord;
import net.boricj.bft.omf.OmfUtils;
import net.boricj.bft.omf.constants.OmfRecordType;

/**
 * LIDATA - Logical Iterated Data Record (32-bit)
 * Contains iterated (repeated) data patterns for a segment.
 */
public class OmfRecordLidata extends OmfRecord {
	private final OmfRecordSegdef segment;
	private final long dataOffset;
	private final byte[] encodedData;
	private final byte specificTypeValue;

	/**
	 * Parses an LIDATA record from the input stream.
	 *
	 * @param file the parent OMF file
	 * @param bis the input stream
	 * @throws IOException if an I/O error occurs
	 */
	public OmfRecordLidata(OmfFile file, ByteInputStream bis) throws IOException {
		super(file, OmfRecordType.LIDATA);

		byte[] rawData = bis.readAllBytes();
		int[] offset = {0};

		this.segment = file.getSegmentByIndex(OmfUtils.readIndex(rawData, offset));
		this.dataOffset = OmfUtils.u16le(rawData, offset[0]);
		offset[0] += 2;
		this.encodedData = Arrays.copyOfRange(rawData, offset[0], rawData.length);
		// Store the original type from parsing, or default to 16-bit variant
		byte parsedType = getParsingTypeValue();
		this.specificTypeValue = (parsedType != 0) ? parsedType : (byte) 0xA2;
	}

	/**
	 * Creates a new LIDATA record.
	 *
	 * @param file the parent OMF file
	 * @param segment the target segment
	 * @param dataOffset the offset within the segment
	 * @param encodedData the encoded iterated data
	 */
	public OmfRecordLidata(OmfFile file, OmfRecordSegdef segment, long dataOffset, byte[] encodedData) {
		super(file, OmfRecordType.LIDATA);
		Objects.requireNonNull(segment);
		Objects.requireNonNull(encodedData);

		this.segment = segment;
		this.dataOffset = dataOffset;
		this.encodedData = encodedData.clone();
		this.specificTypeValue = (dataOffset > 0xFFFF) ? (byte) 0xA3 : (byte) 0xA2;
	}

	@Override
	public byte getSpecificTypeValue() {
		return specificTypeValue;
	}

	/**
	 * Returns the target segment.
	 *
	 * @return the target segment
	 */
	public OmfRecordSegdef getSegment() {
		return segment;
	}

	/**
	 * Returns the offset within the segment.
	 *
	 * @return the data offset
	 */
	public long getDataOffset() {
		return dataOffset;
	}

	/**
	 * Returns the encoded iterated data.
	 *
	 * @return the encoded data bytes
	 */
	public byte[] getEncodedData() {
		return encodedData;
	}

	@Override
	public void write(ByteOutputStream bos) throws IOException {
		OmfUtils.writeIndex(bos, getFile().indexOfSegment(segment));
		bos.writeShort((int) dataOffset);
		bos.write(encodedData);
	}
}
