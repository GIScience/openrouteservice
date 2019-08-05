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
