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
package net.boricj.bft.dwarf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;

public class Leb128UtilsTest {
	@Test
	public void testUleb128RoundTrip() throws IOException {
		List<Long> values = List.of(0L, 1L, 63L, 127L, 128L, 255L, 624485L, Long.MAX_VALUE);

		for (long value : values) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ByteOutputStream bos = ByteOutputStream.asLittleEndian(baos);
			Leb128Utils.writeUleb128(bos, value);

			ByteInputStream bis = ByteInputStream.asLittleEndian(new ByteArrayInputStream(baos.toByteArray()));
			Assertions.assertEquals(value, Leb128Utils.readUleb128(bis));
		}
	}

	@Test
	public void testSleb128RoundTrip() throws IOException {
		List<Long> values =
				List.of(0L, 1L, -1L, 63L, -63L, 127L, -127L, 624485L, -624485L, Long.MIN_VALUE, Long.MAX_VALUE);

		for (long value : values) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ByteOutputStream bos = ByteOutputStream.asLittleEndian(baos);
			Leb128Utils.writeSleb128(bos, value);

			ByteInputStream bis = ByteInputStream.asLittleEndian(new ByteArrayInputStream(baos.toByteArray()));
			Assertions.assertEquals(value, Leb128Utils.readSleb128(bis));
		}
	}
}
