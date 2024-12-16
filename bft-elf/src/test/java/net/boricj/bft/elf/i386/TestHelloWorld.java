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
package net.boricj.bft.elf.i386;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import net.boricj.bft.TestUtils;
import net.boricj.bft.Writable;
import net.boricj.bft.elf.ElfFile;
import net.boricj.bft.elf.ElfHeader;
import net.boricj.bft.elf.ElfSectionFlags;
import net.boricj.bft.elf.ElfSectionTable;
import net.boricj.bft.elf.constants.ElfMachine;
import net.boricj.bft.elf.machines.i386.ElfRelocationType_i386;
import net.boricj.bft.elf.sections.ElfNoBits;
import net.boricj.bft.elf.sections.ElfNullSection;
import net.boricj.bft.elf.sections.ElfProgBits;
import net.boricj.bft.elf.sections.ElfRelTable;
import net.boricj.bft.elf.sections.ElfStringTable;
import net.boricj.bft.elf.sections.ElfSymbolTable;
import net.boricj.bft.elf.sections.ElfSymbolTable.ElfSymbol;

import static net.boricj.bft.elf.constants.ElfClass.ELFCLASS32;
import static net.boricj.bft.elf.constants.ElfData.ELFDATA2LSB;
import static net.boricj.bft.elf.constants.ElfOsAbi.ELFOSABI_NONE;
import static net.boricj.bft.elf.constants.ElfSectionNames._BSS;
import static net.boricj.bft.elf.constants.ElfSectionNames._COMMENT;
import static net.boricj.bft.elf.constants.ElfSectionNames._DATA;
import static net.boricj.bft.elf.constants.ElfSectionNames._NOTE_GNU_STACK;
import static net.boricj.bft.elf.constants.ElfSectionNames._REL;
import static net.boricj.bft.elf.constants.ElfSectionNames._RODATA;
import static net.boricj.bft.elf.constants.ElfSectionNames._SHSTRTAB;
import static net.boricj.bft.elf.constants.ElfSectionNames._STRTAB;
import static net.boricj.bft.elf.constants.ElfSectionNames._SYMTAB;
import static net.boricj.bft.elf.constants.ElfSectionNames._TEXT;
import static net.boricj.bft.elf.constants.ElfSymbolBinding.STB_GLOBAL;
import static net.boricj.bft.elf.constants.ElfSymbolType.STT_FUNC;
import static net.boricj.bft.elf.constants.ElfType.ET_REL;

public class TestHelloWorld {
	public final byte[] RODATA_STR1_1_BYTES = "Hello, world!\0".getBytes(StandardCharsets.US_ASCII);
	public final byte[] TEXT_STARTUP_BYTES =
			HexFormat.of().parseHex("8d4c240483e4f0ff71fc5589e55183ec106800000000e8fcffffff8b4dfc31c0c98d61fcc3");
	public final byte[] COMMENT_BYTES = "\0GCC: (Debian 12.2.0-14) 12.2.0\0".getBytes(StandardCharsets.US_ASCII);

	public final String _TEXT_STARTUP = _TEXT + ".startup";
	public final String _REL_TEXT_STARTUP = _REL + _TEXT_STARTUP;
	public final String _RODATA_STR1_1 = _RODATA + ".str1.1";

	public static final ElfSectionFlags AX = new ElfSectionFlags().alloc().execInstr();
	public static final ElfSectionFlags WA = new ElfSectionFlags().write().alloc();
	public static final ElfSectionFlags MS = new ElfSectionFlags().merge().strings();
	public static final ElfSectionFlags AMS =
			new ElfSectionFlags().alloc().merge().strings();

