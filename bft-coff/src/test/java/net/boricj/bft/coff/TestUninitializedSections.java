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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import net.boricj.bft.TestUtils;
import net.boricj.bft.Writable;
import net.boricj.bft.coff.constants.CoffMachine;
import net.boricj.bft.coff.constants.CoffSectionFlags;
import net.boricj.bft.coff.sections.CoffUninitialized;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class TestUninitializedSections {
	@Test
	public void testSerializeAndParseUninitializedSection() throws IOException {
		CoffFile coff = new CoffFile.Builder(CoffMachine.IMAGE_FILE_MACHINE_I386).build();
		CoffSectionTable sectionTable = coff.getSections();
		CoffSymbolTable symbolTable = coff.getSymbols();

		CoffSectionFlags characteristics = new CoffSectionFlags()
				.cntUninitializedData()
				.alignBytes(4)
				.memRead()
				.memWrite();
		CoffSection bss = new CoffUninitialized(coff, ".bss", characteristics, 0x20);
		sectionTable.add(bss);
		symbolTable.setOffset(coff.getHeader().getLength() + sectionTable.getLength());

		byte[] serialized = serialize(coff);

		Path file = Files.createTempFile("coff-bss", ".obj");
		Files.write(file, serialized);
		CoffFile parsed;
		try (FileInputStream fis = new FileInputStream(file.toFile())) {
			parsed = new CoffFile.Parser(fis).parse();
		}

		CoffSection parsedBss = parsed.getSections().get(1);
		assertInstanceOf(CoffUninitialized.class, parsedBss);
		assertEquals(0x20, parsedBss.getVirtualSize());
		assertEquals(0L, parsedBss.getLength());

		byte[] reparsedSerialized = serialize(parsed);
		TestUtils.assertArrayEquals(serialized, reparsedSerialized);
	}

	@Test
	public void testSectionSymbolAuxUsesVirtualSizeForUninitializedSection() throws IOException {
		CoffFile coff = new CoffFile.Builder(CoffMachine.IMAGE_FILE_MACHINE_I386).build();
		CoffSectionTable sectionTable = coff.getSections();
		CoffSymbolTable symbolTable = coff.getSymbols();

		CoffSectionFlags characteristics = new CoffSectionFlags()
				.cntUninitializedData()
				.alignBytes(4)
				.memRead()
				.memWrite();
		CoffSection bss = new CoffUninitialized(coff, ".bss", characteristics, 0x40);
		sectionTable.add(bss);
		symbolTable.addSection(bss);
		symbolTable.setOffset(coff.getHeader().getLength() + sectionTable.getLength());

		byte[] serialized = serialize(coff);

		int symbolTableOffset = (int) symbolTable.getOffset();
		ByteBuffer aux = ByteBuffer.wrap(
						serialized, symbolTableOffset + CoffSymbolTable.RECORD_LENGTH, CoffSymbolTable.RECORD_LENGTH)
				.order(ByteOrder.LITTLE_ENDIAN);
		assertEquals(0x40, aux.getInt());
	}

	private static byte[] serialize(CoffFile coff) throws IOException {
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

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Writable.write(writables, output);
		return output.toByteArray();
	}
}
