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
package net.boricj.bft.codeview;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

import net.boricj.bft.TestUtils;
import net.boricj.bft.codeview.constants.CodeViewSectionNames;
import net.boricj.bft.coff.CoffFile;
import net.boricj.bft.coff.CoffSection;
import net.boricj.bft.coff.sections.CoffBytes;

public class TestParser {
	@Test
	public void test_hello_world_i686_pc_windows_msvc_debug_S() throws IOException, URISyntaxException {
		compareCodeViewSections("hello-world_i686-pc-windows-msvc.z7.obj", CodeViewSectionNames._DEBUG_S);
	}

	@Test
	public void test_hello_world_i686_pc_windows_msvc_debug_T() throws IOException, URISyntaxException {
		compareCodeViewSections("hello-world_i686-pc-windows-msvc.z7.obj", CodeViewSectionNames._DEBUG_T);
	}

	private void compareCodeViewSections(String filename, String section) throws IOException, URISyntaxException {
		CoffBytes coff_section;
		CoffBytes s_debug_S;
		CoffBytes s_debug_T;

		File file = new File(getClass().getResource(filename).toURI());

		try (FileInputStream fis = new FileInputStream(file)) {
			CoffFile coff = new CoffFile.Parser(fis).parse();

			s_debug_S = (CoffBytes)
					TestUtils.findBy(coff.getSections(), CoffSection::getName, CodeViewSectionNames._DEBUG_S);
			s_debug_T = (CoffBytes)
					TestUtils.findBy(coff.getSections(), CoffSection::getName, CodeViewSectionNames._DEBUG_T);
			coff_section = (CoffBytes) TestUtils.findBy(coff.getSections(), CoffSection::getName, section);
		}

		CodeViewFile codeViewFile = new CodeViewFile.Parser()
				.addSection(CodeViewSectionNames._DEBUG_S, s_debug_S.getBytes())
				.addSection(CodeViewSectionNames._DEBUG_T, s_debug_T.getBytes())
				.parse();

		CodeViewTable cv_table = TestUtils.findBy(codeViewFile, CodeViewTable::getName, section);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		cv_table.write(baos);

		byte[] expected_bytes = coff_section.getBytes();
		byte[] actual_bytes = baos.toByteArray();

		TestUtils.assertArrayEquals(expected_bytes, actual_bytes);
	}
}
