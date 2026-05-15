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
package net.boricj.bft.omf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.MeteredOutputStream;
import net.boricj.bft.StreamWritable;
import net.boricj.bft.omf.constants.OmfRecordType;

/**
 * Abstract base class for all OMF record types.
 *
 * <p>Each OMF record represents a specific data structure in the object file format,
 * such as segment definitions, symbol definitions, or relocation information.
 */
public abstract class OmfRecord implements StreamWritable {
	private final OmfFile file;
	private final OmfRecordType type;
	private static final ThreadLocal<Byte> parsingTypeValue = new ThreadLocal<>();

	/**
	 * Creates a new OMF record.
	 *
	 * @param file the parent OMF file
	 * @param type the record type
	 */
	protected OmfRecord(OmfFile file, OmfRecordType type) {
		Objects.requireNonNull(file);
		Objects.requireNonNull(type);

		this.file = file;
		this.type = type;
	}

	/**
	 * Returns the specific record type value being parsed in the current thread.
	 * This allows subclasses to determine whether they are parsing a 16-bit or 32-bit variant.
	 *
	 * @return the record type byte value, or 0 if not currently parsing
	 */
	protected static byte getParsingTypeValue() {
		Byte value = parsingTypeValue.get();
		return value != null ? value : 0;
	}

	/**
	 * Returns the parent OMF file.
	 *
	 * @return the parent OMF file
	 */
	public OmfFile getFile() {
		return file;
	}

	/**
	 * Returns the record type.
	 *
	 * @return the record type
	 */
	public OmfRecordType getType() {
		return type;
	}

	/**
	 * Get the specific record type value.  For records with multiple variants
	 * (e.g., 16-bit vs 32-bit), this returns the specific variant value.
	 * By default, returns the standard type value.
	 *
	 * @return the specific record type byte value
	 */
	public byte getSpecificTypeValue() {
		return type.getValue();
	}

	/**
	 * Returns the length in bytes of this record's full serialized form
	 * (record type + record length + record data + checksum).
	 *
	 * @return full serialized record length in bytes
	 */
	@Override
	public long getLength() {
		return 1 + 2 + getDataLength() + 1;
	}

	/**
	 * Returns the length in bytes of this record's payload data (excluding
	 * type, length, and checksum).
	 *
	 * @return payload length in bytes
	 */
	protected long getDataLength() {
		try (MeteredOutputStream outputStream = new MeteredOutputStream(OutputStream.nullOutputStream())) {
			write(ByteOutputStream.asLittleEndian(outputStream));
			return outputStream.getCount();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to measure OMF record payload length", e);
		}
	}

	/**
	 * Validates that this record's payload length fits in the OMF record length field.
	 *
	 * @param recordName record name used in diagnostics
	 */
	protected final void validateDataLength(String recordName) {
		long dataLength = getDataLength();
		if (dataLength < 0 || dataLength > OmfUtils.MAX_RECORD_DATA_LENGTH) {
			throw new IllegalArgumentException(recordName + " data length out of range: " + dataLength + " (maximum "
					+ OmfUtils.MAX_RECORD_DATA_LENGTH + ")");
		}
	}

	/**
	 * Writes this record's full encoded form to the output stream.
	 * This includes the type byte, record length, payload, and checksum.
	 *
	 * @param outputStream the output stream
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	public void write(OutputStream outputStream) throws IOException {
		ByteOutputStream bos = ByteOutputStream.asLittleEndian(outputStream);

		ByteArrayOutputStream recordBaos = new ByteArrayOutputStream();
		write(ByteOutputStream.asLittleEndian(recordBaos));
		byte[] recordData = recordBaos.toByteArray();
		int dataLength = recordData.length;
		if (dataLength > OmfUtils.MAX_RECORD_DATA_LENGTH) {
			String message = String.format(
					"OMF record 0x%02X is too large: data length %d (maximum %d)",
					getSpecificTypeValue() & 0xFF, dataLength, OmfUtils.MAX_RECORD_DATA_LENGTH);
			throw new IOException(message);
		}

		int recordLength = recordData.length + 1;
		bos.writeByte(getSpecificTypeValue());
		bos.writeShort(recordLength);
		bos.write(recordData);
		bos.writeByte(calculateChecksum(getSpecificTypeValue(), recordLength, recordData));
	}

	/**
	 * Writes this record's data to the output stream.
	 *
	 * @param bos the output stream
	 * @throws IOException if an I/O error occurs
	 */
	protected abstract void write(ByteOutputStream bos) throws IOException;

	private int calculateChecksum(int recordType, int recordLength, byte[] data) {
		int sum = recordType;
		sum += recordLength & 0xFF;
		sum += (recordLength >> 8) & 0xFF;
		for (byte b : data) {
			sum += b & 0xFF;
		}
		return (256 - (sum & 0xFF)) & 0xFF;
	}

	/**
	 * Parse an OMF record from the input stream.
	 *
	 * @param file the parent OmfFile
	 * @param bis the input stream to read from
	 * @return the parsed OmfRecord
	 * @throws IOException if an I/O error occurs
	 */
	public static OmfRecord parse(OmfFile file, ByteInputStream bis) throws IOException {
		// Read record type (1 byte)
		byte recordTypeTag = bis.readByte();

		// Read record length (2 bytes, little-endian)
		// The length includes data + checksum (1 byte)
		int recordLength = bis.readUnsignedShort();

		// Look up the record type
		OmfRecordType recordType = OmfRecordType.valueFrom(recordTypeTag);
		// Create a sliced stream containing only the record data (excluding checksum)
		ByteInputStream recordBis = bis.slice(recordLength - 1);

		// Read and discard the checksum byte
		bis.readByte();

		// Get the record class
		Class<? extends OmfRecord> recordClass = recordType.getRecordClass();

		try {
			// Store the original type value in ThreadLocal for subclasses to use
			parsingTypeValue.set(recordTypeTag);
			try {
				// Create an instance using the constructor that takes (OmfFile, ByteInputStream)
				Constructor<? extends OmfRecord> constructor =
						recordClass.getConstructor(OmfFile.class, ByteInputStream.class);
				return constructor.newInstance(file, recordBis);
			} finally {
				// Clear the ThreadLocal to avoid memory leaks
				parsingTypeValue.remove();
			}
		} catch (NoSuchMethodException
				| InstantiationException
				| IllegalAccessException
				| InvocationTargetException e) {
			throw new IOException("Failed to instantiate record type: " + recordType, e);
		}
	}
}
