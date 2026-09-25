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
package net.boricj.bft.codeview.constants;

/**
 * CodeView symbol record kind identifiers.
 */
public enum CodeViewSymbolType {
	/** Compile flags symbol. */
	S_COMPILE((short) 0x0001),
	/** Register variable. */
	S_REGISTER_16t((short) 0x0002),
	/** constant symbol. */
	S_CONSTANT_16t((short) 0x0003),
	/** User defined type. */
	S_UDT_16t((short) 0x0004),
	/** Start Search. */
	S_SSEARCH((short) 0x0005),
	/** Block, procedure, "with" or thunk end. */
	S_END((short) 0x0006),
	/** Reserve symbol space in $$Symbols table. */
	S_SKIP((short) 0x0007),
	/** Reserved symbol for CV internal use. */
	S_CVRESERVE((short) 0x0008),
	/** path to object file name. */
	S_OBJNAME_ST((short) 0x0009),
	/** end of argument/return list. */
	S_ENDARG((short) 0x000a),
	/** special UDT for cobol that does not symbol pack. */
	S_COBOLUDT_16t((short) 0x000b),
	/** multiple register variable. */
	S_MANYREG_16t((short) 0x000c),
	/** return description symbol. */
	S_RETURN((short) 0x000d),
	/** description of this pointer on entry. */
	S_ENTRYTHIS((short) 0x000e),

	/** BP-relative. */
	S_BPREL16((short) 0x0100),
	/** Module-local symbol. */
	S_LDATA16((short) 0x0101),
	/** Global data symbol. */
	S_GDATA16((short) 0x0102),
	/** a public symbol. */
	S_PUB16((short) 0x0103),
	/** Local procedure start. */
	S_LPROC16((short) 0x0104),
	/** Global procedure start. */
	S_GPROC16((short) 0x0105),
	/** Thunk Start. */
	S_THUNK16((short) 0x0106),
	/** block start. */
	S_BLOCK16((short) 0x0107),
	/** with start. */
	S_WITH16((short) 0x0108),
	/** code label. */
	S_LABEL16((short) 0x0109),
	/** change execution model. */
	S_CEXMODEL16((short) 0x010a),
	/** address of virtual function table. */
	S_VFTABLE16((short) 0x010b),
	/** register relative address. */
	S_REGREL16((short) 0x010c),

	/** BP-relative. */
	S_BPREL32_16t((short) 0x0200),
	/** Module-local symbol. */
	S_LDATA32_16t((short) 0x0201),
	/** Global data symbol. */
	S_GDATA32_16t((short) 0x0202),
	/** a public symbol (CV internal reserved). */
	S_PUB32_16t((short) 0x0203),
	/** Local procedure start. */
	S_LPROC32_16t((short) 0x0204),
	/** Global procedure start. */
	S_GPROC32_16t((short) 0x0205),
	/** Thunk Start. */
	S_THUNK32_ST((short) 0x0206),
	/** block start. */
	S_BLOCK32_ST((short) 0x0207),
	/** with start. */
	S_WITH32_ST((short) 0x0208),
	/** code label. */
	S_LABEL32_ST((short) 0x0209),
	/** change execution model. */
	S_CEXMODEL32((short) 0x020a),
	/** address of virtual function table. */
	S_VFTABLE32_16t((short) 0x020b),
	/** register relative address. */
	S_REGREL32_16t((short) 0x020c),
	/** local thread storage. */
	S_LTHREAD32_16t((short) 0x020d),
	/** global thread storage. */
	S_GTHREAD32_16t((short) 0x020e),
	/** static link for MIPS EH implementation. */
	S_SLINK32((short) 0x020f),

	/** Local procedure start. */
	S_LPROCMIPS_16t((short) 0x0300),
	/** Global procedure start. */
	S_GPROCMIPS_16t((short) 0x0301),

	/** Reference to a procedure. */
	S_PROCREF_ST((short) 0x0400),
	/** Reference to data. */
	S_DATAREF_ST((short) 0x0401),
	/** Used for page alignment of symbols. */
	S_ALIGN((short) 0x0402),

	/** Local Reference to a procedure. */
	S_LPROCREF_ST((short) 0x0403),
	/** OEM defined symbol. */
	S_OEM((short) 0x0404),

