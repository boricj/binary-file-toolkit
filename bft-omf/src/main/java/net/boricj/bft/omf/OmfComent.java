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
package net.boricj.bft.omf;

import java.io.IOException;
import java.util.Objects;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.omf.coments.OmfComentCompiler;
import net.boricj.bft.omf.coments.OmfComentLibrarySearch;
import net.boricj.bft.omf.coments.OmfComentTranslator;
import net.boricj.bft.omf.coments.OmfComentWeakExternals;
import net.boricj.bft.omf.constants.OmfComentClass;

/**
 * Abstract base class for OMF comment record data.
 * Subclasses provide structured access to different comment types.
 */
public abstract class OmfComent {
	/** The comment class identifying the comment type. */
	protected final OmfComentClass commentClass;

	/**
	 * Creates a new comment with the specified class.
	 *
	 * @param commentClass the comment class
	 */
	protected OmfComent(OmfComentClass commentClass) {
		Objects.requireNonNull(commentClass);
		this.commentClass = commentClass;
	}

	/**
	 * Returns the comment class.
	 *
	 * @return the comment class
	 */
	public OmfComentClass getCommentClass() {
		return commentClass;
	}

	/**
	 * Writes the comment data (excluding the attribute byte and class byte).
	 *
	 * @param bos the output stream
	 * @throws IOException if an I/O error occurs
	 */
	public abstract void write(ByteOutputStream bos) throws IOException;

	/**
	 * Parse comment data and create appropriate subclass instance.
	 *
	 * @param commentClass The comment class type
	 * @param bis ByteInputStream containing the comment data (excluding attribute and class bytes)
	 * @return A concrete OmfComent subclass instance
	 * @throws IOException If parsing fails
	 */
	public static OmfComent parse(OmfComentClass commentClass, ByteInputStream bis) throws IOException {
		switch (commentClass) {
			case TRANSLATOR:
				return new OmfComentTranslator(bis);
			case WEAK_EXTERNALS:
				return new OmfComentWeakExternals(bis);
			case LIBRARY_SEARCH:
				return new OmfComentLibrarySearch(bis);
			case COMPILER:
				return new OmfComentCompiler(bis);
			default:
				throw new IllegalArgumentException("Unsupported OMF comment class: " + commentClass);
		}
	}
}
