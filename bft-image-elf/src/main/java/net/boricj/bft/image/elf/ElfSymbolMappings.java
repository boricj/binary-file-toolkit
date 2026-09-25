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
package net.boricj.bft.image.elf;

import net.boricj.bft.elf.constants.ElfSymbolBinding;
import net.boricj.bft.elf.constants.ElfSymbolType;
import net.boricj.bft.elf.constants.ElfSymbolVisibility;
import net.boricj.bft.elf.constants.ElfType;
import net.boricj.bft.image.ImageFile;
import net.boricj.bft.image.ImageSymbol;

final class ElfSymbolMappings {
	private ElfSymbolMappings() {
		// Utility class.
	}

	static ElfType mapImageKindToElfType(ImageFile.Kind kind) {
		return switch (kind) {
			case OBJECT -> ElfType.ET_REL;
			case EXECUTABLE -> ElfType.ET_EXEC;
			case DYNAMIC_LIBRARY -> ElfType.ET_DYN;
		};
	}

	static ElfSymbolType mapImageTypeToElfType(ImageSymbol.Type type) {
		return switch (type) {
			case NOTYPE -> ElfSymbolType.STT_NOTYPE;
			case OBJECT -> ElfSymbolType.STT_OBJECT;
			case FUNCTION -> ElfSymbolType.STT_FUNC;
			case SECTION -> ElfSymbolType.STT_SECTION;
			case FILE -> ElfSymbolType.STT_FILE;
			case TLS -> ElfSymbolType.STT_TLS;
		};
	}

	static ElfSymbolVisibility mapImageVisibilityToElfVisibility(ImageSymbol.Visibility visibility) {
		return switch (visibility) {
			case DEFAULT -> ElfSymbolVisibility.STV_DEFAULT;
			case INTERNAL -> ElfSymbolVisibility.STV_INTERNAL;
			case HIDDEN -> ElfSymbolVisibility.STV_HIDDEN;
			case PROTECTED -> ElfSymbolVisibility.STV_PROTECTED;
		};
	}

	static ElfSymbolBinding mapImageBindingToElfBinding(ImageSymbol.Binding binding) {
		return switch (binding) {
			case LOCAL -> ElfSymbolBinding.STB_LOCAL;
			case GLOBAL -> ElfSymbolBinding.STB_GLOBAL;
			case WEAK -> ElfSymbolBinding.STB_WEAK;
		};
	}

	static ImageSymbol.Type mapElfTypeToImageType(ElfSymbolType type) {
		return switch (type) {
			case STT_NOTYPE -> ImageSymbol.Type.NOTYPE;
			case STT_OBJECT -> ImageSymbol.Type.OBJECT;
			case STT_FUNC -> ImageSymbol.Type.FUNCTION;
			case STT_SECTION -> ImageSymbol.Type.SECTION;
			case STT_FILE -> ImageSymbol.Type.FILE;
			case STT_TLS -> ImageSymbol.Type.TLS;
		};
	}

	static ImageSymbol.Visibility mapElfVisibilityToImageVisibility(ElfSymbolVisibility visibility) {
		return switch (visibility) {
			case STV_DEFAULT -> ImageSymbol.Visibility.DEFAULT;
			case STV_INTERNAL -> ImageSymbol.Visibility.INTERNAL;
			case STV_HIDDEN -> ImageSymbol.Visibility.HIDDEN;
			case STV_PROTECTED -> ImageSymbol.Visibility.PROTECTED;
		};
	}

	static ImageSymbol.Binding mapElfBindingToImageBinding(ElfSymbolBinding binding) {
		return switch (binding) {
			case STB_LOCAL -> ImageSymbol.Binding.LOCAL;
			case STB_GLOBAL -> ImageSymbol.Binding.GLOBAL;
			case STB_WEAK -> ImageSymbol.Binding.WEAK;
		};
	}
}
