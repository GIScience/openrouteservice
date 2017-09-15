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

	public Locale getLocale() { 
		if (_locale != null) { 
			return _locale; 
		} 
		synchronized (this) { 
			if (_locale == null){ 
				_locale = new Locale(_langCode); 
			} 
		} 
		return _locale; 
	} 
	
	@Override 
	public int hashCode() { 
		return _langCode.hashCode();
	} 
}
