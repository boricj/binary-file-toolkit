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

	public int getRvaStart() {
		return rvaStart;
	}

	public int getCbBlock() {
		return cbBlock;
	}

	public int getCbLocals() {
		return cbLocals;
	}

	public int getCbParams() {
		return cbParams;
	}

	public int getCbStkMax() {
		return cbStkMax;
	}

	public short getCbProlog() {
		return cbProlog;
	}

	public short getCbSavedRegs() {
		return cbSavedRegs;
	}

	public int getFrameFunc() {
		return frameFunc;
	}

	public boolean hasSEH() {
		return (flags & 0x1) != 0;
	}

	public boolean hasEH() {
		return (flags & 0x2) != 0;
	}

	public boolean isFunctionStart() {
		return (flags & 0x4) != 0;
	}

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
