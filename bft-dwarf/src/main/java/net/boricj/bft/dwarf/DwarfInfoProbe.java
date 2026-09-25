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
package net.boricj.bft.dwarf;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.dwarf.constants.DwarfVersion;

/**
 * Utility methods to inspect DWARF units.
 */
public final class DwarfInfoProbe {
	private DwarfInfoProbe() {
		// Utility class - prevent instantiation
	}

	/**
	 * Reads the DWARF version of each compilation unit in a .debug_info payload.
	 *
	 * <p>This method supports classic 32-bit and DWARF64 unit headers.
	 *
	 * @param debugInfo bytes of .debug_info
	 * @param byteOrder endianness of the enclosing object format
	 * @return immutable list of unit versions in encounter order
	 * @throws IOException if parsing fails due to malformed data
	 */
	public static List<DwarfVersion> readCompilationUnitVersions(byte[] debugInfo, ByteOrder byteOrder)
			throws IOException {
		Objects.requireNonNull(debugInfo);
		Objects.requireNonNull(byteOrder);

		ByteInputStream bis = new ByteInputStream(new ByteArrayInputStream(debugInfo), byteOrder);
		List<DwarfVersion> versions = new ArrayList<>();

		while (bis.available() > 0) {
			long unitLengthField = Integer.toUnsignedLong(bis.readInt());
			long unitLength;
			if (unitLengthField == 0xffff_ffffL) {
				unitLength = bis.readLong();
			} else {
				unitLength = unitLengthField;
			}

			if (unitLength < Short.BYTES) {
				throw new EOFException("Invalid DWARF unit length: " + unitLength);
			}

			int unitVersion = bis.readUnsignedShort();
			versions.add(DwarfVersion.valueFrom(unitVersion));

			long bytesConsumedAfterLength = Short.BYTES;
			long remaining = unitLength - bytesConsumedAfterLength;
			long skipped = bis.skipBytes((int) remaining);
			if (skipped != remaining) {
				throw new EOFException("Truncated DWARF unit payload");
			}
		}

		return Collections.unmodifiableList(versions);
	}
}
