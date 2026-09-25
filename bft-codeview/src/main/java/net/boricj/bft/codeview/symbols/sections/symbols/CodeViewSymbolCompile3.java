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
package net.boricj.bft.codeview.symbols.sections.symbols;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.codeview.CodeViewCompilerVersion;
import net.boricj.bft.codeview.constants.CodeViewLanguage;
import net.boricj.bft.codeview.constants.CodeViewProcessor;
import net.boricj.bft.codeview.constants.CodeViewSymbolType;
import net.boricj.bft.codeview.symbols.sections.CodeViewSymbol;

/**
 * CodeView {@code S_COMPILE3} symbol carrying compiler identity and flags.
 */
public class CodeViewSymbolCompile3 extends CodeViewSymbol {
	/**
	 * Builder for {@link CodeViewSymbolCompile3}.
	 */
	public static class Builder {
		private CodeViewLanguage language;
		private CodeViewProcessor machine;
		private boolean hasEditAndContinue;
		private boolean hasNoDebugInfo;
		private boolean hasLTCG;
		private boolean hasNoDataAlign;
		private boolean hasManagedCode;
		private boolean hasSecurityChecks;
		private boolean hasHotPatch;
		private boolean isConvertedByCVTCIL;
		private boolean isMSILModule;
		private boolean hasSDL;
		private boolean hasPGO;
		private boolean hasEXP;
		private int padding;
		private CodeViewCompilerVersion frontendVersion;
		private CodeViewCompilerVersion backendVersion;
		private String version;

		/**
		 * Creates a builder with required language and processor values.
		 *
		 * @param language source language identifier
		 * @param machine processor identifier
		 */
		public Builder(CodeViewLanguage language, CodeViewProcessor machine) {
			this.language = language;
			this.machine = machine;
		}

		/**
		 * Parses a builder from an encoded {@code S_COMPILE3} payload.
		 *
		 * @param bis input stream positioned at the symbol payload
		 * @throws IOException if the payload cannot be parsed
		 */
		public Builder(ByteInputStream bis) throws IOException {
			// Read flags (4 bytes)
			int flags = bis.readInt();

			// Extract fields from flags word
			// Byte 0: language (bits 0-7)
			byte languageValue = (byte) (flags & 0xFF);
			language = CodeViewLanguage.valueFrom(languageValue);

			// Byte 1-2: flags (bits 8-23)
			byte flagsByte = (byte) ((flags >> 8) & 0xFF);
			hasEditAndContinue = (flagsByte & 0x01) != 0;
			hasNoDebugInfo = (flagsByte & 0x02) != 0;
			hasLTCG = (flagsByte & 0x04) != 0;
			hasNoDataAlign = (flagsByte & 0x08) != 0;
			hasManagedCode = (flagsByte & 0x10) != 0;
			hasSecurityChecks = (flagsByte & 0x20) != 0;
			hasHotPatch = (flagsByte & 0x40) != 0;
			isConvertedByCVTCIL = (flagsByte & 0x80) != 0;

			// Byte 3: more flags (bits 24-31)
			byte flagsByte2 = (byte) ((flags >> 24) & 0xFF);
			isMSILModule = (flagsByte2 & 0x01) != 0;
			hasSDL = (flagsByte2 & 0x02) != 0;
			hasPGO = (flagsByte2 & 0x04) != 0;
			hasEXP = (flagsByte2 & 0x08) != 0;
			padding = (flagsByte2 >> 4) & 0x0F;

			// Read processor (2 bytes, uint16)
			byte processorValue = (byte) (bis.readUnsignedShort() & 0xFF);
			machine = CodeViewProcessor.valueFrom(processorValue);

			// Read frontend version (4 shorts: major, minor, build, qfe)
			int feMajor = bis.readUnsignedShort();
			int feMinor = bis.readUnsignedShort();
			int feBuild = bis.readUnsignedShort();
			int feQfe = bis.readUnsignedShort();

			frontendVersion = new CodeViewCompilerVersion(feMajor, feMinor, feBuild, feQfe);

			// Read backend version (4 shorts: major, minor, build, qfe)
			int beMajor = bis.readUnsignedShort();
			int beMinor = bis.readUnsignedShort();
			int beBuild = bis.readUnsignedShort();
			int beQfe = bis.readUnsignedShort();

			backendVersion = new CodeViewCompilerVersion(beMajor, beMinor, beBuild, beQfe);

			version = bis.readNullTerminatedString(StandardCharsets.UTF_8);
		}

