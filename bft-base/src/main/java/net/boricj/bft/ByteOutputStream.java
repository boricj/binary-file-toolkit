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

import java.io.DataOutput;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Output stream with support for little/big-endian multi-byte writes and position tracking.
 * <p>
 * Wraps an {@link OutputStream} and provides {@link DataOutput} operations with configurable
 * byte order and automatic byte counting.
 */
public class ByteOutputStream extends FilterOutputStream implements DataOutput {
	private final byte[] bytes = new byte[8];
	private final ByteBuffer byteBuffer;
	private int count = 0;

	/**
	 * Creates a byte output stream with the specified byte order.
	 *
	 * @param outputStream the underlying output stream
	 * @param byteOrder the byte order for multi-byte writes
	 */
	public ByteOutputStream(OutputStream outputStream, ByteOrder byteOrder) {
		super(outputStream);
		this.byteBuffer = ByteBuffer.wrap(bytes);
		this.byteBuffer.order(byteOrder);
	}

	/**
	 * Creates a little-endian byte output stream.
	 *
	 * @param outputStream the underlying output stream
	 * @return a little-endian ByteOutputStream
	 */
	public static ByteOutputStream asLittleEndian(OutputStream outputStream) {
		return new ByteOutputStream(outputStream, ByteOrder.LITTLE_ENDIAN);
	}

	/**
	 * Creates a big-endian byte output stream.
	 *
	 * @param outputStream the underlying output stream
	 * @return a big-endian ByteOutputStream
	 */
	public static ByteOutputStream asBigEndian(OutputStream outputStream) {
		return new ByteOutputStream(outputStream, ByteOrder.BIG_ENDIAN);
	}

	@Override
	public void write(int b) throws IOException {
		super.write(b);
		count++;
	}

	@Override
	public void writeBoolean(boolean v) throws IOException {
		writeByte(v ? 1 : 0);
	}

	@Override
	public void writeByte(int v) throws IOException {
		write(v);
	}

	@Override
	public void writeShort(int v) throws IOException {
		byteBuffer.putShort(0, (short) v);
		write(bytes, 0, Short.BYTES);
	}

	@Override
	public void writeChar(int v) throws IOException {
		byteBuffer.putChar(0, (char) v);
		write(bytes, 0, Character.BYTES);
	}

	@Override
	public void writeInt(int v) throws IOException {
		byteBuffer.putInt(0, v);
		write(bytes, 0, Integer.BYTES);
	}

	@Override
	public void writeLong(long v) throws IOException {
		byteBuffer.putLong(0, v);
		write(bytes, 0, Long.BYTES);
	}

	@Override
	public void writeFloat(float v) throws IOException {
		byteBuffer.putFloat(0, v);
		write(bytes, 0, Float.BYTES);
	}

	@Override
	public void writeDouble(double v) throws IOException {
		byteBuffer.putDouble(0, v);
		write(bytes, 0, Double.BYTES);
	}

	@Override
	public void writeBytes(String s) throws IOException {
		throw new UnsupportedOperationException("Unimplemented method 'writeBytes'");
	}

	@Override
	public void writeChars(String s) throws IOException {
		throw new UnsupportedOperationException("Unimplemented method 'writeChars'");
	}

	@Override
	public void writeUTF(String s) throws IOException {
		throw new UnsupportedOperationException("Unimplemented method 'writeUTF'");
	}

	/**
	 * Writes a null-terminated string using the specified charset.
	 *
	 * @param name the string to write
	 * @param charset the charset to encode the string
	 * @throws IOException if an I/O error occurs
	 */
	public void writeNullTerminatedString(String name, Charset charset) throws IOException {
		write(name.getBytes(charset));
		writeByte(0);
	}

	/**
	 * Writes a length-prefixed string where the first byte indicates the string length.
	 *
	 * @param value the string to write (max 255 bytes when encoded)
	 * @param charset the charset to encode the string
	 * @throws IOException if an I/O error occurs
	 * @throws IllegalArgumentException if the encoded string exceeds 255 bytes
	 */
	public void writeByteLengthString(String value, Charset charset) throws IOException {
		byte[] bytes = value.getBytes(charset);
		if (bytes.length > 0xFF) {
			throw new IllegalArgumentException("Byte-length string too long: " + bytes.length);
		}
		writeByte(bytes.length);
		write(bytes);
	}

	/**
	 * Writes zero bytes to align the write position to the specified boundary.
	 *
	 * @param i the alignment boundary in bytes
	 * @throws IOException if an I/O error occurs
	 */
	public void alignTo(int i) throws IOException {
		if (count % i != 0) {
			int padding = i - (count % i);
			write(new byte[padding], 0, padding);
		}
	}

	/**
	 * Gets the total number of bytes written to this stream.
	 *
	 * @return the byte count
	 */
	public int getCount() {
		return count;
	}
}
