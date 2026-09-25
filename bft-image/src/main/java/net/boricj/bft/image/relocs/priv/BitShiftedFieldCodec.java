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

import java.nio.ByteOrder;
import java.util.Objects;

/**
 * Codec for contiguous bitfields that represent shifted values.
 */
public class BitShiftedFieldCodec extends ByteFieldCodec {
	private final int bitOffset;
	private final int bitWidth;
	private final int shift;
	private final boolean signed;

	/**
	 * Builds a shifted bitfield codec.
	 *
	 * @param byteWidth byte width of the field container
	 * @param byteOrder byte order of the field container
	 * @param bitOffset bit offset of the encoded field inside the container
	 * @param bitWidth encoded bit width of the field
	 * @param shift arithmetic right-shift count applied when encoding
	 * @param signed whether decoded field values are interpreted as signed
	 */
	protected BitShiftedFieldCodec(
			int byteWidth, ByteOrder byteOrder, int bitOffset, int bitWidth, int shift, boolean signed) {
		super(byteWidth, byteOrder, false);
		if (bitOffset < 0) {
			throw new IllegalArgumentException("bitOffset must be non-negative");
		}
		if (bitWidth < 1 || bitWidth > Long.SIZE) {
			throw new IllegalArgumentException("bitWidth must be in range [1, 64]");
		}
		if (shift < 0 || shift >= Long.SIZE) {
			throw new IllegalArgumentException("shift must be in range [0, 63]");
		}
		if (bitOffset + bitWidth > byteWidth * Byte.SIZE) {
			throw new IllegalArgumentException("bitOffset + bitWidth must be <= container bit width");
		}

		this.bitOffset = bitOffset;
		this.bitWidth = bitWidth;
		this.shift = shift;
		this.signed = signed;
	}

	/**
	 * Creates a codec for contiguous shifted bitfields.
	 *
	 * @param byteWidth byte width of the field container
	 * @param byteOrder byte order of the field container
	 * @param bitOffset bit offset of the encoded field inside the container
	 * @param bitWidth encoded bit width of the field
	 * @param shift arithmetic right-shift count applied when encoding
	 * @param signed whether decoded field values are interpreted as signed
	 * @return configured shifted field codec
	 */
	public static BitShiftedFieldCodec of(
			int byteWidth, ByteOrder byteOrder, int bitOffset, int bitWidth, int shift, boolean signed) {
		return new BitShiftedFieldCodec(byteWidth, byteOrder, bitOffset, bitWidth, shift, signed);
	}

	@Override
	public long read(byte[] bytes, int offset) {
		long container = super.read(bytes, offset);
		long encoded = (container >>> this.bitOffset) & bitMask(this.bitWidth);
		long decoded = this.signed ? signExtend(encoded, this.bitWidth) : encoded;
		return decoded << this.shift;
	}

	/**
	 * Returns the shift amount applied by this codec.
	 *
	 * @return shift amount in bits
	 */
	protected final int shift() {
		return this.shift;
	}

	@Override
	public void write(byte[] bytes, int offset, long value) {
		long encoded = (value >> this.shift) & bitMask(this.bitWidth);
		long mask = bitMask(this.bitWidth);
		long container = super.read(bytes, offset);
		long cleared = container & ~(mask << this.bitOffset);
		long patched = cleared | ((encoded & mask) << this.bitOffset);
		super.write(bytes, offset, patched);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		BitShiftedFieldCodec other = (BitShiftedFieldCodec) obj;
		return byteWidth() == other.byteWidth()
				&& byteOrder().equals(other.byteOrder())
				&& this.bitOffset == other.bitOffset
				&& this.bitWidth == other.bitWidth
				&& this.shift == other.shift
				&& this.signed == other.signed;
	}

	@Override
	public int hashCode() {
		return Objects.hash(byteWidth(), byteOrder(), this.bitOffset, this.bitWidth, this.shift, this.signed);
	}

	@Override
	public String toString() {
		return String.format(
				"BitShiftedFieldCodec[byteWidth=%d, byteOrder=%s, bitOffset=%d, bitWidth=%d, shift=%d, signed=%b]",
				byteWidth(), byteOrder(), this.bitOffset, this.bitWidth, this.shift, this.signed);
	}

	private static long bitMask(int width) {
		if (width == Long.SIZE) {
			return -1L;
		}
		return (1L << width) - 1;
	}

	private static long signExtend(long value, int bitWidth) {
		if (bitWidth == Long.SIZE) {
			return value;
		}

		long signBit = 1L << (bitWidth - 1);
		long mask = (1L << bitWidth) - 1;
		long masked = value & mask;
		return (masked ^ signBit) - signBit;
	}
}
