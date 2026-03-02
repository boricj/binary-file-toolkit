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
package net.boricj.bft.ctf;

import java.io.IOException;
import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;

/**
 * Represents a CTF dictionary (a single container of CTF metadata).
 * 
 * Note: We only support single CTF dictionaries within this module, not archives.
 * Archives can be handled by extracting the desired dictionary externally.
 */
public class CTFDictionary {
	private CTFHeader header;
	private CTFStringSection strings;
	private CTFTypeSection types;
	private CTFFunctionSection functions;

	public static class Parser {
		public CTFDictionary parse(byte[] data) throws CTFException {
			if (data.length < 40) {
				throw new CTFException("CTF data too short for header (minimum 40 bytes)");
			}

			try {
				CTFDictionary dict = new CTFDictionary();

				ByteInputStream stream = ByteInputStream.asLittleEndian(data);
				dict.header = CTFHeader.parse(stream);

				// read remainder of dictionary (everything after header)
				byte[] rest = new byte[stream.available()];
				stream.read(rest);
				// decompress if needed
				if ((dict.header.getFlags() & CTFHeader.FLAG_COMPRESS) != 0) {
					rest = decompress(rest);
				}

				// parse sections using header offsets (relative to end-of-header)
				dict.strings = new CTFStringSection();
				dict.strings.parse(rest, dict.header);

				dict.types = new CTFTypeSection();
				dict.types.parse(rest, dict.header, dict.strings);

				dict.functions = new CTFFunctionSection();
				dict.functions.parse(rest, dict.header, dict.strings, dict.types);

				// TODO: parse dict.variables (requires CTFVariableSection class)

				return dict;
			} catch (IOException e) {
				throw new CTFException("Failed to parse CTF dictionary", e);
			}
		}
	}

	public byte[] write() throws CTFException {
		try {
			java.io.ByteArrayOutputStream baosAll = new java.io.ByteArrayOutputStream();
			ByteOutputStream out = ByteOutputStream.asLittleEndian(baosAll);

			// build section byte arrays
			byte[] lblBytes = new byte[0];
			byte[] objBytes = new byte[0];
			byte[] funcBytes;
			byte[] varBytes = new byte[0]; // TODO: support variables
			// rebuild by writing into a ByteArrayOutputStream wrapped with
			// a little-endian ByteOutputStream helper.
			java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
			ByteOutputStream temp = ByteOutputStream.asLittleEndian(baos);
			if (functions != null) functions.write(temp);
			temp.flush();
			funcBytes = baos.toByteArray();

			baos = new java.io.ByteArrayOutputStream();
			temp = ByteOutputStream.asLittleEndian(baos);
			if (types != null) types.write(temp);
			temp.flush();
			byte[] typeBytes = baos.toByteArray();

			baos = new java.io.ByteArrayOutputStream();
			temp = ByteOutputStream.asLittleEndian(baos);
			if (strings != null) strings.write(temp);
			temp.flush();
			byte[] strBytes = baos.toByteArray();

			// compute offsets relative to end-of-header
			int offset = 0;
			header.setLblOffset(offset);
			header.setLblSize(lblBytes.length);
			offset += lblBytes.length;

			header.setObjOffset(offset);
			header.setObjSize(objBytes.length);
			offset += objBytes.length;

			header.setFuncOffset(offset);
			header.setFuncSize(funcBytes.length);
			offset += funcBytes.length;

			header.setVarOffset(offset);
			header.setVarSize(varBytes.length);
			offset += varBytes.length;

			header.setTypeOffset(offset);
			header.setTypeSize(typeBytes.length);
			offset += typeBytes.length;

			header.setStrOffset(offset);
			header.setStrSize(strBytes.length);

			// write header and sections
			header.write(out);
			out.write(lblBytes);
			out.write(objBytes);
			out.write(funcBytes);
			out.write(varBytes);
			out.write(typeBytes);
			out.write(strBytes);

			return baosAll.toByteArray();
		} catch (IOException e) {
			throw new CTFException("Failed to serialize CTF dictionary", e);
		}
	}

	// helper to decompress zlib-compressed data
	private static byte[] decompress(byte[] compressed) throws IOException {
		java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(compressed);
		java.util.zip.InflaterInputStream iis = new java.util.zip.InflaterInputStream(bais);
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int r;
		while ((r = iis.read(buf)) != -1) {
			baos.write(buf, 0, r);
		}
		return baos.toByteArray();
	}

	public CTFHeader getHeader() {
		return header;
	}

	public CTFStringSection getStrings() {
		return strings;
	}

	public CTFTypeSection getTypes() {
		return types;
	}

	public CTFFunctionSection getFunctions() {
		return functions;
	}
}
