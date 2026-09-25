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

import net.boricj.bft.dwarf.constants.DwarfAttributeName;
import net.boricj.bft.dwarf.constants.DwarfForm;

/**
 * One concrete DIE attribute value.
 *
 * @param rawAttributeCode numeric DW_AT_* code
 * @param attributeName known attribute enum or null
 * @param form encoded form
 * @param value typed attribute value
 */
public record DwarfDieAttribute(
		long rawAttributeCode, DwarfAttributeName attributeName, DwarfForm form, DwarfValue value) {}
