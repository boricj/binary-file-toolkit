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

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;
import net.boricj.bft.codeview.constants.CodeViewSymbolType;
import net.boricj.bft.codeview.symbols.sections.CodeViewSymbol;

/**
 * Represents a S_FRAMEPROC record.  These contain extra frame information for
 * a procedure and are encoded as the {@code FRAMEPROCSYM} structure in the
 * CodeView specification.
 */
public class CodeViewSymbolFrameProc extends CodeViewSymbol {
	public static class Builder {
		private int cbFrame;
		private int cbPad;
		private int padOffset;
		private int cbSaveRegs;
		private int offsetExceptionHandler;
		private short sectionIdExceptionHandler;
		private byte[] remainingData;

		public Builder() {}

		protected Builder(ByteInputStream bis) throws IOException {
			cbFrame = bis.readInt();
			cbPad = bis.readInt();
			padOffset = bis.readInt();
			cbSaveRegs = bis.readInt();
			offsetExceptionHandler = bis.readInt();
			sectionIdExceptionHandler = bis.readShort();
			remainingData = bis.readAllBytes();
		}

		public Builder setCbFrame(int cbFrame) {
			this.cbFrame = cbFrame;
			return this;
		}

		public Builder setCbPad(int cbPad) {
			this.cbPad = cbPad;
			return this;
		}

		public Builder setPadOffset(int padOffset) {
			this.padOffset = padOffset;
			return this;
		}

		public Builder setCbSaveRegs(int cbSaveRegs) {
			this.cbSaveRegs = cbSaveRegs;
			return this;
		}

		public Builder setOffsetExceptionHandler(int offsetExceptionHandler) {
			this.offsetExceptionHandler = offsetExceptionHandler;
			return this;
		}

		public Builder setSectionIdExceptionHandler(short sectionIdExceptionHandler) {
			this.sectionIdExceptionHandler = sectionIdExceptionHandler;
			return this;
		}

		public CodeViewSymbolFrameProc build() {
			return new CodeViewSymbolFrameProc(this);
		}
	}

	private final int cbFrame;
	private final int cbPad;
	private final int padOffset;
	private final int cbSaveRegs;
	private final int offsetExceptionHandler;
	private final short sectionIdExceptionHandler;
	private final byte[] remainingData;

	public CodeViewSymbolFrameProc(ByteInputStream bis) throws IOException {
		this(new Builder(bis));
	}

	protected CodeViewSymbolFrameProc(Builder builder) {
		super(CodeViewSymbolType.S_FRAMEPROC);

		this.cbFrame = builder.cbFrame;
		this.cbPad = builder.cbPad;
		this.padOffset = builder.padOffset;
		this.cbSaveRegs = builder.cbSaveRegs;
		this.offsetExceptionHandler = builder.offsetExceptionHandler;
		this.sectionIdExceptionHandler = builder.sectionIdExceptionHandler;
		this.remainingData = builder.remainingData;
	}

	public int getCbFrame() {
		return cbFrame;
	}

	public int getCbPad() {
		return cbPad;
	}

	public int getPadOffset() {
		return padOffset;
	}

	public int getCbSaveRegs() {
		return cbSaveRegs;
	}

	public int getOffsetExceptionHandler() {
		return offsetExceptionHandler;
	}

	public short getSectionIdExceptionHandler() {
		return sectionIdExceptionHandler;
	}

	@Override
	protected void write(ByteOutputStream bos) throws IOException {
		bos.writeInt(cbFrame);
		bos.writeInt(cbPad);
		bos.writeInt(padOffset);
		bos.writeInt(cbSaveRegs);
		bos.writeInt(offsetExceptionHandler);
		bos.writeShort(sectionIdExceptionHandler);
		bos.write(remainingData);
	}
}
