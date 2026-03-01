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

public abstract class CodeViewTypeRecord {
	private final int type;
	private final byte[] paddingAfter;

	protected CodeViewTypeRecord(int type, byte[] paddingAfter) {
		this.type = type;
		this.paddingAfter = paddingAfter;
	}

	public int getType() {
		return type;
	}

	public byte[] getPaddingAfter() {
		return paddingAfter;
	}

	public abstract void write(ByteOutputStream bos) throws IOException;

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
