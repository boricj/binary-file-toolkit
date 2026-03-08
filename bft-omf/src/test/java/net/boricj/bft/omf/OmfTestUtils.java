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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.omf.coments.OmfComentCompiler;
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
import net.boricj.bft.omf.records.OmfRecordPubdef;
import net.boricj.bft.omf.records.OmfRecordSegdef;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OmfTestUtils {
	public static void assertRecordTypes(List<OmfRecord> records, List<OmfRecordType> expectedTypes) {
		assertEquals(expectedTypes.size(), records.size());
		for (int i = 0; i < expectedTypes.size(); i++) {
			assertEquals(expectedTypes.get(i), records.get(i).getType(), "Unexpected type at record " + i);
		}
	}

	public static void assertComentClassAndContains(
			OmfRecordComent coment, OmfComentClass expectedClass, String expectedSubstring) {
		assertEquals(expectedClass, coment.getCommentClass());

		try {
			java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
			ByteOutputStream bos = ByteOutputStream.asLittleEndian(baos);
			coment.getComment().write(bos);
			String body = new String(baos.toByteArray(), StandardCharsets.US_ASCII);
			assertTrue(body.contains(expectedSubstring));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void assertComentClass(OmfRecordComent coment, OmfComentClass expectedClass) {
		assertEquals(expectedClass, coment.getCommentClass());
	}

	public static void assertComent(
			OmfRecordComent coment, boolean expectedPurge, boolean expectedList, OmfComentClass expectedClass) {
		assertEquals(expectedPurge, coment.isPurge());
		assertEquals(expectedList, coment.isList());
		assertEquals(expectedClass, coment.getCommentClass());
	}

	public static void assertComentTranslator(
			OmfRecordComent coment, boolean expectedPurge, boolean expectedList, String expectedSubstring) {
		assertComent(coment, expectedPurge, expectedList, OmfComentClass.TRANSLATOR);
		OmfComentTranslator translator = (OmfComentTranslator) coment.getComment();
		assertTrue(
				translator.getTranslatorString().contains(expectedSubstring),
				"Expected translator string to contain '" + expectedSubstring + "' but was: '"
						+ translator.getTranslatorString() + "'");
	}

	public static void assertComentCompilerEndMarker(
			OmfRecordComent coment, boolean expectedPurge, boolean expectedList) {
		assertComent(coment, expectedPurge, expectedList, OmfComentClass.COMPILER);
		OmfComentCompiler compiler = (OmfComentCompiler) coment.getComment();
		assertTrue(compiler.isEmpty(), "Expected COMPILER comment to be an end marker");
	}

	public static void assertComentCompilerDependency(
			OmfRecordComent coment, boolean expectedPurge, boolean expectedList, String expectedFilename) {
		assertComent(coment, expectedPurge, expectedList, OmfComentClass.COMPILER);
		OmfComentCompiler compiler = (OmfComentCompiler) coment.getComment();
		assertFalse(compiler.isEmpty(), "Expected COMPILER comment to be a dependency, not an end marker");
		assertEquals(expectedFilename, compiler.getFilename());
	}

	public static void assertLnames(OmfRecordLnames record, List<String> lnames, String... expectedNewNames) {
		List<String> names = record.getNames();
		assertEquals(List.of(expectedNewNames), names);
		lnames.addAll(names);
	}

	public static void assertSegdef(
			OmfRecordSegdef record,
			List<String> lnames,
			List<String> segmentNamesByIndex,
			String expectedSegmentName,
			String expectedClassName,
			long expectedLength) {
		String segmentName = record.getSegmentName();
		String className = record.getClassName();

		assertEquals(expectedSegmentName, segmentName);
		assertEquals(expectedClassName, className);
		assertEquals(expectedLength, record.getSegmentLength());
		segmentNamesByIndex.add(segmentName);
	}

	public static void assertGrpdef(
			OmfRecordGrpdef record,
			List<String> lnames,
			List<String> segmentNamesByIndex,
			String expectedGroupName,
			List<String> expectedSegmentNames) {
		assertEquals(expectedGroupName, record.getGroupName());

		List<String> actualSegmentNames = new ArrayList<>();
		for (OmfRecordSegdef segment : record.getSegments()) {
			actualSegmentNames.add(segment.getSegmentName());
		}
		assertEquals(expectedSegmentNames, actualSegmentNames);
	}

	public static void assertExtdef(OmfRecordExtdef record, String... expectedExternalNames) {
		List<String> names = record.getNames();
		assertEquals(List.of(expectedExternalNames), names);
	}

	public static void assertPubdef(
			OmfRecordPubdef record,
			List<String> segmentNamesByIndex,
			String expectedSegmentName,
			String expectedSymbol,
			long expectedOffset) {
		assertEquals(expectedSegmentName, record.getSegment().getSegmentName());
		assertEquals(1, record.getSymbols().size());

		var symbol = record.getSymbols().get(0);
		assertEquals(expectedSymbol, symbol.name());
		assertEquals(expectedOffset, symbol.offset());
		assertEquals(0, symbol.typeIndex());
	}

	public static void assertLedata(
			OmfRecordLedata record,
			List<String> segmentNamesByIndex,
			String expectedSegmentName,
			long expectedOffset,
			byte[] expectedBytes) {
		assertEquals(expectedSegmentName, record.getSegment().getSegmentName());
		assertEquals(expectedOffset, record.getDataOffset());
		assertArrayEquals(expectedBytes, record.getData());
	}

	public static void assertLidata(
			OmfRecordLidata record, List<String> segmentNamesByIndex, String expectedSegmentName, long expectedOffset) {
		assertEquals(expectedSegmentName, record.getSegment().getSegmentName());
		assertEquals(expectedOffset, record.getDataOffset());
		assertTrue(record.getEncodedData().length > 0);
	}

	public static void assertFixuppContainsOffsets(OmfRecordFixupp record, List<Integer> expectedOffsets) {
		List<Integer> foundOffsets = record.getFixupOffsets();
		assertEquals(expectedOffsets, foundOffsets);
	}

	public static void assertLinnum(
			OmfRecordLinnum record,
			List<String> segmentNamesByIndex,
			String expectedSegmentName,
			List<Integer> expectedLineNumbers,
			List<Long> expectedOffsets) {
		assertEquals(expectedSegmentName, record.getSegment().getSegmentName());

		List<Integer> lineNumbers = new ArrayList<>();
		List<Long> offsets = new ArrayList<>();
		for (var ln : record.getLineNumbers()) {
			lineNumbers.add(ln.lineNumber());
			offsets.add(ln.offset());
		}

		assertEquals(expectedLineNumbers, lineNumbers);
		assertEquals(expectedOffsets, offsets);
	}

	public static String lname(List<String> lnames, int index) {
		assertTrue(index > 0 && index <= lnames.size());
		return lnames.get(index - 1);
	}
}
