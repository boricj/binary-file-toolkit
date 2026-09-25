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

import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.boricj.bft.TestUtils;
import net.boricj.bft.dwarf.constants.DwarfLineContentType;
import net.boricj.bft.dwarf.constants.DwarfVersion;
import net.boricj.bft.dwarf.model.DwarfStringReferenceValue;
import net.boricj.bft.dwarf.model.DwarfStringValue;
import net.boricj.bft.dwarf.model.DwarfUnsignedValue;
import net.boricj.bft.dwarf.model.DwarfValue;

public class DwarfLineInfoTest {
	@Test
	public void testRoundTripAsciiTableX86_64Dwarf2LineInfo() throws Exception {
		DwarfTestResources.DwarfFixture fixture =
				DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.X86_64_LINUX_GNU, 2);
		assertLineInfoInvariantsAndRoundTrip(fixture, DwarfVersion.DWARF2, 8);
	}

	@Test
	public void testRoundTripAsciiTableMipsDwarf2LineInfo() throws Exception {
		DwarfTestResources.DwarfFixture fixture =
				DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.MIPS_LINUX_GNU, 2);
		assertLineInfoInvariantsAndRoundTrip(fixture, DwarfVersion.DWARF3, 4);
	}

	@Test
	public void testRoundTripAsciiTableX86_64Dwarf5LineInfo() throws Exception {
		DwarfTestResources.DwarfFixture fixture =
				DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.X86_64_LINUX_GNU, 5);
		assertLineInfoInvariantsAndRoundTrip(fixture, DwarfVersion.DWARF5, 8);
	}

	@Test
	public void testRoundTripAsciiTableMipsDwarf5LineInfo() throws Exception {
		DwarfTestResources.DwarfFixture fixture =
				DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.MIPS_LINUX_GNU, 5);
		assertLineInfoInvariantsAndRoundTrip(fixture, DwarfVersion.DWARF3, 4);
	}

	@Test
	public void testParseTreatsOpcodeEqualToOpcodeBaseAsSpecialOpcode() throws Exception {
		byte[] debugLineSection = new byte[] {
			0x12, 0x00, 0x00, 0x00, 0x02, 0x00, 0x08, 0x00, 0x00, 0x00, 0x01, 0x01, 0x00, 0x01, 0x02, 0x00, 0x00, 0x00,
			0x02, 0x00, 0x01, 0x01
		};

		DwarfLineInfo lineInfo = DwarfLineInfo.parse(debugLineSection, ByteOrder.LITTLE_ENDIAN, null, 4);
		DwarfLineInfo.DwarfLineUnit unit = lineInfo.getUnits().getFirst();

		Assertions.assertInstanceOf(
				DwarfLineInfo.DwarfLineSpecialOpcode.class, unit.instructions().getFirst());
		Assertions.assertEquals(2, unit.rows().size());
		Assertions.assertTrue(unit.rows().getLast().endSequence());
		TestUtils.assertArrayEquals(debugLineSection, lineInfo.toByteArray());
	}

	@Test
	public void testParseRejectsLineStrpWithoutLineStringTable() {
		byte[] debugLineSection = new byte[] {
			0x19, 0x00, 0x00, 0x00, 0x05, 0x00, 0x04, 0x00, 0x0e, 0x00, 0x00, 0x00, 0x01, 0x01, 0x01, 0x00, 0x01, 0x01,
			0x01, 0x01, 0x1f, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x01
		};

		IllegalArgumentException ex = Assertions.assertThrows(
				IllegalArgumentException.class,
				() -> DwarfLineInfo.parse(debugLineSection, ByteOrder.LITTLE_ENDIAN, null, 4));
		Assertions.assertTrue(ex.getMessage().contains("DW_FORM_line_strp requires a .debug_line_str table"));
	}

	@Test
	public void testRoundTripPreservesUnknownDwarf5LineEntryContentType() throws Exception {
		byte[] debugLineSection = new byte[] {
			0x19,
			0x00,
			0x00,
			0x00,
			0x05,
			0x00,
			0x04,
			0x00,
			0x0e,
			0x00,
			0x00,
			0x00,
			0x01,
			0x01,
			0x01,
			0x00,
			0x01,
			0x01,
			0x01,
			(byte) 0x80,
			0x40,
			0x0b,
			0x01,
			0x7f,
			0x00,
			0x00,
			0x00,
			0x01,
			0x01
		};

		DwarfLineInfo lineInfo = DwarfLineInfo.parse(debugLineSection, ByteOrder.LITTLE_ENDIAN, null, 4);
		TestUtils.assertArrayEquals(debugLineSection, lineInfo.toByteArray());
	}

	@Test
	public void testParseRejectsDuplicateDwarf5LineEntryContentTypeCodes() throws Exception {
		DwarfLineInfo lineInfo = new DwarfLineInfo(
				ByteOrder.LITTLE_ENDIAN,
				List.of(new DwarfLineInfo.DwarfLineUnit(
						0,
						new DwarfLineInfo.DwarfLineHeader(
								0,
								false,
								DwarfVersion.DWARF5,
								4,
								0,
								0,
								1,
								1,
								true,
								0,
								1,
								1,
								List.of(),
								List.of(
										new DwarfLineInfo.DwarfLineEntryFormat(
												0x01,
												DwarfLineContentType.DW_LNCT_path,
												net.boricj.bft.dwarf.constants.DwarfForm.DW_FORM_data1),
										new DwarfLineInfo.DwarfLineEntryFormat(
												0x01,
												DwarfLineContentType.DW_LNCT_path,
												net.boricj.bft.dwarf.constants.DwarfForm.DW_FORM_data1)),
								List.of(),
								List.of(),
								List.of()),
						List.of(new DwarfLineInfo.DwarfLineEndSequence()),
						List.of())));

		byte[] bytes = lineInfo.toByteArray();
		IllegalArgumentException ex = Assertions.assertThrows(
				IllegalArgumentException.class, () -> DwarfLineInfo.parse(bytes, ByteOrder.LITTLE_ENDIAN, null, 4));
		Assertions.assertTrue(ex.getMessage().contains("duplicate content type code"));
	}

	@Test
	public void testWriteRejectsOutOfRangeData1LineEntryValue() {
		DwarfLineInfo lineInfo = new DwarfLineInfo(
				ByteOrder.LITTLE_ENDIAN,
				List.of(new DwarfLineInfo.DwarfLineUnit(
						0,
						new DwarfLineInfo.DwarfLineHeader(
								0,
								false,
								DwarfVersion.DWARF5,
								4,
								0,
								0,
								1,
								1,
								true,
								0,
								1,
								1,
								List.of(),
								List.of(new DwarfLineInfo.DwarfLineEntryFormat(
										0x01,
										DwarfLineContentType.DW_LNCT_path,
										net.boricj.bft.dwarf.constants.DwarfForm.DW_FORM_data1)),
								List.of(new DwarfLineInfo.DwarfLineDirectoryEntry(
										Map.of(), Map.of(0x01L, new DwarfUnsignedValue(0x100L)))),
								List.of(),
								List.of()),
						List.of(new DwarfLineInfo.DwarfLineEndSequence()),
						List.of())));

		IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, lineInfo::toByteArray);
		Assertions.assertTrue(ex.getMessage().contains("does not fit in 1 byte"));
	}

	@Test
	public void testParseRejectsZeroLineRange() {
		byte[] debugLineSection = new byte[] {
			0x12, 0x00, 0x00, 0x00, 0x02, 0x00, 0x08, 0x00, 0x00, 0x00, 0x01, 0x01, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00,
			0x02, 0x00, 0x01, 0x01
		};

		IllegalArgumentException ex = Assertions.assertThrows(
				IllegalArgumentException.class,
				() -> DwarfLineInfo.parse(debugLineSection, ByteOrder.LITTLE_ENDIAN, null, 4));
		Assertions.assertTrue(ex.getMessage().contains("line_range must be non-zero"));
	}

	@Test
	public void testWriteRejectsInvalidData16LineEntryLength() {
		DwarfLineInfo lineInfo = new DwarfLineInfo(
				ByteOrder.LITTLE_ENDIAN,
				List.of(new DwarfLineInfo.DwarfLineUnit(
						0,
						new DwarfLineInfo.DwarfLineHeader(
								0,
								false,
								DwarfVersion.DWARF5,
								4,
								0,
								0,
								1,
								1,
								true,
								0,
								1,
								1,
								List.of(),
								List.of(new DwarfLineInfo.DwarfLineEntryFormat(
										0x01,
										DwarfLineContentType.DW_LNCT_path,
										net.boricj.bft.dwarf.constants.DwarfForm.DW_FORM_data16)),
								List.of(new DwarfLineInfo.DwarfLineDirectoryEntry(
										Map.of(),
										Map.of(0x01L, new net.boricj.bft.dwarf.model.DwarfBlockValue(new byte[15])))),
								List.of(),
								List.of()),
						List.of(new DwarfLineInfo.DwarfLineEndSequence()),
						List.of())));

		IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, lineInfo::toByteArray);
		Assertions.assertTrue(ex.getMessage().contains("DW_FORM_data16 value must be exactly 16 bytes"));
	}

	@Test
	public void testWriteRejectsOutOfRangeSetAddressInstruction() {
		DwarfLineInfo lineInfo = new DwarfLineInfo(
				ByteOrder.LITTLE_ENDIAN,
				List.of(new DwarfLineInfo.DwarfLineUnit(
						0,
						new DwarfLineInfo.DwarfLineHeader(
								0,
								false,
								DwarfVersion.DWARF5,
								1,
								0,
								0,
								1,
								1,
								true,
								0,
								1,
								1,
								List.of(),
								List.of(),
								List.of(),
								List.of(),
								List.of()),
						List.of(
								new DwarfLineInfo.DwarfLineSetAddress(0x100L),
								new DwarfLineInfo.DwarfLineEndSequence()),
						List.of())));

		IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, lineInfo::toByteArray);
		Assertions.assertTrue(ex.getMessage().contains("DW_LNE_set_address does not fit in 1 byte"));
	}

	private static void assertLineInfoInvariantsAndRoundTrip(
			DwarfTestResources.DwarfFixture fixture, DwarfVersion expectedVersion, int expectedAddressSize)
			throws Exception {
		DwarfLineInfo lineInfo = fixture.debugLine();

		DwarfLineInfo.DwarfLineUnit unit = lineInfo.getUnits().getFirst();
		Assertions.assertEquals(expectedVersion, unit.header().version());
		Assertions.assertEquals(expectedAddressSize, unit.header().addressSize());
		Assertions.assertTrue(unit.header().directories().size() >= 1);
		Assertions.assertTrue(unit.header().files().size() >= 1);
		Assertions.assertEquals(
				"main.c", path(unit.header().files().get(0).values().get(DwarfLineContentType.DW_LNCT_path)));
		if (expectedVersion.isAtLeast(DwarfVersion.DWARF5)) {
			Assertions.assertEquals(
					1L,
					unsigned(unit.header().files().get(0).values().get(DwarfLineContentType.DW_LNCT_directory_index)));
		}
		Assertions.assertTrue(unit.rows().size() >= 10);
		Assertions.assertTrue(unit.rows().getFirst().address() > 0);
		Assertions.assertTrue(unit.rows().getFirst().line() >= 1);
		Assertions.assertTrue(unit.rows().getLast().endSequence());

		TestUtils.assertArrayEquals(fixture.debugLineBytes(), lineInfo.toByteArray());
	}

	private static String path(DwarfValue value) {
		if (value instanceof DwarfStringValue stringValue) {
			return stringValue.value();
		}
		if (value instanceof DwarfStringReferenceValue stringReferenceValue) {
			return stringReferenceValue.value();
		}
		throw new IllegalArgumentException("Unsupported path value: " + value);
	}

	private static long unsigned(DwarfValue value) {
		return ((DwarfUnsignedValue) value).value();
	}
}
