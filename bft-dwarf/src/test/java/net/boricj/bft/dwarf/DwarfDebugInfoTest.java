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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.boricj.bft.TestUtils;
import net.boricj.bft.dwarf.constants.DwarfAttributeName;
import net.boricj.bft.dwarf.constants.DwarfForm;
import net.boricj.bft.dwarf.constants.DwarfLanguage;
import net.boricj.bft.dwarf.constants.DwarfTag;
import net.boricj.bft.dwarf.constants.DwarfUnitType;
import net.boricj.bft.dwarf.constants.DwarfVersion;
import net.boricj.bft.dwarf.model.DwarfAbbreviationAttribute;
import net.boricj.bft.dwarf.model.DwarfAbbreviationDeclaration;
import net.boricj.bft.dwarf.model.DwarfAbbreviationTable;
import net.boricj.bft.dwarf.model.DwarfAddressValue;
import net.boricj.bft.dwarf.model.DwarfCompilationUnit;
import net.boricj.bft.dwarf.model.DwarfCompilationUnitHeader;
import net.boricj.bft.dwarf.model.DwarfDie;
import net.boricj.bft.dwarf.model.DwarfDieAttribute;
import net.boricj.bft.dwarf.model.DwarfFlagValue;
import net.boricj.bft.dwarf.model.DwarfReferenceValue;
import net.boricj.bft.dwarf.model.DwarfStringReferenceValue;
import net.boricj.bft.dwarf.model.DwarfStringValue;
import net.boricj.bft.dwarf.model.DwarfUnsignedValue;
import net.boricj.bft.dwarf.model.DwarfValue;

