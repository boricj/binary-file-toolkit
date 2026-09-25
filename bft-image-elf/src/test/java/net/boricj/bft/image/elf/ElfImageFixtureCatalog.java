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

import java.util.HexFormat;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.boricj.bft.elf.ElfFile;
import net.boricj.bft.elf.constants.ElfMachine;
import net.boricj.bft.image.ImageFile;
import net.boricj.bft.image.ImageSection;
import net.boricj.bft.image.ImageSymbol;
import net.boricj.bft.image.RelocationFieldCodec;
import net.boricj.bft.image.RelocationOperation;
import net.boricj.bft.image.relocs.priv.BitShiftedAdjustedFieldCodec;
import net.boricj.bft.image.relocs.priv.BitShiftedFieldCodec;
import net.boricj.bft.image.relocs.priv.ByteFieldCodec;

import static net.boricj.bft.elf.constants.ElfClass.ELFCLASS32;
import static net.boricj.bft.elf.constants.ElfClass.ELFCLASS64;
import static net.boricj.bft.elf.constants.ElfData.ELFDATA2LSB;
import static net.boricj.bft.elf.constants.ElfData.ELFDATA2MSB;
import static net.boricj.bft.elf.constants.ElfOsAbi.ELFOSABI_NONE;
import static net.boricj.bft.elf.constants.ElfType.ET_REL;

final class ElfImageFixtureCatalog {
	private static final HexFormat HEX = HexFormat.of();
	private static final String MIPS_BIG_NOPIC_TEXT =
			"27bdffe0afbf001cafbe001803a0f0253c1c0000279c0000afbc0010afc40020afc500243c020000244400008f8200000040c8250320f809000000008fdc00100000102503c0e8258fbf001c8fbe001827bd002003e000080000000000000000";
	private static final String MIPS_BIG_PIC_TEXT =
			"3c1c0000279c00000399e02127bdffe0afbf001cafbe001803a0f025afbc0010afc40020afc500248f820000244400008f8200000040c8250320f809000000008fdc00100000102503c0e8258fbf001c8fbe001827bd002003e0000800000000";
	private static final String MIPS_LITTLE_NOPIC_TEXT =
			"e0ffbd271c00bfaf1800beaf25f0a00300001c3c00009c271000bcaf2000c4af2400c5af0000023c000044240000828f25c8400009f82003000000001000dc8f2510000025e8c0031c00bf8f1800be8f2000bd270800e0030000000000000000";
	private static final String MIPS_LITTLE_PIC_TEXT =
			"00001c3c00009c2721e09903e0ffbd271c00bfaf1800beaf25f0a0031000bcaf2000c4af2400c5af0000828f000044240000828f25c8400009f82003000000001000dc8f2510000025e8c0031c00bf8f1800be8f2000bd270800e00300000000";
	private static final String MIPS_BIG_PDR = "00000000c0000000fffffffc0000000000000000000000200000001e0000001f";
	private static final String MIPS_LITTLE_PDR = "00000000000000c0fcffffff0000000000000000200000001e0000001f000000";
	private static final String MIPS_RODATA = "48656c6c6f2c20776f726c6421000000";
	private static final String GCC12_COMMENT = "004743433a202844656269616e2031322e322e302d3134292031322e322e3000";
	private static final RelocationFieldCodec MIPS_HI16_CODEC =
			BitShiftedAdjustedFieldCodec.of(2, java.nio.ByteOrder.LITTLE_ENDIAN, 0, 16, 16, true);
	private static final RelocationFieldCodec MIPS_JALR_CODEC =
			BitShiftedFieldCodec.of(4, java.nio.ByteOrder.LITTLE_ENDIAN, 0, 26, 2, true);

	private ElfImageFixtureCatalog() {
		// Utility class
	}

	record FixtureSpec(
			String fixtureName,
			net.boricj.bft.elf.constants.ElfClass elfClass,
			net.boricj.bft.elf.constants.ElfData elfData,
			net.boricj.bft.elf.constants.ElfOsAbi osAbi,
			ElfMachine machine,
			Supplier<ImageFile> expectedImage) {}

