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
 * Runtime protection flags that describe how a section may be used in-memory.
 */
public enum ImageSectionRuntimeFlags {
	/** Section is readable at runtime. */
	READ,
	/** Section is writable at runtime. */
	WRITE,
	/** Section is executable at runtime. */
	EXECUTE,
}
