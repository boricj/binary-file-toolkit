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

public class DwarfStringTableTest {
	@Test
	public void testRoundTripX86_64DebugStrWithSuffixSharing() throws Exception {
		byte[] bytes = DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.X86_64_LINUX_GNU, 2)
				.debugStrBytes();
		assertDebugStrRoundTripInvariants(bytes, "-gdwarf-2", "main");
	}

	@Test
	public void testRoundTripMipsDebugStrWithSuffixSharing() throws Exception {
		byte[] bytes = DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.MIPS_LINUX_GNU, 2)
				.debugStrBytes();
		assertDebugStrRoundTripInvariants(bytes, "-gdwarf-2", "main");
	}

	private static void assertDebugStrRoundTripInvariants(byte[] bytes, String producerSnippet, String symbolSnippet)
			throws Exception {
		DwarfStringTable table = DwarfStringTable.parse(bytes);

		Assertions.assertTrue(
				table.getEntriesByOffset().values().stream().anyMatch(value -> value.contains(producerSnippet)));
		Assertions.assertTrue(
				table.getEntriesByOffset().values().stream().anyMatch(value -> value.contains(symbolSnippet)));
		TestUtils.assertArrayEquals(bytes, table.toByteArray());
	}

	@Test
	public void testRoundTripX86_64DebugLineStr() throws Exception {
		byte[] bytes = DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.X86_64_LINUX_GNU, 5)
				.debugLineStrBytes();
		Assertions.assertNotNull(bytes);
		DwarfStringTable table = DwarfStringTable.parse(bytes);

		Assertions.assertTrue(table.getEntriesByOffset().size() >= 2);
		TestUtils.assertArrayEquals(bytes, table.toByteArray());
	}

	@Test
	public void testMipsDwarf5HasNoDebugLineStrSection() throws Exception {
		byte[] bytes = DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.MIPS_LINUX_GNU, 5)
				.debugLineStrBytes();
		Assertions.assertNull(bytes);
	}
}
