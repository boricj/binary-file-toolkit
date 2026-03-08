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

/**
 * Represents an ELF (Executable and Linkable Format) file.
 *
 * <p>This class provides functionality to parse existing ELF files and build new ones.
 * ELF files contain a header, section table, and optionally program headers.
 */
public class ElfFile {
	private final ElfHeader header;
	/** Section table containing all sections in the ELF file. */
	protected ElfSectionTable sections;

	/**
	 * Builder for constructing ELF files programmatically.
	 */
	public static class Builder {
		/** ELF file class (32-bit or 64-bit). */
		protected final ElfClass ident_class;
		/** ELF data encoding (little-endian or big-endian). */
		protected final ElfData ident_data;
		/** ELF identification version. */
		protected ElfVersion ident_version;
		/** Target OS ABI. */
		protected final ElfOsAbi ident_osAbi;
		/** ABI version byte. */
		protected byte ident_abiVersion;

		/** ELF object file type. */
		protected final ElfType e_type;
		/** Target machine architecture. */
		protected final ElfMachine e_machine;
		/** ELF version field. */
		protected int e_version;
		/** Entry point virtual address. */
		protected long e_entry;
		/** Processor-specific flags. */
		protected int e_flags;
		/** ELF header size. */
		protected short e_ehsize;
		/** Program header entry size. */
		protected short e_phentsize;
		/** Section header entry size. */
		protected short e_shentsize;

		/**
		 * Creates a new ELF file builder for the specified configuration.
		 *
		 * @param class_ ELF file class (32-bit or 64-bit)
		 * @param data data encoding (little-endian or big-endian)
		 * @param osAbi target OS ABI
		 * @param type ELF object file type
		 * @param machine target machine architecture
		 */
		public Builder(ElfClass class_, ElfData data, ElfOsAbi osAbi, ElfType type, ElfMachine machine) {
			Objects.requireNonNull(class_);
			Objects.requireNonNull(data);
			Objects.requireNonNull(osAbi);
			Objects.requireNonNull(type);
			Objects.requireNonNull(machine);

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

		/**
		 * Sets the ELF identification version.
		 *
		 * @param version ELF identification version
		 * @return this builder
		 */
		public Builder setVersion(ElfVersion version) {
			this.ident_version = version;
			return this;
		}

		/**
		 * Sets the ELF ABI version.
		 *
		 * @param abiVersion ABI version byte
		 * @return this builder
		 */
		public Builder setAbiVersion(byte abiVersion) {
			this.ident_abiVersion = abiVersion;
			return this;
		}

		/**
		 * Sets the ELF header version field.
		 *
		 * @param e_version ELF header version value
		 * @return this builder
		 */
		public Builder setVersion(int e_version) {
			this.e_version = e_version;
			return this;
		}

		/**
		 * Sets the ELF entry point address.
		 *
		 * @param e_entry entry point address
		 * @return this builder
		 */
		public Builder setEntry(long e_entry) {
			this.e_entry = e_entry;
			return this;
		}

		/**
		 * Sets processor-specific ELF header flags.
		 *
		 * @param e_flags ELF flags value
		 * @return this builder
		 */
		public Builder setFlags(int e_flags) {
			this.e_flags = e_flags;
			return this;
		}

		/**
		 * Sets the ELF header size value.
		 *
		 * @param e_ehsize ELF header size
		 * @return this builder
		 */
		public Builder setEhsize(short e_ehsize) {
			this.e_ehsize = e_ehsize;
			return this;
		}

		/**
		 * Sets the program header entry size value.
		 *
		 * @param e_phentsize program header entry size
		 * @return this builder
		 */
		public Builder setPhentsize(short e_phentsize) {
			this.e_phentsize = e_phentsize;
			return this;
		}

		/**
		 * Sets the section header entry size value.
		 *
		 * @param e_shentsize section header entry size
		 * @return this builder
		 */
		public Builder setShentsize(short e_shentsize) {
			this.e_shentsize = e_shentsize;
			return this;
		}

		/**
		 * Builds an ELF file from this builder configuration.
		 *
		 * @return constructed ELF file
		 */
		public ElfFile build() {
			return new ElfFile(this);
		}
	}

	/**
	 * Parser for reading ELF files from an input stream.
	 */
	public static class Parser {
		private final FileInputStream fis;
		private Charset charset = ElfStringTable.DEFAULT_CHARSET;
		boolean ignoreSectionErrors = false;

		final List<Integer> sh_names = new ArrayList<>();
		short e_phnum;
		short e_shnum;
		short e_shstrndx;

		/**
		 * Creates a new ELF file parser for the given input stream.
		 *
		 * @param fis input stream positioned at the start of an ELF file
		 */
		public Parser(FileInputStream fis) {
			Objects.requireNonNull(fis);

			this.fis = fis;
		}

		/**
		 * Returns the input stream used for parsing.
		 *
		 * @return the file input stream
		 */
		public FileInputStream getFileInputStream() {
			return fis;
		}

		/**
		 * Returns the charset used for ELF string tables.
		 *
		 * @return string table charset
		 */
		public Charset getCharset() {
			return charset;
		}

		/**
		 * Sets the charset used to decode and encode ELF string tables.
		 *
		 * @param charset string table charset
		 * @return this parser
		 */
		public Parser setCharset(Charset charset) {
			Objects.requireNonNull(charset);

			this.charset = charset;
			return this;
		}

		/**
		 * Enables or disables tolerance for section parsing errors.
		 *
		 * @param error true to continue when section parsing fails, false to propagate errors
		 * @return this parser
		 */
		public Parser setIgnoreSectionErrors(boolean error) {
			ignoreSectionErrors = error;
			return this;
		}

		/**
		 * Parses and returns an ELF file from the configured input stream.
		 *
		 * @return parsed ELF file
		 * @throws IOException if reading from the input stream fails
		 */
		public ElfFile parse() throws IOException {
			return new ElfFile(this);
		}
	}

	/**
	 * Constructs an ELF file from a builder configuration.
	 *
	 * @param builder builder containing ELF file configuration
	 */
	protected ElfFile(Builder builder) {
		this.header = new ElfHeader(this, builder);
	}

	/**
	 * Constructs an ELF file by parsing from an input stream.
	 *
	 * @param parser parser containing input stream and configuration
	 * @throws IOException if reading from the input stream fails
	 */
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

	/**
	 * Returns the ELF header.
	 *
	 * @return the ELF header
	 */
	public ElfHeader getHeader() {
		return header;
	}

	/**
	 * Creates and attaches an empty section table.
	 *
	 * @return the created section table
	 * @throws IllegalStateException if a section table is already present
	 */
	public ElfSectionTable addSectionTable() {
		if (sections != null) {
			throw new IllegalStateException("ELF file already has a section table");
		}

		sections = new ElfSectionTable(this);
		return sections;
	}

	/**
	 * Returns the section table.
	 *
	 * @return section table, or null when the file has no section table
	 */
	public ElfSectionTable getSections() {
		return sections;
	}

	/**
	 * Wraps an output stream using the ELF file endianness.
	 *
	 * @param outputStream output stream to wrap
	 * @return wrapped output stream
	 */
	public ByteOutputStream wrap(OutputStream outputStream) {
		return header.wrap(outputStream);
	}

	/**
	 * Wraps an input stream using the ELF file endianness.
	 *
	 * @param inputStream input stream to wrap
	 * @return wrapped input stream
	 */
	public ByteInputStream wrap(InputStream inputStream) {
		return header.wrap(inputStream);
	}
}
