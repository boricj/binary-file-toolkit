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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.omf.coments.OmfComentTranslator;
import net.boricj.bft.omf.records.OmfRecordExtdef;
import net.boricj.bft.omf.records.OmfRecordFixupp.DisplacementType;
import net.boricj.bft.omf.records.OmfRecordFixupp.FixupEntry;
import net.boricj.bft.omf.records.OmfRecordFixupp.FrameMethod;
import net.boricj.bft.omf.records.OmfRecordFixupp.TargetMethod;
import net.boricj.bft.omf.records.OmfRecordGrpdef;
import net.boricj.bft.omf.records.OmfRecordModend;
import net.boricj.bft.omf.records.OmfRecordPubdef;
import net.boricj.bft.omf.records.OmfRecordSegdef;
import net.boricj.bft.omf.records.OmfSubrecordExtdef;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestOmfUtils {
	@Test
	public void test_translator_comment_write_uses_length_prefixed_payload() throws IOException {
		String translator = "ghidra-delinker-extension v0.8.1-12-gd675646-dirty";
		OmfComentTranslator coment = new OmfComentTranslator((byte) 0x1C, translator);

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ByteOutputStream bos = ByteOutputStream.asLittleEndian(output);
		coment.write(bos);

		byte[] bytes = output.toByteArray();
		byte[] translatorBytes = translator.getBytes(StandardCharsets.US_ASCII);
		assertEquals(translatorBytes.length, bytes[0] & 0xFF);
		assertEquals(translatorBytes.length + 1, bytes.length);
		for (int i = 0; i < translatorBytes.length; i++) {
			assertEquals(translatorBytes[i] & 0xFF, bytes[i + 1] & 0xFF);
		}
	}

	@Test
	public void test_modend_specific_type_can_emit_mode32() {
		OmfFile omf = new OmfFile.Builder().build();

		OmfRecordModend modend16 = new OmfRecordModend(omf, false, false, new byte[0]);
		assertEquals(0x8A, modend16.getSpecificTypeValue() & 0xFF);

		OmfRecordModend modend32 =
				new OmfRecordModend(omf, false, false, new byte[0], OmfRecordModend.SpecificType.MODEND_32);
		assertEquals(0x8B, modend32.getSpecificTypeValue() & 0xFF);
	}

	private static OmfFile buildTestFile() {
		OmfFile omf = new OmfFile.Builder().build();
		OmfRecordSegdef text =
				new OmfRecordSegdef(omf, OmfRecordSegdef.Attributes.ofRaw(0xA9), 0x1000L, "_TEXT", "CODE", "");
		OmfRecordSegdef data =
				new OmfRecordSegdef(omf, OmfRecordSegdef.Attributes.ofRaw(0xA9), 0x1000L, "_DATA", "DATA", "");
		omf.add(text);
		omf.add(data);

		OmfRecordGrpdef group = new OmfRecordGrpdef(omf, "DGROUP", List.of(data));
		omf.add(group);

		omf.add(new OmfRecordExtdef(
				omf, List.of(new OmfSubrecordExtdef("_printf", 0), new OmfSubrecordExtdef("_memset", 0))));

		omf.add(new OmfRecordPubdef(omf, null, text, 0, List.of(new OmfRecordPubdef.PublicSymbol("_main", 0, 0))));
		omf.add(new OmfRecordPubdef(omf, null, text, 0, List.of(new OmfRecordPubdef.PublicSymbol("_helper", 4, 0))));
		omf.add(new OmfRecordPubdef(omf, group, data, 0, List.of(new OmfRecordPubdef.PublicSymbol("_global", 8, 0))));

		return omf;
	}

	@Test
	public void test_find_segment_by_name_and_index_helpers() {
		OmfFile omf = buildTestFile();

		assertEquals("_DATA", OmfUtils.findSegmentByName(omf, "_DATA").getSegmentName());
		assertEquals("_TEXT", OmfUtils.getSegmentByIndex(omf, 1).getSegmentName());
		assertEquals("DGROUP", OmfUtils.getGroupByIndex(omf, 1).getGroupName());
	}

	@Test
	public void test_find_segment_by_name_throws_for_missing_segment() {
		OmfFile omf = buildTestFile();
		assertThrows(IllegalStateException.class, () -> OmfUtils.findSegmentByName(omf, "MISSING"));
	}

	@Test
	public void test_external_symbol_names_flattens_extdef_order() {
		OmfFile omf = buildTestFile();
		assertEquals(List.of("_printf", "_memset"), OmfUtils.externalSymbolNames(omf));
	}

	@Test
	public void test_pubdef_contexts_groups_symbols_by_context() {
		OmfFile omf = buildTestFile();

		List<OmfUtils.PubdefContextData> contexts = OmfUtils.pubdefContexts(omf);
		assertEquals(2, contexts.size());

		OmfUtils.PubdefContextData textContext = contexts.get(0);
		assertEquals("_TEXT", textContext.segment().getSegmentName());
		assertEquals(
				List.of("_main", "_helper"),
				textContext.symbols().stream()
						.map(OmfRecordPubdef.PublicSymbol::name)
						.toList());

		OmfUtils.PubdefContextData dataContext = contexts.get(1);
		assertEquals("_DATA", dataContext.segment().getSegmentName());
		assertEquals("DGROUP", dataContext.group().getGroupName());
		assertEquals(
				List.of("_global"),
				dataContext.symbols().stream()
						.map(OmfRecordPubdef.PublicSymbol::name)
						.toList());
	}

	@Test
	public void test_omf_utils_collection_results_are_immutable() {
		OmfFile omf = buildTestFile();

		List<String> extdefs = OmfUtils.externalSymbolNames(omf);
		assertThrows(UnsupportedOperationException.class, () -> extdefs.add("_new"));

		List<OmfUtils.PubdefContextData> contexts = OmfUtils.pubdefContexts(omf);
		assertThrows(UnsupportedOperationException.class, () -> contexts.add(contexts.get(0)));
		assertTrue(!contexts.isEmpty());
		assertThrows(
				UnsupportedOperationException.class,
				() -> contexts.get(0).symbols().add(new OmfRecordPubdef.PublicSymbol("_x", 0, 0)));
	}

	@Test
	public void test_resolve_fixup_target_name_supports_extdef_segdef_grpdef_and_frame() {
		OmfFile omf = buildTestFile();

		FixupEntry extdefTarget =
				new FixupEntry(0, 0x9, true, FrameMethod.TARGET, TargetMethod.EXTDEF_INDEX, 0, 1, DisplacementType.D32);
		assertEquals("E:_printf", OmfUtils.resolveFixupTargetName(omf, extdefTarget));

		FixupEntry segdefTarget =
				new FixupEntry(0, 0x9, true, FrameMethod.TARGET, TargetMethod.SEGDEF_INDEX, 0, 2, DisplacementType.D32);
		assertEquals("S:_DATA", OmfUtils.resolveFixupTargetName(omf, segdefTarget));

		FixupEntry grpdefTarget =
				new FixupEntry(0, 0x9, true, FrameMethod.TARGET, TargetMethod.GRPDEF_INDEX, 0, 1, DisplacementType.D32);
		assertEquals("G:DGROUP", OmfUtils.resolveFixupTargetName(omf, grpdefTarget));

		FixupEntry frameTarget = new FixupEntry(
				0, 0x9, true, FrameMethod.TARGET, TargetMethod.EXPLICIT_FRAME_NUMBER, 0, 0x1234, DisplacementType.D32);
		assertEquals("F:4660", OmfUtils.resolveFixupTargetName(omf, frameTarget));
	}

	@Test
	public void test_resolve_fixup_target_name_rejects_thread_based_target() {
		OmfFile omf = buildTestFile();
		FixupEntry threadTarget = new FixupEntry(
				0,
				0x9,
				true,
				false,
				FrameMethod.TARGET,
				0,
				true,
				TargetMethod.SEGDEF_INDEX,
				null,
				null,
				DisplacementType.D32);

		assertThrows(IllegalArgumentException.class, () -> OmfUtils.resolveFixupTargetName(omf, threadTarget));
	}

	@Test
	public void test_resolve_fixup_target_name_rejects_out_of_range_extdef_index() {
		OmfFile omf = buildTestFile();
		FixupEntry extdefTarget =
				new FixupEntry(0, 0x9, true, FrameMethod.TARGET, TargetMethod.EXTDEF_INDEX, 0, 3, DisplacementType.D32);

		assertThrows(IndexOutOfBoundsException.class, () -> OmfUtils.resolveFixupTargetName(omf, extdefTarget));
	}

	@Test
	public void test_resolve_fixup_target_name_rejects_missing_target_datum() {
		OmfFile omf = buildTestFile();
		FixupEntry extdefTarget = new FixupEntry(
				0,
				0x9,
				true,
				false,
				FrameMethod.TARGET,
				0,
				false,
				TargetMethod.EXTDEF_INDEX,
				null,
				null,
				DisplacementType.D32);

		assertThrows(IllegalArgumentException.class, () -> OmfUtils.resolveFixupTargetName(omf, extdefTarget));
	}

	@Test
	public void test_resolve_fixup_target_name_rejects_out_of_range_segdef_index() {
		OmfFile omf = buildTestFile();
		FixupEntry segdefTarget =
				new FixupEntry(0, 0x9, true, FrameMethod.TARGET, TargetMethod.SEGDEF_INDEX, 0, 3, DisplacementType.D32);

		assertThrows(IndexOutOfBoundsException.class, () -> OmfUtils.resolveFixupTargetName(omf, segdefTarget));
	}

	@Test
	public void test_resolve_fixup_target_name_rejects_out_of_range_grpdef_index() {
		OmfFile omf = buildTestFile();
		FixupEntry grpdefTarget =
				new FixupEntry(0, 0x9, true, FrameMethod.TARGET, TargetMethod.GRPDEF_INDEX, 0, 2, DisplacementType.D32);

		assertThrows(IndexOutOfBoundsException.class, () -> OmfUtils.resolveFixupTargetName(omf, grpdefTarget));
	}

	@Test
	public void test_encoded_index_length_single_byte() {
		// [0..0x7F] should all be 1 byte
		assertEquals(1, OmfUtils.encodedIndexLength(0));
		assertEquals(1, OmfUtils.encodedIndexLength(1));
		assertEquals(1, OmfUtils.encodedIndexLength(0x7F));
	}

	@Test
	public void test_encoded_index_length_two_bytes() {
		// [0x80..0x7FFF] should all be 2 bytes
		assertEquals(2, OmfUtils.encodedIndexLength(0x80));
		assertEquals(2, OmfUtils.encodedIndexLength(0x81));
		assertEquals(2, OmfUtils.encodedIndexLength(0xFF));
		assertEquals(2, OmfUtils.encodedIndexLength(0x7FFF));
	}

	@Test
	public void test_encoded_index_length_rejects_negative() {
		assertThrows(IllegalArgumentException.class, () -> OmfUtils.encodedIndexLength(-1));
		assertThrows(IllegalArgumentException.class, () -> OmfUtils.encodedIndexLength(Integer.MIN_VALUE));
	}

	@Test
	public void test_encoded_index_length_rejects_out_of_range() {
		assertThrows(IllegalArgumentException.class, () -> OmfUtils.encodedIndexLength(0x8000));
		assertThrows(IllegalArgumentException.class, () -> OmfUtils.encodedIndexLength(0xFFFF));
		assertThrows(IllegalArgumentException.class, () -> OmfUtils.encodedIndexLength(Integer.MAX_VALUE));
	}

	@Test
	public void test_read_index_from_byte_array_single_byte_values() throws IOException {
		// Test reading single-byte indexes from array [0x00, 0x7F]
		byte[] data = {0x00, 0x7F, 0x01, 0x42};
		int[] offset = {0};

		assertEquals(0, OmfUtils.readIndex(data, offset));
		assertEquals(1, offset[0]); // offset advanced by 1

		assertEquals(0x7F, OmfUtils.readIndex(data, offset));
		assertEquals(2, offset[0]); // offset advanced by 1

		assertEquals(1, OmfUtils.readIndex(data, offset));
		assertEquals(3, offset[0]); // offset advanced by 1

		assertEquals(0x42, OmfUtils.readIndex(data, offset));
		assertEquals(4, offset[0]); // offset advanced by 1
	}

	@Test
	public void test_read_index_from_byte_array_two_byte_values() throws IOException {
		// Test reading two-byte indexes
		// 0x80-0x7FFF are encoded as: (0x80 | high_7bits), low_8bits
		byte[] data = {
			(byte) 0x80, (byte) 0x80, // 0x0080
			(byte) 0x80, (byte) 0x81, // 0x0081
			(byte) 0x81, 0x00, // 0x0100
			(byte) 0xFF, (byte) 0xFF // 0x7FFF
		};
		int[] offset = {0};

		assertEquals(0x0080, OmfUtils.readIndex(data, offset));
		assertEquals(2, offset[0]);

		assertEquals(0x0081, OmfUtils.readIndex(data, offset));
		assertEquals(4, offset[0]);

		assertEquals(0x0100, OmfUtils.readIndex(data, offset));
		assertEquals(6, offset[0]);

		assertEquals(0x7FFF, OmfUtils.readIndex(data, offset));
		assertEquals(8, offset[0]);
	}

	@Test
	public void test_write_index_single_byte_values() throws IOException {
		// Test writing single-byte indexes [0, 1, 0x7F]
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ByteOutputStream bos = ByteOutputStream.asLittleEndian(output);
		OmfUtils.writeIndex(bos, 0);
		OmfUtils.writeIndex(bos, 1);
		OmfUtils.writeIndex(bos, 0x7F);

		byte[] bytes = output.toByteArray();
		assertEquals(3, bytes.length);
		assertEquals(0x00, bytes[0] & 0xFF);
		assertEquals(0x01, bytes[1] & 0xFF);
		assertEquals(0x7F, bytes[2] & 0xFF);
	}

	@Test
	public void test_write_index_two_byte_values() throws IOException {
		// Test writing two-byte indexes [0x80, 0x100, 0x7FFF]
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ByteOutputStream bos = ByteOutputStream.asLittleEndian(output);
		OmfUtils.writeIndex(bos, 0x80);
		OmfUtils.writeIndex(bos, 0x0100);
		OmfUtils.writeIndex(bos, 0x7FFF);

		byte[] bytes = output.toByteArray();
		assertEquals(6, bytes.length);
		// 0x80: (0x80 | 0x00), 0x80
		assertEquals(0x80, bytes[0] & 0xFF);
		assertEquals(0x80, bytes[1] & 0xFF);
		// 0x0100: (0x80 | 0x01), 0x00
		assertEquals(0x81, bytes[2] & 0xFF);
		assertEquals(0x00, bytes[3] & 0xFF);
		// 0x7FFF: (0x80 | 0x7F), 0xFF
		assertEquals(0xFF, bytes[4] & 0xFF);
		assertEquals(0xFF, bytes[5] & 0xFF);
	}

	@Test
	public void test_write_index_rejects_negative() throws IOException {
		ByteOutputStream bos = ByteOutputStream.asLittleEndian(new ByteArrayOutputStream());
		assertThrows(IllegalArgumentException.class, () -> OmfUtils.writeIndex(bos, -1));
		assertThrows(IllegalArgumentException.class, () -> OmfUtils.writeIndex(bos, Integer.MIN_VALUE));
	}

	@Test
	public void test_write_index_rejects_out_of_range() throws IOException {
		ByteOutputStream bos = ByteOutputStream.asLittleEndian(new ByteArrayOutputStream());
		assertThrows(IllegalArgumentException.class, () -> OmfUtils.writeIndex(bos, 0x8000));
		assertThrows(IllegalArgumentException.class, () -> OmfUtils.writeIndex(bos, 0xFFFF));
		assertThrows(IllegalArgumentException.class, () -> OmfUtils.writeIndex(bos, Integer.MAX_VALUE));
	}

	@Test
	public void test_round_trip_index_write_then_read() throws IOException {
		// Test round-trip: write value then read it back
		int[] testValues = {0, 1, 0x7E, 0x7F, 0x80, 0x81, 0xFF, 0x0100, 0x7FFF};

		for (int value : testValues) {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			ByteOutputStream bos = ByteOutputStream.asLittleEndian(output);
			OmfUtils.writeIndex(bos, value);
			byte[] bytes = output.toByteArray();

			int[] offset = {0};
			int readValue = OmfUtils.readIndex(bytes, offset);
			assertEquals(value, readValue, "Round-trip failed for value: " + Integer.toHexString(value));
			assertEquals(
					bytes.length, offset[0], "Offset not advanced correctly for value: " + Integer.toHexString(value));
		}
	}

	@Test
	public void test_read_index_from_stream_single_byte_values() throws IOException {
		byte[] data = {0x00, 0x7F, 0x01, 0x42};
		ByteInputStream bis = ByteInputStream.asLittleEndian(data);

		assertEquals(0, OmfUtils.readIndex(bis));
		assertEquals(0x7F, OmfUtils.readIndex(bis));
		assertEquals(1, OmfUtils.readIndex(bis));
		assertEquals(0x42, OmfUtils.readIndex(bis));
	}

	@Test
	public void test_read_index_from_stream_two_byte_values() throws IOException {
		byte[] data = {
			(byte) 0x80, (byte) 0x80, // 0x0080
			(byte) 0x80, (byte) 0x81, // 0x0081
			(byte) 0xFF, (byte) 0xFF // 0x7FFF
		};
		ByteInputStream bis = ByteInputStream.asLittleEndian(data);

		assertEquals(0x0080, OmfUtils.readIndex(bis));
		assertEquals(0x0081, OmfUtils.readIndex(bis));
		assertEquals(0x7FFF, OmfUtils.readIndex(bis));
	}

	@Test
	public void test_stream_round_trip_multiple_indexes() throws IOException {
		// Write multiple values and read them back
		int[] testValues = {0, 1, 0x7F, 0x80, 0x0100, 0x7FFF};

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ByteOutputStream bos = ByteOutputStream.asLittleEndian(output);
		for (int value : testValues) {
			OmfUtils.writeIndex(bos, value);
		}

		ByteInputStream bis = ByteInputStream.asLittleEndian(output.toByteArray());
		for (int expectedValue : testValues) {
			int actualValue = OmfUtils.readIndex(bis);
			assertEquals(expectedValue, actualValue);
		}
	}

	@Test
	public void test_encoded_index_length_matches_written_bytes() throws IOException {
		// Verify that encodedIndexLength() accurately reflects the bytes written
		int[] testValues = {0, 1, 0x7F, 0x80, 0x0100, 0x7FFF};

		for (int value : testValues) {
			int predictedLength = OmfUtils.encodedIndexLength(value);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			ByteOutputStream bos = ByteOutputStream.asLittleEndian(output);
			OmfUtils.writeIndex(bos, value);
			int actualLength = output.toByteArray().length;
			assertEquals(
					predictedLength,
					actualLength,
					"encodedIndexLength mismatch for value: " + Integer.toHexString(value));
		}
	}
}
