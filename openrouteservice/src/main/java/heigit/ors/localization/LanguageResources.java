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
package heigit.ors.localization;

import java.util.HashMap;
import java.util.Map;

public class LanguageResources 
{
	private Map<Integer,LocalString> _localStrings = null;
	private Language _lang;
	
	public LanguageResources(Language lang)
	{
		_lang = lang;
		_localStrings = new HashMap<Integer, LocalString>();
	}
	
	public void addLocalString(String resourceName, String resourceText)
	{
		int hashCode = resourceName.hashCode();

		if (!_localStrings.containsKey(hashCode))
		{
			LocalString localString = new LocalString(_lang, resourceText);
			_localStrings.put(hashCode, localString);
		}
	}
	
	public Language getLangCode()
	{
		return _lang;
	}
	
	public String getTranslation(String name) throws Exception
	{
		return getTranslation(name, false);
	}
	
	public String getTranslation(String name, boolean throwException) throws Exception
	{
		if (name == null)
			return null;

		LocalString ls = _localStrings.get(name.hashCode());
		if (ls != null)
			return ls.getString();
		else
		{
			if (throwException)
				throw new Exception("Unable to find translation for '" + name + "' in language '" + _lang.getLangCode() + "'.");
			else
				return null;
		}
	}
}
