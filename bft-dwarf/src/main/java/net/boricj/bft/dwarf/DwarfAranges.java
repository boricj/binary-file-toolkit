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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.dwarf.constants.DwarfVersion;

/**
 * Semantic representation of a DWARF .debug_aranges section.
 */
public final class DwarfAranges {
	private final ByteOrder byteOrder;
	private final List<DwarfArangesSet> sets;

	/**
	 * Creates a .debug_aranges model.
	 *
	 * @param byteOrder section byte order
	 * @param sets ordered address-range sets
	 */
	public DwarfAranges(ByteOrder byteOrder, List<DwarfArangesSet> sets) {
		this.byteOrder = Objects.requireNonNull(byteOrder);
		this.sets = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(sets)));
	}

	/**
	 * Returns byte order used by this section.
	 *
	 * @return section byte order
	 */
	public ByteOrder getByteOrder() {
		return byteOrder;
	}

	/**
	 * Returns parsed range sets in encounter order.
	 *
	 * @return immutable list of range sets
	 */
	public List<DwarfArangesSet> getSets() {
		return sets;
	}

	/**
	 * Parses raw .debug_aranges bytes.
	 *
	 * @param bytes section bytes
	 * @param byteOrder section byte order
	 * @return parsed .debug_aranges model
	 * @throws IOException if parsing fails
	 */
	public static DwarfAranges parse(byte[] bytes, ByteOrder byteOrder) throws IOException {
		Objects.requireNonNull(bytes);
		Objects.requireNonNull(byteOrder);

		ByteInputStream bis = new ByteInputStream(new ByteArrayInputStream(bytes), byteOrder);
		List<DwarfArangesSet> sets = new ArrayList<>();
		while (bis.available() > 0) {
			long sectionOffset = bis.getCount();
			long initialLength = Integer.toUnsignedLong(bis.readInt());
			boolean dwarf64 = initialLength == 0xffff_ffffL;
			long unitLength = dwarf64 ? bis.readLong() : initialLength;
			byte[] unitBytes = new byte[(int) unitLength];
			bis.readFully(unitBytes);
			sets.add(parseSet(sectionOffset, unitBytes, byteOrder, dwarf64));
		}
		return new DwarfAranges(byteOrder, sets);
	}

	private static DwarfArangesSet parseSet(long sectionOffset, byte[] unitBytes, ByteOrder byteOrder, boolean dwarf64)
			throws IOException {
		ByteInputStream bis = new ByteInputStream(new ByteArrayInputStream(unitBytes), byteOrder);
		DwarfVersion version = DwarfVersion.valueFrom(bis.readUnsignedShort());
		long debugInfoOffset = dwarf64 ? bis.readLong() : Integer.toUnsignedLong(bis.readInt());
		int addressSize = bis.readUnsignedByte();
		int segmentSize = bis.readUnsignedByte();

		int headerSizeExcludingInitialLength = Short.BYTES + (dwarf64 ? Long.BYTES : Integer.BYTES) + 1 + 1;
		int totalHeaderSize = headerSizeExcludingInitialLength + (dwarf64 ? 12 : 4);
		int tupleSize = Math.max(1, addressSize + segmentSize + addressSize);
		int alignedHeaderSize = align(totalHeaderSize, tupleSize);
		int paddingBytes = alignedHeaderSize - totalHeaderSize;
		if (paddingBytes > 0) {
			bis.skipBytes(paddingBytes);
		}

		List<DwarfArangesDescriptor> descriptors = new ArrayList<>();
		while (bis.available() > 0) {
			long segment = segmentSize > 0 ? readUnsignedSizedValue(bis, segmentSize) : 0;
			long address = readUnsignedSizedValue(bis, addressSize);
			long length = readUnsignedSizedValue(bis, addressSize);
			if (segment == 0 && address == 0 && length == 0) {
				break;
			}
			descriptors.add(new DwarfArangesDescriptor(segment, address, length));
		}

		return new DwarfArangesSet(
				sectionOffset,
				unitBytes.length,
				dwarf64,
				version,
				debugInfoOffset,
				addressSize,
				segmentSize,
				descriptors);
	}

	/**
	 * Serializes this .debug_aranges model to raw bytes.
	 *
	 * @return serialized section bytes
	 * @throws IOException if serialization fails
	 */
	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		write(baos);
		return baos.toByteArray();
	}

	/**
	 * Writes this .debug_aranges model to the provided output stream.
	 *
	 * @param outputStream destination stream
	 * @throws IOException if writing fails
	 */
	public void write(OutputStream outputStream) throws IOException {
		ByteOutputStream bos = new ByteOutputStream(outputStream, byteOrder);
		for (DwarfArangesSet set : sets) {
			byte[] body = set.toBody(byteOrder);
			if (set.dwarf64()) {
				bos.writeInt(0xffff_ffff);
				bos.writeLong(body.length);
			} else {
				bos.writeInt(body.length);
			}
			bos.write(body);
		}
	}

	private static long readUnsignedSizedValue(ByteInputStream bis, int size) throws IOException {
		return switch (size) {
			case 0 -> 0;
			case 1 -> bis.readUnsignedByte();
			case 2 -> bis.readUnsignedShort();
			case 4 -> Integer.toUnsignedLong(bis.readInt());
			case 8 -> bis.readLong();
			default -> throw new IllegalArgumentException("Unsupported DWARF aranges field size: " + size);
		};
	}

	private static void writeSizedValue(ByteOutputStream bos, long value, int size) throws IOException {
		switch (size) {
			case 0 -> {}
			case 1 -> bos.writeByte((int) value);
			case 2 -> bos.writeShort((int) value);
			case 4 -> bos.writeInt((int) value);
			case 8 -> bos.writeLong(value);
			default -> throw new IllegalArgumentException("Unsupported DWARF aranges field size: " + size);
		}
	}

	private static int align(int value, int alignment) {
		int remainder = value % alignment;
		return remainder == 0 ? value : value + alignment - remainder;
	}

	/**
	 * One address-range set from .debug_aranges.
	 *
	 * @param sectionOffset section-relative offset of this set
	 * @param unitLength payload length excluding the initial length field encoding
	 * @param dwarf64 true when encoded using DWARF64 initial length form
	 * @param version DWARF version used by the set
	 * @param debugInfoOffset referenced .debug_info compilation-unit offset
	 * @param addressSize address size used by descriptors
	 * @param segmentSize segment selector size used by descriptors
	 * @param descriptors address-range descriptors terminated by an implicit zero tuple in encoding
	 */
	public record DwarfArangesSet(
			long sectionOffset,
			long unitLength,
			boolean dwarf64,
			DwarfVersion version,
			long debugInfoOffset,
			int addressSize,
			int segmentSize,
			List<DwarfArangesDescriptor> descriptors) {
		/**
		 * Creates an immutable address-range set.
		 *
		 * @param sectionOffset section-relative offset of this set
		 * @param unitLength payload length excluding the initial length field encoding
		 * @param dwarf64 true when encoded using DWARF64 initial length form
		 * @param version DWARF version used by the set
		 * @param debugInfoOffset referenced .debug_info compilation-unit offset
		 * @param addressSize address size used by descriptors
		 * @param segmentSize segment selector size used by descriptors
		 * @param descriptors address-range descriptors
		 */
		public DwarfArangesSet {
			descriptors = Collections.unmodifiableList(new ArrayList<>(descriptors));
		}

		byte[] toBody(ByteOrder byteOrder) throws IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ByteOutputStream bos = new ByteOutputStream(baos, byteOrder);
			bos.writeShort(version.getValue());
			if (dwarf64) {
				bos.writeLong(debugInfoOffset);
			} else {
				bos.writeInt((int) debugInfoOffset);
			}
			bos.writeByte(addressSize);
			bos.writeByte(segmentSize);

			int headerSizeExcludingInitialLength = Short.BYTES + (dwarf64 ? Long.BYTES : Integer.BYTES) + 1 + 1;
			int totalHeaderSize = headerSizeExcludingInitialLength + (dwarf64 ? 12 : 4);
			int tupleSize = Math.max(1, addressSize + segmentSize + addressSize);
			int alignedHeaderSize = align(totalHeaderSize, tupleSize);
			for (int index = totalHeaderSize; index < alignedHeaderSize; index++) {
				bos.writeByte(0);
			}

			for (DwarfArangesDescriptor descriptor : descriptors) {
				writeSizedValue(bos, descriptor.segment(), segmentSize);
				writeSizedValue(bos, descriptor.address(), addressSize);
				writeSizedValue(bos, descriptor.length(), addressSize);
			}

			writeSizedValue(bos, 0, segmentSize);
			writeSizedValue(bos, 0, addressSize);
			writeSizedValue(bos, 0, addressSize);
			return baos.toByteArray();
		}
	}

	/**
	 * One address-range descriptor tuple.
	 *
	 * @param segment optional segment selector value, or zero when unused
	 * @param address start address of the covered range
	 * @param length byte length of the covered range
	 */
	public record DwarfArangesDescriptor(long segment, long address, long length) {}
}
