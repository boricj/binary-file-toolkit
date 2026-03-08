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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.omf.OmfFile;
import net.boricj.bft.omf.OmfRecord;
import net.boricj.bft.omf.constants.OmfRecordType;

/**
 * LNAMES - List of Names Record
 * Contains a list of names referenced by other records.
 */
public class OmfRecordLnames extends OmfRecord {
	private final List<String> names;

	/**
	 * Parses an LNAMES record from the input stream.
	 *
	 * @param file the parent OMF file
	 * @param bis the input stream
	 * @throws IOException if an I/O error occurs
	 */
	public OmfRecordLnames(OmfFile file, ByteInputStream bis) throws IOException {
		super(file, OmfRecordType.LNAMES);

		// Parse names from the stream
		List<String> parsedNames = new ArrayList<>();

		while (bis.available() > 0) {
			parsedNames.add(bis.readByteLengthString(StandardCharsets.US_ASCII));
		}

		this.names = Collections.unmodifiableList(parsedNames);
	}

	/**
	 * Creates a new LNAMES record.
	 *
	 * @param file the parent OMF file
	 * @param names the list of names
	 */
	public OmfRecordLnames(OmfFile file, List<String> names) {
		super(file, OmfRecordType.LNAMES);
		Objects.requireNonNull(names);

		this.names = Collections.unmodifiableList(new ArrayList<>(names));
	}

	/**
	 * Returns the list of names.
	 *
	 * @return the names
	 */
	public List<String> getNames() {
		return names;
	}

	@Override
	public void write(ByteOutputStream bos) throws IOException {
		for (String name : names) {
			bos.writeByteLengthString(name, StandardCharsets.US_ASCII);
		}
	}
}
