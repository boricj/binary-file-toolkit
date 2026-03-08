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
package net.boricj.bft.omf;

import java.io.IOException;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;

/**
 * Utility methods for OMF parsing and writing.
 */
public class OmfUtils {
	private OmfUtils() {
		// Utility class - prevent instantiation
	}

	/**
	 * Read a variable-length index from a byte array.
	 * Indexes can be 1 or 2 bytes depending on the value.
	 *
	 * @param data the byte array to read from
	 * @param offset array containing current offset (will be updated)
	 * @return the index value
	 */
	public static int readIndex(byte[] data, int[] offset) {
		int value = data[offset[0]++] & 0xFF;
		if (value == 0) {
			return 0;
		}
		if ((value & 0x80) == 0) {
			return value;
		}
		int high = value & 0x7F;
		int low = data[offset[0]++] & 0xFF;
		return (high << 8) | low;
	}

	/**
	 * Read a variable-length index from a stream.
	 * Indexes can be 1 or 2 bytes depending on the value.
	 *
	 * @param bis the input stream
	 * @return the index value
	 * @throws IOException if an I/O error occurs
	 */
	public static int readIndex(ByteInputStream bis) throws IOException {
		int value = bis.readUnsignedByte();
		if (value == 0) {
			return 0;
		}
		if ((value & 0x80) == 0) {
			return value;
		}
		int high = value & 0x7F;
		int low = bis.readUnsignedByte();
		return (high << 8) | low;
	}

	/**
	 * Write a variable-length index to a stream.
	 * Indexes can be 1 or 2 bytes depending on the value.
	 *
	 * @param bos the output stream
	 * @param index the index value to write
	 * @throws IOException if an I/O error occurs
	 */
	public static void writeIndex(ByteOutputStream bos, int index) throws IOException {
		if (index == 0) {
			bos.writeByte(0);
		} else if (index < 0x80) {
			bos.writeByte(index);
		} else {
			bos.writeByte(0x80 | (index >> 8));
			bos.writeByte(index & 0xFF);
		}
	}

	/**
	 * Read an unsigned 16-bit little-endian integer from a byte array.
	 *
	 * @param data the byte array to read from
	 * @param offset the offset to read at
	 * @return the integer value
	 */
	public static int u16le(byte[] data, int offset) {
		return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8);
	}

	/**
	 * Read an unsigned 32-bit little-endian integer from a byte array.
	 *
	 * @param data the byte array to read from
	 * @param offset the offset to read at
	 * @return the integer value as a long
	 */
	public static long u32le(byte[] data, int offset) {
		return Integer.toUnsignedLong(u16le(data, offset) | (u16le(data, offset + 2) << 16));
	}
}
