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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.omf.OmfFile;
import net.boricj.bft.omf.OmfRecord;
import net.boricj.bft.omf.OmfUtils;
import net.boricj.bft.omf.constants.OmfRecordType;

/**
 * PUBDEF - Public Names Definition Record (32-bit)
 * Defines public symbols exported by the module.
 */
public class OmfRecordPubdef extends OmfRecord {
	private final OmfRecordGrpdef group;
	private final OmfRecordSegdef segment;
	private final int baseFrame; // Only if segmentIndex == 0
	private final List<PublicSymbol> symbols;
	private final byte specificTypeValue;

	/**
	 * Parses a PUBDEF record from the input stream.
	 *
	 * @param file the parent OMF file
	 * @param bis the input stream
	 * @throws IOException if an I/O error occurs
	 */
	public OmfRecordPubdef(OmfFile file, ByteInputStream bis) throws IOException {
		super(file, OmfRecordType.PUBDEF);

		int groupIndex = OmfUtils.readIndex(bis);
		int segmentIndex = OmfUtils.readIndex(bis);
		this.group = groupIndex == 0 ? null : file.getGroupByIndex(groupIndex);
		this.segment = segmentIndex == 0 ? null : file.getSegmentByIndex(segmentIndex);

		if (segment == null) {
			this.baseFrame = bis.readUnsignedShort();
		} else {
			this.baseFrame = 0;
		}

		byte parsedType = getParsingTypeValue();
		boolean isKnown32Bit = parsedType == (byte) 0x91;
		boolean isKnown16Bit = parsedType == (byte) 0x90;

		List<PublicSymbol> parsedSymbols = new ArrayList<>();
		while (bis.available() > 0) {
			String name = bis.readByteLengthString(StandardCharsets.US_ASCII);

			long symbolOffset;
			if (isKnown32Bit) {
				symbolOffset = Integer.toUnsignedLong(bis.readInt());
			} else if (isKnown16Bit) {
				symbolOffset = bis.readUnsignedShort();
			} else {
				int remaining = bis.available();
				if (remaining >= 5) {
					symbolOffset = Integer.toUnsignedLong(bis.readInt());
				} else {
					symbolOffset = bis.readUnsignedShort();
				}
			}

			int typeIndex = OmfUtils.readIndex(bis);
			parsedSymbols.add(new PublicSymbol(name, symbolOffset, typeIndex));
		}

		this.symbols = Collections.unmodifiableList(parsedSymbols);

		// Store the original type from parsing, or determine from offsets
		if (parsedType != 0) {
			this.specificTypeValue = parsedType;
		} else {
			// Fall back to heuristic: check if any offset exceeds 16-bit
			boolean use32Bit = false;
			for (PublicSymbol sym : parsedSymbols) {
				if (sym.offset > 0xFFFF) {
					use32Bit = true;
					break;
				}
			}
			this.specificTypeValue = use32Bit ? (byte) 0x91 : (byte) 0x90;
		}
	}

	/**
	 * Creates a new PUBDEF record.
	 *
	 * @param file the parent OMF file
	 * @param group the group (may be null)
	 * @param segment the segment (may be null if baseFrame is used)
	 * @param baseFrame the base frame number (used when segment is null)
	 * @param symbols the list of public symbols
	 */
	public OmfRecordPubdef(
			OmfFile file, OmfRecordGrpdef group, OmfRecordSegdef segment, int baseFrame, List<PublicSymbol> symbols) {
		super(file, OmfRecordType.PUBDEF);
		Objects.requireNonNull(symbols);

		this.group = group;
		this.segment = segment;
		this.baseFrame = baseFrame;
		this.symbols = Collections.unmodifiableList(new ArrayList<>(symbols));
		// Determine variant based on offsets
		boolean use32Bit = false;
		for (PublicSymbol sym : symbols) {
			if (sym.offset > 0xFFFF) {
				use32Bit = true;
				break;
			}
		}
		this.specificTypeValue = use32Bit ? (byte) 0x91 : (byte) 0x90;
	}

	@Override
	public byte getSpecificTypeValue() {
		return specificTypeValue;
	}

	/**
	 * Returns the associated group.
	 *
	 * @return the group, or null
	 */
	public OmfRecordGrpdef getGroup() {
		return group;
	}

	/**
	 * Returns the associated segment.
	 *
	 * @return the segment, or null
	 */
	public OmfRecordSegdef getSegment() {
		return segment;
	}

	/**
	 * Returns the base frame number (when segment is null).
	 *
	 * @return the base frame
	 */
	public int getBaseFrame() {
		return baseFrame;
	}

	/**
	 * Returns the list of public symbols.
	 *
	 * @return the public symbols
	 */
	public List<PublicSymbol> getSymbols() {
		return symbols;
	}

	@Override
	public void write(ByteOutputStream bos) throws IOException {
		OmfUtils.writeIndex(bos, group == null ? 0 : getFile().indexOfGroup(group));
		OmfUtils.writeIndex(bos, segment == null ? 0 : getFile().indexOfSegment(segment));

		if (segment == null) {
			bos.writeShort(baseFrame);
		}

		for (PublicSymbol symbol : symbols) {
			bos.writeByteLengthString(symbol.name, StandardCharsets.US_ASCII);

			if (symbol.offset <= 0xFFFF) {
				bos.writeShort((int) symbol.offset);
			} else {
				bos.writeInt((int) symbol.offset);
			}

			OmfUtils.writeIndex(bos, symbol.typeIndex);
		}
	}

	/**
	 * A public symbol definition with name, offset, and type index.
	 *
	 * @param name the symbol name
	 * @param offset the offset within the segment
	 * @param typeIndex the type index (0 for no type info)
	 */
	public record PublicSymbol(String name, long offset, int typeIndex) {}
}
