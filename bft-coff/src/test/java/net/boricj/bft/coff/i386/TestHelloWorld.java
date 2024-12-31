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
package net.boricj.bft.coff.i386;

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
import net.boricj.bft.coff.machines.i386.CoffRelocationType_i386;
import net.boricj.bft.coff.sections.CoffBytes;

public class TestHelloWorld {
	public final byte[] DRECTVE_BYTES =
			"   /DEFAULTLIB:\"LIBCMT\" /DEFAULTLIB:\"OLDNAMES\" ".getBytes(StandardCharsets.US_ASCII);
	public final byte[] DEBUG_S_BYTES = HexFormat.of()
			.parseHex(
					"04000000F1000000D00000009200011100000000433A5C55736572735C6A626C62655C446F63756D656E74735C576F726B73706163655C62696E6172792D66696C652D746F6F6C6B69745C6266742D636F66665C7372635C746573745C7265736F75726365735C6E65745C626F7269636A5C6266745C636F66665C68656C6C6F2D776F726C645F693638362D70632D77696E646F77732D6D7376632E6F626A003A003C11002200000700130028001384000013002800138400004D6963726F736F667420285229204F7074696D697A696E6720436F6D70696C657200");
	public final byte[] TEXT_MN_BYTES = HexFormat.of().parseHex("558BEC6800000000E80000000083C40433C05DC3");
	public final byte[] DATA_BYTES = "Hello, world!\0".getBytes(StandardCharsets.US_ASCII);
	public final byte[] CHKS64_BYTES =
			HexFormat.of().parseHex("23076615271ABF1A5F48E463832D80D0A704EFCBEDB25F465450BB49D375BA930000000000000000");

	public final String _DRECTVE = ".drectve";
	public final String _DEBUG_S = ".debug$S";
	public final String _TEXT_MN = ".text$mn";
	public final String _DATA = ".data";
	public final String _CHKS64 = ".chks64";

	@Test
	public void test_hello_world_i686_pc_windows_msvc() throws IOException {
		CoffFile coff = new CoffFile.Builder(CoffMachine.IMAGE_FILE_MACHINE_I386)
				.setTimeDateStamp(0x676FD082)
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
		characteristics = new CoffSectionFlags()
				.cntInitializedData()
				.alignBytes(4)
				.memRead()
				.memWrite();
		CoffSection data = new CoffBytes(coff, _DATA, characteristics, DATA_BYTES);
		sectionTable.add(data);
		characteristics = new CoffSectionFlags().lnkInfo().lnkRemove();
		CoffSection chks64 = new CoffBytes(coff, _CHKS64, characteristics, CHKS64_BYTES);
		sectionTable.add(chks64);

		symbolTable.addAbsolute("@comp.id", 0x01048413);
		symbolTable.addAbsolute("@feat.00", 0x80010191);
		symbolTable.addAbsolute("@vol.md", 0x00000003);
		symbolTable.addSection(drectve);
		symbolTable.addSection(debug_s);
		symbolTable.addSection(text_mn);
		CoffSymbol _puts = symbolTable.addUndefined("_puts");
		symbolTable.addSymbol("_main", 0, text_mn, (byte) 0x20, CoffStorageClass.IMAGE_SYM_CLASS_EXTERNAL);
		symbolTable.addSection(data);
		CoffSymbol _SG7446 =
				symbolTable.addSymbol("$SG7446", 0, data, (byte) 0x00, CoffStorageClass.IMAGE_SYM_CLASS_STATIC);
		symbolTable.addSection(chks64);

		CoffRelocationTable text_mn_rel = text_mn.getRelocations();
		text_mn_rel.add(0x4, _SG7446, CoffRelocationType_i386.IMAGE_REL_I386_DIR32);
		text_mn_rel.add(0x9, _puts, CoffRelocationType_i386.IMAGE_REL_I386_REL32);

		drectve.setOffset(0x0dc);
		debug_s.setOffset(0x10b);
		text_mn.setOffset(0x1e7);
		text_mn_rel.setOffset(0x1fb);
		data.setOffset(0x20f);
		chks64.setOffset(0x21d);
		symbolTable.setOffset(0x245);

		Stream<Writable> streamWritables = Stream.of(
						List.of(header, sectionTable, symbolTable, stringTable).stream(),
						sectionTable.stream(),
						sectionTable.stream().map(s -> s.getRelocations()))
				.flatMap(Function.identity());
		Collection<Writable> writables = streamWritables.collect(Collectors.toList());

		final Map<Integer, byte[]> patches = Map.ofEntries(
				Map.entry(0x2dd, new byte[] {(byte) 0x1C, (byte) 0xD9, (byte) 0x29, (byte) 0xF8
				}), // .text$mn section symbol checksum
				Map.entry(0x325, new byte[] {(byte) 0xC1, (byte) 0x37, (byte) 0x3B, (byte) 0x4A
				}) // .data section symbol checksum
				);

		TestUtils.compare(getClass().getResourceAsStream("hello-world_i686-pc-windows-msvc.obj"), writables, patches);
	}
}
