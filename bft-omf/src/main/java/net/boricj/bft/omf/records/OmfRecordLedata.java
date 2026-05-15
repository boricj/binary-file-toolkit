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
 * LEDATA - Logical Enumerated Data Record (32-bit)
 * Contains data bytes for a segment.
 */
public class OmfRecordLedata extends OmfRecord {
	private final OmfRecordSegdef segment;
	private final long dataOffset;
	private final byte[] data;
	private final byte specificTypeValue;

	/**
	 * Parses an LEDATA record from the input stream.
	 *
	 * @param file the parent OMF file
	 * @param bis the input stream
	 * @throws IOException if an I/O error occurs
	 */
	public OmfRecordLedata(OmfFile file, ByteInputStream bis) throws IOException {
		super(file, OmfRecordType.LEDATA);

		byte parsedType = getParsingTypeValue();
		byte[] rawData = bis.readAllBytes();
		int[] offset = {0};

		this.segment = file.getSegmentByIndex(OmfUtils.readIndex(rawData, offset));
		if (parsedType == (byte) 0xA1) {
			this.dataOffset = OmfUtils.u32le(rawData, offset[0]);
			offset[0] += 4;
		} else {
			this.dataOffset = OmfUtils.u16le(rawData, offset[0]);
			offset[0] += 2;
		}
		this.data = Arrays.copyOfRange(rawData, offset[0], rawData.length);
		// Store the original type from parsing, or default to 16-bit variant
		this.specificTypeValue = (parsedType != 0) ? parsedType : (byte) 0xA0;
	}

	/**
	 * Creates a new LEDATA record.
	 *
	 * @param file the parent OMF file
	 * @param segment the target segment
	 * @param dataOffset the offset within the segment
	 * @param data the data bytes
	 */
	public OmfRecordLedata(OmfFile file, OmfRecordSegdef segment, long dataOffset, byte[] data) {
		super(file, OmfRecordType.LEDATA);
		Objects.requireNonNull(segment);
		Objects.requireNonNull(data);
		if (dataOffset < 0 || dataOffset > 0xFFFFFFFFL) {
			throw new IllegalArgumentException("LEDATA data offset out of range: " + dataOffset);
		}

		this.segment = segment;
		this.dataOffset = dataOffset;
		this.data = data.clone();
		this.specificTypeValue = (dataOffset > 0xFFFF) ? (byte) 0xA1 : (byte) 0xA0;
		validateDataLength("LEDATA");
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
	 * Returns the data bytes.
	 *
	 * @return the data bytes
	 */
	public byte[] getData() {
		return data;
	}

	@Override
	protected void write(ByteOutputStream bos) throws IOException {
		OmfUtils.writeIndex(bos, getFile().indexOfSegment(segment));
		if (specificTypeValue == (byte) 0xA1) {
			bos.writeInt((int) dataOffset);
		} else {
			bos.writeShort((int) dataOffset);
		}
		bos.write(data);
	}
}
