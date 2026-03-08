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
package net.boricj.bft.omf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.boricj.bft.TestUtils;
import net.boricj.bft.omf.coments.OmfComentCompiler;
import net.boricj.bft.omf.coments.OmfComentLibrarySearch;
import net.boricj.bft.omf.coments.OmfComentTranslator;
import net.boricj.bft.omf.coments.OmfComentWeakExternals;
import net.boricj.bft.omf.records.OmfRecordComent;
import net.boricj.bft.omf.records.OmfRecordExtdef;
import net.boricj.bft.omf.records.OmfRecordFixupp;
import net.boricj.bft.omf.records.OmfRecordGrpdef;
import net.boricj.bft.omf.records.OmfRecordLedata;
import net.boricj.bft.omf.records.OmfRecordLidata;
import net.boricj.bft.omf.records.OmfRecordLinnum;
import net.boricj.bft.omf.records.OmfRecordLnames;
import net.boricj.bft.omf.records.OmfRecordModend;
import net.boricj.bft.omf.records.OmfRecordPubdef;
import net.boricj.bft.omf.records.OmfRecordSegdef;
import net.boricj.bft.omf.records.OmfRecordTheadr;

public class TestWriter {
	@Test
	public void test_hello_world_i386_pc_windows_omf() throws IOException, URISyntaxException {
		// Create the OMF file programmatically, record by record
		OmfFile omf = new OmfFile.Builder().build();

		omf.add(new OmfRecordTheadr(omf, ".\\hello-world.c"));
		omf.add(new OmfRecordComent(omf, false, true, new OmfComentLibrarySearch(0L, new byte[] {0, 0})));
		omf.add(new OmfRecordComent(
				omf, false, false, new OmfComentTranslator((byte) 0x1C, "Turbo Assembler  Version 5.3")));
		omf.add(new OmfRecordComent(omf, false, true, new OmfComentCompiler(0x599D98F6L, ".\\hello-world.c")));
		omf.add(new OmfRecordComent(omf, false, true, new OmfComentCompiler()));

		omf.add(new OmfRecordLnames(omf, List.of("")));
		omf.add(new OmfRecordComent(omf, false, true, new OmfComentWeakExternals()));
		omf.add(new OmfRecordLnames(omf, List.of("_TEXT", "CODE")));
		OmfRecordSegdef segText =
				new OmfRecordSegdef(omf, OmfRecordSegdef.Attributes.ofRaw(0xA9), 0x12L, "_TEXT", "CODE", "");
		omf.add(segText);
		omf.add(new OmfRecordLnames(omf, List.of("_DATA", "DATA")));
		OmfRecordSegdef segData =
				new OmfRecordSegdef(omf, OmfRecordSegdef.Attributes.ofRaw(0xA9), 0x10L, "_DATA", "DATA", "");
		omf.add(segData);
		omf.add(new OmfRecordLnames(omf, List.of("_BSS", "BSS")));
		OmfRecordSegdef segBss =
				new OmfRecordSegdef(omf, OmfRecordSegdef.Attributes.ofRaw(0xA9), 0x00L, "_BSS", "BSS", "");
		omf.add(segBss);
		omf.add(new OmfRecordLnames(omf, List.of("$$BSYMS", "DEBSYM")));
		OmfRecordSegdef segBsyms =
				new OmfRecordSegdef(omf, OmfRecordSegdef.Attributes.ofRaw(0x29), 0x78L, "$$BSYMS", "DEBSYM", "");
		omf.add(segBsyms);
		omf.add(new OmfRecordLnames(omf, List.of("$$BTYPES", "DEBTYP")));
		OmfRecordSegdef segBtypes =
				new OmfRecordSegdef(omf, OmfRecordSegdef.Attributes.ofRaw(0x29), 0x70L, "$$BTYPES", "DEBTYP", "");
		omf.add(segBtypes);
		omf.add(new OmfRecordLnames(omf, List.of("$$BNAMES", "DEBNAM")));
		OmfRecordSegdef segBnames =
				new OmfRecordSegdef(omf, OmfRecordSegdef.Attributes.ofRaw(0x29), 0x0FL, "$$BNAMES", "DEBNAM", "");
		omf.add(segBnames);
		omf.add(new OmfRecordLnames(omf, List.of("$$BROWSE", "DEBSYM")));
		OmfRecordSegdef segBrowse =
				new OmfRecordSegdef(omf, OmfRecordSegdef.Attributes.ofRaw(0x29), 0x00L, "$$BROWSE", "DEBSYM", "");
		omf.add(segBrowse);
		omf.add(new OmfRecordLnames(omf, List.of("$$BROWFILE", "DEBSYM")));
		OmfRecordSegdef segBrowfile =
				new OmfRecordSegdef(omf, OmfRecordSegdef.Attributes.ofRaw(0x29), 0x00L, "$$BROWFILE", "DEBSYM", "");
		omf.add(segBrowfile);
		omf.add(new OmfRecordLnames(omf, List.of("FLAT")));
		OmfRecordGrpdef grpFlat = new OmfRecordGrpdef(omf, "FLAT", List.of());
		omf.add(grpFlat);
		omf.add(new OmfRecordLnames(omf, List.of("DGROUP")));
		OmfRecordGrpdef grpDgroup = new OmfRecordGrpdef(omf, "DGROUP", List.of(segBss, segData));
		omf.add(grpDgroup);

		omf.add(new OmfRecordExtdef(omf, List.of("__setargv__", "_puts"), List.of(0, 0)));
		omf.add(new OmfRecordPubdef(
				omf, grpFlat, segText, 0, List.of(new OmfRecordPubdef.PublicSymbol("_main", 0x0000L, 0))));
		omf.add(new OmfRecordLedata(
				omf, segText, 0x0000L, HexFormat.of().parseHex("558BEC6800000000E8000000005933C05DC3")));
		omf.add(new OmfRecordFixupp(
				omf,
				List.of(
						new OmfRecordFixupp.FixupEntry(
								0x004,
								0x9,
								true,
								OmfRecordFixupp.FrameMethod.GRPDEF_INDEX,
								OmfRecordFixupp.TargetMethod.SEGDEF_INDEX,
								1,
								2),
						new OmfRecordFixupp.FixupEntry(
								0x009,
								0x9,
								false,
								OmfRecordFixupp.FrameMethod.GRPDEF_INDEX,
								OmfRecordFixupp.TargetMethod.EXTDEF_INDEX,
								1,
								2)),
				OmfRecordFixupp.SpecificType.FIXUPP_32));
		omf.add(new OmfRecordLedata(omf, segData, 0x0000L, "Hello, world!\0".getBytes(StandardCharsets.US_ASCII)));
		omf.add(new OmfRecordLidata(
				omf, segData, 0x000EL, HexFormat.of().parseHex("01000200010000000100010000000100")));
		omf.add(
				new OmfRecordLedata(
						omf,
						segBsyms,
						0x0000L,
						HexFormat.of()
								.parseHex(
										"020000003400050200000000000000000000000012000000000000001000000000000000000000000010000001"
												+ "00000000000000055F6D61696E1200000208000000740000000200000000000000120000020C00000001100000030000000000000002000600100001000300001809424343333220352E36")));
		omf.add(new OmfRecordFixupp(
				omf,
				List.of(new OmfRecordFixupp.FixupEntry(
						0x020,
						0xB,
						true,
						OmfRecordFixupp.FrameMethod.GRPDEF_INDEX,
						OmfRecordFixupp.TargetMethod.SEGDEF_INDEX,
						1,
						1)),
				OmfRecordFixupp.SpecificType.FIXUPP_32));
		omf.add(
				new OmfRecordLedata(
						omf,
						segBtypes,
						0x0000L,
						HexFormat.of()
								.parseHex(
										"020000000E000800740000000000020002100000080002000A00100400000C000102020074000000011000000E"
												+ "0008007400000002000000041000000400010200000E000800740000000000010008100000080002000A00071000000800010001001000000008000102010006100000")));
		omf.add(new OmfRecordLedata(
				omf, segBnames, 0x0000L, HexFormat.of().parseHex("046D61696E04617267630461726776")));
		omf.add(new OmfRecordLinnum(
				omf,
				grpFlat,
				segText,
				List.of(
						new OmfRecordLinnum.LineNumber(3, 0x0000L),
						new OmfRecordLinnum.LineNumber(4, 0x0003L),
						new OmfRecordLinnum.LineNumber(5, 0x000EL),
						new OmfRecordLinnum.LineNumber(6, 0x0010L),
						new OmfRecordLinnum.LineNumber(0, 0x0012L))));
		omf.add(new OmfRecordModend(omf, false, false, new byte[0]));

		// Write the OMF file to bytes
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		omf.write(baos);
		byte[] actualBytes = baos.toByteArray();

		// Read reference bytes from disk (no parsing of reference file)
		File file = new File(
				getClass().getResource("hello-world_i386-pc-windows-omf.obj").toURI());
		byte[] expectedBytes;
		try (FileInputStream fis = new FileInputStream(file)) {
			expectedBytes = fis.readAllBytes();
		}

		// Compare
		TestUtils.assertArrayEquals(expectedBytes, actualBytes);
	}
}
