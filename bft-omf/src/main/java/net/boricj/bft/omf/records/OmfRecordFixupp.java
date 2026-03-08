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
 * FIXUPP - Fixup Record (32-bit)
 * Contains relocation information for code and data.
 */
public class OmfRecordFixupp extends OmfRecord {
	/**
	 * Specific FIXUPP record type variants (16-bit vs 32-bit).
	 */
	public enum SpecificType {
		/** 16-bit FIXUPP record. */
		FIXUPP_16((byte) 0x9C),
		/** 32-bit FIXUPP record. */
		FIXUPP_32((byte) 0x9D);

		private final byte value;

		SpecificType(byte value) {
			this.value = value;
		}

		/**
		 * Returns the record type byte value.
		 *
		 * @return the encoded specific type value
		 */
		public byte getValue() {
			return value;
		}

		/**
		 * Looks up a specific type by its byte value.
		 *
		 * @param value the record type byte
		 * @return the corresponding specific type
		 * @throws IllegalArgumentException if the value is not recognized
		 */
		public static SpecificType valueFrom(byte value) {
			for (SpecificType type : values()) {
				if (type.value == value) {
					return type;
				}
			}
			throw new IllegalArgumentException(
					"Unknown FIXUPP specific type value: 0x" + Integer.toHexString(value & 0xFF));
		}
	}

	/**
	 * Frame determination methods for fixups.
	 */
	public enum FrameMethod {
		/** Frame determined by SEGDEF index. */
		SEGDEF_INDEX(0),
		/** Frame determined by GRPDEF index. */
		GRPDEF_INDEX(1),
		/** Frame determined by EXTDEF index. */
		EXTDEF_INDEX(2),
		/** Frame specified explicitly. */
		EXPLICIT_FRAME_NUMBER(3),
		/** Frame is the previous data record's segment. */
		PREVIOUS_DATA_RECORD_SEGMENT(4),
		/** Frame is the target. */
		TARGET(5),
		/** Invalid method 6. */
		INVALID_6(6),
		/** Reserved method 7. */
		RESERVED_7(7);

		private final int value;

		FrameMethod(int value) {
			this.value = value;
		}

		/**
		 * Returns the numeric value of this frame method.
		 *
		 * @return the frame method numeric value
		 */
		public int getValue() {
			return value;
		}

		/**
		 * Looks up a frame method by its numeric value.
		 *
		 * @param value the numeric value
		 * @return the corresponding frame method
		 * @throws IllegalArgumentException if the value is not recognized
		 */
		public static FrameMethod valueFrom(int value) {
			for (FrameMethod method : values()) {
				if (method.value == value) {
					return method;
				}
			}
			throw new IllegalArgumentException("Unknown FIXUPP frame method: " + value);
		}
	}

	/**
	 * Target determination methods for fixups.
	 */
	public enum TargetMethod {
		/** Target determined by SEGDEF index. */
		SEGDEF_INDEX(0),
		/** Target determined by GRPDEF index. */
		GRPDEF_INDEX(1),
		/** Target determined by EXTDEF index. */
		EXTDEF_INDEX(2),
		/** Target frame number specified explicitly. */
		EXPLICIT_FRAME_NUMBER(3);

		private final int value;

		TargetMethod(int value) {
			this.value = value;
		}

		/**
		 * Returns the numeric value of this target method.
		 *
		 * @return the target method numeric value
		 */
		public int getValue() {
			return value;
		}

		/**
		 * Looks up a target method by its numeric value.
		 *
		 * @param value the numeric value
		 * @return the corresponding target method
		 * @throws IllegalArgumentException if the value is not recognized
		 */
		public static TargetMethod valueFrom(int value) {
			for (TargetMethod method : values()) {
				if (method.value == value) {
					return method;
				}
			}
			throw new IllegalArgumentException("Unknown FIXUPP target method: " + value);
		}
	}

	/**
	 * Base interface for FIXUPP subrecords (threads and fixups).
	 */
	private interface Subrecord {
		void write(ByteOutputStream bos, byte specificTypeValue) throws IOException;
	}

