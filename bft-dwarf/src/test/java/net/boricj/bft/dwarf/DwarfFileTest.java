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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.boricj.bft.TestUtils;
import net.boricj.bft.dwarf.constants.DwarfAttributeName;
import net.boricj.bft.dwarf.constants.DwarfForm;
import net.boricj.bft.dwarf.constants.DwarfTag;
import net.boricj.bft.dwarf.constants.DwarfVersion;
import net.boricj.bft.dwarf.model.DwarfAbbreviationAttribute;
import net.boricj.bft.dwarf.model.DwarfAbbreviationDeclaration;
import net.boricj.bft.dwarf.model.DwarfAbbreviationTable;

public class DwarfFileTest {
	@Test
	public void testRoundTripModeledSectionsFromX86_64Dwarf5ElfFixture() throws Exception {
		DwarfTestResources.DwarfFixture fixture =
				DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.X86_64_LINUX_GNU, 5);
		DwarfFile dwarfFile = new DwarfFile.Builder(fixture.byteOrder())
				.setDebugAranges(fixture.debugAranges())
				.setDebugStrings(fixture.debugStrings())
				.setDebugLineStrings(fixture.debugLineStrings())
				.setDebugInfo(fixture.debugInfo())
				.setDebugLine(fixture.debugLine())
				.build();
		TestUtils.assertArrayEquals(fixture.debugArangesBytes(), dwarfFile.writeDebugAranges());
		TestUtils.assertArrayEquals(fixture.debugAbbrevBytes(), dwarfFile.writeDebugAbbrev());
		TestUtils.assertArrayEquals(fixture.debugInfoBytes(), dwarfFile.writeDebugInfo());
		TestUtils.assertArrayEquals(fixture.debugStrBytes(), dwarfFile.writeDebugStr());
		TestUtils.assertArrayEquals(fixture.debugLineStrBytes(), dwarfFile.writeDebugLineStr());
		TestUtils.assertArrayEquals(fixture.debugLineBytes(), dwarfFile.writeDebugLine());
	}

	@Test
	public void testParserLoadsRawSectionBytesFromX86_64Dwarf4Fixture() throws Exception {
		DwarfTestResources.DwarfFixture fixture =
				DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.X86_64_LINUX_GNU, 4);
		DwarfFile dwarfFile = new DwarfFile.Parser(
						fixture.byteOrder(), fixture.debugAbbrevBytes(), fixture.debugInfoBytes())
				.setDebugAranges(fixture.debugArangesBytes())
				.setDebugStr(fixture.debugStrBytes())
				.setDebugLine(fixture.debugLineBytes())
				.parse();

		TestUtils.assertArrayEquals(fixture.debugArangesBytes(), dwarfFile.writeDebugAranges());
		TestUtils.assertArrayEquals(fixture.debugAbbrevBytes(), dwarfFile.writeDebugAbbrev());
		TestUtils.assertArrayEquals(fixture.debugInfoBytes(), dwarfFile.writeDebugInfo());
		TestUtils.assertArrayEquals(fixture.debugStrBytes(), dwarfFile.writeDebugStr());
		TestUtils.assertArrayEquals(fixture.debugLineBytes(), dwarfFile.writeDebugLine());
	}

	@Test
	public void testParserLoadsRawSectionBytesFromMipsDwarf4FixtureWithoutDebugLineDecode() throws Exception {
		DwarfTestResources.DwarfFixture fixture =
				DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.MIPS_LINUX_GNU, 4);
		DwarfFile dwarfFile = new DwarfFile.Parser(
						fixture.byteOrder(), fixture.debugAbbrevBytes(), fixture.debugInfoBytes())
				.setDebugAranges(fixture.debugArangesBytes())
				.setDebugStr(fixture.debugStrBytes())
				.setDebugLine(fixture.debugLineBytes())
				.parse();

		TestUtils.assertArrayEquals(fixture.debugArangesBytes(), dwarfFile.writeDebugAranges());
		TestUtils.assertArrayEquals(fixture.debugAbbrevBytes(), dwarfFile.writeDebugAbbrev());
		TestUtils.assertArrayEquals(fixture.debugInfoBytes(), dwarfFile.writeDebugInfo());
		TestUtils.assertArrayEquals(fixture.debugStrBytes(), dwarfFile.writeDebugStr());
		TestUtils.assertArrayEquals(fixture.debugLineBytes(), dwarfFile.writeDebugLine());
	}

	@Test
	public void testBuilderRejectsDwarf5FormInDwarf2Artifact() {
		Map<Long, DwarfAbbreviationDeclaration> declarations = new LinkedHashMap<>();
		declarations.put(
				1L,
				new DwarfAbbreviationDeclaration(
						1L,
						DwarfTag.DW_TAG_compile_unit.getValue(),
						DwarfTag.DW_TAG_compile_unit,
						false,
						List.of(new DwarfAbbreviationAttribute(
								DwarfAttributeName.DW_AT_name.getValue(),
								DwarfAttributeName.DW_AT_name,
								DwarfForm.DW_FORM_line_strp,
								null))));

		DwarfAbbreviationTable table = new DwarfAbbreviationTable(declarations);
		Assertions.assertThrows(
				IllegalArgumentException.class,
				() -> DwarfAbbreviationValidator.validateSupportedIn(table, DwarfVersion.DWARF2));
	}

	@Test
	public void testBuilderRejectsMissingDebugLineFromX86_64Fixture() throws Exception {
		DwarfTestResources.DwarfFixture fixture =
				DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.X86_64_LINUX_GNU, 2);
		Assertions.assertThrows(IllegalStateException.class, () -> new DwarfFile.Builder(fixture.byteOrder())
				.setDebugAranges(fixture.debugAranges())
				.setDebugStrings(fixture.debugStrings())
				.setDebugInfo(fixture.debugInfo())
				.build());
	}

	@Test
	public void testBuilderRejectsMissingDebugLineFromMipsFixture() throws Exception {
		DwarfTestResources.DwarfFixture fixture =
				DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.MIPS_LINUX_GNU, 2);
		Assertions.assertThrows(IllegalStateException.class, () -> new DwarfFile.Builder(fixture.byteOrder())
				.setDebugAranges(fixture.debugAranges())
				.setDebugStrings(fixture.debugStrings())
				.setDebugInfo(fixture.debugInfo())
				.build());
	}
}
