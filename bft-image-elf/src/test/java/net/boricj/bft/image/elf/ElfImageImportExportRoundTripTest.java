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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import net.boricj.bft.elf.ElfFile;
import net.boricj.bft.elf.ElfSection;
import net.boricj.bft.elf.ElfSectionFlags;
import net.boricj.bft.elf.constants.ElfClass;
import net.boricj.bft.elf.constants.ElfData;
import net.boricj.bft.elf.constants.ElfMachine;
import net.boricj.bft.elf.constants.ElfOsAbi;
import net.boricj.bft.elf.constants.ElfSymbolBinding;
import net.boricj.bft.elf.constants.ElfSymbolType;
import net.boricj.bft.elf.constants.ElfSymbolVisibility;
import net.boricj.bft.elf.constants.ElfType;
import net.boricj.bft.elf.sections.ElfProgBits;
import net.boricj.bft.elf.sections.ElfRelTable;
import net.boricj.bft.elf.sections.ElfRelaTable;
import net.boricj.bft.elf.sections.ElfSymbolTable;
import net.boricj.bft.elf.sections.ElfSymbolTable.ElfSymbol;
import net.boricj.bft.image.ImageFile;
import net.boricj.bft.image.ImageSection;
import net.boricj.bft.image.ImageSectionLinktimeFlags;
import net.boricj.bft.image.ImageSectionRuntimeFlags;
import net.boricj.bft.image.ImageSymbol;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElfImageImportExportRoundTripTest {
	@Test
	void test_amd64_hello_world_nopic_o() throws IOException {
		assertFixtureRoundTrip(fixture("amd64/hello-world_x86_64-linux-gnu.nopic.o"));
	}

	@Test
	void test_amd64_hello_world_pic_o() throws IOException {
		assertFixtureRoundTrip(fixture("amd64/hello-world_x86_64-linux-gnu.o"));
	}

	@Test
	void test_i386_hello_world_nopic_o() throws IOException {
		assertFixtureRoundTrip(fixture("i386/hello-world_i686-linux-gnu.nopic.o"));
	}

	@Test
	void test_i386_hello_world_pic_o() throws IOException {
		assertFixtureRoundTrip(fixture("i386/hello-world_i686-linux-gnu.o"));
	}

	@Test
	void test_mips_hello_world_nopic_o() throws IOException {
		assertFixtureRoundTrip(fixture("mips/hello-world_mips-linux-gnu.nopic.o"));
	}

	@Test
	void test_mips_hello_world_pic_o() throws IOException {
		assertFixtureRoundTrip(fixture("mips/hello-world_mips-linux-gnu.o"));
	}

	@Test
	void test_mipsel_hello_world_nopic_o() throws IOException {
		assertFixtureRoundTrip(fixture("mips/hello-world_mipsel-linux-gnu.nopic.o"));
	}

	@Test
	void test_mipsel_hello_world_pic_o() throws IOException {
		assertFixtureRoundTrip(fixture("mips/hello-world_mipsel-linux-gnu.o"));
	}

	@Test
	void preservesSectionAlignmentAndFlagsDuringRoundTrip() {
		ImageFile expected = new ImageFile(ImageFile.Kind.OBJECT);
		ImageSection text = expected.sections().create(".text");
		text.setContents(new byte[] {0x55, (byte) 0xC3});
		text.setAlignment(16);
		text.addLinktimeFlag(ImageSectionLinktimeFlags.ALLOC);
		text.addRuntimeFlag(ImageSectionRuntimeFlags.EXECUTE);

		ElfFile.Builder builder = new ElfFile.Builder(
						ElfClass.ELFCLASS32,
						ElfData.ELFDATA2LSB,
						ElfOsAbi.ELFOSABI_NONE,
						ElfType.ET_REL,
						ElfMachine.EM_386)
				.setPhentsize((short) 0);

		ElfFile exported = new ElfExporter(expected, builder).exportFile();
		ImageFile imported = new ElfImporter(exported).importImage();

		assertEquals(1, imported.sections().size());
		ImageSection importedText = imported.sections().getFirst();
		assertEquals(16, importedText.getAlignment());
		assertTrue(importedText.hasLinktimeFlag(ImageSectionLinktimeFlags.ALLOC));
		assertTrue(importedText.hasRuntimeFlag(ImageSectionRuntimeFlags.EXECUTE));
		assertTrue(importedText.hasLinktimeFlag(ImageSectionLinktimeFlags.CODE));
	}

	private static void assertFixtureRoundTrip(ElfImageFixtureCatalog.FixtureSpec spec) throws IOException {
		ImageFile expectedImage = spec.expectedImage().get();
		ElfFile parsedFixture = parseFixture(spec.fixtureName());
		ImageFile importedImage = new ElfImporter(parsedFixture).importImage();
		assertNotNull(importedImage);
		ImageFileAssertions.assertImageEquals(expectedImage, importedImage);
		assertEquals(spec.machine(), parsedFixture.getHeader().getMachine());
		assertEquals(spec.elfClass(), parsedFixture.getHeader().getIdentClass());
		assertEquals(spec.elfData(), parsedFixture.getHeader().getIdentData());

		ElfFile exportedElf = new ElfExporter(expectedImage, ElfImageFixtureCatalog.builderFor(spec)).exportFile();
		assertEquals(spec.machine(), exportedElf.getHeader().getMachine());
		assertEquals(spec.elfClass(), exportedElf.getHeader().getIdentClass());
		assertEquals(spec.elfData(), exportedElf.getHeader().getIdentData());
		assertEquals(ElfType.ET_REL, exportedElf.getHeader().getType());
		assertExportedElfMatchesExpectedImage(expectedImage, exportedElf);

		ImageFile reimportedImage = new ElfImporter(exportedElf).importImage();
		ImageFileAssertions.assertImageEquals(expectedImage, reimportedImage);
	}

	private static void assertExportedElfMatchesExpectedImage(ImageFile expectedImage, ElfFile actualElf) {
		assertNotNull(actualElf.getSections());
		Set<String> expectedSectionNames = new HashSet<>();
		for (ImageSection expectedSection : expectedImage.sections()) {
			expectedSectionNames.add(expectedSection.getName());
			ElfSection actualSection = actualElf.getSections().stream()
					.filter(section ->
							section != null && expectedSection.getName().equals(section.getName()))
					.findFirst()
					.orElse(null);
			assertNotNull(actualSection, () -> "Missing section " + expectedSection.getName() + " in exported ELF");
			assertEquals(expectedSection.getAlignment(), actualSection.getAddrAlign());

			ElfSectionFlags expectedFlags = expectedSectionFlags(expectedSection);
			assertEquals(expectedFlags.isAlloc(), actualSection.getFlags().isAlloc());
			assertEquals(expectedFlags.isWrite(), actualSection.getFlags().isWrite());
			assertEquals(expectedFlags.isExecInstr(), actualSection.getFlags().isExecInstr());

			if ((expectedSection.getContents().length == 0) && (expectedSection.getLogicalSize() > 0)) {
				assertTrue(
						actualSection instanceof net.boricj.bft.elf.sections.ElfNoBits,
						() -> "Expected NOBITS section for " + expectedSection.getName());
				assertEquals(expectedSection.getLogicalSize(), actualSection.getSize());
			} else {
				assertTrue(
						actualSection instanceof ElfProgBits,
						() -> "Expected PROGBITS section for " + expectedSection.getName());
				assertArrayEquals(expectedSection.getContents(), ((ElfProgBits) actualSection).getBytes());
			}
		}

		long exportedContentSections = actualElf.getSections().stream()
				.filter(section -> section != null)
				.filter(section -> expectedSectionNames.contains(section.getName()))
				.count();
		assertEquals(expectedImage.sections().size(), exportedContentSections);

		assertSymbolsMatch(expectedImage, actualElf);
		assertRelocationsMatch(expectedImage, actualElf);
	}

	private static void assertSymbolsMatch(ImageFile expectedImage, ElfFile actualElf) {
		List<String> expectedSymbols = new ArrayList<>();
		for (ImageSymbol expectedSymbol : expectedImage.symbols()) {
			expectedSymbols.add(canonicalExpectedSymbol(expectedSymbol));
		}
		expectedSymbols.sort(String::compareTo);

		ElfSymbolTable exportedSymtab = actualElf.getSections().stream()
				.filter(section -> section instanceof ElfSymbolTable)
				.map(section -> (ElfSymbolTable) section)
				.findFirst()
				.orElse(null);
		assertNotNull(exportedSymtab, "missing exported symbol table");

		List<String> actualSymbols = new ArrayList<>();
		for (ElfSymbol symbol : exportedSymtab) {
			if (isNullSymbol(symbol)) {
				continue;
			}
			actualSymbols.add(canonicalActualSymbol(symbol, actualElf));
		}
		actualSymbols.sort(String::compareTo);

		assertEquals(expectedSymbols, actualSymbols, "symbol table semantics mismatch");
	}

	private static void assertRelocationsMatch(ImageFile expectedImage, ElfFile actualElf) {
		List<String> expectedRelocations = new ArrayList<>();
		for (ImageSection expectedSection : expectedImage.sections()) {
			for (var group : expectedSection.relocations()) {
				for (var gang : group.gangs()) {
					var type = ElfRelocationCatalog.typeFromDescriptor(
							actualElf.getHeader().getMachine(), group.getOperation(), gang.getFieldCodec());
					for (var entry : gang.entries()) {
						long addend = ElfRelocationCatalog.usesExplicitAddends(
										actualElf.getHeader().getMachine())
								? entry.getAddend()
								: 0L;
						expectedRelocations.add(String.format(
								"%s|%d|%s|%s|%d",
								expectedSection.getName(),
								entry.getOffset(),
								type,
								group.getTarget().getName(),
								addend));
					}
				}
			}
		}
		expectedRelocations.sort(String::compareTo);

		List<String> actualRelocations = new ArrayList<>();
		for (ElfSection section : actualElf.getSections()) {
			if (section instanceof ElfRelTable table) {
				String targetSectionName = table.getSection().getName();
				for (var rel : table) {
					actualRelocations.add(String.format(
							"%s|%d|%s|%s|0",
							targetSectionName,
							rel.getOffset(),
							rel.getType(),
							rel.getSymbol().getName()));
				}
			} else if (section instanceof ElfRelaTable table) {
				ElfSection targetSection = actualElf.getSections().get(table.getInfo());
				String targetSectionName = targetSection.getName();
				for (var rela : table) {
					actualRelocations.add(String.format(
							"%s|%d|%s|%s|%d",
							targetSectionName,
							rela.getOffset(),
							rela.getType(),
							rela.getSymbol().getName(),
							rela.getAddend()));
				}
			}
		}
		actualRelocations.sort(String::compareTo);

		assertEquals(expectedRelocations, actualRelocations, "relocation semantics mismatch");
	}

	private static boolean isNullSymbol(ElfSymbol symbol) {
		return symbol.getName().isEmpty()
				&& symbol.getValue() == 0
				&& symbol.getSize() == 0
				&& symbol.getType() == ElfSymbolType.STT_NOTYPE
				&& symbol.getVisibility() == ElfSymbolVisibility.STV_DEFAULT
				&& symbol.getBinding() == ElfSymbolBinding.STB_LOCAL
				&& Short.toUnsignedInt(symbol.getIndex()) == ElfSection.SHN_UNDEF;
	}

	private static String canonicalExpectedSymbol(ImageSymbol symbol) {
		String sectionName =
				symbol.getSection() == null ? "<undef>" : symbol.getSection().getName();
		return String.format(
				"%s|%s|%d|%d|%s|%s|%s",
				symbol.getName(),
				sectionName,
				symbol.getOffset(),
				symbol.getSize(),
				mapType(symbol.getType()),
				mapVisibility(symbol.getVisibility()),
				mapBinding(symbol.getBinding()));
	}

	private static String canonicalActualSymbol(ElfSymbol symbol, ElfFile elfFile) {
		int sectionIndex = Short.toUnsignedInt(symbol.getIndex());
		String sectionName = "<undef>";
		if (sectionIndex < elfFile.getSections().size()) {
			ElfSection section = elfFile.getSections().get(sectionIndex);
			if (section != null) {
				sectionName = section.getName();
			}
		}
		return String.format(
				"%s|%s|%d|%d|%s|%s|%s",
				symbol.getName(),
				sectionName,
				symbol.getValue(),
				symbol.getSize(),
				symbol.getType(),
				symbol.getVisibility(),
				symbol.getBinding());
	}

	private static ElfSymbolType mapType(ImageSymbol.Type type) {
		return switch (type) {
			case NOTYPE -> ElfSymbolType.STT_NOTYPE;
			case OBJECT -> ElfSymbolType.STT_OBJECT;
			case FUNCTION -> ElfSymbolType.STT_FUNC;
			case SECTION -> ElfSymbolType.STT_SECTION;
			case FILE -> ElfSymbolType.STT_FILE;
			case TLS -> ElfSymbolType.STT_TLS;
		};
	}

	private static ElfSymbolVisibility mapVisibility(ImageSymbol.Visibility visibility) {
		return switch (visibility) {
			case DEFAULT -> ElfSymbolVisibility.STV_DEFAULT;
			case INTERNAL -> ElfSymbolVisibility.STV_INTERNAL;
			case HIDDEN -> ElfSymbolVisibility.STV_HIDDEN;
			case PROTECTED -> ElfSymbolVisibility.STV_PROTECTED;
		};
	}

	private static ElfSymbolBinding mapBinding(ImageSymbol.Binding binding) {
		return switch (binding) {
			case LOCAL -> ElfSymbolBinding.STB_LOCAL;
			case GLOBAL -> ElfSymbolBinding.STB_GLOBAL;
			case WEAK -> ElfSymbolBinding.STB_WEAK;
		};
	}

	private static ElfSectionFlags expectedSectionFlags(ImageSection section) {
		ElfSectionFlags flags = new ElfSectionFlags();
		if (section.hasLinktimeFlag(ImageSectionLinktimeFlags.ALLOC)) {
			flags.alloc();
		}
		if (section.hasRuntimeFlag(ImageSectionRuntimeFlags.WRITE)) {
			flags.write();
		}
		if (section.hasRuntimeFlag(ImageSectionRuntimeFlags.EXECUTE)) {
			flags.execInstr();
		}
		return flags;
	}

	private static ElfImageFixtureCatalog.FixtureSpec fixture(String fixtureName) {
		return ElfImageFixtureCatalog.objectFixtures()
				.filter(spec -> spec.fixtureName().equals(fixtureName))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Unknown ELF fixture: " + fixtureName));
	}

	private static ElfFile parseFixture(String fixtureName) throws IOException {
		String resourcePath = "/net/boricj/bft/elf/" + fixtureName;
		URL resource = ElfImageImportExportRoundTripTest.class.getResource(resourcePath);
		if (resource == null) {
			throw new IOException("Missing resource: " + resourcePath);
		}

		Path tempFile = Files.createTempFile("elf-fixture-", ".o");
		tempFile.toFile().deleteOnExit();
		try (InputStream inputStream = resource.openStream()) {
			Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
		}

		try (FileInputStream fileInputStream = new FileInputStream(tempFile.toFile())) {
			return new ElfFile.Parser(fileInputStream).parse();
		}
	}
}
