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
package net.boricj.bft.codeview.types;

import java.io.IOException;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;

/**
 * Base class for one CodeView type record.
 */
public abstract class CodeViewTypeRecord {
	private final int type;
	private final byte[] paddingAfter;

	/**
	 * Creates a type record model.
	 *
	 * @param type CodeView type-record kind
	 * @param paddingAfter raw bytes that followed the record to satisfy stream alignment
	 */
	protected CodeViewTypeRecord(int type, byte[] paddingAfter) {
		this.type = type;
		this.paddingAfter = paddingAfter;
	}

	/**
	 * Returns type-record kind identifier.
	 *
	 * @return type-record kind identifier
	 */
	public int getType() {
		return type;
	}

	/**
	 * Returns trailing alignment bytes preserved after this record.
	 *
	 * @return trailing alignment bytes
	 */
	public byte[] getPaddingAfter() {
		return paddingAfter;
	}

	/**
	 * Writes this type record including its own record header.
	 *
	 * @param bos destination output stream
	 * @throws IOException if writing fails
	 */
	public abstract void write(ByteOutputStream bos) throws IOException;

	/**
	 * Parses one type record from the current stream position.
	 *
	 * @param bis input stream positioned at the start of a type record
	 * @return parsed type record
	 * @throws IOException if the record cannot be parsed
	 */
	public static CodeViewTypeRecord parse(ByteInputStream bis) throws IOException {
		// Read a single record: length (2 bytes) + type (2 bytes) + data (length-4 bytes)
		if (bis.available() < 4) {
			throw new IOException("Not enough bytes to read type record header");
		}

		int startPos = bis.getCount();
		int recordLength = bis.readShort() & 0xFFFF;

		if (recordLength < 4) {
			throw new IOException("Invalid type record length: " + recordLength);
		}

		int recordType = bis.readShort() & 0xFFFF;
		byte[] data = new byte[recordLength - 4];
		bis.readFully(data);

		// Determine padding (up to next 4-byte boundary)
		int currentPos = bis.getCount();
		int paddingNeeded = (4 - (currentPos % 4)) % 4;
		byte[] padding = new byte[paddingNeeded];
		if (paddingNeeded > 0 && bis.available() >= paddingNeeded) {
			bis.readFully(padding);
		}

		return new CodeViewTypeRecordUnimplemented(recordType, recordLength, data, padding);
	}

	private static class CodeViewTypeRecordUnimplemented extends CodeViewTypeRecord {
		private final int recordLength;
		private final byte[] data;

		public CodeViewTypeRecordUnimplemented(int type, int recordLength, byte[] data, byte[] paddingAfter) {
			super(type, paddingAfter);
			this.recordLength = recordLength;
			this.data = data;
		}

		@Override
		public void write(ByteOutputStream bos) throws IOException {
			bos.writeShort((short) recordLength);
			bos.writeShort((short) getType());
			bos.write(data);
			if (getPaddingAfter().length > 0) {
				bos.write(getPaddingAfter());
			}
		}
	}
}
