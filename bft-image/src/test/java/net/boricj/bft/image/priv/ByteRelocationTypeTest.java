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

import org.junit.jupiter.api.Test;

import net.boricj.bft.image.relocs.priv.ByteFieldCodec;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ByteRelocationTypeTest {

	@Test
	void testLittleEndianUnsigned() {
		ByteFieldCodec codec = ByteFieldCodec.U16LE;
		byte[] bytes = new byte[] {0x00, 0x00, 0x00};

		codec.write(bytes, 1, 0x12345L);

		assertEquals(0x2345L, codec.read(bytes, 1));
		assertEquals(0x45, Byte.toUnsignedInt(bytes[1]));
		assertEquals(0x23, Byte.toUnsignedInt(bytes[2]));
	}

	@Test
	void testBigEndianSigned() {
		ByteFieldCodec codec = ByteFieldCodec.S16BE;
		byte[] bytes = new byte[] {(byte) 0xff, (byte) 0x80};

		assertEquals(-128L, codec.read(bytes, 0));
	}
}
