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
import java.util.Objects;

import net.boricj.bft.IndirectList;
import net.boricj.bft.elf.ElfFile;
import net.boricj.bft.elf.ElfHeader;
import net.boricj.bft.elf.ElfSection;
import net.boricj.bft.elf.ElfSectionFlags;
import net.boricj.bft.elf.ElfSectionTable;
import net.boricj.bft.elf.constants.ElfClass;
import net.boricj.bft.elf.constants.ElfSectionType;
import net.boricj.bft.elf.constants.ElfSymbolBinding;
import net.boricj.bft.elf.constants.ElfSymbolType;
import net.boricj.bft.elf.constants.ElfSymbolVisibility;
import net.boricj.bft.elf.sections.ElfSymbolTable.ElfSymbol;

import static net.boricj.bft.elf.constants.ElfSymbolBinding.STB_GLOBAL;
import static net.boricj.bft.elf.constants.ElfSymbolBinding.STB_LOCAL;
import static net.boricj.bft.elf.constants.ElfSymbolType.STT_FILE;
import static net.boricj.bft.elf.constants.ElfSymbolType.STT_NOTYPE;
import static net.boricj.bft.elf.constants.ElfSymbolType.STT_SECTION;
import static net.boricj.bft.elf.constants.ElfSymbolVisibility.STV_DEFAULT;

public class ElfSymbolTable extends ElfSection implements IndirectList<ElfSymbol> {
	public class ElfSymbol {
		private final String name;
		private final long st_value;
		private final long st_size;
		private final ElfSymbolType type;
		private final ElfSymbolVisibility visibility;
		private final ElfSymbolBinding binding;
		private final short st_shndx;

		protected ElfSymbol(
				String name,
				long st_value,
				long st_size,
				ElfSymbolType type,
				ElfSymbolVisibility visibility,
				ElfSymbolBinding binding,
				short st_shndx) {
			Objects.requireNonNull(name);
			Objects.requireNonNull(type);
			Objects.requireNonNull(visibility);
			Objects.requireNonNull(binding);

			this.name = name;
			this.st_value = st_value;
			this.st_size = st_size;
			this.type = type;
			this.visibility = visibility;
			this.binding = binding;
			this.st_shndx = st_shndx;
		}

		protected ElfSymbol(DataInput dataInput, ElfClass ident_class) throws IOException {
			int st_name;
			byte st_info;
			byte st_other;

			switch (ident_class) {
				case ELFCLASS32:
					st_name = dataInput.readInt();
					this.st_value = dataInput.readInt();
					this.st_size = dataInput.readInt();
					st_info = dataInput.readByte();
					st_other = dataInput.readByte();
					this.st_shndx = dataInput.readShort();
					break;

				case ELFCLASS64:
					st_name = dataInput.readInt();
					st_info = dataInput.readByte();
					st_other = dataInput.readByte();
					this.st_shndx = dataInput.readShort();
					this.st_value = dataInput.readLong();
					this.st_size = dataInput.readLong();
					break;

				default:
					throw new RuntimeException(ident_class.name());
			}

			byte st_type = (byte) (st_info & 0x0F);
			byte st_binding = (byte) (st_info >> 4);

			this.name = stringTable.get(st_name);
			this.type = ElfSymbolType.valueFrom(st_type);
			this.binding = ElfSymbolBinding.valueFrom(st_binding);
			this.visibility = ElfSymbolVisibility.valueFrom(st_other);
		}

		protected void write(DataOutput dataOutput, ElfClass ident_class) throws IOException {
			int st_name = stringTable.find(name);
			byte info = (byte) (type.getValue() | (binding.getValue() << 4));
			byte other = visibility.getValue();

			switch (ident_class) {
				case ELFCLASS32:
					dataOutput.writeInt(st_name);
					dataOutput.writeInt((int) st_value);
					dataOutput.writeInt((int) st_size);
					dataOutput.writeByte(info);
					dataOutput.writeByte(other);
					dataOutput.writeShort(st_shndx);
					break;

				case ELFCLASS64:
					dataOutput.writeInt(st_name);
					dataOutput.writeByte(info);
					dataOutput.writeByte(other);
					dataOutput.writeShort(st_shndx);
					dataOutput.writeLong(st_value);
					dataOutput.writeLong(st_size);
					break;

				default:
					throw new RuntimeException(ident_class.name());
			}
		}

		public String getName() {
			return name;
		}

		public long getValue() {
			return st_value;
		}

