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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.omf.OmfFile;
import net.boricj.bft.omf.OmfRecord;
import net.boricj.bft.omf.OmfUtils;
import net.boricj.bft.omf.constants.OmfRecordType;

/**
 * LINNUM - Line Numbers Record (32-bit)
 * Maps line numbers to code addresses.
 */
public class OmfRecordLinnum extends OmfRecord {
	private final OmfRecordGrpdef group;
	private final OmfRecordSegdef segment;
	private final List<LineNumber> lineNumbers;
	private final byte specificTypeValue;

	/**
	 * Parses a LINNUM record from the input stream.
	 *
	 * @param file the parent OMF file
	 * @param bis the input stream
	 * @throws IOException if an I/O error occurs
	 */
	public OmfRecordLinnum(OmfFile file, ByteInputStream bis) throws IOException {
		super(file, OmfRecordType.LINNUM);

		byte parsedType = getParsingTypeValue();
		boolean use32BitOffsets = parsedType == (byte) 0x95;

		byte[] data = bis.readAllBytes();
		int[] offset = {0};

		int groupIndex = OmfUtils.readIndex(data, offset);
		int segmentIndex = OmfUtils.readIndex(data, offset);
		// Group index can be 0 (meaning no group)
		this.group = groupIndex > 0 ? file.getGroupByIndex(groupIndex) : null;
		this.segment = segmentIndex > 0 ? file.getSegmentByIndex(segmentIndex) : null;

		List<LineNumber> parsedLineNumbers = new ArrayList<>();
		while (offset[0] < data.length) {
			if (offset[0] + 2 > data.length) {
				throw new IOException("Malformed LINNUM record: missing line number");
			}
			int lineNum = OmfUtils.u16le(data, offset[0]);
			offset[0] += 2;
			if (use32BitOffsets) {
				if (offset[0] + 4 > data.length) {
					throw new IOException("Malformed LINNUM record: missing 32-bit line offset");
				}
				long lineOffset = OmfUtils.u32le(data, offset[0]);
				offset[0] += 4;
				if (lineOffset > 0xFFFF) {
					throw new IOException("LINNUM 32-bit offset out of supported range: " + lineOffset);
				}
				parsedLineNumbers.add(new LineNumber(lineNum, lineOffset));
			} else {
				if (offset[0] + 2 > data.length) {
					throw new IOException("Malformed LINNUM record: missing 16-bit line offset");
				}
				long lineOffset = OmfUtils.u16le(data, offset[0]);
				offset[0] += 2;
				parsedLineNumbers.add(new LineNumber(lineNum, lineOffset));
			}
		}

		this.lineNumbers = List.copyOf(parsedLineNumbers);
		// Store the original type from parsing, or default to 16-bit variant
		this.specificTypeValue = (parsedType != 0) ? parsedType : (byte) 0x94;
	}

	/**
	 * Creates a new LINNUM record.
	 *
	 * @param file the parent OMF file
	 * @param group the group (may be null)
	 * @param segment the segment (may be null)
	 * @param lineNumbers the list of line number entries
	 */
	public OmfRecordLinnum(OmfFile file, OmfRecordGrpdef group, OmfRecordSegdef segment, List<LineNumber> lineNumbers) {
		super(file, OmfRecordType.LINNUM);
		Objects.requireNonNull(lineNumbers);
		// group and segment are nullable

		this.group = group;
		this.segment = segment;
		this.lineNumbers = List.copyOf(lineNumbers);
		this.specificTypeValue = (byte) 0x94; // Default to 16-bit variant
		validateDataLength("LINNUM");
	}

	@Override
	public byte getSpecificTypeValue() {
		return specificTypeValue;
	}

	/**
	 * Returns the associated group.
	 *
	 * @return the group, or null
	 */
	public OmfRecordGrpdef getGroup() {
		return group;
	}

	/**
	 * Returns the associated segment.
	 *
	 * @return the segment, or null
	 */
	public OmfRecordSegdef getSegment() {
		return segment;
	}

	/**
	 * Returns the list of line number entries.
	 *
	 * @return the line numbers
	 */
	public List<LineNumber> getLineNumbers() {
		return lineNumbers;
	}

	@Override
	protected void write(ByteOutputStream bos) throws IOException {
		OmfUtils.writeIndex(bos, group == null ? 0 : getFile().indexOfGroup(group));
		OmfUtils.writeIndex(bos, segment == null ? 0 : getFile().indexOfSegment(segment));

		for (LineNumber ln : lineNumbers) {
			bos.writeShort(ln.lineNumber);
			if (specificTypeValue == (byte) 0x95) {
				bos.writeInt((int) ln.offset);
			} else {
				if (ln.offset > 0xFFFF) {
					throw new IOException("LINNUM 16-bit variant cannot encode offset: " + ln.offset);
				}
				bos.writeShort((int) ln.offset);
			}
		}
	}

	/**
	 * A line number entry mapping source line to code offset.
	 *
	 * @param lineNumber the source line number
	 * @param offset the code offset
	 */
	public record LineNumber(int lineNumber, long offset) {
		/**
		 * Validates the line number entry fields.
		 */
		public LineNumber {
			if (lineNumber < 0 || lineNumber > 0xFFFF) {
				throw new IllegalArgumentException("LINNUM line number out of range: " + lineNumber);
			}

			if (offset < 0 || offset > 0xFFFF) {
				throw new IllegalArgumentException("LINNUM offset out of range: " + offset);
			}
		}
	}
}
