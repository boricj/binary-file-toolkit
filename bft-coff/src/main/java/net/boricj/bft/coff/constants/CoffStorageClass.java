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
package net.boricj.bft.coff.constants;

public enum CoffStorageClass {
	IMAGE_SYM_CLASS_END_OF_FUNCTION((byte) -1),
	IMAGE_SYM_CLASS_NULL((byte) 0),
	IMAGE_SYM_CLASS_AUTOMATIC((byte) 1),
	IMAGE_SYM_CLASS_EXTERNAL((byte) 2),
	IMAGE_SYM_CLASS_STATIC((byte) 3),
	IMAGE_SYM_CLASS_REGISTER((byte) 4),
	IMAGE_SYM_CLASS_EXTERNAL_DEF((byte) 5),
	IMAGE_SYM_CLASS_LABEL((byte) 6),
	IMAGE_SYM_CLASS_UNDEFINED_LABEL((byte) 7),
	IMAGE_SYM_CLASS_MEMBER_OF_STRUCT((byte) 8),
	IMAGE_SYM_CLASS_ARGUMENT((byte) 9),
	IMAGE_SYM_CLASS_STRUCT_TAG((byte) 10),
	IMAGE_SYM_CLASS_MEMBER_OF_UNION((byte) 11),
	IMAGE_SYM_CLASS_UNION_TAG((byte) 12),
	IMAGE_SYM_CLASS_TYPE_DEFINITION((byte) 13),
	IMAGE_SYM_CLASS_UNDEFINED_STATIC((byte) 14),
	IMAGE_SYM_CLASS_ENUM_TAG((byte) 15),
	IMAGE_SYM_CLASS_MEMBER_OF_ENUM((byte) 16),
	IMAGE_SYM_CLASS_REGISTER_PARAM((byte) 17),
	IMAGE_SYM_CLASS_BIT_FIELD((byte) 18),
	IMAGE_SYM_CLASS_BLOCK((byte) 100),
	IMAGE_SYM_CLASS_FUNCTION((byte) 101),
	IMAGE_SYM_CLASS_END_OF_STRUCT((byte) 102),
	IMAGE_SYM_CLASS_FILE((byte) 103),
	IMAGE_SYM_CLASS_SECTION((byte) 104),
	IMAGE_SYM_CLASS_WEAK_EXTERNAL((byte) 105),
	IMAGE_SYM_CLASS_CLR_TOKEN((byte) 107),
	;

	private final byte value;

	CoffStorageClass(byte value) {
		this.value = value;
	}

	public byte getValue() {
		return value;
	}

	public static CoffStorageClass valueFrom(byte value) {
		for (CoffStorageClass storageClass : values()) {
			if (storageClass.getValue() == value) {
				return storageClass;
			}
		}

		throw new IllegalArgumentException();
	}
}
