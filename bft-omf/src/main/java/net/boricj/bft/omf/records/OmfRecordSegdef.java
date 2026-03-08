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
import net.boricj.bft.omf.OmfUtils;
import net.boricj.bft.omf.constants.OmfRecordType;

/**
 * SEGDEF - Segment Definition Record (32-bit)
 * Defines a logical segment and its attributes.
 */
public class OmfRecordSegdef extends OmfRecord {
	/**
	 * Segment alignment codes.
	 */
	public enum AlignmentCode {
		/** Alignment code 0. */
		CODE_0(0),
		/** Alignment code 1. */
		CODE_1(1),
		/** Alignment code 2. */
		CODE_2(2),
		/** Alignment code 3. */
		CODE_3(3),
		/** Alignment code 4. */
		CODE_4(4),
		/** Alignment code 5. */
		CODE_5(5),
		/** Alignment code 6. */
		CODE_6(6),
		/** Alignment code 7. */
		CODE_7(7);

		private final int value;

		AlignmentCode(int value) {
			this.value = value;
		}

		/**
		 * Returns the numeric value of this alignment code.
		 *
		 * @return the alignment code numeric value
		 */
		public int getValue() {
			return value;
		}

		/**
		 * Looks up an alignment code by its numeric value.
		 *
		 * @param value the numeric value
		 * @return the corresponding alignment code
		 * @throws IllegalArgumentException if the value is not recognized
		 */
		public static AlignmentCode valueFrom(int value) {
			for (AlignmentCode code : values()) {
				if (code.value == value) {
					return code;
				}
			}
			throw new IllegalArgumentException("Unknown SEGDEF alignment code: " + value);
		}
	}

	/**
	 * Segment combine codes.
	 */
	public enum CombineCode {
		/** Combine code 0. */
		CODE_0(0),
		/** Combine code 1. */
		CODE_1(1),
		/** Combine code 2. */
		CODE_2(2),
		/** Combine code 3. */
		CODE_3(3),
		/** Combine code 4. */
		CODE_4(4),
		/** Combine code 5. */
		CODE_5(5),
		/** Combine code 6. */
		CODE_6(6),
		/** Combine code 7. */
		CODE_7(7);

		private final int value;

		CombineCode(int value) {
			this.value = value;
		}

		/**
		 * Returns the numeric value of this combine code.
		 *
		 * @return the combine code numeric value
		 */
		public int getValue() {
			return value;
		}

		/**
		 * Looks up a combine code by its numeric value.
		 *
		 * @param value the numeric value
		 * @return the corresponding combine code
		 * @throws IllegalArgumentException if the value is not recognized
		 */
		public static CombineCode valueFrom(int value) {
			for (CombineCode code : values()) {
				if (code.value == value) {
					return code;
				}
			}
			throw new IllegalArgumentException("Unknown SEGDEF combine code: " + value);
		}
	}

	/**
	 * Segment attributes wrapper providing typed access to the attributes byte.
	 */
	public static final class Attributes {
		private final int rawValue;

		private Attributes(int rawValue) {
			if ((rawValue & ~0xFF) != 0) {
				throw new IllegalArgumentException("SEGDEF attributes must fit in one byte: " + rawValue);
			}
			this.rawValue = rawValue;
		}

		/**
		 * Creates attributes from a raw byte value.
		 *
		 * @param rawValue the raw attributes byte
		 * @return the attributes object
		 */
		public static Attributes ofRaw(int rawValue) {
			return new Attributes(rawValue);
		}

		/**
		 * Creates attributes from individual components.
		 *
		 * @param alignment the alignment code
		 * @param combine the combine code
		 * @param bigSegment whether this is a big segment
		 * @param use32Bit whether this uses 32-bit addressing
		 * @return the attributes object
		 */
		public static Attributes of(
				AlignmentCode alignment, CombineCode combine, boolean bigSegment, boolean use32Bit) {
			int raw = (alignment.getValue() << 5)
					| (combine.getValue() << 2)
					| ((bigSegment ? 1 : 0) << 1)
					| (use32Bit ? 1 : 0);
			return new Attributes(raw);
		}

		/**
		 * Returns the raw attributes byte value.
		 *
		 * @return the raw attributes value
		 */
		public int getRawValue() {
			return rawValue;
		}

		/**
		 * Returns the alignment code.
		 *
		 * @return the decoded alignment code
		 */
		public AlignmentCode getAlignmentCode() {
			return AlignmentCode.valueFrom((rawValue >> 5) & 0x07);
		}

		/**
		 * Returns the combine code.
		 *
		 * @return the decoded combine code
		 */
		public CombineCode getCombineCode() {
			return CombineCode.valueFrom((rawValue >> 2) & 0x07);
		}

		/**
		 * Returns whether this is a big segment.
		 *
		 * @return true if the segment is marked as big
		 */
		public boolean isBigSegment() {
			return (rawValue & 0x02) != 0;
		}

		/**
		 * Returns whether this uses 32-bit addressing.
		 *
		 * @return true if 32-bit addressing is enabled
		 */
		public boolean isUse32Bit() {
			return (rawValue & 0x01) != 0;
		}
	}

	private final int attributes;
	private final long segmentLength;
	private final String segmentName;
	private final String className;
	private final String overlayName;
	private final byte specificTypeValue;

