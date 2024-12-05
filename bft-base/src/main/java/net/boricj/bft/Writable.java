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

public interface Writable {
	public long getOffset();

	public long getLength();

	public void write(OutputStream outputStream) throws IOException;

	public static void write(Collection<Writable> writables, OutputStream outputStream) throws IOException {
		write(writables, outputStream, 0x00);
	}

	public static void write(Collection<Writable> writables, OutputStream outputStream, int padding)
			throws IOException {
		List<Writable> sortedWritables = writables.stream()
				.sorted(Comparator.comparingLong(w -> w.getOffset()))
				.collect(Collectors.toList());

		Writable previousWritable = null;
		long previousOffset = 0;
		long previousLength = 0;
		for (Writable writable : sortedWritables) {
			long offset = writable.getOffset();
			long length = writable.getLength();

			if (previousOffset + previousLength > offset + length && length > 0) {
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
			writable.write(mos);
			long bytesWritten = mos.getCount() - offset;
			long length = writable.getLength();

			if (bytesWritten != length) {
				String fmt = "%s has length %d bytes, wrote %d bytes";
				String msg = String.format(fmt, writable, length, bytesWritten);
				throw new RuntimeException(msg);
			}
		}
	}
}
