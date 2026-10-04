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
package net.boricj.bft.image.coff;

import org.junit.jupiter.api.Test;

import net.boricj.bft.coff.constants.CoffMachine;
import net.boricj.bft.coff.machines.amd64.CoffRelocationType_amd64;
import net.boricj.bft.coff.machines.i386.CoffRelocationType_i386;
import net.boricj.bft.image.ImageFile;
import net.boricj.bft.image.ImageRelocationEntry;
import net.boricj.bft.image.ImageSymbol;
import net.boricj.bft.image.RelocationOperation;
import net.boricj.bft.image.relocs.priv.ByteFieldCodec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CoffRelocationCatalogTest {
	private static final CoffRelocationCatalogRegistry CATALOGS = DefaultCoffRelocationCatalogRegistry.INSTANCE;

	@Test
	void mapsCoffTypesToStructuredDescriptors() {
		var i386Catalog = CATALOGS.catalogFor(CoffMachine.IMAGE_FILE_MACHINE_I386);
		assertNotNull(i386Catalog);
		var i386Abs = i386Catalog.descriptorFromType(CoffRelocationType_i386.IMAGE_REL_I386_DIR32);
		assertEquals(RelocationOperation.ABSOLUTE, i386Abs.operation());
		assertEquals(ByteFieldCodec.U32LE, i386Abs.fieldCodec());

		var i386Rel = i386Catalog.descriptorFromType(CoffRelocationType_i386.IMAGE_REL_I386_REL32);
		assertEquals(RelocationOperation.PC_RELATIVE, i386Rel.operation());
		assertEquals(ByteFieldCodec.S32LE, i386Rel.fieldCodec());

		var amd64Catalog = CATALOGS.catalogFor(CoffMachine.IMAGE_FILE_MACHINE_AMD64);
		assertNotNull(amd64Catalog);
		var amd64Abs = amd64Catalog.descriptorFromType(CoffRelocationType_amd64.IMAGE_REL_AMD64_ADDR64);
		assertEquals(RelocationOperation.ABSOLUTE, amd64Abs.operation());
		assertEquals(ByteFieldCodec.U64LE, amd64Abs.fieldCodec());
	}

	@Test
	void mapsStructuredDescriptorsBackToCoffTypes() {
		var i386Catalog = CATALOGS.catalogFor(CoffMachine.IMAGE_FILE_MACHINE_I386);
		var amd64Catalog = CATALOGS.catalogFor(CoffMachine.IMAGE_FILE_MACHINE_AMD64);
		assertNotNull(i386Catalog);
		assertNotNull(amd64Catalog);

		assertEquals(
				CoffRelocationType_i386.IMAGE_REL_I386_REL32,
				i386Catalog.typeFromEntry(entry(
						CoffMachine.IMAGE_FILE_MACHINE_I386,
						RelocationOperation.PC_RELATIVE,
						ByteFieldCodec.S32LE,
						0,
						false,
						false)));
		assertEquals(
				CoffRelocationType_amd64.IMAGE_REL_AMD64_REL32,
				amd64Catalog.typeFromEntry(entry(
						CoffMachine.IMAGE_FILE_MACHINE_AMD64,
						RelocationOperation.PC_RELATIVE,
						ByteFieldCodec.S32LE,
						0,
						false,
						false)));
		assertEquals(
				CoffRelocationType_amd64.IMAGE_REL_AMD64_ADDR64,
				amd64Catalog.typeFromEntry(entry(
						CoffMachine.IMAGE_FILE_MACHINE_AMD64,
						RelocationOperation.ABSOLUTE,
						ByteFieldCodec.U64LE,
						0,
						false,
						false)));

		// The ABSOLUTE+U32LE i386 descriptor is intentionally shared by several relocation
		// types (DIR32, DIR32NB, SECREL, SECTION). Validate descriptor stability from type
		// rather than expecting a bijective reverse map.
		assertEquals(
				RelocationOperation.ABSOLUTE,
				i386Catalog
						.descriptorFromType(CoffRelocationType_i386.IMAGE_REL_I386_SECTION)
						.operation());
		assertEquals(
				ByteFieldCodec.U32LE,
				i386Catalog
						.descriptorFromType(CoffRelocationType_i386.IMAGE_REL_I386_SECTION)
						.fieldCodec());
	}

	@Test
	void returnsNullForUnsupportedMachineOrDescriptor() {
		assertNull(CATALOGS.catalogFor(CoffMachine.IMAGE_FILE_MACHINE_UNKNOWN));

		var i386Catalog = CATALOGS.catalogFor(CoffMachine.IMAGE_FILE_MACHINE_I386);
		assertNotNull(i386Catalog);
		assertNull(i386Catalog.typeFromEntry(entry(
				CoffMachine.IMAGE_FILE_MACHINE_I386, RelocationOperation.GOT, ByteFieldCodec.U32LE, 0, false, false)));
	}

	@Test
	void handlesSpecialVariantSelection() {
		var amd64Catalog = CATALOGS.catalogFor(CoffMachine.IMAGE_FILE_MACHINE_AMD64);
		assertNotNull(amd64Catalog);

		assertEquals(
				CoffRelocationType_amd64.IMAGE_REL_AMD64_REL32_3,
				amd64Catalog.typeFromEntry(entry(
						CoffMachine.IMAGE_FILE_MACHINE_AMD64,
						RelocationOperation.PC_RELATIVE,
						ByteFieldCodec.S32LE,
						3,
						false,
						false)));
		assertEquals(
				CoffRelocationType_amd64.IMAGE_REL_AMD64_SECTION,
				amd64Catalog.typeFromEntry(entry(
						CoffMachine.IMAGE_FILE_MACHINE_AMD64,
						RelocationOperation.ABSOLUTE,
						ByteFieldCodec.U32LE,
						0,
						true,
						false)));
		assertEquals(
				CoffRelocationType_amd64.IMAGE_REL_AMD64_ADDR32NB,
				amd64Catalog.typeFromEntry(entry(
						CoffMachine.IMAGE_FILE_MACHINE_AMD64,
						RelocationOperation.ABSOLUTE,
						ByteFieldCodec.U32LE,
						0,
						false,
						true)));
	}

	private static ImageRelocationEntry entry(
			CoffMachine machine,
			RelocationOperation operation,
			net.boricj.bft.image.RelocationFieldCodec codec,
			long localAddend,
			boolean sectionTarget,
			boolean definedNonSectionTarget) {
		ImageFile image = new ImageFile(ImageFile.Kind.OBJECT);
		var section = image.sections().create(".text");
		section.setBytes(new byte[8]);
		ImageSymbol target;
		if (sectionTarget) {
			target = image.symbols().createSection(section);
		} else if (definedNonSectionTarget) {
			target = image.symbols()
					.create(
							"local_data",
							section,
							0,
							0,
							ImageSymbol.Type.OBJECT,
							ImageSymbol.Visibility.DEFAULT,
							ImageSymbol.Binding.LOCAL);
		} else {
			target = image.symbols()
					.createUndefined(
							"extern",
							0,
							ImageSymbol.Type.NOTYPE,
							ImageSymbol.Visibility.DEFAULT,
							ImageSymbol.Binding.GLOBAL);
		}
		var group = section.relocations().create(operation, target, 0);
		var gang = group.gangs().create(codec);
		return gang.entries().create(machine == CoffMachine.IMAGE_FILE_MACHINE_AMD64 ? 4 : 0, localAddend);
	}
}