		public long getSize() {
			return st_size;
		}

		public ElfSymbolType getType() {
			return type;
		}

		public ElfSymbolVisibility getVisibility() {
			return visibility;
		}

		public ElfSymbolBinding getBinding() {
			return binding;
		}

		public short getIndex() {
			return st_shndx;
		}
	}

	private final List<ElfSymbol> symbols = new ArrayList<>();
	private final ElfStringTable stringTable;

	public ElfSymbolTable(ElfFile elf, String name, ElfStringTable stringTable) {
		this(elf, name, new ElfSectionFlags(), 0, 0, computeAddrAlign(elf), computeEntSize(elf), stringTable);
	}

	public ElfSymbolTable(
			ElfFile elf,
			String name,
			ElfSectionFlags flags,
			long addr,
			long offset,
			long addralign,
			long entsize,
			ElfStringTable stringTable) {
		super(elf, name, flags, addr, offset, addralign, entsize);

		Objects.requireNonNull(stringTable);

		if (elf != stringTable.getElfFile()) {
			throw new RuntimeException("symbol table and string table don't belong to the same ELF file");
		}

		this.stringTable = stringTable;
	}

	public ElfSymbolTable(
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
		this.stringTable = (ElfStringTable) sectionTable.get(link, parser);

		FileInputStream fis = parser.getFileInputStream();
		ElfHeader header = elf.getHeader();
		ElfClass ident_class = header.getIdentClass();

		fis.getChannel().position(offset);
		DataInput dataInput = elf.wrap(fis);

		for (int i = 0; i < size / entsize; i++) {
			symbols.add(new ElfSymbol(dataInput, ident_class));
		}
	}

	@Override
	public int getType() {
		return ElfSectionType.SHT_SYMTAB.getValue();
	}

	public ElfSymbol add(
			String st_name,
			long st_value,
			long st_size,
			ElfSymbolType type,
			ElfSymbolVisibility visibility,
			ElfSymbolBinding binding,
			ElfSection st_sh) {
		short st_shndx = (short) getElfFile().getSections().indexOf(st_sh);
		return add(st_name, st_value, st_size, type, visibility, binding, st_shndx);
	}

	public ElfSymbol add(
			String st_name,
			long st_value,
			long st_size,
			ElfSymbolType type,
			ElfSymbolVisibility visibility,
			ElfSymbolBinding binding,
			short st_shndx) {
		ElfSymbol symbol = new ElfSymbol(st_name, st_value, st_size, type, visibility, binding, st_shndx);
		symbols.add(symbol);
		return symbol;
	}

	public ElfSymbol addNull() {
		return add("", 0, 0, STT_NOTYPE, STV_DEFAULT, STB_LOCAL, (short) SHN_UNDEF);
	}

	public ElfSymbol addFile(String value) {
		return add(value, 0, 0, STT_FILE, STV_DEFAULT, STB_LOCAL, (short) SHN_ABS);
	}

	public ElfSymbol addSection(ElfSection section) {
		return add("", 0, 0, STT_SECTION, STV_DEFAULT, STB_LOCAL, section);
	}

	public ElfSymbol addDefined(
			String name, long offset, long size, ElfSymbolType type, ElfSymbolBinding binding, ElfSection section) {
		return add(name, offset, size, type, STV_DEFAULT, binding, section);
	}

	public ElfSymbol addUndefined(String name) {
		return add(name, 0, 0, STT_NOTYPE, STV_DEFAULT, STB_GLOBAL, (short) SHN_UNDEF);
	}

	@Override
	public List<ElfSymbol> getElements() {
		return Collections.unmodifiableList(symbols);
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		DataOutput dataOutput = getElfFile().wrap(outputStream);
		ElfClass ident_class = getElfFile().getHeader().getIdentClass();

		for (ElfSymbol symbol : symbols) {
			symbol.write(dataOutput, ident_class);
		}
	}

	@Override
	public long getLength() {
		return symbols.size() * getEntSize();
	}

	@Override
	public int getLink() {
		return getElfFile().getSections().indexOf(stringTable);
	}

	@Override
	public int getInfo() {
		int info = 0;

		for (ElfSymbol symbol : symbols) {
			if (symbol.getBinding() != STB_LOCAL) {
				break;
			}

			info++;
		}

		return info;
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
				return 16;

			case ELFCLASS64:
				return 24;

			default:
				throw new RuntimeException(ident_class.name());
		}
	}
}
