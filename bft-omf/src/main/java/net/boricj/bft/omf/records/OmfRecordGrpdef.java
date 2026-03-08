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
 * GRPDEF - Group Definition Record
 * Defines a group of segments.
 */
public class OmfRecordGrpdef extends OmfRecord {
	private final String groupName;
	private final List<OmfRecordSegdef> segments;

	/**
	 * Parses a GRPDEF record from the input stream.
	 *
	 * @param file the parent OMF file
	 * @param bis the input stream
	 * @throws IOException if an I/O error occurs
	 */
	public OmfRecordGrpdef(OmfFile file, ByteInputStream bis) throws IOException {
		super(file, OmfRecordType.GRPDEF);

		byte[] data = bis.readAllBytes();
		int[] offset = {0};
		this.groupName = file.getLnameByIndex(OmfUtils.readIndex(data, offset));

		List<OmfRecordSegdef> parsedSegments = new ArrayList<>();
		while (offset[0] < data.length) {
			// Each segment descriptor starts with 0xFF
			if ((data[offset[0]++] & 0xFF) != 0xFF) {
				throw new IOException("Invalid GRPDEF segment descriptor");
			}
			parsedSegments.add(file.getSegmentByIndex(OmfUtils.readIndex(data, offset)));
		}
		this.segments = Collections.unmodifiableList(parsedSegments);
	}

	/**
	 * Creates a new GRPDEF record.
	 *
	 * @param file the parent OMF file
	 * @param groupName the group name
	 * @param segments the list of segments in this group
	 */
	public OmfRecordGrpdef(OmfFile file, String groupName, List<OmfRecordSegdef> segments) {
		super(file, OmfRecordType.GRPDEF);
		Objects.requireNonNull(groupName);
		Objects.requireNonNull(segments);

		this.groupName = groupName;
		this.segments = Collections.unmodifiableList(new ArrayList<>(segments));
	}

	/**
	 * Returns the group name.
	 *
	 * @return the group name
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * Returns the list of segments in this group.
	 *
	 * @return the segments
	 */
	public List<OmfRecordSegdef> getSegments() {
		return segments;
	}

	@Override
	public void write(ByteOutputStream bos) throws IOException {
		OmfUtils.writeIndex(bos, getFile().indexOfLnameBefore(groupName, this));
		for (OmfRecordSegdef segment : segments) {
			bos.writeByte(0xFF);
			OmfUtils.writeIndex(bos, getFile().indexOfSegment(segment));
		}
	}
}
