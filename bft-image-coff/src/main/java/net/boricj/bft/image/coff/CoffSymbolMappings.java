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
package net.boricj.bft.image.coff;

import net.boricj.bft.coff.CoffSymbolTable.CoffSymbol;
import net.boricj.bft.coff.constants.CoffStorageClass;
import net.boricj.bft.image.ImageSymbol;

final class CoffSymbolMappings {
	private CoffSymbolMappings() {
		// Utility class.
	}

	static CoffStorageClass mapStorageClass(ImageSymbol symbol) {
		return switch (symbol.getBinding()) {
			case GLOBAL -> CoffStorageClass.IMAGE_SYM_CLASS_EXTERNAL;
			case WEAK -> CoffStorageClass.IMAGE_SYM_CLASS_WEAK_EXTERNAL;
			case LOCAL -> {
				if (symbol.getType() == ImageSymbol.Type.SECTION) {
					yield CoffStorageClass.IMAGE_SYM_CLASS_STATIC;
				}
				if (symbol.getRole() == ImageSymbol.Role.LOCAL_LABEL) {
					yield CoffStorageClass.IMAGE_SYM_CLASS_LABEL;
				}
				yield CoffStorageClass.IMAGE_SYM_CLASS_STATIC;
			}
		};
	}

	static byte mapImageTypeToCoffType(ImageSymbol.Type type) {
		return switch (type) {
			case NOTYPE -> 0x00;
			case OBJECT -> 0x01;
			case FUNCTION -> 0x20;
			case SECTION -> 0x00;
			case FILE -> 0x00;
			case TLS -> 0x00;
		};
	}

	static ImageSymbol.Type mapCoffTypeToImageType(CoffSymbol symbol) {
		if (symbol.getStorageClass() == CoffStorageClass.IMAGE_SYM_CLASS_FILE) {
			return ImageSymbol.Type.FILE;
		}
		if (symbol.getStorageClass() == CoffStorageClass.IMAGE_SYM_CLASS_SECTION) {
			return ImageSymbol.Type.SECTION;
		}
		if (symbol.getType() == 0x20) {
			return ImageSymbol.Type.FUNCTION;
		}
		if (symbol.getType() == 0x00) {
			return ImageSymbol.Type.NOTYPE;
		}
		return ImageSymbol.Type.OBJECT;
	}

	static ImageSymbol.Binding mapCoffStorageClassToImageBinding(CoffSymbol symbol) {
		if (symbol.getStorageClass() == CoffStorageClass.IMAGE_SYM_CLASS_EXTERNAL
				|| symbol.getStorageClass() == CoffStorageClass.IMAGE_SYM_CLASS_EXTERNAL_DEF
				|| symbol.getStorageClass() == CoffStorageClass.IMAGE_SYM_CLASS_WEAK_EXTERNAL) {
			return ImageSymbol.Binding.GLOBAL;
		}
		return ImageSymbol.Binding.LOCAL;
	}

	static ImageSymbol.Role mapCoffStorageClassToImageRole(CoffSymbol symbol) {
		if (symbol.getStorageClass() == CoffStorageClass.IMAGE_SYM_CLASS_LABEL) {
			return ImageSymbol.Role.LOCAL_LABEL;
		}
		return ImageSymbol.Role.UNSPECIFIED;
	}
}
