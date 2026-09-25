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
package net.boricj.bft.dwarf.model;

import net.boricj.bft.dwarf.constants.DwarfUnitType;
import net.boricj.bft.dwarf.constants.DwarfVersion;

/**
 * Compilation unit header fields from .debug_info.
 *
 * @param unitLength encoded DWARF unit length value, excluding the length field itself
 * @param dwarf64 whether this compilation unit uses the DWARF64 encoding form
 * @param version DWARF version encoded in the compilation unit header
 * @param unitType DWARF unit type as encoded in DWARF v5+ headers
 * @param abbreviationOffset offset of the associated abbreviation table contribution
 * @param addressSize address size in bytes used by this compilation unit
 */
public record DwarfCompilationUnitHeader(
		long unitLength,
		boolean dwarf64,
		DwarfVersion version,
		DwarfUnitType unitType,
		long abbreviationOffset,
		int addressSize) {}
