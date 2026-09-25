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
 * Opaque read/write codec for a relocation field.
 */
public interface RelocationFieldCodec {
	/**
	 * Returns the width of the relocation field in bytes.
	 *
	 * @return byte width of the field container
	 */
	int byteWidth();

	/**
	 * Reads a field value from {@code bytes} at {@code offset}.
	 *
	 * @param bytes backing byte array containing the encoded field
	 * @param offset byte index of the field in {@code bytes}
	 * @return decoded relocation field value
	 */
	long read(byte[] bytes, int offset);

	/**
	 * Writes {@code value} into {@code bytes} at {@code offset}.
	 *
	 * <p>The value must be representable by the concrete field encoding. Implementations
	 * should throw {@link IllegalArgumentException} when encoding would overflow or truncate.
	 *
	 * @param bytes backing byte array receiving the encoded field
	 * @param offset byte index of the field in {@code bytes}
	 * @param value decoded relocation field value to encode
	 */
	void write(byte[] bytes, int offset, long value);
}
