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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import net.boricj.bft.elf.ElfFile;
import net.boricj.bft.image.ImageFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ElfExporterBuilderContractTest {
	@ParameterizedTest
	@MethodSource("net.boricj.bft.image.elf.ElfImageFixtureCatalog#objectFixtures")
	void exportsFromBuilderConfiguration(ElfImageFixtureCatalog.FixtureSpec spec) {
		ImageFile expectedImage = spec.expectedImage().get();
		ElfFile exportedElf = new ElfExporter(expectedImage, ElfImageFixtureCatalog.builderFor(spec)).exportFile();
		assertEquals(spec.machine(), exportedElf.getHeader().getMachine());
		assertEquals(spec.elfClass(), exportedElf.getHeader().getIdentClass());
		assertEquals(spec.elfData(), exportedElf.getHeader().getIdentData());
	}

	@ParameterizedTest
	@MethodSource("net.boricj.bft.image.elf.ElfImageFixtureCatalog#objectFixtures")
	void rejectsMismatchedBuilderType(ElfImageFixtureCatalog.FixtureSpec spec) {
		ImageFile expectedImage = spec.expectedImage().get();
		ElfFile.Builder wrongTypeBuilder = new ElfFile.Builder(
						spec.elfClass(),
						spec.elfData(),
						spec.osAbi(),
						net.boricj.bft.elf.constants.ElfType.ET_EXEC,
						spec.machine())
				.setPhentsize((short) 0);

		assertThrows(
				IllegalArgumentException.class, () -> new ElfExporter(expectedImage, wrongTypeBuilder).exportFile());
	}

	@ParameterizedTest
	@MethodSource("net.boricj.bft.image.elf.ElfImageFixtureCatalog#objectFixtures")
	void preservesBuilderOsAbiConfiguration(ElfImageFixtureCatalog.FixtureSpec spec) {
		ImageFile expectedImage = spec.expectedImage().get();
		ElfFile exportedElf = new ElfExporter(expectedImage, ElfImageFixtureCatalog.builderFor(spec)).exportFile();
		assertEquals(spec.osAbi(), exportedElf.getHeader().getIdentOsAbi());
	}
}