	static Stream<FixtureSpec> objectFixtures() {
		return Stream.of(
				new FixtureSpec(
						"amd64/hello-world_x86_64-linux-gnu.nopic.o",
						ELFCLASS64,
						ELFDATA2LSB,
						ELFOSABI_NONE,
						ElfMachine.EM_X86_64,
						ElfImageFixtureCatalog::expectedAmd64NoPic),
				new FixtureSpec(
						"amd64/hello-world_x86_64-linux-gnu.o",
						ELFCLASS64,
						ELFDATA2LSB,
						ELFOSABI_NONE,
						ElfMachine.EM_X86_64,
						ElfImageFixtureCatalog::expectedAmd64Pic),
				new FixtureSpec(
						"i386/hello-world_i686-linux-gnu.nopic.o",
						ELFCLASS32,
						ELFDATA2LSB,
						ELFOSABI_NONE,
						ElfMachine.EM_386,
						ElfImageFixtureCatalog::expectedI386NoPic),
				new FixtureSpec(
						"i386/hello-world_i686-linux-gnu.o",
						ELFCLASS32,
						ELFDATA2LSB,
						ELFOSABI_NONE,
						ElfMachine.EM_386,
						ElfImageFixtureCatalog::expectedI386Pic),
				new FixtureSpec(
						"mips/hello-world_mips-linux-gnu.nopic.o",
						ELFCLASS32,
						ELFDATA2MSB,
						ELFOSABI_NONE,
						ElfMachine.EM_MIPS,
						ElfImageFixtureCatalog::expectedMipsBigNoPic),
				new FixtureSpec(
						"mips/hello-world_mips-linux-gnu.o",
						ELFCLASS32,
						ELFDATA2MSB,
						ELFOSABI_NONE,
						ElfMachine.EM_MIPS,
						ElfImageFixtureCatalog::expectedMipsBigPic),
				new FixtureSpec(
						"mips/hello-world_mipsel-linux-gnu.nopic.o",
						ELFCLASS32,
						ELFDATA2LSB,
						ELFOSABI_NONE,
						ElfMachine.EM_MIPS,
						ElfImageFixtureCatalog::expectedMipsLittleNoPic),
				new FixtureSpec(
						"mips/hello-world_mipsel-linux-gnu.o",
						ELFCLASS32,
						ELFDATA2LSB,
						ELFOSABI_NONE,
						ElfMachine.EM_MIPS,
						ElfImageFixtureCatalog::expectedMipsLittlePic));
	}

	static ElfFile.Builder builderFor(FixtureSpec spec) {
		return new ElfFile.Builder(spec.elfClass(), spec.elfData(), spec.osAbi(), ET_REL, spec.machine())
				.setPhentsize((short) 0);
	}

	private static ImageFile expectedAmd64NoPic() {
		ImageFile image = new ImageFile(ImageFile.Kind.OBJECT);
		ImageSection text = image.sections().create(".text");
		text.setContents(hex("554889e5bf00000000e800000000b8000000005dc3"));
		ImageSection data = image.sections().create(".data");
		data.setContents(new byte[0]);
		ImageSection bss = image.sections().create(".bss");
		bss.setContents(new byte[0]);
		ImageSection rodata = image.sections().create(".rodata");
		rodata.setContents(hex("48656c6c6f2c20776f726c642100"));
		ImageSection comment = image.sections().create(".comment");
		comment.setContents(hex("004743433a202844656269616e2031342e322e302d3139292031342e322e3000"));
		ImageSection note = image.sections().create(".note.GNU-stack");
		note.setContents(new byte[0]);

		image.symbols().createFile("hello-world.c");
		ImageSymbol rodataSymbol = image.symbols().createSection(rodata);
		image.symbols()
				.create(
						"main",
						text,
						0,
						21,
						ImageSymbol.Type.FUNCTION,
						ImageSymbol.Visibility.DEFAULT,
						ImageSymbol.Binding.GLOBAL);
		ImageSymbol puts = image.symbols()
				.createUndefined(
						"puts", 0, ImageSymbol.Type.NOTYPE, ImageSymbol.Visibility.DEFAULT, ImageSymbol.Binding.GLOBAL);

		addRelocation(text, 5, RelocationOperation.ABSOLUTE, ByteFieldCodec.U32LE, rodataSymbol, 0);
		addRelocation(text, 10, RelocationOperation.PLT, ByteFieldCodec.S32LE, puts, -4);
		return image;
	}

