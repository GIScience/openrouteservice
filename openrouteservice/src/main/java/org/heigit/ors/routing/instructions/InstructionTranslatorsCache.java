/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
 */
package org.heigit.ors.routing.instructions;

import org.heigit.ors.localization.LocalizationManager;

import java.util.HashMap;
import java.util.Map;

public class InstructionTranslatorsCache {
	private final Map<Integer, InstructionTranslator> translators;
	private static InstructionTranslatorsCache mInstance = null;

	private InstructionTranslatorsCache()
	{
		translators = new HashMap<>();
	}

	public static InstructionTranslatorsCache getInstance() {
		if(null == mInstance) {
			synchronized(InstructionTranslatorsCache.class) {
				mInstance = new InstructionTranslatorsCache();
			}
		}
		return mInstance;
	}

	public InstructionTranslator getTranslator(String langCode) throws Exception {
		int hashCode = langCode.hashCode();
		InstructionTranslator res = translators.get(hashCode);
		if (res == null) {
			synchronized(InstructionTranslatorsCache.class) {
				res = new InstructionTranslator(LocalizationManager.getInstance().getLanguageResources(langCode));
				translators.put(hashCode, res);
			}
		}
		return res;
	}
}
