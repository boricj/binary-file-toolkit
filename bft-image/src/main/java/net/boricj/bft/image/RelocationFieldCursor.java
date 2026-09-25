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
package net.boricj.bft.image;

import java.util.Arrays;
import java.util.Objects;

/**
 * Generic cursor for reading and rewriting a relocation field in a section buffer.
 */
public final class RelocationFieldCursor {
	private final RelocationFieldCodec codec;
	private final long offset;

	/**
	 * Creates a relocation field cursor.
	 *
	 * @param codec relocation field codec
	 * @param offset section-relative byte offset
	 */
	public RelocationFieldCursor(RelocationFieldCodec codec, long offset) {
		this.codec = Objects.requireNonNull(codec, "codec");
		if (offset < 0) {
			throw new IllegalArgumentException("offset cannot be negative");
		}
		this.offset = offset;
	}

	/**
	 * Reads a relocation field at the given offset.
	 *
	 * @param sectionBytes section byte buffer
	 * @param offset section-relative byte offset
	 * @param codec relocation field codec
	 * @return decoded relocation field value
	 */
	public static long read(byte[] sectionBytes, long offset, RelocationFieldCodec codec) {
		return new RelocationFieldCursor(codec, offset).read(sectionBytes);
	}

	/**
	 * Checks whether a relocation field is zero at the given offset.
	 *
	 * @param sectionBytes section byte buffer
	 * @param offset section-relative byte offset
	 * @param codec relocation field codec
	 * @return true when the field decodes to zero
	 */
	public static boolean isZero(byte[] sectionBytes, long offset, RelocationFieldCodec codec) {
		return new RelocationFieldCursor(codec, offset).isZero(sectionBytes);
	}

	/**
	 * Creates a patch that clears a relocation field at the given offset.
	 *
	 * @param sectionBytes section byte buffer
	 * @param offset section-relative byte offset
	 * @param codec relocation field codec
	 * @return patch that writes zeros over the field
	 */
	public static RelocationFieldPatch mask(byte[] sectionBytes, long offset, RelocationFieldCodec codec) {
		return new RelocationFieldCursor(codec, offset).mask(sectionBytes);
	}

	/**
	 * Alias for {@link #mask(byte[], long, RelocationFieldCodec)}.
	 *
	 * @param sectionBytes section byte buffer
	 * @param offset section-relative byte offset
	 * @param codec relocation field codec
	 * @return patch that writes zeros over the field
	 */
	public static RelocationFieldPatch zeroField(byte[] sectionBytes, long offset, RelocationFieldCodec codec) {
		return mask(sectionBytes, offset, codec);
	}

	/**
	 * Creates a patch that writes a relocation field value at the given offset.
	 *
	 * @param sectionBytes section byte buffer
	 * @param offset section-relative byte offset
	 * @param codec relocation field codec
	 * @param value replacement field value
	 * @return patch containing the newly encoded field bytes
	 */
	public static RelocationFieldPatch write(byte[] sectionBytes, long offset, RelocationFieldCodec codec, long value) {
		return new RelocationFieldCursor(codec, offset).write(sectionBytes, value);
	}

	/**
	 * Returns the section-relative byte offset addressed by this cursor.
	 *
	 * @return section-relative byte offset
	 */
	public long getOffset() {
		return this.offset;
	}

	/**
	 * Returns the field codec used by this cursor.
	 *
	 * @return relocation field codec
	 */
	public RelocationFieldCodec getCodec() {
		return this.codec;
	}

	/**
	 * Reads the relocation field value at this cursor offset.
	 *
	 * @param sectionBytes section byte buffer
	 * @return decoded relocation field value
	 */
	public long read(byte[] sectionBytes) {
		return this.codec.read(sectionBytes, Math.toIntExact(this.offset));
	}

	/**
	 * Checks whether the relocation field at this cursor offset is zero.
	 *
	 * @param sectionBytes section byte buffer
	 * @return true when the field decodes to zero
	 */
	public boolean isZero(byte[] sectionBytes) {
		return read(sectionBytes) == 0L;
	}

	/**
	 * Creates a patch that clears the relocation field at this cursor offset.
	 *
	 * @param sectionBytes section byte buffer
	 * @return patch that writes zeros over the field
	 */
	public RelocationFieldPatch mask(byte[] sectionBytes) {
		int start = Math.toIntExact(this.offset);
		byte[] patched = Arrays.copyOf(sectionBytes, sectionBytes.length);
		this.codec.write(patched, start, 0L);
		int width = this.codec.byteWidth();
		return new RelocationFieldPatch(this.offset, Arrays.copyOfRange(patched, start, start + width));
	}

	/**
	 * Creates a patch that writes a replacement field value at this cursor offset.
	 *
	 * @param sectionBytes section byte buffer
	 * @param value replacement field value
	 * @return patch containing the newly encoded field bytes
	 */
	public RelocationFieldPatch write(byte[] sectionBytes, long value) {
		int start = Math.toIntExact(this.offset);
		byte[] patched = Arrays.copyOf(sectionBytes, sectionBytes.length);
		this.codec.write(patched, start, value);
		int width = this.codec.byteWidth();
		return new RelocationFieldPatch(this.offset, Arrays.copyOfRange(patched, start, start + width));
	}
}
