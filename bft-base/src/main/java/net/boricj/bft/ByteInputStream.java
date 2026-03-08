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
package net.boricj.bft;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Input stream with support for little/big-endian multi-byte reads and position tracking.
 * <p>
 * Wraps an {@link InputStream} and provides {@link DataInput} operations with configurable
 * byte order and automatic byte counting.
 */
public class ByteInputStream extends FilterInputStream implements DataInput {
	private final byte[] bytes = new byte[8];
	private final ByteBuffer byteBuffer;
	private int count = 0;

	/**
	 * Creates a byte input stream with the specified byte order.
	 *
	 * @param inputStream the underlying input stream
	 * @param byteOrder the byte order for multi-byte reads
	 */
	public ByteInputStream(InputStream inputStream, ByteOrder byteOrder) {
		super(inputStream);
		this.byteBuffer = ByteBuffer.wrap(bytes);
		this.byteBuffer.order(byteOrder);
	}

	/**
	 * Creates a little-endian byte input stream from an input stream.
	 *
	 * @param inputStream the underlying input stream
	 * @return a little-endian ByteInputStream
	 */
	public static ByteInputStream asLittleEndian(InputStream inputStream) {
		return new ByteInputStream(inputStream, ByteOrder.LITTLE_ENDIAN);
	}

	/**
	 * Creates a little-endian byte input stream from a byte array.
	 *
	 * @param bytes the byte array to read from
	 * @return a little-endian ByteInputStream
	 */
	public static ByteInputStream asLittleEndian(byte[] bytes) {
		return new ByteInputStream(new ByteArrayInputStream(bytes), ByteOrder.LITTLE_ENDIAN);
	}

	/**
	 * Creates a big-endian byte input stream from an input stream.
	 *
	 * @param inputStream the underlying input stream
	 * @return a big-endian ByteInputStream
	 */
	public static ByteInputStream asBigEndian(InputStream inputStream) {
		return new ByteInputStream(inputStream, ByteOrder.BIG_ENDIAN);
	}

	/**
	 * Creates a big-endian byte input stream from a byte array.
	 *
	 * @param bytes the byte array to read from
	 * @return a big-endian ByteInputStream
	 */
	public static ByteInputStream asBigEndian(byte[] bytes) {
		return new ByteInputStream(new ByteArrayInputStream(bytes), ByteOrder.BIG_ENDIAN);
	}

	@Override
	public int read() throws IOException {
		int ret = super.read();

		if (ret >= 0) {
			count++;
		}

		return ret;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int ret = super.read(b, off, len);

		if (ret >= 0) {
			count += ret;
		}

		return ret;
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		readFully(b, 0, b.length);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		int ret = read(b, off, len);

		if (ret != len && len > 0) {
			throw new EOFException();
		}
	}

	@Override
	public int skipBytes(int n) throws IOException {
		int ret = (int) super.skip(n);

		if (ret >= 0) {
			count += ret;
		}

		return ret;
	}

	@Override
	public boolean readBoolean() throws IOException {
		readFully(bytes, 0, 1);
		return byteBuffer.get(0) != 0 ? true : false;
	}

	@Override
	public byte readByte() throws IOException {
		readFully(bytes, 0, 1);
		return bytes[0];
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return Byte.toUnsignedInt(readByte());
	}

	@Override
	public short readShort() throws IOException {
		readFully(bytes, 0, 2);
		return byteBuffer.getShort(0);
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return Short.toUnsignedInt(readShort());
	}

	@Override
	public char readChar() throws IOException {
		throw new UnsupportedOperationException("Unimplemented method 'readChar'");
	}

	@Override
	public int readInt() throws IOException {
		readFully(bytes, 0, 4);
		return byteBuffer.getInt(0);
	}

	@Override
	public long readLong() throws IOException {
		readFully(bytes, 0, 8);
		return byteBuffer.getLong(0);
	}

	@Override
	public float readFloat() throws IOException {
		readFully(bytes, 0, 4);
		return byteBuffer.getFloat(0);
	}

	@Override
	public double readDouble() throws IOException {
		readFully(bytes, 0, 8);
		return byteBuffer.getDouble(0);
	}

	@Override
	public String readLine() throws IOException {
		throw new UnsupportedOperationException("Unimplemented method 'readLine'");
	}

	@Override
	public String readUTF() throws IOException {
		throw new UnsupportedOperationException("Unimplemented method 'readUTF'");
	}

	@Override
	public long skip(long n) throws IOException {
		return skipBytes((int) n);
	}

	/**
	 * Skips bytes to align the read position to the specified boundary.
	 *
	 * @param alignment the alignment boundary in bytes
	 * @return the new byte count after alignment
	 * @throws IOException if an I/O error occurs
	 */
	public int alignTo(int alignment) throws IOException {
		int target = count % alignment;
		if (target != 0) {
			skipBytes(alignment - target);
		}
		return count;
	}

	/**
	 * Reads a null-terminated string using the specified charset.
	 *
	 * @param utf8 the charset to decode the string
	 * @return the decoded string, not including the null terminator
	 * @throws IOException if an I/O error occurs
	 */
	public String readNullTerminatedString(Charset utf8) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int b;
		while ((b = read()) != -1) {
			if (b == 0) {
				break;
			}
			buffer.write(b);
		}

		return buffer.toString(utf8.name());
	}

	/**
	 * Reads a length-prefixed string where the first byte indicates the string length.
	 *
	 * @param charset the charset to decode the string
	 * @return the decoded string
	 * @throws IOException if an I/O error occurs
	 */
	public String readByteLengthString(Charset charset) throws IOException {
		int length = readUnsignedByte();
		byte[] data = new byte[length];
		readFully(data);
		return new String(data, charset);
	}

	/**
	 * Creates a new ByteInputStream containing the next specified number of bytes.
	 *
	 * @param length the number of bytes to read into the slice
	 * @return a new ByteInputStream with the same byte order
	 * @throws IOException if an I/O error occurs
	 */
	public ByteInputStream slice(int length) throws IOException {
		byte[] data = new byte[length];
		readFully(data);
		return new ByteInputStream(new ByteArrayInputStream(data), byteBuffer.order());
	}

	/**
	 * Gets the total number of bytes read from this stream.
	 *
	 * @return the byte count
	 */
	public int getCount() {
		return count;
	}
}
