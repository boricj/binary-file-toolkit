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
package net.boricj.bft.image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CrossFormatImageAssertions {
	private CrossFormatImageAssertions() {
		// utility class
	}

	static void assertImageEquivalent(ImageFile expected, ImageFile actual) {
		assertEquals(expected.getKind(), actual.getKind(), "kind mismatch");
		Set<String> expectedSections = sectionSignatures(expected);
		Set<String> actualSections = sectionSignatures(actual);
		if (!expectedSections.equals(actualSections)) {
			Set<String> expectedOnly = new java.util.LinkedHashSet<>(expectedSections);
			expectedOnly.removeAll(actualSections);
			Set<String> actualOnly = new java.util.LinkedHashSet<>(actualSections);
			actualOnly.removeAll(expectedSections);
			assertEquals(
					expectedSections,
					actualSections,
					"section semantics mismatch; expected-only=" + expectedOnly + ", actual-only=" + actualOnly);
		}
		assertEquals(symbolSignatures(expected), symbolSignatures(actual), "symbol semantics mismatch");
	}

	private static Set<String> sectionSignatures(ImageFile image) {
		Set<String> sections = new LinkedHashSet<>();
		for (int i = 0; i < image.sections().size(); i++) {
			sections.add(sectionSignature(image.sections().get(i)));
		}
		return sections;
	}

	private static String sectionSignature(ImageSection section) {
		List<String> relocationSignatures = new ArrayList<>();
		for (int i = 0; i < section.relocations().size(); i++) {
			relocationSignatures.add(relocationSignature(section.relocations().get(i)));
		}
		Collections.sort(relocationSignatures);

		// UNALLOCATED sections are deliberately compared by payload bytes and logical shape only; vendor-specific
		// runtime/link-time metadata is not portable across COFF/ELF projections.
		String runtimeFlags =
				section.getType() == ImageSectionType.UNALLOCATED ? "" : formatFlags(section.getRuntimeFlags());
		String linktimeFlags =
				section.getType() == ImageSectionType.UNALLOCATED ? "" : formatFlags(portableLinktimeFlags(section));
		return "name=" + section.getName()
				+ "|type=" + section.getType()
				+ "|bytes=" + java.util.Arrays.toString(section.getBytes())
				+ "|logicalSize=" + section.getLogicalSize()
				+ "|alignment=" + section.getAlignment()
				+ "|runtime=" + runtimeFlags
				+ "|linktime=" + linktimeFlags
				+ "|relocations=[" + String.join(";", relocationSignatures) + "]";
	}

	private static String relocationSignature(ImageRelocationGroup group) {
		List<String> gangSignatures = new ArrayList<>();
		for (int i = 0; i < group.gangs().size(); i++) {
			gangSignatures.add(gangSignature(group.gangs().get(i)));
		}
		Collections.sort(gangSignatures);

		return "op=" + group.getOperation()
				+ "|addend=" + group.getAddend()
				+ "|target=" + targetName(group.getTarget())
				+ "|gangs=[" + String.join(";", gangSignatures) + "]";
	}

	private static String gangSignature(ImageRelocationGang gang) {
		List<String> entrySignatures = new ArrayList<>();
		for (int i = 0; i < gang.entries().size(); i++) {
			entrySignatures.add(entrySignature(gang.entries().get(i)));
		}
		Collections.sort(entrySignatures);

		return "codec=" + gang.getFieldCodec() + "|entries=[" + String.join(";", entrySignatures) + "]";
	}

	private static String entrySignature(ImageRelocationEntry entry) {
		return "offset=" + entry.getOffset() + ",localAddend=" + entry.getLocalAddend();
	}

	private static Set<String> symbolSignatures(ImageFile image) {
		Set<String> symbols = new LinkedHashSet<>();
		for (int i = 0; i < image.symbols().size(); i++) {
			ImageSymbol symbol = image.symbols().get(i);
			symbols.add(symbolSignature(symbol));
		}
		return symbols;
	}

	private static String symbolSignature(ImageSymbol symbol) {
		String sectionName =
				symbol.getSection() == null ? "<null>" : symbol.getSection().getName();
		return "name=" + symbol.getName()
				+ "|section=" + sectionName
				+ "|offset=" + symbol.getOffset()
				+ "|size=" + symbol.getSize()
				+ "|type=" + symbol.getType()
				+ "|visibility=" + symbol.getVisibility()
				+ "|binding=" + symbol.getBinding()
				+ "|role=" + symbol.getRole();
	}

	private static Set<ImageSectionLinktimeFlags> portableLinktimeFlags(ImageSection section) {
		Set<ImageSectionLinktimeFlags> flags = new java.util.LinkedHashSet<>(section.getLinktimeFlags());
		flags.remove(ImageSectionLinktimeFlags.INFO);
		flags.remove(ImageSectionLinktimeFlags.COMMENT);
		flags.remove(ImageSectionLinktimeFlags.REMOVE);
		flags.remove(ImageSectionLinktimeFlags.DISCARDABLE);
		return flags;
	}

	private static String targetName(ImageSymbol symbol) {
		return symbol == null ? "<null>" : symbol.getName();
	}

	private static String formatFlags(Set<? extends Enum<?>> flags) {
		List<String> names = new ArrayList<>();
		for (var flag : flags) {
			names.add(flag.name());
		}
		Collections.sort(names);
		return String.join(",", names);
	}
}
