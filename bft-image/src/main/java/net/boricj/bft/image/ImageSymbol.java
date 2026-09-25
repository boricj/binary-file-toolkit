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

import java.util.Objects;

/**
 * A named semantic symbol.
 */
public final class ImageSymbol {
	/** Semantic symbol type categories. */
	public enum Type {
		/** Unspecified type. */
		NOTYPE,
		/** Data object. */
		OBJECT,
		/** Function or code symbol. */
		FUNCTION,
		/** Section symbol. */
		SECTION,
		/** File symbol. */
		FILE,
		/** Thread-local storage symbol. */
		TLS,
	}

	/** Symbol visibility classes. */
	public enum Visibility {
		/** Default visibility. */
		DEFAULT,
		/** Internal visibility. */
		INTERNAL,
		/** Hidden visibility. */
		HIDDEN,
		/** Protected visibility. */
		PROTECTED,
	}

	/** Symbol linkage binding classes. */
	public enum Binding {
		/** Local (translation-unit/private) symbol. */
		LOCAL,
		/** Global symbol. */
		GLOBAL,
		/** Weak symbol. */
		WEAK,
	}

	private final String name;
	private final ImageSection section;
	private final long offset;
	private final long size;
	private final Type type;
	private final Visibility visibility;
	private final Binding binding;

	ImageSymbol(String name, ImageSection section, long offset) {
		this(name, section, offset, 0, Type.NOTYPE, Visibility.DEFAULT, Binding.GLOBAL);
	}

	ImageSymbol(
			String name,
			ImageSection section,
			long offset,
			long size,
			Type type,
			Visibility visibility,
			Binding binding) {
		this.name = Objects.requireNonNull(name, "name");
		this.section = section;
		this.offset = offset;
		this.size = size;
		this.type = Objects.requireNonNull(type, "type");
		this.visibility = Objects.requireNonNull(visibility, "visibility");
		this.binding = Objects.requireNonNull(binding, "binding");
	}

	/**
	 * Returns symbol name.
	 *
	 * @return symbol name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns defining section.
	 *
	 * @return defining section, or null when undefined/absolute
	 */
	public ImageSection getSection() {
		return this.section;
	}

	/**
	 * Returns section-relative symbol value.
	 *
	 * @return symbol value
	 */
	public long getOffset() {
		return this.offset;
	}

	/**
	 * Returns symbol size in bytes.
	 *
	 * @return symbol size
	 */
	public long getSize() {
		return this.size;
	}

	/**
	 * Returns semantic symbol type.
	 *
	 * @return symbol type
	 */
	public Type getType() {
		return this.type;
	}

	/**
	 * Returns symbol visibility.
	 *
	 * @return symbol visibility
	 */
	public Visibility getVisibility() {
		return this.visibility;
	}

	/**
	 * Returns symbol binding.
	 *
	 * @return symbol binding
	 */
	public Binding getBinding() {
		return this.binding;
	}

	boolean semanticallyEquals(ImageSymbol other) {
		return this.name.equals(other.name)
				&& Objects.equals(
						this.section == null ? null : this.section.getName(),
						other.section == null ? null : other.section.getName())
				&& this.offset == other.offset
				&& this.size == other.size
				&& this.type == other.type
				&& this.visibility == other.visibility
				&& this.binding == other.binding;
	}
}
