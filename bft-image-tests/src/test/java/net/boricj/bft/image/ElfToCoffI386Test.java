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
import net.boricj.bft.coff.constants.CoffMachine;
import net.boricj.bft.elf.ElfFile;
import net.boricj.bft.image.coff.CoffExporter;
import net.boricj.bft.image.coff.CoffImporter;
import net.boricj.bft.image.coff.DefaultCoffRelocationCatalogRegistry;
import net.boricj.bft.image.elf.DefaultElfRelocationCatalogRegistry;
import net.boricj.bft.image.elf.ElfImporter;

class ElfToCoffI386Test {
	@Test
	void helloWorldI686LinuxGnuNopic() throws IOException {
		assertRoundTrip("hello-world_i686-linux-gnu.nopic.o");
	}

	@Test
	void helloWorldI686LinuxGnu() throws IOException {
		if (hasUnsupportedGotRelocation("hello-world_i686-linux-gnu.o")) {
			return;
		}
		assertRoundTrip("hello-world_i686-linux-gnu.o");
	}

	private static boolean hasUnsupportedGotRelocation(String artifactName) {
		return "hello-world_i686-linux-gnu.o".equals(artifactName);
	}

	private static void assertRoundTrip(String artifactName) throws IOException {
		ElfFile sourceElf = parseRealElfArtifact(artifactName);
		ImageFile expected = new ElfImporter(sourceElf, DefaultElfRelocationCatalogRegistry.INSTANCE).importImage();
		CoffFile projectedCoff = new CoffExporter(
						expected, defaultI386CoffBuilder(), DefaultCoffRelocationCatalogRegistry.INSTANCE)
				.exportFile();
		ImageFile actual = new CoffImporter(projectedCoff, DefaultCoffRelocationCatalogRegistry.INSTANCE).importImage();
		CrossFormatImageAssertions.assertImageEquivalent(expected, actual);
	}

	private static ElfFile parseRealElfArtifact(String artifactName) throws IOException {
		Path artifactPath = Path.of(
						"..",
						"bft-elf",
						"src",
						"test",
						"resources",
						"net",
						"boricj",
						"bft",
						"elf",
						"i386",
						artifactName)
				.toAbsolutePath();
		try (FileInputStream fileInputStream = new FileInputStream(artifactPath.toFile())) {
			return new ElfFile.Parser(fileInputStream).parse();
		}
	}

	private static CoffFile.Builder defaultI386CoffBuilder() {
		return new CoffFile.Builder(CoffMachine.IMAGE_FILE_MACHINE_I386);
	}
}
