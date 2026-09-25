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
package net.boricj.bft.codeview;

import java.io.IOException;
import java.io.OutputStream;

import net.boricj.bft.codeview.constants.CodeViewSignature;

/**
 * Base abstraction for a named CodeView table payload.
 */
public abstract class CodeViewTable {
	/** Owning CodeView file model. */
	protected final CodeViewFile codeView;

	private final String name;
	private final CodeViewSignature signature;

	/**
	 * Creates a CodeView table descriptor.
	 *
	 * @param codeView owning file model
	 * @param name source section name
	 * @param signature table signature
	 */
	public CodeViewTable(CodeViewFile codeView, String name, CodeViewSignature signature) {
		this.codeView = codeView;
		this.name = name;
		this.signature = signature;
	}

	/**
	 * Returns source section name.
	 *
	 * @return section name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns table signature.
	 *
	 * @return CodeView signature
	 */
	public CodeViewSignature getSignature() {
		return signature;
	}

	/**
	 * Writes the complete encoded table payload.
	 *
	 * @param outputStream destination stream
	 * @throws IOException if writing fails
	 */
	public abstract void write(OutputStream outputStream) throws IOException;
}
