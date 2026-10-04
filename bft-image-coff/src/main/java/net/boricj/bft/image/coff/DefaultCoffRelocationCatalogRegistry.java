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
package net.boricj.bft.image.coff;

import java.util.EnumMap;
import java.util.Map;

import net.boricj.bft.coff.constants.CoffMachine;
import net.boricj.bft.image.coff.catalogs.Amd64CoffRelocationCatalog;
import net.boricj.bft.image.coff.catalogs.I386CoffRelocationCatalog;

/**
 * Default canonical COFF relocation catalog registry.
 */
public final class DefaultCoffRelocationCatalogRegistry implements CoffRelocationCatalogRegistry {
	/** Shared immutable default instance. */
	public static final DefaultCoffRelocationCatalogRegistry INSTANCE = new DefaultCoffRelocationCatalogRegistry();

	private final Map<CoffMachine, CoffRelocationCatalog> catalogs = new EnumMap<>(CoffMachine.class);

	private DefaultCoffRelocationCatalogRegistry() {
		catalogs.put(CoffMachine.IMAGE_FILE_MACHINE_I386, new I386CoffRelocationCatalog());
		catalogs.put(CoffMachine.IMAGE_FILE_MACHINE_AMD64, new Amd64CoffRelocationCatalog());
	}

	@Override
	public CoffRelocationCatalog catalogFor(CoffMachine machine) {
		return catalogs.get(machine);
	}
}