	private static ImageFile expectedAmd64Pic() {
		ImageFile image = new ImageFile(ImageFile.Kind.OBJECT);
		ImageSection text = image.sections().create(".text");
		text.setContents(hex("554889e5488d05000000004889c7e800000000b8000000005dc3"));
		ImageSection data = image.sections().create(".data");
		data.setContents(new byte[0]);
		ImageSection bss = image.sections().create(".bss");
		bss.setContents(new byte[0]);
		ImageSection rodata = image.sections().create(".rodata");
		rodata.setContents(hex("48656c6c6f2c20776f726c642100"));
		ImageSection comment = image.sections().create(".comment");
		comment.setContents(hex("004743433a202844656269616e2031342e322e302d3139292031342e322e3000"));
		ImageSection note = image.sections().create(".note.GNU-stack");
		note.setContents(new byte[0]);

		image.symbols().createFile("hello-world.c");
		ImageSymbol rodataSymbol = image.symbols().createSection(rodata);
		image.symbols()
				.create(
						"main",
						text,
						0,
						26,
						ImageSymbol.Type.FUNCTION,
						ImageSymbol.Visibility.DEFAULT,
						ImageSymbol.Binding.GLOBAL);
		ImageSymbol puts = image.symbols()
				.createUndefined(
						"puts", 0, ImageSymbol.Type.NOTYPE, ImageSymbol.Visibility.DEFAULT, ImageSymbol.Binding.GLOBAL);

		addRelocation(text, 7, RelocationOperation.PC_RELATIVE, ByteFieldCodec.S32LE, rodataSymbol, -4);
		addRelocation(text, 15, RelocationOperation.PLT, ByteFieldCodec.S32LE, puts, -4);
		return image;
	}

	private static ImageFile expectedI386NoPic() {
		ImageFile image = new ImageFile(ImageFile.Kind.OBJECT);
		ImageSection text = image.sections().create(".text");
		text.setContents(new byte[0]);
		ImageSection data = image.sections().create(".data");
		data.setContents(new byte[0]);
		ImageSection bss = image.sections().create(".bss");
		bss.setContents(new byte[0]);
		ImageSection rodata = image.sections().create(".rodata.str1.1");
		rodata.setContents(hex("48656c6c6f2c20776f726c642100"));
		ImageSection startup = image.sections().create(".text.startup");
		startup.setContents(hex("8d4c240483e4f0ff71fc5589e55183ec106800000000e8fcffffff8b4dfc31c0c98d61fcc3"));
		ImageSection comment = image.sections().create(".comment");
		comment.setContents(hex("004743433a202844656269616e2031322e322e302d3134292031322e322e3000"));
		ImageSection note = image.sections().create(".note.GNU-stack");
		note.setContents(new byte[0]);

		image.symbols().createFile("hello-world.c");
		ImageSymbol rodataSymbol = image.symbols().createSection(rodata);
		image.symbols()
				.create(
						"main",
						startup,
						0,
						37,
						ImageSymbol.Type.FUNCTION,
						ImageSymbol.Visibility.DEFAULT,
						ImageSymbol.Binding.GLOBAL);
		ImageSymbol puts = image.symbols()
				.createUndefined(
						"puts", 0, ImageSymbol.Type.NOTYPE, ImageSymbol.Visibility.DEFAULT, ImageSymbol.Binding.GLOBAL);

		addRelocation(startup, 18, RelocationOperation.ABSOLUTE, ByteFieldCodec.U32LE, rodataSymbol, 0);
		addRelocation(startup, 23, RelocationOperation.PC_RELATIVE, ByteFieldCodec.S32LE, puts, 0);
		return image;
	}

