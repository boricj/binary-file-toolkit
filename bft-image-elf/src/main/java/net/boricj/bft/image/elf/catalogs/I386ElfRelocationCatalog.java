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

import java.util.Map;

import net.boricj.bft.elf.machines.i386.ElfRelocationType_i386;
import net.boricj.bft.image.RelocationDescriptor;
import net.boricj.bft.image.RelocationOperation;
import net.boricj.bft.image.elf.ElfRelocationTableKind;
import net.boricj.bft.image.relocs.priv.ByteFieldCodec;

/**
 * Canonical relocation mapping catalog for i386 ELF objects.
 */
public final class I386ElfRelocationCatalog extends BaseMapElfRelocationCatalog {
	/** Creates an i386 ELF relocation mapping catalog. */
	public I386ElfRelocationCatalog() {
		super(Map.of(
				ElfRelocationType_i386.R_386_32,
				new RelocationDescriptor(RelocationOperation.ABSOLUTE, ByteFieldCodec.U32LE),
				ElfRelocationType_i386.R_386_PC32,
				new RelocationDescriptor(RelocationOperation.PC_RELATIVE, ByteFieldCodec.S32LE),
				ElfRelocationType_i386.R_386_GOTPC,
				new RelocationDescriptor(RelocationOperation.GOT, ByteFieldCodec.S32LE),
				ElfRelocationType_i386.R_386_GOTOFF,
				new RelocationDescriptor(RelocationOperation.REGION_RELATIVE, ByteFieldCodec.S32LE),
				ElfRelocationType_i386.R_386_PLT32,
				new RelocationDescriptor(RelocationOperation.PLT, ByteFieldCodec.S32LE)));
	}

	@Override
	public ElfRelocationTableKind relocationTableKind() {
		return ElfRelocationTableKind.REL;
	}
}
