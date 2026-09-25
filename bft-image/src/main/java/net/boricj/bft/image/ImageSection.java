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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import net.boricj.bft.IndirectList;

/**
 * A logical section in a semantic image.
 */
public final class ImageSection {

	private final String name;
	private byte[] contents = new byte[0];
	private long logicalSize = 0;
	private final RelocationTable relocations = new RelocationTable();

	ImageSection(String name) {
		this.name = Objects.requireNonNull(name, "name");
	}

	/**
	 * Returns section name.
	 *
	 * @return section name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns a defensive copy of section bytes.
	 *
	 * @return section contents
	 */
	public byte[] getContents() {
		return this.contents.clone();
	}

	/**
	 * Replaces section contents and resets logical size to match byte length.
	 *
	 * @param contents new section bytes
	 */
	public void setContents(byte[] contents) {
		this.contents = contents.clone();
		this.logicalSize = this.contents.length;
	}

	/**
	 * Returns logical section size used for nobits-style sections.
	 *
	 * @return logical section size
	 */
	public long getLogicalSize() {
		return this.logicalSize;
	}

	/**
	 * Sets logical section size.
	 *
	 * @param logicalSize logical section size in bytes
	 * @throws IllegalArgumentException if smaller than current content length
	 */
	public void setLogicalSize(long logicalSize) {
		if (logicalSize < this.contents.length) {
			throw new IllegalArgumentException("logicalSize cannot be smaller than content length");
		}
		this.logicalSize = logicalSize;
	}

	/**
	 * Returns mutable relocation table for this section.
	 *
	 * @return section relocation table
	 */
	public RelocationTable relocations() {
		return this.relocations;
	}

	boolean semanticallyEquals(ImageSection other) {
		return this.name.equals(other.name)
				&& Arrays.equals(this.contents, other.contents)
				&& this.logicalSize == other.logicalSize
				&& this.relocations.semanticallyEquals(other.relocations);
	}

	/** Mutable relocation table for a section. */
	public final class RelocationTable implements IndirectList<ImageRelocationGroup> {
		private final List<ImageRelocationGroup> elements = new ArrayList<>();

		/**
		 * Creates an empty mutable relocation-group table.
		 */
		public RelocationTable() {}

		/**
		 * Creates and appends a logical relocation group.
		 *
		 * @param operation coarse relocation operation family
		 * @param target relocation target symbol
		 * @param addend group-level addend
		 * @return newly created relocation group
		 */
		public ImageRelocationGroup create(RelocationOperation operation, ImageSymbol target, long addend) {
			ImageRelocationGroup relocation = new ImageRelocationGroup(operation, target, addend);
			this.elements.add(relocation);
			return relocation;
		}

		/**
		 * Creates a relocation group containing exactly one gang and one entry.
		 *
		 * @param offset relocation site offset
		 * @param operation coarse relocation operation family
		 * @param fieldCodec relocation field codec
		 * @param target relocation target symbol
		 * @param addend group-level addend
		 * @return newly created relocation group
		 */
		public ImageRelocationGroup createSingleEntry(
				long offset,
				RelocationOperation operation,
				RelocationFieldCodec fieldCodec,
				ImageSymbol target,
				long addend) {
			ImageRelocationGroup relocation = create(operation, target, addend);
			relocation.gangs().create(fieldCodec).entries().create(offset);
			return relocation;
		}

		/**
		 * Returns backing relocation-group list.
		 *
		 * @return mutable relocation-group list
		 */
		@Override
		public List<ImageRelocationGroup> getElements() {
			return this.elements;
		}

		private boolean semanticallyEquals(RelocationTable other) {
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
