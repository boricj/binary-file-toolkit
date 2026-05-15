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
package net.boricj.bft.omf.constants;

import net.boricj.bft.omf.OmfRecord;

/**
 * Enumeration of OMF record types.
 *
 * <p>Each enum value corresponds to a specific OMF record format and includes
 * the record type byte value and the implementing class.
 */
public enum OmfRecordType {
	/** Translator Header Record - module name. */
	THEADR(Integer.valueOf(0x80).byteValue(), net.boricj.bft.omf.records.OmfRecordTheadr.class),
	/** Comment Record - tool directives and metadata. */
	COMENT(Integer.valueOf(0x88).byteValue(), net.boricj.bft.omf.records.OmfRecordComent.class),
	/** Module End Record - marks end of module. */
	MODEND(Integer.valueOf(0x8B).byteValue(), net.boricj.bft.omf.records.OmfRecordModend.class),
	/** External Names Definition Record - external symbol references. */
	EXTDEF(Integer.valueOf(0x8C).byteValue(), net.boricj.bft.omf.records.OmfRecordExtdef.class),
	/** Public Names Definition Record - exported symbols. */
	PUBDEF(Integer.valueOf(0x91).byteValue(), net.boricj.bft.omf.records.OmfRecordPubdef.class),
	/** Line Numbers Record - source line to address mapping. */
	LINNUM(Integer.valueOf(0x95).byteValue(), net.boricj.bft.omf.records.OmfRecordLinnum.class),
	/** List of Names Record - string table. */
	LNAMES(Integer.valueOf(0x96).byteValue(), net.boricj.bft.omf.records.OmfRecordLnames.class),
	/** Segment Definition Record - segment attributes and size. */
	SEGDEF(Integer.valueOf(0x99).byteValue(), net.boricj.bft.omf.records.OmfRecordSegdef.class),
	/** Group Definition Record - segment grouping. */
	GRPDEF(Integer.valueOf(0x9A).byteValue(), net.boricj.bft.omf.records.OmfRecordGrpdef.class),
	/** Fixup Record - relocation information. */
	FIXUPP(Integer.valueOf(0x9D).byteValue(), net.boricj.bft.omf.records.OmfRecordFixupp.class),
	/** Logical Enumerated Data Record - segment data. */
	LEDATA(Integer.valueOf(0xA1).byteValue(), net.boricj.bft.omf.records.OmfRecordLedata.class),
	/** Logical Iterated Data Record - repeated data patterns. */
	LIDATA(Integer.valueOf(0xA3).byteValue(), net.boricj.bft.omf.records.OmfRecordLidata.class);

	private final byte value;
	private final Class<? extends OmfRecord> recordClass;

	OmfRecordType(byte value, Class<? extends OmfRecord> recordClass) {
		this.value = value;
		this.recordClass = recordClass;
	}

	/**
	 * Returns the record type byte value.
	 *
	 * @return the byte value
	 */
	public byte getValue() {
		return value;
	}

	/**
	 * Returns the Java class that implements this record type.
	 *
	 * @return the record class
	 */
	public Class<? extends OmfRecord> getRecordClass() {
		return recordClass;
	}

	/**
	 * Looks up an OMF record type by its byte value.
	 *
	 * @param value the record type byte
	 * @return the corresponding record type
	 * @throws IllegalArgumentException if the value is not recognized
	 */
	public static OmfRecordType valueFrom(byte value) {
		switch (value & 0xFF) {
			case 0x8A:
			case 0x8B:
				return MODEND;
			case 0x90:
			case 0x91:
				return PUBDEF;
			case 0x94:
			case 0x95:
				return LINNUM;
			case 0x98:
			case 0x99:
				return SEGDEF;
			case 0x9C:
			case 0x9D:
				return FIXUPP;
			case 0xA0:
			case 0xA1:
				return LEDATA;
			case 0xA2:
			case 0xA3:
				return LIDATA;
			default:
				break;
		}

		for (OmfRecordType recordType : values()) {
			if (recordType.getValue() == value) {
				return recordType;
			}
		}

		throw new IllegalArgumentException();
	}
}
