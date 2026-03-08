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
import java.util.Objects;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.omf.OmfFile;
import net.boricj.bft.omf.OmfRecord;
import net.boricj.bft.omf.constants.OmfRecordType;

/**
 * THEADR - Translator Header Record
 * Contains the name of the object module.
 */
public class OmfRecordTheadr extends OmfRecord {
	private final String moduleName;

	/**
	 * Parses a THEADR record from the input stream.
	 *
	 * @param file the parent OMF file
	 * @param bis the input stream
	 * @throws IOException if an I/O error occurs
	 */
	public OmfRecordTheadr(OmfFile file, ByteInputStream bis) throws IOException {
		super(file, OmfRecordType.THEADR);

		// Read the module name (length-prefixed string)
		this.moduleName = bis.readByteLengthString(file.getCharset());
	}

	/**
	 * Creates a new THEADR record.
	 *
	 * @param file the parent OMF file
	 * @param moduleName the module name
	 */
	public OmfRecordTheadr(OmfFile file, String moduleName) {
		super(file, OmfRecordType.THEADR);
		Objects.requireNonNull(moduleName);

		this.moduleName = moduleName;
	}

	/**
	 * Returns the module name.
	 *
	 * @return the module name
	 */
	public String getModuleName() {
		return moduleName;
	}

	@Override
	public void write(ByteOutputStream bos) throws IOException {
		// Write module name as length-prefixed string
		bos.writeByteLengthString(moduleName, getFile().getCharset());
	}
}
