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
package net.boricj.bft.image;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import net.boricj.bft.coff.CoffFile;
import net.boricj.bft.elf.ElfFile;
import net.boricj.bft.elf.constants.ElfClass;
import net.boricj.bft.elf.constants.ElfData;
import net.boricj.bft.elf.constants.ElfMachine;
import net.boricj.bft.elf.constants.ElfOsAbi;
import net.boricj.bft.elf.constants.ElfType;
import net.boricj.bft.image.coff.CoffImporter;
import net.boricj.bft.image.coff.DefaultCoffRelocationCatalogRegistry;
import net.boricj.bft.image.elf.DefaultElfRelocationCatalogRegistry;
import net.boricj.bft.image.elf.ElfExporter;
import net.boricj.bft.image.elf.ElfImporter;

class CoffToElfI386Test {
	@Test
	void helloWorldDjgpp() throws IOException {
		assertRoundTrip("hello-world_djgpp.obj");
	}

	@Test
	void helloWorldI686PcWindowsMsvc() throws IOException {
		assertRoundTrip("hello-world_i686-pc-windows-msvc.obj");
	}

	@Test
	void helloWorldI686W64Mingw32Gcc() throws IOException {
		assertRoundTrip("hello-world_i686-w64-mingw32-gcc.obj");
	}

	private static void assertRoundTrip(String artifactName) throws IOException {
		CoffFile sourceCoff = parseRealCoffArtifact(artifactName);
		ImageFile expected = new CoffImporter(sourceCoff, DefaultCoffRelocationCatalogRegistry.INSTANCE).importImage();
		ElfFile projectedElf = new ElfExporter(
						expected, defaultI386ElfBuilder(), DefaultElfRelocationCatalogRegistry.INSTANCE)
				.exportFile();
		ImageFile actual = new ElfImporter(projectedElf, DefaultElfRelocationCatalogRegistry.INSTANCE).importImage();
		CrossFormatImageAssertions.assertImageEquivalent(expected, actual);
	}

	private static CoffFile parseRealCoffArtifact(String artifactName) throws IOException {
		Path artifactPath = Path.of(
						"..",
						"bft-coff",
						"src",
						"test",
						"resources",
						"net",
						"boricj",
						"bft",
						"coff",
						"i386",
						artifactName)
				.toAbsolutePath();
		try (FileInputStream fileInputStream = new FileInputStream(artifactPath.toFile())) {
			return new CoffFile.Parser(fileInputStream).parse();
		}
	}

	private static ElfFile.Builder defaultI386ElfBuilder() {
		return new ElfFile.Builder(
						ElfClass.ELFCLASS32,
						ElfData.ELFDATA2LSB,
						ElfOsAbi.ELFOSABI_NONE,
						ElfType.ET_REL,
						ElfMachine.EM_386)
				.setPhentsize((short) 0);
	}
}