	/** Register variable. */
	S_REGISTER_ST((short) 0x1001),
	/** constant symbol. */
	S_CONSTANT_ST((short) 0x1002),
	/** User defined type. */
	S_UDT_ST((short) 0x1003),
	/** special UDT for cobol that does not symbol pack. */
	S_COBOLUDT_ST((short) 0x1004),
	/** multiple register variable. */
	S_MANYREG_ST((short) 0x1005),
	/** BP-relative. */
	S_BPREL32_ST((short) 0x1006),
	/** Module-local symbol. */
	S_LDATA32_ST((short) 0x1007),
	/** Global data symbol. */
	S_GDATA32_ST((short) 0x1008),
	/** a public symbol (CV internal reserved). */
	S_PUB32_ST((short) 0x1009),
	/** Local procedure start. */
	S_LPROC32_ST((short) 0x100a),
	/** Global procedure start. */
	S_GPROC32_ST((short) 0x100b),
	/** address of virtual function table. */
	S_VFTABLE32((short) 0x100c),
	/** register relative address. */
	S_REGREL32_ST((short) 0x100d),
	/** local thread storage. */
	S_LTHREAD32_ST((short) 0x100e),
	/** global thread storage. */
	S_GTHREAD32_ST((short) 0x100f),

	/** Local procedure start. */
	S_LPROCMIPS_ST((short) 0x1010),
	/** Global procedure start. */
	S_GPROCMIPS_ST((short) 0x1011),

	/** extra frame and proc information. */
	S_FRAMEPROC((short) 0x1012),
	/** extended compile flags and info. */
	S_COMPILE2_ST((short) 0x1013),

	/** multiple register variable. */
	S_MANYREG2_ST((short) 0x1014),
	/** Local procedure start (IA64). */
	S_LPROCIA64_ST((short) 0x1015),
	/** Global procedure start (IA64). */
	S_GPROCIA64_ST((short) 0x1016),

	// Local symbols for IL
	/** local IL sym with field for local slot index. */
	S_LOCALSLOT_ST((short) 0x1017),
	/** local IL sym with field for parameter slot index. */
	S_PARAMSLOT_ST((short) 0x1018),

	/** Annotation string literals. */
	S_ANNOTATION((short) 0x1019),

	/** Global proc. */
	S_GMANPROC_ST((short) 0x101a),
	/** Local proc. */
	S_LMANPROC_ST((short) 0x101b),
	/** reserved. */
	S_RESERVED1((short) 0x101c),
	/** reserved. */
	S_RESERVED2((short) 0x101d),
	/** reserved. */
	S_RESERVED3((short) 0x101e),
	/** reserved. */
	S_RESERVED4((short) 0x101f),
	/** CodeView symbol kind S_LMANDATA_ST. */
	S_LMANDATA_ST((short) 0x1020),
	/** CodeView symbol kind S_GMANDATA_ST. */
	S_GMANDATA_ST((short) 0x1021),
	/** CodeView symbol kind S_MANFRAMEREL_ST. */
	S_MANFRAMEREL_ST((short) 0x1022),
	/** CodeView symbol kind S_MANREGISTER_ST. */
	S_MANREGISTER_ST((short) 0x1023),
	/** CodeView symbol kind S_MANSLOT_ST. */
	S_MANSLOT_ST((short) 0x1024),
	/** CodeView symbol kind S_MANMANYREG_ST. */
	S_MANMANYREG_ST((short) 0x1025),
	/** CodeView symbol kind S_MANREGREL_ST. */
	S_MANREGREL_ST((short) 0x1026),
	/** CodeView symbol kind S_MANMANYREG2_ST. */
	S_MANMANYREG2_ST((short) 0x1027),
	/** Index for type referenced by name from metadata. */
	S_MANTYPREF((short) 0x1028),
	/** Using namespace. */
	S_UNAMESPACE_ST((short) 0x1029),

	/** path to object file name. */
	S_OBJNAME((short) 0x1101),
	/** Thunk Start. */
	S_THUNK32((short) 0x1102),
	/** block start. */
	S_BLOCK32((short) 0x1103),
	/** with start. */
	S_WITH32((short) 0x1104),
	/** code label. */
	S_LABEL32((short) 0x1105),
	/** Register variable. */
	S_REGISTER((short) 0x1106),
	/** constant symbol. */
	S_CONSTANT((short) 0x1107),
	/** User defined type. */
	S_UDT((short) 0x1108),
	/** special UDT for cobol that does not symbol pack. */
	S_COBOLUDT((short) 0x1109),
	/** multiple register variable. */
	S_MANYREG((short) 0x110a),
	/** BP-relative. */
	S_BPREL32((short) 0x110b),
	/** Module-local symbol. */
	S_LDATA32((short) 0x110c),
	/** Global data symbol. */
	S_GDATA32((short) 0x110d),
	/** a public symbol (CV internal reserved). */
	S_PUB32((short) 0x110e),
	/** Local procedure start. */
	S_LPROC32((short) 0x110f),
	/** Global procedure start. */
	S_GPROC32((short) 0x1110),
	/** register relative address. */
	S_REGREL32((short) 0x1111),
	/** local thread storage. */
	S_LTHREAD32((short) 0x1112),
	/** global thread storage. */
	S_GTHREAD32((short) 0x1113),

