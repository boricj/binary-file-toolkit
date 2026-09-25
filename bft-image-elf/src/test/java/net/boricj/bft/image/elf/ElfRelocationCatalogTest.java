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
import net.boricj.bft.image.ImageFile;
import net.boricj.bft.image.ImageRelocationEntry;
import net.boricj.bft.image.ImageSymbol;
import net.boricj.bft.image.RelocationOperation;
import net.boricj.bft.image.relocs.priv.BitShiftedAdjustedFieldCodec;
import net.boricj.bft.image.relocs.priv.BitShiftedFieldCodec;
import net.boricj.bft.image.relocs.priv.ByteFieldCodec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ElfRelocationCatalogTest {
	private static final ElfRelocationCatalogRegistry CATALOGS = DefaultElfRelocationCatalogRegistry.INSTANCE;

	@Test
	void mapsElfTypesToStructuredDescriptors() {
		var amd64Catalog = CATALOGS.catalogFor(ElfMachine.EM_X86_64);
		assertNotNull(amd64Catalog);
		var amd64 = amd64Catalog.descriptorFromType(ElfRelocationType_amd64.R_X86_64_PC32);
		assertEquals(RelocationOperation.PC_RELATIVE, amd64.operation());
		assertEquals(ByteFieldCodec.S32LE, amd64.fieldCodec());

		var i386Catalog = CATALOGS.catalogFor(ElfMachine.EM_386);
		assertNotNull(i386Catalog);
		var i386 = i386Catalog.descriptorFromType(ElfRelocationType_i386.R_386_GOTPC);
		assertEquals(RelocationOperation.GOT, i386.operation());
		assertEquals(ByteFieldCodec.S32LE, i386.fieldCodec());

		var mipsCatalog = CATALOGS.catalogFor(ElfMachine.EM_MIPS);
		assertNotNull(mipsCatalog);
		var mips = mipsCatalog.descriptorFromType(ElfRelocationType_Mips.R_MIPS_HI16);
		assertEquals(RelocationOperation.ABSOLUTE, mips.operation());
		assertEquals(
				BitShiftedAdjustedFieldCodec.of(2, java.nio.ByteOrder.LITTLE_ENDIAN, 0, 16, 16, true),
				mips.fieldCodec());
	}

	@Test
	void mapsStructuredDescriptorsBackToElfTypes() {
		var amd64Catalog = CATALOGS.catalogFor(ElfMachine.EM_X86_64);
		var i386Catalog = CATALOGS.catalogFor(ElfMachine.EM_386);
		var mipsCatalog = CATALOGS.catalogFor(ElfMachine.EM_MIPS);
		assertNotNull(amd64Catalog);
		assertNotNull(i386Catalog);
		assertNotNull(mipsCatalog);

		assertEquals(
				ElfRelocationType_amd64.R_X86_64_PLT32,
				amd64Catalog.typeFromEntry(entry(RelocationOperation.PLT, ByteFieldCodec.S32LE, 0)));
		assertEquals(
				ElfRelocationType_i386.R_386_GOTOFF,
				i386Catalog.typeFromEntry(entry(RelocationOperation.REGION_RELATIVE, ByteFieldCodec.S32LE, 0)));
		assertEquals(
				ElfRelocationType_Mips.R_MIPS_CALL16,
				mipsCatalog.typeFromEntry(entry(RelocationOperation.PLT, ByteFieldCodec.S16LE, 0)));
		assertEquals(
				ElfRelocationType_amd64.R_X86_64_32,
				amd64Catalog.typeFromEntry(entry(RelocationOperation.ABSOLUTE, ByteFieldCodec.U32LE, 0)));
		assertEquals(
				ElfRelocationType_i386.R_386_32,
				i386Catalog.typeFromEntry(entry(RelocationOperation.ABSOLUTE, ByteFieldCodec.U32LE, 0)));
		assertEquals(
				ElfRelocationType_Mips.R_MIPS_32,
				mipsCatalog.typeFromEntry(entry(RelocationOperation.ABSOLUTE, ByteFieldCodec.U32LE, 0)));
		assertEquals(
				ElfRelocationType_Mips.R_MIPS_JALR,
				mipsCatalog.typeFromEntry(entry(
						RelocationOperation.REGION_RELATIVE,
						BitShiftedFieldCodec.of(4, java.nio.ByteOrder.LITTLE_ENDIAN, 0, 26, 2, true),
						0)));
	}

	@Test
	void keepsAddendPolicyOutsideDescriptors() {
		assertEquals(
				ElfRelocationTableKind.RELA,
				CATALOGS.catalogFor(ElfMachine.EM_X86_64).relocationTableKind());
		assertEquals(
				ElfRelocationTableKind.REL,
				CATALOGS.catalogFor(ElfMachine.EM_386).relocationTableKind());
		assertEquals(
				ElfRelocationTableKind.REL,
				CATALOGS.catalogFor(ElfMachine.EM_MIPS).relocationTableKind());
	}

	private static ImageRelocationEntry entry(
			RelocationOperation operation, net.boricj.bft.image.RelocationFieldCodec codec, long localAddend) {
		ImageFile image = new ImageFile(ImageFile.Kind.OBJECT);
		var section = image.sections().create(".text");
		section.setBytes(new byte[8]);
		ImageSymbol target = image.symbols().create("target", section, 0);
		var group = section.relocations().create(operation, target, 0);
		var gang = group.gangs().create(codec);
		return gang.entries().create(0, localAddend);
	}
}
