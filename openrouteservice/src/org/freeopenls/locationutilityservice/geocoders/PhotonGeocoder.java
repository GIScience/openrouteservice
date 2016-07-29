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

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.freeopenls.tools.HTTPUtility;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.graphhopper.util.Helper;

public class PhotonGeocoder extends AbstractGeocoder {

	public PhotonGeocoder(String geocodingURL, String reverseGeocodingURL, String userAgent) {
		super(geocodingURL, reverseGeocodingURL, userAgent);
		// TODO Auto-generated constructor stub
	}

    public GeocodingResult[] geocode(String address, String languages, int limit) throws IOException
    {
    	String code = (languages == null) ? "en" : languages.toLowerCase();
    	
		String reqParams = "?q=" + URLEncoder.encode(GeocodingUtils.sanitizeAddress(address), "UTF-8") + "&limit=" + limit + "&lang=" + code;
		String respContent = HTTPUtility.getResponse(geocodingURL + reqParams, 5000, userAgent, "UTF-8");
		
		if (!Helper.isEmpty(respContent) && !respContent.equals("[]")) {
	    	
			ArrayList<GeocodingResult> res = getGeocodeResults(respContent);
			return res.toArray(new GeocodingResult[res.size()]);
		}
		
		return null;
    }
	
	public GeocodingResult reverseGeocode(double lat, double lon, int limit) throws IOException
	{
		String reqParams = "?lat=" + lat  + "&lon=" + lon + "&limit=" + limit;
		String respContent = HTTPUtility.getResponse(reverseGeocodingURL + reqParams, 5000, userAgent, "UTF-8");

		if (!Helper.isEmpty(respContent) && !respContent.equals("[]")) {
			return getGeocodeResults(respContent).get(0);
		}

		return null;
	}
	
	private ArrayList<GeocodingResult> getGeocodeResults(String respContent)
	{
		ArrayList<GeocodingResult> res = new ArrayList<GeocodingResult>();
    	
		JSONObject features = (JSONObject) JSONValue.parse(respContent);
		JSONArray arr = (JSONArray)features.get("features");
		
		for (int j = 0; j < arr.size(); j++) {
			Object obj = arr.get(j);

			JSONObject geomObj = (JSONObject)((JSONObject) obj).get("geometry");
			
			JSONArray coordsObj = (JSONArray)geomObj.get("coordinates");
			String lon = coordsObj.get(0).toString();
			String lat = coordsObj.get(1).toString();

			JSONObject addressObj = (JSONObject) ((JSONObject) obj).get("properties");

			String country = getJSONValue(addressObj, "country");
			String state = getJSONValue(addressObj, "state");
			String state_district = null;
			String postal_code = getJSONValue(addressObj, "postcode");
			String city = getJSONValue(addressObj, "city");
			String street = getJSONValue(addressObj, "street");
			String house_number = getJSONValue(addressObj, "housenumber");
			String name = getJSONValue(addressObj, "name");
			String house = name;
			String osm_value = getJSONValue(addressObj, "osm_value");
			if (!Helper.isEmpty(osm_value))
			{
				String osm_key = getJSONValue(addressObj, "osm_key");
				if (osm_value.equals("district"))
					state_district = osm_key;
				else if (osm_value.equals("state") && osm_key.equals("place"))
					state = name;
			}

			if (state != null || city != null || street != null) {
				GeocodingResult gr = new GeocodingResult();
				gr.city = city;
				gr.country = country;
				gr.state = state;
				gr.stateDistrict = state_district;
				gr.postalCode = postal_code;
				gr.road = street;
				gr.house = house;
				gr.houseNumber = house_number;
				gr.longitude = Double.parseDouble(lon);
				gr.latitude = Double.parseDouble(lat);
				
				res.add(gr);
			}
		}
		
		return res;
	}
	
	private String getJSONValue(JSONObject obj, String propName)
	{
		String value = null;
		if (obj.containsKey(propName)) {
			Object obj2 = obj.get(propName);
			if (obj2 != null)
				value = obj2.toString();
		}
		
		return value;
	}
}
