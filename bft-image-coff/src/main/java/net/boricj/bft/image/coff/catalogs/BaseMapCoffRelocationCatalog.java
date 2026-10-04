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
package net.boricj.bft.image.coff.catalogs;

import java.util.HashMap;
import java.util.Map;

import net.boricj.bft.coff.constants.CoffRelocationType;
import net.boricj.bft.image.ImageRelocationEntry;
import net.boricj.bft.image.ImageSymbol;
import net.boricj.bft.image.RelocationDescriptor;
import net.boricj.bft.image.RelocationFieldCodec;
import net.boricj.bft.image.RelocationOperation;
import net.boricj.bft.image.coff.CoffRelocationCatalog;

abstract class BaseMapCoffRelocationCatalog implements CoffRelocationCatalog {
	private final Map<CoffRelocationType, RelocationDescriptor> typeToDescriptor = new HashMap<>();
	private final Map<RelocationDescriptor, CoffRelocationType> descriptorToType = new HashMap<>();

	/**
	 * Registers a canonical mapping from a COFF relocation type to a normalized descriptor.
	 *
	 * @param type COFF relocation type
	 * @param operation normalized relocation operation
	 * @param fieldCodec normalized field codec
	 */
	protected final void register(
			CoffRelocationType type, RelocationOperation operation, RelocationFieldCodec fieldCodec) {
		RelocationDescriptor descriptor = new RelocationDescriptor(operation, fieldCodec);
		this.typeToDescriptor.put(type, descriptor);
		this.descriptorToType.putIfAbsent(descriptor, type);
	}

	@Override
	public final RelocationDescriptor descriptorFromType(CoffRelocationType type) {
		return this.typeToDescriptor.get(type);
	}

	/**
	 * Returns the first registered relocation type for the entry descriptor.
	 *
	 * @param entry relocation entry to map
	 * @return mapped COFF relocation type, or null when unmapped
	 */
	protected final CoffRelocationType defaultTypeFromEntry(ImageRelocationEntry entry) {
		return this.descriptorToType.get(new RelocationDescriptor(entry.getOperation(), entry.getFieldCodec()));
	}

	/**
	 * Checks whether a relocation target is a section symbol.
	 *
	 * @param target relocation target
	 * @return true when target is a section symbol
	 */
	protected static final boolean isSectionTarget(ImageSymbol target) {
		return target != null && target.getType() == ImageSymbol.Type.SECTION;
	}

	/**
	 * Checks whether a relocation target is a defined non-section symbol.
	 *
	 * @param target relocation target
	 * @return true when target is defined and not a section symbol
	 */
	protected static final boolean isDefinedNonSectionTarget(ImageSymbol target) {
		return target != null && target.getSection() != null && target.getType() != ImageSymbol.Type.SECTION;
	}
}
