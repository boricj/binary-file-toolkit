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
import net.boricj.bft.ByteOutputStream;

/**
 * Represents a single CTF type record.
 * Each type has a kind (leaf type identifier) and a size/alignment information.
 */
public class CTFType {
	private int typeId;

	/**
	 * Kinds of CTF types; values taken from the specification.
	 */
	public enum Kind {
		UNKNOWN(0),
		INTEGER(1),
		FLOAT(2),
		POINTER(3),
		ARRAY(4),
		FUNCTION(5),
		STRUCT(6),
		UNION(7),
		ENUM(8),
		FORWARD(9),
		TYPEDEF(10),
		VOLATILE(11),
		CONST(12),
		RESTRICT(13),
		SLICE(14);

		private final int value;
		Kind(int v) { this.value = v; }
		public int value() { return value; }
		public static Kind fromValue(int v) {
			for (Kind k : values()) {
				if (k.value == v) return k;
			}
			return UNKNOWN;
		}
	}

	private Kind kind;
	private boolean root;
	private int vlen;
	private int size;
	private int type; // used when kind encodes a referenced type
	private int align;

	// kind-dependent extras
	private Integer intEncoding;
	private Integer returnType;
	private java.util.List<Integer> parameters;
	private byte[] rawPayload;

	public static CTFType parse(ByteInputStream stream, int typeId, CTFStringSection strings)
			throws IOException, CTFException {
		// there are at least 12 bytes (name, info, size/type, align)
		if (stream.available() < 12) {
			return null;
		}

		CTFType t = new CTFType();
		t.typeId = typeId;

		int nameOff = stream.readInt();
		int info = stream.readInt();
		System.out.printf("[TYPE DEBUG] Type ID: %d, nameOff: 0x%x, info: 0x%x%n", typeId, nameOff, info);
		t.kind = Kind.fromValue((info >>> 26) & 0x3F);
		System.out.printf("[TYPE DEBUG] Parsed Kind Value: 0x%x, Enum: %s%n", ((info >>> 26) & 0x3F), t.kind.name());
		t.root = ((info >>> 25) & 1) != 0;
		t.vlen = info & 0x1FFFFFF;

		if (usesSize(t.kind)) {
			t.size = stream.readInt();
		} else {
			t.type = stream.readInt();
		}
		t.align = stream.readInt();

		switch (t.kind) {
			case INTEGER:
				t.intEncoding = stream.readInt();
				break;
			case FUNCTION:
				t.returnType = t.type;
				t.parameters = new java.util.ArrayList<>();
				for (int i = 0; i < t.vlen; i++) {
					t.parameters.add(stream.readInt());
				}
				if ((t.vlen & 1) != 0) {
					stream.readInt(); // padding
				}
				break;
			default:
				int bytes = t.vlen * 4;
				if (bytes > 0) {
					t.rawPayload = new byte[bytes];
					stream.read(t.rawPayload);
				}
				break;
		}

		return t;
	}

    public int getTypeId() { return typeId; }
    public Kind getKind() { return kind; }
    public int getSize() { return size; }
    public int getAlign() { return align; }
    public int getReferencedType() { return type; }
    public boolean isRoot() { return root; }
    public int getVlen() { return vlen; }
    public Integer getIntEncoding() { return intEncoding; }
    public Integer getReturnType() { return returnType; }
    public java.util.List<Integer> getParameters() { return parameters; }
    public byte[] getRawPayload() { return rawPayload; }

    // setters omitted for brevity; fields are generally immutable after parse

	public byte[] write() throws IOException {
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		ByteOutputStream out = ByteOutputStream.asLittleEndian(baos);

		// name offset is not tracked; write zero
		out.writeInt(0);

		int info = (kind.value() & 0x3F) << 26;
		if (root) info |= 1 << 25;
		info |= (vlen & 0x1FFFFFF);
		out.writeInt(info);

		if (usesSize(kind)) {
			out.writeInt(size);
		} else {
			out.writeInt(type);
		}
		out.writeInt(align);

		switch (kind) {
			case INTEGER:
				out.writeInt(intEncoding != null ? intEncoding : 0);
				break;
			case FUNCTION:
				if (parameters != null) {
					for (int p : parameters) {
						out.writeInt(p);
					}
					if ((parameters.size() & 1) != 0) {
						out.writeInt(0);
					}
				}
				break;
			default:
				if (rawPayload != null) {
					out.write(rawPayload);
				}
				break;
		}

		return baos.toByteArray();
	}

	private static boolean usesSize(Kind k) {
		switch (k) {
			case INTEGER:
			case FLOAT:
			case STRUCT:
			case UNION:
			case ENUM:
			case SLICE:
				return true;
			default:
				return false;
		}
	}
}
