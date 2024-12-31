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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class Utils {
	private Utils() {}

	public static String decodeNullTerminatedString(byte[] bytes, Charset charset) {
		int idx;
		for (idx = 0; idx < bytes.length; idx++) {
			if (bytes[idx] == 0x00) {
				break;
			}
		}

		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, 0, idx);
		return charset.decode(byteBuffer).toString();
	}

	public static int roundUp(int value, int alignment) {
		if (alignment > 0) {
			value = ((value + alignment - 1) / alignment) * alignment;
		}

		return value;
	}

	public static long roundUp(long value, long alignment) {
		if (alignment > 0) {
			value = ((value + alignment - 1) / alignment) * alignment;
		}

		return value;
	}
}
