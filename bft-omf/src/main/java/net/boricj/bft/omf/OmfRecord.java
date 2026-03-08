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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.omf.constants.OmfRecordType;

/**
 * Abstract base class for all OMF record types.
 *
 * <p>Each OMF record represents a specific data structure in the object file format,
 * such as segment definitions, symbol definitions, or relocation information.
 */
public abstract class OmfRecord {
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
	 * Writes this record's data to the output stream.
	 *
	 * @param bos the output stream
	 * @throws IOException if an I/O error occurs
	 */
	public abstract void write(ByteOutputStream bos) throws IOException;

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
