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
package net.boricj.bft.omf.coments;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.omf.OmfComent;
import net.boricj.bft.omf.constants.OmfComentClass;

/**
 * TRANSLATOR comment (class 0x00) - Contains compiler/assembler name and version.
 * Structure: length byte + ASCII string bytes
 */
public class OmfComentTranslator extends OmfComent {
	private final byte subtype;
	private final String translatorString;

	/**
	 * Parses a TRANSLATOR comment from the input stream.
	 *
	 * @param bis the input stream
	 * @throws IOException if an I/O error occurs
	 */
	public OmfComentTranslator(ByteInputStream bis) throws IOException {
		super(OmfComentClass.TRANSLATOR);

		int length = bis.readUnsignedByte();
		this.subtype = (byte) length;

		byte[] stringBytes = new byte[length];
		bis.readFully(stringBytes);
		this.translatorString = new String(stringBytes, StandardCharsets.US_ASCII);
	}

	/**
	 * Creates a new TRANSLATOR comment.
	 *
	 * @param subtype the subtype byte
	 * @param translatorString the translator identification string
	 */
	public OmfComentTranslator(byte subtype, String translatorString) {
		super(OmfComentClass.TRANSLATOR);
		this.translatorString = translatorString == null ? "" : translatorString;
		byte[] stringBytes = this.translatorString.getBytes(StandardCharsets.US_ASCII);
		if (stringBytes.length > 0xFF) {
			throw new IllegalArgumentException("TRANSLATOR string too long: " + stringBytes.length);
		}
		this.subtype = (byte) stringBytes.length;
	}

	/**
	 * Returns the subtype byte.
	 *
	 * @return the subtype byte
	 */
	public byte getSubtype() {
		return subtype;
	}

	/**
	 * Returns the translator identification string.
	 *
	 * @return the translator string
	 */
	public String getTranslatorString() {
		return translatorString;
	}

	@Override
	public void write(ByteOutputStream bos) throws IOException {
		bos.writeByte(subtype);
		byte[] stringBytes = translatorString.getBytes(StandardCharsets.US_ASCII);
		bos.write(stringBytes);
	}
}
