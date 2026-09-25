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
package net.boricj.bft.image.elf.catalogs;

import java.util.HashMap;
import java.util.Map;

import net.boricj.bft.elf.constants.ElfRelocationType;
import net.boricj.bft.image.ImageRelocationEntry;
import net.boricj.bft.image.RelocationDescriptor;
import net.boricj.bft.image.elf.ElfRelocationCatalog;

abstract class BaseMapElfRelocationCatalog implements ElfRelocationCatalog {
	private final Map<ElfRelocationType, RelocationDescriptor> typeToDescriptor;
	private final Map<RelocationDescriptor, ElfRelocationType> descriptorToType;

	BaseMapElfRelocationCatalog(Map<ElfRelocationType, RelocationDescriptor> typeToDescriptor) {
		this.typeToDescriptor = typeToDescriptor;
		this.descriptorToType = new HashMap<>();
		for (Map.Entry<ElfRelocationType, RelocationDescriptor> entry : typeToDescriptor.entrySet()) {
			this.descriptorToType.put(entry.getValue(), entry.getKey());
		}
	}

	@Override
	public RelocationDescriptor descriptorFromType(ElfRelocationType type) {
		return this.typeToDescriptor.get(type);
	}

	@Override
	public ElfRelocationType typeFromEntry(ImageRelocationEntry entry) {
		return this.descriptorToType.get(new RelocationDescriptor(entry.getOperation(), entry.getFieldCodec()));
	}
}
