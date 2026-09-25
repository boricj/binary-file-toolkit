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
package net.boricj.bft.image.elf;

import net.boricj.bft.image.ImageFile;
import net.boricj.bft.image.ImageRelocationEntry;
import net.boricj.bft.image.ImageRelocationGang;
import net.boricj.bft.image.ImageRelocationGroup;
import net.boricj.bft.image.ImageSection;
import net.boricj.bft.image.ImageSymbol;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class ImageFileAssertions {
	private ImageFileAssertions() {
		// Utility class
	}

	static void assertImageEquals(ImageFile expected, ImageFile actual) {
		assertEquals(expected.getKind(), actual.getKind());
		assertEquals(expected.sections().size(), actual.sections().size());
		for (int sectionIndex = 0; sectionIndex < expected.sections().size(); sectionIndex++) {
			ImageSection expectedSection = expected.sections().get(sectionIndex);
			ImageSection actualSection = actual.sections().get(sectionIndex);
			assertEquals(expectedSection.getName(), actualSection.getName());
			assertArrayEquals(expectedSection.getContents(), actualSection.getContents());
			assertEquals(expectedSection.getLogicalSize(), actualSection.getLogicalSize());
			assertEquals(
					expectedSection.relocations().size(),
					actualSection.relocations().size());
			for (int relocationIndex = 0;
					relocationIndex < expectedSection.relocations().size();
					relocationIndex++) {
				ImageRelocationGroup expectedRelocation =
						expectedSection.relocations().get(relocationIndex);
				ImageRelocationGroup actualRelocation =
						actualSection.relocations().get(relocationIndex);
				assertEquals(expectedRelocation.getOperation(), actualRelocation.getOperation());
				assertEquals(expectedRelocation.getAddend(), actualRelocation.getAddend());
				assertSymbolEquals(expectedRelocation.getTarget(), actualRelocation.getTarget());
				assertEquals(
						expectedRelocation.gangs().size(),
						actualRelocation.gangs().size());
				for (int gangIndex = 0; gangIndex < expectedRelocation.gangs().size(); gangIndex++) {
					ImageRelocationGang expectedGang =
							expectedRelocation.gangs().get(gangIndex);
					ImageRelocationGang actualGang = actualRelocation.gangs().get(gangIndex);
					assertEquals(expectedGang.getFieldCodec(), actualGang.getFieldCodec());
					assertEquals(
							expectedGang.entries().size(), actualGang.entries().size());
					for (int entryIndex = 0; entryIndex < expectedGang.entries().size(); entryIndex++) {
						ImageRelocationEntry expectedEntry =
								expectedGang.entries().get(entryIndex);
						ImageRelocationEntry actualEntry = actualGang.entries().get(entryIndex);
						assertEquals(expectedEntry.getOffset(), actualEntry.getOffset());
						assertEquals(expectedEntry.getLocalAddend(), actualEntry.getLocalAddend());
					}
				}
			}
		}
		assertEquals(expected.symbols().size(), actual.symbols().size());
		for (int symbolIndex = 0; symbolIndex < expected.symbols().size(); symbolIndex++) {
			assertSymbolEquals(
					expected.symbols().get(symbolIndex), actual.symbols().get(symbolIndex));
		}
	}

	static void assertSymbolEquals(ImageSymbol expected, ImageSymbol actual) {
		assertNotNull(expected);
		assertNotNull(actual);
		assertEquals(expected.getName(), actual.getName());
		assertEquals(
				expected.getSection() == null ? null : expected.getSection().getName(),
				actual.getSection() == null ? null : actual.getSection().getName());
		assertEquals(expected.getOffset(), actual.getOffset());
		assertEquals(expected.getSize(), actual.getSize());
		assertEquals(expected.getType(), actual.getType());
		assertEquals(expected.getVisibility(), actual.getVisibility());
		assertEquals(expected.getBinding(), actual.getBinding());
	}
}
