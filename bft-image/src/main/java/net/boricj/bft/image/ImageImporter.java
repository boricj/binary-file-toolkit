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

import java.util.function.Consumer;

/**
 * Imports a native binary representation into a semantic image model.
 */
public interface ImageImporter {

	/**
	 * Imports an image while discarding diagnostic messages.
	 *
	 * @return imported semantic image
	 */
	default ImageFile importImage() {
		return importImage(message -> {});
	}

	/**
	 * Imports an image and emits diagnostic messages to the provided sink.
	 *
	 * @param logSink diagnostic message sink
	 * @return imported semantic image
	 */
	ImageFile importImage(Consumer<String> logSink);
}
