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
package net.boricj.bft.coff.amd64;

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
import net.boricj.bft.coff.machines.amd64.CoffRelocationType_amd64;
import net.boricj.bft.coff.sections.CoffBytes;

public class TestHelloWorld {
	public final byte[] DRECTVE_BYTES =
			"   /DEFAULTLIB:\"LIBCMT\" /DEFAULTLIB:\"OLDNAMES\" ".getBytes(StandardCharsets.US_ASCII);
	public final byte[] DEBUG_S_BYTES = HexFormat.of()
			.parseHex(
					"04000000F10000007D0000003F00011100000000443A5C70726F6772616D6D696E675C776964626572675C62696E6172792D66696C652D746F6F6C6B69745C646973745C6D61696E2E6F626A003A003C1100620000D00013002C008F89000013002C008F8900004D6963726F736F667420285229204F7074696D697A696E6720436F6D70696C657200000000");
	public final byte[] TEXT_MN_BYTES = HexFormat.of().parseHex("4883EC28488D0D00000000E80000000033C04883C428C3");
	public final byte[] XDATA_BYTES = HexFormat.of().parseHex("0104010004420000");
	public final byte[] PDATA_BYTES = HexFormat.of().parseHex("000000001700000000000000");
	public final byte[] DATA_BYTES = "Hello, World!\0".getBytes(StandardCharsets.US_ASCII);
	public final byte[] CHKS64_BYTES = HexFormat.of()
			.parseHex(
					"23076615271ABF1AF1BB89DE55F408B9CB0F62BA849F3D962D8867F028F39EF520574A761CE82E87219942BB7CFA570C0000000000000000");

	public final String _DRECTVE = ".drectve";
	public final String _DEBUG_S = ".debug$S";
	public final String _TEXT_MN = ".text$mn";
	public final String _XDATA = ".xdata";
	public final String _PDATA = ".pdata";
	public final String _DATA = ".data";
	public final String _CHKS64 = ".chks64";

	@Test
	public void test_hello_world_amd64_pc_windows_msvc() throws IOException {
		CoffFile coff = new CoffFile.Builder(CoffMachine.IMAGE_FILE_MACHINE_AMD64)
				.setTimeDateStamp(0x69595421)
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
		characteristics =
				new CoffSectionFlags().cntCode().alignBytes(16).memExecute().memRead();
		CoffSection text_mn = new CoffBytes(coff, _TEXT_MN, characteristics, TEXT_MN_BYTES);
		sectionTable.add(text_mn);
		characteristics =
				new CoffSectionFlags().cntInitializedData().alignBytes(4).memRead();
		CoffSection xdata = new CoffBytes(coff, _XDATA, characteristics, XDATA_BYTES);
		sectionTable.add(xdata);
		characteristics =
				new CoffSectionFlags().cntInitializedData().alignBytes(4).memRead();
		CoffSection pdata = new CoffBytes(coff, _PDATA, characteristics, PDATA_BYTES);
		sectionTable.add(pdata);
		characteristics = new CoffSectionFlags()
				.cntInitializedData()
				.alignBytes(8)
				.memRead()
				.memWrite();
		CoffSection data = new CoffBytes(coff, _DATA, characteristics, DATA_BYTES);
		sectionTable.add(data);
		characteristics = new CoffSectionFlags().lnkInfo().lnkRemove();
		CoffSection chks64 = new CoffBytes(coff, _CHKS64, characteristics, CHKS64_BYTES);
		sectionTable.add(chks64);

		stringTable.add("$unwind$main");
		stringTable.add("$pdata$main");

		symbolTable.addAbsolute("@comp.id", 0x0104898F);
		symbolTable.addAbsolute("@feat.00", 0x80010190);
		symbolTable.addAbsolute("@vol.md", 0x00000003);
		symbolTable.addSection(drectve);
		symbolTable.addSection(debug_s);
		symbolTable.addSection(text_mn);
		CoffSymbol _puts = symbolTable.addUndefined("puts");
		symbolTable.addSymbol("main", 0, text_mn, (byte) 0x20, CoffStorageClass.IMAGE_SYM_CLASS_EXTERNAL);
		CoffSymbol _LN3 =
				symbolTable.addSymbol("$LN3", 0, text_mn, (byte) 0x00, CoffStorageClass.IMAGE_SYM_CLASS_LABEL);
		symbolTable.addSection(xdata);
		CoffSymbol _unwind_main_symbol =
				symbolTable.addSymbol("$unwind$main", 0, xdata, (byte) 0x00, CoffStorageClass.IMAGE_SYM_CLASS_STATIC);
		symbolTable.addSection(pdata);
		symbolTable.addSymbol("$pdata$main", 0, pdata, (byte) 0x00, CoffStorageClass.IMAGE_SYM_CLASS_STATIC);
		symbolTable.addSection(data);
		CoffSymbol _SG9913 =
				symbolTable.addSymbol("$SG9913", 0, data, (byte) 0x00, CoffStorageClass.IMAGE_SYM_CLASS_STATIC);
		symbolTable.addSection(chks64);

		CoffRelocationTable text_mn_rel = text_mn.getRelocations();
		text_mn_rel.add(0x7, _SG9913, CoffRelocationType_amd64.IMAGE_REL_AMD64_REL32);
		text_mn_rel.add(0xC, _puts, CoffRelocationType_amd64.IMAGE_REL_AMD64_REL32);

		CoffRelocationTable pdata_rel = pdata.getRelocations();
		pdata_rel.add(0x0, _LN3, CoffRelocationType_amd64.IMAGE_REL_AMD64_ADDR32NB);
		pdata_rel.add(0x4, _LN3, CoffRelocationType_amd64.IMAGE_REL_AMD64_ADDR32NB);
		pdata_rel.add(0x8, _unwind_main_symbol, CoffRelocationType_amd64.IMAGE_REL_AMD64_ADDR32NB);

		drectve.setOffset(0x12C);
		debug_s.setOffset(0x15B);
		text_mn.setOffset(0x1E7);
		text_mn_rel.setOffset(0x1FE);
		xdata.setOffset(0x212);
		pdata.setOffset(0x21A);
		pdata_rel.setOffset(0x226);
		data.setOffset(0x244);
		chks64.setOffset(0x252);
		symbolTable.setOffset(0x28A);

		Stream<Writable> streamWritables = Stream.of(
						List.of(header, sectionTable, symbolTable, stringTable).stream(),
						sectionTable.stream(),
						sectionTable.stream().map(s -> s.getRelocations()))
				.flatMap(Function.identity());
		Collection<Writable> writables = streamWritables.collect(Collectors.toList());

		final Map<Integer, byte[]> patches = Map.ofEntries(
				Map.entry(0x322, new byte[] {(byte) 0xBE, (byte) 0x6E, (byte) 0x84, (byte) 0x01
				}), // .text$mn section symbol checksum
				Map.entry(0x37C, new byte[] {(byte) 0xD1, (byte) 0x39, (byte) 0xC5, (byte) 0x0F
				}), // .xdata section symbol checksum
				Map.entry(0x3B2, new byte[] {(byte) 0x32, (byte) 0x38, (byte) 0x7E, (byte) 0x76
				}), // .pdata section symbol checksum
				Map.entry(0x3E8, new byte[] {(byte) 0x5D, (byte) 0x0E, (byte) 0x86, (byte) 0x85
				}) // .data section symbol checksum
				);

		TestUtils.compare(getClass().getResourceAsStream("hello-world_amd64-pc-windows-msvc.obj"), writables, patches);
	}
}
