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
package net.boricj.bft.codeview.symbols.sections.frame;

import java.io.IOException;

import net.boricj.bft.ByteInputStream;
import net.boricj.bft.ByteOutputStream;

/**
 * Decoded frame descriptor entry from a CodeView frame data subsection.
 */
public class CodeViewFrameData {
	private final int rvaStart;
	private final int cbBlock;
	private final int cbLocals;
	private final int cbParams;
	private final int cbStkMax;
	private final int frameFunc;
	private final short cbProlog;
	private final short cbSavedRegs;
	private final int flags;

	/**
	 * Parses one frame data entry.
	 *
	 * @param bis input stream positioned at the start of a frame data entry
	 * @throws IOException if the entry cannot be read
	 */
	public CodeViewFrameData(ByteInputStream bis) throws IOException {
		this.rvaStart = bis.readInt();
		this.cbBlock = bis.readInt();
		this.cbLocals = bis.readInt();
		this.cbParams = bis.readInt();
		this.cbStkMax = bis.readInt();
		this.frameFunc = bis.readInt();
		this.cbProlog = bis.readShort();
		this.cbSavedRegs = bis.readShort();
		this.flags = bis.readInt();
	}

	/**
	 * Returns the start RVA for the associated function body.
	 *
	 * @return start RVA
	 */
	public int getRvaStart() {
		return rvaStart;
	}

	/**
	 * Returns the code size covered by this frame record.
	 *
	 * @return code block size in bytes
	 */
	public int getCbBlock() {
		return cbBlock;
	}

	/**
	 * Returns local-variable stack storage size.
	 *
	 * @return local stack storage size in bytes
	 */
	public int getCbLocals() {
		return cbLocals;
	}

	/**
	 * Returns parameter stack storage size.
	 *
	 * @return parameter storage size in bytes
	 */
	public int getCbParams() {
		return cbParams;
	}

	/**
	 * Returns maximum stack size used by the function.
	 *
	 * @return maximum stack size in bytes
	 */
	public int getCbStkMax() {
		return cbStkMax;
	}

	/**
	 * Returns prolog size.
	 *
	 * @return prolog size in bytes
	 */
	public short getCbProlog() {
		return cbProlog;
	}

	/**
	 * Returns bytes used to save callee-preserved registers.
	 *
	 * @return saved-register area size in bytes
	 */
	public short getCbSavedRegs() {
		return cbSavedRegs;
	}

	/**
	 * Returns pointer to the frame procedure description.
	 *
	 * @return offset or token of the frame procedure descriptor
	 */
	public int getFrameFunc() {
		return frameFunc;
	}

	/**
	 * Returns whether structured exception handling data is present.
	 *
	 * @return {@code true} when SEH metadata is present
	 */
	public boolean hasSEH() {
		return (flags & 0x1) != 0;
	}

	/**
	 * Returns whether C++ exception handling data is present.
	 *
	 * @return {@code true} when EH metadata is present
	 */
	public boolean hasEH() {
		return (flags & 0x2) != 0;
	}

	/**
	 * Returns whether this record marks a function entry point.
	 *
	 * @return {@code true} when the function-start flag is set
	 */
	public boolean isFunctionStart() {
		return (flags & 0x4) != 0;
	}

	/**
	 * Writes this frame record to an output stream.
	 *
	 * @param bos output stream receiving the encoded frame record
	 * @throws IOException if the record cannot be written
	 */
	public void write(ByteOutputStream bos) throws IOException {
		bos.writeInt(rvaStart);
		bos.writeInt(cbBlock);
		bos.writeInt(cbLocals);
		bos.writeInt(cbParams);
		bos.writeInt(cbStkMax);
		bos.writeInt(frameFunc);
		bos.writeShort(cbProlog);
		bos.writeShort(cbSavedRegs);
		bos.writeInt(flags);
	}
}