	/**
	 * Thread subrecord for establishing fixup context.
	 */
	public static class ThreadEntry implements Subrecord {
		private final boolean targetThread;
		private final FrameMethod method;
		private final int threadNumber;
		private final Integer datum;

		/**
		 * Creates a thread entry.
		 *
		 * @param targetThread whether this is a target thread (vs frame thread)
		 * @param method the frame method value
		 * @param threadNumber the thread number (0-3)
		 * @param datum the optional datum value
		 */
		public ThreadEntry(boolean targetThread, int method, int threadNumber, Integer datum) {
			this(targetThread, FrameMethod.valueFrom(method), threadNumber, datum);
		}

		/**
		 * Creates a thread entry with typed method.
		 *
		 * @param targetThread whether this is a target thread (vs frame thread)
		 * @param method the frame method
		 * @param threadNumber the thread number (0-3)
		 * @param datum the optional datum value
		 */
		public ThreadEntry(boolean targetThread, FrameMethod method, int threadNumber, Integer datum) {
			if ((threadNumber & ~0x03) != 0) {
				throw new IllegalArgumentException("THREAD number must be in range 0..3: " + threadNumber);
			}
			if (targetThread && method.getValue() > 3) {
				throw new IllegalArgumentException(
						"TARGET THREAD method uses only low 2 bits (0..3): " + method.getValue());
			}
			this.targetThread = targetThread;
			this.method = method;
			this.threadNumber = threadNumber;
			this.datum = datum;
		}

		/**
		 * Returns whether this is a target thread.
		 *
		 * @return true if this entry defines a target thread, false for a frame thread
		 */
		public boolean isTargetThread() {
			return targetThread;
		}

		/**
		 * Returns the frame method value.
		 *
		 * @return the method value
		 */
		public int getMethod() {
			return method.getValue();
		}

		/**
		 * Returns the frame method enum.
		 *
		 * @return the frame method
		 */
		public FrameMethod getMethodEnum() {
			return method;
		}

		/**
		 * Returns the target method low 2 bits.
		 *
		 * @return the low 2 bits of the method value
		 * @throws IllegalStateException if this is a frame thread
		 */
		public int getTargetMethodLowBits() {
			if (!targetThread) {
				throw new IllegalStateException("Thread is a FRAME thread, not a TARGET thread");
			}
			return method.getValue() & 0x03;
		}

		/**
		 * Returns the thread number (0-3).
		 *
		 * @return the thread number
		 */
		public int getThreadNumber() {
			return threadNumber;
		}

		/**
		 * Returns the optional datum value.
		 *
		 * @return the datum, or null
		 */
		public Integer getDatum() {
			return datum;
		}

		@Override
		public void write(ByteOutputStream bos, byte specificTypeValue) throws IOException {
			int threadByte = ((targetThread ? 1 : 0) << 6) | ((method.getValue() & 0x07) << 2) | (threadNumber & 0x03);
			bos.writeByte(threadByte);
			if (usesDatum(method.getValue())) {
				if (datum == null) {
					throw new IOException("THREAD subrecord requires datum for method " + method.getValue());
				}
				OmfUtils.writeIndex(bos, datum);
			}
		}
	}

	/**
	 * Fixup subrecord specifying a relocation entry.
	 */
	public static class FixupEntry implements Subrecord {
		private final int dataRecordOffset;
		private final int locationType;
		private final boolean selfRelative;
		private final boolean frameFromThread;
		private final FrameMethod frameMethod;
		private final Integer frameDatum;
		private final boolean targetFromThread;
		private final TargetMethod targetMethod;
		private final Integer targetDatum;
		private final Integer targetDisplacement;

		/**
		 * Creates a fixup entry with basic parameters.
		 *
		 * @param dataRecordOffset offset into the data record
		 * @param locationType type of location to fix
		 * @param selfRelative whether the fixup is self-relative
		 * @param frameMethod frame determination method
		 * @param targetMethod target determination method
		 * @param frameDatum frame datum value
		 * @param targetDatum target datum value
		 */
		public FixupEntry(
				int dataRecordOffset,
				int locationType,
				boolean selfRelative,
				int frameMethod,
				int targetMethod,
				int frameDatum,
				int targetDatum) {
			this(
					dataRecordOffset,
					locationType,
					selfRelative,
					false,
					FrameMethod.valueFrom(frameMethod),
					frameDatum,
					false,
					TargetMethod.valueFrom(targetMethod),
					targetDatum,
					null);
		}