		/**
		 * Sets the source language identifier.
		 *
		 * @param language source language identifier
		 * @return this builder
		 */
		public Builder setLanguage(CodeViewLanguage language) {
			this.language = language;
			return this;
		}

		/**
		 * Sets the target processor identifier.
		 *
		 * @param machine processor identifier
		 * @return this builder
		 */
		public Builder setMachine(CodeViewProcessor machine) {
			this.machine = machine;
			return this;
		}

		/**
		 * Sets whether edit-and-continue support is enabled.
		 *
		 * @param hasEditAndContinue edit-and-continue flag value
		 * @return this builder
		 */
		public Builder setHasEditAndContinue(boolean hasEditAndContinue) {
			this.hasEditAndContinue = hasEditAndContinue;
			return this;
		}

		/**
		 * Sets whether debug info generation was disabled.
		 *
		 * @param hasNoDebugInfo no-debug-info flag value
		 * @return this builder
		 */
		public Builder setHasNoDebugInfo(boolean hasNoDebugInfo) {
			this.hasNoDebugInfo = hasNoDebugInfo;
			return this;
		}

		/**
		 * Sets whether link-time code generation was enabled.
		 *
		 * @param hasLTCG LTCG flag value
		 * @return this builder
		 */
		public Builder setHasLTCG(boolean hasLTCG) {
			this.hasLTCG = hasLTCG;
			return this;
		}

		/**
		 * Sets whether data alignment was disabled.
		 *
		 * @param hasNoDataAlign no-data-alignment flag value
		 * @return this builder
		 */
		public Builder setHasNoDataAlign(boolean hasNoDataAlign) {
			this.hasNoDataAlign = hasNoDataAlign;
			return this;
		}

		/**
		 * Sets whether managed code is present.
		 *
		 * @param hasManagedCode managed-code flag value
		 * @return this builder
		 */
		public Builder setHasManagedCode(boolean hasManagedCode) {
			this.hasManagedCode = hasManagedCode;
			return this;
		}

		/**
		 * Sets whether security checks were enabled.
		 *
		 * @param hasSecurityChecks security-check flag value
		 * @return this builder
		 */
		public Builder setHasSecurityChecks(boolean hasSecurityChecks) {
			this.hasSecurityChecks = hasSecurityChecks;
			return this;
		}

		/**
		 * Sets whether hot patching was enabled.
		 *
		 * @param hasHotPatch hot-patch flag value
		 * @return this builder
		 */
		public Builder setHasHotPatch(boolean hasHotPatch) {
			this.hasHotPatch = hasHotPatch;
			return this;
		}

		/**
		 * Sets whether conversion by CVTCIL occurred.
		 *
		 * @param isConvertedByCVTCIL conversion flag value
		 * @return this builder
		 */
		public Builder setIsConvertedByCVTCIL(boolean isConvertedByCVTCIL) {
			this.isConvertedByCVTCIL = isConvertedByCVTCIL;
			return this;
		}

		/**
		 * Sets whether this module is MSIL.
		 *
		 * @param isMSILModule MSIL-module flag value
		 * @return this builder
		 */
		public Builder setIsMSILModule(boolean isMSILModule) {
			this.isMSILModule = isMSILModule;
			return this;
		}

		/**
		 * Sets whether SDL checks were enabled.
		 *
		 * @param hasSDL SDL flag value
		 * @return this builder
		 */
		public Builder setHasSDL(boolean hasSDL) {
			this.hasSDL = hasSDL;
			return this;
		}

		/**
		 * Sets whether profile-guided optimization was enabled.
		 *
		 * @param hasPGO PGO flag value
		 * @return this builder
		 */
		public Builder setHasPGO(boolean hasPGO) {
			this.hasPGO = hasPGO;
			return this;
		}

		/**
		 * Sets whether EXP metadata is present.
		 *
		 * @param hasEXP EXP flag value
		 * @return this builder
		 */
		public Builder setHasEXP(boolean hasEXP) {
			this.hasEXP = hasEXP;
			return this;
		}

