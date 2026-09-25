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
 * A logical relocation in a semantic image.
 */
public final class ImageRelocationGroup {

	private final RelocationOperation operation;
	private final ImageSymbol target;
	private final long addend;
	private final GangTable gangs = new GangTable();

	ImageRelocationGroup(RelocationOperation operation, ImageSymbol target, long addend) {
		this.operation = Objects.requireNonNull(operation, "operation");
		this.target = target;
		this.addend = addend;
	}

	/**
	 * Returns coarse relocation operation family.
	 *
	 * @return relocation operation
	 */
	public RelocationOperation getOperation() {
		return this.operation;
	}

	/**
	 * Returns mutable table of gangs belonging to this relocation.
	 *
	 * @return gang table
	 */
	public GangTable gangs() {
		return this.gangs;
	}

	/**
	 * Returns relocation target symbol.
	 *
	 * @return target symbol, or null for target-less relocations
	 */
	public ImageSymbol getTarget() {
		return this.target;
	}

	/**
	 * Returns group-level relocation addend.
	 *
	 * @return relocation addend
	 */
	public long getAddend() {
		return this.addend;
	}

	boolean semanticallyEquals(ImageRelocationGroup other) {
		return this.addend == other.addend
				&& this.operation == other.operation
				&& this.gangs.semanticallyEquals(other.gangs)
				&& ((this.target == null && other.target == null)
						|| (this.target != null
								&& other.target != null
								&& this.target.semanticallyEquals(other.target)));
	}

	/** Mutable table of relocation gangs for this logical relocation. */
	public final class GangTable implements IndirectList<ImageRelocationGang> {
		private final List<ImageRelocationGang> elements = new ArrayList<>();

		/**
		 * Creates an empty mutable relocation-gang table.
		 */
		public GangTable() {}

		/**
		 * Creates and appends a relocation gang for the provided field codec.
		 *
		 * @param fieldCodec relocation field codec
		 * @return newly created relocation gang
		 */
		public ImageRelocationGang create(RelocationFieldCodec fieldCodec) {
			ImageRelocationGang gang = new ImageRelocationGang(ImageRelocationGroup.this, fieldCodec);
			this.elements.add(gang);
			return gang;
		}

		/**
		 * Returns backing relocation-gang list.
		 *
		 * @return mutable relocation-gang list
		 */
		@Override
		public List<ImageRelocationGang> getElements() {
			return this.elements;
		}

		private boolean semanticallyEquals(GangTable other) {
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
