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
package heigit.ors.routing.instructions;

import java.util.HashMap;
import java.util.Map;

import heigit.ors.localization.LocalizationManager;

public class InstructionTranslatorsCache 
{
	private Map<Integer, InstructionTranslator> _translators = null;
	private static volatile InstructionTranslatorsCache m_instance = null;

	private InstructionTranslatorsCache()
	{
		_translators = new HashMap<Integer, InstructionTranslator>();
	}

	public static InstructionTranslatorsCache getInstance()
	{
		if(null == m_instance)
		{
			synchronized(InstructionTranslatorsCache.class)
			{
				m_instance = new InstructionTranslatorsCache();
			}
		}

		return m_instance;
	}

	public InstructionTranslator getTranslator(String langCode) throws Exception
	{
		int hashCode = langCode.hashCode();

		InstructionTranslator res = _translators.get(hashCode);

		if (res == null)
		{
			synchronized(InstructionTranslatorsCache.class)
			{
				res = new InstructionTranslator(LocalizationManager.getInstance().getLanguageResources(langCode));
				_translators.put(hashCode, res);
			}
		}

		return res;
	}
}
