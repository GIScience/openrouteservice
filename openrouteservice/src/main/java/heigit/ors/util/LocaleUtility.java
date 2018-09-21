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
package heigit.ors.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocaleUtility {
	private static Map<String, String> _iso2ToISO3Map;
	
	static
	{
		String[] countries = Locale.getISOCountries();
		_iso2ToISO3Map = new HashMap<String, String>(countries.length);
		for (String country : countries) {
	        Locale locale = new Locale("", country);
	        _iso2ToISO3Map.put(locale.getISO3Country(), country);
	    }
	}
	
	public static String getISO2CountryFromISO3(String iso3)
	{
		return _iso2ToISO3Map.get(iso3);
	}
}
