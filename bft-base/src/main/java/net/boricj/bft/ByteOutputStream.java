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

public class ByteOutputStream extends FilterOutputStream implements DataOutput {
	private final byte[] bytes = new byte[8];
	private final ByteBuffer byteBuffer;

	public ByteOutputStream(OutputStream outputStream, ByteOrder byteOrder) {
		super(outputStream);
		this.byteBuffer = ByteBuffer.wrap(bytes);
		this.byteBuffer.order(byteOrder);
	}

	public static ByteOutputStream asLittleEndian(OutputStream outputStream) {
		return new ByteOutputStream(outputStream, ByteOrder.LITTLE_ENDIAN);
	}

	public static ByteOutputStream asBigEndian(OutputStream outputStream) {
		return new ByteOutputStream(outputStream, ByteOrder.BIG_ENDIAN);
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
}
