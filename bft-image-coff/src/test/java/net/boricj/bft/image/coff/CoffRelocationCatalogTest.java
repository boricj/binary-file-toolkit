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
import net.boricj.bft.image.RelocationOperation;
import net.boricj.bft.image.relocs.priv.ByteFieldCodec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CoffRelocationCatalogTest {
	@Test
	void mapsCoffTypesToStructuredDescriptors() {
		var i386Abs = CoffRelocationCatalog.descriptorFromType(CoffRelocationType_i386.IMAGE_REL_I386_DIR32);
		assertEquals(RelocationOperation.ABSOLUTE, i386Abs.operation());
		assertEquals(ByteFieldCodec.U32LE, i386Abs.fieldCodec());

		var i386Rel = CoffRelocationCatalog.descriptorFromType(CoffRelocationType_i386.IMAGE_REL_I386_REL32);
		assertEquals(RelocationOperation.PC_RELATIVE, i386Rel.operation());
		assertEquals(ByteFieldCodec.S32LE, i386Rel.fieldCodec());

		var amd64Abs = CoffRelocationCatalog.descriptorFromType(CoffRelocationType_amd64.IMAGE_REL_AMD64_ADDR64);
		assertEquals(RelocationOperation.ABSOLUTE, amd64Abs.operation());
		assertEquals(ByteFieldCodec.U64LE, amd64Abs.fieldCodec());
	}

	@Test
	void mapsStructuredDescriptorsBackToCoffTypes() {
		assertEquals(
				CoffRelocationType_i386.IMAGE_REL_I386_REL32,
				CoffRelocationCatalog.typeFromDescriptor(
						CoffMachine.IMAGE_FILE_MACHINE_I386, RelocationOperation.PC_RELATIVE, ByteFieldCodec.S32LE));
		assertEquals(
				CoffRelocationType_amd64.IMAGE_REL_AMD64_REL32,
				CoffRelocationCatalog.typeFromDescriptor(
						CoffMachine.IMAGE_FILE_MACHINE_AMD64, RelocationOperation.PC_RELATIVE, ByteFieldCodec.S32LE));
		assertEquals(
				CoffRelocationType_amd64.IMAGE_REL_AMD64_ADDR64,
				CoffRelocationCatalog.typeFromDescriptor(
						CoffMachine.IMAGE_FILE_MACHINE_AMD64, RelocationOperation.ABSOLUTE, ByteFieldCodec.U64LE));

		// The ABSOLUTE+U32LE i386 descriptor is intentionally shared by several relocation
		// types (DIR32, DIR32NB, SECREL, SECTION). Validate descriptor stability from type
		// rather than expecting a bijective reverse map.
		assertEquals(
				RelocationOperation.ABSOLUTE,
				CoffRelocationCatalog.descriptorFromType(CoffRelocationType_i386.IMAGE_REL_I386_SECTION)
						.operation());
		assertEquals(
				ByteFieldCodec.U32LE,
				CoffRelocationCatalog.descriptorFromType(CoffRelocationType_i386.IMAGE_REL_I386_SECTION)
						.fieldCodec());
	}

	@Test
	void rejectsUnsupportedMachineOrDescriptor() {
		assertThrows(
				IllegalArgumentException.class,
				() -> CoffRelocationCatalog.typeFromDescriptor(
						CoffMachine.IMAGE_FILE_MACHINE_UNKNOWN, RelocationOperation.ABSOLUTE, ByteFieldCodec.U32LE));
		assertThrows(
				IllegalArgumentException.class,
				() -> CoffRelocationCatalog.typeFromDescriptor(
						CoffMachine.IMAGE_FILE_MACHINE_I386, RelocationOperation.GOT, ByteFieldCodec.U32LE));
	}
}
