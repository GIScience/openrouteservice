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

import java.util.Locale;

public class Language {
	private final String langCode;
	private final String enLangName;
	private final String nativeName;
	private Locale locale;

	public Language(String langCode, String enLangName, String nativeName) { 
		this.langCode = langCode;
		this.enLangName = enLangName;
		this.nativeName = nativeName;
	} 

	public String getLangCode() { 
		return langCode;
	} 

	public String getEnLangName() { 
		return enLangName;
	} 

	public String getNativeName() { 
		return nativeName;
	} 

	public synchronized Locale getLocale() { 
		if (locale == null) {
			locale = new Locale(langCode);
		} 
		return locale;
	} 
	
	@Override 
	public int hashCode() { 
		return langCode.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Language other = (Language) obj;
		return langCode.equals(other.getLangCode());
	}
}
