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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.AssertionFailureBuilder;

/**
 * Utility methods for testing binary file serialization and deserialization.
 *
 * <p>This class provides helper methods for comparing serialized output against expected binary data,
 * with support for applying patches and detailed error reporting on mismatches.
 */
public class TestUtils {
	private record Span(int offset, int length) {}

	private TestUtils() {
		// Utility class, no instances allowed
	}

	/**
	 * Compares serialized output from a collection of writables against expected binary data.
	 *
	 * @param expected the expected binary data as an input stream
	 * @param writables the collection of writable objects to serialize
	 * @throws IOException if an I/O error occurs during serialization or comparison
	 */
	public static void compare(InputStream expected, Collection<Writable> writables) throws IOException {
		compare(expected, writables, Collections.emptyMap());
	}

	/**
	 * Compares serialized output from a collection of writables against expected binary data,
	 * with support for applying patches to the actual output before comparison.
	 *
	 * @param expected the expected binary data as an input stream
	 * @param writables the collection of writable objects to serialize
	 * @param patches a map of byte offsets to byte arrays to patch into the actual output before comparison
	 * @throws IOException if an I/O error occurs during serialization or comparison
	 */
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

	/**
	 * Asserts that two byte arrays are equal. If they differ, throws an assertion failure with
	 * detailed context showing the first difference location and surrounding bytes.
	 *
	 * @param expected the expected byte array
	 * @param actual the actual byte array
	 */
	public static void assertArrayEquals(byte[] expected, byte[] actual) {
		StringBuffer sb = new StringBuffer();
		if (expected.length != actual.length) {
			sb.append(String.format(
					"Byte arrays differ in length: expected %d bytes, actual %d bytes.\n",
					expected.length, actual.length));
		}

		List<Span> differences = new ArrayList<>();

		// Find differences.
		boolean inDiff = false;
		for (int i = 0; i < Math.max(expected.length, actual.length); i++) {
			if (i >= Math.min(expected.length, actual.length) || (expected[i] != actual[i])) {
				if (!inDiff) {
					differences.add(new Span(i, 0));
					inDiff = true;
				}
			} else if (expected[i] == actual[i]) {
				if (inDiff) {
					Span last = differences.get(differences.size() - 1);
					differences.set(differences.size() - 1, new Span(last.offset(), i - last.offset()));
					inDiff = false;
				}
			}
		}

		if (inDiff) {
			Span last = differences.get(differences.size() - 1);
			differences.set(
					differences.size() - 1,
					new Span(last.offset(), Math.max(expected.length, actual.length) - last.offset()));
			inDiff = false;
		}

		int count = 0;
		for (Span diff : differences) {
			// Build context around the first difference (12 bytes before and after) for error message.
			int start = Math.max(0, diff.offset() - 12);
			int end = Math.min(start + 24, Math.max(expected.length, actual.length));

			StringBuffer expected_values = new StringBuffer();
			StringBuffer actual_values = new StringBuffer();

			if (start > 0) {
				expected_values.append("... ");
				actual_values.append("... ");
			}

			for (int i = start; i <= end; i++) {
				String before = " ";
				String after = " ";
				String exp = i < expected.length ? String.format("%02x", expected[i] & 0xff) : "--";
				String act = i < actual.length ? String.format("%02x", actual[i] & 0xff) : "--";

				if (i == diff.offset()) {
					before = "[";
				}
				if (i == diff.offset() + diff.length() - 1) {
					after = "]";
				}

				expected_values.append(before);
				expected_values.append(exp);
				expected_values.append(after);
				actual_values.append(before);
				actual_values.append(act);
				actual_values.append(after);
			}

			if (end < expected.length) {
				expected_values.append("... ");
			}
			if (end < actual.length) {
				actual_values.append("... ");
			}

			sb.append(String.format(
					"Byte arrays differ at offset 0x%04x (byte %d) length %d:\n",
					diff.offset(), diff.offset(), diff.length()));
			sb.append(String.format("Expected:\n  %s\n", expected_values.toString()));
			sb.append(String.format("Actual:\n  %s\n", actual_values.toString()));

			if (++count > 4) {
				sb.append(String.format("... and %d more differences.\n", differences.size() - count));
				break;
			}
		}

		if (sb.length() > 0) {
			AssertionFailureBuilder.assertionFailure().message(sb.toString()).buildAndThrow();
		}
	}

	/**
	 * Finds an item in an indirect list collection by comparing a property value.
	 *
	 * @param <Col> the collection type extending IndirectList
	 * @param <Value> the type of the property value to search for
	 * @param <Item> the type of items in the collection
	 * @param collection the collection to search
	 * @param nameGetter a function that extracts the property value from an item
	 * @param value the property value to search for
	 * @return the first matching item, or null if not found
	 */
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