		/**
		 * Creates a fixup entry with typed methods.
		 *
		 * @param dataRecordOffset offset into the data record
		 * @param locationType type of location to fix
		 * @param selfRelative whether the fixup is self-relative
		 * @param frameMethod frame determination method
		 * @param targetMethod target determination method
		 * @param frameDatum frame datum value
		 * @param targetDatum target datum value
		 */
		public FixupEntry(
				int dataRecordOffset,
				int locationType,
				boolean selfRelative,
				FrameMethod frameMethod,
				TargetMethod targetMethod,
				int frameDatum,
				int targetDatum) {
			this(
					dataRecordOffset,
					locationType,
					selfRelative,
					false,
					frameMethod,
					frameDatum,
					false,
					targetMethod,
					targetDatum,
					null);
		}

		/**
		 * Creates a fixup entry with all parameters.
		 *
		 * @param dataRecordOffset offset into the data record
		 * @param locationType type of location to fix
		 * @param selfRelative whether the fixup is self-relative
		 * @param frameFromThread whether frame comes from a thread
		 * @param frameMethod frame determination method
		 * @param frameDatum frame datum value
		 * @param targetFromThread whether target comes from a thread
		 * @param targetMethod target determination method
		 * @param targetDatum target datum value
		 * @param targetDisplacement optional target displacement
		 */
		public FixupEntry(
				int dataRecordOffset,
				int locationType,
				boolean selfRelative,
				boolean frameFromThread,
				int frameMethod,
				Integer frameDatum,
				boolean targetFromThread,
				int targetMethod,
				Integer targetDatum,
				Integer targetDisplacement) {
			this(
					dataRecordOffset,
					locationType,
					selfRelative,
					frameFromThread,
					FrameMethod.valueFrom(frameMethod),
					targetFromThread,
					TargetMethod.valueFrom(targetMethod),
					targetDatum,
					targetDisplacement,
					frameDatum);
		}

		/**
		 * Creates a fixup entry with typed methods and all parameters.
		 *
		 * @param dataRecordOffset offset into the data record
		 * @param locationType type of location to fix
		 * @param selfRelative whether the fixup is self-relative
		 * @param frameFromThread whether frame comes from a thread
		 * @param frameMethod frame determination method
		 * @param frameDatum frame datum value
		 * @param targetFromThread whether target comes from a thread
		 * @param targetMethod target determination method
		 * @param targetDatum target datum value
		 * @param targetDisplacement optional target displacement
		 */
		public FixupEntry(
				int dataRecordOffset,
				int locationType,
				boolean selfRelative,
				boolean frameFromThread,
				FrameMethod frameMethod,
				Integer frameDatum,
				boolean targetFromThread,
				TargetMethod targetMethod,
				Integer targetDatum,
				Integer targetDisplacement) {
			this(
					dataRecordOffset,
					locationType,
					selfRelative,
					frameFromThread,
					frameMethod,
					targetFromThread,
					targetMethod,
					targetDatum,
					targetDisplacement,
					frameDatum);
		}

		private FixupEntry(
				int dataRecordOffset,
				int locationType,
				boolean selfRelative,
				boolean frameFromThread,
				FrameMethod frameMethod,
				boolean targetFromThread,
				TargetMethod targetMethod,
				Integer targetDatum,
				Integer targetDisplacement,
				Integer frameDatum) {
			if ((dataRecordOffset & ~0x03FF) != 0) {
				throw new IllegalArgumentException(
						"FIXUPP dataRecordOffset must fit in 10 bits (0..1023): " + dataRecordOffset);
			}
			if ((locationType & ~0x0F) != 0) {
				throw new IllegalArgumentException("FIXUPP locationType must fit in 4 bits: " + locationType);
			}
			if (frameFromThread && frameMethod.getValue() > 3) {
				throw new IllegalArgumentException(
						"FRAME thread selector uses only low 2 bits (0..3): " + frameMethod.getValue());
			}
			this.dataRecordOffset = dataRecordOffset;
			this.locationType = locationType;
			this.selfRelative = selfRelative;
			this.frameFromThread = frameFromThread;
			this.frameMethod = frameMethod;
			this.frameDatum = frameDatum;
			this.targetFromThread = targetFromThread;
			this.targetMethod = targetMethod;
			this.targetDatum = targetDatum;
			this.targetDisplacement = targetDisplacement;
		}

