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
package net.boricj.bft.coff.powerpcbe;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import net.boricj.bft.TestUtils;
import net.boricj.bft.Writable;
import net.boricj.bft.coff.CoffFile;
import net.boricj.bft.coff.CoffHeader;
import net.boricj.bft.coff.CoffRelocationTable;
import net.boricj.bft.coff.CoffSection;
import net.boricj.bft.coff.CoffSectionTable;
import net.boricj.bft.coff.CoffStringTable;
import net.boricj.bft.coff.CoffSymbolTable;
import net.boricj.bft.coff.CoffSymbolTable.CoffSymbol;
import net.boricj.bft.coff.constants.CoffMachine;
import net.boricj.bft.coff.constants.CoffSectionFlags;
import net.boricj.bft.coff.constants.CoffStorageClass;
import net.boricj.bft.coff.machines.powerpcbe.CoffRelocationType_powerpcbe;
import net.boricj.bft.coff.sections.CoffBytes;

public class TestHelloWorld {
	public final byte[] DRECTVE_BYTES =
			"   /DEFAULTLIB:\"OLDNAMES\" /DEFAULTLIB:\"LIBCMT\" /DEFAULTLIB:\"XAPILIB\" /DEFAULTLIB:\"XBOXKRNL\" "
					.getBytes(StandardCharsets.US_ASCII);
	public final byte[] DEBUG_S_BYTES = HexFormat.of()
			.parseHex(
					"04000000f10000008b0000005000011100000000433a5c55736572735c776964626572675c4465736b746f705c68656c6c6f2d776f726c645f706f776572706362652d6d6963726f736f66742d78626f783336302d6d7376632e6f626a00370016110002000042000f000000d91f0f000000d91f4d6963726f736f667420285229204f7074696d697a696e6720436f6d70696c6572000000");
	public final byte[] DATA_BYTES = "Hello, World!\n\0".getBytes(StandardCharsets.US_ASCII);
	public final byte[] TEXT_BYTES = HexFormat.of()
			.parseHex("7d8802a69181fff89421ffa03d600000386b00004bffffed38600000382100608181fff87d8803a64e800020");
	public final byte[] PDATA_BYTES = HexFormat.of().parseHex("0000000040000b03");

	public final String _DRECTVE = ".drectve";
	public final String _DEBUG_S = ".debug$S";
	public final String _DATA = ".data";
	public final String _TEXT = ".text";
	public final String _PDATA = ".pdata";

