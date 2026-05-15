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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import net.boricj.bft.omf.records.OmfRecordFixupp;
import net.boricj.bft.omf.records.OmfRecordFixupp.FixupEntry;
import net.boricj.bft.omf.records.OmfRecordLedata;
import net.boricj.bft.omf.records.OmfRecordLidata;
import net.boricj.bft.omf.records.OmfRecordSegdef;

/**
 * Logical segment-level representation on top of OMF records.
 *
 * <p>This helper reconstructs one segment's payload/fixups from LEDATA/FIXUPP records and can
 * emit those logical contents back into chunked LEDATA/FIXUPP records under a chunking policy.
 */
public final class OmfSegmentData {
	private static final byte LIDATA_32_RECORD_TYPE = (byte) 0xA3;

	private static final class LidataDecoder {
		private final byte[] encoded;
		private final int repeatWidthBytes;
		private int position;

		private LidataDecoder(byte[] encoded, boolean is32Bit) {
			this.encoded = encoded;
			this.repeatWidthBytes = is32Bit ? 4 : 2;
			this.position = 0;
		}

		private byte[] decodeAll() {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			while (position < encoded.length) {
				decodeBlockInto(out);
			}
			return out.toByteArray();
		}

		private void decodeBlockInto(ByteArrayOutputStream out) {
			long repeatCount = readUnsigned(repeatWidthBytes);
			int blockCount = readU16();

			if (repeatCount < 0 || repeatCount > Integer.MAX_VALUE) {
				throw new IllegalStateException("Unsupported LIDATA repeat count: " + repeatCount);
			}

			if (blockCount == 0) {
				int payloadLength = readU8();
				byte[] payload = readBytes(payloadLength);
				for (int i = 0; i < repeatCount; i++) {
					out.writeBytes(payload);
				}
				return;
			}

			ByteArrayOutputStream nested = new ByteArrayOutputStream();
			for (int i = 0; i < blockCount; i++) {
				decodeBlockInto(nested);
			}
			byte[] expandedNested = nested.toByteArray();
			for (int i = 0; i < repeatCount; i++) {
				out.writeBytes(expandedNested);
			}
		}

		private long readUnsigned(int widthBytes) {
			if (position + widthBytes > encoded.length) {
				throw new IllegalStateException("Malformed LIDATA: truncated repeat count");
			}

			long value = 0;
			for (int i = 0; i < widthBytes; i++) {
				value |= ((long) encoded[position++] & 0xFFL) << (8 * i);
			}
			return value;
		}

		private int readU16() {
			if (position + 2 > encoded.length) {
				throw new IllegalStateException("Malformed LIDATA: truncated block count");
			}
			int value = (encoded[position] & 0xFF) | ((encoded[position + 1] & 0xFF) << 8);
			position += 2;
			return value;
		}

		private int readU8() {
			if (position >= encoded.length) {
				throw new IllegalStateException("Malformed LIDATA: truncated payload length");
			}
			return encoded[position++] & 0xFF;
		}

		private byte[] readBytes(int length) {
			if (position + length > encoded.length) {
				throw new IllegalStateException("Malformed LIDATA: truncated payload bytes");
			}
			byte[] result = Arrays.copyOfRange(encoded, position, position + length);
			position += length;
			return result;
		}
	}

	/**
	 * One fixup anchored at an absolute segment byte offset.
	 *
	 * @param segmentOffset absolute byte offset within the logical segment
	 * @param entry fixup entry anchored at that offset
	 */
	public record FixupAtOffset(int segmentOffset, FixupEntry entry) {
		/**
		 * Validates the logical fixup location.
		 */
		public FixupAtOffset {
			Objects.requireNonNull(entry);
			if (segmentOffset < 0) {
				throw new IllegalArgumentException("segmentOffset must be non-negative: " + segmentOffset);
			}
		}
	}

	private final OmfRecordSegdef segment;
	private final byte[] bytes;
	private final List<FixupAtOffset> fixups;

	/**
	 * Creates immutable logical segment contents.
	 *
	 * @param segment target segment
	 * @param bytes full logical segment bytes
	 * @param fixups absolute segment-offset fixups
	 */
	public OmfSegmentData(OmfRecordSegdef segment, byte[] bytes, List<FixupAtOffset> fixups) {
		Objects.requireNonNull(segment);
		Objects.requireNonNull(bytes);
		Objects.requireNonNull(fixups);
		if (bytes.length > segment.getSegmentLength()) {
			throw new IllegalArgumentException(
					"Segment bytes exceed SEGDEF length: " + bytes.length + " > " + segment.getSegmentLength());
		}

		this.segment = segment;
		this.bytes = bytes.clone();
		this.fixups = fixups.stream()
				.sorted(Comparator.comparingInt(FixupAtOffset::segmentOffset))
				.toList();

		for (FixupAtOffset fixup : fixups) {
			if (fixup.segmentOffset() >= bytes.length) {
				throw new IllegalArgumentException(
						"Fixup segment offset out of bounds: " + fixup.segmentOffset() + " >= " + bytes.length);
			}
		}
	}

