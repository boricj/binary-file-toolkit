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

/** COFF symbol storage classes. */
public enum CoffStorageClass {
	/** End of function marker. */
	IMAGE_SYM_CLASS_END_OF_FUNCTION((byte) -1),
	/** Null storage class. */
	IMAGE_SYM_CLASS_NULL((byte) 0),
	/** Automatic variable. */
	IMAGE_SYM_CLASS_AUTOMATIC((byte) 1),
	/** External symbol. */
	IMAGE_SYM_CLASS_EXTERNAL((byte) 2),
	/** Static symbol. */
	IMAGE_SYM_CLASS_STATIC((byte) 3),
	/** Register variable. */
	IMAGE_SYM_CLASS_REGISTER((byte) 4),
	/** External definition. */
	IMAGE_SYM_CLASS_EXTERNAL_DEF((byte) 5),
	/** Label symbol. */
	IMAGE_SYM_CLASS_LABEL((byte) 6),
	/** Undefined label. */
	IMAGE_SYM_CLASS_UNDEFINED_LABEL((byte) 7),
	/** Struct member. */
	IMAGE_SYM_CLASS_MEMBER_OF_STRUCT((byte) 8),
	/** Function argument. */
	IMAGE_SYM_CLASS_ARGUMENT((byte) 9),
	/** Struct tag. */
	IMAGE_SYM_CLASS_STRUCT_TAG((byte) 10),
	/** Union member. */
	IMAGE_SYM_CLASS_MEMBER_OF_UNION((byte) 11),
	/** Union tag. */
	IMAGE_SYM_CLASS_UNION_TAG((byte) 12),
	/** Type definition. */
	IMAGE_SYM_CLASS_TYPE_DEFINITION((byte) 13),
	/** Undefined static symbol. */
	IMAGE_SYM_CLASS_UNDEFINED_STATIC((byte) 14),
	/** Enum tag. */
	IMAGE_SYM_CLASS_ENUM_TAG((byte) 15),
	/** Enum member. */
	IMAGE_SYM_CLASS_MEMBER_OF_ENUM((byte) 16),
	/** Register parameter. */
	IMAGE_SYM_CLASS_REGISTER_PARAM((byte) 17),
	/** Bit-field member. */
	IMAGE_SYM_CLASS_BIT_FIELD((byte) 18),
	/** Block marker. */
	IMAGE_SYM_CLASS_BLOCK((byte) 100),
	/** Function marker. */
	IMAGE_SYM_CLASS_FUNCTION((byte) 101),
	/** End of struct marker. */
	IMAGE_SYM_CLASS_END_OF_STRUCT((byte) 102),
	/** File symbol. */
	IMAGE_SYM_CLASS_FILE((byte) 103),
	/** Section symbol. */
	IMAGE_SYM_CLASS_SECTION((byte) 104),
	/** Weak external symbol. */
	IMAGE_SYM_CLASS_WEAK_EXTERNAL((byte) 105),
	/** CLR token. */
	IMAGE_SYM_CLASS_CLR_TOKEN((byte) 107),
	;

	private final byte value;

	CoffStorageClass(byte value) {
		this.value = value;
	}

	/**
	 * Returns the numeric storage class value.
	 *
	 * @return the numeric storage class value
	 */
	public byte getValue() {
		return value;
	}

	/**
	 * Returns the storage class for a numeric value.
	 *
	 * @param value storage class value
	 * @return matching storage class
	 * @throws IllegalArgumentException if {@code value} is not mapped
	 */
	public static CoffStorageClass valueFrom(byte value) {
		for (CoffStorageClass storageClass : values()) {
			if (storageClass.getValue() == value) {
				return storageClass;
			}
		}

		throw new IllegalArgumentException();
	}
}