	/** Local procedure start. */
	S_LPROCMIPS((short) 0x1114),
	/** Global procedure start. */
	S_GPROCMIPS((short) 0x1115),
	/** extended compile flags and info. */
	S_COMPILE2((short) 0x1116),
	/** multiple register variable. */
	S_MANYREG2((short) 0x1117),
	/** Local procedure start (IA64). */
	S_LPROCIA64((short) 0x1118),
	/** Global procedure start (IA64). */
	S_GPROCIA64((short) 0x1119),
	/** local IL sym with field for local slot index. */
	S_LOCALSLOT((short) 0x111a),

	/** local IL sym with field for parameter slot index. */
	S_PARAMSLOT((short) 0x111b),

	/** CodeView symbol kind S_LMANDATA. */
	S_LMANDATA((short) 0x111c),
	/** CodeView symbol kind S_GMANDATA. */
	S_GMANDATA((short) 0x111d),
	/** CodeView symbol kind S_MANFRAMEREL. */
	S_MANFRAMEREL((short) 0x111e),
	/** CodeView symbol kind S_MANREGISTER. */
	S_MANREGISTER((short) 0x111f),
	/** CodeView symbol kind S_MANSLOT. */
	S_MANSLOT((short) 0x1120),
	/** CodeView symbol kind S_MANMANYREG. */
	S_MANMANYREG((short) 0x1121),
	/** CodeView symbol kind S_MANREGREL. */
	S_MANREGREL((short) 0x1122),
	/** CodeView symbol kind S_MANMANYREG2. */
	S_MANMANYREG2((short) 0x1123),
	/** Using namespace. */
	S_UNAMESPACE((short) 0x1124),

	/** Reference to a procedure. */
	S_PROCREF((short) 0x1125),
	/** Reference to data. */
	S_DATAREF((short) 0x1126),
	/** Local Reference to a procedure. */
	S_LPROCREF((short) 0x1127),
	/** Reference to an S_ANNOTATION symbol. */
	S_ANNOTATIONREF((short) 0x1128),
	/** Reference to one of the many MANPROCSYM's. */
	S_TOKENREF((short) 0x1129),

	/** Global proc. */
	S_GMANPROC((short) 0x112a),
	/** Local proc. */
	S_LMANPROC((short) 0x112b),

	/** trampoline thunks. */
	S_TRAMPOLINE((short) 0x112c),
	/** constants with metadata type info. */
	S_MANCONSTANT((short) 0x112d),

	/** relative to virtual frame ptr. */
	S_ATTR_FRAMEREL((short) 0x112e),
	/** stored in a register. */
	S_ATTR_REGISTER((short) 0x112f),
	/** relative to register (alternate frame ptr). */
	S_ATTR_REGREL((short) 0x1130),
	/** stored in >1 register. */
	S_ATTR_MANYREG((short) 0x1131),

	/** CodeView symbol kind S_SEPCODE. */
	S_SEPCODE((short) 0x1132),

	/** defines a local symbol in optimized code. */
	S_LOCAL_2005((short) 0x1133),
	/** defines a single range of addresses in which symbol can be evaluated. */
	S_DEFRANGE_2005((short) 0x1134),
	/** defines ranges of addresses in which symbol can be evaluated. */
	S_DEFRANGE2_2005((short) 0x1135),

	/** A COFF section in a PE executable. */
	S_SECTION((short) 0x1136),
	/** A COFF group. */
	S_COFFGROUP((short) 0x1137),
	/** A export. */
	S_EXPORT((short) 0x1138),

	/** Indirect call site information. */
	S_CALLSITEINFO((short) 0x1139),
	/** Security cookie information. */
	S_FRAMECOOKIE((short) 0x113a),

	/** Discarded by LINK /OPT:REF (experimental, see richards). */
	S_DISCARDED((short) 0x113b),

	/** Replacement for S_COMPILE2. */
	S_COMPILE3((short) 0x113c),
	/** Environment block split off from S_COMPILE2. */
	S_ENVBLOCK((short) 0x113d),

	/** defines a local symbol in optimized code. */
	S_LOCAL((short) 0x113e),
	/** defines a single range of addresses in which symbol can be evaluated. */
	S_DEFRANGE((short) 0x113f),
	/** ranges for a subfield. */
	S_DEFRANGE_SUBFIELD((short) 0x1140),

