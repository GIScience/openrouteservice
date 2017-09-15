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

public class LocalString {
	private Language _language; 
	private String _string; 

	public LocalString(Language language, String string) 
	{ 
		this._language = language; 
		this._string = string; 
	} 

	public Language getLanguage() { 
		return _language; 
	} 

	public String getString() { 
		return _string; 
	} 

	@Override 
	public boolean equals(Object o) { 
		if (this == o) return true; 
		if (o == null || getClass() != o.getClass()) return false; 

		LocalString that = (LocalString) o; 

		return _language.equals(that._language) && _string.equals(that._string); 

	} 

	@Override 
	public int hashCode() { 
		int result = _language.hashCode(); 
		result = 31 * result + _string.hashCode(); 
		return result; 
	} 

	@Override 
	public String toString() { 
		return "LocalString{" + 
				"language=" + _language + 
				", string='" + _string + '\'' + 
				'}'; 
	} 
}
