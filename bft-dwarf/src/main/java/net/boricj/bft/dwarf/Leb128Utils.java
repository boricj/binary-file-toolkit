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
package net.boricj.bft.dwarf;

import java.io.IOException;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;

/**
 * Utility methods for DWARF ULEB128 and SLEB128 encoding.
 */
public final class Leb128Utils {
	private Leb128Utils() {
		// Utility class - prevent instantiation
	}

	/**
	 * Reads an unsigned LEB128 value.
	 *
	 * @param bis byte input stream
	 * @return decoded value
	 * @throws IOException if reading fails
	 */
	public static long readUleb128(ByteInputStream bis) throws IOException {
		long value = 0;
		int shift = 0;
		int b;
		do {
			b = bis.readUnsignedByte();
			value |= ((long) (b & 0x7f)) << shift;
			shift += 7;
			if (shift >= Long.SIZE && (b & 0x80) != 0) {
				throw new IllegalArgumentException("ULEB128 value exceeds 64-bit range");
			}
		} while ((b & 0x80) != 0);
		return value;
	}

	/**
	 * Reads a signed LEB128 value.
	 *
	 * @param bis byte input stream
	 * @return decoded value
	 * @throws IOException if reading fails
	 */
	public static long readSleb128(ByteInputStream bis) throws IOException {
		long value = 0;
		int shift = 0;
		int b;
		do {
			b = bis.readUnsignedByte();
			value |= ((long) (b & 0x7f)) << shift;
			shift += 7;
			if (shift > Long.SIZE && (b & 0x80) != 0) {
				throw new IllegalArgumentException("SLEB128 value exceeds 64-bit range");
			}
		} while ((b & 0x80) != 0);

		if ((shift < Long.SIZE) && ((b & 0x40) != 0)) {
			value |= -1L << shift;
		}
		return value;
	}

	/**
	 * Writes an unsigned LEB128 value.
	 *
	 * @param bos byte output stream
	 * @param value value to encode
	 * @throws IOException if writing fails
	 */
	public static void writeUleb128(ByteOutputStream bos, long value) throws IOException {
		if (value < 0) {
			throw new IllegalArgumentException("ULEB128 cannot encode negative values: " + value);
		}

		do {
			int b = (int) (value & 0x7f);
			value >>>= 7;
			if (value != 0) {
				b |= 0x80;
			}
			bos.writeByte(b);
		} while (value != 0);
	}

	/**
	 * Writes a signed LEB128 value.
	 *
	 * @param bos byte output stream
	 * @param value value to encode
	 * @throws IOException if writing fails
	 */
	public static void writeSleb128(ByteOutputStream bos, long value) throws IOException {
		boolean more;
		do {
			int b = (int) (value & 0x7f);
			boolean signBitSet = (b & 0x40) != 0;
			value >>= 7;
			more = !((value == 0 && !signBitSet) || (value == -1 && signBitSet));
			if (more) {
				b |= 0x80;
			}
			bos.writeByte(b);
		} while (more);
	}
}
