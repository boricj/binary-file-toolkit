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

/**
 * Codec for contiguous bitfields that require a carry adjustment before shifting.
 */
public final class BitShiftedAdjustedFieldCodec extends BitShiftedFieldCodec {
	private BitShiftedAdjustedFieldCodec(
			int byteWidth, ByteOrder byteOrder, int bitOffset, int bitWidth, int shift, boolean signed) {
		super(byteWidth, byteOrder, bitOffset, bitWidth, shift, signed);
	}

	/**
	 * Creates a codec for shifted bitfields using half-up adjustment before the shift.
	 *
	 * @param byteWidth byte width of the field container
	 * @param byteOrder byte order of the field container
	 * @param bitOffset bit offset of the encoded field inside its container
	 * @param bitWidth encoded bit width of the field
	 * @param shift arithmetic right-shift count applied when encoding
	 * @param signed whether decoded field values are interpreted as signed
	 * @return configured adjusted shifted field codec
	 */
	public static BitShiftedAdjustedFieldCodec of(
			int byteWidth, ByteOrder byteOrder, int bitOffset, int bitWidth, int shift, boolean signed) {
		return new BitShiftedAdjustedFieldCodec(byteWidth, byteOrder, bitOffset, bitWidth, shift, signed);
	}

	@Override
	public void write(byte[] bytes, int offset, long value) {
		long adjustment = shift() == 0 ? 0 : 1L << (shift() - 1);
		super.write(bytes, offset, value + adjustment);
	}
}
