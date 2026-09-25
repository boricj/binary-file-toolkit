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
package net.boricj.bft.dwarf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import net.boricj.bft.ByteInputStream;

/**
 * String table for .debug_str and .debug_line_str sections.
 */
public final class DwarfStringTable {
	private static final Charset CHARSET = StandardCharsets.UTF_8;

	private final Map<Long, String> entriesByOffset;
	private final byte[] bytes;

	/**
	 * Creates a DWARF string table from explicit offsets.
	 *
	 * @param entriesByOffset ordered string entries keyed by offset
	 */
	public DwarfStringTable(Map<Long, String> entriesByOffset) {
		this.entriesByOffset = Collections.unmodifiableMap(new LinkedHashMap<>(entriesByOffset));
		try {
			this.bytes = buildBytes(this.entriesByOffset);
		} catch (IOException ex) {
			throw new IllegalArgumentException("Failed to encode DWARF string table", ex);
		}
	}

	private DwarfStringTable(Map<Long, String> entriesByOffset, byte[] bytes) {
		this.entriesByOffset = Collections.unmodifiableMap(new LinkedHashMap<>(entriesByOffset));
		this.bytes = Arrays.copyOf(bytes, bytes.length);
	}

	/**
	 * Parses a DWARF string table.
	 *
	 * @param bytes section bytes
	 * @return parsed string table
	 * @throws IOException if parsing fails
	 */
	public static DwarfStringTable parse(byte[] bytes) throws IOException {
		Objects.requireNonNull(bytes);
		Map<Long, String> entriesByOffset = new LinkedHashMap<>();
		ByteInputStream bis = ByteInputStream.asLittleEndian(new ByteArrayInputStream(bytes));
		while (bis.available() > 0) {
			long offset = bis.getCount();
			String value = bis.readNullTerminatedString(StandardCharsets.UTF_8);
			entriesByOffset.put(offset, value);
		}
		return new DwarfStringTable(entriesByOffset, bytes);
	}

	/**
	 * Returns immutable string entries keyed by section offset.
	 *
	 * @return ordered string entries by offset
	 */
	public Map<Long, String> getEntriesByOffset() {
		return entriesByOffset;
	}

	/**
	 * Returns the string located at the provided section offset.
	 *
	 * @param offset section-relative offset
	 * @return decoded string value
	 * @throws IllegalArgumentException if the offset is outside the section bytes
	 */
	public String get(long offset) {
		String value = entriesByOffset.get(offset);
		if (value != null) {
			return value;
		}
		if (offset < 0 || offset >= bytes.length) {
			throw new IllegalArgumentException("Missing DWARF string at offset " + offset);
		}

		int end = (int) offset;
		while (end < bytes.length && bytes[end] != 0) {
			end++;
		}
		return new String(bytes, (int) offset, end - (int) offset, CHARSET);
	}

	/**
	 * Serializes this string table to raw section bytes.
	 *
	 * @return serialized string-table bytes
	 * @throws IOException never thrown in normal operation, kept for API symmetry
	 */
	public byte[] toByteArray() throws IOException {
		return Arrays.copyOf(bytes, bytes.length);
	}

	/**
	 * Writes this string table to an output stream.
	 *
	 * @param outputStream destination stream
	 * @throws IOException if writing fails
	 */
	public void write(OutputStream outputStream) throws IOException {
		outputStream.write(bytes);
	}

	private static byte[] buildBytes(Map<Long, String> entriesByOffset) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		long expectedOffset = 0;
		for (Map.Entry<Long, String> entry : entriesByOffset.entrySet()) {
			if (entry.getKey() != expectedOffset) {
				throw new IllegalStateException("Non-contiguous DWARF string table offsets are not supported");
			}
			byte[] stringBytes = entry.getValue().getBytes(CHARSET);
			baos.write(stringBytes);
			baos.write(0);
			expectedOffset += stringBytes.length + 1L;
		}
		return baos.toByteArray();
	}
}
