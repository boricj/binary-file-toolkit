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
package net.boricj.bft.elf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;

import net.boricj.bft.Writable;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class Utils {
	public static void compare(InputStream expected, Collection<Writable> writables) throws IOException {
		File outputFile = File.createTempFile("output", "bin");
		outputFile.deleteOnExit();
		FileOutputStream fos = new FileOutputStream(outputFile);

		Writable.write(writables, fos);

		byte[] expectedBytes = expected.readAllBytes();
		byte[] actualBytes = Files.readAllBytes(outputFile.toPath());

		assertArrayEquals(expectedBytes, actualBytes);
	}
}
