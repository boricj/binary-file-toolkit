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
	/**
	 * Builder for {@link CodeViewSymbolFrameProc}.
	 */
	public static class Builder {
		private int cbFrame;
		private int cbPad;
		private int padOffset;
		private int cbSaveRegs;
		private int offsetExceptionHandler;
		private short sectionIdExceptionHandler;
		private byte[] remainingData;

		/** Creates an empty builder. */
		public Builder() {}

		/**
		 * Parses builder values from an encoded frame-procedure payload.
		 *
		 * @param bis input stream positioned at the frame-procedure payload
		 * @throws IOException if parsing fails
		 */
		protected Builder(ByteInputStream bis) throws IOException {
			cbFrame = bis.readInt();
			cbPad = bis.readInt();
			padOffset = bis.readInt();
			cbSaveRegs = bis.readInt();
			offsetExceptionHandler = bis.readInt();
			sectionIdExceptionHandler = bis.readShort();
			remainingData = bis.readAllBytes();
		}

		/**
		 * Sets total stack frame size.
		 *
		 * @param cbFrame total stack frame size in bytes
		 * @return this builder
		 */
		public Builder setCbFrame(int cbFrame) {
			this.cbFrame = cbFrame;
			return this;
		}

		/**
		 * Sets stack padding size.
		 *
		 * @param cbPad stack padding size in bytes
		 * @return this builder
		 */
		public Builder setCbPad(int cbPad) {
			this.cbPad = cbPad;
			return this;
		}

		/**
		 * Sets stack padding offset.
		 *
		 * @param padOffset padding offset in bytes
		 * @return this builder
		 */
		public Builder setPadOffset(int padOffset) {
			this.padOffset = padOffset;
			return this;
		}

		/**
		 * Sets saved-register area size.
		 *
		 * @param cbSaveRegs saved-register area size in bytes
		 * @return this builder
		 */
		public Builder setCbSaveRegs(int cbSaveRegs) {
			this.cbSaveRegs = cbSaveRegs;
			return this;
		}

		/**
		 * Sets exception handler offset.
		 *
		 * @param offsetExceptionHandler exception handler offset
		 * @return this builder
		 */
		public Builder setOffsetExceptionHandler(int offsetExceptionHandler) {
			this.offsetExceptionHandler = offsetExceptionHandler;
			return this;
		}

		/**
		 * Sets exception handler section identifier.
		 *
		 * @param sectionIdExceptionHandler exception handler section index
		 * @return this builder
		 */
		public Builder setSectionIdExceptionHandler(short sectionIdExceptionHandler) {
			this.sectionIdExceptionHandler = sectionIdExceptionHandler;
			return this;
		}

		/**
		 * Builds a frame-procedure symbol.
		 *
		 * @return built frame-procedure symbol
		 */
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

	/**
	 * Parses an {@code S_FRAMEPROC} payload.
	 *
	 * @param bis input stream positioned at the payload start
	 * @throws IOException if parsing fails
	 */
	public CodeViewSymbolFrameProc(ByteInputStream bis) throws IOException {
		this(new Builder(bis));
	}

	/**
	 * Creates a frame-procedure symbol from builder state.
	 *
	 * @param builder source builder
	 */
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

	/**
	 * Returns total stack frame size.
	 *
	 * @return total stack frame size in bytes
	 */
	public int getCbFrame() {
		return cbFrame;
	}

	/**
	 * Returns stack padding size.
	 *
	 * @return stack padding size in bytes
	 */
	public int getCbPad() {
		return cbPad;
	}

	/**
	 * Returns stack padding offset.
	 *
	 * @return stack padding offset in bytes
	 */
	public int getPadOffset() {
		return padOffset;
	}

	/**
	 * Returns saved-register area size.
	 *
	 * @return saved-register area size in bytes
	 */
	public int getCbSaveRegs() {
		return cbSaveRegs;
	}

	/**
	 * Returns exception handler offset.
	 *
	 * @return exception handler offset
	 */
	public int getOffsetExceptionHandler() {
		return offsetExceptionHandler;
	}

	/**
	 * Returns section index containing the exception handler.
	 *
	 * @return section index containing the exception handler
	 */
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
