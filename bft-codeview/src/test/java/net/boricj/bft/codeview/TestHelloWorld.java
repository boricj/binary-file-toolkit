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

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

import net.boricj.bft.TestUtils;
import net.boricj.bft.codeview.constants.CodeViewLanguage;
import net.boricj.bft.codeview.constants.CodeViewProcessor;
import net.boricj.bft.codeview.constants.CodeViewSectionNames;
import net.boricj.bft.codeview.constants.CodeViewSignature;
import net.boricj.bft.codeview.constants.CodeViewSymbolTableSectionType;
import net.boricj.bft.codeview.constants.CodeViewSymbolType;
import net.boricj.bft.codeview.symbols.CodeViewSymbolTableSection;
import net.boricj.bft.codeview.symbols.sections.CodeViewSymbol;
import net.boricj.bft.codeview.symbols.sections.CodeViewSymbolTableFileChecksumsSection;
import net.boricj.bft.codeview.symbols.sections.CodeViewSymbolTableFrameDataSection;
import net.boricj.bft.codeview.symbols.sections.CodeViewSymbolTableLinesSection;
import net.boricj.bft.codeview.symbols.sections.CodeViewSymbolTableStringTableSection;
import net.boricj.bft.codeview.symbols.sections.CodeViewSymbolTableSymbolSection;
import net.boricj.bft.codeview.symbols.sections.frame.CodeViewFrameData;
import net.boricj.bft.codeview.symbols.sections.lines.CodeViewFileBlock;
import net.boricj.bft.codeview.symbols.sections.symbols.CodeViewSymbolBpRel32;
import net.boricj.bft.codeview.symbols.sections.symbols.CodeViewSymbolBuildInfo;
import net.boricj.bft.codeview.symbols.sections.symbols.CodeViewSymbolCompile3;
import net.boricj.bft.codeview.symbols.sections.symbols.CodeViewSymbolFrameProc;
import net.boricj.bft.codeview.symbols.sections.symbols.CodeViewSymbolObjname;
import net.boricj.bft.codeview.symbols.sections.symbols.CodeViewSymbolProcSym32;
import net.boricj.bft.codeview.tables.CodeViewSymbolTable;
import net.boricj.bft.codeview.tables.CodeViewTypeTable;
import net.boricj.bft.codeview.types.CodeViewTypeRecord;
import net.boricj.bft.coff.CoffFile;
import net.boricj.bft.coff.CoffSection;
import net.boricj.bft.coff.sections.CoffBytes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestHelloWorld {
	@Test
	public void test_hello_world_i686_pc_windows_msvc() throws Exception {
		CoffBytes s_debug_S;
		CoffBytes s_debug_T;

		File file = new File(getClass()
				.getResource("hello-world_i686-pc-windows-msvc.z7.obj")
				.toURI());

		try (FileInputStream fis = new FileInputStream(file)) {
			CoffFile coff = new CoffFile.Parser(fis).parse();

			s_debug_S = (CoffBytes)
					TestUtils.findBy(coff.getSections(), CoffSection::getName, CodeViewSectionNames._DEBUG_S);
			s_debug_T = (CoffBytes)
					TestUtils.findBy(coff.getSections(), CoffSection::getName, CodeViewSectionNames._DEBUG_T);
		}

		CodeViewFile codeViewFile = new CodeViewFile.Parser()
				.addSection(CodeViewSectionNames._DEBUG_S, s_debug_S.getBytes())
				.addSection(CodeViewSectionNames._DEBUG_T, s_debug_T.getBytes())
				.parse();

		assertEquals(2, codeViewFile.getElements().size());

		{
			CodeViewSymbolTable cv_debug_S = (CodeViewSymbolTable)
					TestUtils.findBy(codeViewFile, CodeViewTable::getName, CodeViewSectionNames._DEBUG_S);

			assertEquals(CodeViewSectionNames._DEBUG_S, cv_debug_S.getName());
			assertEquals(CodeViewSignature.CV_SIGNATURE_C13, cv_debug_S.getSignature());

			Iterator<CodeViewSymbolTableSection> sectionIt = cv_debug_S.iterator();

			{
				CodeViewSymbolTableSymbolSection symbols = (CodeViewSymbolTableSymbolSection) sectionIt.next();
				assertEquals(CodeViewSymbolTableSectionType.DEBUG_S_SYMBOLS, symbols.getType());

				Iterator<CodeViewSymbol> symbolsIt = symbols.iterator();
				CodeViewSymbol symbol;

				{
					symbol = symbolsIt.next();
					assertEquals(CodeViewSymbolType.S_OBJNAME, symbol.getType());

					CodeViewSymbolObjname objname = (CodeViewSymbolObjname) symbol;

					assertEquals(0x00000000, objname.getSignature());
					assertEquals(
							"C:\\Users\\jblbe\\Documents\\Workspace\\binary-file-toolkit\\bft-coff\\src\\test\\resources\\net\\boricj\\bft\\coff\\hello-world_i686-pc-windows-msvc.z7.obj",
							objname.getName());
				}
				{
					symbol = symbolsIt.next();
					assertEquals(CodeViewSymbolType.S_COMPILE3, symbol.getType());

					CodeViewSymbolCompile3 compile3 = (CodeViewSymbolCompile3) symbol;

					assertEquals(CodeViewLanguage.CV_CFL_C, compile3.getLanguage());
					assertEquals(CodeViewProcessor.CV_CFL_PENTIUMIII, compile3.getMachine());
					assertFalse(compile3.hasEditAndContinue());
					assertFalse(compile3.hasNoDebugInfo());
					assertFalse(compile3.hasLTCG());
					assertFalse(compile3.hasNoDataAlign());
					assertFalse(compile3.hasManagedCode());
					assertTrue(compile3.hasSecurityChecks());
					assertFalse(compile3.hasHotPatch());
					assertFalse(compile3.isConvertedByCVTCIL());
					assertFalse(compile3.isMSILModule());
					assertFalse(compile3.hasSDL());
					assertFalse(compile3.hasPGO());
					assertFalse(compile3.hasEXP());
					assertEquals(0, compile3.getPadding());
					assertEquals(new CodeViewCompilerVersion(19, 43, 34808, 0), compile3.getFrontendVersion());
					assertEquals(new CodeViewCompilerVersion(19, 43, 34808, 0), compile3.getBackendVersion());
					assertEquals("Microsoft (R) Optimizing Compiler", compile3.getVersion());
				}

				assertFalse(symbolsIt.hasNext());
			}
			{
				CodeViewSymbolTableFrameDataSection frameData = (CodeViewSymbolTableFrameDataSection) sectionIt.next();
				assertEquals(CodeViewSymbolTableSectionType.DEBUG_S_FRAMEDATA, frameData.getType());

				Iterator<CodeViewFrameData> frameDataIt = frameData.iterator();

				{
					CodeViewFrameData fd = (CodeViewFrameData) frameDataIt.next();

					assertEquals(0x00000000, fd.getRvaStart());
					assertEquals(0x14, fd.getCbBlock());
					assertEquals(0x0, fd.getCbLocals());
					assertEquals(0x8, fd.getCbParams());
					assertEquals(0x0, fd.getCbStkMax());
					assertEquals(0x00000076, fd.getFrameFunc());
					assertEquals(0x3, fd.getCbProlog());
					assertEquals(0x0, fd.getCbSavedRegs());
					assertFalse(fd.hasSEH());
					assertFalse(fd.hasEH());
					assertTrue(fd.isFunctionStart());
				}

				assertFalse(frameDataIt.hasNext());
			}
			{
				CodeViewSymbolTableSymbolSection symbols = (CodeViewSymbolTableSymbolSection) sectionIt.next();
				assertEquals(CodeViewSymbolTableSectionType.DEBUG_S_SYMBOLS, symbols.getType());

				Iterator<CodeViewSymbol> symbolsIt = symbols.iterator();
				CodeViewSymbol symbol;

				{
					symbol = symbolsIt.next();
					assertEquals(CodeViewSymbolType.S_GPROC32_ID, symbol.getType());

					CodeViewSymbolProcSym32 procSym32 = (CodeViewSymbolProcSym32) symbol;

					assertEquals(0x00000000, procSym32.getParentPointer());
					assertEquals(0x00000000, procSym32.getEndPointer());
					assertEquals(0x00000000, procSym32.getNextPointer());
					assertEquals(0x00000014, procSym32.getLength());
					assertEquals(0x00000003, procSym32.getDebugStart());
					assertEquals(0x00000012, procSym32.getDebugEnd());
					assertEquals(0x00001003, procSym32.getTypeIndex());
					assertEquals(0x00000000, procSym32.getOffset());
					assertEquals(0x0000, procSym32.getSegment());
				}
				{
					symbol = symbolsIt.next();
					assertEquals(CodeViewSymbolType.S_FRAMEPROC, symbol.getType());

					CodeViewSymbolFrameProc frameProc = (CodeViewSymbolFrameProc) symbol;

					assertEquals(0x00000000, frameProc.getCbFrame());
					assertEquals(0x00000000, frameProc.getCbPad());
					assertEquals(0x00000000, frameProc.getPadOffset());
					assertEquals(0x00000000, frameProc.getCbSaveRegs());
					assertEquals(0x00000000, frameProc.getCbSaveRegs());
					assertEquals(0x00000000, frameProc.getOffsetExceptionHandler());
					assertEquals(0x00000000, frameProc.getSectionIdExceptionHandler());
				}
				{
					symbol = symbolsIt.next();
					assertEquals(CodeViewSymbolType.S_BPREL32, symbol.getType());

					CodeViewSymbolBpRel32 bpRel32 = (CodeViewSymbolBpRel32) symbol;

					assertEquals(0x00000008, bpRel32.getBpOffset());
					assertEquals(0x00000074, bpRel32.getTypeIndex());
					assertEquals("argc", bpRel32.getName());
				}
				{
					symbol = symbolsIt.next();
					assertEquals(CodeViewSymbolType.S_BPREL32, symbol.getType());

					CodeViewSymbolBpRel32 bpRel32 = (CodeViewSymbolBpRel32) symbol;

					assertEquals(0x0000000c, bpRel32.getBpOffset());
					assertEquals(0x00001000, bpRel32.getTypeIndex());
					assertEquals("argv", bpRel32.getName());
				}
				{
					symbol = symbolsIt.next();
					assertEquals(CodeViewSymbolType.S_PROC_ID_END, symbol.getType());
				}

				assertFalse(symbolsIt.hasNext());
			}
			{
				CodeViewSymbolTableLinesSection lines = (CodeViewSymbolTableLinesSection) sectionIt.next();
				assertEquals(CodeViewSymbolTableSectionType.DEBUG_S_LINES, lines.getType());
				assertTrue(lines.getFileBlocks().size() > 0);
				assertEquals(0, lines.getOffsetCon());
				CodeViewFileBlock fb = lines.getFileBlocks().get(0);
				assertTrue(fb.getLines().size() > 0);
			}
			{
				CodeViewSymbolTableFileChecksumsSection chks =
						(CodeViewSymbolTableFileChecksumsSection) sectionIt.next();
				assertEquals(CodeViewSymbolTableSectionType.DEBUG_S_FILECHKSMS, chks.getType());
				assertTrue(chks.getRecords().size() > 0);
				CodeViewSymbolTableFileChecksumsSection.FileChecksum rc =
						chks.getRecords().get(0);
				assertTrue(rc.getCbChecksum() >= 0);
			}
			{
				CodeViewSymbolTableStringTableSection st = (CodeViewSymbolTableStringTableSection) sectionIt.next();
				assertEquals(CodeViewSymbolTableSectionType.DEBUG_S_STRINGTABLE, st.getType());
				assertTrue(st.getStringsByOffset().size() > 0);
				String firstStr = st.getStringAtOffset(0);
				assertNotNull(firstStr);
			}
			{
				CodeViewSymbolTableSymbolSection symbols = (CodeViewSymbolTableSymbolSection) sectionIt.next();
				assertEquals(CodeViewSymbolTableSectionType.DEBUG_S_SYMBOLS, symbols.getType());

				Iterator<CodeViewSymbol> symbolsIt = symbols.iterator();
				CodeViewSymbol symbol;

				{
					symbol = symbolsIt.next();
					assertEquals(CodeViewSymbolType.S_BUILDINFO, symbol.getType());

					CodeViewSymbolBuildInfo buildInfo = (CodeViewSymbolBuildInfo) symbol;
					assertEquals(0x0000100d, buildInfo.getTypeIndex());
				}
			}

			assertFalse(sectionIt.hasNext());
		}
		{
			CodeViewTable cv_debug_T =
					TestUtils.findBy(codeViewFile, CodeViewTable::getName, CodeViewSectionNames._DEBUG_T);

			assertEquals(CodeViewSectionNames._DEBUG_T, cv_debug_T.getName());
			assertEquals(CodeViewSignature.CV_SIGNATURE_C13, cv_debug_T.getSignature());

			// Validate parsed type table
			CodeViewTypeTable tt = (CodeViewTypeTable) cv_debug_T;
			assertTrue(tt.getElements().size() > 0);
			CodeViewTypeRecord rec0 = tt.getElements().get(0);
			assertNotNull(rec0);
		}
	}
}
