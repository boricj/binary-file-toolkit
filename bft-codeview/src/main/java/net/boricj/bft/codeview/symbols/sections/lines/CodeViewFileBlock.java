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
package net.boricj.bft.codeview.symbols.sections.lines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CodeViewFileBlock {
	private final int offFile;
	private final List<CodeViewLineEntry> lines;

	public CodeViewFileBlock(int offFile, List<CodeViewLineEntry> lines) {
		this.offFile = offFile;
		this.lines = Collections.unmodifiableList(new ArrayList<>(lines));
	}

	public int getOffFile() {
		return offFile;
	}

	public List<CodeViewLineEntry> getLines() {
		return lines;
	}
}
