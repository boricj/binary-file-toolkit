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

/**
 * Output stream wrapper that counts the number of bytes written through it.
 */
public class MeteredOutputStream extends OutputStream {
	private final OutputStream out;
	private long count;

	/**
	 * Creates a metered wrapper around an existing output stream.
	 *
	 * @param out wrapped output stream
	 */
	public MeteredOutputStream(OutputStream out) {
		this.out = out;
		this.count = 0;
	}

	/**
	 * Returns the number of bytes written since construction or the last reset.
	 *
	 * @return byte count
	 */
	public long getCount() {
		return count;
	}

	/**
	 * Resets the counted byte total to zero.
	 */
	public void reset() {
		count = 0;
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
		count += 1;
	}

	@Override
	public void write(byte[] b) throws IOException {
		out.write(b);
		count += b.length;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
		count += len;
	}

	@Override
	public void close() throws IOException {
		out.close();
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}
}
