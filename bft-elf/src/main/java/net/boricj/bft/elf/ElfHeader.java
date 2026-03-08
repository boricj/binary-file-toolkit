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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.NoSuchElementException;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.Writable;
import net.boricj.bft.elf.constants.ElfClass;
import net.boricj.bft.elf.constants.ElfData;
import net.boricj.bft.elf.constants.ElfMachine;
import net.boricj.bft.elf.constants.ElfOsAbi;
import net.boricj.bft.elf.constants.ElfType;
import net.boricj.bft.elf.constants.ElfVersion;
import net.boricj.bft.elf.sections.ElfStringTable;

/**
 * ELF file header containing identification, machine type, entry point, and section table information.
 * The header appears at the start of an ELF object file.
 */
public class ElfHeader implements Writable {
	/** ELF magic number identifying ELF files (0x7F followed by 'ELF'). */
	public static final byte[] MAGIC = new byte[] {0x7f, 'E', 'L', 'F'};

	private final ElfFile elf;

	private final ElfClass ident_class;
	private final ElfData ident_data;
	private final ElfVersion ident_version;
	private final ElfOsAbi ident_osAbi;
	private final byte ident_abiVersion;

	private final ElfType e_type;
	private final ElfMachine e_machine;
	private final int e_version;
	private final long e_entry;
	private long e_phoff;
	private long e_shoff;
	private final int e_flags;
	private final short e_ehsize;
	private final short e_phentsize;
	private final short e_shentsize;
	private ElfStringTable e_shstr;

	/**
	 * Constructs an ELF header from a builder configuration.
	 *
	 * @param elf the parent ELF file
	 * @param builder builder containing header configuration
	 */
	protected ElfHeader(ElfFile elf, ElfFile.Builder builder) {
		this.elf = elf;

		this.ident_class = builder.ident_class;
		this.ident_data = builder.ident_data;
		this.ident_version = builder.ident_version;
		this.ident_osAbi = builder.ident_osAbi;
		this.ident_abiVersion = builder.ident_abiVersion;
		this.e_type = builder.e_type;
		this.e_machine = builder.e_machine;
		this.e_version = builder.e_version;
		this.e_entry = builder.e_entry;
		this.e_flags = builder.e_flags;
		this.e_ehsize = builder.e_ehsize;
		this.e_phentsize = builder.e_phentsize;
		this.e_shentsize = builder.e_shentsize;
	}

	/**
	 * Constructs an ELF header by parsing from an input stream.
	 *
	 * @param elf the parent ELF file
	 * @param parser parser containing input stream and configuration
	 * @throws IOException if reading from the input stream fails
	 */
	protected ElfHeader(ElfFile elf, ElfFile.Parser parser) throws IOException {
		this.elf = elf;

		FileInputStream fis = parser.getFileInputStream();
		byte[] magic = new byte[4];
		if (fis.read(magic) != magic.length) {
			throw new RuntimeException();
		}
		if (!Arrays.equals(magic, MAGIC)) {
			throw new RuntimeException();
		}

		byte[] ident = new byte[12];
		if (fis.read(ident) != ident.length) {
			throw new RuntimeException();
		}

		this.ident_class = ElfClass.valueFrom(ident[0]);
		this.ident_data = ElfData.valueFrom(ident[1]);
		this.ident_version = ElfVersion.valueFrom(ident[2]);
		this.ident_osAbi = ElfOsAbi.valueFrom(ident[3]);
		this.ident_abiVersion = ident[4];

		DataInput dataInput = wrap(fis);
		this.e_type = ElfType.valueFrom(dataInput.readShort());
		this.e_machine = ElfMachine.valueFrom(dataInput.readShort());
		this.e_version = dataInput.readInt();

		switch (ident_class) {
			case ELFCLASS32:
				this.e_entry = dataInput.readInt();
				this.e_phoff = dataInput.readInt();
				this.e_shoff = dataInput.readInt();
				break;

			case ELFCLASS64:
				this.e_entry = dataInput.readLong();
				this.e_phoff = dataInput.readLong();
				this.e_shoff = dataInput.readLong();
				break;

			default:
				throw new RuntimeException(ident_class.name());
		}

		this.e_flags = dataInput.readInt();
		this.e_ehsize = dataInput.readShort();

		this.e_phentsize = dataInput.readShort();
		parser.e_phnum = dataInput.readShort();

		this.e_shentsize = dataInput.readShort();
		parser.e_shnum = dataInput.readShort();
		parser.e_shstrndx = dataInput.readShort();
	}

	/**
	 * Sets the section header string table.
	 *
	 * @param shstr the string table section, or null to clear
	 */
	public void setShStr(ElfStringTable shstr) {
		if (shstr != null) {
			if (!elf.getSections().contains(shstr)) {
				throw new NoSuchElementException("string table isn't inside section table");
			}
		}

		this.e_shstr = shstr;
	}

	/**
	 * Returns the ELF file class (32-bit or 64-bit).
	 *
	 * @return the ELF class
	 */
	public ElfClass getIdentClass() {
		return ident_class;
	}

	/**
	 * Returns the ELF data encoding (little-endian or big-endian).
	 *
	 * @return the data encoding
	 */
	public ElfData getIdentData() {
		return ident_data;
	}

	/**
	 * Returns the ELF identification version.
	 *
	 * @return the identification version
	 */
	public ElfVersion getIdentVersion() {
		return ident_version;
	}

