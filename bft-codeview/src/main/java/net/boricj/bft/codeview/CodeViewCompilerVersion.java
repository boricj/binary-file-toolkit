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
package net.boricj.bft.codeview;

/**
 * Four-part compiler version value found in CodeView metadata.
 */
public class CodeViewCompilerVersion {
	private final int major;
	private final int minor;
	private final int build;
	private final int revision;

	/**
	 * Creates a compiler version tuple.
	 *
	 * @param major major version component in range {@code [0, 65535]}
	 * @param minor minor version component in range {@code [0, 65535]}
	 * @param build build number component in range {@code [0, 65535]}
	 * @param revision revision component in range {@code [0, 65535]}
	 */
	public CodeViewCompilerVersion(int major, int minor, int build, int revision) {
		if ((major < 0 || minor < 0 || build < 0 || revision < 0)
				|| (major > 0xFFFF || minor > 0xFFFF || build > 0xFFFF || revision > 0xFFFF)) {
			throw new IllegalArgumentException("Version numbers out of range");
		}

		this.major = major;
		this.minor = minor;
		this.build = build;
		this.revision = revision;
	}

	/**
	 * Returns the major version component.
	 *
	 * @return major version component
	 */
	public int getMajor() {
		return major;
	}

	/**
	 * Returns the minor version component.
	 *
	 * @return minor version component
	 */
	public int getMinor() {
		return minor;
	}

	/**
	 * Returns the build number component.
	 *
	 * @return build number component
	 */
	public int getBuild() {
		return build;
	}

	/**
	 * Returns the revision component.
	 *
	 * @return revision component
	 */
	public int getRevision() {
		return revision;
	}

	@Override
	public boolean equals(Object obj) {
		CodeViewCompilerVersion other = (CodeViewCompilerVersion) obj;
		return major == other.major && minor == other.minor && build == other.build && revision == other.revision;
	}

	@Override
	public String toString() {
		return "CodeViewCompilerVersion [major=" + major + ", minor=" + minor + ", build=" + build + ", revision="
				+ revision + "]";
	}
}
