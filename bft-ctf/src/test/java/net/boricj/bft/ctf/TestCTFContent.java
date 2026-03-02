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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import net.boricj.bft.TestUtils;
import net.boricj.bft.elf.ElfFile;
import net.boricj.bft.elf.ElfSection;
import net.boricj.bft.elf.sections.ElfProgBits;
import org.junit.jupiter.api.Test;

/**
 * Tests for CTF content validation.
 * Verifies that we correctly parse the structure and contents of CTF data.
 */
public class TestCTFContent {
	@Test
	public void testHelloWorldI686HeaderAndSections() throws Exception {
		File file = new File(getClass()
				.getResource("hello-world_i686-linux-gnu.o")
				.toURI());

		byte[] ctfData = extractCTFSection(file);

		// Parse CTF
		CTFDictionary.Parser parser = new CTFDictionary.Parser();
		CTFDictionary dict = parser.parse(ctfData);

		// Validate header
		CTFHeader header = dict.getHeader();
		assertEquals(0xdff2, header.getMagic(), "Magic should be 0xdff2");
		assertEquals(4, header.getVersion(), "Version should be 4 (CTF_VERSION_3)");
		assertEquals(2, header.getFlags(), "Flags should include CTF_F_NEWFUNCINFO");

		// Debug: print out all header offsets and sizes
		System.out.printf(
				"[TEST DEBUG] Header offsets: func(%d,0x%x) type(%d,0x%x) str(%d,0x%x)%n",
				header.getFuncOffset(), header.getFuncOffset(),
				header.getTypeOffset(), header.getTypeOffset(),
				header.getStrOffset(), header.getStrOffset());
		System.out.printf(
				"[TEST DEBUG] Header sizes: func(%d,0x%x) type(%d,0x%x) str(%d,0x%x)%n",
				header.getFuncSize(), header.getFuncSize(),
				header.getTypeSize(), header.getTypeSize(),
				header.getStrSize(), header.getStrSize());

		// Validate section offsets (from CTF header in file)
		// Function info section: 0x0 -- 0x7 (0x8 bytes)
		// Function index section: 0x8 -- 0xf (0x8 bytes)
		// Type section: 0x10 onwards (size determined by header)
		// String section: offset and size from header
		// Note: offsets are relative to start of CTF data
		assertEquals(0x8, header.getFuncOffset(), "Function info+index should start at offset 0x8");
		assertEquals(0x10, header.getFuncSize(), "Function info+index section should be 0x10 bytes total");

		assertEquals(0x10, header.getTypeOffset(), "Type section should start at offset 0x10");
		assertEquals(0x88, header.getTypeSize(), "Type section should be 0x88 bytes");

		assertEquals(0x8b, header.getStrOffset(), "String section should start at offset 0x8b");
		assertEquals(0x6, header.getStrSize(), "String section should be 0x6 bytes");

		// Validate string section was parsed
		CTFStringSection strings = dict.getStrings();
		assertNotNull(strings);
		assertNotNull(strings.getStrings());
		// String section size is 0x6 bytes, so we should have a small number of strings
		assertTrue(strings.getStrings().size() > 0, "String table should have at least one entry");

		// Validate type section was parsed
		CTFTypeSection types = dict.getTypes();
		assertNotNull(types);
		// Type section is 0x88 bytes, so we should have type records
		assertTrue(types.getTypes().size() > 0, "Should have at least some type records");
		// for this artifact we expect exactly 6 types (rounded-trip after parsing)
		assertEquals(6, types.getTypes().size(), "Expected six types in sample");
		// verify kind enums for the first couple records
		CTFType t1 = types.getType(1);
		assertNotNull(t1);
		assertEquals(CTFType.Kind.INTEGER, t1.getKind(), "first type should be INTEGER");
		CTFType t2 = types.getType(2);
		assertNotNull(t2);
		assertEquals(CTFType.Kind.FUNCTION, t2.getKind(), "second type should be FUNCTION pointer");
	}

	@Test
	public void testHelloWorldX86_64HeaderAndSections() throws Exception {
		File file = new File(getClass()
				.getResource("hello-world_x86_64-linux-gnu.o")
				.toURI());

		byte[] ctfData = extractCTFSection(file);

		// Parse CTF
		CTFDictionary.Parser parser = new CTFDictionary.Parser();
		CTFDictionary dict = parser.parse(ctfData);

		// Validate header
		CTFHeader header = dict.getHeader();
		assertEquals(0xdff2, header.getMagic(), "Magic should be 0xdff2");
		assertEquals(4, header.getVersion(), "Version should be 4");

		// Should have string and type sections
		CTFStringSection strings = dict.getStrings();
		assertNotNull(strings);
		assertNotNull(strings.getStrings());

		CTFTypeSection types = dict.getTypes();
		assertNotNull(types);
		assertNotNull(types.getTypes());

		// Both should be non-empty
		assert !strings.getStrings().isEmpty() : "String table should not be empty";
		assert !types.getTypes().isEmpty() : "Type table should not be empty";
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
