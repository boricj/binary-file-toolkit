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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class TestUtils {
	public static void compare(InputStream expected, Collection<Writable> writables) throws IOException {
		compare(expected, writables, Collections.emptyMap());
	}

	public static void compare(InputStream expected, Collection<Writable> writables, Map<Integer, byte[]> patches)
			throws IOException {
		File outputFile = File.createTempFile("output", ".bin");
		outputFile.deleteOnExit();
		try (FileOutputStream fos = new FileOutputStream(outputFile)) {
			try (BufferedOutputStream bos = new BufferedOutputStream(fos)) {
				Writable.write(writables, bos);
			}
		}

		byte[] expectedBytes = expected.readAllBytes();
		byte[] actualBytes = Files.readAllBytes(outputFile.toPath());
		for (Map.Entry<Integer, byte[]> entry : patches.entrySet()) {
			byte[] patch = entry.getValue();
			System.arraycopy(patch, 0, actualBytes, entry.getKey(), patch.length);
		}

		assertArrayEquals(expectedBytes, actualBytes);
	}
}
