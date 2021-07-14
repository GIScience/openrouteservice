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
package org.heigit.ors.localization;

import java.util.HashMap;
import java.util.Map;

public class LanguageResources {
	private final Map<Integer,LocalString> localStrings;
	private final Language lang;
	
	public LanguageResources(String langTag) {
		this.lang = new Language(langTag);
		localStrings = new HashMap<>();
	}
	
	public void addLocalString(String resourceName, String resourceText) {
		int hashCode = resourceName.hashCode();
		if (!localStrings.containsKey(hashCode)) {
			LocalString localString = new LocalString(lang, resourceText);
			localStrings.put(hashCode, localString);
		}
	}
	
	public Language getLangCode() {
		return lang;
	}
	
	public String getTranslation(String name) throws Exception {
		return getTranslation(name, false);
	}
	
	public String getTranslation(String name, boolean throwException) throws Exception {
		if (name == null)
			return null;
		LocalString ls = localStrings.get(name.hashCode());
		if (ls != null) {
			return ls.getString();
		} else {
			if (throwException)
				throw new Exception("Unable to find translation for '" + name + "' in language '" + lang.getLangTag() + "'.");
			else
				return null;
		}
	}
}
