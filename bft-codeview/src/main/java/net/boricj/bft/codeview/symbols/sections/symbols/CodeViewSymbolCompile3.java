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

public class CodeViewSymbolCompile3 extends CodeViewSymbol {
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

		public Builder(CodeViewLanguage language, CodeViewProcessor machine) {
			this.language = language;
			this.machine = machine;
		}

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

		public Builder setLanguage(CodeViewLanguage language) {
			this.language = language;
			return this;
		}

		public Builder setMachine(CodeViewProcessor machine) {
			this.machine = machine;
			return this;
		}

		public Builder setHasEditAndContinue(boolean hasEditAndContinue) {
			this.hasEditAndContinue = hasEditAndContinue;
			return this;
		}

		public Builder setHasNoDebugInfo(boolean hasNoDebugInfo) {
			this.hasNoDebugInfo = hasNoDebugInfo;
			return this;
		}

		public Builder setHasLTCG(boolean hasLTCG) {
			this.hasLTCG = hasLTCG;
			return this;
		}

		public Builder setHasNoDataAlign(boolean hasNoDataAlign) {
			this.hasNoDataAlign = hasNoDataAlign;
			return this;
		}

		public Builder setHasManagedCode(boolean hasManagedCode) {
			this.hasManagedCode = hasManagedCode;
			return this;
		}

		public Builder setHasSecurityChecks(boolean hasSecurityChecks) {
			this.hasSecurityChecks = hasSecurityChecks;
			return this;
		}

		public Builder setHasHotPatch(boolean hasHotPatch) {
			this.hasHotPatch = hasHotPatch;
			return this;
		}

		public Builder setIsConvertedByCVTCIL(boolean isConvertedByCVTCIL) {
			this.isConvertedByCVTCIL = isConvertedByCVTCIL;
			return this;
		}

		public Builder setIsMSILModule(boolean isMSILModule) {
			this.isMSILModule = isMSILModule;
			return this;
		}

		public Builder setHasSDL(boolean hasSDL) {
			this.hasSDL = hasSDL;
			return this;
		}

		public Builder setHasPGO(boolean hasPGO) {
			this.hasPGO = hasPGO;
			return this;
		}

		public Builder setHasEXP(boolean hasEXP) {
			this.hasEXP = hasEXP;
			return this;
		}

		public Builder setPadding(int padding) {
			this.padding = padding;
			return this;
		}

		public Builder setFrontendVersion(CodeViewCompilerVersion frontendVersion) {
			this.frontendVersion = frontendVersion;
			return this;
		}

		public Builder setBackendVersion(CodeViewCompilerVersion backendVersion) {
			this.backendVersion = backendVersion;
			return this;
		}

		public Builder setVersion(String version) {
			this.version = version;
			return this;
		}

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

	public CodeViewSymbolCompile3(ByteInputStream bis) throws IOException {
		this(new Builder(bis));
	}

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

	public CodeViewLanguage getLanguage() {
		return language;
	}

	public CodeViewProcessor getMachine() {
		return machine;
	}

	public boolean hasEditAndContinue() {
		return hasEditAndContinue;
	}

	public boolean hasNoDebugInfo() {
		return hasNoDebugInfo;
	}

	public boolean hasLTCG() {
		return hasLTCG;
	}

	public boolean hasNoDataAlign() {
		return hasNoDataAlign;
	}

	public boolean hasManagedCode() {
		return hasManagedCode;
	}

	public boolean hasSecurityChecks() {
		return hasSecurityChecks;
	}

	public boolean hasHotPatch() {
		return hasHotPatch;
	}

	public boolean isConvertedByCVTCIL() {
		return isConvertedByCVTCIL;
	}

	public boolean isMSILModule() {
		return isMSILModule;
	}

	public boolean hasSDL() {
		return hasSDL;
	}

	public boolean hasPGO() {
		return hasPGO;
	}

	public boolean hasEXP() {
		return hasEXP;
	}

	public int getPadding() {
		return padding;
	}

	public CodeViewCompilerVersion getFrontendVersion() {
		return frontendVersion;
	}

	public CodeViewCompilerVersion getBackendVersion() {
		return backendVersion;
	}

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
