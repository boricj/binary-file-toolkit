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
package net.boricj.bft.image.coff;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import net.boricj.bft.coff.CoffFile;
import net.boricj.bft.coff.CoffRelocationTable;
import net.boricj.bft.coff.CoffSection;
import net.boricj.bft.coff.CoffSectionTable;
import net.boricj.bft.coff.CoffSymbolTable;
import net.boricj.bft.coff.CoffSymbolTable.CoffSymbol;
import net.boricj.bft.coff.constants.CoffMachine;
import net.boricj.bft.coff.constants.CoffStorageClass;
import net.boricj.bft.coff.machines.i386.CoffRelocationType_i386;
import net.boricj.bft.coff.sections.CoffBytes;
import net.boricj.bft.coff.sections.CoffUninitialized;
import net.boricj.bft.image.ImageFile;

import static net.boricj.bft.coff.constants.CoffStorageClass.IMAGE_SYM_CLASS_EXTERNAL;
import static net.boricj.bft.coff.machines.i386.CoffRelocationType_i386.IMAGE_REL_I386_REL32;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoffImageImportExportRoundTripTest {
	private record FixtureSpec(String resourcePath, CoffMachine machine) {}

	private static Stream<FixtureSpec> fixtures() {
		return Stream.of(
				new FixtureSpec(
						"/net/boricj/bft/coff/i386/hello-world_i686-pc-windows-msvc.obj",
						CoffMachine.IMAGE_FILE_MACHINE_I386),
				new FixtureSpec(
						"/net/boricj/bft/coff/amd64/hello-world_amd64-pc-windows-msvc.obj",
						CoffMachine.IMAGE_FILE_MACHINE_AMD64));
	}

	@ParameterizedTest
	@MethodSource("fixtures")
	void test_fixture_round_trip(FixtureSpec fixture) throws IOException {
		CoffFile expected = parseFixture(fixture.resourcePath());
		assertEquals(fixture.machine(), expected.getHeader().getMachine());

		ImageFile importedImage = new CoffImporter(expected).importImage();
		assertNotNull(importedImage);
		assertFalse(importedImage.sections().isEmpty());

		CoffFile actual = new CoffExporter(importedImage, new CoffFile.Builder(fixture.machine())).exportFile();
		assertEquals(fixture.machine(), actual.getHeader().getMachine());

		assertCoffSemanticsEquals(expected, actual);
	}

	@Test
	void test_hello_world_obj_symbol_and_relocation_smoke() throws IOException {
		CoffFile expected = parseFixture("/net/boricj/bft/coff/i386/hello-world_i686-pc-windows-msvc.obj");

		ImageFile importedImage = new CoffImporter(expected).importImage();
		CoffFile actual =
				new CoffExporter(importedImage, new CoffFile.Builder(CoffMachine.IMAGE_FILE_MACHINE_I386)).exportFile();

		CoffSectionTable actualSections = actual.getSections();
		var actual_text = findSectionByName(actualSections, ".text$mn", CoffBytes.class);
		var actual_data = findSectionByName(actualSections, ".data", CoffBytes.class);

		CoffRelocationTable actual_rel_text = actual_text.getRelocations();
		CoffSymbolTable actual_symtab = actual.getSymbols();
		short actual_text_index = sectionNumber(actualSections, actual_text);
		short actual_data_index = sectionNumber(actualSections, actual_data);

		assertTrue(actual_text.getCharacteristics().isCntCode());
		assertTrue(actual_text.getCharacteristics().isMemExecute());
		assertTrue(actual_text.getCharacteristics().isMemRead());
		assertFalse(actual_text.getCharacteristics().isMemWrite());
		assertTrue(actual_data.getCharacteristics().isCntInitializedData());
		assertTrue(actual_data.getCharacteristics().isMemRead());
		assertTrue(actual_data.getCharacteristics().isMemWrite());
		assertFalse(actual_data.getCharacteristics().isMemExecute());

		assertSymbol(actual_symtab, actual_text_index, 0x00000000, "_main", IMAGE_SYM_CLASS_EXTERNAL);
		assertSymbol(actual_symtab, actual_data_index, 0x00000000, "$SG7446", IMAGE_SYM_CLASS_EXTERNAL);
		assertUndefined(actual_symtab, "_puts");

		assertRel(
				actual_rel_text,
				actual_symtab,
				0x00000004,
				net.boricj.bft.coff.machines.i386.CoffRelocationType_i386.IMAGE_REL_I386_SECTION,
				"$SG7446");
		assertRel(actual_rel_text, actual_symtab, 0x00000009, IMAGE_REL_I386_REL32, "_puts");
		assertEquals(2, actual_rel_text.size());
	}

	private static void assertCoffSemanticsEquals(CoffFile expected, CoffFile actual) {
		assertEquals(expected.getHeader().getMachine(), actual.getHeader().getMachine());
		assertSectionsEqual(expected.getSections(), actual.getSections());
		assertSymbolsEqual(expected.getSymbols(), actual.getSymbols());
		assertRelocationsEqual(expected, actual);
	}

	private static void assertSectionsEqual(CoffSectionTable expected, CoffSectionTable actual) {
		assertEquals(expected.size(), actual.size(), "section count mismatch");
		for (int index = 1; index <= expected.size(); index++) {
			CoffSection expectedSection = expected.get(index);
			CoffSection actualSection = actual.get(index);
			assertEquals(expectedSection.getName(), actualSection.getName(), "section name mismatch at index " + index);
			assertEquals(
					expectedSection.getCharacteristics().getValue(),
					actualSection.getCharacteristics().getValue(),
					"section characteristics mismatch for " + expectedSection.getName());
			if (expectedSection instanceof CoffBytes expectedBytes) {
				assertTrue(
						actualSection instanceof CoffBytes, "expected bytes section for " + expectedSection.getName());
				assertArrayEquals(expectedBytes.getBytes(), ((CoffBytes) actualSection).getBytes());
			} else if (expectedSection instanceof CoffUninitialized) {
				assertTrue(
						actualSection instanceof CoffUninitialized,
						"expected uninitialized section for " + expectedSection.getName());
				assertEquals(expectedSection.getVirtualSize(), actualSection.getVirtualSize());
			}
		}
	}

	private static void assertSymbolsEqual(CoffSymbolTable expected, CoffSymbolTable actual) {
		assertEquals(expected.size(), actual.size(), "symbol count mismatch");
		for (int index = 0; index < expected.size(); index++) {
			CoffSymbol expectedSymbol = expected.get(index);
			CoffSymbol actualSymbol = actual.get(index);
			assertEquals(expectedSymbol.getName(), actualSymbol.getName(), "symbol name mismatch at index " + index);
			assertEquals(
					expectedSymbol.getValue(),
					actualSymbol.getValue(),
					"symbol value mismatch for " + expectedSymbol.getName());
			assertEquals(
					expectedSymbol.getSectionNumber(),
					actualSymbol.getSectionNumber(),
					"symbol section mismatch for " + expectedSymbol.getName());
			assertEquals(
					expectedSymbol.getType(),
					actualSymbol.getType(),
					"symbol type mismatch for " + expectedSymbol.getName());
			assertEquals(
					expectedSymbol.getStorageClass(),
					actualSymbol.getStorageClass(),
					"symbol storage class mismatch for " + expectedSymbol.getName());
			assertEquals(
					expectedSymbol.getNumberOfAuxSymbols(),
					actualSymbol.getNumberOfAuxSymbols(),
					"aux symbol count mismatch for " + expectedSymbol.getName());
		}
	}

	private static void assertRelocationsEqual(CoffFile expected, CoffFile actual) {
		CoffSectionTable expectedSections = expected.getSections();
		CoffSectionTable actualSections = actual.getSections();
		CoffSymbolTable expectedSymbols = expected.getSymbols();
		CoffSymbolTable actualSymbols = actual.getSymbols();
		assertEquals(expectedSections.size(), actualSections.size());

		for (int sectionIndex = 1; sectionIndex <= expectedSections.size(); sectionIndex++) {
			CoffSection expectedSection = expectedSections.get(sectionIndex);
			CoffSection actualSection = actualSections.get(sectionIndex);
			CoffRelocationTable expectedRelocations = expectedSection.getRelocations();
			CoffRelocationTable actualRelocations = actualSection.getRelocations();
			assertEquals(
					expectedRelocations.size(),
					actualRelocations.size(),
					"relocation count mismatch for section " + expectedSection.getName());

			for (int relocationIndex = 0; relocationIndex < expectedRelocations.size(); relocationIndex++) {
				var expectedRelocation = expectedRelocations.get(relocationIndex);
				var actualRelocation = actualRelocations.get(relocationIndex);
				assertEquals(
						expectedRelocation.getVirtualAddress(),
						actualRelocation.getVirtualAddress(),
						"relocation offset mismatch for section " + expectedSection.getName());
				assertEquals(
						expectedRelocation.getType(),
						actualRelocation.getType(),
						"relocation type mismatch for section " + expectedSection.getName());

				String expectedTargetName =
						relocationTargetName(expectedSymbols, expectedRelocation.getSymbolTableIndex());
				String actualTargetName = relocationTargetName(actualSymbols, actualRelocation.getSymbolTableIndex());
				assertEquals(
						expectedTargetName,
						actualTargetName,
						"relocation target mismatch for section " + expectedSection.getName());
			}
		}
	}

	private static String relocationTargetName(CoffSymbolTable symbolTable, int symbolTableIndex) {
		if (symbolTableIndex < 0 || symbolTableIndex >= symbolTable.size()) {
			return "<none:" + symbolTableIndex + ">";
		}
		return symbolTable.get(symbolTableIndex).getName();
	}

	private static CoffFile parseFixture(String resourcePath) throws IOException {
		URL resource = CoffImageImportExportRoundTripTest.class.getResource(resourcePath);
		if (resource == null) {
			throw new IOException("Missing resource: " + resourcePath);
		}

		Path tempFile = Files.createTempFile("coff-fixture-", ".obj");
		tempFile.toFile().deleteOnExit();
		try (InputStream inputStream = resource.openStream()) {
			Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
		}

		try (FileInputStream fileInputStream = new FileInputStream(tempFile.toFile())) {
			return new CoffFile.Parser(fileInputStream).parse();
		}
	}

	private static short sectionNumber(CoffSectionTable sections, net.boricj.bft.coff.CoffSection section) {
		int index = sections.getElements().indexOf(section);
		assertTrue(index >= 0);
		return (short) (index + 1);
	}

	private static <T> T findSectionByName(CoffSectionTable sections, String name, Class<T> clazz) {
		Objects.requireNonNull(sections);
		var section = sections.getElements().stream()
				.filter(s -> s.getName().equals(name))
				.findFirst();
		assertTrue(section.isPresent());
		assertTrue(clazz.isInstance(section.get()));
		return clazz.cast(section.get());
	}

	private static void assertSymbol(
			CoffSymbolTable symtab, short sectionNumber, int value, String name, CoffStorageClass storageClass) {
		var symbol = symtab.getElements().stream()
				.filter(s -> s.getName().equals(name))
				.findFirst();
		assertTrue(symbol.isPresent());
		assertEquals(sectionNumber, symbol.get().getSectionNumber());
		assertEquals(value, symbol.get().getValue());
		assertEquals(storageClass, symbol.get().getStorageClass());
	}

	private static void assertUndefined(CoffSymbolTable symtab, String name) {
		assertSymbol(symtab, (short) 0, 0, name, CoffStorageClass.IMAGE_SYM_CLASS_EXTERNAL);
	}

	private static void assertRel(
			CoffRelocationTable rels,
			CoffSymbolTable symtab,
			int offset,
			CoffRelocationType_i386 type,
			String symbolName) {
		var rel = rels.getElements().stream()
				.filter(r -> r.getVirtualAddress() == offset)
				.findFirst();
		assertTrue(rel.isPresent());
		assertEquals(type, rel.get().getType());
		assertEquals(symbolName, symtab.get(rel.get().getSymbolTableIndex()).getName());
	}
}
