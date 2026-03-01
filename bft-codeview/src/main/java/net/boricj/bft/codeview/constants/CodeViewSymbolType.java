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

public enum CodeViewSymbolType {
	S_COMPILE((short) 0x0001), // Compile flags symbol
	S_REGISTER_16t((short) 0x0002), // Register variable
	S_CONSTANT_16t((short) 0x0003), // constant symbol
	S_UDT_16t((short) 0x0004), // User defined type
	S_SSEARCH((short) 0x0005), // Start Search
	S_END((short) 0x0006), // Block, procedure, "with" or thunk end
	S_SKIP((short) 0x0007), // Reserve symbol space in $$Symbols table
	S_CVRESERVE((short) 0x0008), // Reserved symbol for CV internal use
	S_OBJNAME_ST((short) 0x0009), // path to object file name
	S_ENDARG((short) 0x000a), // end of argument/return list
	S_COBOLUDT_16t((short) 0x000b), // special UDT for cobol that does not symbol pack
	S_MANYREG_16t((short) 0x000c), // multiple register variable
	S_RETURN((short) 0x000d), // return description symbol
	S_ENTRYTHIS((short) 0x000e), // description of this pointer on entry

	S_BPREL16((short) 0x0100), // BP-relative
	S_LDATA16((short) 0x0101), // Module-local symbol
	S_GDATA16((short) 0x0102), // Global data symbol
	S_PUB16((short) 0x0103), // a public symbol
	S_LPROC16((short) 0x0104), // Local procedure start
	S_GPROC16((short) 0x0105), // Global procedure start
	S_THUNK16((short) 0x0106), // Thunk Start
	S_BLOCK16((short) 0x0107), // block start
	S_WITH16((short) 0x0108), // with start
	S_LABEL16((short) 0x0109), // code label
	S_CEXMODEL16((short) 0x010a), // change execution model
	S_VFTABLE16((short) 0x010b), // address of virtual function table
	S_REGREL16((short) 0x010c), // register relative address

	S_BPREL32_16t((short) 0x0200), // BP-relative
	S_LDATA32_16t((short) 0x0201), // Module-local symbol
	S_GDATA32_16t((short) 0x0202), // Global data symbol
	S_PUB32_16t((short) 0x0203), // a public symbol (CV internal reserved)
	S_LPROC32_16t((short) 0x0204), // Local procedure start
	S_GPROC32_16t((short) 0x0205), // Global procedure start
	S_THUNK32_ST((short) 0x0206), // Thunk Start
	S_BLOCK32_ST((short) 0x0207), // block start
	S_WITH32_ST((short) 0x0208), // with start
	S_LABEL32_ST((short) 0x0209), // code label
	S_CEXMODEL32((short) 0x020a), // change execution model
	S_VFTABLE32_16t((short) 0x020b), // address of virtual function table
	S_REGREL32_16t((short) 0x020c), // register relative address
	S_LTHREAD32_16t((short) 0x020d), // local thread storage
	S_GTHREAD32_16t((short) 0x020e), // global thread storage
	S_SLINK32((short) 0x020f), // static link for MIPS EH implementation

	S_LPROCMIPS_16t((short) 0x0300), // Local procedure start
	S_GPROCMIPS_16t((short) 0x0301), // Global procedure start

	// if these ref symbols have names following then the names are in ST format
	S_PROCREF_ST((short) 0x0400), // Reference to a procedure
	S_DATAREF_ST((short) 0x0401), // Reference to data
	S_ALIGN((short) 0x0402), // Used for page alignment of symbols

	S_LPROCREF_ST((short) 0x0403), // Local Reference to a procedure
	S_OEM((short) 0x0404), // OEM defined symbol

	// sym records with 32-bit types embedded instead of 16-bit
	// all have 0x1000 bit set for easy identification
	// only do the 32-bit target versions since we don't really
	// care about 16-bit ones anymore.
	// S_TI16_MAX          =  0x1000,

	S_REGISTER_ST((short) 0x1001), // Register variable
	S_CONSTANT_ST((short) 0x1002), // constant symbol
	S_UDT_ST((short) 0x1003), // User defined type
	S_COBOLUDT_ST((short) 0x1004), // special UDT for cobol that does not symbol pack
	S_MANYREG_ST((short) 0x1005), // multiple register variable
	S_BPREL32_ST((short) 0x1006), // BP-relative
	S_LDATA32_ST((short) 0x1007), // Module-local symbol
	S_GDATA32_ST((short) 0x1008), // Global data symbol
	S_PUB32_ST((short) 0x1009), // a public symbol (CV internal reserved)
	S_LPROC32_ST((short) 0x100a), // Local procedure start
	S_GPROC32_ST((short) 0x100b), // Global procedure start
	S_VFTABLE32((short) 0x100c), // address of virtual function table
	S_REGREL32_ST((short) 0x100d), // register relative address
	S_LTHREAD32_ST((short) 0x100e), // local thread storage
	S_GTHREAD32_ST((short) 0x100f), // global thread storage

