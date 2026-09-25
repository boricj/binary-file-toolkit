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

import net.boricj.bft.image.RelocationFieldCodec;
import net.boricj.bft.image.relocs.priv.BitShiftedAdjustedFieldCodec;
import net.boricj.bft.image.relocs.priv.BitShiftedFieldCodec;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BitShiftedRelocationTypeTest {

	@Test
	void writesContiguousSliceAndPreservesOtherBits() {
		RelocationFieldCodec type = BitShiftedFieldCodec.of(4, ByteOrder.LITTLE_ENDIAN, 0, 26, 2, true);
		byte[] instruction = new byte[] {0x00, 0x00, 0x00, 0x08};

		type.write(instruction, 0, 0x10L);

		assertEquals(0x04, Byte.toUnsignedInt(instruction[0]));
		assertEquals(0x00, Byte.toUnsignedInt(instruction[1]));
		assertEquals(0x00, Byte.toUnsignedInt(instruction[2]));
		assertEquals(0x08, Byte.toUnsignedInt(instruction[3]));
		assertEquals(0x10L, type.read(instruction, 0));
	}

	@Test
	void appliesCarryAdjustmentBeforeRightShift() {
		RelocationFieldCodec type = BitShiftedAdjustedFieldCodec.of(2, ByteOrder.LITTLE_ENDIAN, 0, 16, 16, true);
		byte[] field = new byte[] {0x00, 0x00};

		type.write(field, 0, 0x00008000L);

		assertEquals(0x01, Byte.toUnsignedInt(field[0]));
		assertEquals(0x00, Byte.toUnsignedInt(field[1]));
		assertEquals(0x00010000L, type.read(field, 0));
	}
}
