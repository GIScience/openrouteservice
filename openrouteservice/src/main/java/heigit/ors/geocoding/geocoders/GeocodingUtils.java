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
