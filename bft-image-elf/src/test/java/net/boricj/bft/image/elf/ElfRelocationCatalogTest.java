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

import org.junit.jupiter.api.Test;

import net.boricj.bft.elf.constants.ElfMachine;
import net.boricj.bft.elf.machines.amd64.ElfRelocationType_amd64;
import net.boricj.bft.elf.machines.i386.ElfRelocationType_i386;
import net.boricj.bft.elf.machines.mips.ElfRelocationType_Mips;
import net.boricj.bft.image.RelocationOperation;
import net.boricj.bft.image.relocs.priv.BitShiftedAdjustedFieldCodec;
import net.boricj.bft.image.relocs.priv.ByteFieldCodec;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElfRelocationCatalogTest {

	@Test
	void mapsElfTypesToStructuredDescriptors() {
		var amd64 = ElfRelocationCatalog.descriptorFromType(ElfRelocationType_amd64.R_X86_64_PC32);
		assertEquals(RelocationOperation.PC_RELATIVE, amd64.operation());
		assertEquals(ByteFieldCodec.S32LE, amd64.fieldCodec());

		var i386 = ElfRelocationCatalog.descriptorFromType(ElfRelocationType_i386.R_386_GOTPC);
		assertEquals(RelocationOperation.GOT, i386.operation());
		assertEquals(ByteFieldCodec.S32LE, i386.fieldCodec());

		var mips = ElfRelocationCatalog.descriptorFromType(ElfRelocationType_Mips.R_MIPS_HI16);
		assertEquals(RelocationOperation.ABSOLUTE, mips.operation());
		assertEquals(
				BitShiftedAdjustedFieldCodec.of(2, java.nio.ByteOrder.LITTLE_ENDIAN, 0, 16, 16, true),
				mips.fieldCodec());
	}

	@Test
	void mapsStructuredDescriptorsBackToElfTypes() {
		assertEquals(
				ElfRelocationType_amd64.R_X86_64_PLT32,
				ElfRelocationCatalog.typeFromDescriptor(
						ElfMachine.EM_X86_64, RelocationOperation.PLT, ByteFieldCodec.S32LE));
		assertEquals(
				ElfRelocationType_i386.R_386_GOTOFF,
				ElfRelocationCatalog.typeFromDescriptor(
						ElfMachine.EM_386, RelocationOperation.REGION_RELATIVE, ByteFieldCodec.S32LE));
		assertEquals(
				ElfRelocationType_Mips.R_MIPS_CALL16,
				ElfRelocationCatalog.typeFromDescriptor(
						ElfMachine.EM_MIPS, RelocationOperation.PLT, ByteFieldCodec.S16LE));
		assertEquals(
				ElfRelocationType_amd64.R_X86_64_32,
				ElfRelocationCatalog.typeFromDescriptor(
						ElfMachine.EM_X86_64, RelocationOperation.ABSOLUTE, ByteFieldCodec.U32LE));
		assertEquals(
				ElfRelocationType_i386.R_386_32,
				ElfRelocationCatalog.typeFromDescriptor(
						ElfMachine.EM_386, RelocationOperation.ABSOLUTE, ByteFieldCodec.U32LE));
		assertEquals(
				ElfRelocationType_Mips.R_MIPS_32,
				ElfRelocationCatalog.typeFromDescriptor(
						ElfMachine.EM_MIPS, RelocationOperation.ABSOLUTE, ByteFieldCodec.U32LE));
	}

	@Test
	void keepsAddendPolicyOutsideDescriptors() {
		assertEquals(true, ElfRelocationCatalog.usesExplicitAddends(ElfMachine.EM_X86_64));
		assertEquals(false, ElfRelocationCatalog.usesExplicitAddends(ElfMachine.EM_386));
		assertEquals(false, ElfRelocationCatalog.usesExplicitAddends(ElfMachine.EM_MIPS));
	}
}
