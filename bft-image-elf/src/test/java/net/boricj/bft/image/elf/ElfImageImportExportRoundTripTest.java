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
package net.boricj.bft.image.elf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import net.boricj.bft.elf.ElfFile;
import net.boricj.bft.elf.ElfSectionTable;
import net.boricj.bft.image.ImageFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ElfImageImportExportRoundTripTest {
	@ParameterizedTest
	@MethodSource("net.boricj.bft.image.elf.ElfImageFixtureCatalog#objectFixtures")
	void importsAndExportsMatchHandcraftedImages(ElfImageFixtureCatalog.FixtureSpec spec)
			throws IOException, URISyntaxException {
		ImageFile expectedImage = spec.expectedImage().get();
		ElfFile parsedFixture = parseFixture(spec.fixtureName());
		ElfImporter importer = new ElfImporter(parsedFixture);
		List<String> importedDiagnostics = new ArrayList<>();
		ImageFile importedImage = importer.importImage(importedDiagnostics::add);

		assertNotNull(importedImage);
		ImageFileAssertions.assertImageEquals(expectedImage, importedImage);
		assertEquals(spec.machine(), importer.machine());
		assertEquals(spec.elfClass(), importer.elfClass());
		assertEquals(spec.elfData(), importer.elfData());

		ElfFile exportedElf = new ElfExporter(expectedImage, ElfImageFixtureCatalog.builderFor(spec)).exportFile();
		ElfSectionTable exportedSections = exportedElf.getSections();
		ElfImporter reimporter = new ElfImporter(exportedElf);
		List<String> reimportedDiagnostics = new ArrayList<>();
		ImageFile reimportedImage = reimporter.importImage(reimportedDiagnostics::add);

		assertNotNull(exportedSections);
		assertEquals(List.of(), reimportedDiagnostics);
		ImageFileAssertions.assertImageEquals(expectedImage, reimportedImage);
		assertEquals(spec.machine(), exportedElf.getHeader().getMachine());
		assertEquals(spec.elfClass(), exportedElf.getHeader().getIdentClass());
		assertEquals(spec.elfData(), exportedElf.getHeader().getIdentData());
	}

	private ElfFile parseFixture(String fixtureName) throws URISyntaxException, IOException {
		String resourcePath = "/net/boricj/bft/image/elf/" + fixtureName;
		File file = new File(getClass().getResource(resourcePath).toURI());
		return new ElfFile.Parser(new FileInputStream(file)).parse();
	}
}
