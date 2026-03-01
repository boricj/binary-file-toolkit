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

public class CodeViewTypeTable extends CodeViewTable implements IndirectList<CodeViewTypeRecord> {
	private final List<CodeViewTypeRecord> typeRecords = new ArrayList<>();
	private final int minTypeIndex;
	private final int maxTypeIndex;

	protected CodeViewTypeTable(CodeViewFile codeView, String name, CodeViewSignature signature) {
		super(codeView, name, signature);
		this.minTypeIndex = 0;
		this.maxTypeIndex = 0;
	}

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

	public int getMinTypeIndex() {
		return minTypeIndex;
	}

	public int getMaxTypeIndex() {
		return maxTypeIndex;
	}

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
