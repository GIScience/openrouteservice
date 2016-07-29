/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package org.freeopenls.locationutilityservice.geocoders;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.graphhopper.util.Helper;

public class GeocodingUtils {
	private static final String GERMAN_ZIPCODE_PATTERN = "[D]-\\d{5}?";
	private static final String AUSTRIAN_ZIPCODE_PATTERN = "[A]-\\d{4}?";
	private static final String SWISS_ZIPCODE_PATTERN = "[CH]-\\d{4}?";
	
	private static Pattern german_zipcode_pattern;
	private static Pattern austrian_zipcode_pattern;
	private static Pattern swiss_zipcode_pattern;
	
	static {
		german_zipcode_pattern = Pattern.compile(GERMAN_ZIPCODE_PATTERN);
		austrian_zipcode_pattern = Pattern.compile(AUSTRIAN_ZIPCODE_PATTERN);
		swiss_zipcode_pattern = Pattern.compile(SWISS_ZIPCODE_PATTERN);
	}

	public static double getDistance(double angle) {
		return 6378000 * angle / (180 / Math.PI);
	}

	public static String sanitizeAddress(String address)
	{
		if (!Helper.isEmpty(address))
		{
			 Matcher matcher = german_zipcode_pattern.matcher(address);
			 if (matcher.find())
			 {
				 String replace = matcher.group(0).replace("-", "");
				 return matcher.replaceAll(replace);
			 }
			 
			 matcher = austrian_zipcode_pattern.matcher(address);
			 if (matcher.find())
			 {
				 String replace = matcher.group(0).replace("-", "");
				 return matcher.replaceAll(replace);
			 }
			 
			 matcher = swiss_zipcode_pattern.matcher(address);
			 if (matcher.find())
			 {
				 String replace = matcher.group(0).replace("-", "");
				 return matcher.replaceAll(replace);
			 }
		}
		
		return address;
	}
}
