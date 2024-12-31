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

import net.boricj.bft.Writable;

import static net.boricj.bft.Utils.decodeNullTerminatedString;

public class CoffStringTable implements Iterable<Integer>, Writable {
	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	private final CoffFile coff;
	private final TreeMap<Integer, byte[]> strings = new TreeMap<>();
	private final Map<String, Integer> lookup = new HashMap<>();
	private final Charset charset;

	protected CoffStringTable(CoffFile coff, CoffFile.Builder builder) {
		this.coff = coff;
		this.charset = builder.getStringTableCharset();
	}

	protected CoffStringTable(CoffFile coff, CoffFile.Parser parser) throws IOException {
		this.coff = coff;
		this.charset = parser.charset;

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

	public String decodeSecName(byte[] bytes) throws IOException {
		if (bytes[0] == '/') {
			String number = decodeNullTerminatedString(Arrays.copyOfRange(bytes, 1, bytes.length), charset);
			return get(Integer.parseInt(number, 10));
		} else {
			return decodeNullTerminatedString(bytes, charset);
		}
	}

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

	public String decodeSymName(byte[] bytes) throws IOException {
		if (bytes[0] == 0) {
			InputStream inputStream = new ByteArrayInputStream(bytes, 4, 4);
			DataInput dataInput = coff.wrap(inputStream);
			return get(dataInput.readInt());
		} else {
			return decodeNullTerminatedString(bytes, charset);
		}
	}
}
