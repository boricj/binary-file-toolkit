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

/**
 * Coarse semantic operation family for relocation kinds.
 */
public enum RelocationOperation {
	/** Absolute relocation semantics. */
	ABSOLUTE,
	/** Program-counter-relative relocation semantics. */
	PC_RELATIVE,
	/** Region-base-relative relocation semantics. */
	REGION_RELATIVE,
	/** Global offset table relocation semantics. */
	GOT,
	/** Global pointer relative relocation semantics. */
	GP,
	/** Procedure linkage table relocation semantics. */
	PLT,
	/** Thread-local storage relocation semantics. */
	TLS,
	/** Relocation semantics not covered by predefined categories. */
	OTHER
}
