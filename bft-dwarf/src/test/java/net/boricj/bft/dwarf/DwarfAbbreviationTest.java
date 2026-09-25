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

public class DwarfAbbreviationTest {
	@Test
	public void testRoundTripAsciiTableX86_64Dwarf2Abbreviations() throws Exception {
		DwarfTestResources.DwarfFixture fixture =
				DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.X86_64_LINUX_GNU, 2);
		assertAbbreviationTableBasicInvariantsAndRoundTrip(fixture);
	}

	@Test
	public void testRoundTripAsciiTableMipsDwarf2Abbreviations() throws Exception {
		DwarfTestResources.DwarfFixture fixture =
				DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.MIPS_LINUX_GNU, 2);
		assertAbbreviationTableBasicInvariantsAndRoundTrip(fixture);
	}

	@Test
	public void testRoundTripAsciiTableX86_64Dwarf5Abbreviations() throws Exception {
		DwarfTestResources.DwarfFixture fixture =
				DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.X86_64_LINUX_GNU, 5);
		assertAbbreviationTableBasicInvariantsAndRoundTrip(fixture);
	}

	@Test
	public void testRoundTripAsciiTableMipsDwarf5Abbreviations() throws Exception {
		DwarfTestResources.DwarfFixture fixture =
				DwarfTestResources.fixture(DwarfTestResources.FixtureTarget.MIPS_LINUX_GNU, 5);
		assertAbbreviationTableBasicInvariantsAndRoundTrip(fixture);
	}

	private static void assertAbbreviationTableBasicInvariantsAndRoundTrip(DwarfTestResources.DwarfFixture fixture)
			throws Exception {
		DwarfAbbreviationTable table = fixture.abbreviationTable();

		Assertions.assertTrue(table.getDeclarationsByCode().size() >= 10);
		Assertions.assertTrue(table.getDeclarationsByCode().containsKey(1L));
		Assertions.assertFalse(
				table.getDeclarationsByCode().get(1L).getAttributes().isEmpty());
		Assertions.assertNotNull(table.getDeclarationsByCode().get(2L));
		Assertions.assertTrue(
				table.getDeclarationsByCode().get(2L).getAttributes().size() >= 2);

		TestUtils.assertArrayEquals(fixture.debugAbbrevBytes(), table.toByteArray());
	}

	@Test
	public void testWriteAsciiTableDwarf5AbbreviationsFromSemanticObjects() throws Exception {
		Map<Long, DwarfAbbreviationDeclaration> declarations = new LinkedHashMap<>();
		declarations.put(
				1L,
				new DwarfAbbreviationDeclaration(
						1L,
						DwarfTag.DW_TAG_base_type.getValue(),
						DwarfTag.DW_TAG_base_type,
						false,
						List.of(
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_byte_size.getValue(),
										DwarfAttributeName.DW_AT_byte_size,
										DwarfForm.DW_FORM_data1,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_encoding.getValue(),
										DwarfAttributeName.DW_AT_encoding,
										DwarfForm.DW_FORM_data1,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_name.getValue(),
										DwarfAttributeName.DW_AT_name,
										DwarfForm.DW_FORM_strp,
										null))));
		declarations.put(
				2L,
				new DwarfAbbreviationDeclaration(
						2L,
						DwarfTag.DW_TAG_variable.getValue(),
						DwarfTag.DW_TAG_variable,
						false,
						List.of(
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_name.getValue(),
										DwarfAttributeName.DW_AT_name,
										DwarfForm.DW_FORM_string,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_decl_file.getValue(),
										DwarfAttributeName.DW_AT_decl_file,
										DwarfForm.DW_FORM_implicit_const,
										1L),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_decl_line.getValue(),
										DwarfAttributeName.DW_AT_decl_line,
										DwarfForm.DW_FORM_data1,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_decl_column.getValue(),
										DwarfAttributeName.DW_AT_decl_column,
										DwarfForm.DW_FORM_data1,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_type.getValue(),
										DwarfAttributeName.DW_AT_type,
										DwarfForm.DW_FORM_ref4,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_location.getValue(),
										DwarfAttributeName.DW_AT_location,
										DwarfForm.DW_FORM_exprloc,
										null))));
		declarations.put(
				3L,
				new DwarfAbbreviationDeclaration(
						3L,
						DwarfTag.DW_TAG_compile_unit.getValue(),
						DwarfTag.DW_TAG_compile_unit,
						true,
						List.of(
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_producer.getValue(),
										DwarfAttributeName.DW_AT_producer,
										DwarfForm.DW_FORM_strp,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_language.getValue(),
										DwarfAttributeName.DW_AT_language,
										DwarfForm.DW_FORM_data1,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_name.getValue(),
										DwarfAttributeName.DW_AT_name,
										DwarfForm.DW_FORM_line_strp,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_comp_dir.getValue(),
										DwarfAttributeName.DW_AT_comp_dir,
										DwarfForm.DW_FORM_line_strp,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_low_pc.getValue(),
										DwarfAttributeName.DW_AT_low_pc,
										DwarfForm.DW_FORM_addr,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_high_pc.getValue(),
										DwarfAttributeName.DW_AT_high_pc,
										DwarfForm.DW_FORM_data8,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_stmt_list.getValue(),
										DwarfAttributeName.DW_AT_stmt_list,
										DwarfForm.DW_FORM_sec_offset,
										null))));
		declarations.put(
				4L,
				new DwarfAbbreviationDeclaration(
						4L,
						DwarfTag.DW_TAG_base_type.getValue(),
						DwarfTag.DW_TAG_base_type,
						false,
						List.of(
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_byte_size.getValue(),
										DwarfAttributeName.DW_AT_byte_size,
										DwarfForm.DW_FORM_data1,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_encoding.getValue(),
										DwarfAttributeName.DW_AT_encoding,
										DwarfForm.DW_FORM_data1,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_name.getValue(),
										DwarfAttributeName.DW_AT_name,
										DwarfForm.DW_FORM_string,
										null))));
		declarations.put(
				5L,
				new DwarfAbbreviationDeclaration(
						5L,
						DwarfTag.DW_TAG_const_type.getValue(),
						DwarfTag.DW_TAG_const_type,
						false,
						List.of(new DwarfAbbreviationAttribute(
								DwarfAttributeName.DW_AT_type.getValue(),
								DwarfAttributeName.DW_AT_type,
								DwarfForm.DW_FORM_ref4,
								null))));
		declarations.put(
				6L,
				new DwarfAbbreviationDeclaration(
						6L,
						DwarfTag.DW_TAG_pointer_type.getValue(),
						DwarfTag.DW_TAG_pointer_type,
						false,
						List.of(
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_byte_size.getValue(),
										DwarfAttributeName.DW_AT_byte_size,
										DwarfForm.DW_FORM_data1,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_type.getValue(),
										DwarfAttributeName.DW_AT_type,
										DwarfForm.DW_FORM_ref4,
										null))));
		declarations.put(
				7L,
				new DwarfAbbreviationDeclaration(
						7L,
						DwarfTag.DW_TAG_subprogram.getValue(),
						DwarfTag.DW_TAG_subprogram,
						true,
						List.of(
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_external.getValue(),
										DwarfAttributeName.DW_AT_external,
										DwarfForm.DW_FORM_flag_present,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_name.getValue(),
										DwarfAttributeName.DW_AT_name,
										DwarfForm.DW_FORM_strp,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_decl_file.getValue(),
										DwarfAttributeName.DW_AT_decl_file,
										DwarfForm.DW_FORM_data1,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_decl_line.getValue(),
										DwarfAttributeName.DW_AT_decl_line,
										DwarfForm.DW_FORM_data2,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_decl_column.getValue(),
										DwarfAttributeName.DW_AT_decl_column,
										DwarfForm.DW_FORM_data1,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_prototyped.getValue(),
										DwarfAttributeName.DW_AT_prototyped,
										DwarfForm.DW_FORM_flag_present,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_type.getValue(),
										DwarfAttributeName.DW_AT_type,
										DwarfForm.DW_FORM_ref4,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_declaration.getValue(),
										DwarfAttributeName.DW_AT_declaration,
										DwarfForm.DW_FORM_flag_present,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_sibling.getValue(),
										DwarfAttributeName.DW_AT_sibling,
										DwarfForm.DW_FORM_ref4,
										null))));
		declarations.put(
				8L,
				new DwarfAbbreviationDeclaration(
						8L,
						DwarfTag.DW_TAG_formal_parameter.getValue(),
						DwarfTag.DW_TAG_formal_parameter,
						false,
						List.of(new DwarfAbbreviationAttribute(
								DwarfAttributeName.DW_AT_type.getValue(),
								DwarfAttributeName.DW_AT_type,
								DwarfForm.DW_FORM_ref4,
								null))));
		declarations.put(
				9L,
				new DwarfAbbreviationDeclaration(
						9L,
						DwarfTag.DW_TAG_unspecified_parameters.getValue(),
						DwarfTag.DW_TAG_unspecified_parameters,
						false,
						List.of()));
		declarations.put(
				10L,
				new DwarfAbbreviationDeclaration(
						10L,
						DwarfTag.DW_TAG_subprogram.getValue(),
						DwarfTag.DW_TAG_subprogram,
						true,
						List.of(
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_external.getValue(),
										DwarfAttributeName.DW_AT_external,
										DwarfForm.DW_FORM_flag_present,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_name.getValue(),
										DwarfAttributeName.DW_AT_name,
										DwarfForm.DW_FORM_strp,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_decl_file.getValue(),
										DwarfAttributeName.DW_AT_decl_file,
										DwarfForm.DW_FORM_data1,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_decl_line.getValue(),
										DwarfAttributeName.DW_AT_decl_line,
										DwarfForm.DW_FORM_data1,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_decl_column.getValue(),
										DwarfAttributeName.DW_AT_decl_column,
										DwarfForm.DW_FORM_data1,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_prototyped.getValue(),
										DwarfAttributeName.DW_AT_prototyped,
										DwarfForm.DW_FORM_flag_present,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_type.getValue(),
										DwarfAttributeName.DW_AT_type,
										DwarfForm.DW_FORM_ref4,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_low_pc.getValue(),
										DwarfAttributeName.DW_AT_low_pc,
										DwarfForm.DW_FORM_addr,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_high_pc.getValue(),
										DwarfAttributeName.DW_AT_high_pc,
										DwarfForm.DW_FORM_data8,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_frame_base.getValue(),
										DwarfAttributeName.DW_AT_frame_base,
										DwarfForm.DW_FORM_exprloc,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_call_all_tail_calls.getValue(),
										DwarfAttributeName.DW_AT_call_all_tail_calls,
										DwarfForm.DW_FORM_flag_present,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_sibling.getValue(),
										DwarfAttributeName.DW_AT_sibling,
										DwarfForm.DW_FORM_ref4,
										null))));
		declarations.put(
				11L,
				new DwarfAbbreviationDeclaration(
						11L,
						DwarfTag.DW_TAG_lexical_block.getValue(),
						DwarfTag.DW_TAG_lexical_block,
						true,
						List.of(
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_low_pc.getValue(),
										DwarfAttributeName.DW_AT_low_pc,
										DwarfForm.DW_FORM_addr,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_high_pc.getValue(),
										DwarfAttributeName.DW_AT_high_pc,
										DwarfForm.DW_FORM_data8,
										null))));
		declarations.put(
				12L,
				new DwarfAbbreviationDeclaration(
						12L,
						DwarfTag.DW_TAG_subprogram.getValue(),
						DwarfTag.DW_TAG_subprogram,
						true,
						List.of(
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_name.getValue(),
										DwarfAttributeName.DW_AT_name,
										DwarfForm.DW_FORM_strp,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_decl_file.getValue(),
										DwarfAttributeName.DW_AT_decl_file,
										DwarfForm.DW_FORM_data1,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_decl_line.getValue(),
										DwarfAttributeName.DW_AT_decl_line,
										DwarfForm.DW_FORM_data1,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_decl_column.getValue(),
										DwarfAttributeName.DW_AT_decl_column,
										DwarfForm.DW_FORM_data1,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_prototyped.getValue(),
										DwarfAttributeName.DW_AT_prototyped,
										DwarfForm.DW_FORM_flag_present,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_type.getValue(),
										DwarfAttributeName.DW_AT_type,
										DwarfForm.DW_FORM_ref4,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_low_pc.getValue(),
										DwarfAttributeName.DW_AT_low_pc,
										DwarfForm.DW_FORM_addr,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_high_pc.getValue(),
										DwarfAttributeName.DW_AT_high_pc,
										DwarfForm.DW_FORM_data8,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_frame_base.getValue(),
										DwarfAttributeName.DW_AT_frame_base,
										DwarfForm.DW_FORM_exprloc,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_call_all_calls.getValue(),
										DwarfAttributeName.DW_AT_call_all_calls,
										DwarfForm.DW_FORM_flag_present,
										null))));
		declarations.put(
				13L,
				new DwarfAbbreviationDeclaration(
						13L,
						DwarfTag.DW_TAG_formal_parameter.getValue(),
						DwarfTag.DW_TAG_formal_parameter,
						false,
						List.of(
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_name.getValue(),
										DwarfAttributeName.DW_AT_name,
										DwarfForm.DW_FORM_string,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_decl_file.getValue(),
										DwarfAttributeName.DW_AT_decl_file,
										DwarfForm.DW_FORM_data1,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_decl_line.getValue(),
										DwarfAttributeName.DW_AT_decl_line,
										DwarfForm.DW_FORM_data1,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_decl_column.getValue(),
										DwarfAttributeName.DW_AT_decl_column,
										DwarfForm.DW_FORM_data1,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_type.getValue(),
										DwarfAttributeName.DW_AT_type,
										DwarfForm.DW_FORM_ref4,
										null),
								new DwarfAbbreviationAttribute(
										DwarfAttributeName.DW_AT_location.getValue(),
										DwarfAttributeName.DW_AT_location,
										DwarfForm.DW_FORM_exprloc,
										null))));

		DwarfAbbreviationTable table = new DwarfAbbreviationTable(declarations);
		byte[] bytes = table.toByteArray();
		DwarfAbbreviationTable reparsed =
				DwarfAbbreviationParser.parseFirstTable(bytes, DwarfVersion.DWARF5, ByteOrder.LITTLE_ENDIAN);

		Assertions.assertEquals(
				table.getDeclarationsByCode().size(),
				reparsed.getDeclarationsByCode().size());
		Assertions.assertEquals(
				table.getDeclarationsByCode().get(1L).getTag(),
				reparsed.getDeclarationsByCode().get(1L).getTag());
		Assertions.assertEquals(
				table.getDeclarationsByCode().get(10L).getAttributes().size(),
				reparsed.getDeclarationsByCode().get(10L).getAttributes().size());
		TestUtils.assertArrayEquals(bytes, reparsed.toByteArray());
	}
}
