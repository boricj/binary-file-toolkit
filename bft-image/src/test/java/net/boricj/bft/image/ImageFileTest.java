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

import org.junit.jupiter.api.Test;

import net.boricj.bft.image.relocs.priv.ByteFieldCodec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageFileTest {

	@Test
	void sectionsAndSymbolsAreOwnedByTheirCollections() {
		ImageFile image = new ImageFile(ImageFile.Kind.OBJECT);
		ImageSection text = image.sections().create(".text");
		text.setContents(new byte[] {0x01, 0x02, 0x03});
		ImageSymbol symbol = image.symbols().create("entry", text, 1);
		text.relocations().createSingleEntry(1, RelocationOperation.ABSOLUTE, ByteFieldCodec.U32LE, symbol, 0x44);

		assertEquals(1, image.sections().size());
		assertEquals(text, image.sections().getFirst());
		assertEquals(1, image.symbols().size());
		assertEquals(symbol, image.symbols().getFirst());
		assertEquals(1, text.relocations().size());
		assertEquals(1, image.relocations().size());
		assertEquals(1, image.relocationEntries().size());
	}

	@Test
	void semanticEqualityIgnoresObjectIdentity() {
		ImageFile left = new ImageFile(ImageFile.Kind.EXECUTABLE);
		ImageSection leftSection = left.sections().create(".text");
		leftSection.setContents(new byte[] {0x01, 0x02});
		left.symbols().create("entry", leftSection, 0);

		ImageFile right = new ImageFile(ImageFile.Kind.EXECUTABLE);
		ImageSection rightSection = right.sections().create(".text");
		rightSection.setContents(new byte[] {0x01, 0x02});
		right.symbols().create("entry", rightSection, 0);

		assertTrue(left.semanticallyEquals(right));
	}
}
