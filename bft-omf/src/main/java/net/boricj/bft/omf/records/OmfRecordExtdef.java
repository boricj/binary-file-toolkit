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
import net.boricj.bft.omf.OmfUtils;
import net.boricj.bft.omf.constants.OmfRecordType;

/**
 * EXTDEF - External Names Definition Record
 * Defines external symbols referenced by the module.
 */
public class OmfRecordExtdef extends OmfRecord {
	private final List<String> names;
	private final List<Integer> typeIndices;

	/**
	 * Parses an EXTDEF record from the input stream.
	 *
	 * @param file the parent OMF file
	 * @param bis the input stream
	 * @throws IOException if an I/O error occurs
	 */
	public OmfRecordExtdef(OmfFile file, ByteInputStream bis) throws IOException {
		super(file, OmfRecordType.EXTDEF);

		List<String> parsedNames = new ArrayList<>();
		List<Integer> parsedTypeIndices = new ArrayList<>();

		while (bis.available() > 0) {
			parsedNames.add(bis.readByteLengthString(StandardCharsets.US_ASCII));
			parsedTypeIndices.add(OmfUtils.readIndex(bis));
		}

		this.names = Collections.unmodifiableList(parsedNames);
		this.typeIndices = Collections.unmodifiableList(parsedTypeIndices);
	}

	/**
	 * Creates a new EXTDEF record.
	 *
	 * @param file the parent OMF file
	 * @param names the list of external symbol names
	 * @param typeIndices the list of type indices (one per name)
	 */
	public OmfRecordExtdef(OmfFile file, List<String> names, List<Integer> typeIndices) {
		super(file, OmfRecordType.EXTDEF);
		Objects.requireNonNull(names);
		Objects.requireNonNull(typeIndices);

		if (names.size() != typeIndices.size()) {
			throw new IllegalArgumentException("Names and type indices must have same size");
		}
		this.names = Collections.unmodifiableList(new ArrayList<>(names));
		this.typeIndices = Collections.unmodifiableList(new ArrayList<>(typeIndices));
	}

	/**
	 * Returns the list of external symbol names.
	 *
	 * @return the names
	 */
	public List<String> getNames() {
		return names;
	}

	/**
	 * Returns the list of type indices.
	 *
	 * @return the type indices
	 */
	public List<Integer> getTypeIndices() {
		return typeIndices;
	}

	@Override
	public void write(ByteOutputStream bos) throws IOException {
		for (int i = 0; i < names.size(); i++) {
			bos.writeByteLengthString(names.get(i), StandardCharsets.US_ASCII);
			OmfUtils.writeIndex(bos, typeIndices.get(i));
		}
	}
}
