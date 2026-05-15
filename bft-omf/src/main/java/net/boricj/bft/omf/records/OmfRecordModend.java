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
package net.boricj.bft.omf.records;

import java.io.IOException;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.omf.OmfFile;
import net.boricj.bft.omf.OmfRecord;
import net.boricj.bft.omf.constants.OmfRecordType;

/**
 * MODEND - Module End Record (32-bit)
 * Marks the end of an object module.
 */
public class OmfRecordModend extends OmfRecord {
	/**
	 * Specific MODEND record type variants.
	 */
	public enum SpecificType {
		/** 16-bit MODEND variant (record type 0x8A). */
		MODEND_16((byte) 0x8A),
		/** 32-bit MODEND variant (record type 0x8B, MODE32 in Turbo Dump). */
		MODEND_32((byte) 0x8B);

		private final byte value;

		SpecificType(byte value) {
			this.value = value;
		}

		/**
		 * Returns the encoded OMF record type byte for this MODEND variant.
		 *
		 * @return the MODEND record type byte value
		 */
		public byte getValue() {
			return value;
		}
	}

	private final boolean isMainModule;
	private final boolean hasStartAddress;
	private final byte[] startAddressFixup;
	private final byte specificTypeValue;

	/**
	 * Parses a MODEND record from the input stream.
	 *
	 * @param file the parent OMF file
	 * @param bis the input stream
	 * @throws IOException if an I/O error occurs
	 */
	public OmfRecordModend(OmfFile file, ByteInputStream bis) throws IOException {
		super(file, OmfRecordType.MODEND);

		// Read module type flags
		byte flags = bis.readByte();
		this.isMainModule = (flags & 0x80) != 0;
		this.hasStartAddress = (flags & 0x40) != 0;

		// Read optional start address fixup data
		this.startAddressFixup = bis.readAllBytes();

		// Store the original type from parsing, or default to 16-bit variant
		byte parsedType = getParsingTypeValue();
		this.specificTypeValue = (parsedType != 0) ? parsedType : (byte) 0x8A;
	}

	/**
	 * Creates a new MODEND record.
	 *
	 * @param file the parent OMF file
	 * @param isMainModule whether this is the main module
	 * @param hasStartAddress whether a start address is specified
	 * @param startAddressFixup optional start address fixup data
	 */
	public OmfRecordModend(OmfFile file, boolean isMainModule, boolean hasStartAddress, byte[] startAddressFixup) {
		this(file, isMainModule, hasStartAddress, startAddressFixup, SpecificType.MODEND_16);
	}

	/**
	 * Creates a new MODEND record with a specific 16/32-bit variant.
	 *
	 * @param file the parent OMF file
	 * @param isMainModule whether this is the main module
	 * @param hasStartAddress whether a start address is specified
	 * @param startAddressFixup optional start address fixup data
	 * @param specificType the specific MODEND variant to emit
	 */
	public OmfRecordModend(
			OmfFile file,
			boolean isMainModule,
			boolean hasStartAddress,
			byte[] startAddressFixup,
			SpecificType specificType) {
		super(file, OmfRecordType.MODEND);
		this.isMainModule = isMainModule;
		this.hasStartAddress = hasStartAddress;
		this.startAddressFixup = startAddressFixup != null ? startAddressFixup.clone() : new byte[0];
		this.specificTypeValue = specificType.getValue();
	}

	@Override
	public byte getSpecificTypeValue() {
		return specificTypeValue;
	}

	/**
	 * Returns whether this is the main module.
	 *
	 * @return true if this is the main module
	 */
	public boolean isMainModule() {
		return isMainModule;
	}

	/**
	 * Returns whether a start address is specified.
	 *
	 * @return true if a start address is specified
	 */
	public boolean hasStartAddress() {
		return hasStartAddress;
	}

	/**
	 * Returns the start address fixup data.
	 *
	 * @return the start address fixup data
	 */
	public byte[] getStartAddressFixup() {
		return startAddressFixup.clone();
	}

	@Override
	protected void write(ByteOutputStream bos) throws IOException {
		byte flags = 0;
		if (isMainModule) {
			flags |= 0x80;
		}
		if (hasStartAddress) {
			flags |= 0x40;
		}
		bos.writeByte(flags);
		bos.write(startAddressFixup);
	}
}
