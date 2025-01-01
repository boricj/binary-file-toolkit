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
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Stream;

import net.boricj.bft.IndirectList;
import net.boricj.bft.Writable;
import net.boricj.bft.elf.constants.ElfClass;
import net.boricj.bft.elf.constants.ElfMachine;
import net.boricj.bft.elf.constants.ElfSectionType;
import net.boricj.bft.elf.sections.ElfNullSection;
import net.boricj.bft.elf.sections.ElfStringTable;

import static net.boricj.bft.elf.ElfSection.SHN_LORESERVE;

public class ElfSectionTable implements IndirectList<ElfSection>, Writable {
	private final ElfFile elf;
	private final List<ElfSection> table = new ArrayList<>();
	private final Map<ElfSection, Integer> reverseLookup = new IdentityHashMap<>();

	protected ElfSectionTable(ElfFile elf) {
		this.elf = elf;
	}

	public ElfSectionTable(ElfFile elf, ElfFile.Parser parser) throws IOException {
		this.elf = elf;
		this.table.addAll(
				Stream.generate(() -> (ElfSection) null).limit(parser.e_shnum).toList());
		parser.sh_names.addAll(Stream.generate(() -> 0).limit(parser.e_shnum).toList());

		// Actual parsing of section table is done inside ElfFile because its sections
		// member is null until this constructor returns and we need it to instanciate
		// sections which references other sections.
	}

	@Override
	public long getOffset() {
		return elf.getHeader().getShoff();
	}

	@Override
	public long getLength() {
		return elf.getHeader().getShentsize() * size();
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		DataOutput dataOutput = elf.wrap(outputStream);
		ElfHeader header = elf.getHeader();
		ElfStringTable shstr = header.getShstr();

		ElfClass ident_class = header.getIdentClass();
		switch (ident_class) {
			case ELFCLASS32:
				for (ElfSection section : table) {
					int sh_name = 0;
					if (shstr != null) {
						sh_name = shstr.find(section.getName());
					}

					dataOutput.writeInt(sh_name);
					dataOutput.writeInt(section.getType());
					dataOutput.writeInt((int) section.getFlags().getValue());
					dataOutput.writeInt((int) section.getAddress());
					dataOutput.writeInt((int) section.getOffset());
					dataOutput.writeInt((int) section.getSize());
					dataOutput.writeInt(section.getLink());
					dataOutput.writeInt(section.getInfo());
					dataOutput.writeInt((int) section.getAddrAlign());
					dataOutput.writeInt((int) section.getEntSize());
				}
				break;

			case ELFCLASS64:
				for (ElfSection section : table) {
					int sh_name = 0;
					if (shstr != null) {
						sh_name = shstr.find(section.getName());
					}

					dataOutput.writeInt(sh_name);
					dataOutput.writeInt(section.getType());
					dataOutput.writeLong(section.getFlags().getValue());
					dataOutput.writeLong(section.getAddress());
					dataOutput.writeLong(section.getOffset());
					dataOutput.writeLong(section.getSize());
					dataOutput.writeInt(section.getLink());
					dataOutput.writeInt(section.getInfo());
					dataOutput.writeLong(section.getAddrAlign());
					dataOutput.writeLong(section.getEntSize());
				}
				break;

			default:
				throw new RuntimeException(ident_class.name());
		}
	}

	public ElfSection get(int index, ElfFile.Parser parser) throws IOException {
		ElfSection section = get(index);

		if (section == null) {
			FileInputStream fis = parser.getFileInputStream();
			ElfHeader header = elf.getHeader();
			long e_shoff = header.getShoff();
			long e_shentsize = header.getShentsize();

			fis.getChannel().position(e_shoff + e_shentsize * index);
			DataInput dataInput = elf.wrap(fis);

			ElfClass ident_class = header.getIdentClass();
			switch (ident_class) {
				case ELFCLASS32:
					section = instanciateSection32bits(index, dataInput, parser);
					break;

				case ELFCLASS64:
					section = instanciateSection64bits(index, dataInput, parser);
					break;

				default:
					throw new RuntimeException(ident_class.name());
			}

			reverseLookup.put(section, index);
			table.set(index, section);
		}

		return section;
	}

