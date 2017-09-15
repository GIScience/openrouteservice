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
