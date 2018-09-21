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
package heigit.ors.geocoding.geocoders;

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

	public static double getDistanceAccuracyScore(double dist)
	{
		if (dist < 1)
			return 1.0;
		else if (dist < 10)
			return 0.9;
		else if (dist < 100)
			return 0.8;
		else if (dist < 250)
			return 0.7;
		else if (dist < 500)
			return 0.6;
		else if (dist < 1000)
			return 0.5;
		else
			return 0.5;
	}
}
