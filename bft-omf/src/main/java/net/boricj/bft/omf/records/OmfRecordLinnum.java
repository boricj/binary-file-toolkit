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
import java.util.Collections;
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

		byte[] data = bis.readAllBytes();
		int[] offset = {0};

		int groupIndex = OmfUtils.readIndex(data, offset);
		int segmentIndex = OmfUtils.readIndex(data, offset);
		// Group index can be 0 (meaning no group)
		this.group = groupIndex > 0 ? file.getGroupByIndex(groupIndex) : null;
		this.segment = segmentIndex > 0 ? file.getSegmentByIndex(segmentIndex) : null;

		List<LineNumber> parsedLineNumbers = new ArrayList<>();
		while (offset[0] < data.length) {
			int lineNum = OmfUtils.u16le(data, offset[0]);
			offset[0] += 2;
			long lineOffset = OmfUtils.u16le(data, offset[0]);
			offset[0] += 2;
			parsedLineNumbers.add(new LineNumber(lineNum, lineOffset));
		}

		this.lineNumbers = Collections.unmodifiableList(parsedLineNumbers);
		// Store the original type from parsing, or default to 16-bit variant
		byte parsedType = getParsingTypeValue();
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
		this.lineNumbers = Collections.unmodifiableList(new ArrayList<>(lineNumbers));
		this.specificTypeValue = (byte) 0x94; // Default to 16-bit variant
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
	public void write(ByteOutputStream bos) throws IOException {
		OmfUtils.writeIndex(bos, getFile().indexOfGroup(group));
		OmfUtils.writeIndex(bos, getFile().indexOfSegment(segment));

		for (LineNumber ln : lineNumbers) {
			bos.writeShort(ln.lineNumber);
			bos.writeShort((int) ln.offset);
		}
	}

	/**
	 * A line number entry mapping source line to code offset.
	 *
	 * @param lineNumber the source line number
	 * @param offset the code offset
	 */
	public record LineNumber(int lineNumber, long offset) {}
}
