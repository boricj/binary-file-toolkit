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
package net.boricj.bft.elf.sections;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.TreeMap;

import net.boricj.bft.elf.ElfFile;
import net.boricj.bft.elf.ElfSection;
import net.boricj.bft.elf.ElfSectionFlags;
import net.boricj.bft.elf.constants.ElfSectionType;

public class ElfStringTable extends ElfSection implements Iterable<Integer> {
	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	private final TreeMap<Integer, byte[]> strings = new TreeMap<>();
	private final Map<String, Integer> lookup = new HashMap<>();
	private final Charset charset;

	public ElfStringTable(ElfFile elf, String name) {
		this(elf, name, new ElfSectionFlags(), 1, 0);
	}

	public ElfStringTable(ElfFile elf, String name, ElfSectionFlags flags, long addralign, long entsize) {
		this(elf, name, flags, 0, 0, addralign, entsize, DEFAULT_CHARSET);
	}

	public ElfStringTable(
			ElfFile elf,
			String name,
			ElfSectionFlags flags,
			long addr,
			long offset,
			long addralign,
			long entsize,
			Charset charset) {
		super(elf, name, flags, addr, offset, addralign, entsize);

		Objects.requireNonNull(charset);

		this.charset = charset;
	}

	public ElfStringTable(
			ElfFile elf,
			ElfFile.Parser parser,
			ElfSectionFlags flags,
			long addr,
			long offset,
			long size,
			int link,
			int info,
			long addralign,
			long entsize)
			throws IOException {
		this(elf, "", flags, addr, offset, addralign, entsize, parser.stringTableCharset);

		FileInputStream fis = parser.getFileInputStream();
		fis.getChannel().position(offset);

		byte[] bytes = new byte[(int) size];
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
	public void write(OutputStream outputStream) throws IOException {
		for (Entry<Integer, byte[]> entry : strings.entrySet()) {
			outputStream.write(entry.getValue());
			outputStream.write(0x00);
		}
	}

	public int add(String string) {
		Objects.requireNonNull(string);

		if (strings.isEmpty() && !string.isEmpty()) {
			throw new IllegalStateException("string table must start with an empty string");
		}

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

	public int add(String alias, String target) {
		Objects.requireNonNull(alias);
		Objects.requireNonNull(target);

		if (!target.endsWith(alias)) {
			throw new IllegalArgumentException("alias string doesn't end with target string");
		}

		Integer targetValue = lookup.get(target);
		if (targetValue == null) {
			throw new IllegalArgumentException("target string isn't part of the string table");
		}
		int nextKey = targetValue + target.getBytes(charset).length - alias.getBytes(charset).length;
		lookup.put(alias, nextKey);
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

	@Override
	public int getType() {
		return ElfSectionType.SHT_STRTAB.getValue();
	}

	@Override
	public long getLength() {
		if (strings.isEmpty()) {
			return 0;
		}

		Entry<Integer, byte[]> lastEntry = strings.lastEntry();
		return lastEntry.getKey() + lastEntry.getValue().length + 1;
	}
}
