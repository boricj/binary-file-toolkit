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
package net.boricj.bft.elf.amd64;

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
import net.boricj.bft.elf.machines.amd64.ElfRelocationType_amd64;
import net.boricj.bft.elf.sections.ElfNoBits;
import net.boricj.bft.elf.sections.ElfNullSection;
import net.boricj.bft.elf.sections.ElfProgBits;
import net.boricj.bft.elf.sections.ElfRelaTable;
import net.boricj.bft.elf.sections.ElfStringTable;
import net.boricj.bft.elf.sections.ElfSymbolTable;
import net.boricj.bft.elf.sections.ElfSymbolTable.ElfSymbol;

import static net.boricj.bft.elf.constants.ElfClass.ELFCLASS64;
import static net.boricj.bft.elf.constants.ElfData.ELFDATA2LSB;
import static net.boricj.bft.elf.constants.ElfOsAbi.ELFOSABI_NONE;
import static net.boricj.bft.elf.constants.ElfSectionNames._BSS;
import static net.boricj.bft.elf.constants.ElfSectionNames._COMMENT;
import static net.boricj.bft.elf.constants.ElfSectionNames._DATA;
import static net.boricj.bft.elf.constants.ElfSectionNames._NOTE_GNU_STACK;
import static net.boricj.bft.elf.constants.ElfSectionNames._RELA;
import static net.boricj.bft.elf.constants.ElfSectionNames._RODATA;
import static net.boricj.bft.elf.constants.ElfSectionNames._SHSTRTAB;
import static net.boricj.bft.elf.constants.ElfSectionNames._STRTAB;
import static net.boricj.bft.elf.constants.ElfSectionNames._SYMTAB;
import static net.boricj.bft.elf.constants.ElfSectionNames._TEXT;
import static net.boricj.bft.elf.constants.ElfSymbolBinding.STB_GLOBAL;
import static net.boricj.bft.elf.constants.ElfSymbolType.STT_FUNC;
import static net.boricj.bft.elf.constants.ElfType.ET_REL;

public class TestHelloWorld {
	public final byte[] RODATA_BYTES = "Hello, world!\0".getBytes(StandardCharsets.US_ASCII);
	public final byte[] TEXT_BYTES = HexFormat.of().parseHex("554889e5bf00000000e800000000b8000000005dc3");
	public final byte[] COMMENT_BYTES = "\0GCC: (Debian 14.2.0-19) 14.2.0\0".getBytes(StandardCharsets.US_ASCII);

	public final String _RELA_TEXT = _RELA + _TEXT;

	public static final ElfSectionFlags A = new ElfSectionFlags().alloc();
	public static final ElfSectionFlags AX = new ElfSectionFlags().alloc().execInstr();
	public static final ElfSectionFlags WA = new ElfSectionFlags().write().alloc();
	public static final ElfSectionFlags MS = new ElfSectionFlags().merge().strings();

	@Test
	public void test_hello_world_amd64_linux_gnu_nopic() throws IOException, URISyntaxException {
		ElfFile elf = new ElfFile.Builder(ELFCLASS64, ELFDATA2LSB, ELFOSABI_NONE, ET_REL, ElfMachine.EM_X86_64)
				.setPhentsize((short) 0)
				.build();
		ElfHeader header = elf.getHeader();
		ElfSectionTable sectionTable = elf.addSectionTable();

		ElfProgBits rodata = new ElfProgBits(elf, _RODATA, A, 1, 0, RODATA_BYTES);
		ElfProgBits text = new ElfProgBits(elf, _TEXT, AX, 1, TEXT_BYTES);
		ElfStringTable strtab = new ElfStringTable(elf, _STRTAB);
		ElfSymbolTable symtab = new ElfSymbolTable(elf, _SYMTAB, strtab);
		ElfRelaTable relaText = new ElfRelaTable(elf, _RELA_TEXT, symtab, text);
		ElfStringTable shstrtab = new ElfStringTable(elf, _SHSTRTAB);

		sectionTable.add(new ElfNullSection(elf));
		sectionTable.add(text);
		sectionTable.add(relaText);
		sectionTable.add(new ElfProgBits(elf, _DATA, WA, 1, new byte[0]));
		sectionTable.add(new ElfNoBits(elf, _BSS, WA, 1, 0));
		sectionTable.add(rodata);
		sectionTable.add(new ElfProgBits(elf, _COMMENT, MS, 1, 1, COMMENT_BYTES));
		sectionTable.add(new ElfProgBits(elf, _NOTE_GNU_STACK, new ElfSectionFlags(), 1, new byte[0]));
		sectionTable.add(symtab);
		sectionTable.add(strtab);
		sectionTable.add(shstrtab);

		symtab.addNull();
		symtab.addFile("hello-world.c");
		ElfSymbol symbol_rodata = symtab.addSection(rodata);
		symtab.addDefined("main", 0, 21, STT_FUNC, STB_GLOBAL, text);
		ElfSymbol symbol_puts = symtab.addUndefined("puts");

		strtab.add("");
		strtab.add("hello-world.c");
		strtab.add("main");
		strtab.add("puts");

		relaText.add(0x5, symbol_rodata, ElfRelocationType_amd64.R_X86_64_32, 0);
		relaText.add(0xa, symbol_puts, ElfRelocationType_amd64.R_X86_64_PLT32, -4);

		sectionTable.get(1).setOffset(0x40);
		sectionTable.get(2).setOffset(0x120);
		sectionTable.get(3).setOffset(0x55);
		sectionTable.get(4).setOffset(0x55);
		sectionTable.get(5).setOffset(0x55);
		sectionTable.get(6).setOffset(0x63);
		sectionTable.get(7).setOffset(0x83);
		sectionTable.get(8).setOffset(0x88);
		sectionTable.get(9).setOffset(0x100);
		sectionTable.get(10).setOffset(0x150);
		header.setShoff(424);
		header.setShStr(shstrtab);

		shstrtab.add("");
		shstrtab.add(_SYMTAB);
		shstrtab.add(_STRTAB);
		shstrtab.add(_SHSTRTAB);
		shstrtab.add(_RELA_TEXT);
		shstrtab.add(_TEXT, _RELA_TEXT);
		shstrtab.add(_DATA);
		shstrtab.add(_BSS);
		shstrtab.add(_RODATA);
		shstrtab.add(_COMMENT);
		shstrtab.add(_NOTE_GNU_STACK);

		Collection<Writable> writables = Stream.concat(List.of(header, sectionTable).stream(), sectionTable.stream())
				.collect(Collectors.toList());

		TestUtils.compare(getClass().getResourceAsStream("hello-world_x86_64-linux-gnu.nopic.o"), writables);
	}
}
