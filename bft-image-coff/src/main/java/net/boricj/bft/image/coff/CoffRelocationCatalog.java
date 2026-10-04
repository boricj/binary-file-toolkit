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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.boricj.bft.coff.constants.CoffMachine;
import net.boricj.bft.coff.constants.CoffRelocationType;
import net.boricj.bft.coff.machines.amd64.CoffRelocationType_amd64;
import net.boricj.bft.coff.machines.i386.CoffRelocationType_i386;
import net.boricj.bft.image.RelocationFieldCodec;
import net.boricj.bft.image.RelocationOperation;
import net.boricj.bft.image.relocs.priv.ByteFieldCodec;

final class CoffRelocationCatalog {
	record RelocationDescriptor(RelocationOperation operation, RelocationFieldCodec fieldCodec) {}

	private record RelocationKey(CoffMachine machine, int typeValue) {}

	private static final RelocationDescriptor ABSOLUTE_U32LE =
			new RelocationDescriptor(RelocationOperation.ABSOLUTE, ByteFieldCodec.U32LE);
	private static final RelocationDescriptor ABSOLUTE_U32BE =
			new RelocationDescriptor(RelocationOperation.ABSOLUTE, ByteFieldCodec.U32BE);
	private static final RelocationDescriptor PCREL_S32LE =
			new RelocationDescriptor(RelocationOperation.PC_RELATIVE, ByteFieldCodec.S32LE);
	private static final RelocationDescriptor PCREL_S32BE =
			new RelocationDescriptor(RelocationOperation.PC_RELATIVE, ByteFieldCodec.S32BE);
	private static final RelocationDescriptor REL32_LE =
			new RelocationDescriptor(RelocationOperation.PC_RELATIVE, ByteFieldCodec.S32LE);
	private static final RelocationDescriptor ABSOLUTE_ADDR64 =
			new RelocationDescriptor(RelocationOperation.ABSOLUTE, ByteFieldCodec.U64LE);

	private static final Map<RelocationKey, RelocationDescriptor> TYPE_TO_DESCRIPTOR = new HashMap<>();
	private static final Map<CoffMachine, Map<RelocationDescriptor, CoffRelocationType>> DESCRIPTOR_TO_TYPE =
			new EnumMap<>(CoffMachine.class);

	static {
		register(CoffRelocationType_i386.IMAGE_REL_I386_DIR32, ABSOLUTE_U32LE);
		register(CoffRelocationType_i386.IMAGE_REL_I386_REL32, PCREL_S32LE);
		register(CoffRelocationType_i386.IMAGE_REL_I386_DIR32NB, ABSOLUTE_U32LE);
		register(CoffRelocationType_i386.IMAGE_REL_I386_SECREL, ABSOLUTE_U32LE);
		register(CoffRelocationType_i386.IMAGE_REL_I386_SECTION, ABSOLUTE_U32LE);
		register(CoffRelocationType_amd64.IMAGE_REL_AMD64_ADDR64, ABSOLUTE_ADDR64);
		register(CoffRelocationType_amd64.IMAGE_REL_AMD64_ADDR32, ABSOLUTE_U32LE);
		register(CoffRelocationType_amd64.IMAGE_REL_AMD64_ADDR32NB, ABSOLUTE_U32LE);
		register(CoffRelocationType_amd64.IMAGE_REL_AMD64_REL32, REL32_LE);
		register(CoffRelocationType_amd64.IMAGE_REL_AMD64_SECTION, ABSOLUTE_U32LE);
		register(CoffRelocationType_amd64.IMAGE_REL_AMD64_SECREL, ABSOLUTE_U32LE);
	}

	private CoffRelocationCatalog() {
		// utility class
	}

	static RelocationDescriptor descriptorFromType(CoffRelocationType type) {
		Objects.requireNonNull(type, "type");
		RelocationDescriptor descriptor = TYPE_TO_DESCRIPTOR.get(new RelocationKey(type.getMachine(), type.getValue()));
		if (descriptor == null) {
			throw new IllegalArgumentException(
					String.format("Unsupported COFF relocation type %s for machine %s", type, type.getMachine()));
		}
		return descriptor;
	}

	static CoffRelocationType typeFromDescriptor(
			CoffMachine machine, RelocationOperation operation, RelocationFieldCodec fieldCodec) {
		Objects.requireNonNull(machine, "machine");
		Objects.requireNonNull(operation, "operation");
		Objects.requireNonNull(fieldCodec, "fieldCodec");
		Map<RelocationDescriptor, CoffRelocationType> perMachine = DESCRIPTOR_TO_TYPE.get(machine);
		if (perMachine == null) {
			throw new IllegalArgumentException(String.format("Unsupported COFF machine: %s", machine));
		}
		RelocationDescriptor descriptor = new RelocationDescriptor(operation, fieldCodec);
		CoffRelocationType type = perMachine.get(descriptor);
		if (type == null) {
			throw new IllegalArgumentException(String.format(
					"No COFF relocation type mapping for operation '%s' and codec '%s' on machine %s",
					operation, fieldCodec, machine));
		}
		return type;
	}

	private static void register(CoffRelocationType type, RelocationDescriptor descriptor) {
		RelocationKey key = new RelocationKey(type.getMachine(), type.getValue());
		TYPE_TO_DESCRIPTOR.put(key, descriptor);
		DESCRIPTOR_TO_TYPE
				.computeIfAbsent(type.getMachine(), ignored -> new HashMap<>())
				.put(descriptor, type);
	}
}
