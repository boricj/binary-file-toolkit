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
package net.boricj.bft.omf.coments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.omf.OmfComent;
import net.boricj.bft.omf.OmfUtils;
import net.boricj.bft.omf.constants.OmfComentClass;

/**
 * WEAK_EXTERNALS comment (class 0xA1) - Indicates object module extensions are present.
 * Contains pairs of (weak external index, default resolution index).
 */
public class OmfComentWeakExternals extends OmfComent {
	/**
	 * A pair mapping weak external index to default resolution index.
	 *
	 * @param weakExternalIndex index of the weak external symbol
	 * @param defaultResolutionIndex index of the default resolution symbol
	 */
	public record WeakExternalPair(int weakExternalIndex, int defaultResolutionIndex) {}

	private final List<WeakExternalPair> pairs;

	/**
	 * Parses a WEAK_EXTERNALS comment from the input stream.
	 *
	 * @param bis the input stream
	 * @throws IOException if an I/O error occurs
	 */
	public OmfComentWeakExternals(ByteInputStream bis) throws IOException {
		super(OmfComentClass.WEAK_EXTERNALS);

		byte[] data = bis.readAllBytes();
		List<WeakExternalPair> pairList = new ArrayList<>();

		if (data.length > 0) {
			int[] offset = {0};
			while (offset[0] < data.length) {
				int weakIndex = OmfUtils.readIndex(data, offset);
				int defaultIndex = OmfUtils.readIndex(data, offset);
				pairList.add(new WeakExternalPair(weakIndex, defaultIndex));
			}
		}

		this.pairs = Collections.unmodifiableList(pairList);
	}

	/**
	 * Creates a WEAK_EXTERNALS comment with the specified pairs.
	 *
	 * @param pairs the list of weak external pairs
	 */
	public OmfComentWeakExternals(List<WeakExternalPair> pairs) {
		super(OmfComentClass.WEAK_EXTERNALS);
		Objects.requireNonNull(pairs);
		this.pairs = Collections.unmodifiableList(new ArrayList<>(pairs));
	}

	/**
	 * Creates an empty WEAK_EXTERNALS comment.
	 */
	public OmfComentWeakExternals() {
		this(Collections.emptyList());
	}

	/**
	 * Returns the list of weak external pairs.
	 *
	 * @return the weak external pairs
	 */
	public List<WeakExternalPair> getPairs() {
		return pairs;
	}

	@Override
	public void write(ByteOutputStream bos) throws IOException {
		for (WeakExternalPair pair : pairs) {
			OmfUtils.writeIndex(bos, pair.weakExternalIndex());
			OmfUtils.writeIndex(bos, pair.defaultResolutionIndex());
		}
	}
}
