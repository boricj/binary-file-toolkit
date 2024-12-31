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
package net.boricj.bft.coff.i386;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import net.boricj.bft.TestUtils;
import net.boricj.bft.Writable;
import net.boricj.bft.coff.CoffFile;
import net.boricj.bft.coff.CoffHeader;
import net.boricj.bft.coff.CoffSectionTable;
import net.boricj.bft.coff.CoffStringTable;
import net.boricj.bft.coff.CoffSymbolTable;

public class TestParser {
	@Test
	public void test_hello_world_djgpp() throws IOException, URISyntaxException {
		final Map<Integer, byte[]> patches = Map.ofEntries(
				Map.entry(
						0x152,
						new byte[] {(byte) 0x3E, (byte) 0x00, (byte) 0x00, (byte) 0x00}), // .text section symbol length
				Map.entry(0x1be, new byte[] {(byte) 0x12, (byte) 0x00, (byte) 0x00, (byte) 0x00
				}) // .comment section symbol length
				);

		compareObjectFiles("hello-world_djgpp.obj", patches);
	}

	@Test
	public void test_hello_world_i686_w64_mingw32_gcc() throws IOException, URISyntaxException {
		final Map<Integer, byte[]> patches = Map.ofEntries(
				Map.entry(
						0x0b4,
						new byte[] {(byte) '/', (byte) '4', (byte) 0x00, (byte) 0x00}), // .rdata$zzz section entry name
				Map.entry(
						0x19c,
						new byte[] {(byte) 0x21, (byte) 0x00, (byte) 0x00, (byte) 0x00}), // .text symbol section length
				Map.entry(
						0x208,
						new byte[] {(byte) 0x0E, (byte) 0x00, (byte) 0x00, (byte) 0x00}) // .rdata symbol section length
				);

		compareObjectFiles("hello-world_i686-w64-mingw32-gcc.obj", patches);
	}

	@Test
	public void test_hello_world_i686_pc_windows_msvc() throws IOException, URISyntaxException {
		final Map<Integer, byte[]> patches = Map.ofEntries(
				Map.entry(0x2dd, new byte[] {(byte) 0x1C, (byte) 0xD9, (byte) 0x29, (byte) 0xF8
				}), // .text$mn section symbol checksum
				Map.entry(0x325, new byte[] {(byte) 0xC1, (byte) 0x37, (byte) 0x3B, (byte) 0x4A
				}) // .data section symbol checksum
				);

		compareObjectFiles("hello-world_i686-pc-windows-msvc.obj", patches);
	}

	@Test
	public void test_standard_relocations_i686_pc_windows_msvc() throws IOException, URISyntaxException {
		compareObjectFiles("standard-relocations_i686-pc-windows-msvc.obj");
	}

	@Test
	public void test_extended_relocations_i686_pc_windows_msvc() throws IOException, URISyntaxException {
		compareObjectFiles("extended-relocations_i686-pc-windows-msvc.obj");
	}

	private void compareObjectFiles(String name) throws IOException, URISyntaxException {
		compareObjectFiles(name, Collections.emptyMap());
	}

	private void compareObjectFiles(String name, Map<Integer, byte[]> patches) throws IOException, URISyntaxException {
		File file = new File(getClass().getResource(name).toURI());
		CoffFile coff = new CoffFile.Parser(new FileInputStream(file)).parse();

		CoffHeader header = coff.getHeader();
		CoffSectionTable sectionTable = coff.getSections();
		CoffSymbolTable symbolTable = coff.getSymbols();
		CoffStringTable stringTable = coff.getStrings();

		Stream<Writable> streamWritables = Stream.of(
						List.of(header, sectionTable, symbolTable, stringTable).stream(),
						sectionTable.stream(),
						sectionTable.stream().map(s -> s.getRelocations()))
				.flatMap(Function.identity());
		Collection<Writable> writables = streamWritables.collect(Collectors.toList());

		TestUtils.compare(getClass().getResourceAsStream(name), writables, patches);
	}
}
