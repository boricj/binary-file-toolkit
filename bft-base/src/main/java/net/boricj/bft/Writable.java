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
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Interface for objects that can be written to an output stream at a specific offset. */
public interface Writable {
	/**
	 * Returns the file offset where this object should be written.
	 *
	 * @return the file offset in bytes
	 */
	public long getOffset();

	/**
	 * Returns the length in bytes of this object's serialized form.
	 *
	 * @return the serialized length in bytes
	 */
	public long getLength();

	/**
	 * Writes this object to the given output stream.
	 *
	 * @param outputStream the stream to write to
	 * @throws IOException if writing fails
	 */
	public void write(OutputStream outputStream) throws IOException;

	/**
	 * Writes a collection of writables to an output stream, filling gaps with zero bytes.
	 *
	 * @param writables collection of objects to write
	 * @param outputStream stream to write to
	 * @throws IOException if writing fails
	 */
	public static void write(Collection<Writable> writables, OutputStream outputStream) throws IOException {
		write(writables, outputStream, 0x00);
	}

	/**
	 * Writes a collection of writables to an output stream, filling gaps with a specified padding byte.
	 *
	 * @param writables collection of objects to write
	 * @param outputStream stream to write to
	 * @param padding byte value to use for padding between objects
	 * @throws IOException if writing fails
	 */
	public static void write(Collection<Writable> writables, OutputStream outputStream, int padding)
			throws IOException {
		List<Writable> sortedWritables = writables.stream()
				.filter(w -> w.getLength() > 0)
				.sorted(Comparator.comparingLong(w -> w.getOffset()))
				.collect(Collectors.toList());

		Writable previousWritable = null;
		long previousOffset = 0;
		long previousLength = 0;
		for (Writable writable : sortedWritables) {
			long offset = writable.getOffset();
			long length = writable.getLength();

			if (previousOffset + previousLength > offset) {
				String fmt = "%s (offset: %d, length: %d) overlaps with %s (offset: %d, length %d)";
				String msg =
						String.format(fmt, previousWritable, previousOffset, previousLength, writable, offset, length);
				throw new RuntimeException(msg);
			}

			previousWritable = writable;
			previousOffset = offset;
			previousLength = length;
		}

		MeteredOutputStream mos = new MeteredOutputStream(outputStream);
		for (Writable writable : sortedWritables) {
			for (long offset = mos.getCount(); offset < writable.getOffset(); offset++) {
				mos.write(0x00);
			}

			long offset = mos.getCount();
			long length = writable.getLength();
			writable.write(mos);
			long bytesWritten = mos.getCount() - offset;

			if (bytesWritten != length) {
				String fmt = "%s has length %d bytes, wrote %d bytes";
				String msg = String.format(fmt, writable, length, bytesWritten);
				throw new RuntimeException(msg);
			}
		}
	}
}
