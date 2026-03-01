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

public abstract class CodeViewTable {
	protected final CodeViewFile codeView;

	private final String name;
	private final CodeViewSignature signature;

	public CodeViewTable(CodeViewFile codeView, String name, CodeViewSignature signature) {
		this.codeView = codeView;
		this.name = name;
		this.signature = signature;
	}

	public String getName() {
		return name;
	}

	public CodeViewSignature getSignature() {
		return signature;
	}

	public abstract void write(OutputStream outputStream) throws IOException;
}
