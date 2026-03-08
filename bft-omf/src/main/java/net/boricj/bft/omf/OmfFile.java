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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.IndirectList;
import net.boricj.bft.Writable;
import net.boricj.bft.omf.records.OmfRecordFixupp;
import net.boricj.bft.omf.records.OmfRecordGrpdef;
import net.boricj.bft.omf.records.OmfRecordLedata;
import net.boricj.bft.omf.records.OmfRecordLidata;
import net.boricj.bft.omf.records.OmfRecordLnames;
import net.boricj.bft.omf.records.OmfRecordSegdef;

/**
 * Represents an OMF (Object Module Format) file.
 *
 * <p>This class provides functionality to parse existing OMF files and build new ones.
 * OMF files contain a sequence of typed records that define segments, symbols, relocations, and other object module data.
 */
public class OmfFile implements IndirectList<OmfRecord>, Writable {
	private final List<OmfRecord> records;
	private final Charset charset;

	/**
	 * Builder for constructing OMF files programmatically.
	 */
	public static class Builder {
		private Charset charset = StandardCharsets.UTF_8;

		/** Creates a new OMF file builder. */
		public Builder() {}

		/**
		 * Returns the charset used for string encoding.
		 *
		 * @return the charset
		 */
		public Charset getCharset() {
			return charset;
		}

		/**
		 * Sets the charset used for string encoding.
		 *
		 * @param charset the charset to use
		 * @return this builder
		 */
		public Builder setCharset(Charset charset) {
			Objects.requireNonNull(charset);

			this.charset = charset;

			return this;
		}

		/**
		 * Builds the OMF file.
		 *
		 * @return the constructed OMF file
		 */
		public OmfFile build() {
			return new OmfFile(this);
		}
	}

	/**
	 * Parser for reading OMF files from input streams.
	 */
	public static class Parser {
		private final FileInputStream fis;
		private Charset charset = StandardCharsets.UTF_8;

		/**
		 * Creates a new OMF file parser.
		 *
		 * @param fis the file input stream to read from
		 */
		public Parser(FileInputStream fis) {
			Objects.requireNonNull(fis);

			this.fis = fis;
		}

		/**
		 * Returns the file input stream being parsed.
		 *
		 * @return the file input stream
		 */
		public FileInputStream getFileInputStream() {
			return fis;
		}

		/**
		 * Returns the charset used for string decoding.
		 *
		 * @return the charset
		 */
		public Charset getCharset() {
			return charset;
		}

		/**
		 * Sets the charset used for string decoding.
		 *
		 * @param charset the charset to use
		 * @return this parser
		 */
		public Parser setCharset(Charset charset) {
			Objects.requireNonNull(charset);

			this.charset = charset;

			return this;
		}

		/**
		 * Parses the OMF file from the input stream.
		 *
		 * @return the parsed OMF file
		 * @throws IOException if an I/O error occurs
		 */
		public OmfFile parse() throws IOException {
			return new OmfFile(this);
		}
	}

	/**
	 * Creates an empty OMF file using a builder configuration.
	 *
	 * @param builder the builder providing configuration values
	 */
	protected OmfFile(Builder builder) {
		this.records = new ArrayList<>();
		this.charset = builder.charset;
	}

	/**
	 * Parses an OMF file from the parser input stream.
	 *
	 * @param parser the parser containing input source and configuration
	 * @throws IOException if parsing fails due to invalid data or I/O errors
	 */
	protected OmfFile(Parser parser) throws IOException {
		this.records = new ArrayList<>();
		this.charset = parser.charset;

		ByteInputStream bis = ByteInputStream.asLittleEndian(parser.fis);

		// Parse records until end of file
		while (bis.available() > 0) {
			OmfRecord record = OmfRecord.parse(this, bis);
			records.add(record);

			// Stop if we encounter MODEND (end of module record)
			if (record.getType().getValue() == 0x8A || record.getType().getValue() == 0x8B) {
				break;
			}
		}
	}

	/**
	 * Returns the charset used for string encoding and decoding.
	 *
	 * @return the charset
	 */
	public Charset getCharset() {
		return charset;
	}

	/**
	 * Gets a name from the LNAMES records by its 1-based index.
	 *
	 * @param index the 1-based index
	 * @return the name at the specified index
	 * @throws IndexOutOfBoundsException if the index is invalid
	 */
	public String getLnameByIndex(int index) {
		if (index <= 0) {
			throw new IndexOutOfBoundsException(index);
		}

		int current = 1;
		for (OmfRecord record : records) {
			if (record instanceof OmfRecordLnames lnames) {
				for (String name : lnames.getNames()) {
					if (current == index) {
						return name;
					}
					current++;
				}
			}
		}

		throw new IndexOutOfBoundsException(index);
	}

	/**
	 * Gets the 1-based index of a name in the LNAMES records.
	 *
	 * @param name the name to find
	 * @return the 1-based index
	 * @throws IllegalStateException if the name is not defined
	 */
	public int indexOfLname(String name) {
		int current = 1;
		for (OmfRecord record : records) {
			if (record instanceof OmfRecordLnames lnames) {
				for (String candidate : lnames.getNames()) {
					if (candidate.equals(name)) {
						return current;
					}
					current++;
				}
			}
		}

		throw new IllegalStateException("Name is not defined in LNAMES: " + name);
	}

	/**
	 * Gets the 1-based index of a name that appears before a specified record.
	 *
	 * @param name the name to find
	 * @param record the context record
	 * @return the 1-based index
	 * @throws IllegalStateException if the name is not defined before the record
	 */
	public int indexOfLnameBefore(String name, OmfRecord record) {
		int current = 1;
		int match = -1;

		for (OmfRecord rec : records) {
			if (rec instanceof OmfRecordLnames lnames) {
				for (String candidate : lnames.getNames()) {
					if (candidate.equals(name)) {
						match = current;
					}
					current++;
				}
			}

			if (rec == record) {
				break;
			}
		}

		if (match < 0) {
			throw new IllegalStateException("Name is not defined in LNAMES before context record: " + name);
		}

		return match;
	}

