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
 * Byte-range patch describing how to overwrite an inline relocation field.
 *
 * @param offset section-relative byte offset where the patch applies
 * @param bytes replacement bytes to copy into the target buffer
 */
public record RelocationFieldPatch(long offset, byte[] bytes) {
	/**
	 * Creates a relocation field patch.
	 */
	public RelocationFieldPatch {
		if (offset < 0) {
			throw new IllegalArgumentException("offset cannot be negative");
		}
		Objects.requireNonNull(bytes, "bytes");
	}

	/**
	 * Applies this patch to the supplied target byte array.
	 *
	 * @param target target array receiving the patch
	 */
	public void apply(byte[] target) {
		System.arraycopy(this.bytes, 0, target, Math.toIntExact(this.offset), this.bytes.length);
	}
}
