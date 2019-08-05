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
package heigit.ors.localization;

import java.util.Locale;

public class Language {
	private final String _langCode; 
	private final String _enLangName; 
	private final String _nativeName; 
	private Locale _locale; 

	public Language(String langCode, String enLangName, String nativeName) { 
		_langCode = langCode; 
		_enLangName = enLangName; 
		_nativeName = nativeName; 
	} 

	public String getLangCode() { 
		return _langCode; 
	} 

	public String getEnLangName() { 
		return _enLangName; 
	} 

	public String getNativeName() { 
		return _nativeName; 
	} 

	public synchronized Locale getLocale() { 
		if (_locale == null) { 
			_locale = new Locale(_langCode); 
		} 
		return _locale; 
	} 
	
	@Override 
	public int hashCode() { 
		return _langCode.hashCode();
	} 
}
