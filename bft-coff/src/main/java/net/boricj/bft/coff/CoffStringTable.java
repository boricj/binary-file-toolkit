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
package net.boricj.bft.coff;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.TreeMap;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.Writable;

/**
 * COFF string table for storing symbol names that don't fit in the symbol table entry.
 * Long names are stored here and referenced by offset.
 */
public class CoffStringTable implements Iterable<Integer>, Writable {
	/** Default charset used to encode and decode string table entries. */
	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	private final CoffFile coff;
	private final TreeMap<Integer, byte[]> strings = new TreeMap<>();
	private final Map<String, Integer> lookup = new HashMap<>();
	private final Charset charset;

	/**
	 * Creates an empty string table for a builder-backed COFF file.
	 *
	 * @param coff COFF file instance
	 * @param builder COFF file builder
	 */
	protected CoffStringTable(CoffFile coff, CoffFile.Builder builder) {
		this.coff = coff;
		this.charset = builder.getCharset();
	}

	/**
	 * Parses a string table from a parser-backed COFF file.
	 *
	 * @param coff COFF file instance
	 * @param parser COFF file parser
	 * @throws IOException if an I/O error occurs
	 */
	protected CoffStringTable(CoffFile coff, CoffFile.Parser parser) throws IOException {
		this.coff = coff;
		this.charset = parser.getCharset();

		FileInputStream fis = parser.getFileInputStream();
		fis.getChannel().position(getOffset(parser));
		DataInput dataInput = coff.wrap(fis);

		int length = dataInput.readInt();
		byte[] bytes = new byte[(int) length - 4];
		fis.readNBytes(bytes, 0, bytes.length);

		int lastIndex = 0;
		for (int index = 0; index < bytes.length; index++) {
			if (bytes[index] == 0) {
				ByteBuffer buffer = ByteBuffer.wrap(bytes, lastIndex, index - lastIndex);
				String string = charset.decode(buffer).toString();

				add(string);
				lastIndex = index + 1;
			}
		}
	}

	@Override
	public long getOffset() {
		CoffSymbolTable symbolTable = coff.getSymbols();
		return symbolTable.getOffset() + symbolTable.getLength();
	}

	private long getOffset(CoffFile.Parser parser) {
		return parser.pointerToSymbolTable + parser.numberOfSymbols * CoffSymbolTable.RECORD_LENGTH;
	}

	@Override
	public long getLength() {
		if (strings.isEmpty()) {
			return 4;
		}

		Entry<Integer, byte[]> lastEntry = strings.lastEntry();
		return lastEntry.getKey() + lastEntry.getValue().length + 1;
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		DataOutput dataOutput = coff.wrap(outputStream);

		dataOutput.writeInt((int) getLength());
		for (Entry<Integer, byte[]> entry : strings.entrySet()) {
			dataOutput.write(entry.getValue());
			dataOutput.writeByte(0x00);
		}
	}

	/**
	 * Adds a string to the table.
	 *
	 * @param string string to add
	 * @return offset where the string was inserted
	 */
	public int add(String string) {
		Objects.requireNonNull(string);

		byte[] bytes = string.getBytes(charset);
		for (byte b : bytes) {
			if (b == 0x00) {
				String fmt = "string %s encoded using charset %s produced a null byte";
				String msg = String.format(fmt, string, charset.name());
				throw new IllegalArgumentException(msg);
			}
		}

		int nextKey = (int) getLength();
		strings.put(nextKey, bytes);
		lookup.put(string, nextKey);
		return nextKey;
	}

	/**
	 * Returns the string at the given offset.
	 *
	 * @param index string table offset
	 * @return decoded string at {@code index}
	 */
	public String get(int index) {
		Entry<Integer, byte[]> entry = strings.floorEntry(index);
		if (entry == null || index < 0) {
			throw new IndexOutOfBoundsException(index);
		}

		byte[] bytes = entry.getValue();
		int offset = index - entry.getKey();
		String string = charset.decode(ByteBuffer.wrap(bytes, offset, bytes.length - offset))
				.toString();
		if (!lookup.containsKey(string)) {
			lookup.put(string, index);
		}
		return string;
	}

	/**
	 * Finds the offset for a string.
	 *
	 * @param string string to look up
	 * @return offset of {@code string}
	 */
	public int find(String string) {
		Integer index = lookup.get(string);
		if (index == null) {
			throw new NoSuchElementException();
		}
		return index;
	}

	@Override
	public Iterator<Integer> iterator() {
		return strings.keySet().iterator();
	}

	/**
	 * Encodes a section name for use in a section header.
	 *
	 * @param name section name
	 * @return encoded 8-byte section name field
	 */
	public byte[] encodeSecName(String name) {
		byte[] bytes = name.getBytes(charset);
		if (bytes.length > 8) {
			bytes = ("/" + String.valueOf(find(name))).getBytes(charset);
		}

		if (bytes.length < 8) {
			bytes = Arrays.copyOf(bytes, 8);
		}

		return bytes;
	}

	/**
	 * Decodes a section name from a section header field.
	 *
	 * @param bytes encoded section name bytes
	 * @return decoded section name
	 * @throws IOException if an I/O error occurs while decoding
	 */
	public String decodeSecName(byte[] bytes) throws IOException {
		if (bytes[0] == '/') {
			ByteInputStream bis = ByteInputStream.asLittleEndian(bytes);
			bis.skip(1);

			String number = bis.readNullTerminatedString(charset);
			return get(Integer.parseInt(number, 10));
		} else {
			ByteInputStream bis = ByteInputStream.asLittleEndian(bytes);
			return bis.readNullTerminatedString(charset);
		}
	}

	/**
	 * Encodes a symbol name for use in a symbol record.
	 *
	 * @param name symbol name
	 * @return encoded 8-byte symbol name field
	 * @throws IOException if an I/O error occurs while encoding
	 */
	public byte[] encodeSymName(String name) throws IOException {
		byte[] bytes = name.getBytes(charset);
		if (bytes.length < 8) {
			bytes = Arrays.copyOf(bytes, 8);
		}

		if (bytes.length == 8) {
			return bytes;
		} else {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(8);
			DataOutput dataOutput = coff.wrap(outputStream);
			dataOutput.writeInt(0);
			dataOutput.writeInt(find(name));
			return outputStream.toByteArray();
		}
	}

	/**
	 * Decodes a symbol name from a symbol record.
	 *
	 * @param bytes encoded symbol name bytes
	 * @return decoded symbol name
	 * @throws IOException if an I/O error occurs while decoding
	 */
	public String decodeSymName(byte[] bytes) throws IOException {
		if (bytes[0] == 0) {
			InputStream inputStream = new ByteArrayInputStream(bytes, 4, 4);
			DataInput dataInput = coff.wrap(inputStream);
			return get(dataInput.readInt());
		} else {
			ByteInputStream bis = ByteInputStream.asLittleEndian(bytes);
			return bis.readNullTerminatedString(charset);
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
