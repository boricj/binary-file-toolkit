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
import net.boricj.bft.coff.machines.amd64.CoffRelocationType_amd64;
import net.boricj.bft.image.ImageRelocationEntry;
import net.boricj.bft.image.RelocationOperation;
import net.boricj.bft.image.relocs.priv.ByteFieldCodec;

/**
 * Canonical relocation mapping catalog for AMD64 COFF objects.
 */
public final class Amd64CoffRelocationCatalog extends BaseMapCoffRelocationCatalog {
	/** Creates an AMD64 COFF relocation mapping catalog. */
	public Amd64CoffRelocationCatalog() {
		register(CoffRelocationType_amd64.IMAGE_REL_AMD64_ADDR64, RelocationOperation.ABSOLUTE, ByteFieldCodec.U64LE);
		register(CoffRelocationType_amd64.IMAGE_REL_AMD64_ADDR32, RelocationOperation.ABSOLUTE, ByteFieldCodec.U32LE);
		register(CoffRelocationType_amd64.IMAGE_REL_AMD64_ADDR32NB, RelocationOperation.ABSOLUTE, ByteFieldCodec.U32LE);
		register(
				CoffRelocationType_amd64.IMAGE_REL_AMD64_REL32_1,
				RelocationOperation.PC_RELATIVE,
				ByteFieldCodec.S32LE);
		register(
				CoffRelocationType_amd64.IMAGE_REL_AMD64_REL32_2,
				RelocationOperation.PC_RELATIVE,
				ByteFieldCodec.S32LE);
		register(
				CoffRelocationType_amd64.IMAGE_REL_AMD64_REL32_3,
				RelocationOperation.PC_RELATIVE,
				ByteFieldCodec.S32LE);
		register(
				CoffRelocationType_amd64.IMAGE_REL_AMD64_REL32_4,
				RelocationOperation.PC_RELATIVE,
				ByteFieldCodec.S32LE);
		register(
				CoffRelocationType_amd64.IMAGE_REL_AMD64_REL32_5,
				RelocationOperation.PC_RELATIVE,
				ByteFieldCodec.S32LE);
		register(CoffRelocationType_amd64.IMAGE_REL_AMD64_REL32, RelocationOperation.PC_RELATIVE, ByteFieldCodec.S32LE);
		register(CoffRelocationType_amd64.IMAGE_REL_AMD64_SECTION, RelocationOperation.ABSOLUTE, ByteFieldCodec.U32LE);
		register(CoffRelocationType_amd64.IMAGE_REL_AMD64_SECREL, RelocationOperation.ABSOLUTE, ByteFieldCodec.U32LE);
	}

	@Override
	public CoffRelocationType typeFromEntry(ImageRelocationEntry entry) {
		if (isSectionTarget(entry.getTarget())) {
			return CoffRelocationType_amd64.IMAGE_REL_AMD64_SECTION;
		}
		if (entry.getOperation() == RelocationOperation.PC_RELATIVE && entry.getFieldCodec() == ByteFieldCodec.S32LE) {
			return switch ((int) entry.getLocalAddend()) {
				case 0 -> CoffRelocationType_amd64.IMAGE_REL_AMD64_REL32;
				case 1 -> CoffRelocationType_amd64.IMAGE_REL_AMD64_REL32_1;
				case 2 -> CoffRelocationType_amd64.IMAGE_REL_AMD64_REL32_2;
				case 3 -> CoffRelocationType_amd64.IMAGE_REL_AMD64_REL32_3;
				case 4 -> CoffRelocationType_amd64.IMAGE_REL_AMD64_REL32_4;
				case 5 -> CoffRelocationType_amd64.IMAGE_REL_AMD64_REL32_5;
				default -> CoffRelocationType_amd64.IMAGE_REL_AMD64_REL32;
			};
		}
		if (isDefinedNonSectionTarget(entry.getTarget())
				&& entry.getOperation() == RelocationOperation.ABSOLUTE
				&& entry.getFieldCodec() == ByteFieldCodec.U32LE) {
			return CoffRelocationType_amd64.IMAGE_REL_AMD64_ADDR32NB;
		}
		return defaultTypeFromEntry(entry);
	}
}
