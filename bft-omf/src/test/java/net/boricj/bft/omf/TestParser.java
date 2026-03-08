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

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.boricj.bft.omf.coments.OmfComentCompiler;
import net.boricj.bft.omf.coments.OmfComentLibrarySearch;
import net.boricj.bft.omf.coments.OmfComentTranslator;
import net.boricj.bft.omf.constants.OmfComentClass;
import net.boricj.bft.omf.constants.OmfRecordType;
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestParser {
	@Test
	public void test_hello_world_i386_pc_windows_omf() throws Exception {
		File file = new File(
				getClass().getResource("hello-world_i386-pc-windows-omf.obj").toURI());

		OmfFile omf;
		try (FileInputStream fis = new FileInputStream(file)) {
			omf = new OmfFile.Parser(fis).parse();
		}

		Iterator<OmfRecord> recordsIt = omf.iterator();
		OmfRecord record;

		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.THEADR, record.getType());

			OmfRecordTheadr theadr = (OmfRecordTheadr) record;
			assertEquals(".\\hello-world.c", theadr.getModuleName());
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.COMENT, record.getType());

			OmfRecordComent coment = (OmfRecordComent) record;
			assertFalse(coment.isPurge());
			assertTrue(coment.isList());
			assertEquals(OmfComentClass.LIBRARY_SEARCH, coment.getCommentClass());
			OmfComentLibrarySearch librarySearch = (OmfComentLibrarySearch) coment.getComment();
			assertEquals(0L, librarySearch.getTimestamp());
			assertArrayEquals(new byte[] {0, 0}, librarySearch.getAdditionalData());
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.COMENT, record.getType());

			OmfRecordComent coment = (OmfRecordComent) record;
			assertFalse(coment.isPurge());
			assertFalse(coment.isList());
			assertEquals(OmfComentClass.TRANSLATOR, coment.getCommentClass());
			OmfComentTranslator translator = (OmfComentTranslator) coment.getComment();
			assertTrue(translator.getTranslatorString().contains("Turbo Assembler"));
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.COMENT, record.getType());

			OmfRecordComent coment = (OmfRecordComent) record;
			assertFalse(coment.isPurge());
			assertTrue(coment.isList());
			assertEquals(OmfComentClass.COMPILER, coment.getCommentClass());
			OmfComentCompiler compiler = (OmfComentCompiler) coment.getComment();
			assertFalse(compiler.isEmpty());
			assertEquals(".\\hello-world.c", compiler.getFilename());
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.COMENT, record.getType());

			OmfRecordComent coment = (OmfRecordComent) record;
			assertFalse(coment.isPurge());
			assertTrue(coment.isList());
			assertEquals(OmfComentClass.COMPILER, coment.getCommentClass());
			OmfComentCompiler compiler = (OmfComentCompiler) coment.getComment();
			assertTrue(compiler.isEmpty());
		}

		List<String> lnames = new ArrayList<>();
		List<String> segmentNamesByIndex = new ArrayList<>();
		segmentNamesByIndex.add(null);

		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.LNAMES, record.getType());

			OmfRecordLnames lnamesRecord = (OmfRecordLnames) record;
			List<String> names = lnamesRecord.getNames();
			assertEquals(List.of(""), names);
			lnames.addAll(names);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.COMENT, record.getType());

			OmfRecordComent coment = (OmfRecordComent) record;
			assertFalse(coment.isPurge());
			assertTrue(coment.isList());
			assertEquals(OmfComentClass.WEAK_EXTERNALS, coment.getCommentClass());
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.LNAMES, record.getType());

			OmfRecordLnames lnamesRecord = (OmfRecordLnames) record;
			List<String> names = lnamesRecord.getNames();
			assertEquals(List.of("_TEXT", "CODE"), names);
			lnames.addAll(names);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.SEGDEF, record.getType());

			OmfRecordSegdef segdef = (OmfRecordSegdef) record;
			String segmentName = segdef.getSegmentName();
			String className = segdef.getClassName();
			assertEquals("_TEXT", segmentName);
			assertEquals("CODE", className);
			assertEquals(0x12L, segdef.getSegmentLength());
			segmentNamesByIndex.add(segmentName);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.LNAMES, record.getType());

			OmfRecordLnames lnamesRecord = (OmfRecordLnames) record;
			List<String> names = lnamesRecord.getNames();
			assertEquals(List.of("_DATA", "DATA"), names);
			lnames.addAll(names);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.SEGDEF, record.getType());

			OmfRecordSegdef segdef = (OmfRecordSegdef) record;
			String segmentName = segdef.getSegmentName();
			String className = segdef.getClassName();
			assertEquals("_DATA", segmentName);
			assertEquals("DATA", className);
			assertEquals(0x10L, segdef.getSegmentLength());
			segmentNamesByIndex.add(segmentName);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.LNAMES, record.getType());

			OmfRecordLnames lnamesRecord = (OmfRecordLnames) record;
			List<String> names = lnamesRecord.getNames();
			assertEquals(List.of("_BSS", "BSS"), names);
			lnames.addAll(names);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.SEGDEF, record.getType());

			OmfRecordSegdef segdef = (OmfRecordSegdef) record;
			String segmentName = segdef.getSegmentName();
			String className = segdef.getClassName();
			assertEquals("_BSS", segmentName);
			assertEquals("BSS", className);
			assertEquals(0x0L, segdef.getSegmentLength());
			segmentNamesByIndex.add(segmentName);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.LNAMES, record.getType());

			OmfRecordLnames lnamesRecord = (OmfRecordLnames) record;
			List<String> names = lnamesRecord.getNames();
			assertEquals(List.of("$$BSYMS", "DEBSYM"), names);
			lnames.addAll(names);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.SEGDEF, record.getType());

			OmfRecordSegdef segdef = (OmfRecordSegdef) record;
			String segmentName = segdef.getSegmentName();
			String className = segdef.getClassName();
			assertEquals("$$BSYMS", segmentName);
			assertEquals("DEBSYM", className);
			assertEquals(0x78L, segdef.getSegmentLength());
			segmentNamesByIndex.add(segmentName);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.LNAMES, record.getType());

			OmfRecordLnames lnamesRecord = (OmfRecordLnames) record;
			List<String> names = lnamesRecord.getNames();
			assertEquals(List.of("$$BTYPES", "DEBTYP"), names);
			lnames.addAll(names);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.SEGDEF, record.getType());

			OmfRecordSegdef segdef = (OmfRecordSegdef) record;
			String segmentName = segdef.getSegmentName();
			String className = segdef.getClassName();
			assertEquals("$$BTYPES", segmentName);
			assertEquals("DEBTYP", className);
			assertEquals(0x70L, segdef.getSegmentLength());
			segmentNamesByIndex.add(segmentName);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.LNAMES, record.getType());

			OmfRecordLnames lnamesRecord = (OmfRecordLnames) record;
			List<String> names = lnamesRecord.getNames();
			assertEquals(List.of("$$BNAMES", "DEBNAM"), names);
			lnames.addAll(names);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.SEGDEF, record.getType());

			OmfRecordSegdef segdef = (OmfRecordSegdef) record;
			String segmentName = segdef.getSegmentName();
			String className = segdef.getClassName();
			assertEquals("$$BNAMES", segmentName);
			assertEquals("DEBNAM", className);
			assertEquals(0x0FL, segdef.getSegmentLength());
			segmentNamesByIndex.add(segmentName);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.LNAMES, record.getType());

			OmfRecordLnames lnamesRecord = (OmfRecordLnames) record;
			List<String> names = lnamesRecord.getNames();
			assertEquals(List.of("$$BROWSE", "DEBSYM"), names);
			lnames.addAll(names);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.SEGDEF, record.getType());

			OmfRecordSegdef segdef = (OmfRecordSegdef) record;
			String segmentName = segdef.getSegmentName();
			String className = segdef.getClassName();
			assertEquals("$$BROWSE", segmentName);
			assertEquals("DEBSYM", className);
			assertEquals(0x0L, segdef.getSegmentLength());
			segmentNamesByIndex.add(segmentName);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.LNAMES, record.getType());

			OmfRecordLnames lnamesRecord = (OmfRecordLnames) record;
			List<String> names = lnamesRecord.getNames();
			assertEquals(List.of("$$BROWFILE", "DEBSYM"), names);
			lnames.addAll(names);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.SEGDEF, record.getType());

			OmfRecordSegdef segdef = (OmfRecordSegdef) record;
			String segmentName = segdef.getSegmentName();
			String className = segdef.getClassName();
			assertEquals("$$BROWFILE", segmentName);
			assertEquals("DEBSYM", className);
			assertEquals(0x0L, segdef.getSegmentLength());
			segmentNamesByIndex.add(segmentName);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.LNAMES, record.getType());

			OmfRecordLnames lnamesRecord = (OmfRecordLnames) record;
			List<String> names = lnamesRecord.getNames();
			assertEquals(List.of("FLAT"), names);
			lnames.addAll(names);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.GRPDEF, record.getType());

			OmfRecordGrpdef grpdef = (OmfRecordGrpdef) record;
			assertEquals("FLAT", grpdef.getGroupName());
			List<String> actualSegmentNames = new ArrayList<>();
			for (OmfRecordSegdef segment : grpdef.getSegments()) {
				actualSegmentNames.add(segment.getSegmentName());
			}
			assertEquals(List.of(), actualSegmentNames);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.LNAMES, record.getType());

			OmfRecordLnames lnamesRecord = (OmfRecordLnames) record;
			List<String> names = lnamesRecord.getNames();
			assertEquals(List.of("DGROUP"), names);
			lnames.addAll(names);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.GRPDEF, record.getType());

			OmfRecordGrpdef grpdef = (OmfRecordGrpdef) record;
			assertEquals("DGROUP", grpdef.getGroupName());
			List<String> actualSegmentNames = new ArrayList<>();
			for (OmfRecordSegdef segment : grpdef.getSegments()) {
				actualSegmentNames.add(segment.getSegmentName());
			}
			assertEquals(List.of("_BSS", "_DATA"), actualSegmentNames);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.EXTDEF, record.getType());

			OmfRecordExtdef extdef = (OmfRecordExtdef) record;
			List<String> names = extdef.getNames();
			assertEquals(List.of("__setargv__", "_puts"), names);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.PUBDEF, record.getType());

			OmfRecordPubdef pubdef = (OmfRecordPubdef) record;
			assertEquals("_TEXT", pubdef.getSegment().getSegmentName());
			assertEquals(1, pubdef.getSymbols().size());
			var symbol = pubdef.getSymbols().get(0);
			assertEquals("_main", symbol.name());
			assertEquals(0x0L, symbol.offset());
			assertEquals(0, symbol.typeIndex());
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.LEDATA, record.getType());

			OmfRecordLedata ledata = (OmfRecordLedata) record;
			assertEquals("_TEXT", ledata.getSegment().getSegmentName());
			assertEquals(0x0L, ledata.getDataOffset());
			assertArrayEquals(HexFormat.of().parseHex("558BEC6800000000E8000000005933C05DC3"), ledata.getData());
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.FIXUPP, record.getType());

			OmfRecordFixupp fixupp = (OmfRecordFixupp) record;
			List<Integer> foundOffsets = fixupp.getFixupOffsets();
			assertEquals(List.of(0x004, 0x009), foundOffsets);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.LEDATA, record.getType());

			OmfRecordLedata ledata = (OmfRecordLedata) record;
			assertEquals("_DATA", ledata.getSegment().getSegmentName());
			assertEquals(0x0L, ledata.getDataOffset());
			assertArrayEquals("Hello, world!\0".getBytes(StandardCharsets.US_ASCII), ledata.getData());
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.LIDATA, record.getType());

			OmfRecordLidata lidata = (OmfRecordLidata) record;
			assertEquals("_DATA", lidata.getSegment().getSegmentName());
			assertEquals(0x0EL, lidata.getDataOffset());
			assertTrue(lidata.getEncodedData().length > 0);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.LEDATA, record.getType());

			OmfRecordLedata ledata = (OmfRecordLedata) record;
			assertEquals("$$BSYMS", ledata.getSegment().getSegmentName());
			assertEquals(0x0L, ledata.getDataOffset());
			assertArrayEquals(
					HexFormat.of()
							.parseHex("0200000034000502000000000000000000000000120000000000000010000000"
									+ "0000000000000000001000000100000000000000055F6D61696E120000020800"
									+ "0000740000000200000000000000120000020C00000001100000030000000000"
									+ "000002000600100001000300001809424343333220352E36"),
					ledata.getData());
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.FIXUPP, record.getType());

			OmfRecordFixupp fixupp = (OmfRecordFixupp) record;
			List<Integer> foundOffsets = fixupp.getFixupOffsets();
			assertEquals(List.of(0x020), foundOffsets);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.LEDATA, record.getType());

			OmfRecordLedata ledata = (OmfRecordLedata) record;
			assertEquals("$$BTYPES", ledata.getSegment().getSegmentName());
			assertEquals(0x0L, ledata.getDataOffset());
			assertArrayEquals(
					HexFormat.of()
							.parseHex("020000000E000800740000000000020002100000080002000A00100400000C00"
									+ "0102020074000000011000000E00080074000000020000000410000004000102"
									+ "00000E000800740000000000010008100000080002000A000710000008000100"
									+ "01001000000008000102010006100000"),
					ledata.getData());
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.LEDATA, record.getType());

			OmfRecordLedata ledata = (OmfRecordLedata) record;
			assertEquals("$$BNAMES", ledata.getSegment().getSegmentName());
			assertEquals(0x0L, ledata.getDataOffset());
			assertArrayEquals(HexFormat.of().parseHex("046D61696E04617267630461726776"), ledata.getData());
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.LINNUM, record.getType());

			OmfRecordLinnum linnum = (OmfRecordLinnum) record;
			assertEquals("_TEXT", linnum.getSegment().getSegmentName());
			List<Integer> lineNumbers = new ArrayList<>();
			List<Long> offsets = new ArrayList<>();
			for (var ln : linnum.getLineNumbers()) {
				lineNumbers.add(ln.lineNumber());
				offsets.add(ln.offset());
			}
			assertEquals(List.of(3, 4, 5, 6, 0), lineNumbers);
			assertEquals(List.of(0L, 3L, 0xEL, 0x10L, 0x12L), offsets);
		}
		{
			record = recordsIt.next();
			assertEquals(OmfRecordType.MODEND, record.getType());

			OmfRecordModend modend = (OmfRecordModend) record;
			assertFalse(modend.isMainModule());
			assertFalse(modend.hasStartAddress());
			assertEquals(0, modend.getStartAddressFixup().length);
		}

		assertFalse(recordsIt.hasNext());
	}
}
