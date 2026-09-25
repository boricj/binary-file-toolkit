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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.boricj.bft.IndirectList;

/**
 * A homogeneous set of relocation entries sharing the same field codec.
 */
public final class ImageRelocationGang {
	private final ImageRelocationGroup relocation;
	private final RelocationFieldCodec fieldCodec;
	private final EntryTable entries = new EntryTable();

	ImageRelocationGang(ImageRelocationGroup relocation, RelocationFieldCodec fieldCodec) {
		this.relocation = Objects.requireNonNull(relocation, "relocation");
		this.fieldCodec = Objects.requireNonNull(fieldCodec, "fieldCodec");
	}

	/**
	 * Returns owning relocation group.
	 *
	 * @return owning relocation group
	 */
	public ImageRelocationGroup getRelocation() {
		return this.relocation;
	}

	/**
	 * Returns field codec shared by all entries in this gang.
	 *
	 * @return relocation field codec
	 */
	public RelocationFieldCodec getFieldCodec() {
		return this.fieldCodec;
	}

	/**
	 * Returns entry table for this gang.
	 *
	 * @return relocation entry table
	 */
	public EntryTable entries() {
		return this.entries;
	}

	boolean semanticallyEquals(ImageRelocationGang other) {
		return this.fieldCodec.equals(other.fieldCodec) && this.entries.semanticallyEquals(other.entries);
	}

	/** Mutable table of relocation entries that share this gang's codec. */
	public final class EntryTable implements IndirectList<ImageRelocationEntry> {
		private final List<ImageRelocationEntry> elements = new ArrayList<>();

		/**
		 * Creates an empty mutable relocation-entry table.
		 */
		public EntryTable() {}

		/**
		 * Creates a relocation entry with a zero local addend.
		 *
		 * @param offset section-relative relocation offset
		 * @return newly created relocation entry
		 */
		public ImageRelocationEntry create(long offset) {
			return create(offset, 0);
		}

		/**
		 * Creates a relocation entry with an explicit local addend.
		 *
		 * @param offset section-relative relocation offset
		 * @param localAddend entry-local addend contribution
		 * @return newly created relocation entry
		 */
		public ImageRelocationEntry create(long offset, long localAddend) {
			ImageRelocationEntry entry = new ImageRelocationEntry(ImageRelocationGang.this, offset, localAddend);
			this.elements.add(entry);
			return entry;
		}

		/**
		 * Returns backing relocation-entry list.
		 *
		 * @return mutable relocation-entry list
		 */
		@Override
		public List<ImageRelocationEntry> getElements() {
			return this.elements;
		}

		private boolean semanticallyEquals(EntryTable other) {
			if (this.size() != other.size()) {
				return false;
			}

			for (int i = 0; i < this.size(); i++) {
				if (!this.get(i).semanticallyEquals(other.get(i))) {
					return false;
				}
			}

			return true;
		}
	}
}
