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
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.boricj.bft.IndirectList;

/**
 * A logical section in a semantic image.
 */
public final class ImageSection {

	private final String name;
	private byte[] contents = new byte[0];
	private long logicalSize = 0;
	private long alignment = 1;
	private final EnumSet<ImageSectionRuntimeFlags> runtimeFlags = EnumSet.noneOf(ImageSectionRuntimeFlags.class);
	private final EnumSet<ImageSectionLinktimeFlags> linktimeFlags = EnumSet.noneOf(ImageSectionLinktimeFlags.class);
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
	 * Returns section alignment in bytes.
	 *
	 * @return alignment in bytes
	 */
	public long getAlignment() {
		return this.alignment;
	}

	/**
	 * Sets section alignment in bytes.
	 *
	 * @param alignment alignment in bytes
	 * @throws IllegalArgumentException if {@code alignment} is negative
	 */
	public void setAlignment(long alignment) {
		if (alignment < 0) {
			throw new IllegalArgumentException("alignment cannot be negative");
		}
		this.alignment = alignment;
	}

	/**
	 * Returns an immutable view of runtime section flags.
	 *
	 * @return runtime section flags
	 */
	public Set<ImageSectionRuntimeFlags> getRuntimeFlags() {
		return Collections.unmodifiableSet(this.runtimeFlags);
	}

	/**
	 * Adds one runtime section flag to this section.
	 *
	 * @param flag runtime flag to add
	 */
	public void addRuntimeFlag(ImageSectionRuntimeFlags flag) {
		this.runtimeFlags.add(Objects.requireNonNull(flag, "flag"));
	}

	/**
	 * Removes one runtime section flag from this section.
	 *
	 * @param flag runtime flag to remove
	 */
	public void removeRuntimeFlag(ImageSectionRuntimeFlags flag) {
		this.runtimeFlags.remove(Objects.requireNonNull(flag, "flag"));
	}

	/**
	 * Checks whether this section has a runtime section flag.
	 *
	 * @param flag runtime flag to check
	 * @return true when present
	 */
	public boolean hasRuntimeFlag(ImageSectionRuntimeFlags flag) {
		return this.runtimeFlags.contains(Objects.requireNonNull(flag, "flag"));
	}

	/**
	 * Replaces all runtime section flags on this section.
	 *
	 * @param flags replacement runtime flag set
	 */
	public void setRuntimeFlags(Set<ImageSectionRuntimeFlags> flags) {
		Objects.requireNonNull(flags, "flags");
		this.runtimeFlags.clear();
		this.runtimeFlags.addAll(flags);
	}

	/**
	 * Returns an immutable view of link-time section flags.
	 *
	 * @return link-time section flags
	 */
	public Set<ImageSectionLinktimeFlags> getLinktimeFlags() {
		return Collections.unmodifiableSet(this.linktimeFlags);
	}

	/**
	 * Adds one link-time section flag to this section.
	 *
	 * @param flag link-time flag to add
	 */
	public void addLinktimeFlag(ImageSectionLinktimeFlags flag) {
		this.linktimeFlags.add(Objects.requireNonNull(flag, "flag"));
	}

	/**
	 * Removes one link-time section flag from this section.
	 *
	 * @param flag link-time flag to remove
	 */
	public void removeLinktimeFlag(ImageSectionLinktimeFlags flag) {
		this.linktimeFlags.remove(Objects.requireNonNull(flag, "flag"));
	}

	/**
	 * Checks whether this section has a link-time section flag.
	 *
	 * @param flag link-time flag to check
	 * @return true when present
	 */
	public boolean hasLinktimeFlag(ImageSectionLinktimeFlags flag) {
		return this.linktimeFlags.contains(Objects.requireNonNull(flag, "flag"));
	}

	/**
	 * Replaces all link-time section flags on this section.
	 *
	 * @param flags replacement link-time flag set
	 */
	public void setLinktimeFlags(Set<ImageSectionLinktimeFlags> flags) {
		Objects.requireNonNull(flags, "flags");
		this.linktimeFlags.clear();
		this.linktimeFlags.addAll(flags);
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
				&& this.alignment == other.alignment
				&& this.runtimeFlags.equals(other.runtimeFlags)
				&& this.linktimeFlags.equals(other.linktimeFlags)
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
