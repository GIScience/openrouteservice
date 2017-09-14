/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