		/**
		 * Returns the offset into the data record.
		 *
		 * @return the data record offset
		 */
		public int getDataRecordOffset() {
			return dataRecordOffset;
		}

		/**
		 * Returns the location type.
		 *
		 * @return the location type
		 */
		public int getLocationType() {
			return locationType;
		}

		/**
		 * Returns whether the fixup is self-relative.
		 *
		 * @return true if relocation is relative to the current location
		 */
		public boolean isSelfRelative() {
			return selfRelative;
		}

		/**
		 * Returns whether the frame comes from a thread.
		 *
		 * @return true if frame information is taken from a thread entry
		 */
		public boolean isFrameFromThread() {
			return frameFromThread;
		}

		/**
		 * Returns the frame method value.
		 *
		 * @return the frame method value
		 */
		public int getFrameMethod() {
			return frameMethod.getValue();
		}

		/**
		 * Returns the frame method enum.
		 *
		 * @return the frame method
		 */
		public FrameMethod getFrameMethodEnum() {
			return frameMethod;
		}

		/**
		 * Returns the frame datum value.
		 *
		 * @return the frame datum, or null
		 */
		public Integer getFrameDatum() {
			return frameDatum;
		}

		/**
		 * Returns whether the target comes from a thread.
		 *
		 * @return true if target information is taken from a thread entry
		 */
		public boolean isTargetFromThread() {
			return targetFromThread;
		}

		/**
		 * Returns the target method value.
		 *
		 * @return the target method value
		 */
		public int getTargetMethod() {
			return targetMethod.getValue();
		}

		/**
		 * Returns the target method enum.
		 *
		 * @return the target method
		 */
		public TargetMethod getTargetMethodEnum() {
			return targetMethod;
		}

		/**
		 * Returns the effective target method code.
		 *
		 * @return the effective method code
		 * @throws IllegalStateException if target is from a thread
		 */
		public int getEffectiveTargetMethodCode() {
			if (targetFromThread) {
				throw new IllegalStateException(
						"Effective TARGET method requires thread context when targetFromThread is true");
			}
			int pBit = targetDisplacement == null ? 1 : 0;
			return (pBit << 2) | (targetMethod.getValue() & 0x03);
		}

		/**
		 * Returns the effective target method code with thread context.
		 *
		 * @param targetThreadEntry the target thread entry
		 * @return the effective method code
		 */
		public int getEffectiveTargetMethodCode(ThreadEntry targetThreadEntry) {
			if (!targetFromThread) {
				return getEffectiveTargetMethodCode();
			}
			if (targetThreadEntry == null) {
				throw new IllegalArgumentException("targetThreadEntry must not be null");
			}
			if (!targetThreadEntry.isTargetThread()) {
				throw new IllegalArgumentException("Provided thread entry is not a TARGET thread");
			}
			if (targetThreadEntry.getThreadNumber() != (targetMethod.getValue() & 0x03)) {
				throw new IllegalArgumentException(
						"Provided TARGET thread number does not match FIXUP thread selector");
			}
			int pBit = targetDisplacement == null ? 1 : 0;
			return (pBit << 2) | targetThreadEntry.getTargetMethodLowBits();
		}

		/**
		 * Returns the target datum value.
		 *
		 * @return the target datum, or null
		 */
		public Integer getTargetDatum() {
			return targetDatum;
		}

		/**
		 * Returns the target displacement value.
		 *
		 * @return the target displacement, or null
		 */
		public Integer getTargetDisplacement() {
			return targetDisplacement;
		}

