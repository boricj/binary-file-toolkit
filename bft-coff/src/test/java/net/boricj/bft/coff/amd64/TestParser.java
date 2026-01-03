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
package net.boricj.bft.coff.amd64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
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
	public void test_hello_world_amd64_pc_windows_msvc() throws IOException, URISyntaxException {
		final Map<Integer, byte[]> patches = Map.ofEntries(
				Map.entry(0x322, new byte[] {(byte) 0xBE, (byte) 0x6E, (byte) 0x84, (byte) 0x01
				}), // .text$mn section symbol checksum
				Map.entry(0x37C, new byte[] {(byte) 0xD1, (byte) 0x39, (byte) 0xC5, (byte) 0x0F
				}), // .xdata section symbol checksum
				Map.entry(0x3B2, new byte[] {(byte) 0x32, (byte) 0x38, (byte) 0x7E, (byte) 0x76
				}), // .pdata section symbol checksum
				Map.entry(0x3E8, new byte[] {(byte) 0x5D, (byte) 0x0E, (byte) 0x86, (byte) 0x85
				}) // .data section symbol checksum
				);

		compareObjectFiles("hello-world_amd64-pc-windows-msvc.obj", patches);
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
