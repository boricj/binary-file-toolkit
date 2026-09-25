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
package net.boricj.bft.image;

import java.nio.ByteOrder;

import org.junit.jupiter.api.Test;

import net.boricj.bft.image.relocs.priv.BitShiftedAdjustedFieldCodec;
import net.boricj.bft.image.relocs.priv.BitShiftedFieldCodec;
import net.boricj.bft.image.relocs.priv.ByteFieldCodec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageRelocationModelTest {

	@Test
	void relocationGroupsOwnOrderedGangsAndEntries() {
		ImageFile image = new ImageFile(ImageFile.Kind.OBJECT);
		ImageSection text = image.sections().create(".text");
		ImageSymbol symbol = image.symbols().create("target", text, 0);
		ImageRelocationGroup relocation = text.relocations().create(RelocationOperation.ABSOLUTE, symbol, 0x12340000L);
		ImageRelocationGang hiGang =
				relocation.gangs().create(BitShiftedAdjustedFieldCodec.of(2, ByteOrder.LITTLE_ENDIAN, 0, 16, 16, true));
		ImageRelocationGang loGang = relocation.gangs().create(ByteFieldCodec.S16LE);

		hiGang.entries().create(0);
		loGang.entries().create(4, 0x5678L);

		assertEquals(2, relocation.gangs().size());
		assertEquals(1, hiGang.entries().size());
		assertEquals(1, loGang.entries().size());
		assertEquals(0x12345678L, loGang.entries().getFirst().getAddend());
	}

	@Test
	void semanticEqualityUsesGroupStructureRatherThanIds() {
		ImageFile left = new ImageFile(ImageFile.Kind.OBJECT);
		ImageSection leftSection = left.sections().create(".text");
		ImageSymbol leftSymbol = left.symbols().create("target", leftSection, 0);
		ImageRelocationGroup leftRelocation =
				leftSection.relocations().create(RelocationOperation.REGION_RELATIVE, leftSymbol, 0);
		leftRelocation
				.gangs()
				.create(BitShiftedFieldCodec.of(4, ByteOrder.LITTLE_ENDIAN, 0, 26, 2, true))
				.entries()
				.create(8);

		ImageFile right = new ImageFile(ImageFile.Kind.OBJECT);
		ImageSection rightSection = right.sections().create(".text");
		ImageSymbol rightSymbol = right.symbols().create("target", rightSection, 0);
		ImageRelocationGroup rightRelocation =
				rightSection.relocations().create(RelocationOperation.REGION_RELATIVE, rightSymbol, 0);
		rightRelocation
				.gangs()
				.create(BitShiftedFieldCodec.of(4, ByteOrder.LITTLE_ENDIAN, 0, 26, 2, true))
				.entries()
				.create(8);

		assertTrue(left.semanticallyEquals(right));
	}

	@Test
	void gangFieldCodecsSupportReadMaskWriteContract() {
		var hiCodec = BitShiftedAdjustedFieldCodec.of(2, ByteOrder.LITTLE_ENDIAN, 0, 16, 16, true);
		byte[] data = new byte[] {0x00, 0x00, 0x00, 0x00};
		ByteFieldCodec.S32LE.write(data, 0, -4L);
		assertEquals(-4L, ByteFieldCodec.S32LE.read(data, 0));
		byte[] hiField = new byte[] {0x00, 0x00};
		hiCodec.write(hiField, 0, -65536L);
		assertEquals(-65536L, hiCodec.read(hiField, 0));
	}

	@Test
	void mipsHi16AdjustAppliesBiasBeforeShift() {
		var codec = BitShiftedAdjustedFieldCodec.of(2, ByteOrder.LITTLE_ENDIAN, 0, 16, 16, true);
		byte[] field = new byte[] {0x00, 0x00};

		codec.write(field, 0, 0x00008000L);

		assertEquals(0x01, Byte.toUnsignedInt(field[0]));
		assertEquals(0x00, Byte.toUnsignedInt(field[1]));
		assertEquals(0x00010000L, codec.read(field, 0));
	}

	@Test
	void mipsRegrel26ShiftedPreservesInstructionHighBits() {
		var codec = BitShiftedFieldCodec.of(4, ByteOrder.LITTLE_ENDIAN, 0, 26, 2, true);
		byte[] instruction = new byte[] {0x00, 0x00, 0x00, 0x08};

		codec.write(instruction, 0, 0x10L);

		assertEquals(0x04, Byte.toUnsignedInt(instruction[0]));
		assertEquals(0x00, Byte.toUnsignedInt(instruction[1]));
		assertEquals(0x00, Byte.toUnsignedInt(instruction[2]));
		assertEquals(0x08, Byte.toUnsignedInt(instruction[3]));
		assertEquals(0x10L, codec.read(instruction, 0));
	}
}
