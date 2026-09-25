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
package net.boricj.bft.dwarf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.boricj.bft.TestUtils;
import net.boricj.bft.dwarf.constants.DwarfVersion;

public class DwarfArangesTest {
	@Test
	public void testRoundTripAsciiTableX86_64Dwarf2Aranges() throws Exception {
		DwarfTestResources.DwarfFixture fixture =
				DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.X86_64_LINUX_GNU, 2);
		assertArangesInvariantsAndRoundTrip(fixture, DwarfVersion.DWARF2);
	}

	@Test
	public void testRoundTripAsciiTableMipsDwarf2Aranges() throws Exception {
		DwarfTestResources.DwarfFixture fixture =
				DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.MIPS_LINUX_GNU, 2);
		assertArangesInvariantsAndRoundTrip(fixture, DwarfVersion.DWARF2);
	}

	@Test
	public void testRoundTripAsciiTableX86_64Dwarf5Aranges() throws Exception {
		DwarfTestResources.DwarfFixture fixture =
				DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.X86_64_LINUX_GNU, 5);
		assertArangesInvariantsAndRoundTrip(fixture, DwarfVersion.DWARF2);
	}

	@Test
	public void testRoundTripAsciiTableMipsDwarf5Aranges() throws Exception {
		DwarfTestResources.DwarfFixture fixture =
				DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.MIPS_LINUX_GNU, 5);
		assertArangesInvariantsAndRoundTrip(fixture, DwarfVersion.DWARF2);
	}

	private static void assertArangesInvariantsAndRoundTrip(
			DwarfTestResources.DwarfFixture fixture, DwarfVersion expectedVersion) throws Exception {
		DwarfAranges aranges = fixture.debugAranges();
		DwarfAranges.DwarfArangesSet set = aranges.getSets().getFirst();

		Assertions.assertEquals(expectedVersion, set.version());
		Assertions.assertEquals(0L, set.debugInfoOffset());
		Assertions.assertTrue(set.addressSize() == 4 || set.addressSize() == 8);
		Assertions.assertEquals(0, set.segmentSize());
		Assertions.assertEquals(1, aranges.getSets().size());
		Assertions.assertFalse(set.descriptors().isEmpty());
		Assertions.assertTrue(set.descriptors().getFirst().address() > 0);
		Assertions.assertTrue(set.descriptors().getFirst().length() > 0);

		TestUtils.assertArrayEquals(fixture.debugArangesBytes(), aranges.toByteArray());
	}
}
