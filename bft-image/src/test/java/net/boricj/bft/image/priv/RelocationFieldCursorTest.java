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
package net.boricj.bft.image.priv;

import java.nio.ByteOrder;

import org.junit.jupiter.api.Test;

import net.boricj.bft.image.RelocationFieldCursor;
import net.boricj.bft.image.relocs.priv.BitShiftedFieldCodec;
import net.boricj.bft.image.relocs.priv.ByteFieldCodec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelocationFieldCursorTest {
	@Test
	void hoistsAndZerosInlineAddendForByteCodec() {
		byte[] section = new byte[] {0x00, 0x00, 0x12, 0x34};
		var cursor = new RelocationFieldCursor(ByteFieldCodec.U16LE, 2L);
		long addend = cursor.read(section);
		cursor.mask(section).apply(section);

		assertEquals(0x3412L, addend);
		assertEquals(0L, ByteFieldCodec.U16LE.read(section, 2));
		assertTrue(cursor.isZero(section));
	}

	@Test
	void hoistsAndZerosInlineAddendForBitShiftedCodec() {
		byte[] section = new byte[] {(byte) 0x30, (byte) 0x40, (byte) 0x00, (byte) 0x00};
		var codec = BitShiftedFieldCodec.of(2, ByteOrder.LITTLE_ENDIAN, 0, 16, 0, false);
		var cursor = new RelocationFieldCursor(codec, 0L);
		long addend = cursor.read(section);
		cursor.mask(section).apply(section);

		assertEquals(0x4030L, addend);
		assertEquals(0L, codec.read(section, 0));
		assertTrue(cursor.isZero(section));
	}

	@Test
	void rejectsOutOfBoundsOffsets() {
		byte[] section = new byte[] {0x00, 0x00, 0x00};
		assertThrows(IndexOutOfBoundsException.class, () -> new RelocationFieldCursor(ByteFieldCodec.U32LE, 3L)
				.read(section));
	}
}
