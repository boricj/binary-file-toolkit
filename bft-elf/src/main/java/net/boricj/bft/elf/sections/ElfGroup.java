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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.boricj.bft.ImmutableList;
import net.boricj.bft.elf.ElfFile;
import net.boricj.bft.elf.ElfSection;
import net.boricj.bft.elf.ElfSectionFlags;
import net.boricj.bft.elf.ElfSectionTable;
import net.boricj.bft.elf.constants.ElfSectionType;
import net.boricj.bft.elf.sections.ElfSymbolTable.ElfSymbol;

public class ElfGroup extends ElfSection implements ImmutableList<ElfSection> {
	private final ElfSymbolTable symbolTable;
	private final ElfSymbol signature;
	private final int flags;
	private final List<ElfSection> sections = new ArrayList<>();

	public ElfGroup(
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
		super(elf, "", flags, addr, offset, addralign, entsize);

		ElfSectionTable sectionTable = elf.getSections();
		FileInputStream fis = parser.getFileInputStream();

		DataInput dataInput = elf.wrap(fis);

		this.symbolTable = (ElfSymbolTable) sectionTable.get(link, parser);
		this.signature = this.symbolTable.get(info);

		fis.getChannel().position(offset);
		this.flags = dataInput.readInt();

		for (int i = 0; i < (size - 4) / entsize; i++) {
			fis.getChannel().position(offset + 4 * i + 4);
			int shndx = dataInput.readInt();

			sections.add(sectionTable.get(shndx, parser));
		}
	}

	@Override
	public long getLength() {
		return 4 + 4 * sections.size();
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		ElfFile elf = getElfFile();
		ElfSectionTable sectionTable = elf.getSections();
		DataOutput dataOutput = elf.wrap(outputStream);

		dataOutput.writeInt(flags);
		for (ElfSection section : this) {
			dataOutput.writeInt(sectionTable.indexOf(section));
		}
	}

	public ElfSymbol getSignature() {
		return signature;
	}

	@Override
	public List<ElfSection> getElements() {
		return Collections.unmodifiableList(sections);
	}

	@Override
	public int getType() {
		return ElfSectionType.SHT_GROUP.getValue();
	}

	@Override
	public int getLink() {
		return getElfFile().getSections().indexOf(symbolTable);
	}

	@Override
	public int getInfo() {
		return symbolTable.indexOf(signature);
	}
}
