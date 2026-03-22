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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestStreamWritable {
	private static class BytesWritable implements StreamWritable {
		private final byte[] bytes;

		private BytesWritable(byte[] bytes) {
			this.bytes = bytes;
		}

		@Override
		public void write(OutputStream outputStream) throws IOException {
			outputStream.write(bytes);
		}
	}

	private static class FailingWritable implements StreamWritable {
		@Override
		public void write(OutputStream outputStream) throws IOException {
			throw new IOException("boom");
		}
	}

	@Test
	public void testWriteCollectionSequentialOrder() throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		StreamWritable.write(
				List.of(
						new BytesWritable(new byte[] {0x01, 0x02}),
						new BytesWritable(new byte[] {0x03}),
						new BytesWritable(new byte[] {0x04, 0x05})),
				outputStream);

		assertArrayEquals(new byte[] {0x01, 0x02, 0x03, 0x04, 0x05}, outputStream.toByteArray());
	}

	@Test
	public void testWriteCollectionPropagatesIOException() {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		assertThrows(
				IOException.class,
				() -> StreamWritable.write(
						List.of(new BytesWritable(new byte[] {0x01}), new FailingWritable()), outputStream));
	}
}