	@Test
	public void test_hello_world_powerpc_microsoft_xbox360_msvc() throws IOException {
		CoffFile coff = new CoffFile.Builder(CoffMachine.IMAGE_FILE_MACHINE_POWERPCBE)
				.setTimeDateStamp(0x69E8DFE9)
				.setCharacteristics((short) 0x0180)
				.build();
		CoffHeader header = coff.getHeader();
		CoffSectionTable sectionTable = coff.getSections();
		CoffStringTable stringTable = coff.getStrings();
		CoffSymbolTable symbolTable = coff.getSymbols();

		CoffSectionFlags characteristics;
		characteristics = new CoffSectionFlags().lnkInfo().lnkRemove().alignBytes(1);
		CoffSection drectve = new CoffBytes(coff, _DRECTVE, characteristics, DRECTVE_BYTES);
		sectionTable.add(drectve);
		characteristics = new CoffSectionFlags()
				.cntInitializedData()
				.alignBytes(1)
				.memDiscardable()
				.memRead();
		CoffSection debug_s = new CoffBytes(coff, _DEBUG_S, characteristics, DEBUG_S_BYTES);
		sectionTable.add(debug_s);
		characteristics = new CoffSectionFlags()
				.cntInitializedData()
				.alignBytes(4)
				.memRead()
				.memWrite();
		CoffSection data = new CoffBytes(coff, _DATA, characteristics, DATA_BYTES);
		sectionTable.add(data);
		characteristics =
				new CoffSectionFlags().cntCode().alignBytes(8).memExecute().memRead();
		CoffSection text = new CoffBytes(coff, _TEXT, characteristics, TEXT_BYTES);
		sectionTable.add(text);
		characteristics =
				new CoffSectionFlags().cntInitializedData().alignBytes(8).memRead();
		CoffSection pdata = new CoffBytes(coff, _PDATA, characteristics, PDATA_BYTES);
		sectionTable.add(pdata);

		symbolTable.addAbsolute("@comp.id", 0x00831FD9);
		symbolTable.addSection(drectve);
		symbolTable.addSection(debug_s);
		symbolTable.addSection(data);
		CoffSymbol _SG2061 =
				symbolTable.addSymbol("$SG2061", 0, data, (byte) 0x00, CoffStorageClass.IMAGE_SYM_CLASS_STATIC);
		symbolTable.addSection(text);
		CoffSymbol _main =
				symbolTable.addSymbol("main", 0, text, (byte) 0x20, CoffStorageClass.IMAGE_SYM_CLASS_EXTERNAL);
		symbolTable.addSymbol("$M2067", 0x2c, text, (byte) 0x00, CoffStorageClass.IMAGE_SYM_CLASS_LABEL);
		CoffSymbol _puts = symbolTable.addUndefined("puts");
		symbolTable.addSymbol("$M2066", 0x0c, text, (byte) 0x00, CoffStorageClass.IMAGE_SYM_CLASS_LABEL);
		symbolTable.addSection(pdata);
		symbolTable.addSymbol("$T2068", 0, pdata, (byte) 0x00, CoffStorageClass.IMAGE_SYM_CLASS_STATIC);

		CoffRelocationTable text_rel = text.getRelocations();
		text_rel.add(0x0c, _SG2061, CoffRelocationType_powerpcbe.IMAGE_REL_PPC_REFHI);
		text_rel.add(0x0c, 0, CoffRelocationType_powerpcbe.IMAGE_REL_PPC_PAIR);
		text_rel.add(0x10, _SG2061, CoffRelocationType_powerpcbe.IMAGE_REL_PPC_REFLO);
		text_rel.add(0x10, 0, CoffRelocationType_powerpcbe.IMAGE_REL_PPC_PAIR);
		text_rel.add(0x14, _puts, CoffRelocationType_powerpcbe.IMAGE_REL_PPC_REL24);

		CoffRelocationTable pdata_rel = pdata.getRelocations();
		pdata_rel.add(0x0, _main, CoffRelocationType_powerpcbe.IMAGE_REL_PPC_ADDR32);

		drectve.setOffset(0x0dc);
		debug_s.setOffset(0x138);
		data.setOffset(0x1d0);
		text.setOffset(0x1df);
		text_rel.setOffset(0x20b);
		pdata.setOffset(0x23d);
		pdata_rel.setOffset(0x245);
		symbolTable.setOffset(0x24f);

		Stream<Writable> streamWritables = Stream.of(
						List.of(header, sectionTable, symbolTable, stringTable).stream(),
						sectionTable.stream(),
						sectionTable.stream().map(s -> s.getRelocations()))
				.flatMap(Function.identity());
		Collection<Writable> writables = streamWritables.collect(Collectors.toList());

		final Map<Integer, byte[]> patches = Map.ofEntries(
				Map.entry(0x2c3, new byte[] {(byte) 0xCD, (byte) 0x43, (byte) 0xB0, (byte) 0xEF
				}), // .data section symbol checksum
				Map.entry(0x365, new byte[] {(byte) 0x4C, (byte) 0xD0, (byte) 0xE9, (byte) 0xE1
				}) // .pdata section symbol checksum
				);

		TestUtils.compare(
				getClass().getResourceAsStream("hello-world_powerpcbe-microsoft-xbox360-msvc.obj"), writables, patches);
	}
}
