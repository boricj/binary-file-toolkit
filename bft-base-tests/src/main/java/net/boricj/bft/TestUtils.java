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
import java.util.function.Function;

import org.junit.jupiter.api.AssertionFailureBuilder;

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

	public static void assertArrayEquals(byte[] expected, byte[] actual) {
		// Find first difference for debugging.
		int firstDiff = -1;
		for (int i = 0; i < Math.max(expected.length, actual.length); i++) {
			if (i == Math.min(expected.length, actual.length) || (expected[i] != actual[i])) {
				firstDiff = i;
				break;
			}
		}

		if (firstDiff >= 0) {
			// Build context around the first difference (12 bytes before and after) for error message.
			int start = Math.max(0, firstDiff - 12);
			int end = Math.min(start + 24, Math.max(expected.length, actual.length));

			StringBuffer expected_values = new StringBuffer();
			StringBuffer actual_values = new StringBuffer();

			if (start > 0) {
				expected_values.append("... ");
				actual_values.append("... ");
			}

			for (int i = start; i <= end; i++) {
				String exp = i < expected.length ? String.format("%02x", expected[i] & 0xff) : "--";
				String act = i < actual.length ? String.format("%02x", actual[i] & 0xff) : "--";

				if (i == firstDiff) {
					exp = "[" + exp + "]";
					act = "[" + act + "]";
				} else {
					exp = " " + exp + " ";
					act = " " + act + " ";
				}
				expected_values.append(exp);
				actual_values.append(act);
			}

			if (end < expected.length) {
				expected_values.append("... ");
			}
			if (end < actual.length) {
				actual_values.append("... ");
			}

			// Format error message with context around the first difference.
			StringBuffer sb = new StringBuffer();
			if (expected.length != actual.length) {
				sb.append(String.format(
						"Byte arrays differ in length: expected %d bytes, actual %d bytes.\n",
						expected.length, actual.length));
			}
			sb.append(String.format("Byte arrays differ at offset 0x%04x (byte %d):\n", firstDiff, firstDiff));
			sb.append(String.format("Expected:\n  %s\n", expected_values.toString()));
			sb.append(String.format("Actual:\n  %s\n", actual_values.toString()));

			AssertionFailureBuilder.assertionFailure().message(sb.toString()).buildAndThrow();
		}
	}

	public static <Col extends IndirectList<Item>, Value, Item> Item findBy(
			Col collection, Function<? super Item, String> nameGetter, Value value) {
		for (Item item : collection.getElements()) {
			if (value.equals(nameGetter.apply(item))) {
				return item;
			}
		}

		return null;
	}
}
