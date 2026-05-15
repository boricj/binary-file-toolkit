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
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.omf.coments.OmfComentWeakExternals;
import net.boricj.bft.omf.constants.OmfRecordType;
import net.boricj.bft.omf.records.OmfRecordExtdef;
import net.boricj.bft.omf.records.OmfRecordFixupp;
import net.boricj.bft.omf.records.OmfRecordLedata;
import net.boricj.bft.omf.records.OmfRecordLidata;
import net.boricj.bft.omf.records.OmfRecordLinnum;
import net.boricj.bft.omf.records.OmfRecordPubdef;
import net.boricj.bft.omf.records.OmfRecordSegdef;
import net.boricj.bft.omf.records.OmfSubrecordExtdef;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestConstraints {
	private static final class OversizedRecord extends OmfRecord {
		private final int payloadSize;

		OversizedRecord(OmfFile file, int payloadSize) {
			super(file, OmfRecordType.COMENT);
			this.payloadSize = payloadSize;
		}

		@Override
		public void write(ByteOutputStream bos) throws IOException {
			bos.write(new byte[payloadSize]);
		}
	}

	@Test
	public void test_reject_oversized_ledata_record() {
		OmfFile omf = new OmfFile.Builder().build();
		OmfRecordSegdef segment =
				new OmfRecordSegdef(omf, OmfRecordSegdef.Attributes.ofRaw(0xA9), 0x20L, "_DATA", "DATA", "");
		omf.add(segment);

		byte[] oversizedPayload = new byte[OmfUtils.MAX_RECORD_DATA_LENGTH - 2];
		IllegalArgumentException ex = assertThrows(
				IllegalArgumentException.class, () -> new OmfRecordLedata(omf, segment, 0, oversizedPayload));
		assertTrue(ex.getMessage().contains("LEDATA data length out of range"));
	}

	@Test
	public void test_reject_oversized_lidata_record() {
		OmfFile omf = new OmfFile.Builder().build();
		OmfRecordSegdef segment =
				new OmfRecordSegdef(omf, OmfRecordSegdef.Attributes.ofRaw(0xA9), 0x20L, "_DATA", "DATA", "");
		omf.add(segment);

		byte[] oversizedPayload = new byte[OmfUtils.MAX_RECORD_DATA_LENGTH - 2];
		IllegalArgumentException ex = assertThrows(
				IllegalArgumentException.class, () -> new OmfRecordLidata(omf, segment, 0, oversizedPayload));
		assertTrue(ex.getMessage().contains("LIDATA data length out of range"));
	}

	@Test
	public void test_reject_oversized_extdef_record() {
		OmfFile omf = new OmfFile.Builder().build();
		List<OmfSubrecordExtdef> entries = new ArrayList<>();

		String longName = "X".repeat(255);
		for (int i = 0; i < 260; i++) {
			entries.add(new OmfSubrecordExtdef(longName, 0));
		}

		IllegalArgumentException ex =
				assertThrows(IllegalArgumentException.class, () -> new OmfRecordExtdef(omf, entries));
		assertTrue(ex.getMessage().contains("EXTDEF data length out of range"));
	}

	@Test
	public void test_extdef_elements_are_immutable() {
		OmfFile omf = new OmfFile.Builder().build();
		OmfRecordExtdef extdef = new OmfRecordExtdef(omf, List.of(new OmfSubrecordExtdef("_ext", 0)));

		assertThrows(UnsupportedOperationException.class, () -> extdef.getElements()
				.add(new OmfSubrecordExtdef("_other", 0)));
	}

	@Test
	public void test_reject_oversized_pubdef_record() {
		OmfFile omf = new OmfFile.Builder().build();
		OmfRecordSegdef segment =
				new OmfRecordSegdef(omf, OmfRecordSegdef.Attributes.ofRaw(0xA9), 0x20L, "_DATA", "DATA", "");
		omf.add(segment);
		List<OmfRecordPubdef.PublicSymbol> symbols = new ArrayList<>();

		String longName = "Y".repeat(255);
		for (int i = 0; i < 260; i++) {
			symbols.add(new OmfRecordPubdef.PublicSymbol(longName, i, 0));
		}

		IllegalArgumentException ex =
				assertThrows(IllegalArgumentException.class, () -> new OmfRecordPubdef(omf, null, segment, 0, symbols));
		assertTrue(ex.getMessage().contains("PUBDEF data length out of range"));
	}

	@Test
	public void test_reject_pubdef_with_segment_and_base_frame() {
		OmfFile omf = new OmfFile.Builder().build();
		OmfRecordSegdef segment =
				new OmfRecordSegdef(omf, OmfRecordSegdef.Attributes.ofRaw(0xA9), 0x20L, "_DATA", "DATA", "");

		IllegalArgumentException recordEx = assertThrows(
				IllegalArgumentException.class,
				() -> new OmfRecordPubdef(
						omf, null, segment, 0x1234, List.of(new OmfRecordPubdef.PublicSymbol("_sym", 0, 0))));
		assertTrue(recordEx.getMessage().contains("base frame must be zero"));
	}

	@Test
	public void test_reject_invalid_pubdef_symbol_fields() {
		IllegalArgumentException offsetEx = assertThrows(
				IllegalArgumentException.class, () -> new OmfRecordPubdef.PublicSymbol("_sym", 0x1_0000_0000L, 0));
		assertTrue(offsetEx.getMessage().contains("offset out of range"));

		IllegalArgumentException typeEx =
				assertThrows(IllegalArgumentException.class, () -> new OmfRecordPubdef.PublicSymbol("_sym", 0, -1));
		assertTrue(typeEx.getMessage().contains("type index must be non-negative"));
	}

	@Test
	public void test_reject_invalid_linnum_and_weak_external_fields() {
		IllegalArgumentException lineEx =
				assertThrows(IllegalArgumentException.class, () -> new OmfRecordLinnum.LineNumber(0x1_0000, 0));
		assertTrue(lineEx.getMessage().contains("line number out of range"));

		IllegalArgumentException offsetEx =
				assertThrows(IllegalArgumentException.class, () -> new OmfRecordLinnum.LineNumber(1, 0x1_0000L));
		assertTrue(offsetEx.getMessage().contains("offset out of range"));

		IllegalArgumentException weakEx = assertThrows(
				IllegalArgumentException.class,
				() -> new OmfComentWeakExternals.WeakExternalPair(OmfUtils.MAX_INDEX_VALUE + 1, 0));
		assertTrue(weakEx.getMessage().contains("weak external index out of range"));
	}

	@Test
	public void test_reject_invalid_index_encoding_boundaries() {
		IllegalArgumentException negativeEx =
				assertThrows(IllegalArgumentException.class, () -> OmfUtils.encodedIndexLength(-1));
		assertTrue(negativeEx.getMessage().contains("OMF index out of range"));

		IllegalArgumentException tooLargeEx = assertThrows(
				IllegalArgumentException.class, () -> OmfUtils.encodedIndexLength(OmfUtils.MAX_INDEX_VALUE + 1));
		assertTrue(tooLargeEx.getMessage().contains("OMF index out of range"));
	}

	@Test
	public void test_write_index_rejects_out_of_range_values() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ByteOutputStream bos = ByteOutputStream.asLittleEndian(baos);

		IllegalArgumentException negativeEx =
				assertThrows(IllegalArgumentException.class, () -> OmfUtils.writeIndex(bos, -1));
		assertTrue(negativeEx.getMessage().contains("OMF index out of range"));

		IllegalArgumentException tooLargeEx = assertThrows(
				IllegalArgumentException.class, () -> OmfUtils.writeIndex(bos, OmfUtils.MAX_INDEX_VALUE + 1));
		assertTrue(tooLargeEx.getMessage().contains("OMF index out of range"));
	}

	@Test
	public void test_reject_null_elements_in_collection_based_apis() {
		OmfFile omf = new OmfFile.Builder().build();
		OmfRecordSegdef segment =
				new OmfRecordSegdef(omf, OmfRecordSegdef.Attributes.ofRaw(0xA9), 0x20L, "_DATA", "DATA", "");

		List<OmfRecordPubdef.PublicSymbol> symbols = new ArrayList<>();
		symbols.add(new OmfRecordPubdef.PublicSymbol("_sym", 0, 0));
		symbols.add(null);
		assertThrows(NullPointerException.class, () -> new OmfRecordPubdef(omf, null, segment, 0, symbols));
	}

	@Test
	public void test_reject_oversized_fixupp_record() {
		OmfFile omf = new OmfFile.Builder().build();
		List<OmfRecordFixupp.FixupEntry> entries = new ArrayList<>();

		for (int i = 0; i < 15000; i++) {
			entries.add(new OmfRecordFixupp.FixupEntry(
					i & 0x3FF,
					0x9,
					true,
					OmfRecordFixupp.FrameMethod.GRPDEF_INDEX,
					OmfRecordFixupp.TargetMethod.SEGDEF_INDEX,
					1,
					1,
					OmfRecordFixupp.DisplacementType.D32));
		}

		IllegalArgumentException ex = assertThrows(
				IllegalArgumentException.class,
				() -> new OmfRecordFixupp(omf, entries, OmfRecordFixupp.SpecificType.FIXUPP_32));
		assertTrue(ex.getMessage().contains("FIXUPP data length out of range"));
	}

	@Test
	public void test_write_rejects_oversized_record_payload() {
		OmfFile omf = new OmfFile.Builder().build();
		omf.add(new OversizedRecord(omf, OmfUtils.MAX_RECORD_DATA_LENGTH + 1));

		IOException ex = assertThrows(IOException.class, () -> omf.write(new ByteArrayOutputStream()));
		assertTrue(ex.getMessage().contains("is too large"));
	}

	@Test
	public void test_omf_file_elements_are_immutable() {
		OmfFile omf = new OmfFile.Builder().build();

		assertThrows(
				UnsupportedOperationException.class, () -> omf.getElements().add(new OversizedRecord(omf, 1)));
	}
}
