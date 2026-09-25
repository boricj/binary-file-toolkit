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

import org.junit.jupiter.api.Test;

import net.boricj.bft.TestUtils;

public class DwarfAllVersionsRoundTripTest {
	@Test
	public void testRoundTripModeledSectionsAcrossAllCheckedInVersions() throws Exception {
		for (DwarfTestResources.FixtureTarget target : DwarfTestResources.FixtureTarget.values()) {
			for (int version = 2; version <= 5; version++) {
				DwarfTestResources.DwarfFixture fixture = DwarfTestResources.fixture(target, version);
				TestUtils.assertArrayEquals(
						fixture.debugArangesBytes(), fixture.debugAranges().toByteArray());
				TestUtils.assertArrayEquals(
						fixture.debugAbbrevBytes(), fixture.abbreviationTable().toByteArray());
				TestUtils.assertArrayEquals(
						fixture.debugInfoBytes(), fixture.debugInfo().toByteArray());
				TestUtils.assertArrayEquals(
						fixture.debugStrBytes(), fixture.debugStrings().toByteArray());

				DwarfFile.Parser parser = new DwarfFile.Parser(
								fixture.byteOrder(), fixture.debugAbbrevBytes(), fixture.debugInfoBytes())
						.setDebugAranges(fixture.debugArangesBytes())
						.setDebugStr(fixture.debugStrBytes())
						.setDebugLine(fixture.debugLineBytes());

				DwarfLineInfo lineInfo = DwarfLineInfo.parse(
						fixture.debugLineBytes(),
						fixture.byteOrder(),
						fixture.debugLineStrings(),
						fixture.debugInfo()
								.getCompilationUnits()
								.getFirst()
								.getHeader()
								.addressSize());
				TestUtils.assertArrayEquals(fixture.debugLineBytes(), lineInfo.toByteArray());
				if (fixture.debugLineStrBytes() != null) {
					parser.setDebugLineStr(fixture.debugLineStrBytes());
				}

				DwarfFile dwarfFile = parser.parse();
				TestUtils.assertArrayEquals(fixture.debugArangesBytes(), dwarfFile.writeDebugAranges());
				TestUtils.assertArrayEquals(fixture.debugInfoBytes(), dwarfFile.writeDebugInfo());
				TestUtils.assertArrayEquals(fixture.debugAbbrevBytes(), dwarfFile.writeDebugAbbrev());
				TestUtils.assertArrayEquals(fixture.debugStrBytes(), dwarfFile.writeDebugStr());
				TestUtils.assertArrayEquals(fixture.debugLineBytes(), dwarfFile.writeDebugLine());
				if (fixture.debugLineStrBytes() != null && fixture.debugLineStrings() != null) {
					TestUtils.assertArrayEquals(
							fixture.debugLineStrBytes(),
							fixture.debugLineStrings().toByteArray());
					TestUtils.assertArrayEquals(fixture.debugLineStrBytes(), dwarfFile.writeDebugLineStr());
				}
			}
		}
	}
}
