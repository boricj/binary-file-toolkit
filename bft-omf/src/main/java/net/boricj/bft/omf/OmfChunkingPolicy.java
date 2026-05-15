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

/**
 * Chunking policy for OMF record emission.
 *
 * <p>This policy distinguishes between plain record chunking (e.g. LNAMES/EXTDEF/PUBDEF)
 * and paired data/relocation chunking where LEDATA and FIXUPP records must stay adjacent.
 *
 * @param maxRecordSize maximum full OMF record size in bytes
 */
public record OmfChunkingPolicy(int maxRecordSize) {

	/**
	 * Validates the maximum record size.
	 */
	public OmfChunkingPolicy {
		if (maxRecordSize <= 16) {
			throw new IllegalArgumentException(
					"maxRecordSize is too small for practical OMF records: " + maxRecordSize);
		}
	}

	/**
	 * Returns a Borland-compatible default chunking policy targeting 1024-byte max records.
	 *
	 * @return default Borland-like policy
	 */
	public static OmfChunkingPolicy borland1024() {
		return forMaxRecordSize(1024);
	}

	/**
	 * Returns a policy tuned for a caller-provided maximum full record size.
	 *
	 * @param maxRecordSize maximum full OMF record size in bytes
	 * @return policy derived from max record size
	 */
	public static OmfChunkingPolicy forMaxRecordSize(int maxRecordSize) {
		return new OmfChunkingPolicy(maxRecordSize);
	}

	/**
	 * Returns the hard maximum full FIXUPP record size in bytes.
	 *
	 * @return maximum FIXUPP record size
	 */
	public int maxFixuppRecordSize() {
		return Math.min(maxRecordSize, 0x400);
	}

	/**
	 * Returns the hard maximum size for symbol/name style records in bytes.
	 *
	 * @return maximum LNAMES/EXTDEF/PUBDEF record size
	 */
	public int hardMaxSymbolAndNameRecordSize() {
		return Math.min(maxRecordSize, 0x3ff);
	}
}
