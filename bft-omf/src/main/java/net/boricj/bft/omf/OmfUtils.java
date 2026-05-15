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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.omf.records.OmfRecordExtdef;
import net.boricj.bft.omf.records.OmfRecordFixupp.FixupEntry;
import net.boricj.bft.omf.records.OmfRecordFixupp.TargetMethod;
import net.boricj.bft.omf.records.OmfRecordGrpdef;
import net.boricj.bft.omf.records.OmfRecordPubdef;
import net.boricj.bft.omf.records.OmfRecordPubdef.PublicSymbol;
import net.boricj.bft.omf.records.OmfRecordSegdef;

/**
 * Utility methods for OMF parsing and writing.
 */
public final class OmfUtils {
	/** Maximum value representable by the 16-bit OMF Record Length field. */
	public static final int MAX_RECORD_CONTENT_LENGTH = 0xFFFF;
	/** Maximum record data length (record contents minus checksum byte). */
	public static final int MAX_RECORD_DATA_LENGTH = MAX_RECORD_CONTENT_LENGTH - 1;
	/** Maximum index representable by OMF 1/2-byte index encoding. */
	public static final int MAX_INDEX_VALUE = 0x7FFF;

	private OmfUtils() {
		// Utility class - prevent instantiation
	}

	/**
	 * Read a variable-length index from a byte array.
	 * Indexes can be 1 or 2 bytes depending on the value.
	 *
	 * @param data the byte array to read from
	 * @param offset array containing current offset (will be updated)
	 * @return the index value
	 */
	public static int readIndex(byte[] data, int[] offset) {
		int value = data[offset[0]++] & 0xFF;
		if (value == 0) {
			return 0;
		}
		if ((value & 0x80) == 0) {
			return value;
		}
		int high = value & 0x7F;
		int low = data[offset[0]++] & 0xFF;
		return (high << 8) | low;
	}

	/**
	 * Read a variable-length index from a stream.
	 * Indexes can be 1 or 2 bytes depending on the value.
	 *
	 * @param bis the input stream
	 * @return the index value
	 * @throws IOException if an I/O error occurs
	 */
	public static int readIndex(ByteInputStream bis) throws IOException {
		int value = bis.readUnsignedByte();
		if (value == 0) {
			return 0;
		}
		if ((value & 0x80) == 0) {
			return value;
		}
		int high = value & 0x7F;
		int low = bis.readUnsignedByte();
		return (high << 8) | low;
	}

	/**
	 * Write a variable-length index to a stream.
	 * Indexes can be 1 or 2 bytes depending on the value.
	 *
	 * @param bos the output stream
	 * @param index the index value to write
	 * @throws IOException if an I/O error occurs
	 */
	public static void writeIndex(ByteOutputStream bos, int index) throws IOException {
		if (index < 0 || index > MAX_INDEX_VALUE) {
			throw new IllegalArgumentException("OMF index out of range: " + index);
		}
		if (index == 0) {
			bos.writeByte(0);
		} else if (index < 0x80) {
			bos.writeByte(index);
		} else {
			bos.writeByte(0x80 | (index >> 8));
			bos.writeByte(index & 0xFF);
		}
	}

