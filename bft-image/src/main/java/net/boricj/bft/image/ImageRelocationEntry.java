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

/**
 * A single concrete relocation site belonging to a relocation gang.
 */
public final class ImageRelocationEntry {
	private final ImageRelocationGang gang;
	private final long offset;
	private final long localAddend;

	ImageRelocationEntry(ImageRelocationGang gang, long offset, long localAddend) {
		if (offset < 0) {
			throw new IllegalArgumentException("offset cannot be negative");
		}
		this.gang = gang;
		this.offset = offset;
		this.localAddend = localAddend;
	}

	/**
	 * Returns owning relocation gang.
	 *
	 * @return owning gang
	 */
	public ImageRelocationGang getGang() {
		return this.gang;
	}

	/**
	 * Returns relocation site offset inside the owning section.
	 *
	 * @return section-relative relocation offset
	 */
	public long getOffset() {
		return this.offset;
	}

	/**
	 * Returns local addend stored directly on this entry.
	 *
	 * @return entry-local addend contribution
	 */
	public long getLocalAddend() {
		return this.localAddend;
	}

	/**
	 * Returns total addend, combining group addend and entry-local addend.
	 *
	 * @return effective relocation addend
	 */
	public long getAddend() {
		return this.gang.getRelocation().getAddend() + this.localAddend;
	}

	/**
	 * Returns relocation target symbol.
	 *
	 * @return target symbol, or null for target-less relocations
	 */
	public ImageSymbol getTarget() {
		return this.gang.getRelocation().getTarget();
	}

	/**
	 * Returns coarse relocation operation family.
	 *
	 * @return relocation operation
	 */
	public RelocationOperation getOperation() {
		return this.gang.getRelocation().getOperation();
	}

	/**
	 * Returns field codec used to patch this relocation site.
	 *
	 * @return relocation field codec
	 */
	public RelocationFieldCodec getFieldCodec() {
		return this.gang.getFieldCodec();
	}

	/**
	 * Reads the inline relocation field from the provided section bytes.
	 *
	 * @param sectionBytes section bytes containing the field
	 * @return inline field value
	 */
	public long readInlineAddend(byte[] sectionBytes) {
		return new RelocationFieldCursor(this.gang.getFieldCodec(), this.offset).read(sectionBytes);
	}

	/**
	 * Checks whether the inline field is zero in the provided section bytes.
	 *
	 * @param sectionBytes section bytes containing the field
	 * @return true when the relocation field is zero
	 */
	public boolean isInlineFieldZero(byte[] sectionBytes) {
		return new RelocationFieldCursor(this.gang.getFieldCodec(), this.offset).isZero(sectionBytes);
	}

	/**
	 * Produces a patch that zeros the inline field in a working section buffer.
	 *
	 * @param sectionBytes section bytes containing the field
	 * @return patch to zero the relocation field
	 */
	public RelocationFieldPatch maskInlineAddend(byte[] sectionBytes) {
		return new RelocationFieldCursor(this.gang.getFieldCodec(), this.offset).mask(sectionBytes);
	}

	/**
	 * Produces a patch that writes the given inline relocation value into a working section buffer.
	 *
	 * @param sectionBytes section bytes containing the field
	 * @param value inline field value to emit
	 * @return patch to write the relocation field
	 */
	public RelocationFieldPatch writeInlineAddend(byte[] sectionBytes, long value) {
		return new RelocationFieldCursor(this.gang.getFieldCodec(), this.offset).write(sectionBytes, value);
	}

	boolean semanticallyEquals(ImageRelocationEntry other) {
		return this.offset == other.offset && this.localAddend == other.localAddend;
	}
}