	/**
	 * Parses logical segment contents from an OMF record stream using strict contiguous
	 * LEDATA/LIDATA reconstruction rules.
	 *
	 * <p>This is an alias for parseStrict and preserves historical call sites while making
	 * strict behavior explicit in the API.
	 *
	 * <p>This parser is intentionally strict: LEDATA chunks for the segment must be contiguous from
	 * offset 0 with no overlaps or gaps.
	 *
	 * @param file OMF file to inspect
	 * @param segment target segment
	 * @return logical segment data representation
	 */
	public static OmfSegmentData parse(OmfFile file, OmfRecordSegdef segment) {
		return parseStrict(file, segment);
	}

	/**
	 * Parses logical segment contents from an OMF record stream using strict contiguous
	 * LEDATA/LIDATA reconstruction rules.
	 *
	 * <p>This parser is intentionally strict: LEDATA chunks for the segment must be contiguous from
	 * offset 0 with no overlaps or gaps.
	 *
	 * @param file OMF file to inspect
	 * @param segment target segment
	 * @return logical segment data representation
	 */
	public static OmfSegmentData parseStrict(OmfFile file, OmfRecordSegdef segment) {
		Objects.requireNonNull(file);
		Objects.requireNonNull(segment);

		ByteArrayOutputStream data = new ByteArrayOutputStream();
		List<FixupAtOffset> fixups = new ArrayList<>();
		int expectedOffset = 0;
		int activeChunkStart = -1;
		int activeChunkLength = 0;
		boolean activeChunkBelongsToTarget = false;

		for (OmfRecord record : file.getElements()) {
			if (record instanceof OmfRecordFixupp fixupp) {
				if (!activeChunkBelongsToTarget) {
					continue;
				}
				for (FixupEntry entry : fixupp.getFixupEntries()) {
					int relativeOffset = entry.getDataRecordOffset();
					int displacementWidth = entry.getDisplacementType().getByteCount();
					int lastTargetByteOffsetExclusive = relativeOffset + displacementWidth;
					if (relativeOffset < 0 || lastTargetByteOffsetExclusive > activeChunkLength) {
						throw new IllegalStateException(
								"FIXUPP offset out of data chunk bounds for segment " + segment.getSegmentName()
										+ ": chunkStart=" + activeChunkStart + ", chunkLength=" + activeChunkLength
										+ ", relativeOffset=" + relativeOffset + ", width=" + displacementWidth
										+ ", validRange=[0, " + activeChunkLength + ")");
					}
					fixups.add(new FixupAtOffset(activeChunkStart + relativeOffset, entry));
				}
				continue;
			}

			activeChunkBelongsToTarget = false;

			if (record instanceof OmfRecordLedata ledata && ledata.getSegment() == segment) {
				int chunkStart = Math.toIntExact(ledata.getDataOffset());
				expectedOffset =
						appendChunkBytes(segment, data, expectedOffset, chunkStart, ledata.getData(), "LEDATA");
				activeChunkStart = chunkStart;
				activeChunkLength = ledata.getData().length;
				activeChunkBelongsToTarget = true;
				continue;
			}

			if (record instanceof OmfRecordLidata lidata && lidata.getSegment() == segment) {
				int chunkStart = Math.toIntExact(lidata.getDataOffset());
				byte[] decoded = decodeLidataChunk(segment, lidata);
				expectedOffset = appendChunkBytes(segment, data, expectedOffset, chunkStart, decoded, "LIDATA");
				activeChunkStart = chunkStart;
				activeChunkLength = decoded.length;
				activeChunkBelongsToTarget = true;
			}
		}

		return new OmfSegmentData(segment, data.toByteArray(), fixups);
	}