	/**
	 * Parses a SEGDEF record from the input stream.
	 *
	 * @param file the parent OMF file
	 * @param bis the input stream
	 * @throws IOException if an I/O error occurs
	 */
	public OmfRecordSegdef(OmfFile file, ByteInputStream bis) throws IOException {
		super(file, OmfRecordType.SEGDEF);

		byte[] data = bis.readAllBytes();
		int offset = 0;

		// Read attributes byte
		this.attributes = data[offset++] & 0xFF;

		// Try to parse with 16-bit length first, then 32-bit
		ParseResult result = tryParse(data, offset, 2);
		boolean parsed32Bit = false;
		if (result == null) {
			result = tryParse(data, offset, 4);
			parsed32Bit = true;
		}
		if (result == null) {
			throw new IOException("Failed to parse SEGDEF record");
		}

		this.segmentLength = result.segmentLength;
		this.segmentName = file.getLnameByIndex(result.segmentNameIndex);
		this.className = file.getLnameByIndex(result.classNameIndex);
		// Overlay name index can be 0 (meaning no overlay)
		this.overlayName = result.overlayNameIndex > 0 ? file.getLnameByIndex(result.overlayNameIndex) : "";

		// Store the original type from parsing, or the variant we detected
		byte parsedType = getParsingTypeValue();
		this.specificTypeValue = (parsedType != 0) ? parsedType : (parsed32Bit ? (byte) 0x99 : (byte) 0x98);
	}

	/**
	 * Creates a new SEGDEF record.
	 *
	 * @param file the parent OMF file
	 * @param attributes the raw attributes byte
	 * @param segmentLength the segment length
	 * @param segmentName the segment name
	 * @param className the class name
	 * @param overlayName the overlay name
	 */
	public OmfRecordSegdef(
			OmfFile file,
			int attributes,
			long segmentLength,
			String segmentName,
			String className,
			String overlayName) {
		super(file, OmfRecordType.SEGDEF);
		if ((attributes & ~0xFF) != 0) {
			throw new IllegalArgumentException("SEGDEF attributes must fit in one byte: " + attributes);
		}
		this.attributes = attributes;
		this.segmentLength = segmentLength;
		this.segmentName = segmentName;
		this.className = className;
		this.overlayName = overlayName;
		// Determine variant based on length
		this.specificTypeValue = (segmentLength > 0xFFFF) ? (byte) 0x99 : (byte) 0x98;
	}

	/**
	 * Creates a new SEGDEF record with typed attributes.
	 *
	 * @param file the parent OMF file
	 * @param attributes the typed attributes
	 * @param segmentLength the segment length
	 * @param segmentName the segment name
	 * @param className the class name
	 * @param overlayName the overlay name
	 */
	public OmfRecordSegdef(
			OmfFile file,
			Attributes attributes,
			long segmentLength,
			String segmentName,
			String className,
			String overlayName) {
		this(file, attributes.getRawValue(), segmentLength, segmentName, className, overlayName);
	}

	@Override
	public byte getSpecificTypeValue() {
		return specificTypeValue;
	}

	private static class ParseResult {
		long segmentLength;
		int segmentNameIndex;
		int classNameIndex;
		int overlayNameIndex;
	}

	private ParseResult tryParse(byte[] data, int startOffset, int lengthFieldSize) {
		if (data.length < startOffset + lengthFieldSize + 3) {
			return null;
		}

		try {
			int offset = startOffset;
			long segmentLength;
			if (lengthFieldSize == 2) {
				segmentLength = OmfUtils.u16le(data, offset);
			} else {
				segmentLength = OmfUtils.u32le(data, offset);
			}
			offset += lengthFieldSize;

			int[] offsetArr = {offset};
			int segmentNameIndex = OmfUtils.readIndex(data, offsetArr);
			int classNameIndex = OmfUtils.readIndex(data, offsetArr);
			int overlayNameIndex = OmfUtils.readIndex(data, offsetArr);

			if (offsetArr[0] != data.length) {
				return null;
			}

			ParseResult result = new ParseResult();
			result.segmentLength = segmentLength;
			result.segmentNameIndex = segmentNameIndex;
			result.classNameIndex = classNameIndex;
			result.overlayNameIndex = overlayNameIndex;
			return result;
		} catch (ArrayIndexOutOfBoundsException ex) {
			return null;
		}
	}

	/**
	 * Returns the raw attributes byte.
	 *
	 * @return the attributes byte
	 */
	public int getAttributes() {
		return attributes;
	}

	/**
	 * Returns the typed segment attributes.
	 *
	 * @return the segment attributes
	 */
	public Attributes getSegmentAttributes() {
		return Attributes.ofRaw(attributes);
	}

	/**
	 * Returns the segment length.
	 *
	 * @return the segment length
	 */
	public long getSegmentLength() {
		return segmentLength;
	}

	/**
	 * Returns the segment name.
	 *
	 * @return the segment name
	 */
	public String getSegmentName() {
		return segmentName;
	}

	/**
	 * Returns the class name.
	 *
	 * @return the class name
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Returns the overlay name.
	 *
	 * @return the overlay name
	 */
	public String getOverlayName() {
		return overlayName;
	}

	@Override
	public void write(ByteOutputStream bos) throws IOException {
		bos.writeByte(attributes);

		// Use the format matching the specific type
		if (specificTypeValue == (byte) 0x99) {
			bos.writeInt((int) segmentLength);
		} else {
			bos.writeShort((int) segmentLength);
		}

		OmfUtils.writeIndex(bos, getFile().indexOfLnameBefore(segmentName, this));
		OmfUtils.writeIndex(bos, getFile().indexOfLnameBefore(className, this));
		OmfUtils.writeIndex(bos, getFile().indexOfLnameBefore(overlayName, this));
	}
}
