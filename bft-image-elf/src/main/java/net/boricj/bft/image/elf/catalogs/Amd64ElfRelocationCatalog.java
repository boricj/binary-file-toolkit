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

import net.boricj.bft.elf.machines.amd64.ElfRelocationType_amd64;
import net.boricj.bft.image.RelocationDescriptor;
import net.boricj.bft.image.RelocationOperation;
import net.boricj.bft.image.elf.ElfRelocationTableKind;
import net.boricj.bft.image.relocs.priv.ByteFieldCodec;

/**
 * Canonical relocation mapping catalog for AMD64 ELF objects.
 */
public final class Amd64ElfRelocationCatalog extends BaseMapElfRelocationCatalog {
	/** Creates an AMD64 ELF relocation mapping catalog. */
	public Amd64ElfRelocationCatalog() {
		super(Map.of(
				ElfRelocationType_amd64.R_X86_64_32,
				new RelocationDescriptor(RelocationOperation.ABSOLUTE, ByteFieldCodec.U32LE),
				ElfRelocationType_amd64.R_X86_64_PC32,
				new RelocationDescriptor(RelocationOperation.PC_RELATIVE, ByteFieldCodec.S32LE),
				ElfRelocationType_amd64.R_X86_64_PLT32,
				new RelocationDescriptor(RelocationOperation.PLT, ByteFieldCodec.S32LE)));
	}

	@Override
	public ElfRelocationTableKind relocationTableKind() {
		return ElfRelocationTableKind.RELA;
	}
}
