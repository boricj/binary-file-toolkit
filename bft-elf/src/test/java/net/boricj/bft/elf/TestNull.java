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
package net.boricj.bft.elf;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.boricj.bft.TestUtils;
import net.boricj.bft.Writable;
import net.boricj.bft.elf.constants.ElfMachine;

import static net.boricj.bft.elf.constants.ElfClass.ELFCLASS32;
import static net.boricj.bft.elf.constants.ElfClass.ELFCLASS64;
import static net.boricj.bft.elf.constants.ElfData.ELFDATA2LSB;
import static net.boricj.bft.elf.constants.ElfData.ELFDATA2MSB;
import static net.boricj.bft.elf.constants.ElfOsAbi.ELFOSABI_NONE;
import static net.boricj.bft.elf.constants.ElfType.ET_NONE;

public class TestNull {
	@Test
	public void test_null_ELFCLASS32_ELFDATA2LSB() throws IOException, URISyntaxException {
		ElfFile elf = new ElfFile.Builder(ELFCLASS32, ELFDATA2LSB, ELFOSABI_NONE, ET_NONE, ElfMachine.EM_NONE).build();

		Collection<Writable> writables = List.of(elf.getHeader());

		TestUtils.compare(getClass().getResourceAsStream("null_ELFCLASS32_ELFDATA2LSB.o"), writables);
	}

	@Test
	public void test_null_ELFCLASS32_ELFDATA2MSB() throws IOException, URISyntaxException {
		ElfFile elf = new ElfFile.Builder(ELFCLASS32, ELFDATA2MSB, ELFOSABI_NONE, ET_NONE, ElfMachine.EM_NONE).build();

		Collection<Writable> writables = List.of(elf.getHeader());

		TestUtils.compare(getClass().getResourceAsStream("null_ELFCLASS32_ELFDATA2MSB.o"), writables);
	}

	@Test
	public void test_null_ELFCLASS64_ELFDATA2LSB() throws IOException, URISyntaxException {
		ElfFile elf = new ElfFile.Builder(ELFCLASS64, ELFDATA2LSB, ELFOSABI_NONE, ET_NONE, ElfMachine.EM_NONE).build();

		Collection<Writable> writables = List.of(elf.getHeader());

		TestUtils.compare(getClass().getResourceAsStream("null_ELFCLASS64_ELFDATA2LSB.o"), writables);
	}

	@Test
	public void test_null_ELFCLASS64_ELFDATA2MSB() throws IOException, URISyntaxException {
		ElfFile elf = new ElfFile.Builder(ELFCLASS64, ELFDATA2MSB, ELFOSABI_NONE, ET_NONE, ElfMachine.EM_NONE).build();

		Collection<Writable> writables = List.of(elf.getHeader());

		TestUtils.compare(getClass().getResourceAsStream("null_ELFCLASS64_ELFDATA2MSB.o"), writables);
	}
}
