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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.boricj.bft.elf.constants.ElfMachine;
import net.boricj.bft.elf.constants.ElfRelocationType;
import net.boricj.bft.elf.machines.amd64.ElfRelocationType_amd64;
import net.boricj.bft.elf.machines.i386.ElfRelocationType_i386;
import net.boricj.bft.elf.machines.mips.ElfRelocationType_Mips;
import net.boricj.bft.image.RelocationFieldCodec;
import net.boricj.bft.image.RelocationOperation;
import net.boricj.bft.image.relocs.priv.BitShiftedAdjustedFieldCodec;
import net.boricj.bft.image.relocs.priv.BitShiftedFieldCodec;
import net.boricj.bft.image.relocs.priv.ByteFieldCodec;

/**
 * Registry mapping ELF relocation types to architecture catalogs in bft-image.
 */
final class ElfRelocationCatalog {
	record RelocationDescriptor(RelocationOperation operation, RelocationFieldCodec fieldCodec) {}

	private record RelocationKey(ElfMachine machine, int typeValue) {}

	private static final RelocationDescriptor ABSOLUTE_U32LE =
			new RelocationDescriptor(RelocationOperation.ABSOLUTE, ByteFieldCodec.U32LE);
	private static final RelocationDescriptor PCREL_S32LE =
			new RelocationDescriptor(RelocationOperation.PC_RELATIVE, ByteFieldCodec.S32LE);
	private static final RelocationDescriptor PLT_S32LE =
			new RelocationDescriptor(RelocationOperation.PLT, ByteFieldCodec.S32LE);
	private static final RelocationDescriptor GOT_S32LE =
			new RelocationDescriptor(RelocationOperation.GOT, ByteFieldCodec.S32LE);
	private static final RelocationDescriptor REGIONREL_S32LE =
			new RelocationDescriptor(RelocationOperation.REGION_RELATIVE, ByteFieldCodec.S32LE);
	private static final RelocationDescriptor ABSOLUTE_HI16_ADJ = new RelocationDescriptor(
			RelocationOperation.ABSOLUTE,
			BitShiftedAdjustedFieldCodec.of(2, java.nio.ByteOrder.LITTLE_ENDIAN, 0, 16, 16, true));
	private static final RelocationDescriptor ABSOLUTE_S16LE =
			new RelocationDescriptor(RelocationOperation.ABSOLUTE, ByteFieldCodec.S16LE);
	private static final RelocationDescriptor GOT_S16LE =
			new RelocationDescriptor(RelocationOperation.GOT, ByteFieldCodec.S16LE);
	private static final RelocationDescriptor PLT_S16LE =
			new RelocationDescriptor(RelocationOperation.PLT, ByteFieldCodec.S16LE);
	private static final RelocationDescriptor REGIONREL_26_SH2 = new RelocationDescriptor(
			RelocationOperation.REGION_RELATIVE,
			BitShiftedFieldCodec.of(4, java.nio.ByteOrder.LITTLE_ENDIAN, 0, 26, 2, true));

	private static final Map<RelocationKey, RelocationDescriptor> TYPE_TO_DESCRIPTOR = new HashMap<>();
	private static final Map<ElfMachine, Map<RelocationDescriptor, ElfRelocationType>> DESCRIPTOR_TO_TYPE =
			new EnumMap<>(ElfMachine.class);
	private static final Map<ElfMachine, Boolean> MACHINE_EXPLICIT_ADDENDS = new EnumMap<>(ElfMachine.class);

	static {
		register(ElfRelocationType_amd64.R_X86_64_32, ABSOLUTE_U32LE);
		register(ElfRelocationType_amd64.R_X86_64_PC32, PCREL_S32LE);
		register(ElfRelocationType_amd64.R_X86_64_PLT32, PLT_S32LE);

		register(ElfRelocationType_i386.R_386_32, ABSOLUTE_U32LE);
		register(ElfRelocationType_i386.R_386_PC32, PCREL_S32LE);
		register(ElfRelocationType_i386.R_386_GOTPC, GOT_S32LE);
		register(ElfRelocationType_i386.R_386_GOTOFF, REGIONREL_S32LE);
		register(ElfRelocationType_i386.R_386_PLT32, PLT_S32LE);

		register(ElfRelocationType_Mips.R_MIPS_32, ABSOLUTE_U32LE);
		register(ElfRelocationType_Mips.R_MIPS_HI16, ABSOLUTE_HI16_ADJ);
		register(ElfRelocationType_Mips.R_MIPS_LO16, ABSOLUTE_S16LE);
		register(ElfRelocationType_Mips.R_MIPS_GOT16, GOT_S16LE);
		register(ElfRelocationType_Mips.R_MIPS_CALL16, PLT_S16LE);
		register(ElfRelocationType_Mips.R_MIPS_JALR, REGIONREL_26_SH2);

		MACHINE_EXPLICIT_ADDENDS.put(ElfMachine.EM_X86_64, true);
		MACHINE_EXPLICIT_ADDENDS.put(ElfMachine.EM_386, false);
		MACHINE_EXPLICIT_ADDENDS.put(ElfMachine.EM_MIPS, false);
	}

	private ElfRelocationCatalog() {
		// Utility class.
	}

	static RelocationDescriptor descriptorFromType(ElfRelocationType type) {
		Objects.requireNonNull(type, "type");
		RelocationDescriptor descriptor = TYPE_TO_DESCRIPTOR.get(new RelocationKey(type.getMachine(), type.getValue()));
		if (descriptor == null) {
			throw new IllegalArgumentException(
					String.format("Unsupported ELF relocation type %s for machine %s", type, type.getMachine()));
		}
		return descriptor;
	}

	static ElfRelocationType typeFromDescriptor(
			ElfMachine machine, RelocationOperation operation, RelocationFieldCodec fieldCodec) {
		Objects.requireNonNull(machine, "machine");
		Objects.requireNonNull(operation, "operation");
		Objects.requireNonNull(fieldCodec, "fieldCodec");
		Map<RelocationDescriptor, ElfRelocationType> perMachine = DESCRIPTOR_TO_TYPE.get(machine);
		if (perMachine == null) {
			throw new IllegalArgumentException(String.format("Unsupported ELF machine: %s", machine));
		}

		RelocationDescriptor descriptor = new RelocationDescriptor(operation, fieldCodec);
		ElfRelocationType type = perMachine.get(descriptor);
		if (type == null) {
			throw new IllegalArgumentException(String.format(
					"No ELF relocation type mapping for operation '%s' and codec '%s' on machine %s",
					operation, fieldCodec, machine));
		}
		return type;
	}

	static boolean usesExplicitAddends(ElfMachine machine) {
		Objects.requireNonNull(machine, "machine");
		Boolean explicit = MACHINE_EXPLICIT_ADDENDS.get(machine);
		if (explicit == null) {
			throw new IllegalArgumentException(String.format("Unsupported ELF machine: %s", machine));
		}
		return explicit;
	}

	private static void register(ElfRelocationType type, RelocationDescriptor descriptor) {
		RelocationKey key = new RelocationKey(type.getMachine(), type.getValue());
		TYPE_TO_DESCRIPTOR.put(key, descriptor);
		DESCRIPTOR_TO_TYPE
				.computeIfAbsent(type.getMachine(), ignored -> new HashMap<>())
				.put(descriptor, type);
	}
}