	private ElfSection instanciateSection32bits(int index, DataInput dataInput, ElfFile.Parser parser)
			throws IOException {
		int sh_name = dataInput.readInt();
		int sh_type = dataInput.readInt();
		int sh_flags = dataInput.readInt();
		int sh_addr = dataInput.readInt();
		int sh_offset = dataInput.readInt();
		int sh_size = dataInput.readInt();
		int sh_link = dataInput.readInt();
		int sh_info = dataInput.readInt();
		int sh_addralign = dataInput.readInt();
		int sh_entsize = dataInput.readInt();

		parser.sh_names.set(index, sh_name);

		return instanciateSection(
				parser, sh_type, sh_flags, sh_addr, sh_offset, sh_size, sh_link, sh_info, sh_addralign, sh_entsize);
	}

	private ElfSection instanciateSection64bits(int index, DataInput dataInput, ElfFile.Parser parser)
			throws IOException {
		int sh_name = dataInput.readInt();
		int sh_type = dataInput.readInt();
		long sh_flags = dataInput.readLong();
		long sh_addr = dataInput.readLong();
		long sh_offset = dataInput.readLong();
		long sh_size = dataInput.readLong();
		int sh_link = dataInput.readInt();
		int sh_info = dataInput.readInt();
		long sh_addralign = dataInput.readLong();
		long sh_entsize = dataInput.readLong();

		parser.sh_names.set(index, sh_name);

		return instanciateSection(
				parser, sh_type, sh_flags, sh_addr, sh_offset, sh_size, sh_link, sh_info, sh_addralign, sh_entsize);
	}

	private ElfSection instanciateSection(
			ElfFile.Parser parser,
			int sh_type,
			long sh_flags,
			long sh_addr,
			long sh_offset,
			long sh_size,
			int sh_link,
			int sh_info,
			long sh_addralign,
			long sh_entsize)
			throws IOException {
		ElfMachine machine = elf.getHeader().getMachine();
		ElfSectionType type = ElfSectionType.valueFrom(sh_type, machine);
		ElfSectionFlags flags;

		try {
			Constructor<? extends ElfSectionFlags> constructor;
			constructor = machine.getSectionFlagsClass().getConstructor(Long.TYPE);
			flags = constructor.newInstance(sh_flags);
		} catch (NoSuchMethodException
				| SecurityException
				| InstantiationException
				| IllegalAccessException
				| IllegalArgumentException
				| InvocationTargetException ex) {
			throw new RuntimeException(ex);
		}

		try {
			Constructor<? extends ElfSection> constructor;
			constructor = type.getSectionClass()
					.getConstructor(
							ElfFile.class,
							ElfFile.Parser.class,
							ElfSectionFlags.class,
							Long.TYPE,
							Long.TYPE,
							Long.TYPE,
							Integer.TYPE,
							Integer.TYPE,
							Long.TYPE,
							Long.TYPE);
			return constructor.newInstance(
					elf, parser, flags, sh_addr, sh_offset, sh_size, sh_link, sh_info, sh_addralign, sh_entsize);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException ex) {
			throw new RuntimeException(ex);
		} catch (InstantiationException | InvocationTargetException ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			}
			throw new RuntimeException(cause);
		}
	}

	@Override
	public boolean add(ElfSection section) {
		Objects.requireNonNull(section);

		if (section.getElfFile() != elf) {
			throw new NoSuchElementException("section doesn't belong to this ELF file");
		}
		if (isEmpty() && !(section instanceof ElfNullSection)) {
			throw new IllegalArgumentException("first section must be null section");
		}
		if (contains(section)) {
			throw new IllegalStateException("section is already present inside section table");
		}
		if (size() == SHN_LORESERVE - 1) {
			throw new RuntimeException("too many sections (extended count not implemented)");
		}

		reverseLookup.put(section, size());
		return table.add(section);
	}

	@Override
	public boolean contains(Object object) {
		return reverseLookup.containsKey(object);
	}

	@Override
	public int indexOf(Object object) {
		return reverseLookup.getOrDefault(object, -1);
	}

	@Override
	public List<ElfSection> getElements() {
		return Collections.unmodifiableList(table);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