		/**
		 * Sets the padding nibble stored in the flags field.
		 *
		 * @param padding raw 4-bit padding value
		 * @return this builder
		 */
		public Builder setPadding(int padding) {
			this.padding = padding;
			return this;
		}

		/**
		 * Sets frontend compiler version.
		 *
		 * @param frontendVersion frontend compiler version
		 * @return this builder
		 */
		public Builder setFrontendVersion(CodeViewCompilerVersion frontendVersion) {
			this.frontendVersion = frontendVersion;
			return this;
		}

		/**
		 * Sets backend compiler version.
		 *
		 * @param backendVersion backend compiler version
		 * @return this builder
		 */
		public Builder setBackendVersion(CodeViewCompilerVersion backendVersion) {
			this.backendVersion = backendVersion;
			return this;
		}

		/**
		 * Sets free-form compiler version text.
		 *
		 * @param version null-terminated version string
		 * @return this builder
		 */
		public Builder setVersion(String version) {
			this.version = version;
			return this;
		}

		/**
		 * Builds a symbol instance from current builder state.
		 *
		 * @return built {@link CodeViewSymbolCompile3}
		 */
		public CodeViewSymbolCompile3 build() {
			return new CodeViewSymbolCompile3(this);
		}
	}

	private final CodeViewLanguage language;
	private final CodeViewProcessor machine;
	private final boolean hasEditAndContinue;
	private final boolean hasNoDebugInfo;
	private final boolean hasLTCG;
	private final boolean hasNoDataAlign;
	private final boolean hasManagedCode;
	private final boolean hasSecurityChecks;
	private final boolean hasHotPatch;
	private final boolean isConvertedByCVTCIL;
	private final boolean isMSILModule;
	private final boolean hasSDL;
	private final boolean hasPGO;
	private final boolean hasEXP;
	private final int padding;
	private final CodeViewCompilerVersion frontendVersion;
	private final CodeViewCompilerVersion backendVersion;
	private final String version;

	/**
	 * Parses a compile3 symbol payload.
	 *
	 * @param bis input stream positioned at the symbol payload
	 * @throws IOException if parsing fails
	 */
	public CodeViewSymbolCompile3(ByteInputStream bis) throws IOException {
		this(new Builder(bis));
	}

	/**
	 * Creates a compile3 symbol from a builder.
	 *
	 * @param builder source builder
	 */
	protected CodeViewSymbolCompile3(Builder builder) {
		super(CodeViewSymbolType.S_COMPILE3);

		this.language = builder.language;
		this.machine = builder.machine;
		this.hasEditAndContinue = builder.hasEditAndContinue;
		this.hasNoDebugInfo = builder.hasNoDebugInfo;
		this.hasLTCG = builder.hasLTCG;
		this.hasNoDataAlign = builder.hasNoDataAlign;
		this.hasManagedCode = builder.hasManagedCode;
		this.hasSecurityChecks = builder.hasSecurityChecks;
		this.hasHotPatch = builder.hasHotPatch;
		this.isConvertedByCVTCIL = builder.isConvertedByCVTCIL;
		this.isMSILModule = builder.isMSILModule;
		this.hasSDL = builder.hasSDL;
		this.hasPGO = builder.hasPGO;
		this.hasEXP = builder.hasEXP;
		this.padding = builder.padding;
		this.frontendVersion = builder.frontendVersion;
		this.backendVersion = builder.backendVersion;
		this.version = builder.version;
	}

	/**
	 * Returns source language identifier.
	 *
	 * @return source language identifier
	 */
	public CodeViewLanguage getLanguage() {
		return language;
	}

	/**
	 * Returns processor identifier.
	 *
	 * @return processor identifier
	 */
	public CodeViewProcessor getMachine() {
		return machine;
	}

	/**
	 * Returns whether edit-and-continue is enabled.
	 *
	 * @return {@code true} if edit-and-continue is enabled
	 */
	public boolean hasEditAndContinue() {
		return hasEditAndContinue;
	}

	/**
	 * Returns whether debug-info generation was disabled.
	 *
	 * @return {@code true} if debug info generation was disabled
	 */
	public boolean hasNoDebugInfo() {
		return hasNoDebugInfo;
	}

	/**
	 * Returns whether link-time code generation was enabled.
	 *
	 * @return {@code true} if LTCG was enabled
	 */
	public boolean hasLTCG() {
		return hasLTCG;
	}

