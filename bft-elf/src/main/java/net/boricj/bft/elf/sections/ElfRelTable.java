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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.boricj.bft.IndirectList;
import net.boricj.bft.elf.ElfFile;
import net.boricj.bft.elf.ElfHeader;
import net.boricj.bft.elf.ElfSection;
import net.boricj.bft.elf.ElfSectionFlags;
import net.boricj.bft.elf.constants.ElfClass;
import net.boricj.bft.elf.constants.ElfMachine;
import net.boricj.bft.elf.constants.ElfRelocationType;
import net.boricj.bft.elf.constants.ElfSectionType;
import net.boricj.bft.elf.sections.ElfRelTable.ElfRel;
import net.boricj.bft.elf.sections.ElfSymbolTable.ElfSymbol;

/**
 * ELF relocation table section without explicit addends (REL format).
 * Relocations specify how to adjust addresses during linking.
 */
public class ElfRelTable extends ElfSection implements IndirectList<ElfRel> {
	/**
	 * An ELF relocation entry without explicit addend.
	 * The addend is stored in the location to be modified.
	 */
	public class ElfRel {
		private final long r_offset;
		private final ElfSymbol symbol;
		private final ElfRelocationType type;

		/**
		 * Creates a new relocation entry.
		 *
		 * @param r_offset offset where relocation should be applied
		 * @param symbol symbol referenced by this relocation
		 * @param type relocation type
		 */
		protected ElfRel(long r_offset, ElfSymbol symbol, ElfRelocationType type) {
			Objects.requireNonNull(symbol);
			Objects.requireNonNull(type);

			if (type.getMachine().getRelocationTypeClass() != type.getClass()) {
				throw new IllegalArgumentException("relocation type doesn't match ELF machine");
			}

			this.r_offset = r_offset;
			this.symbol = symbol;
			this.type = type;
		}

		/**
		 * Reads a relocation entry from input.
		 *
		 * @param dataInput data input to read from
		 * @param ident_class ELF class (32-bit or 64-bit)
		 * @param machine ELF machine type
		 * @throws IOException if an I/O error occurs
		 */
		protected ElfRel(DataInput dataInput, ElfClass ident_class, ElfMachine machine) throws IOException {
			long r_info;
			int symidx;
			int typeval;

			switch (ident_class) {
				case ELFCLASS32:
					this.r_offset = dataInput.readInt();
					r_info = dataInput.readInt();
					symidx = (int) (r_info >> 8);
					typeval = (int) (r_info & 0xFF);
					break;

				case ELFCLASS64:
					this.r_offset = dataInput.readLong();
					r_info = dataInput.readLong();
					symidx = (int) (r_info >> 32);
					typeval = (int) (r_info & 0xFFFFFFFF);
					break;

				default:
					throw new RuntimeException(ident_class.name());
			}

			this.symbol = symbolTable.get(symidx);

			Class<? extends ElfRelocationType> clazz = machine.getRelocationTypeClass();
			try {
				this.type = (ElfRelocationType)
						clazz.getMethod("valueFrom", Integer.TYPE).invoke(null, typeval);
			} catch (IllegalAccessException
					| InvocationTargetException
					| NoSuchMethodException
					| SecurityException ex) {
				throw new RuntimeException(ex);
			}
		}

		/**
		 * Writes this relocation entry to output.
		 *
		 * @param dataOutput data output to write to
		 * @param ident_class ELF class (32-bit or 64-bit)
		 * @throws IOException if an I/O error occurs
		 */
		protected void write(DataOutput dataOutput, ElfClass ident_class) throws IOException {
			long symidx = symbolTable.indexOf(symbol);
			long typeval = type.getValue();

			switch (ident_class) {
				case ELFCLASS32:
					dataOutput.writeInt((int) r_offset);
					dataOutput.writeInt((int) (symidx << 8 | typeval));
					break;

				case ELFCLASS64:
					dataOutput.writeLong(r_offset);
					dataOutput.writeLong(symidx << 32 | typeval);
					break;

				default:
					throw new RuntimeException(ident_class.name());
			}
		}

		/**
		 * Returns the offset where this relocation should be applied.
		 *
		 * @return the relocation offset
		 */
		public long getOffset() {
			return r_offset;
		}

		/**
		 * Returns the symbol referenced by this relocation.
		 *
		 * @return the symbol
		 */
		public ElfSymbol getSymbol() {
			return symbol;
		}

		/**
		 * Returns the relocation type.
		 *
		 * @return the relocation type
		 */
		public ElfRelocationType getType() {
			return type;
		}
	}

	private final List<ElfRel> relocations = new ArrayList<>();
	private final ElfSymbolTable symbolTable;
	private final ElfSection section;

