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

/**
 * Utility methods for binary file processing.
 */
public class Utils {
	private Utils() {}

	/**
	 * Rounds up an int value to the next multiple of the alignment.
	 *
	 * @param value the value to round
	 * @param alignment the alignment boundary
	 * @return the rounded value
	 */
	public static int roundUp(int value, int alignment) {
		if (alignment > 0) {
			value = ((value + alignment - 1) / alignment) * alignment;
		}

		return value;
	}

	/**
	 * Rounds up a long value to the next multiple of the alignment.
	 *
	 * @param value the value to round
	 * @param alignment the alignment boundary
	 * @return the rounded value
	 */
	public static long roundUp(long value, long alignment) {
		if (alignment > 0) {
			value = ((value + alignment - 1) / alignment) * alignment;
		}

		return value;
	}
}
