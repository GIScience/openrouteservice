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
	private final String langTag;
	private final String enLangName;
	private final String nativeName;
	private final Locale locale;

	public Language(String langTag) {
		this.langTag = langTag;
		this.locale = Locale.forLanguageTag(langTag);
		this.enLangName = locale.getDisplayName();
		this.nativeName = locale.getDisplayName(locale);
	}

	public String getLangTag() {
		return langTag;
	} 

	public String getEnLangName() { 
		return enLangName;
	} 

	public String getNativeName() { 
		return nativeName;
	} 

	public Locale getLocale() {
		return locale;
	} 
	
	@Override 
	public int hashCode() { 
		return langTag.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && getClass() == obj.getClass() && langTag.equals(((Language)obj).getLangTag());
	}
}
