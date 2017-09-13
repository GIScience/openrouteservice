/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
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