	@Test
	public void test_hello_world_i686_linux_gnu_nopic() throws IOException, URISyntaxException {
		ElfFile elf = new ElfFile.Builder(ELFCLASS32, ELFDATA2LSB, ELFOSABI_NONE, ET_REL, ElfMachine.EM_386)
				.setPhentsize((short) 0)
				.build();
		ElfHeader header = elf.getHeader();
		ElfSectionTable sectionTable = elf.addSectionTable();

		sectionTable.add(new ElfNullSection(elf));
		sectionTable.add(new ElfProgBits(elf, _TEXT, AX, 1, new byte[0]));
		sectionTable.add(new ElfProgBits(elf, _DATA, WA, 1, new byte[0]));
		sectionTable.add(new ElfNoBits(elf, _BSS, WA, 1, 0));
		ElfProgBits rodataStr11 = new ElfProgBits(elf, _RODATA_STR1_1, AMS, 1, 1, RODATA_STR1_1_BYTES);
		sectionTable.add(rodataStr11);
		ElfProgBits textStartup = new ElfProgBits(elf, _TEXT_STARTUP, AX, 1, TEXT_STARTUP_BYTES);
		sectionTable.add(textStartup);
		ElfStringTable strtab = new ElfStringTable(elf, _STRTAB);
		ElfSymbolTable symtab = new ElfSymbolTable(elf, _SYMTAB, strtab);
		ElfRelTable relTextStartup = new ElfRelTable(elf, _REL_TEXT_STARTUP, symtab, textStartup);
		sectionTable.add(relTextStartup);
		sectionTable.add(new ElfProgBits(elf, _COMMENT, MS, 1, 1, COMMENT_BYTES));
		sectionTable.add(new ElfProgBits(elf, _NOTE_GNU_STACK, new ElfSectionFlags(), 1, new byte[0]));
		sectionTable.add(symtab);
		sectionTable.add(strtab);
		ElfStringTable shstrtab = new ElfStringTable(elf, _SHSTRTAB);
		sectionTable.add(shstrtab);

		symtab.addNull();
		symtab.addFile("hello-world.c");
		ElfSymbol symbol_rodataStr11 = symtab.addSection(rodataStr11);
		symtab.addDefined("main", 0, 37, STT_FUNC, STB_GLOBAL, textStartup);
		ElfSymbol symbol_puts = symtab.addUndefined("puts");

		strtab.add("");
		strtab.add("hello-world.c");
		strtab.add("main");
		strtab.add("puts");

		relTextStartup.add(0x12, symbol_rodataStr11, ElfRelocationType_i386.R_386_32);
		relTextStartup.add(0x17, symbol_puts, ElfRelocationType_i386.R_386_PC32);

		sectionTable.get(1).setOffset(0x34);
		sectionTable.get(2).setOffset(0x34);
		sectionTable.get(3).setOffset(0x34);
		sectionTable.get(4).setOffset(0x34);
		sectionTable.get(5).setOffset(0x42);
		sectionTable.get(6).setOffset(0xf4);
		sectionTable.get(7).setOffset(0x67);
		sectionTable.get(8).setOffset(0x87);
		sectionTable.get(9).setOffset(0x88);
		sectionTable.get(10).setOffset(0xd8);
		sectionTable.get(11).setOffset(0x104);
		header.setShoff(364);
		header.setShStr(shstrtab);

		shstrtab.add("");
		shstrtab.add(_SYMTAB);
		shstrtab.add(_STRTAB);
		shstrtab.add(_SHSTRTAB);
		shstrtab.add(_TEXT);
		shstrtab.add(_DATA);
		shstrtab.add(_BSS);
		shstrtab.add(_RODATA_STR1_1);
		shstrtab.add(_REL_TEXT_STARTUP);
		shstrtab.add(_TEXT_STARTUP, _REL_TEXT_STARTUP);
		shstrtab.add(_COMMENT);
		shstrtab.add(_NOTE_GNU_STACK);

		Collection<Writable> writables = Stream.concat(List.of(header, sectionTable).stream(), sectionTable.stream())
				.collect(Collectors.toList());

		TestUtils.compare(getClass().getResourceAsStream("hello-world_i686-linux-gnu.nopic.o"), writables);
	}
}