	/**
	 * Returns whether data alignment was disabled.
	 *
	 * @return {@code true} if data alignment was disabled
	 */
	public boolean hasNoDataAlign() {
		return hasNoDataAlign;
	}

	/**
	 * Returns whether managed code is present.
	 *
	 * @return {@code true} if managed code is present
	 */
	public boolean hasManagedCode() {
		return hasManagedCode;
	}

	/**
	 * Returns whether security checks were enabled.
	 *
	 * @return {@code true} if security checks were enabled
	 */
	public boolean hasSecurityChecks() {
		return hasSecurityChecks;
	}

	/**
	 * Returns whether hot-patch support is enabled.
	 *
	 * @return {@code true} if hot patch support is enabled
	 */
	public boolean hasHotPatch() {
		return hasHotPatch;
	}

	/**
	 * Returns whether conversion by CVTCIL occurred.
	 *
	 * @return {@code true} if converted by CVTCIL
	 */
	public boolean isConvertedByCVTCIL() {
		return isConvertedByCVTCIL;
	}

	/**
	 * Returns whether this module is marked as MSIL.
	 *
	 * @return {@code true} if this module is marked as MSIL
	 */
	public boolean isMSILModule() {
		return isMSILModule;
	}

	/**
	 * Returns whether SDL checks are enabled.
	 *
	 * @return {@code true} if SDL checks are enabled
	 */
	public boolean hasSDL() {
		return hasSDL;
	}

	/**
	 * Returns whether profile-guided optimization is enabled.
	 *
	 * @return {@code true} if profile-guided optimization is enabled
	 */
	public boolean hasPGO() {
		return hasPGO;
	}

	/**
	 * Returns whether EXP metadata is present.
	 *
	 * @return {@code true} if EXP metadata is present
	 */
	public boolean hasEXP() {
		return hasEXP;
	}

	/**
	 * Returns raw high-nibble padding value from the flags word.
	 *
	 * @return raw padding nibble
	 */
	public int getPadding() {
		return padding;
	}

	/**
	 * Returns frontend compiler version.
	 *
	 * @return frontend compiler version
	 */
	public CodeViewCompilerVersion getFrontendVersion() {
		return frontendVersion;
	}

	/**
	 * Returns backend compiler version.
	 *
	 * @return backend compiler version
	 */
	public CodeViewCompilerVersion getBackendVersion() {
		return backendVersion;
	}

	/**
	 * Returns compiler version string.
	 *
	 * @return null-terminated compiler version string without terminator
	 */
	public String getVersion() {
		return version;
	}

	@Override
	protected void write(ByteOutputStream bos) throws IOException {
		// Encode flags into 4-byte word
		int flagsByte1 = (hasEditAndContinue ? 0x01 : 0)
				| (hasNoDebugInfo ? 0x02 : 0)
				| (hasLTCG ? 0x04 : 0)
				| (hasNoDataAlign ? 0x08 : 0)
				| (hasManagedCode ? 0x10 : 0)
				| (hasSecurityChecks ? 0x20 : 0)
				| (hasHotPatch ? 0x40 : 0)
				| (isConvertedByCVTCIL ? 0x80 : 0);

		int flagsByte2 = (isMSILModule ? 0x01 : 0)
				| (hasSDL ? 0x02 : 0)
				| (hasPGO ? 0x04 : 0)
				| (hasEXP ? 0x08 : 0)
				| ((padding & 0x0F) << 4);

		int flags = (language.getValue() & 0xFF) | ((flagsByte1 & 0xFF) << 8) | ((flagsByte2 & 0xFF) << 24);

		bos.writeInt(flags);
		bos.writeShort(machine.getValue());

		// Frontend version
		bos.writeShort(frontendVersion.getMajor());
		bos.writeShort(frontendVersion.getMinor());
		bos.writeShort(frontendVersion.getBuild());
		bos.writeShort(frontendVersion.getRevision());

		// Backend version
		bos.writeShort(backendVersion.getMajor());
		bos.writeShort(backendVersion.getMinor());
		bos.writeShort(backendVersion.getBuild());
		bos.writeShort(backendVersion.getRevision());

		bos.writeNullTerminatedString(version, StandardCharsets.UTF_8);
	}
}
