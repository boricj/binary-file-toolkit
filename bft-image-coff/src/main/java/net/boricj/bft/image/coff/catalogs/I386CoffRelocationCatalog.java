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

import net.boricj.bft.coff.constants.CoffRelocationType;
import net.boricj.bft.coff.machines.i386.CoffRelocationType_i386;
import net.boricj.bft.image.ImageRelocationEntry;
import net.boricj.bft.image.RelocationOperation;
import net.boricj.bft.image.relocs.priv.ByteFieldCodec;

/**
 * Canonical relocation mapping catalog for i386 COFF objects.
 */
public final class I386CoffRelocationCatalog extends BaseMapCoffRelocationCatalog {
	/** Creates an i386 COFF relocation mapping catalog. */
	public I386CoffRelocationCatalog() {
		register(CoffRelocationType_i386.IMAGE_REL_I386_DIR32, RelocationOperation.ABSOLUTE, ByteFieldCodec.U32LE);
		register(CoffRelocationType_i386.IMAGE_REL_I386_REL32, RelocationOperation.PC_RELATIVE, ByteFieldCodec.S32LE);
		register(CoffRelocationType_i386.IMAGE_REL_I386_DIR32NB, RelocationOperation.ABSOLUTE, ByteFieldCodec.U32LE);
		register(CoffRelocationType_i386.IMAGE_REL_I386_SECREL, RelocationOperation.ABSOLUTE, ByteFieldCodec.U32LE);
		register(CoffRelocationType_i386.IMAGE_REL_I386_SECTION, RelocationOperation.ABSOLUTE, ByteFieldCodec.U32LE);
	}

	@Override
	public CoffRelocationType typeFromEntry(ImageRelocationEntry entry) {
		if (isSectionTarget(entry.getTarget())) {
			return CoffRelocationType_i386.IMAGE_REL_I386_SECTION;
		}
		return defaultTypeFromEntry(entry);
	}
}
