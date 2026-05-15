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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.IndirectList;
import net.boricj.bft.omf.OmfFile;
import net.boricj.bft.omf.OmfRecord;
import net.boricj.bft.omf.OmfUtils;
import net.boricj.bft.omf.constants.OmfRecordType;

/**
 * EXTDEF - External Names Definition Record
 * Defines external symbols referenced by the module.
 */
public class OmfRecordExtdef extends OmfRecord implements IndirectList<OmfSubrecordExtdef> {
	private final List<OmfSubrecordExtdef> elements;

	/**
	 * Parses an EXTDEF record from the input stream.
	 *
	 * @param file the parent OMF file
	 * @param bis the input stream
	 * @throws IOException if an I/O error occurs
	 */
	public OmfRecordExtdef(OmfFile file, ByteInputStream bis) throws IOException {
		super(file, OmfRecordType.EXTDEF);

		List<OmfSubrecordExtdef> elements = new ArrayList<>();

		while (bis.available() > 0) {
			elements.add(new OmfSubrecordExtdef(
					bis.readByteLengthString(StandardCharsets.US_ASCII), OmfUtils.readIndex(bis)));
		}

		this.elements = List.copyOf(elements);
	}

	/**
	 * Creates a new EXTDEF record.
	 *
	 * @param file the parent OMF file
	 * @param entries EXTDEF subrecords
	 */
	public OmfRecordExtdef(OmfFile file, List<OmfSubrecordExtdef> entries) {
		super(file, OmfRecordType.EXTDEF);
		Objects.requireNonNull(entries);
		this.elements = List.copyOf(entries);
		validateDataLength("EXTDEF");
	}

	@Override
	public List<OmfSubrecordExtdef> getElements() {
		return elements;
	}

	@Override
	protected void write(ByteOutputStream bos) throws IOException {
		for (OmfSubrecordExtdef entry : elements) {
			bos.writeByteLengthString(entry.name(), StandardCharsets.US_ASCII);
			OmfUtils.writeIndex(bos, entry.typeIndex());
		}
	}
}
