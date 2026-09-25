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

import net.boricj.bft.elf.constants.ElfMachine;

/**
 * Registry exposing one canonical ELF relocation catalog per machine.
 */
public interface ElfRelocationCatalogRegistry {
	/**
	 * Returns relocation catalog for one machine.
	 *
	 * @param machine ELF machine
	 * @return catalog, or {@code null} when unsupported
	 */
	ElfRelocationCatalog catalogFor(ElfMachine machine);
}
