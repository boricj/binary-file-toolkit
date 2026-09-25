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

import net.boricj.bft.elf.constants.ElfRelocationType;
import net.boricj.bft.image.ImageRelocationEntry;
import net.boricj.bft.image.RelocationDescriptor;

/**
 * Architecture-specific mapping between ELF relocation types and bft-image descriptors.
 */
public interface ElfRelocationCatalog {
	/**
	 * Preferred ELF relocation table kind for this catalog.
	 *
	 * @return REL or RELA
	 */
	ElfRelocationTableKind relocationTableKind();

	/**
	 * Resolves an ELF relocation type to a semantic descriptor.
	 *
	 * @param type ELF relocation type
	 * @return relocation descriptor, or {@code null} when unsupported
	 */
	RelocationDescriptor descriptorFromType(ElfRelocationType type);

	/**
	 * Resolves one semantic relocation entry to an ELF relocation type.
	 *
	 * @param entry semantic relocation entry
	 * @return ELF relocation type, or {@code null} when unsupported
	 */
	ElfRelocationType typeFromEntry(ImageRelocationEntry entry);
}