	/**
	 * Emits this logical segment as chunked LEDATA/FIXUPP records to the provided target segment.
	 *
	 * @param file destination OMF file
	 * @param policy chunking policy
	 */
	public void emit(OmfFile file, OmfChunkingPolicy policy) {
		Objects.requireNonNull(file);
		Objects.requireNonNull(policy);

		ByteArrayInputStream data = new ByteArrayInputStream(bytes);
		Iterator<FixupAtOffset> fixupIterator = fixups.iterator();
		FixupAtOffset nextFixup = null;

		while (data.available() > 0) {
			int chunkStart = bytes.length - data.available();
			int maxChunkDataSize = bytes.length - chunkStart;

			List<FixupAtOffset> chunkFixups = new ArrayList<>();
			int fixuppLength = (int) new OmfRecordFixupp(file, List.of()).getLength();

			// Truncate chunk data size to largest allowed by policy.
			maxChunkDataSize =
					Math.min(maxChunkDataSize, maxLedataDataSize(file, segment, chunkStart, policy.maxRecordSize()));

			while (nextFixup != null || fixupIterator.hasNext()) {
				// Grab next fixup if needed.
				if (nextFixup == null && fixupIterator.hasNext()) {
					nextFixup = fixupIterator.next();
				}

				int fixupOffset = nextFixup.segmentOffset() - chunkStart;
				int fixupLength = (int) nextFixup.entry().getLength();
				int fixupLastTargetByteOffset =
						fixupOffset + nextFixup.entry().getDisplacementType().getByteCount();

				boolean fixupStraddlesDataChunk = fixupLastTargetByteOffset > maxChunkDataSize;
				boolean fixupStraddlesFixuppChunk = (fixuppLength + fixupLength) > policy.maxFixuppRecordSize();

				if (fixupStraddlesDataChunk || fixupStraddlesFixuppChunk) {
					// Truncate chunk to end before this fixup and emit what we have so far.
					maxChunkDataSize = fixupOffset;
					break;
				} else {
					// Include this fixup in the chunk and keep going.
					chunkFixups.add(nextFixup);
					fixuppLength += fixupLength;
					nextFixup = null;
				}
			}

			// Don't get stuck in a loop if we can't make forward progress.
			if (maxChunkDataSize == 0) {
				throw new IllegalStateException("Chunking made no forward progress");
			}

			// Emit data record.
			try {
				file.add(new OmfRecordLedata(file, segment, chunkStart, data.readNBytes(maxChunkDataSize)));
			} catch (IOException e) {
				// Shouldn't happen.
				throw new RuntimeException(e);
			}

			// Emit fixups.
			if (!chunkFixups.isEmpty()) {
				file.add(new OmfRecordFixupp(file, toChunkRelativeFixupEntries(chunkFixups, chunkStart)));
			}
		}
	}

	/**
	 * Returns the target segment.
	 *
	 * @return segment definition
	 */
	public OmfRecordSegdef getSegment() {
		return segment;
	}

	/**
	 * Returns a copy of the segment bytes.
	 *
	 * @return segment bytes
	 */
	public byte[] getBytes() {
		return bytes.clone();
	}

	/**
	 * Returns absolute-offset fixups for the segment.
	 *
	 * @return immutable ordered fixup list
	 */
	public List<FixupAtOffset> getFixups() {
		return fixups;
	}

	private static int appendChunkBytes(
			OmfRecordSegdef segment,
			ByteArrayOutputStream data,
			int expectedOffset,
			int chunkStart,
			byte[] chunkBytes,
			String recordKind) {
		if (chunkStart != expectedOffset) {
			throw new IllegalStateException(recordKind + " chunks must be contiguous from offset 0 for segment "
					+ segment.getSegmentName() + ": expectedStart=" + expectedOffset + ", actualStart=" + chunkStart
					+ ", chunkLength=" + chunkBytes.length);
		}

		data.writeBytes(chunkBytes);
		return chunkStart + chunkBytes.length;
	}

	private static byte[] decodeLidataChunk(OmfRecordSegdef segment, OmfRecordLidata lidata) {
		byte[] encoded = lidata.getEncodedData();
		if (encoded.length == 0) {
			return new byte[0];
		}

		LidataDecoder decoder = new LidataDecoder(encoded, lidata.getSpecificTypeValue() == LIDATA_32_RECORD_TYPE);
		try {
			return decoder.decodeAll();
		} catch (IllegalStateException ex) {
			throw new IllegalStateException(
					"Malformed LIDATA for segment " + segment.getSegmentName() + " at dataOffset="
							+ lidata.getDataOffset() + ": " + ex.getMessage(),
					ex);
		}
	}

	private static int maxLedataDataSize(OmfFile file, OmfRecordSegdef segment, long dataOffset, int maxRecordSize) {
		Objects.requireNonNull(file);
		Objects.requireNonNull(segment);
		long overhead = new OmfRecordLedata(file, segment, dataOffset, new byte[0]).getLength();
		return (int) Math.max(1, maxRecordSize - overhead);
	}

	private static List<FixupEntry> toChunkRelativeFixupEntries(List<FixupAtOffset> fixups, int chunkStart) {
		List<FixupEntry> entries = new ArrayList<>(fixups.size());
		for (FixupAtOffset fixup : fixups) {
			FixupEntry source = fixup.entry();
			entries.add(new FixupEntry(
					fixup.segmentOffset() - chunkStart,
					source.getLocationType(),
					source.isSegmentRelative(),
					source.isFrameFromThread(),
					source.getFrameMethodEnum(),
					source.getFrameDatum(),
					source.isTargetFromThread(),
					source.getTargetMethodEnum(),
					source.getTargetDatum(),
					source.getTargetDisplacement(),
					source.getDisplacementType()));
		}
		return entries;
	}
}