	/**
	 * Creates a new relocation table with default settings.
	 *
	 * @param elf parent ELF file
	 * @param name section name
	 * @param symbolTable symbol table referenced by relocations
	 * @param section section to which relocations apply
	 */
	public ElfRelTable(ElfFile elf, String name, ElfSymbolTable symbolTable, ElfSection section) {
		this(
				elf,
				name,
				new ElfSectionFlags().infoLink(),
				0,
				0,
				computeAddrAlign(elf),
				computeEntSize(elf),
				symbolTable,
				section);
	}

	/**
	 * Creates a new relocation table with specified settings.
	 *
	 * @param elf parent ELF file
	 * @param name section name
	 * @param flags section flags
	 * @param addr section virtual address
	 * @param offset section file offset
	 * @param addralign section alignment
	 * @param entsize entry size
	 * @param symbolTable symbol table referenced by relocations
	 * @param section section to which relocations apply
	 */
	public ElfRelTable(
			ElfFile elf,
			String name,
			ElfSectionFlags flags,
			long addr,
			long offset,
			long addralign,
			long entsize,
			ElfSymbolTable symbolTable,
			ElfSection section) {
		super(elf, name, flags, addr, offset, addralign, entsize);

		Objects.requireNonNull(symbolTable);
		Objects.requireNonNull(section);

		if (elf != symbolTable.getElfFile()) {
			throw new RuntimeException("symbol table and relocation table don't belong to the same ELF file");
		}
		if (elf != section.getElfFile()) {
			throw new RuntimeException("section and relocation table don't belong to the same ELF file");
		}

		this.symbolTable = symbolTable;
		this.section = section;
	}

	/**
	 * Reads a relocation table from an ELF file.
	 *
	 * @param elf parent ELF file
	 * @param parser ELF file parser
	 * @param flags section flags
	 * @param addr section virtual address
	 * @param offset section file offset
	 * @param size section size in bytes
	 * @param link index of associated symbol table
	 * @param info index of section to which relocations apply
	 * @param addralign section alignment
	 * @param entsize entry size
	 * @throws IOException if an I/O error occurs
	 */
	public ElfRelTable(
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

		this.symbolTable = (ElfSymbolTable) elf.getSections().get(link, parser);
		this.section = elf.getSections().get(info, parser);

		FileInputStream fis = parser.getFileInputStream();
		ElfHeader header = elf.getHeader();
		ElfClass ident_class = header.getIdentClass();
		ElfMachine machine = header.getMachine();

		fis.getChannel().position(offset);
		DataInput dataInput = elf.wrap(fis);

		for (int i = 0; i < size / entsize; i++) {
			relocations.add(new ElfRel(dataInput, ident_class, machine));
		}
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		ElfFile elf = getElfFile();
		ElfHeader header = elf.getHeader();
		DataOutput dataOutput = elf.wrap(outputStream);

		ElfClass ident_class = header.getIdentClass();
		for (ElfRel relocation : this) {
			relocation.write(dataOutput, ident_class);
		}
	}

	@Override
	public long getLength() {
		return size() * getEntSize();
	}

	@Override
	public List<ElfRel> getElements() {
		return Collections.unmodifiableList(relocations);
	}

	/**
	 * Adds a new relocation entry to this table.
	 *
	 * @param r_offset offset where relocation should be applied
	 * @param symbol symbol referenced by this relocation
	 * @param type relocation type
	 * @return the created relocation entry
	 */
	public ElfRel add(long r_offset, ElfSymbol symbol, ElfRelocationType type) {
		ElfRel relocation = new ElfRel(r_offset, symbol, type);
		relocations.add(relocation);
		return relocation;
	}

	@Override
	public int getType() {
		return ElfSectionType.SHT_REL.getValue();
	}

	@Override
	public int getLink() {
		return getElfFile().getSections().indexOf(symbolTable);
	}

	@Override
	public int getInfo() {
		return getElfFile().getSections().indexOf(section);
	}

	/**
	 * Returns the section to which relocations in this table apply.
	 *
	 * @return the target section
	 */
	public ElfSection getSection() {
		return section;
	}

	/**
	 * Returns the symbol table referenced by relocations in this table.
	 *
	 * @return the symbol table
	 */
	public ElfSymbolTable getSymbolTable() {
		return symbolTable;
	}

	private static long computeAddrAlign(ElfFile file) {
		ElfClass ident_class = file.getHeader().getIdentClass();
		switch (ident_class) {
			case ELFCLASS32:
				return 4;

			case ELFCLASS64:
				return 8;

			default:
				throw new RuntimeException(ident_class.name());
		}
	}

	private static long computeEntSize(ElfFile file) {
		ElfClass ident_class = file.getHeader().getIdentClass();
		switch (ident_class) {
			case ELFCLASS32:
				return 8;

			case ELFCLASS64:
				return 16;

			default:
				throw new RuntimeException(ident_class.name());
		}
	}
}
