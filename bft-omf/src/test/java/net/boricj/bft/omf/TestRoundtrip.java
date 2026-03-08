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
package net.boricj.bft.omf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

import net.boricj.bft.TestUtils;

public class TestRoundtrip {
	@Test
	public void test_hello_world_i386_pc_windows_omf() throws IOException, URISyntaxException {
		File file = new File(
				getClass().getResource("hello-world_i386-pc-windows-omf.obj").toURI());

		OmfFile omf;
		try (FileInputStream fis = new FileInputStream(file)) {
			omf = new OmfFile.Parser(fis).parse();
		}

		// Write the OMF file to a byte array
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		omf.write(baos);
		byte[] actualBytes = baos.toByteArray();

		// Read the expected bytes from the reference file
		byte[] expectedBytes;
		try (FileInputStream fis = new FileInputStream(file)) {
			expectedBytes = fis.readAllBytes();
		}

		// Compare the bytes
		TestUtils.assertArrayEquals(expectedBytes, actualBytes);
	}

	@Test
	public void test_ascii_table_i386_pc_windows_omf() throws IOException, URISyntaxException {
		File file = new File(
				getClass().getResource("ascii-table_i386-pc-windows-omf.obj").toURI());

		OmfFile omf;
		try (FileInputStream fis = new FileInputStream(file)) {
			omf = new OmfFile.Parser(fis).parse();
		}

		// Write the OMF file to a byte array
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		omf.write(baos);
		byte[] actualBytes = baos.toByteArray();

		// Read the expected bytes from the reference file
		byte[] expectedBytes;
		try (FileInputStream fis = new FileInputStream(file)) {
			expectedBytes = fis.readAllBytes();
		}

		// Compare the bytes
		TestUtils.assertArrayEquals(expectedBytes, actualBytes);
	}
}