	private static ImageFile expectedI386Pic() {
		ImageFile image = new ImageFile(ImageFile.Kind.OBJECT);
		ImageSection text = image.sections().create(".text");
		text.setContents(
				hex(
						"8d4c240483e4f0ff71fc5589e55351e8fcffffff050100000083ec0c8d90000000005289c3e8fcffffff83c410b8000000008d65f8595b5d8d61fcc3"));
		ImageSection data = image.sections().create(".data");
		data.setContents(new byte[0]);
		ImageSection bss = image.sections().create(".bss");
		bss.setContents(new byte[0]);
		ImageSection rodata = image.sections().create(".rodata");
		rodata.setContents(hex("48656c6c6f2c20776f726c642100"));
		ImageSection thunk = image.sections().create(".text.__x86.get_pc_thunk.ax");
		thunk.setContents(hex("8b0424c3"));
		ImageSection comment = image.sections().create(".comment");
		comment.setContents(hex("004743433a202844656269616e2031322e322e302d3134292031322e322e3000"));
		ImageSection note = image.sections().create(".note.GNU-stack");
		note.setContents(new byte[0]);

		image.symbols().createFile("hello-world.c");
		ImageSymbol rodataSymbol = image.symbols().createSection(rodata);
		image.symbols()
				.create(
						"main",
						text,
						0,
						60,
						ImageSymbol.Type.FUNCTION,
						ImageSymbol.Visibility.DEFAULT,
						ImageSymbol.Binding.GLOBAL);
		ImageSymbol thunkSymbol = image.symbols()
				.create(
						"__x86.get_pc_thunk.ax",
						thunk,
						0,
						0,
						ImageSymbol.Type.FUNCTION,
						ImageSymbol.Visibility.HIDDEN,
						ImageSymbol.Binding.GLOBAL);
		ImageSymbol got = image.symbols()
				.createUndefined(
						"_GLOBAL_OFFSET_TABLE_",
						0,
						ImageSymbol.Type.NOTYPE,
						ImageSymbol.Visibility.DEFAULT,
						ImageSymbol.Binding.GLOBAL);
		ImageSymbol puts = image.symbols()
				.createUndefined(
						"puts", 0, ImageSymbol.Type.NOTYPE, ImageSymbol.Visibility.DEFAULT, ImageSymbol.Binding.GLOBAL);

		addRelocation(text, 16, RelocationOperation.PC_RELATIVE, ByteFieldCodec.S32LE, thunkSymbol, 0);
		addRelocation(text, 21, RelocationOperation.GOT, ByteFieldCodec.S32LE, got, 0);
		addRelocation(text, 30, RelocationOperation.REGION_RELATIVE, ByteFieldCodec.S32LE, rodataSymbol, 0);
		addRelocation(text, 38, RelocationOperation.PLT, ByteFieldCodec.S32LE, puts, 0);
		return image;
	}

	private static ImageFile expectedMipsBigNoPic() {
		return expectedMipsImage(false, false);
	}

	private static ImageFile expectedMipsBigPic() {
		return expectedMipsImage(true, false);
	}

	private static ImageFile expectedMipsLittleNoPic() {
		return expectedMipsImage(false, true);
	}

	private static ImageFile expectedMipsLittlePic() {
		return expectedMipsImage(true, true);
	}

	private static ImageFile expectedMipsImage(boolean pic, boolean littleEndian) {
		ImageFile image = new ImageFile(ImageFile.Kind.OBJECT);
		ImageSection text = addMipsTextSection(image, pic, littleEndian);
		ImageSection data = addEmptySection(image, ".data");
		ImageSection bss = addEmptySection(image, ".bss");
		ImageSection pdr = addMipsPdrSection(image, littleEndian);
		ImageSection mdebug = addEmptySection(image, ".mdebug.abi32");
		ImageSection rodata = addSection(image, ".rodata", MIPS_RODATA);
		ImageSection comment = addSection(image, ".comment", GCC12_COMMENT);
		ImageSection note = addEmptySection(image, ".note.GNU-stack");

		addMipsSectionSymbols(image, text, data, bss, mdebug, note, rodata, pdr, comment);
		ImageSymbol rodataSectionSymbol = image.symbols().get(6);
		ImageSymbol main = image.symbols()
				.create(
						"main",
						text,
						0,
						pic ? 96 : 92,
						ImageSymbol.Type.FUNCTION,
						ImageSymbol.Visibility.DEFAULT,
						ImageSymbol.Binding.GLOBAL);
		ImageSymbol gpSymbol = image.symbols()
				.createUndefined(
						pic ? "_gp_disp" : "__gnu_local_gp",
						0,
						pic ? ImageSymbol.Type.OBJECT : ImageSymbol.Type.NOTYPE,
						ImageSymbol.Visibility.DEFAULT,
						ImageSymbol.Binding.GLOBAL);
		ImageSymbol puts = image.symbols()
				.createUndefined(
						"puts", 0, ImageSymbol.Type.NOTYPE, ImageSymbol.Visibility.DEFAULT, ImageSymbol.Binding.GLOBAL);

		addMipsTextRelocations(text, rodataSectionSymbol, gpSymbol, puts, pic);
		addRelocation(pdr, 0, RelocationOperation.ABSOLUTE, ByteFieldCodec.U32LE, main, 0);
		return image;
	}

