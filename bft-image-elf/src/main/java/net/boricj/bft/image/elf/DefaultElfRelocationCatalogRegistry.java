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
package net.boricj.bft.image.elf;

import java.util.EnumMap;
import java.util.Map;

import net.boricj.bft.elf.constants.ElfMachine;
import net.boricj.bft.image.elf.catalogs.Amd64ElfRelocationCatalog;
import net.boricj.bft.image.elf.catalogs.I386ElfRelocationCatalog;
import net.boricj.bft.image.elf.catalogs.MipsElfRelocationCatalog;

/**
 * Default canonical ELF relocation catalog registry.
 */
public final class DefaultElfRelocationCatalogRegistry implements ElfRelocationCatalogRegistry {
	/** Shared immutable default instance. */
	public static final DefaultElfRelocationCatalogRegistry INSTANCE = new DefaultElfRelocationCatalogRegistry();

	private final Map<ElfMachine, ElfRelocationCatalog> catalogs = new EnumMap<>(ElfMachine.class);

	private DefaultElfRelocationCatalogRegistry() {
		catalogs.put(ElfMachine.EM_X86_64, new Amd64ElfRelocationCatalog());
		catalogs.put(ElfMachine.EM_386, new I386ElfRelocationCatalog());
		catalogs.put(ElfMachine.EM_MIPS, new MipsElfRelocationCatalog());
	}

	@Override
	public ElfRelocationCatalog catalogFor(ElfMachine machine) {
		return catalogs.get(machine);
	}
}
