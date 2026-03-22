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
package net.boricj.bft;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

/** Interface for objects that can serialize themselves to an output stream. */
public interface StreamWritable {
	/**
	 * Writes a collection of stream writables sequentially to an output stream.
	 *
	 * @param writables collection of objects to write in iteration order
	 * @param outputStream stream to write to
	 * @throws IOException if writing fails
	 */
	public static void write(List<? extends StreamWritable> writables, OutputStream outputStream) throws IOException {
		Objects.requireNonNull(writables);
		Objects.requireNonNull(outputStream);

		for (StreamWritable writable : writables) {
			writable.write(outputStream);
		}
	}

	/**
	 * Returns the length in bytes of this object's serialized form.
	 *
	 * @return the serialized length in bytes
	 */
	public default long getLength() {
		try (MeteredOutputStream outputStream = new MeteredOutputStream(OutputStream.nullOutputStream())) {
			write(outputStream);
			return outputStream.getCount();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to measure serialized length", e);
		}
	}

	/**
	 * Writes this object to the given output stream.
	 *
	 * @param outputStream the stream to write to
	 * @throws IOException if writing fails
	 */
	public void write(OutputStream outputStream) throws IOException;
}
