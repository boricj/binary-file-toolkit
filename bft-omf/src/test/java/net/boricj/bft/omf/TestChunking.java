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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.boricj.bft.omf.records.OmfRecordExtdef;
import net.boricj.bft.omf.records.OmfRecordLnames;
import net.boricj.bft.omf.records.OmfRecordPubdef;
import net.boricj.bft.omf.records.OmfRecordSegdef;
import net.boricj.bft.omf.records.OmfSubrecordExtdef;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestChunking {
	@Test
	public void test_emit_chunked_records_splits_lnames_records() {
		OmfFile omf = new OmfFile.Builder().build();
		List<String> names = List.of("", "segment_name_1", "segment_name_2", "segment_name_3");

		OmfUtils.emitChunkedRecords(omf, names, 32, "LNAMES", chunk -> new OmfRecordLnames(omf, chunk));

		assertTrue(omf.getElements().size() > 1);
		for (OmfRecord record : omf.getElements()) {
			assertTrue(record instanceof OmfRecordLnames);
			assertTrue(record.getLength() <= 32);
		}
	}

	@Test
	public void test_chunking_policy_for_small_max_record_size() {
		OmfChunkingPolicy policy = OmfChunkingPolicy.forMaxRecordSize(64);

		assertTrue(policy.maxRecordSize() == 64);
		assertTrue(policy.hardMaxSymbolAndNameRecordSize() <= 64);
		assertTrue(policy.maxFixuppRecordSize() <= 64);
	}

	@Test
	public void test_emit_chunked_records_small_max_respects_limit() {
		OmfFile omf = new OmfFile.Builder().build();
		// Many short names: each single name fits, but batches don't
		List<String> names = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			names.add("seg" + i);
		}

		OmfUtils.emitChunkedRecords(omf, names, 16, "LNAMES", chunk -> new OmfRecordLnames(omf, chunk));

		assertTrue(omf.getElements().size() > 1, "Expected multiple records for small max size");
		for (OmfRecord record : omf.getElements()) {
			assertTrue(record instanceof OmfRecordLnames);
			assertTrue(record.getLength() <= 16, "Record length " + record.getLength() + " exceeds max 16");
		}
	}

	@Test
	public void test_emit_chunked_records_splits_extdef_and_preserves_order() {
		OmfFile omf = new OmfFile.Builder().build();
		List<OmfSubrecordExtdef> entries = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			entries.add(new OmfSubrecordExtdef("_ext_" + i, 0));
		}

		OmfUtils.emitChunkedRecords(omf, entries, 32, "EXTDEF", chunk -> new OmfRecordExtdef(omf, chunk));

		List<String> flattened = new ArrayList<>();
		assertTrue(omf.getElements().size() > 1);
		for (OmfRecord record : omf.getElements()) {
			assertTrue(record instanceof OmfRecordExtdef);
			assertTrue(record.getLength() <= 32, "Record length " + record.getLength() + " exceeds max 32");
			OmfRecordExtdef extdef = (OmfRecordExtdef) record;
			extdef.stream().map(e -> e.name()).forEach(flattened::add);
		}

		assertEquals(entries.stream().map(e -> e.name()).toList(), flattened);
	}

	@Test
	public void test_emit_chunked_records_splits_pubdef_and_preserves_order() {
		OmfFile omf = new OmfFile.Builder().build();
		OmfRecordSegdef segment =
				new OmfRecordSegdef(omf, OmfRecordSegdef.Attributes.ofRaw(0xA9), 0x1000L, "_DATA", "DATA", "");
		omf.add(segment);

		List<OmfRecordPubdef.PublicSymbol> symbols = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			symbols.add(new OmfRecordPubdef.PublicSymbol("_sym_" + i, i * 4L, 0));
		}

		OmfUtils.emitChunkedRecords(
				omf, symbols, 48, "PUBDEF", chunk -> new OmfRecordPubdef(omf, null, segment, 0, chunk));

		List<OmfRecordPubdef.PublicSymbol> flattened = new ArrayList<>();
		int pubdefRecordCount = 0;
		for (OmfRecord record : omf.getElements()) {
			if (!(record instanceof OmfRecordPubdef pubdef)) {
				continue;
			}

			pubdefRecordCount++;
			assertTrue(record.getLength() <= 48, "Record length " + record.getLength() + " exceeds max 48");
			flattened.addAll(pubdef.getSymbols());
		}

		assertTrue(pubdefRecordCount > 1, "Expected multiple PUBDEF records for small max size");
		assertEquals(symbols, flattened);
	}
}