	S_LPROCMIPS_ST((short) 0x1010), // Local procedure start
	S_GPROCMIPS_ST((short) 0x1011), // Global procedure start

	S_FRAMEPROC((short) 0x1012), // extra frame and proc information
	S_COMPILE2_ST((short) 0x1013), // extended compile flags and info

	// new symbols necessary for 16-bit enumerates of IA64 registers
	// and IA64 specific symbols

	S_MANYREG2_ST((short) 0x1014), // multiple register variable
	S_LPROCIA64_ST((short) 0x1015), // Local procedure start (IA64)
	S_GPROCIA64_ST((short) 0x1016), // Global procedure start (IA64)

	// Local symbols for IL
	S_LOCALSLOT_ST((short) 0x1017), // local IL sym with field for local slot index
	S_PARAMSLOT_ST((short) 0x1018), // local IL sym with field for parameter slot index

	S_ANNOTATION((short) 0x1019), // Annotation string literals

	// symbols to support managed code debugging
	S_GMANPROC_ST((short) 0x101a), // Global proc
	S_LMANPROC_ST((short) 0x101b), // Local proc
	S_RESERVED1((short) 0x101c), // reserved
	S_RESERVED2((short) 0x101d), // reserved
	S_RESERVED3((short) 0x101e), // reserved
	S_RESERVED4((short) 0x101f), // reserved
	S_LMANDATA_ST((short) 0x1020),
	S_GMANDATA_ST((short) 0x1021),
	S_MANFRAMEREL_ST((short) 0x1022),
	S_MANREGISTER_ST((short) 0x1023),
	S_MANSLOT_ST((short) 0x1024),
	S_MANMANYREG_ST((short) 0x1025),
	S_MANREGREL_ST((short) 0x1026),
	S_MANMANYREG2_ST((short) 0x1027),
	S_MANTYPREF((short) 0x1028), // Index for type referenced by name from metadata
	S_UNAMESPACE_ST((short) 0x1029), // Using namespace

	// Symbols w/ SZ name fields. All name fields contain utf8 encoded strings.
	// S_ST_MAX        =  0x1100,  // starting point for SZ name symbols

	S_OBJNAME((short) 0x1101), // path to object file name
	S_THUNK32((short) 0x1102), // Thunk Start
	S_BLOCK32((short) 0x1103), // block start
	S_WITH32((short) 0x1104), // with start
	S_LABEL32((short) 0x1105), // code label
	S_REGISTER((short) 0x1106), // Register variable
	S_CONSTANT((short) 0x1107), // constant symbol
	S_UDT((short) 0x1108), // User defined type
	S_COBOLUDT((short) 0x1109), // special UDT for cobol that does not symbol pack
	S_MANYREG((short) 0x110a), // multiple register variable
	S_BPREL32((short) 0x110b), // BP-relative
	S_LDATA32((short) 0x110c), // Module-local symbol
	S_GDATA32((short) 0x110d), // Global data symbol
	S_PUB32((short) 0x110e), // a public symbol (CV internal reserved)
	S_LPROC32((short) 0x110f), // Local procedure start
	S_GPROC32((short) 0x1110), // Global procedure start
	S_REGREL32((short) 0x1111), // register relative address
	S_LTHREAD32((short) 0x1112), // local thread storage
	S_GTHREAD32((short) 0x1113), // global thread storage

	S_LPROCMIPS((short) 0x1114), // Local procedure start
	S_GPROCMIPS((short) 0x1115), // Global procedure start
	S_COMPILE2((short) 0x1116), // extended compile flags and info
	S_MANYREG2((short) 0x1117), // multiple register variable
	S_LPROCIA64((short) 0x1118), // Local procedure start (IA64)
	S_GPROCIA64((short) 0x1119), // Global procedure start (IA64)
	S_LOCALSLOT((short) 0x111a), // local IL sym with field for local slot index
	// S_SLOT          = S_LOCALSLOT,  // alias for LOCALSLOT
	S_PARAMSLOT((short) 0x111b), // local IL sym with field for parameter slot index

	// symbols to support managed code debugging
	S_LMANDATA((short) 0x111c),
	S_GMANDATA((short) 0x111d),
	S_MANFRAMEREL((short) 0x111e),
	S_MANREGISTER((short) 0x111f),
	S_MANSLOT((short) 0x1120),
	S_MANMANYREG((short) 0x1121),
	S_MANREGREL((short) 0x1122),
	S_MANMANYREG2((short) 0x1123),
	S_UNAMESPACE((short) 0x1124), // Using namespace

	// ref symbols with name fields
	S_PROCREF((short) 0x1125), // Reference to a procedure
	S_DATAREF((short) 0x1126), // Reference to data
	S_LPROCREF((short) 0x1127), // Local Reference to a procedure
	S_ANNOTATIONREF((short) 0x1128), // Reference to an S_ANNOTATION symbol
	S_TOKENREF((short) 0x1129), // Reference to one of the many MANPROCSYM's

	// continuation of managed symbols
	S_GMANPROC((short) 0x112a), // Global proc
	S_LMANPROC((short) 0x112b), // Local proc

