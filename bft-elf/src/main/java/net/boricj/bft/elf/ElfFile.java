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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.elf.constants.ElfClass;
import net.boricj.bft.elf.constants.ElfData;
import net.boricj.bft.elf.constants.ElfMachine;
import net.boricj.bft.elf.constants.ElfOsAbi;
import net.boricj.bft.elf.constants.ElfType;
import net.boricj.bft.elf.constants.ElfVersion;
import net.boricj.bft.elf.sections.ElfStringTable;

public class ElfFile {
	private final ElfHeader header;
	protected ElfSectionTable sections;

	public static class Builder {
		protected final ElfClass ident_class;
		protected final ElfData ident_data;
		protected ElfVersion ident_version;
		protected final ElfOsAbi ident_osAbi;
		protected byte ident_abiVersion;

		protected final ElfType e_type;
		protected final ElfMachine e_machine;
		protected int e_version;
		protected long e_entry;
		protected int e_flags;
		protected short e_ehsize;
		protected short e_phentsize;
		protected short e_shentsize;

		public Builder(ElfClass class_, ElfData data, ElfOsAbi osAbi, ElfType type, ElfMachine machine) {
			this.ident_class = class_;
			this.ident_data = data;
			this.ident_version = ElfVersion.EV_CURRENT;
			this.ident_osAbi = osAbi;
			this.ident_abiVersion = 0;

			this.e_type = type;
			this.e_machine = machine;
			this.e_version = ElfVersion.EV_CURRENT.getValue();

			switch (class_) {
				case ELFCLASS32:
					this.e_ehsize = 52;
					this.e_phentsize = 32;
					this.e_shentsize = 40;
					break;
				case ELFCLASS64:
					this.e_ehsize = 64;
					this.e_phentsize = 56;
					this.e_shentsize = 64;
					break;
				default:
					break;
			}
		}

		public Builder setVersion(ElfVersion version) {
			this.ident_version = version;
			return this;
		}

		public Builder setAbiVersion(byte abiVersion) {
			this.ident_abiVersion = abiVersion;
			return this;
		}

		public Builder setVersion(int e_version) {
			this.e_version = e_version;
			return this;
		}

		public Builder setEntry(long e_entry) {
			this.e_entry = e_entry;
			return this;
		}

		public Builder setFlags(int e_flags) {
			this.e_flags = e_flags;
			return this;
		}

		public Builder setEhsize(short e_ehsize) {
			this.e_ehsize = e_ehsize;
			return this;
		}

		public Builder setPhentsize(short e_phentsize) {
			this.e_phentsize = e_phentsize;
			return this;
		}

		public Builder setShentsize(short e_shentsize) {
			this.e_shentsize = e_shentsize;
			return this;
		}

		public ElfFile build() {
			return new ElfFile(this);
		}
	}

	public static class Parser {
		private final FileInputStream fis;
		public Charset stringTableCharset;
		public boolean ignoreSectionErrors;

		public final List<Integer> sh_names = new ArrayList<>();
		public short e_phnum;
		public short e_shnum;
		public short e_shstrndx;

		public Parser(FileInputStream fis) {
			Objects.requireNonNull(fis);

			this.fis = fis;
			this.stringTableCharset = ElfStringTable.DEFAULT_CHARSET;
			this.ignoreSectionErrors = false;
		}

		public FileInputStream getFileInputStream() {
			return fis;
		}

		public Parser setStringTableCharset(Charset charset) {
			Objects.requireNonNull(charset);

			stringTableCharset = charset;
			return this;
		}

		public Parser setIgnoreSectionErrors(boolean error) {
			ignoreSectionErrors = error;
			return this;
		}

		public Charset getStringTableCharset() {
			return stringTableCharset;
		}

		public ElfFile parse() throws IOException {
			return new ElfFile(this);
		}
	}

	protected ElfFile(Builder builder) {
		this.header = new ElfHeader(this, builder);
	}

	protected ElfFile(Parser parser) throws IOException {
		this.header = new ElfHeader(this, parser);

		if (parser.e_shnum > 0) {
			this.sections = new ElfSectionTable(this, parser);

			// Ensure all sections are instanciated.
			for (short idx = 0; idx < parser.e_shnum; idx++) {
				try {
					this.sections.get(idx, parser);
				} catch (Exception ex) {
					if (!parser.ignoreSectionErrors) {
						throw ex;
					}
				}
			}

			if (parser.e_shstrndx != ElfSection.SHN_UNDEF) {
				ElfStringTable shstr = (ElfStringTable) this.sections.get(parser.e_shstrndx);
				this.header.setShStr(shstr);

				for (int index = 0; index < parser.e_shnum; index++) {
					String name = shstr.get(parser.sh_names.get(index));

					ElfSection section = this.sections.get(index);
					if (parser.ignoreSectionErrors && section == null) {
						continue;
					}
					section.setName(name);
				}
			}
		}
	}

	public ElfHeader getHeader() {
		return header;
	}

	public ElfSectionTable addSectionTable() {
		if (sections != null) {
			throw new IllegalStateException("ELF file already has a section table");
		}

		sections = new ElfSectionTable(this);
		return sections;
	}

	public ElfSectionTable getSections() {
		return sections;
	}

	public ByteOutputStream wrap(OutputStream outputStream) {
		return header.wrap(outputStream);
	}

	public ByteInputStream wrap(InputStream inputStream) {
		return header.wrap(inputStream);
	}
}
