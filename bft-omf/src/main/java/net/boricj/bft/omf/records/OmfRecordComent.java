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
import java.util.Objects;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.omf.OmfComent;
import net.boricj.bft.omf.OmfFile;
import net.boricj.bft.omf.OmfRecord;
import net.boricj.bft.omf.constants.OmfComentClass;
import net.boricj.bft.omf.constants.OmfRecordType;

/**
 * COMENT - Comment Record
 * Contains comments and directives for tools.
 */
public class OmfRecordComent extends OmfRecord {
	private final boolean purge;
	private final boolean list;
	private final OmfComent comment;

	/**
	 * Parses a COMENT record from the input stream.
	 *
	 * @param file the parent OMF file
	 * @param bis the input stream
	 * @throws IOException if an I/O error occurs
	 */
	public OmfRecordComent(OmfFile file, ByteInputStream bis) throws IOException {
		super(file, OmfRecordType.COMENT);

		byte attributeByte = bis.readByte();
		this.purge = (attributeByte & 0x80) != 0;
		this.list = (attributeByte & 0x40) != 0;

		OmfComentClass commentClass = OmfComentClass.valueFrom(bis.readByte());
		this.comment = OmfComent.parse(commentClass, bis);
	}

	/**
	 * Creates a new COMENT record.
	 *
	 * @param file the parent OMF file
	 * @param purge whether the comment should be purged
	 * @param list whether the comment should be listed
	 * @param comment the comment data
	 */
	public OmfRecordComent(OmfFile file, boolean purge, boolean list, OmfComent comment) {
		super(file, OmfRecordType.COMENT);
		Objects.requireNonNull(comment);

		this.purge = purge;
		this.list = list;
		this.comment = comment;
	}

	/**
	 * Returns whether the comment should be purged.
	 *
	 * @return true if the comment is purgeable
	 */
	public boolean isPurge() {
		return purge;
	}

	/**
	 * Returns whether the comment should be listed.
	 *
	 * @return true if the comment should appear in listings
	 */
	public boolean isList() {
		return list;
	}

	/**
	 * Returns the comment data.
	 *
	 * @return the comment data
	 */
	public OmfComent getComment() {
		return comment;
	}

	/**
	 * Returns the comment class.
	 *
	 * @return the comment class
	 */
	public OmfComentClass getCommentClass() {
		return comment.getCommentClass();
	}

	@Override
	public void write(ByteOutputStream bos) throws IOException {
		byte attributeByte = 0;
		if (purge) {
			attributeByte |= 0x80;
		}
		if (list) {
			attributeByte |= 0x40;
		}
		bos.writeByte(attributeByte);
		bos.writeByte(comment.getCommentClass().getValue());
		comment.write(bos);
	}
}
