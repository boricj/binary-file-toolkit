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
import net.boricj.bft.image.ImageSectionRuntimeFlags;
import net.boricj.bft.image.ImageSectionType;
import net.boricj.bft.image.ImageSymbol;
import net.boricj.bft.image.RelocationFieldCursor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElfImageImportExportRoundTripTest {
	private static final ElfRelocationCatalogRegistry CATALOGS = DefaultElfRelocationCatalogRegistry.INSTANCE;

	private record SymbolSemantics(
			String name,
			String section,
			long value,
			long size,
			ElfSymbolType type,
			ElfSymbolVisibility visibility,
			ElfSymbolBinding binding) {}

	private record RelocationSemantics(
			String sectionName,
			long offset,
			net.boricj.bft.elf.constants.ElfRelocationType type,
			String target,
			long addend) {}

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
		text.setBytes(new byte[] {0x55, (byte) 0xC3});
		text.setAlignment(16);
		text.setType(ImageSectionType.INITIALIZED);
		text.addRuntimeFlag(ImageSectionRuntimeFlags.EXECUTE);

		ElfFile.Builder builder = new ElfFile.Builder(
						ElfClass.ELFCLASS32,
						ElfData.ELFDATA2LSB,
						ElfOsAbi.ELFOSABI_NONE,
						ElfType.ET_REL,
						ElfMachine.EM_386)
				.setPhentsize((short) 0);

		ElfFile exported = new ElfExporter(expected, builder, CATALOGS).exportFile();
		ImageFile imported = new ElfImporter(exported, CATALOGS).importImage();

		assertEquals(1, imported.sections().size());
		ImageSection importedText = imported.sections().getFirst();
		assertEquals(16, importedText.getAlignment());
		assertTrue(importedText.hasRuntimeFlag(ImageSectionRuntimeFlags.READ));
		assertTrue(importedText.hasRuntimeFlag(ImageSectionRuntimeFlags.EXECUTE));
		assertEquals(ImageSectionType.INITIALIZED, importedText.getType());
	}

	@Test
	void rejectsNullRelocationTargets() {
		ImageFile image = new ImageFile(ImageFile.Kind.OBJECT);
		ImageSection text = image.sections().create(".text");
		text.setBytes(new byte[] {0x00, 0x00, 0x00, 0x00});
		var relocation = text.relocations().create(net.boricj.bft.image.RelocationOperation.PC_RELATIVE, null, 0);
		relocation
				.gangs()
				.create(net.boricj.bft.image.relocs.priv.ByteFieldCodec.S32LE)
				.entries()
				.create(0);

		assertThrows(IllegalArgumentException.class, () -> new ElfExporter(image, defaultElfBuilder(), CATALOGS)
				.exportFile());
	}

	@Test
	void rejectsEmptyRelocationGangLists() {
		ImageFile image = new ImageFile(ImageFile.Kind.OBJECT);
		ImageSection text = image.sections().create(".text");
		text.setBytes(new byte[] {0x00, 0x00, 0x00, 0x00});
		ImageSymbol target = image.symbols().create("target", text, 0);
		text.relocations().create(net.boricj.bft.image.RelocationOperation.PC_RELATIVE, target, 0);

		assertThrows(IllegalArgumentException.class, () -> new ElfExporter(image, defaultElfBuilder(), CATALOGS)
				.exportFile());
	}

	private static void assertFixtureRoundTrip(ElfImageFixtureCatalog.FixtureSpec spec) throws IOException {
		ImageFile expectedImage = spec.expectedImage().get();
		ElfFile parsedFixture = parseFixture(spec.fixtureName());
		ImageFile importedImage = new ElfImporter(parsedFixture, CATALOGS).importImage();
		assertNotNull(importedImage);
		ImageFileAssertions.assertImageEqualsFixtureCompatible(expectedImage, importedImage);
		assertEquals(spec.machine(), parsedFixture.getHeader().getMachine());
		assertEquals(spec.elfClass(), parsedFixture.getHeader().getIdentClass());
		assertEquals(spec.elfData(), parsedFixture.getHeader().getIdentData());

		ElfFile exportedElf =
				new ElfExporter(expectedImage, ElfImageFixtureCatalog.builderFor(spec), CATALOGS).exportFile();
		assertEquals(spec.machine(), exportedElf.getHeader().getMachine());
		assertEquals(spec.elfClass(), exportedElf.getHeader().getIdentClass());
		assertEquals(spec.elfData(), exportedElf.getHeader().getIdentData());
		assertEquals(ElfType.ET_REL, exportedElf.getHeader().getType());
		assertExportedElfMatchesExpectedImage(expectedImage, exportedElf);

		ImageFile reimportedImage = new ElfImporter(exportedElf, CATALOGS).importImage();
		ImageFileAssertions.assertImageEqualsFixtureCompatible(expectedImage, reimportedImage);
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

			if ((expectedSection.getBytes().length == 0) && (expectedSection.getLogicalSize() > 0)) {
				assertTrue(
						actualSection instanceof net.boricj.bft.elf.sections.ElfNoBits,
						() -> "Expected NOBITS section for " + expectedSection.getName());
				assertEquals(expectedSection.getLogicalSize(), actualSection.getSize());
			} else {
				assertTrue(
						actualSection instanceof ElfProgBits,
						() -> "Expected PROGBITS section for " + expectedSection.getName());
				byte[] expectedNormalized = normalizeSectionContents(expectedSection, expectedSection.getBytes());
				byte[] actualNormalized =
						normalizeSectionContents(expectedSection, ((ElfProgBits) actualSection).getBytes());
				assertArrayEquals(expectedNormalized, actualNormalized);
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
		List<SymbolSemantics> expectedSymbols = new ArrayList<>();
		for (ImageSymbol expectedSymbol : expectedImage.symbols()) {
			expectedSymbols.add(canonicalExpectedSymbol(expectedSymbol));
		}
		expectedSymbols.sort((left, right) -> compareSymbols(left, right));

		ElfSymbolTable exportedSymtab = actualElf.getSections().stream()
				.filter(section -> section instanceof ElfSymbolTable)
				.map(section -> (ElfSymbolTable) section)
				.findFirst()
				.orElse(null);
		assertNotNull(exportedSymtab, "missing exported symbol table");

		List<SymbolSemantics> actualSymbols = new ArrayList<>();
		for (ElfSymbol symbol : exportedSymtab) {
			if (isNullSymbol(symbol)) {
				continue;
			}
			actualSymbols.add(canonicalActualSymbol(symbol, actualElf));
		}
		actualSymbols.sort((left, right) -> compareSymbols(left, right));

		assertEquals(expectedSymbols, actualSymbols, "symbol table semantics mismatch");
	}

	private static void assertRelocationsMatch(ImageFile expectedImage, ElfFile actualElf) {
		ElfRelocationCatalog catalog = CATALOGS.catalogFor(actualElf.getHeader().getMachine());
		assertNotNull(
				catalog, "missing catalog for machine " + actualElf.getHeader().getMachine());
		ElfRelocationTableKind tableKind = catalog.relocationTableKind();

		List<RelocationSemantics> expectedRelocations = new ArrayList<>();
		for (ImageSection expectedSection : expectedImage.sections()) {
			for (var group : expectedSection.relocations()) {
				for (var gang : group.gangs()) {
					for (var entry : gang.entries()) {
						var type = catalog.typeFromEntry(entry);
						assertNotNull(
								type,
								() -> String.format(
										"Missing type mapping for operation '%s' and codec '%s' on machine %s",
										entry.getOperation(),
										entry.getFieldCodec(),
										actualElf.getHeader().getMachine()));
						long addend = tableKind == ElfRelocationTableKind.RELA ? entry.getAddend() : 0L;
						expectedRelocations.add(new RelocationSemantics(
								expectedSection.getName(),
								entry.getOffset(),
								type,
								group.getTarget().getName(),
								addend));
					}
				}
			}
		}
		expectedRelocations.sort((left, right) -> compareRelocations(left, right));

		List<RelocationSemantics> actualRelocations = new ArrayList<>();
		for (ElfSection section : actualElf.getSections()) {
			if (section instanceof ElfRelTable table) {
				String targetSectionName = table.getSection().getName();
				for (var rel : table) {
					actualRelocations.add(new RelocationSemantics(
							targetSectionName,
							rel.getOffset(),
							rel.getType(),
							rel.getSymbol().getName(),
							0));
				}
			} else if (section instanceof ElfRelaTable table) {
				ElfSection targetSection = actualElf.getSections().get(table.getInfo());
				String targetSectionName = targetSection.getName();
				for (var rela : table) {
					actualRelocations.add(new RelocationSemantics(
							targetSectionName,
							rela.getOffset(),
							rela.getType(),
							rela.getSymbol().getName(),
							rela.getAddend()));
				}
			}
		}
		actualRelocations.sort((left, right) -> compareRelocations(left, right));

		assertEquals(expectedRelocations, actualRelocations, "relocation semantics mismatch");
	}

	private static byte[] normalizeSectionContents(ImageSection section, byte[] sectionContents) {
		byte[] normalized = sectionContents.clone();
		for (var group : section.relocations()) {
			for (var gang : group.gangs()) {
				for (var entry : gang.entries()) {
					new RelocationFieldCursor(gang.getFieldCodec(), entry.getOffset())
							.mask(normalized)
							.apply(normalized);
				}
			}
		}
		return normalized;
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

	private static SymbolSemantics canonicalExpectedSymbol(ImageSymbol symbol) {
		String sectionName =
				symbol.getSection() == null ? "<undef>" : symbol.getSection().getName();
		return new SymbolSemantics(
				symbol.getName(),
				sectionName,
				symbol.getOffset(),
				symbol.getSize(),
				mapType(symbol.getType()),
				mapVisibility(symbol.getVisibility()),
				mapBinding(symbol.getBinding()));
	}

	private static SymbolSemantics canonicalActualSymbol(ElfSymbol symbol, ElfFile elfFile) {
		int sectionIndex = Short.toUnsignedInt(symbol.getIndex());
		String sectionName = "<undef>";
		if (sectionIndex != ElfSection.SHN_UNDEF
				&& sectionIndex < elfFile.getSections().size()) {
			ElfSection section = elfFile.getSections().get(sectionIndex);
			if (section != null) {
				sectionName = section.getName();
			}
		}
		return new SymbolSemantics(
				symbol.getName(),
				sectionName,
				symbol.getValue(),
				symbol.getSize(),
				symbol.getType(),
				symbol.getVisibility(),
				symbol.getBinding());
	}

	private static int compareSymbols(SymbolSemantics left, SymbolSemantics right) {
		int value = left.name().compareTo(right.name());
		if (value != 0) {
			return value;
		}
		value = left.section().compareTo(right.section());
		if (value != 0) {
			return value;
		}
		value = Long.compare(left.value(), right.value());
		if (value != 0) {
			return value;
		}
		value = Long.compare(left.size(), right.size());
		if (value != 0) {
			return value;
		}
		value = left.type().name().compareTo(right.type().name());
		if (value != 0) {
			return value;
		}
		value = left.visibility().name().compareTo(right.visibility().name());
		if (value != 0) {
			return value;
		}
		return left.binding().name().compareTo(right.binding().name());
	}

	private static int compareRelocations(RelocationSemantics left, RelocationSemantics right) {
		int value = left.sectionName().compareTo(right.sectionName());
		if (value != 0) {
			return value;
		}
		value = Long.compare(left.offset(), right.offset());
		if (value != 0) {
			return value;
		}
		value = left.type().toString().compareTo(right.type().toString());
		if (value != 0) {
			return value;
		}
		value = left.target().compareTo(right.target());
		if (value != 0) {
			return value;
		}
		return Long.compare(left.addend(), right.addend());
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
		if (section.getType() != ImageSectionType.UNALLOCATED) {
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

	private static ElfFile.Builder defaultElfBuilder() {
		return new ElfFile.Builder(
						ElfClass.ELFCLASS32,
						ElfData.ELFDATA2LSB,
						ElfOsAbi.ELFOSABI_NONE,
						ElfType.ET_REL,
						ElfMachine.EM_386)
				.setPhentsize((short) 0);
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
