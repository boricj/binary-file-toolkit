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
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.boricj.bft.omf.OmfSegmentData.FixupAtOffset;
import net.boricj.bft.omf.records.OmfRecordFixupp;
import net.boricj.bft.omf.records.OmfRecordFixupp.FixupEntry;
import net.boricj.bft.omf.records.OmfRecordLedata;
import net.boricj.bft.omf.records.OmfRecordLidata;
import net.boricj.bft.omf.records.OmfRecordSegdef;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSegmentData {
	@Test
	public void test_parse_reconstructs_bytes_and_fixups() {
		OmfFile omf = new OmfFile.Builder().build();
		OmfRecordSegdef segment = new OmfRecordSegdef(omf, 0x69, 0x1000, "_DATA", "DATA", "");
		omf.add(segment);

		omf.add(new OmfRecordLedata(omf, segment, 0, new byte[] {1, 2, 3, 4}));
		omf.add(new OmfRecordFixupp(
				omf,
				List.of(new FixupEntry(
						0,
						0x9,
						true,
						OmfRecordFixupp.FrameMethod.GRPDEF_INDEX,
						OmfRecordFixupp.TargetMethod.SEGDEF_INDEX,
						1,
						1,
						OmfRecordFixupp.DisplacementType.D32))));
		omf.add(new OmfRecordLedata(omf, segment, 4, new byte[] {5, 6}));

		OmfSegmentData parsed = OmfSegmentData.parse(omf, segment);
		assertArrayEquals(new byte[] {1, 2, 3, 4, 5, 6}, parsed.getBytes());
		assertEquals(1, parsed.getFixups().size());
		assertEquals(0, parsed.getFixups().get(0).segmentOffset());
	}

	@Test
	public void test_parse_rejects_non_contiguous_ledata_offsets() {
		OmfFile omf = new OmfFile.Builder().build();
		OmfRecordSegdef segment = new OmfRecordSegdef(omf, 0x69, 0x1000, "_DATA", "DATA", "");
		omf.add(segment);
		omf.add(new OmfRecordLedata(omf, segment, 0, new byte[] {1, 2}));
		omf.add(new OmfRecordLedata(omf, segment, 3, new byte[] {3}));

		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> OmfSegmentData.parse(omf, segment));
		assertTrue(ex.getMessage().contains("segment _DATA"));
		assertTrue(ex.getMessage().contains("expectedStart=2"));
		assertTrue(ex.getMessage().contains("actualStart=3"));
	}

	@Test
	public void test_parse_strict_alias_matches_parse_behavior() {
		OmfFile omf = new OmfFile.Builder().build();
		OmfRecordSegdef segment = new OmfRecordSegdef(omf, 0x69, 0x1000, "_DATA", "DATA", "");
		omf.add(segment);
		omf.add(new OmfRecordLedata(omf, segment, 0, new byte[] {1, 2, 3}));

		OmfSegmentData viaParse = OmfSegmentData.parse(omf, segment);
		OmfSegmentData viaStrict = OmfSegmentData.parseStrict(omf, segment);
		assertArrayEquals(viaParse.getBytes(), viaStrict.getBytes());
		assertEquals(viaParse.getFixups(), viaStrict.getFixups());
	}

	@Test
	public void test_emit_chunks_and_roundtrips_fixup_offsets() {
		OmfFile destination = new OmfFile.Builder().build();
		OmfRecordSegdef destinationSegment = new OmfRecordSegdef(destination, 0x69, 0x2000, "_DATA", "DATA", "");
		destination.add(destinationSegment);

		byte[] bytes = new byte[20];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) (i + 1);
		}
		List<FixupAtOffset> fixups = List.of(
				new FixupAtOffset(
						1,
						new FixupEntry(
								0,
								0x9,
								true,
								OmfRecordFixupp.FrameMethod.GRPDEF_INDEX,
								OmfRecordFixupp.TargetMethod.SEGDEF_INDEX,
								1,
								1,
								OmfRecordFixupp.DisplacementType.D32)),
				new FixupAtOffset(
						10,
						new FixupEntry(
								0,
								0x9,
								true,
								OmfRecordFixupp.FrameMethod.GRPDEF_INDEX,
								OmfRecordFixupp.TargetMethod.SEGDEF_INDEX,
								1,
								1,
								OmfRecordFixupp.DisplacementType.D32)),
				new FixupAtOffset(
						16,
						new FixupEntry(
								0,
								0x9,
								true,
								OmfRecordFixupp.FrameMethod.GRPDEF_INDEX,
								OmfRecordFixupp.TargetMethod.SEGDEF_INDEX,
								1,
								1,
								OmfRecordFixupp.DisplacementType.D32)));
		OmfSegmentData data = new OmfSegmentData(destinationSegment, bytes, fixups);

		OmfChunkingPolicy policy = OmfChunkingPolicy.forMaxRecordSize(24);
		data.emit(destination, policy);

		List<OmfRecordLedata> ledatas = new ArrayList<>();
		for (OmfRecord record : destination.getElements()) {
			if (record instanceof OmfRecordLedata ledata && ledata.getSegment() == destinationSegment) {
				ledatas.add(ledata);
			}
		}
		assertTrue(ledatas.size() > 1, "Expected chunking to produce multiple LEDATA records");
		for (OmfRecordLedata ledata : ledatas) {
			assertTrue(ledata.getLength() <= policy.maxRecordSize());
		}

		OmfSegmentData reparsed = OmfSegmentData.parse(destination, destinationSegment);
		List<Integer> reparsedOffsets =
				reparsed.getFixups().stream().map(FixupAtOffset::segmentOffset).toList();
		assertEquals(List.of(1, 10, 16), reparsedOffsets);
	}

	@Test
	public void test_emit_rejects_unemittable_fixup_range_with_no_forward_progress() {
		OmfFile destination = new OmfFile.Builder().build();
		OmfRecordSegdef destinationSegment = new OmfRecordSegdef(destination, 0x69, 4, "_DATA", "DATA", "");
		destination.add(destinationSegment);

		OmfSegmentData data = new OmfSegmentData(
				destinationSegment,
				new byte[] {1, 2, 3, 4},
				List.of(new FixupAtOffset(
						1,
						new FixupEntry(
								0,
								0x9,
								true,
								OmfRecordFixupp.FrameMethod.GRPDEF_INDEX,
								OmfRecordFixupp.TargetMethod.SEGDEF_INDEX,
								1,
								1,
								OmfRecordFixupp.DisplacementType.D32))));

		OmfRecordSegdef tooSmallSegment = new OmfRecordSegdef(destination, 0x69, 3, "_SMALL", "DATA", "");
		destination.add(tooSmallSegment);

		assertThrows(IllegalStateException.class, () -> data.emit(destination, OmfChunkingPolicy.forMaxRecordSize(64)));
	}

	@Test
	public void test_parse_lidata_between_ledatas_reconstructs_contiguous_bytes() {
		OmfFile omf = new OmfFile.Builder().build();
		OmfRecordSegdef segment = new OmfRecordSegdef(omf, 0x69, 16, "_DATA", "DATA", "");
		omf.add(segment);

		omf.add(new OmfRecordLedata(omf, segment, 0, new byte[] {(byte) 0x41}));
		omf.add(new OmfRecordLidata(omf, segment, 1, new byte[] {
			0x01, 0x00, 0x02, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x00
		}));
		omf.add(new OmfRecordLedata(omf, segment, 3, new byte[] {(byte) 0x42}));

		OmfSegmentData parsed = OmfSegmentData.parse(omf, segment);
		assertArrayEquals(new byte[] {(byte) 0x41, 0x00, 0x00, (byte) 0x42}, parsed.getBytes());
	}

	@Test
	public void test_parse_collects_fixupp_offsets_after_lidata_chunk() {
		OmfFile omf = new OmfFile.Builder().build();
		OmfRecordSegdef segment = new OmfRecordSegdef(omf, 0x69, 16, "_DATA", "DATA", "");
		omf.add(segment);

		omf.add(new OmfRecordLidata(
				omf, segment, 0, new byte[] {0x01, 0x00, 0x00, 0x00, 0x04, 0x11, 0x22, 0x33, 0x44}));
		omf.add(new OmfRecordFixupp(
				omf,
				List.of(new FixupEntry(
						0,
						0x9,
						true,
						OmfRecordFixupp.FrameMethod.GRPDEF_INDEX,
						OmfRecordFixupp.TargetMethod.SEGDEF_INDEX,
						1,
						1,
						OmfRecordFixupp.DisplacementType.D32))));

		OmfSegmentData parsed = OmfSegmentData.parse(omf, segment);
		List<Integer> offsets =
				parsed.getFixups().stream().map(FixupAtOffset::segmentOffset).toList();
		assertEquals(List.of(0), offsets);
	}

	@Test
	public void test_parse_rejects_fixup_displacement_straddling_chunk_end() {
		OmfFile omf = new OmfFile.Builder().build();
		OmfRecordSegdef segment = new OmfRecordSegdef(omf, 0x69, 16, "_DATA", "DATA", "");
		omf.add(segment);

		omf.add(new OmfRecordLedata(omf, segment, 0, new byte[] {0x11, 0x22, 0x33, 0x44}));
		omf.add(new OmfRecordFixupp(
				omf,
				List.of(new FixupEntry(
						3,
						0x9,
						true,
						OmfRecordFixupp.FrameMethod.GRPDEF_INDEX,
						OmfRecordFixupp.TargetMethod.SEGDEF_INDEX,
						1,
						1,
						OmfRecordFixupp.DisplacementType.D32))));

		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> OmfSegmentData.parse(omf, segment));
		assertTrue(ex.getMessage().contains("chunkStart=0"));
		assertTrue(ex.getMessage().contains("chunkLength=4"));
		assertTrue(ex.getMessage().contains("relativeOffset=3"));
	}

	@Test
	public void test_parse_hello_world_fixture_data_segment_includes_lidata_bytes() throws Exception {
		File file = new File(
				getClass().getResource("hello-world_i386-pc-windows-omf.obj").toURI());

		OmfFile omf;
		try (FileInputStream fis = new FileInputStream(file)) {
			omf = new OmfFile.Parser(fis).parse();
		}

		OmfRecordSegdef dataSegment = null;
		for (OmfRecord record : omf.getElements()) {
			if (record instanceof OmfRecordSegdef segdef
					&& segdef.getSegmentName().equals("_DATA")) {
				dataSegment = segdef;
				break;
			}
		}
		assertTrue(dataSegment != null, "Expected _DATA segment in fixture");

		OmfSegmentData parsed = OmfSegmentData.parse(omf, dataSegment);
		assertArrayEquals(
				new byte[] {'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!', 0x00, 0x00, 0x00},
				parsed.getBytes());
	}

	@Test
	public void test_parse_rejects_malformed_lidata_with_segment_context() {
		OmfFile omf = new OmfFile.Builder().build();
		OmfRecordSegdef segment = new OmfRecordSegdef(omf, 0x69, 16, "_DATA", "DATA", "");
		omf.add(segment);

		// repeatCount=1, blockCount=0, payloadLength=4 but only 2 payload bytes present
		omf.add(new OmfRecordLidata(omf, segment, 0, new byte[] {0x01, 0x00, 0x00, 0x00, 0x04, 0x11, 0x22}));

		IllegalStateException ex =
				assertThrows(IllegalStateException.class, () -> OmfSegmentData.parseStrict(omf, segment));
		assertTrue(ex.getMessage().contains("Malformed LIDATA for segment _DATA"));
		assertTrue(ex.getMessage().contains("dataOffset=0"));
		assertTrue(ex.getMessage().contains("truncated payload bytes"));
	}

	@Test
	public void test_parse_rejects_lidata_with_truncated_repeat_count() {
		OmfFile omf = new OmfFile.Builder().build();
		OmfRecordSegdef segment = new OmfRecordSegdef(omf, 0x69, 16, "_DATA", "DATA", "");
		omf.add(segment);

		// 16-bit LIDATA with only one byte, so repeat count is truncated.
		omf.add(new OmfRecordLidata(omf, segment, 0, new byte[] {0x01}));

		IllegalStateException ex =
				assertThrows(IllegalStateException.class, () -> OmfSegmentData.parseStrict(omf, segment));
		assertTrue(ex.getMessage().contains("Malformed LIDATA for segment _DATA"));
		assertTrue(ex.getMessage().contains("truncated repeat count"));
	}

	@Test
	public void test_parse_rejects_lidata_with_truncated_block_count() {
		OmfFile omf = new OmfFile.Builder().build();
		OmfRecordSegdef segment = new OmfRecordSegdef(omf, 0x69, 16, "_DATA", "DATA", "");
		omf.add(segment);

		// repeatCount=1 then truncated blockCount (missing second byte).
		omf.add(new OmfRecordLidata(omf, segment, 0, new byte[] {0x01, 0x00, 0x00}));

		IllegalStateException ex =
				assertThrows(IllegalStateException.class, () -> OmfSegmentData.parseStrict(omf, segment));
		assertTrue(ex.getMessage().contains("Malformed LIDATA for segment _DATA"));
		assertTrue(ex.getMessage().contains("truncated block count"));
	}
}