	/**
	 * Returns the target OS ABI.
	 *
	 * @return the OS ABI
	 */
	public ElfOsAbi getIdentOsAbi() {
		return ident_osAbi;
	}

	/**
	 * Returns the ABI version byte.
	 *
	 * @return the ABI version
	 */
	public byte getIdentAbiVersion() {
		return ident_abiVersion;
	}

	/**
	 * Returns the ELF object file type.
	 *
	 * @return the file type
	 */
	public ElfType getType() {
		return e_type;
	}

	/**
	 * Returns the target machine architecture.
	 *
	 * @return the machine type
	 */
	public ElfMachine getMachine() {
		return e_machine;
	}

	/**
	 * Returns the ELF version field value.
	 *
	 * @return the version field
	 */
	public int getVersion() {
		return e_version;
	}

	/**
	 * Returns the entry point virtual address.
	 *
	 * @return the entry point address
	 */
	public long getEntry() {
		return e_entry;
	}

	/**
	 * Returns the program header table file offset.
	 *
	 * @return the program header offset
	 */
	public long getPhoff() {
		return e_phoff;
	}

	/**
	 * Sets the program header table file offset.
	 *
	 * @param offset new program header offset
	 */
	public void setPhoff(long offset) {
		e_phoff = offset;
	}

	/**
	 * Returns the section header table file offset.
	 *
	 * @return the section header offset
	 */
	public long getShoff() {
		return e_shoff;
	}

	/**
	 * Sets the section header table file offset.
	 *
	 * @param offset new section header offset
	 */
	public void setShoff(long offset) {
		e_shoff = offset;
	}

	/**
	 * Returns the processor-specific flags.
	 *
	 * @return the flags value
	 */
	public int getFlags() {
		return e_flags;
	}

	/**
	 * Returns the ELF header size.
	 *
	 * @return the header size in bytes
	 */
	public short getEhsize() {
		return e_ehsize;
	}

	/**
	 * Returns the program header entry size.
	 *
	 * @return the program header entry size in bytes
	 */
	public short getPhentsize() {
		return e_phentsize;
	}

	/**
	 * Returns the section header entry size.
	 *
	 * @return the section header entry size in bytes
	 */
	public short getShentsize() {
		return e_shentsize;
	}

	/**
	 * Returns the section header string table.
	 *
	 * @return the string table section, or null if not set
	 */
	public ElfStringTable getShstr() {
		return e_shstr;
	}

	@Override
	public long getOffset() {
		return 0;
	}

	@Override
	public long getLength() {
		return e_ehsize;
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		ElfSectionTable sectionTable = elf.getSections();

		short phnum = ElfSection.SHN_UNDEF;

		short shnum = ElfSection.SHN_UNDEF;
		short shstrndx = ElfSection.SHN_UNDEF;
		if (sectionTable != null) {
			shnum = (short) sectionTable.size();

			if (e_shstr != null) {
				shstrndx = (short) sectionTable.indexOf(e_shstr);
			}
		}

		DataOutput dataOutput = elf.wrap(outputStream);
		dataOutput.write(MAGIC);
		dataOutput.writeByte(ident_class.getValue());
		dataOutput.writeByte(ident_data.getValue());
		dataOutput.writeByte(ident_version.getValue());
		dataOutput.writeByte(ident_osAbi.getValue());
		dataOutput.writeByte(ident_abiVersion);
		dataOutput.write(new byte[7]);
		dataOutput.writeShort(e_type.getValue());
		dataOutput.writeShort(e_machine.getValue());
		dataOutput.writeInt(e_version);

		switch (ident_class) {
			case ELFCLASS32:
				dataOutput.writeInt((int) e_entry);
				dataOutput.writeInt((int) e_phoff);
				dataOutput.writeInt((int) e_shoff);
				break;

			case ELFCLASS64:
				dataOutput.writeLong(e_entry);
				dataOutput.writeLong(e_phoff);
				dataOutput.writeLong(e_shoff);
				break;

			default:
				throw new RuntimeException(ident_class.name());
		}

		dataOutput.writeInt(e_flags);
		dataOutput.writeShort(e_ehsize);

		dataOutput.writeShort(e_phentsize);
		dataOutput.writeShort(phnum);

		dataOutput.writeShort(e_shentsize);
		dataOutput.writeShort(shnum);
		dataOutput.writeShort(shstrndx);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	/**
	 * Wraps an output stream using the ELF file endianness.
	 *
	 * @param outputStream output stream to wrap
	 * @return wrapped output stream with correct endianness
	 */
	protected ByteOutputStream wrap(OutputStream outputStream) {
		switch (ident_data) {
			case ELFDATA2LSB:
				return ByteOutputStream.asLittleEndian(outputStream);
			case ELFDATA2MSB:
				return ByteOutputStream.asBigEndian(outputStream);
			default:
				throw new RuntimeException(ident_data.name());
		}
	}

	/**
	 * Wraps an input stream using the ELF file endianness.
	 *
	 * @param inputStream input stream to wrap
	 * @return wrapped input stream with correct endianness
	 */
	protected ByteInputStream wrap(InputStream inputStream) {
		switch (ident_data) {
			case ELFDATA2LSB:
				return ByteInputStream.asLittleEndian(inputStream);
			case ELFDATA2MSB:
				return ByteInputStream.asBigEndian(inputStream);
			default:
				throw new RuntimeException(ident_data.name());
		}
	}
}