	// short, light-weight thunks
	S_TRAMPOLINE((short) 0x112c), // trampoline thunks
	S_MANCONSTANT((short) 0x112d), // constants with metadata type info

	// native attributed local/parms
	S_ATTR_FRAMEREL((short) 0x112e), // relative to virtual frame ptr
	S_ATTR_REGISTER((short) 0x112f), // stored in a register
	S_ATTR_REGREL((short) 0x1130), // relative to register (alternate frame ptr)
	S_ATTR_MANYREG((short) 0x1131), // stored in >1 register

	// Separated code (from the compiler) support
	S_SEPCODE((short) 0x1132),

	S_LOCAL_2005((short) 0x1133), // defines a local symbol in optimized code
	S_DEFRANGE_2005((short) 0x1134), // defines a single range of addresses in which symbol can be evaluated
	S_DEFRANGE2_2005((short) 0x1135), // defines ranges of addresses in which symbol can be evaluated

	S_SECTION((short) 0x1136), // A COFF section in a PE executable
	S_COFFGROUP((short) 0x1137), // A COFF group
	S_EXPORT((short) 0x1138), // A export

	S_CALLSITEINFO((short) 0x1139), // Indirect call site information
	S_FRAMECOOKIE((short) 0x113a), // Security cookie information

	S_DISCARDED((short) 0x113b), // Discarded by LINK /OPT:REF (experimental, see richards)

	S_COMPILE3((short) 0x113c), // Replacement for S_COMPILE2
	S_ENVBLOCK((short) 0x113d), // Environment block split off from S_COMPILE2

	S_LOCAL((short) 0x113e), // defines a local symbol in optimized code
	S_DEFRANGE((short) 0x113f), // defines a single range of addresses in which symbol can be evaluated
	S_DEFRANGE_SUBFIELD((short) 0x1140), // ranges for a subfield

	S_DEFRANGE_REGISTER((short) 0x1141), // ranges for en-registered symbol
	S_DEFRANGE_FRAMEPOINTER_REL((short) 0x1142), // range for stack symbol.
	S_DEFRANGE_SUBFIELD_REGISTER((short) 0x1143), // ranges for en-registered field of symbol
	S_DEFRANGE_FRAMEPOINTER_REL_FULL_SCOPE(
			(short) 0x1144), // range for stack symbol span valid full scope of function body, gap might apply.
	S_DEFRANGE_REGISTER_REL((short) 0x1145), // range for symbol address as register + offset.

	// S_PROC symbols that reference ID instead of type
	S_LPROC32_ID((short) 0x1146),
	S_GPROC32_ID((short) 0x1147),
	S_LPROCMIPS_ID((short) 0x1148),
	S_GPROCMIPS_ID((short) 0x1149),
	S_LPROCIA64_ID((short) 0x114a),
	S_GPROCIA64_ID((short) 0x114b),

	S_BUILDINFO((short) 0x114c), // build information.
	S_INLINESITE((short) 0x114d), // inlined function callsite.
	S_INLINESITE_END((short) 0x114e),
	S_PROC_ID_END((short) 0x114f),

	S_DEFRANGE_HLSL((short) 0x1150),
	S_GDATA_HLSL((short) 0x1151),
	S_LDATA_HLSL((short) 0x1152),

	S_FILESTATIC((short) 0x1153),

	// #if defined(CC_DP_CXX) && CC_DP_CXX

	S_LOCAL_DPC_GROUPSHARED((short) 0x1154), // DPC groupshared variable
	S_LPROC32_DPC((short) 0x1155), // DPC local procedure start
	S_LPROC32_DPC_ID((short) 0x1156),
	S_DEFRANGE_DPC_PTR_TAG((short) 0x1157), // DPC pointer tag definition range
	S_DPC_SYM_TAG_MAP((short) 0x1158), // DPC pointer tag value to symbol record map

	// #endif // CC_DP_CXX

	S_ARMSWITCHTABLE((short) 0x1159),
	S_CALLEES((short) 0x115a),
	S_CALLERS((short) 0x115b),
	S_POGODATA((short) 0x115c),
	S_INLINESITE2((short) 0x115d), // extended inline site information

	S_HEAPALLOCSITE((short) 0x115e), // heap allocation site

	S_MOD_TYPEREF((short) 0x115f), // only generated at link time

	S_REF_MINIPDB((short) 0x1160), // only generated at link time for mini PDB
	S_PDBMAP((short) 0x1161), // only generated at link time for mini PDB

	S_GDATA_HLSL32((short) 0x1162),
	S_LDATA_HLSL32((short) 0x1163),

	S_GDATA_HLSL32_EX((short) 0x1164),
	S_LDATA_HLSL32_EX((short) 0x1165);

	private final short value;

	CodeViewSymbolType(short value) {
		this.value = value;
	}

	public short getValue() {
		return value;
	}

	public static CodeViewSymbolType valueFrom(short value) {
		for (CodeViewSymbolType symbol : values()) {
			if (symbol.getValue() == value) {
				return symbol;
			}
		}

		throw new IllegalArgumentException();
	}
}