		@Override
		public void write(ByteOutputStream bos, byte specificTypeValue) throws IOException {
			int firstByte = 0x80;
			if (selfRelative) {
				firstByte |= 0x40;
			}
			firstByte |= (locationType & 0x0F) << 2;
			firstByte |= (dataRecordOffset >> 8) & 0x03;

			int secondByte = dataRecordOffset & 0xFF;
			int fixData = ((frameFromThread ? 1 : 0) << 7)
					| ((frameMethod.getValue() & 0x07) << 4)
					| ((targetFromThread ? 1 : 0) << 3)
					| ((targetDisplacement == null ? 1 : 0) << 2)
					| (targetMethod.getValue() & 0x03);

			bos.writeByte(firstByte);
			bos.writeByte(secondByte);
			bos.writeByte(fixData);

			if (!frameFromThread && usesDatum(frameMethod.getValue())) {
				if (frameDatum == null) {
					throw new IOException("FIXUP frame datum is required for method " + frameMethod.getValue());
				}
				OmfUtils.writeIndex(bos, frameDatum);
			}

			if (!targetFromThread && usesDatum(targetMethod.getValue())) {
				if (targetDatum == null) {
					throw new IOException("FIXUP target datum is required for method " + targetMethod.getValue());
				}
				OmfUtils.writeIndex(bos, targetDatum);
			}

			if (targetDisplacement != null) {
				if (specificTypeValue == (byte) 0x9D) {
					bos.writeInt(targetDisplacement);
				} else {
					bos.writeShort(targetDisplacement);
				}
			}
		}
	}

	private final List<Subrecord> subrecords;
	private final List<ThreadEntry> threadEntries;
	private final List<FixupEntry> fixupEntries;
	private final List<Integer> fixupOffsets;
	private final byte specificTypeValue;

	private static boolean usesDatum(int method) {
		return method >= 0 && method <= 2;
	}

	/**
	 * Parses a FIXUPP record from the input stream.
	 *
	 * @param file the parent OMF file
	 * @param bis the input stream
	 * @throws IOException if an I/O error occurs
	 */
	public OmfRecordFixupp(OmfFile file, ByteInputStream bis) throws IOException {
		super(file, OmfRecordType.FIXUPP);

		byte parsedType = getParsingTypeValue();
		byte assumedType = (parsedType != 0) ? parsedType : (byte) 0x9C;
		byte[] encodedData = bis.readAllBytes();
		ByteInputStream entryBis = ByteInputStream.asLittleEndian(encodedData);

		List<Subrecord> parsedSubrecords = new ArrayList<>();
		List<ThreadEntry> parsedThreads = new ArrayList<>();
		List<FixupEntry> parsedFixups = new ArrayList<>();
		List<Integer> offsets = new ArrayList<>();

		while (entryBis.available() > 0) {
			int firstByte = entryBis.readUnsignedByte();

			if ((firstByte & 0x80) == 0) {
				boolean targetThread = (firstByte & 0x40) != 0;
				int method = (firstByte >> 2) & 0x07;
				int threadNumber = firstByte & 0x03;
				Integer datum = usesDatum(method) ? OmfUtils.readIndex(entryBis) : null;
				ThreadEntry entry = new ThreadEntry(targetThread, FrameMethod.valueFrom(method), threadNumber, datum);
				parsedThreads.add(entry);
				parsedSubrecords.add(entry);
				continue;
			}

			int secondByte = entryBis.readUnsignedByte();
			int fixData = entryBis.readUnsignedByte();

			int dataRecordOffset = ((firstByte & 0x03) << 8) | secondByte;
			int locationType = (firstByte >> 2) & 0x0F;
			boolean selfRelative = (firstByte & 0x40) != 0;

			boolean frameFromThread = (fixData & 0x80) != 0;
			FrameMethod frameMethod = FrameMethod.valueFrom((fixData >> 4) & 0x07);
			boolean targetFromThread = (fixData & 0x08) != 0;
			boolean targetDisplacementPresent = (fixData & 0x04) == 0;
			TargetMethod targetMethod = TargetMethod.valueFrom(fixData & 0x03);

			Integer frameDatum =
					(!frameFromThread && usesDatum(frameMethod.getValue())) ? OmfUtils.readIndex(entryBis) : null;
			Integer targetDatum =
					(!targetFromThread && usesDatum(targetMethod.getValue())) ? OmfUtils.readIndex(entryBis) : null;
			Integer targetDisplacement = null;
			if (targetDisplacementPresent) {
				targetDisplacement = assumedType == (byte) 0x9D ? entryBis.readInt() : entryBis.readUnsignedShort();
			}

			FixupEntry entry = new FixupEntry(
					dataRecordOffset,
					locationType,
					selfRelative,
					frameFromThread,
					frameMethod,
					frameDatum,
					targetFromThread,
					targetMethod,
					targetDatum,
					targetDisplacement);

			parsedFixups.add(entry);
			parsedSubrecords.add(entry);
			offsets.add(dataRecordOffset);
		}

		this.subrecords = Collections.unmodifiableList(parsedSubrecords);
		this.threadEntries = Collections.unmodifiableList(parsedThreads);
		this.fixupEntries = Collections.unmodifiableList(parsedFixups);
		this.fixupOffsets = Collections.unmodifiableList(offsets);

		if (parsedType != 0) {
			this.specificTypeValue = parsedType;
		} else {
			boolean use32Bit = false;
			for (Integer off : offsets) {
				if (off > 0xFFFF) {
					use32Bit = true;
					break;
				}
			}
			this.specificTypeValue = use32Bit ? (byte) 0x9D : (byte) 0x9C;
		}
	}