	/**
	 * Read an unsigned 16-bit little-endian integer from a byte array.
	 *
	 * @param data the byte array to read from
	 * @param offset the offset to read at
	 * @return the integer value
	 */
	public static int u16le(byte[] data, int offset) {
		return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8);
	}

	/**
	 * Read an unsigned 32-bit little-endian integer from a byte array.
	 *
	 * @param data the byte array to read from
	 * @param offset the offset to read at
	 * @return the integer value as a long
	 */
	public static long u32le(byte[] data, int offset) {
		return Integer.toUnsignedLong(u16le(data, offset) | (u16le(data, offset + 2) << 16));
	}

	/**
	 * Returns the encoded byte length for an OMF index value.
	 *
	 * @param index the index value
	 * @return encoded length in bytes
	 */
	public static int encodedIndexLength(int index) {
		if (index < 0 || index > MAX_INDEX_VALUE) {
			throw new IllegalArgumentException("OMF index out of range: " + index);
		}
		if (index == 0) {
			return 1;
		}
		return index < 0x80 ? 1 : 2;
	}

	/**
	 * Emits a chunked sequence of plain records where each output record must fit under the provided
	 * maximum size.
	 *
	 * @param file destination OMF file
	 * @param items ordered items to chunk
	 * @param maxRecordSize maximum full record size in bytes
	 * @param itemKind item kind used in diagnostics
	 * @param recordFactory factory that builds an OMF record from the current item chunk
	 * @param <T> item type
	 */
	public static <T> void emitChunkedRecords(
			OmfFile file,
			List<T> items,
			int maxRecordSize,
			String itemKind,
			Function<List<T>, OmfRecord> recordFactory) {
		Objects.requireNonNull(file);
		Objects.requireNonNull(items);
		Objects.requireNonNull(itemKind);
		Objects.requireNonNull(recordFactory);
		if (maxRecordSize <= 0) {
			throw new IllegalArgumentException("maxRecordSize must be positive: " + maxRecordSize);
		}
		if (items.isEmpty()) {
			return;
		}

		List<T> current = new ArrayList<>();
		for (T item : items) {
			current.add(item);
			long measured = recordFactory.apply(List.copyOf(current)).getLength();
			if (measured > maxRecordSize) {
				if (current.size() == 1) {
					throw new IllegalArgumentException(
							itemKind + " entry exceeds chunk limit: " + measured + " > " + maxRecordSize);
				}

				T overflow = current.remove(current.size() - 1);
				file.add(recordFactory.apply(List.copyOf(current)));

				current = new ArrayList<>();
				current.add(overflow);
				long singleMeasured = recordFactory.apply(List.copyOf(current)).getLength();
				if (singleMeasured > maxRecordSize) {
					throw new IllegalArgumentException(
							itemKind + " entry exceeds chunk limit: " + singleMeasured + " > " + maxRecordSize);
				}
			}
		}

		if (!current.isEmpty()) {
			file.add(recordFactory.apply(List.copyOf(current)));
		}
	}

	/**
	 * One grouped PUBDEF context with all symbols emitted for that context.
	 *
	 * @param group PUBDEF group (nullable)
	 * @param segment PUBDEF segment (nullable)
	 * @param baseFrame PUBDEF base frame
	 * @param symbols symbols emitted under this context
	 */
	public record PubdefContextData(
			OmfRecordGrpdef group, OmfRecordSegdef segment, int baseFrame, List<PublicSymbol> symbols) {
		/**
		 * Creates immutable grouped PUBDEF context data.
		 */
		public PubdefContextData {
			Objects.requireNonNull(symbols);
			symbols = List.copyOf(symbols);
		}
	}

	private record PubdefContextKey(OmfRecordGrpdef group, OmfRecordSegdef segment, int baseFrame) {}

	/**
	 * Finds a segment definition by segment name.
	 *
	 * @param file OMF file
	 * @param segmentName target segment name
	 * @return matching segment definition
	 * @throws NullPointerException if file or segmentName is null
	 * @throws IllegalStateException if no segment with the provided name exists
	 */
	public static OmfRecordSegdef findSegmentByName(OmfFile file, String segmentName) {
		Objects.requireNonNull(file);
		Objects.requireNonNull(segmentName);

		for (OmfRecord record : file.getElements()) {
			if (record instanceof OmfRecordSegdef segdef && segmentName.equals(segdef.getSegmentName())) {
				return segdef;
			}
		}

		throw new IllegalStateException("Missing segment: " + segmentName);
	}

	/**
	 * Returns a SEGDEF record by 1-based index.
	 *
	 * <p>This is a utility wrapper around OmfFile.getSegmentByIndex for callers that only
	 * depend on OmfUtils.
	 *
	 * @param file OMF file
	 * @param index 1-based SEGDEF index
	 * @return matching SEGDEF
	 * @throws NullPointerException if file is null
	 * @throws IndexOutOfBoundsException if index is not a valid SEGDEF index
	 */
	public static OmfRecordSegdef getSegmentByIndex(OmfFile file, int index) {
		Objects.requireNonNull(file);
		return file.getSegmentByIndex(index);
	}

	/**
	 * Returns a GRPDEF record by 1-based index.
	 *
	 * <p>This is a utility wrapper around OmfFile.getGroupByIndex for callers that only
	 * depend on OmfUtils.
	 *
	 * @param file OMF file
	 * @param index 1-based GRPDEF index
	 * @return matching GRPDEF
	 * @throws NullPointerException if file is null
	 * @throws IndexOutOfBoundsException if index is not a valid GRPDEF index
	 */
	public static OmfRecordGrpdef getGroupByIndex(OmfFile file, int index) {
		Objects.requireNonNull(file);
		return file.getGroupByIndex(index);
	}

	/**
	 * Flattens all EXTDEF symbol names from an OMF file in record order.
	 *
	 * @param file OMF file
	 * @return EXTDEF symbol names
	 * @throws NullPointerException if file is null
	 */
	public static List<String> externalSymbolNames(OmfFile file) {
		Objects.requireNonNull(file);
		List<String> names = new ArrayList<>();

		for (OmfRecord record : file.getElements()) {
			if (record instanceof OmfRecordExtdef extdef) {
				extdef.stream().map(e -> e.name()).forEach(names::add);
			}
		}

		return List.copyOf(names);
	}

	/**
	 * Groups all PUBDEF symbols by (group, segment, baseFrame) context in record order.
	 *
	 * @param file OMF file
	 * @return grouped PUBDEF contexts
	 * @throws NullPointerException if file is null
	 */
	public static List<PubdefContextData> pubdefContexts(OmfFile file) {
		Objects.requireNonNull(file);
		Map<PubdefContextKey, List<PublicSymbol>> groupedSymbols = new LinkedHashMap<>();

		for (OmfRecord record : file.getElements()) {
			if (record instanceof OmfRecordPubdef pubdef) {
				PubdefContextKey key =
						new PubdefContextKey(pubdef.getGroup(), pubdef.getSegment(), pubdef.getBaseFrame());
				groupedSymbols.computeIfAbsent(key, k -> new ArrayList<>()).addAll(pubdef.getSymbols());
			}
		}

		List<PubdefContextData> result = new ArrayList<>();
		for (Map.Entry<PubdefContextKey, List<PublicSymbol>> entry : groupedSymbols.entrySet()) {
			PubdefContextKey key = entry.getKey();
			result.add(new PubdefContextData(key.group(), key.segment(), key.baseFrame(), entry.getValue()));
		}
		return List.copyOf(result);
	}

	/**
	 * Resolves a non-threaded fixup target into a stable human-readable token.
	 *
	 * @param file OMF file used for SEGDEF/GRPDEF/EXTDEF lookup
	 * @param entry fixup entry
	 * @return target token in the form E:name, S:name, G:name, or F:index
	 * @throws NullPointerException if file or entry is null
	 * @throws IllegalArgumentException if target uses thread context or has no target datum
	 * @throws IndexOutOfBoundsException if target datum indexes an out-of-range SEGDEF/GRPDEF/EXTDEF
	 */
	public static String resolveFixupTargetName(OmfFile file, FixupEntry entry) {
		Objects.requireNonNull(file);
		Objects.requireNonNull(entry);

		if (entry.isTargetFromThread()) {
			throw new IllegalArgumentException("Cannot resolve thread-based target fixup");
		}

		TargetMethod method = entry.getTargetMethodEnum();
		Integer datum = entry.getTargetDatum();
		if (datum == null) {
			throw new IllegalArgumentException("Fixup target datum is required");
		}

		if (method == TargetMethod.EXTDEF_INDEX) {
			int extIndex = datum.intValue() - 1;
			List<String> extdefs = externalSymbolNames(file);
			if (extIndex < 0 || extIndex >= extdefs.size()) {
				throw new IndexOutOfBoundsException("EXTDEF index out of range: " + datum);
			}
			return "E:" + extdefs.get(extIndex);
		}

		if (method == TargetMethod.SEGDEF_INDEX) {
			return "S:" + getSegmentByIndex(file, datum.intValue()).getSegmentName();
		}

		if (method == TargetMethod.GRPDEF_INDEX) {
			return "G:" + getGroupByIndex(file, datum.intValue()).getGroupName();
		}

		return "F:" + datum;
	}
}