	private static ImageSection addMipsTextSection(ImageFile image, boolean pic, boolean littleEndian) {
		String bytes;
		if (pic) {
			bytes = littleEndian ? MIPS_LITTLE_PIC_TEXT : MIPS_BIG_PIC_TEXT;
		} else {
			bytes = littleEndian ? MIPS_LITTLE_NOPIC_TEXT : MIPS_BIG_NOPIC_TEXT;
		}
		return addSection(image, ".text", bytes);
	}

	private static ImageSection addMipsPdrSection(ImageFile image, boolean littleEndian) {
		return addSection(image, ".pdr", littleEndian ? MIPS_LITTLE_PDR : MIPS_BIG_PDR);
	}

	private static void addMipsSectionSymbols(
			ImageFile image,
			ImageSection text,
			ImageSection data,
			ImageSection bss,
			ImageSection mdebug,
			ImageSection note,
			ImageSection rodata,
			ImageSection pdr,
			ImageSection comment) {
		image.symbols().createFile("hello-world.c");
		image.symbols().createSection(text);
		image.symbols().createSection(data);
		image.symbols().createSection(bss);
		image.symbols().createSection(mdebug);
		image.symbols().createSection(note);
		image.symbols().createSection(rodata);
		image.symbols().createSection(pdr);
		image.symbols().createSection(comment);
	}

	private static void addMipsTextRelocations(
			ImageSection text, ImageSymbol rodataSectionSymbol, ImageSymbol gpSymbol, ImageSymbol puts, boolean pic) {
		if (pic) {
			addRelocation(text, 0, RelocationOperation.ABSOLUTE, MIPS_HI16_CODEC, gpSymbol, 0);
			addRelocation(text, 4, RelocationOperation.ABSOLUTE, ByteFieldCodec.S16LE, gpSymbol, 0);
			addRelocation(text, 40, RelocationOperation.GOT, ByteFieldCodec.S16LE, rodataSectionSymbol, 0);
			addRelocation(text, 44, RelocationOperation.ABSOLUTE, ByteFieldCodec.S16LE, rodataSectionSymbol, 0);
			addRelocation(text, 48, RelocationOperation.PLT, ByteFieldCodec.S16LE, puts, 0);
			addRelocation(text, 56, RelocationOperation.REGION_RELATIVE, MIPS_JALR_CODEC, puts, 0);
			return;
		}

		addRelocation(text, 16, RelocationOperation.ABSOLUTE, MIPS_HI16_CODEC, gpSymbol, 0);
		addRelocation(text, 20, RelocationOperation.ABSOLUTE, ByteFieldCodec.S16LE, gpSymbol, 0);
		addRelocation(text, 36, RelocationOperation.ABSOLUTE, MIPS_HI16_CODEC, rodataSectionSymbol, 0);
		addRelocation(text, 40, RelocationOperation.ABSOLUTE, ByteFieldCodec.S16LE, rodataSectionSymbol, 0);
		addRelocation(text, 44, RelocationOperation.PLT, ByteFieldCodec.S16LE, puts, 0);
		addRelocation(text, 52, RelocationOperation.REGION_RELATIVE, MIPS_JALR_CODEC, puts, 0);
	}

	private static ImageSection addEmptySection(ImageFile image, String name) {
		ImageSection section = image.sections().create(name);
		section.setContents(new byte[0]);
		return section;
	}

	private static ImageSection addSection(ImageFile image, String name, String hexBytes) {
		ImageSection section = image.sections().create(name);
		section.setContents(hex(hexBytes));
		return section;
	}

	private static byte[] hex(String value) {
		return HEX.parseHex(value);
	}

	private static void addRelocation(
			ImageSection section,
			long offset,
			RelocationOperation operation,
			RelocationFieldCodec fieldCodec,
			ImageSymbol target,
			long addend) {
		section.relocations().createSingleEntry(offset, operation, fieldCodec, target, addend);
	}
}
