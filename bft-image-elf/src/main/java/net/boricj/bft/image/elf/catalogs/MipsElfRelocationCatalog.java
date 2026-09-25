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

import java.nio.ByteOrder;
import java.util.Map;

import net.boricj.bft.elf.machines.mips.ElfRelocationType_Mips;
import net.boricj.bft.image.RelocationDescriptor;
import net.boricj.bft.image.RelocationOperation;
import net.boricj.bft.image.elf.ElfRelocationTableKind;
import net.boricj.bft.image.relocs.priv.BitShiftedAdjustedFieldCodec;
import net.boricj.bft.image.relocs.priv.BitShiftedFieldCodec;
import net.boricj.bft.image.relocs.priv.ByteFieldCodec;

/**
 * Canonical relocation mapping catalog for MIPS ELF objects.
 */
public final class MipsElfRelocationCatalog extends BaseMapElfRelocationCatalog {
	/** Creates a MIPS ELF relocation mapping catalog. */
	public MipsElfRelocationCatalog() {
		super(Map.of(
				ElfRelocationType_Mips.R_MIPS_32,
				new RelocationDescriptor(RelocationOperation.ABSOLUTE, ByteFieldCodec.U32LE),
				ElfRelocationType_Mips.R_MIPS_HI16,
				new RelocationDescriptor(
						RelocationOperation.ABSOLUTE,
						BitShiftedAdjustedFieldCodec.of(2, ByteOrder.LITTLE_ENDIAN, 0, 16, 16, true)),
				ElfRelocationType_Mips.R_MIPS_LO16,
				new RelocationDescriptor(RelocationOperation.ABSOLUTE, ByteFieldCodec.S16LE),
				ElfRelocationType_Mips.R_MIPS_GOT16,
				new RelocationDescriptor(RelocationOperation.GOT, ByteFieldCodec.S16LE),
				ElfRelocationType_Mips.R_MIPS_CALL16,
				new RelocationDescriptor(RelocationOperation.PLT, ByteFieldCodec.S16LE),
				ElfRelocationType_Mips.R_MIPS_JALR,
				new RelocationDescriptor(
						RelocationOperation.REGION_RELATIVE,
						BitShiftedFieldCodec.of(4, ByteOrder.LITTLE_ENDIAN, 0, 26, 2, true))));
	}

	@Override
	public ElfRelocationTableKind relocationTableKind() {
		return ElfRelocationTableKind.REL;
	}
}
