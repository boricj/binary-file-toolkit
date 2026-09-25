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
package net.boricj.bft.codeview.symbols.sections;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.codeview.CodeViewFile.Parser;
import net.boricj.bft.codeview.constants.CodeViewSymbolTableSectionType;
import net.boricj.bft.codeview.symbols.CodeViewSymbolTableSection;
import net.boricj.bft.codeview.tables.CodeViewSymbolTable;

/**
 * {@code DEBUG_S_FILECHKSMS} subsection containing file checksum records.
 */
public class CodeViewSymbolTableFileChecksumsSection extends CodeViewSymbolTableSection {
	/**
	 * One file-checksum entry from the subsection payload.
	 */
	public static class FileChecksum {
		private final int offsetFileName;
		private final int cbChecksum;
		private final int checksumType;
		private final byte[] checksumBytes;

		/**
		 * Creates a file checksum record.
		 *
		 * @param offsetFileName offset into the related string table
		 * @param cbChecksum checksum byte length
		 * @param checksumType checksum algorithm identifier
		 * @param checksumBytes raw checksum bytes
		 */
		public FileChecksum(int offsetFileName, int cbChecksum, int checksumType, byte[] checksumBytes) {
			this.offsetFileName = offsetFileName;
			this.cbChecksum = cbChecksum;
			this.checksumType = checksumType;
			this.checksumBytes = checksumBytes.clone();
		}

		/**
		 * Returns file-name string-table offset.
		 *
		 * @return file-name string-table offset
		 */
		public int getOffsetFileName() {
			return offsetFileName;
		}

		/**
		 * Returns checksum byte length.
		 *
		 * @return checksum byte length
		 */
		public int getCbChecksum() {
			return cbChecksum;
		}

		/**
		 * Returns checksum algorithm identifier.
		 *
		 * @return checksum algorithm identifier
		 */
		public int getChecksumType() {
			return checksumType;
		}

		/**
		 * Returns a defensive copy of checksum bytes.
		 *
		 * @return checksum bytes
		 */
		public byte[] getChecksumBytes() {
			return checksumBytes.clone();
		}
	}

	private final List<FileChecksum> records;
	private final int padding;

	/**
	 * Parses the file checksums subsection.
	 *
	 * @param parser parser settings
	 * @param table owning symbol table
	 * @param bis input stream positioned at subsection payload
	 * @throws IOException if parsing fails
	 */
	public CodeViewSymbolTableFileChecksumsSection(Parser parser, CodeViewSymbolTable table, ByteInputStream bis)
			throws IOException {
		super(CodeViewSymbolTableSectionType.DEBUG_S_FILECHKSMS);

		// Read the entire section data once so we can measure padding, then parse from it.
		byte[] allBytes = bis.readAllBytes();
		ByteInputStream dataBis = ByteInputStream.asLittleEndian(new ByteArrayInputStream(allBytes));

		List<FileChecksum> recs = new ArrayList<>();

		// loop while we have at least enough bytes for one record header (offset + cb + type = 6 bytes)
		while (dataBis.available() >= 6) {
			int offsetFileName = dataBis.readInt();
			int cbChecksum = dataBis.readByte() & 0xFF;
			int checksumType = dataBis.readByte() & 0xFF;

			byte[] checksumBytes = new byte[cbChecksum];
			if (cbChecksum > 0 && dataBis.available() >= cbChecksum) {
				dataBis.readFully(checksumBytes);
			} else if (cbChecksum > 0) {
				// not enough bytes for checksum, break out to avoid EOF
				System.out.printf(
						"[DEBUG] FileChecksumsSection incomplete checksum, expected %d bytes but only %d remain\n",
						cbChecksum, dataBis.available());
				break;
			}

			recs.add(new FileChecksum(offsetFileName, cbChecksum, checksumType, checksumBytes));
		}
		// any leftover bytes are padding; preserve count so we can emit the same length
		this.padding = dataBis.available();

		this.records = Collections.unmodifiableList(recs);
	}

	@Override
	public void write(ByteOutputStream bos) throws IOException {
		// construct bytes in a temporary buffer so we don't depend on external
		// stream position tracking.  Padding is already computed and must be
		// written after the record bytes.
		ByteArrayOutputStream tmpBaos = new ByteArrayOutputStream();
		ByteOutputStream tmpBos = ByteOutputStream.asLittleEndian(tmpBaos);

		for (FileChecksum rec : records) {
			tmpBos.writeInt(rec.offsetFileName);
			tmpBos.writeByte(rec.cbChecksum);
			tmpBos.writeByte(rec.checksumType);
			if (rec.cbChecksum > 0) {
				tmpBos.write(rec.checksumBytes);
			}
		}

		bos.write(tmpBaos.toByteArray());
		if (padding > 0) {
			bos.write(new byte[padding]);
		}
	}

	/**
	 * Returns immutable checksum records.
	 *
	 * @return checksum records
	 */
	public List<FileChecksum> getRecords() {
		return records;
	}
}
