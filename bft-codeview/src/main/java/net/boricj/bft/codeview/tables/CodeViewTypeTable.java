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
package net.boricj.bft.codeview.tables;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.IndirectList;
import net.boricj.bft.codeview.CodeViewFile;
import net.boricj.bft.codeview.CodeViewFile.Parser;
import net.boricj.bft.codeview.CodeViewTable;
import net.boricj.bft.codeview.constants.CodeViewSignature;
import net.boricj.bft.codeview.types.CodeViewTypeRecord;

/**
 * Parsed CodeView type table ({@code .debug$T}) containing type records.
 */
public class CodeViewTypeTable extends CodeViewTable implements IndirectList<CodeViewTypeRecord> {
	private final List<CodeViewTypeRecord> typeRecords = new ArrayList<>();
	private final int minTypeIndex;
	private final int maxTypeIndex;

	/**
	 * Creates an empty type table with explicit signature.
	 *
	 * @param codeView owning CodeView file
	 * @param name source section name
	 * @param signature type table signature
	 */
	protected CodeViewTypeTable(CodeViewFile codeView, String name, CodeViewSignature signature) {
		super(codeView, name, signature);
		this.minTypeIndex = 0;
		this.maxTypeIndex = 0;
	}

	/**
	 * Parses a type table from encoded bytes.
	 *
	 * @param parser parser settings
	 * @param codeView owning CodeView file
	 * @param name source section name
	 * @param bis input stream positioned at table payload
	 * @throws IOException if parsing fails
	 */
	public CodeViewTypeTable(Parser parser, CodeViewFile codeView, String name, ByteInputStream bis)
			throws IOException {
		super(codeView, name, CodeViewSignature.valueFrom(bis.readInt()));

		// Type table structure: records directly follow signature
		// Records are 4-byte aligned

		int typeIndex = 0x1000; // First type index

		while (bis.available() > 0) {
			// Align to 4-byte boundary before reading next record
			bis.alignTo(4);

			if (bis.available() > 0) {
				typeRecords.add(CodeViewTypeRecord.parse(bis));
				typeIndex++;
			}
		}

		if (typeRecords.size() > 0) {
			this.minTypeIndex = 0x1000;
			this.maxTypeIndex = 0x1000 + typeRecords.size() - 1;
		} else {
			this.minTypeIndex = 0;
			this.maxTypeIndex = 0;
		}
	}

	@Override
	public List<CodeViewTypeRecord> getElements() {
		return Collections.unmodifiableList(typeRecords);
	}

	/**
	 * Returns the minimum type index present in this table.
	 *
	 * @return minimum type index, or {@code 0} when table is empty
	 */
	public int getMinTypeIndex() {
		return minTypeIndex;
	}

	/**
	 * Returns the maximum type index present in this table.
	 *
	 * @return maximum type index, or {@code 0} when table is empty
	 */
	public int getMaxTypeIndex() {
		return maxTypeIndex;
	}

	/**
	 * Looks up a record by CodeView type index.
	 *
	 * @param index CodeView type index
	 * @return matching record, or {@code null} if out of range
	 */
	public CodeViewTypeRecord getTypeRecord(int index) {
		if (index < minTypeIndex || index > maxTypeIndex) {
			return null;
		}
		int recordIndex = index - minTypeIndex;
		if (recordIndex >= 0 && recordIndex < typeRecords.size()) {
			return typeRecords.get(recordIndex);
		}
		return null;
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		ByteOutputStream bos = ByteOutputStream.asLittleEndian(outputStream);
		bos.writeInt(getSignature().getValue());

		for (CodeViewTypeRecord record : typeRecords) {
			record.write(bos);
		}
	}
}
