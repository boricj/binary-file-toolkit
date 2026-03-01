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
package net.boricj.bft.codeview.constants;

public enum CodeViewSignature {
	CV_SIGNATURE_C6(0), // Actual signature is >64K
	CV_SIGNATURE_C7(1), // First explicit signature
	CV_SIGNATURE_C11(2), // C11 (vc5.x) 32-bit types
	CV_SIGNATURE_C13(4); // C13 (vc7.x) zero terminated names

	private final int value;

	CodeViewSignature(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static CodeViewSignature valueFrom(int value) {
		for (CodeViewSignature signature : values()) {
			if (signature.getValue() == value) {
				return signature;
			}
		}

		throw new IllegalArgumentException();
	}
}
