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
package net.boricj.bft.ctf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import net.boricj.bft.TestUtils;
import net.boricj.bft.elf.ElfFile;
import net.boricj.bft.elf.ElfSection;
import net.boricj.bft.elf.sections.ElfProgBits;
import org.junit.jupiter.api.Test;

/**
 * Tests for CTF parsing and round-trip serialization.
 * Uses hello-world ELF object files compiled with GCC -gctf option.
 */
public class TestCTFRoundTrip {
	@Test
	public void testHelloWorldI686RoundTrip() throws Exception {
		File file = new File(getClass()
				.getResource("hello-world_i686-linux-gnu.o")
				.toURI());

		byte[] ctfData = extractCTFSection(file);
		assertNotNull(ctfData, "CTF section not found");

		// Parse CTF dictionary
		CTFDictionary.Parser parser = new CTFDictionary.Parser();
		CTFDictionary dict = parser.parse(ctfData);
		assertNotNull(dict);
		assertNotNull(dict.getHeader());

		// Verify header magic
		assertEquals(0xdff2, dict.getHeader().getMagic());

		// Write back and compare
		byte[] written = dict.write();
		assertArrayEquals(ctfData, written, "CTF round-trip failed: written bytes do not match original");
	}

	@Test
	public void testHelloWorldX86_64RoundTrip() throws Exception {
		File file = new File(getClass()
				.getResource("hello-world_x86_64-linux-gnu.o")
				.toURI());

		byte[] ctfData = extractCTFSection(file);
		assertNotNull(ctfData, "CTF section not found");

		// Parse CTF dictionary
		CTFDictionary.Parser parser = new CTFDictionary.Parser();
		CTFDictionary dict = parser.parse(ctfData);
		assertNotNull(dict);

		// Write back and compare
		byte[] written = dict.write();
		assertArrayEquals(ctfData, written, "CTF round-trip failed: written bytes do not match original");
	}

	private byte[] extractCTFSection(File file) throws Exception {
		try (FileInputStream fis = new FileInputStream(file)) {
			ElfFile elf = new ElfFile.Parser(fis).parse();

			ElfProgBits ctfSection = (ElfProgBits) TestUtils.findBy(elf.getSections(), ElfSection::getName, ".ctf");
			if (ctfSection == null) {
				throw new CTFException("CTF section (.ctf) not found in ELF file");
			}

			return ctfSection.getBytes();
		}
	}
}

