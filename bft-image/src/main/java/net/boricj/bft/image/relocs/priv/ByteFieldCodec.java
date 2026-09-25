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
package net.boricj.bft.image.relocs.priv;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToLongFunction;

import net.boricj.bft.image.RelocationFieldCodec;

/**
 * Opaque codec for a fixed-width relocation field.
 */
public class ByteFieldCodec implements RelocationFieldCodec {
	private static final Map<Integer, ToLongFunction<ByteBuffer>> READERS_UNSIGNED = Map.of(
			1, b -> Byte.toUnsignedLong(b.get()),
			2, b -> Short.toUnsignedLong(b.getShort()),
			4, b -> Integer.toUnsignedLong(b.getInt()),
			8, ByteBuffer::getLong);

	private static final Map<Integer, ToLongFunction<ByteBuffer>> READERS_SIGNED = Map.of(
			1, ByteBuffer::get,
			2, ByteBuffer::getShort,
			4, ByteBuffer::getInt,
			8, ByteBuffer::getLong);

	private static final Map<Integer, Writer> WRITERS = Map.of(
			1, (b, v) -> b.put((byte) v),
			2, (b, v) -> b.putShort((short) v),
			4, (b, v) -> b.putInt((int) v),
			8, ByteBuffer::putLong);

	/** Unsigned 8-bit little-endian field codec. */
	public static final ByteFieldCodec U8LE = new ByteFieldCodec(1, ByteOrder.LITTLE_ENDIAN, false);
	/** Signed 8-bit little-endian field codec. */
	public static final ByteFieldCodec S8LE = new ByteFieldCodec(1, ByteOrder.LITTLE_ENDIAN, true);
	/** Unsigned 16-bit little-endian field codec. */
	public static final ByteFieldCodec U16LE = new ByteFieldCodec(2, ByteOrder.LITTLE_ENDIAN, false);
	/** Signed 16-bit little-endian field codec. */
	public static final ByteFieldCodec S16LE = new ByteFieldCodec(2, ByteOrder.LITTLE_ENDIAN, true);
	/** Unsigned 32-bit little-endian field codec. */
	public static final ByteFieldCodec U32LE = new ByteFieldCodec(4, ByteOrder.LITTLE_ENDIAN, false);
	/** Signed 32-bit little-endian field codec. */
	public static final ByteFieldCodec S32LE = new ByteFieldCodec(4, ByteOrder.LITTLE_ENDIAN, true);
	/** Unsigned 64-bit little-endian field codec. */
	public static final ByteFieldCodec U64LE = new ByteFieldCodec(8, ByteOrder.LITTLE_ENDIAN, false);
	/** Signed 64-bit little-endian field codec. */
	public static final ByteFieldCodec S64LE = new ByteFieldCodec(8, ByteOrder.LITTLE_ENDIAN, true);

	/** Unsigned 8-bit big-endian field codec. */
	public static final ByteFieldCodec U8BE = new ByteFieldCodec(1, ByteOrder.BIG_ENDIAN, false);
	/** Signed 8-bit big-endian field codec. */
	public static final ByteFieldCodec S8BE = new ByteFieldCodec(1, ByteOrder.BIG_ENDIAN, true);
	/** Unsigned 16-bit big-endian field codec. */
	public static final ByteFieldCodec U16BE = new ByteFieldCodec(2, ByteOrder.BIG_ENDIAN, false);
	/** Signed 16-bit big-endian field codec. */
	public static final ByteFieldCodec S16BE = new ByteFieldCodec(2, ByteOrder.BIG_ENDIAN, true);
	/** Unsigned 32-bit big-endian field codec. */
	public static final ByteFieldCodec U32BE = new ByteFieldCodec(4, ByteOrder.BIG_ENDIAN, false);
	/** Signed 32-bit big-endian field codec. */
	public static final ByteFieldCodec S32BE = new ByteFieldCodec(4, ByteOrder.BIG_ENDIAN, true);
	/** Unsigned 64-bit big-endian field codec. */
	public static final ByteFieldCodec U64BE = new ByteFieldCodec(8, ByteOrder.BIG_ENDIAN, false);
	/** Signed 64-bit big-endian field codec. */
	public static final ByteFieldCodec S64BE = new ByteFieldCodec(8, ByteOrder.BIG_ENDIAN, true);

	private final int byteWidth;
	private final ByteOrder byteOrder;
	private final boolean signed;
	private final ToLongFunction<ByteBuffer> reader;
	private final Writer writer;

	/**
	 * Creates a fixed-width relocation field codec.
	 *
	 * @param byteWidth field width in bytes; must be a power of two between 1 and 8
	 * @param byteOrder byte order used to encode/decode the field
	 * @param signed whether the field should be interpreted as signed on read
	 */
	protected ByteFieldCodec(int byteWidth, ByteOrder byteOrder, boolean signed) {
		if (byteWidth <= 0 || (byteWidth & (byteWidth - 1)) != 0 || byteWidth > Long.BYTES) {
			throw new IllegalArgumentException("byteWidth must be a power of two in range [1, 8]");
		}
		Objects.requireNonNull(byteOrder, "byteOrder");

		ToLongFunction<ByteBuffer> resolvedReader =
				signed ? READERS_SIGNED.get(byteWidth) : READERS_UNSIGNED.get(byteWidth);
		Writer resolvedWriter = WRITERS.get(byteWidth);
		if (resolvedReader == null || resolvedWriter == null) {
			throw new IllegalArgumentException("Unsupported byteWidth: " + byteWidth);
		}

		this.byteWidth = byteWidth;
		this.byteOrder = byteOrder;
		this.signed = signed;
		this.reader = resolvedReader;
		this.writer = resolvedWriter;
	}

	@Override
	public long read(byte[] bytes, int offset) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, this.byteWidth).order(this.byteOrder);
		return this.reader.applyAsLong(buffer);
	}

	/**
	 * Returns the fixed field width in bytes.
	 *
	 * @return fixed field width in bytes
	 */
	protected final int byteWidth() {
		return this.byteWidth;
	}

	/**
	 * Returns the byte order used by this codec.
	 *
	 * @return configured byte order
	 */
	protected final ByteOrder byteOrder() {
		return this.byteOrder;
	}

	@Override
	public void write(byte[] bytes, int offset, long value) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, this.byteWidth).order(this.byteOrder);
		this.writer.write(buffer, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ByteFieldCodec other = (ByteFieldCodec) obj;
		return this.byteWidth == other.byteWidth && this.byteOrder == other.byteOrder && this.signed == other.signed;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.byteWidth, this.byteOrder, this.signed);
	}

	@Override
	public String toString() {
		return String.format(
				"ByteFieldCodec[byteWidth=%d, byteOrder=%s, signed=%b]", this.byteWidth, this.byteOrder, this.signed);
	}

	@FunctionalInterface
	private interface Writer {
		void write(ByteBuffer buffer, long value);
	}
}