	/** ranges for en-registered symbol. */
	S_DEFRANGE_REGISTER((short) 0x1141),
	/** range for stack symbol. */
	S_DEFRANGE_FRAMEPOINTER_REL((short) 0x1142),
	/** ranges for en-registered field of symbol. */
	S_DEFRANGE_SUBFIELD_REGISTER((short) 0x1143),
	/** CodeView symbol kind S_DEFRANGE_FRAMEPOINTER_REL_FULL_SCOPE. */
	S_DEFRANGE_FRAMEPOINTER_REL_FULL_SCOPE((short) 0x1144),
	/** range for symbol address as register + offset. */
	S_DEFRANGE_REGISTER_REL((short) 0x1145),

	/** CodeView symbol kind S_LPROC32_ID. */
	S_LPROC32_ID((short) 0x1146),
	/** CodeView symbol kind S_GPROC32_ID. */
	S_GPROC32_ID((short) 0x1147),
	/** CodeView symbol kind S_LPROCMIPS_ID. */
	S_LPROCMIPS_ID((short) 0x1148),
	/** CodeView symbol kind S_GPROCMIPS_ID. */
	S_GPROCMIPS_ID((short) 0x1149),
	/** CodeView symbol kind S_LPROCIA64_ID. */
	S_LPROCIA64_ID((short) 0x114a),
	/** CodeView symbol kind S_GPROCIA64_ID. */
	S_GPROCIA64_ID((short) 0x114b),

	/** build information. */
	S_BUILDINFO((short) 0x114c),
	/** inlined function callsite. */
	S_INLINESITE((short) 0x114d),
	/** CodeView symbol kind S_INLINESITE_END. */
	S_INLINESITE_END((short) 0x114e),
	/** CodeView symbol kind S_PROC_ID_END. */
	S_PROC_ID_END((short) 0x114f),

	/** CodeView symbol kind S_DEFRANGE_HLSL. */
	S_DEFRANGE_HLSL((short) 0x1150),
	/** CodeView symbol kind S_GDATA_HLSL. */
	S_GDATA_HLSL((short) 0x1151),
	/** CodeView symbol kind S_LDATA_HLSL. */
	S_LDATA_HLSL((short) 0x1152),

	/** CodeView symbol kind S_FILESTATIC. */
	S_FILESTATIC((short) 0x1153),

	// #if defined(CC_DP_CXX) && CC_DP_CXX

	/** DPC groupshared variable. */
	S_LOCAL_DPC_GROUPSHARED((short) 0x1154),
	/** DPC local procedure start. */
	S_LPROC32_DPC((short) 0x1155),
	/** CodeView symbol kind S_LPROC32_DPC_ID. */
	S_LPROC32_DPC_ID((short) 0x1156),
	/** DPC pointer tag definition range. */
	S_DEFRANGE_DPC_PTR_TAG((short) 0x1157),
	/** DPC pointer tag value to symbol record map. */
	S_DPC_SYM_TAG_MAP((short) 0x1158),

	/** CodeView symbol kind S_ARMSWITCHTABLE. */
	S_ARMSWITCHTABLE((short) 0x1159),
	/** CodeView symbol kind S_CALLEES. */
	S_CALLEES((short) 0x115a),
	/** CodeView symbol kind S_CALLERS. */
	S_CALLERS((short) 0x115b),
	/** CodeView symbol kind S_POGODATA. */
	S_POGODATA((short) 0x115c),
	/** extended inline site information. */
	S_INLINESITE2((short) 0x115d),

	/** heap allocation site. */
	S_HEAPALLOCSITE((short) 0x115e),

	/** only generated at link time. */
	S_MOD_TYPEREF((short) 0x115f),

	/** only generated at link time for mini PDB. */
	S_REF_MINIPDB((short) 0x1160),
	/** only generated at link time for mini PDB. */
	S_PDBMAP((short) 0x1161),

	/** CodeView symbol kind S_GDATA_HLSL32. */
	S_GDATA_HLSL32((short) 0x1162),
	/** CodeView symbol kind S_LDATA_HLSL32. */
	S_LDATA_HLSL32((short) 0x1163),

	/** CodeView symbol kind S_GDATA_HLSL32_EX. */
	S_GDATA_HLSL32_EX((short) 0x1164),
	/** CodeView symbol kind S_LDATA_HLSL32_EX. */
	S_LDATA_HLSL32_EX((short) 0x1165);

	private final short value;

	CodeViewSymbolType(short value) {
		this.value = value;
	}

	/**
	 * Returns the raw symbol kind value.
	 *
	 * @return raw symbol kind value
	 */
	public short getValue() {
		return value;
	}

	/**
	 * Resolves a raw symbol kind value.
	 *
	 * @param value raw symbol kind value
	 * @return matching symbol kind
	 * @throws IllegalArgumentException if {@code value} is unknown
	 */
	public static CodeViewSymbolType valueFrom(short value) {
		for (CodeViewSymbolType symbol : values()) {
			if (symbol.getValue() == value) {
				return symbol;
			}
		}

		throw new IllegalArgumentException();
	}
}