public class DwarfDebugInfoTest {
	@Test
	public void testRoundTripAsciiTableX86_64Dwarf2Info() throws Exception {
		DwarfTestResources.DwarfFixture fixture =
				DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.X86_64_LINUX_GNU, 2);
		assertDebugInfoInvariantsAndRoundTrip(fixture, DwarfVersion.DWARF2, DwarfLanguage.DW_LANG_C99, true);
	}

	@Test
	public void testRoundTripAsciiTableMipsDwarf2Info() throws Exception {
		DwarfTestResources.DwarfFixture fixture =
				DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.MIPS_LINUX_GNU, 2);
		assertDebugInfoInvariantsAndRoundTrip(fixture, DwarfVersion.DWARF2, DwarfLanguage.DW_LANG_C99, false);
	}

	@Test
	public void testRoundTripAsciiTableX86_64Dwarf5Info() throws Exception {
		DwarfTestResources.DwarfFixture fixture =
				DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.X86_64_LINUX_GNU, 5);
		assertDebugInfoInvariantsAndRoundTrip(fixture, DwarfVersion.DWARF5, DwarfLanguage.DW_LANG_C11, true);
	}

	@Test
	public void testRoundTripAsciiTableMipsDwarf5Info() throws Exception {
		DwarfTestResources.DwarfFixture fixture =
				DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.MIPS_LINUX_GNU, 5);
		assertDebugInfoInvariantsAndRoundTrip(fixture, DwarfVersion.DWARF5, DwarfLanguage.DW_LANG_C11, false);
	}

	private static void assertDebugInfoInvariantsAndRoundTrip(
			DwarfTestResources.DwarfFixture fixture,
			DwarfVersion expectedVersion,
			DwarfLanguage expectedLanguage,
			boolean expectLineStringName)
			throws Exception {
		DwarfDebugInfo info = fixture.debugInfo();

		Assertions.assertEquals(1, info.getCompilationUnits().size());
		DwarfCompilationUnit unit = info.getCompilationUnits().getFirst();
		Assertions.assertEquals(expectedVersion, unit.getHeader().version());
		if (expectedVersion.isAtLeast(DwarfVersion.DWARF5)) {
			Assertions.assertEquals(
					DwarfUnitType.DW_UT_compile, unit.getHeader().unitType());
		} else {
			Assertions.assertNull(unit.getHeader().unitType());
		}

		DwarfDie root = unit.getDies().getFirst();
		Assertions.assertEquals(DwarfTag.DW_TAG_compile_unit, root.getTag());
		DwarfValue languageValue = DwarfDebugInfo.getAttributeValue(root, DwarfAttributeName.DW_AT_language);
		Assertions.assertEquals(
				expectedLanguage, DwarfLanguage.valueFromOrNull(((DwarfUnsignedValue) languageValue).value()));
		DwarfStringReferenceValue producer =
				(DwarfStringReferenceValue) DwarfDebugInfo.getAttributeValue(root, DwarfAttributeName.DW_AT_producer);
		Assertions.assertTrue(producer.value().contains("-gdwarf-" + expectedVersion.getValue()));
		if (expectedVersion.isAtLeast(DwarfVersion.DWARF5)) {
			Assertions.assertEquals(
					expectLineStringName,
					((DwarfStringReferenceValue) DwarfDebugInfo.getAttributeValue(root, DwarfAttributeName.DW_AT_name))
							.lineString());
		}

		List<String> subprogramNames = flatten(root).stream()
				.filter(die -> die.getTag() == DwarfTag.DW_TAG_subprogram)
				.map(die -> valueToString(DwarfDebugInfo.getAttributeValue(die, DwarfAttributeName.DW_AT_name)))
				.collect(Collectors.toList());
		Assertions.assertTrue(subprogramNames.contains("main"));

		DwarfDie mainDie = flatten(root).stream()
				.filter(die -> die.getTag() == DwarfTag.DW_TAG_subprogram)
				.filter(die -> "main"
						.equals(valueToString(DwarfDebugInfo.getAttributeValue(die, DwarfAttributeName.DW_AT_name))))
				.findFirst()
				.orElseThrow();
		Assertions.assertEquals(
				true,
				((DwarfFlagValue) DwarfDebugInfo.getAttributeValue(mainDie, DwarfAttributeName.DW_AT_external))
						.value());
		Assertions.assertTrue(
				unsignedOrAddress(DwarfDebugInfo.getAttributeValue(mainDie, DwarfAttributeName.DW_AT_high_pc)) > 0);

		TestUtils.assertArrayEquals(fixture.debugInfoBytes(), info.toByteArray());
	}

	@Test
	public void testWriteAsciiTableX86_64Dwarf5InfoFromSemanticObjects() throws Exception {
		DwarfTestResources.DwarfFixture fixture =
				DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.X86_64_LINUX_GNU, 5);
		DwarfDebugInfo original = fixture.debugInfo();
		DwarfDebugInfo copied = new DwarfDebugInfo(
				original.getByteOrder(),
				original.getAbbreviationTable(),
				new DwarfStringTable(original.getDebugStrings().getEntriesByOffset()),
				new DwarfStringTable(original.getDebugLineStrings().getEntriesByOffset()),
				original.getCompilationUnits().stream()
						.map(DwarfDebugInfoTest::copyCompilationUnit)
						.toList());

		TestUtils.assertArrayEquals(fixture.debugInfoBytes(), copied.toByteArray());
	}

	@Test
	public void testRefAddrUsesAddressSizeInDwarf2Units() throws Exception {
		DwarfAbbreviationTable abbreviationTable = singleRefAddrAbbreviationTable();
		DwarfDebugInfo debugInfo = new DwarfDebugInfo(
				ByteOrder.LITTLE_ENDIAN,
				abbreviationTable,
				null,
				null,
				List.of(new DwarfCompilationUnit(
						0,
						new DwarfCompilationUnitHeader(0, false, DwarfVersion.DWARF2, null, 0, 8),
						List.of(singleRefAddrDie(0x1122_3344_5566_7788L)))));

		byte[] bytes = debugInfo.toByteArray();
		Assertions.assertEquals(20, bytes.length);
		Assertions.assertArrayEquals(
				new byte[] {(byte) 0x88, 0x77, 0x66, 0x55, 0x44, 0x33, 0x22, 0x11},
				java.util.Arrays.copyOfRange(bytes, 12, 20));

		DwarfDebugInfo reparsed = DwarfDebugInfo.parse(bytes, ByteOrder.LITTLE_ENDIAN, abbreviationTable, null, null);
		Assertions.assertEquals(
				0x1122_3344_5566_7788L,
				((DwarfReferenceValue) reparsed.getCompilationUnits()
								.getFirst()
								.getDies()
								.getFirst()
								.getAttributes()
								.getFirst()
								.value())
						.offset());
	}

	@Test
	public void testRefAddrUsesOffsetSizeInDwarf5Units() throws Exception {
		DwarfAbbreviationTable abbreviationTable = singleRefAddrAbbreviationTable();
		DwarfDebugInfo debugInfo = new DwarfDebugInfo(
				ByteOrder.LITTLE_ENDIAN,
				abbreviationTable,
				null,
				null,
				List.of(new DwarfCompilationUnit(
						0,
						new DwarfCompilationUnitHeader(
								0, false, DwarfVersion.DWARF5, DwarfUnitType.DW_UT_compile, 0, 8),
						List.of(singleRefAddrDie(0x1234_5678L)))));

		byte[] bytes = debugInfo.toByteArray();
		Assertions.assertEquals(17, bytes.length);
		Assertions.assertArrayEquals(new byte[] {0x78, 0x56, 0x34, 0x12}, java.util.Arrays.copyOfRange(bytes, 13, 17));

		DwarfDebugInfo reparsed = DwarfDebugInfo.parse(bytes, ByteOrder.LITTLE_ENDIAN, abbreviationTable, null, null);
		Assertions.assertEquals(
				0x1234_5678L,
				((DwarfReferenceValue) reparsed.getCompilationUnits()
								.getFirst()
								.getDies()
								.getFirst()
								.getAttributes()
								.getFirst()
								.value())
						.offset());
	}

	@Test
	public void testWriterRejectsDieAttributesThatDoNotMatchAbbreviationDeclaration() {
		DwarfAbbreviationTable abbreviationTable = singleRefAddrAbbreviationTable();
		DwarfDie mismatchedDie = new DwarfDie(
				0,
				1,
				DwarfTag.DW_TAG_compile_unit.getValue(),
				DwarfTag.DW_TAG_compile_unit,
				List.of(new DwarfDieAttribute(
						DwarfAttributeName.DW_AT_name.getValue(),
						DwarfAttributeName.DW_AT_name,
						DwarfForm.DW_FORM_data4,
						new DwarfUnsignedValue(1))),
				List.of());

		DwarfDebugInfo debugInfo = new DwarfDebugInfo(
				ByteOrder.LITTLE_ENDIAN,
				abbreviationTable,
				null,
				null,
				List.of(new DwarfCompilationUnit(
						0,
						new DwarfCompilationUnitHeader(
								0, false, DwarfVersion.DWARF5, DwarfUnitType.DW_UT_compile, 0, 8),
						List.of(mismatchedDie))));

		IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, debugInfo::toByteArray);
		Assertions.assertTrue(ex.getMessage().contains("does not match abbreviation declaration"));
	}

	@Test
	public void testParseRejectsOversizedBlockLength() {
		Map<Long, DwarfAbbreviationDeclaration> declarations = new LinkedHashMap<>();
		declarations.put(
				1L,
				new DwarfAbbreviationDeclaration(
						1L,
						DwarfTag.DW_TAG_compile_unit.getValue(),
						DwarfTag.DW_TAG_compile_unit,
						false,
						List.of(new DwarfAbbreviationAttribute(
								DwarfAttributeName.DW_AT_location.getValue(),
								DwarfAttributeName.DW_AT_location,
								DwarfForm.DW_FORM_block4,
								null))));
		DwarfAbbreviationTable abbreviationTable = new DwarfAbbreviationTable(declarations);

		byte[] debugInfoBytes = new byte[] {
			0x0c, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0x01, 0x00, 0x00, 0x00, (byte) 0x80
		};

		IllegalArgumentException ex = Assertions.assertThrows(
				IllegalArgumentException.class,
				() -> DwarfDebugInfo.parse(debugInfoBytes, ByteOrder.LITTLE_ENDIAN, abbreviationTable, null, null));
		Assertions.assertTrue(ex.getMessage().contains("DWARF block length"));
	}

	private static DwarfCompilationUnit copyCompilationUnit(DwarfCompilationUnit unit) {
		return new DwarfCompilationUnit(
				unit.getSectionOffset(),
				unit.getHeader(),
				unit.getDies().stream().map(DwarfDebugInfoTest::copyDie).toList());
	}

	private static DwarfDie copyDie(DwarfDie die) {
		return new DwarfDie(
				die.getOffset(),
				die.getAbbreviationCode(),
				die.getRawTagCode(),
				die.getTag(),
				die.getAttributes(),
				die.getChildren().stream().map(DwarfDebugInfoTest::copyDie).toList());
	}

	private static List<DwarfDie> flatten(DwarfDie root) {
		List<DwarfDie> result = new ArrayList<>();
		result.add(root);
		for (DwarfDie child : root.getChildren()) {
			result.addAll(flatten(child));
		}
		return result;
	}

	private static String valueToString(DwarfValue value) {
		if (value instanceof DwarfStringReferenceValue stringReferenceValue) {
			return stringReferenceValue.value();
		}
		if (value instanceof DwarfStringValue stringValue) {
			return stringValue.value();
		}
		throw new IllegalArgumentException("Unsupported string value type: " + value);
	}

	private static long unsignedOrAddress(DwarfValue value) {
		if (value instanceof DwarfUnsignedValue unsignedValue) {
			return unsignedValue.value();
		}
		if (value instanceof DwarfAddressValue addressValue) {
			return addressValue.value();
		}
		throw new IllegalArgumentException("Unsupported numeric value type: " + value);
	}

	private static DwarfAbbreviationTable singleRefAddrAbbreviationTable() {
		Map<Long, DwarfAbbreviationDeclaration> declarations = new LinkedHashMap<>();
		declarations.put(
				1L,
				new DwarfAbbreviationDeclaration(
						1L,
						DwarfTag.DW_TAG_compile_unit.getValue(),
						DwarfTag.DW_TAG_compile_unit,
						false,
						List.of(new DwarfAbbreviationAttribute(
								DwarfAttributeName.DW_AT_sibling.getValue(),
								DwarfAttributeName.DW_AT_sibling,
								DwarfForm.DW_FORM_ref_addr,
								null))));
		return new DwarfAbbreviationTable(declarations);
	}

	private static DwarfDie singleRefAddrDie(long referenceOffset) {
		return new DwarfDie(
				0,
				1,
				DwarfTag.DW_TAG_compile_unit.getValue(),
				DwarfTag.DW_TAG_compile_unit,
				List.of(new DwarfDieAttribute(
						DwarfAttributeName.DW_AT_sibling.getValue(),
						DwarfAttributeName.DW_AT_sibling,
						DwarfForm.DW_FORM_ref_addr,
						new DwarfReferenceValue(referenceOffset))),
				List.of());
	}
}
