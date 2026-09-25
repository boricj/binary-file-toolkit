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
 * Represents a single CTF type record.
 * Each type has a kind (leaf type identifier) and a size/alignment information.
 */
public class CTFType {
	private int typeId;

	/**
	 * Creates an empty type record model.
	 */
	public CTFType() {}

	/**
	 * Kinds of CTF types; values taken from the specification.
	 */
	public enum Kind {
		/** Invalid or unknown kind value. */
		UNKNOWN(0),
		/** Integer type. */
		INTEGER(1),
		/** Floating-point type. */
		FLOAT(2),
		/** Pointer type. */
		POINTER(3),
		/** Array type. */
		ARRAY(4),
		/** Function prototype type. */
		FUNCTION(5),
		/** Structure type. */
		STRUCT(6),
		/** Union type. */
		UNION(7),
		/** Enumeration type. */
		ENUM(8),
		/** Forward declaration type. */
		FORWARD(9),
		/** Typedef alias type. */
		TYPEDEF(10),
		/** Volatile-qualified type. */
		VOLATILE(11),
		/** Const-qualified type. */
		CONST(12),
		/** Restrict-qualified type. */
		RESTRICT(13),
		/** Slice type. */
		SLICE(14);

		private final int value;

		Kind(int v) {
			this.value = v;
		}

		/**
		 * Returns the raw numeric kind value.
		 *
		 * @return raw kind value
		 */
		public int value() {
			return value;
		}

		/**
		 * Resolves a raw kind value.
		 *
		 * @param v raw kind value
		 * @return matching kind, or {@link Kind#UNKNOWN} for unrecognized values
		 */
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
	private int nameOffset;
	private int size;
	private int type; // used when kind encodes a referenced type
	private int align;

	// kind-dependent extras
	private Integer intEncoding;
	private Integer returnType;
	private java.util.List<Integer> parameters;
	private byte[] rawPayload;

	/**
	 * Parses one type record from the type-section stream.
	 *
	 * @param stream type-section stream positioned at the next record
	 * @param typeId sequential CTF type identifier to assign
	 * @param strings dictionary string section
	 * @return parsed type record, or {@code null} if not enough bytes remain for a full header
	 * @throws IOException if data cannot be read
	 * @throws CTFException if the type record is structurally invalid
	 */
	public static CTFType parse(ByteInputStream stream, int typeId, CTFStringSection strings)
			throws IOException, CTFException {
		// there are at least 12 bytes (name, info, size/type, align)
		if (stream.available() < 12) {
			return null;
		}

		CTFType t = new CTFType();
		t.typeId = typeId;

		int nameOff = stream.readInt();
		t.nameOffset = nameOff;
		int info = stream.readInt();
		int kindValue = (info >>> 26) & 0x3F;
		t.kind = Kind.fromValue(kindValue);
		if (kindValue != 0 && t.kind == Kind.UNKNOWN) {
			throw new CTFException("Invalid CTF type kind: " + kindValue);
		}
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
					int toRead = Math.min(bytes, stream.available());
					if (toRead > 0) {
						t.rawPayload = new byte[toRead];
						int read = stream.read(t.rawPayload);
						if (read < toRead) {
							t.rawPayload = java.util.Arrays.copyOf(t.rawPayload, Math.max(read, 0));
						}
					} else {
						t.rawPayload = new byte[0];
					}
				}
				break;
		}

		return t;
	}

	/**
	 * Returns CTF type identifier.
	 *
	 * @return type identifier
	 */
	public int getTypeId() {
		return typeId;
	}

	/**
	 * Returns type kind.
	 *
	 * @return type kind
	 */
	public Kind getKind() {
		return kind;
	}

	/**
	 * Returns string-table offset of the type name.
	 *
	 * @return type-name string offset
	 */
	public int getNameOffset() {
		return nameOffset;
	}

	/**
	 * Returns size field for size-bearing kinds.
	 *
	 * @return kind-dependent size value
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Returns alignment value.
	 *
	 * @return alignment value
	 */
	public int getAlign() {
		return align;
	}

	/**
	 * Returns referenced type identifier for reference-bearing kinds.
	 *
	 * @return referenced type identifier
	 */
	public int getReferencedType() {
		return type;
	}

	/**
	 * Returns whether the root bit is set.
	 *
	 * @return {@code true} when the type is marked as root
	 */
	public boolean isRoot() {
		return root;
	}

	/**
	 * Returns vlen field value.
	 *
	 * @return variable-length payload count field
	 */
	public int getVlen() {
		return vlen;
	}

	/**
	 * Returns integer encoding payload for integer kinds.
	 *
	 * @return integer encoding word, or {@code null} when not applicable
	 */
	public Integer getIntEncoding() {
		return intEncoding;
	}

	/**
	 * Returns function return type identifier for function kinds.
	 *
	 * @return return type identifier, or {@code null} when not applicable
	 */
	public Integer getReturnType() {
		return returnType;
	}

	/**
	 * Returns parameter type identifiers for function kinds.
	 *
	 * @return parameter list, or {@code null} when not applicable
	 */
	public java.util.List<Integer> getParameters() {
		return parameters;
	}

	/**
	 * Returns preserved raw payload bytes for kinds not yet structurally modeled.
	 *
	 * @return raw payload bytes, or {@code null} when none were preserved
	 */
	public byte[] getRawPayload() {
		return rawPayload;
	}

	// setters omitted for brevity; fields are generally immutable after parse

	/**
	 * Serializes this type record to bytes.
	 *
	 * @return encoded type record bytes
	 * @throws IOException if serialization fails
	 */
	public byte[] write() throws IOException {
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		ByteOutputStream out = ByteOutputStream.asLittleEndian(baos);

		out.writeInt(nameOffset);

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