	/**
	 * Creates a new FIXUPP record.
	 *
	 * @param file the parent OMF file
	 * @param fixupEntries the list of fixup entries
	 * @param specificTypeValue the specific type value (may be null to auto-detect)
	 */
	public OmfRecordFixupp(OmfFile file, List<FixupEntry> fixupEntries, Byte specificTypeValue) {
		super(file, OmfRecordType.FIXUPP);
		Objects.requireNonNull(fixupEntries);
		// specificTypeValue is nullable

		this.threadEntries = Collections.emptyList();
		this.fixupEntries = Collections.unmodifiableList(new ArrayList<>(fixupEntries));
		this.subrecords = Collections.unmodifiableList(new ArrayList<>(this.fixupEntries));
		List<Integer> offsets =
				this.fixupEntries.stream().map(FixupEntry::getDataRecordOffset).toList();
		this.fixupOffsets = Collections.unmodifiableList(offsets);

		boolean use32Bit = false;
		for (Integer off : offsets) {
			if (off > 0xFFFF) {
				use32Bit = true;
				break;
			}
		}
		byte inferredTypeValue = use32Bit ? (byte) 0x9D : (byte) 0x9C;
		this.specificTypeValue = specificTypeValue != null ? specificTypeValue : inferredTypeValue;
	}

	/**
	 * Creates a new FIXUPP record with typed specific type.
	 *
	 * @param file the parent OMF file
	 * @param fixupEntries the list of fixup entries
	 * @param specificType the specific type (may be null to auto-detect)
	 */
	public OmfRecordFixupp(OmfFile file, List<FixupEntry> fixupEntries, SpecificType specificType) {
		this(file, fixupEntries, specificType != null ? specificType.getValue() : null);
	}

	/**
	 * Creates a new FIXUPP record with auto-detected type.
	 *
	 * @param file the parent OMF file
	 * @param fixupEntries the list of fixup entries
	 */
	public OmfRecordFixupp(OmfFile file, List<FixupEntry> fixupEntries) {
		this(file, fixupEntries, (Byte) null);
	}

	@Override
	public byte getSpecificTypeValue() {
		return specificTypeValue;
	}

	/**
	 * Returns the list of fixup offsets.
	 *
	 * @return the fixup offsets
	 */
	public List<Integer> getFixupOffsets() {
		return fixupOffsets;
	}

	/**
	 * Returns the list of fixup entries.
	 *
	 * @return the fixup entries
	 */
	public List<FixupEntry> getFixupEntries() {
		return fixupEntries;
	}

	/**
	 * Returns the list of thread entries.
	 *
	 * @return the thread entries
	 */
	public List<ThreadEntry> getThreadEntries() {
		return threadEntries;
	}

	@Override
	public void write(ByteOutputStream bos) throws IOException {
		for (Subrecord subrecord : subrecords) {
			subrecord.write(bos, specificTypeValue);
		}
	}
}