	/**
	 * Gets a SEGDEF record by its 1-based index.
	 *
	 * @param index the 1-based index
	 * @return the SEGDEF record at the specified index
	 * @throws IndexOutOfBoundsException if the index is invalid
	 */
	public OmfRecordSegdef getSegmentByIndex(int index) {
		if (index <= 0) {
			throw new IndexOutOfBoundsException(index);
		}

		int current = 1;
		for (OmfRecord record : records) {
			if (record instanceof OmfRecordSegdef segdef) {
				if (current == index) {
					return segdef;
				}
				current++;
			}
		}

		throw new IndexOutOfBoundsException(index);
	}

	/**
	 * Gets the 1-based index of a SEGDEF record.
	 *
	 * @param segment the SEGDEF record to find
	 * @return the 1-based index
	 * @throws IllegalStateException if the segment is not defined
	 */
	public int indexOfSegment(OmfRecordSegdef segment) {
		int current = 1;
		for (OmfRecord record : records) {
			if (record instanceof OmfRecordSegdef segdef) {
				if (segdef == segment) {
					return current;
				}
				current++;
			}
		}

		throw new IllegalStateException("Segment is not defined in this OMF file");
	}

	/**
	 * Gets a GRPDEF record by its 1-based index.
	 *
	 * @param index the 1-based index
	 * @return the GRPDEF record at the specified index
	 * @throws IndexOutOfBoundsException if the index is invalid
	 */
	public OmfRecordGrpdef getGroupByIndex(int index) {
		if (index <= 0) {
			throw new IndexOutOfBoundsException(index);
		}

		int current = 1;
		for (OmfRecord record : records) {
			if (record instanceof OmfRecordGrpdef grpdef) {
				if (current == index) {
					return grpdef;
				}
				current++;
			}
		}

		throw new IndexOutOfBoundsException(index);
	}

	/**
	 * Gets the 1-based index of a GRPDEF record.
	 *
	 * @param group the GRPDEF record to find
	 * @return the 1-based index
	 * @throws IllegalStateException if the group is not defined
	 */
	public int indexOfGroup(OmfRecordGrpdef group) {
		int current = 1;
		for (OmfRecord record : records) {
			if (record instanceof OmfRecordGrpdef grpdef) {
				if (grpdef == group) {
					return current;
				}
				current++;
			}
		}

		throw new IllegalStateException("Group is not defined in this OMF file");
	}

	@Override
	public List<OmfRecord> getElements() {
		return records;
	}

	@Override
	public boolean add(OmfRecord record) {
		// Per OMF specification: FIXUPP records must immediately follow
		// LEDATA, LIDATA, or COMDAT records.
		if (record instanceof OmfRecordFixupp && !records.isEmpty()) {
			OmfRecord lastRecord = records.get(records.size() - 1);
			if (!(lastRecord instanceof OmfRecordLedata || lastRecord instanceof OmfRecordLidata)) {
				throw new IllegalStateException(
						"FIXUPP record must immediately follow LEDATA or LIDATA record, but previous record is: "
								+ lastRecord.getClass().getSimpleName());
			}
		}
		return records.add(record);
	}

	@Override
	public long getOffset() {
		return 0;
	}

	@Override
	public long getLength() {
		// Calculate total length of all records
		long length = 0;
		for (OmfRecord record : records) {
			// Each record has: type (1 byte) + length (2 bytes) + data + checksum (1 byte)
			// The length field includes data + checksum
			length += 3; // type + length field (2 bytes)
			// We need to calculate the actual data length for each record
			// For simplicity, we'll return 0 for now since this is a basic implementation
			// TODO: Implement proper length calculation
		}
		return 0;
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		ByteOutputStream bos = wrap(outputStream);
		for (OmfRecord record : records) {
			// Write record type
			bos.writeByte(record.getSpecificTypeValue());

			// Capture record data
			java.io.ByteArrayOutputStream recordBaos = new java.io.ByteArrayOutputStream();
			record.write(ByteOutputStream.asLittleEndian(recordBaos));
			byte[] recordData = recordBaos.toByteArray();

			// Write record length (data + checksum)
			bos.writeShort(recordData.length + 1);

			// Write record data
			bos.write(recordData);

			// Calculate and write checksum
			int checksum = calculateChecksum(record.getSpecificTypeValue(), recordData.length + 1, recordData);
			bos.writeByte(checksum);
		}
	}

	private int calculateChecksum(int recordType, int recordLength, byte[] data) {
		int sum = recordType;
		// Add record length bytes
		sum += recordLength & 0xFF;
		sum += (recordLength >> 8) & 0xFF;
		// Add data bytes
		for (byte b : data) {
			sum += b & 0xFF;
		}
		return (256 - (sum & 0xFF)) & 0xFF;
	}

	/**
	 * Wraps an output stream as a little-endian byte output stream.
	 *
	 * @param outputStream the output stream to wrap
	 * @return a little-endian byte output stream view
	 */
	public ByteOutputStream wrap(OutputStream outputStream) {
		return ByteOutputStream.asLittleEndian(outputStream);
	}

	/**
	 * Wraps an input stream as a little-endian byte input stream.
	 *
	 * @param inputStream the input stream to wrap
	 * @return a little-endian byte input stream view
	 */
	public ByteInputStream wrap(InputStream inputStream) {
		return ByteInputStream.asLittleEndian(inputStream);
	}
}
