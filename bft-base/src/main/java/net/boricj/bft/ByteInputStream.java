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

import java.io.DataInput;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteInputStream extends FilterInputStream implements DataInput {
	private final byte[] bytes = new byte[8];
	private final ByteBuffer byteBuffer;

	public ByteInputStream(InputStream inputStream, ByteOrder byteOrder) {
		super(inputStream);
		this.byteBuffer = ByteBuffer.wrap(bytes);
		this.byteBuffer.order(byteOrder);
	}

	public static ByteInputStream asLittleEndian(InputStream inputStream) {
		return new ByteInputStream(inputStream, ByteOrder.LITTLE_ENDIAN);
	}

	public static ByteInputStream asBigEndian(InputStream inputStream) {
		return new ByteInputStream(inputStream, ByteOrder.BIG_ENDIAN);
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		read(b, 0, b.length);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		if (in.read(b, off, len) != len) {
			throw new EOFException();
		}
	}

	@Override
	public int skipBytes(int n) throws IOException {
		return (int) in.skip(n);
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
}
