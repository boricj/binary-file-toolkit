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
 * An in-memory semantic representation of an object file, executable, or shared library.
 */
public final class ImageFile {

	/** Kind of binary represented by this semantic image. */
	public enum Kind {
		/** Relocatable object file. */
		OBJECT,
		/** Executable image. */
		EXECUTABLE,
		/** Dynamically loadable shared object or library. */
		DYNAMIC_LIBRARY,
	}

	private final Kind kind;
	private final SectionTable sections = new SectionTable();
	private final SymbolTable symbols = new SymbolTable();

	/**
	 * Creates an empty semantic image of the specified kind.
	 *
	 * @param kind object/executable/shared-library kind
	 */
	public ImageFile(Kind kind) {
		this.kind = Objects.requireNonNull(kind, "kind");
	}

	/**
	 * Returns the image kind.
	 *
	 * @return image kind
	 */
	public Kind getKind() {
		return this.kind;
	}

	/**
	 * Returns the mutable section table.
	 *
	 * @return section table
	 */
	public SectionTable sections() {
		return this.sections;
	}

	/**
	 * Returns the mutable symbol table.
	 *
	 * @return symbol table
	 */
	public SymbolTable symbols() {
		return this.symbols;
	}

	/**
	 * Returns all relocation groups across every section.
	 *
	 * @return flattened relocation groups
	 */
	public List<ImageRelocationGroup> relocations() {
		List<ImageRelocationGroup> relocations = new ArrayList<>();
		for (ImageSection section : this.sections()) {
			relocations.addAll(section.relocations());
		}
		return relocations;
	}

	/**
	 * Returns all concrete relocation entries across all sections and gangs.
	 *
	 * @return flattened relocation entries
	 */
	public List<ImageRelocationEntry> relocationEntries() {
		List<ImageRelocationEntry> entries = new ArrayList<>();
		for (ImageSection section : this.sections()) {
			for (ImageRelocationGroup relocation : section.relocations()) {
				for (ImageRelocationGang gang : relocation.gangs()) {
					entries.addAll(gang.entries());
				}
			}
		}
		return entries;
	}

	/**
	 * Compares two semantic images for content equivalence.
	 *
	 * @param other image to compare against
	 * @return true when both images carry equivalent semantic content
	 */
	public boolean semanticallyEquals(ImageFile other) {
		if (this == other) {
			return true;
		}
		if (other == null || this.kind != other.kind) {
			return false;
		}
		if (!this.sections.semanticallyEquals(other.sections)) {
			return false;
		}
		return this.symbols.semanticallyEquals(other.symbols);
	}

	/** Mutable section table. */
	public final class SectionTable implements IndirectList<ImageSection> {
		private final List<ImageSection> elements = new ArrayList<>();

		/**
		 * Creates an empty mutable section table.
		 */
		public SectionTable() {}

		/**
		 * Creates and appends a section with the provided name.
		 *
		 * @param name section name
		 * @return newly created section
		 */
		public ImageSection create(String name) {
			ImageSection section = new ImageSection(name);
			this.elements.add(section);
			return section;
		}

		/**
		 * Returns backing section list.
		 *
		 * @return mutable section list
		 */
		@Override
		public List<ImageSection> getElements() {
			return this.elements;
		}

		private boolean semanticallyEquals(SectionTable other) {
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

	/** Mutable symbol table. */
	public final class SymbolTable implements IndirectList<ImageSymbol> {
		private final List<ImageSymbol> elements = new ArrayList<>();

		/**
		 * Creates an empty mutable symbol table.
		 */
		public SymbolTable() {}

		/**
		 * Creates and appends a default global symbol.
		 *
		 * @param name symbol name
		 * @param section defining section, or null for undefined symbols
		 * @param offset section-relative symbol value
		 * @return newly created symbol
		 */
		public ImageSymbol create(String name, ImageSection section, long offset) {
			ImageSymbol symbol = new ImageSymbol(name, section, offset);
			this.elements.add(symbol);
			return symbol;
		}

		/**
		 * Creates and appends a fully specified symbol.
		 *
		 * @param name symbol name
		 * @param section defining section, or null when undefined
		 * @param offset section-relative value
		 * @param size symbol byte size
		 * @param type semantic symbol type
		 * @param visibility symbol visibility
		 * @param binding symbol binding
		 * @return newly created symbol
		 */
		public ImageSymbol create(
				String name,
				ImageSection section,
				long offset,
				long size,
				ImageSymbol.Type type,
				ImageSymbol.Visibility visibility,
				ImageSymbol.Binding binding) {
			ImageSymbol symbol = new ImageSymbol(name, section, offset, size, type, visibility, binding);
			this.elements.add(symbol);
			return symbol;
		}

		/**
		 * Creates and appends a file symbol.
		 *
		 * @param name file symbol name
		 * @return newly created file symbol
		 */
		public ImageSymbol createFile(String name) {
			return create(
					name, null, 0, 0, ImageSymbol.Type.FILE, ImageSymbol.Visibility.DEFAULT, ImageSymbol.Binding.LOCAL);
		}

		/**
		 * Creates and appends a section symbol for a concrete section.
		 *
		 * @param section referenced section
		 * @return newly created section symbol
		 */
		public ImageSymbol createSection(ImageSection section) {
			return create(
					"",
					section,
					0,
					0,
					ImageSymbol.Type.SECTION,
					ImageSymbol.Visibility.DEFAULT,
					ImageSymbol.Binding.LOCAL);
		}

		/**
		 * Creates and appends an undefined symbol.
		 *
		 * @param name symbol name
		 * @param size symbol size hint
		 * @param type semantic symbol type
		 * @param visibility symbol visibility
		 * @param binding symbol binding
		 * @return newly created undefined symbol
		 */
		public ImageSymbol createUndefined(
				String name,
				long size,
				ImageSymbol.Type type,
				ImageSymbol.Visibility visibility,
				ImageSymbol.Binding binding) {
			ImageSymbol symbol = new ImageSymbol(name, null, 0, size, type, visibility, binding);
			this.elements.add(symbol);
			return symbol;
		}

		/**
		 * Returns backing symbol list.
		 *
		 * @return mutable symbol list
		 */
		@Override
		public List<ImageSymbol> getElements() {
			return this.elements;
		}

		private boolean semanticallyEquals(SymbolTable other) {
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
