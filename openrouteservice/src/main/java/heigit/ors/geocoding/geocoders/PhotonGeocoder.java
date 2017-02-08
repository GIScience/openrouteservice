/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2016
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.geocoding.geocoders;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Envelope;

import heigit.ors.util.HTTPUtility;

public class PhotonGeocoder extends AbstractGeocoder {

	private static final ArrayList<String> supportedLanguages = new ArrayList<String>();
	
	static
	{
		//{"message":"language kg is not supported, supported languages are: de, en, it, fr"}
		supportedLanguages.add("de");
		supportedLanguages.add("en");
		supportedLanguages.add("it");
		supportedLanguages.add("fr");
	}
	
	public PhotonGeocoder(String geocodingURL, String reverseGeocodingURL, String userAgent) {
		super(geocodingURL, reverseGeocodingURL, userAgent);
	}

    public GeocodingResult[] geocode(String address, String languages, int limit, Envelope bbox) throws IOException
    {
    	String code = (languages == null) ? "en" : languages.toLowerCase();
    	
    	if (!supportedLanguages.contains(code))
    		code = "en";
    	
		String reqParams = "?q=" + URLEncoder.encode(GeocodingUtils.sanitizeAddress(address), "UTF-8") + "&limit=" + limit + "&lang=" + code;
		String respContent = HTTPUtility.getResponse(geocodingURL + reqParams, 5000, userAgent, "UTF-8");
		
		if (!Helper.isEmpty(respContent) && !respContent.equals("[]")) {
	    	
			ArrayList<GeocodingResult> res = getGeocodeResults(respContent, bbox);
			return res.toArray(new GeocodingResult[res.size()]);
		}
		
		return null;
    }
	
	public GeocodingResult[] reverseGeocode(double lon, double lat, int limit, Envelope bbox) throws IOException
	{
		String reqParams = "?lat=" + lat  + "&lon=" + lon + "&limit=" + limit;
		String respContent = HTTPUtility.getResponse(reverseGeocodingURL + reqParams, 5000, userAgent, "UTF-8");

		if (!Helper.isEmpty(respContent) && !respContent.equals("[]")) {
			ArrayList<GeocodingResult> res = getGeocodeResults(respContent, bbox);
			return res.toArray(new GeocodingResult[res.size()]);
		}

		return null;
	}
	
	private ArrayList<GeocodingResult> getGeocodeResults(String respContent, Envelope bbox)
	{
		ArrayList<GeocodingResult> res = new ArrayList<GeocodingResult>();
    	
		JSONObject features = new JSONObject(respContent);
		JSONArray arr = (JSONArray)features.get("features");
		
		for (int j = 0; j < arr.length(); j++) {
			Object obj = arr.get(j);

			JSONObject geomObj = (JSONObject)((JSONObject) obj).get("geometry");
			
			JSONArray coordsObj = (JSONArray)geomObj.get("coordinates");
			double lon = Double.parseDouble(coordsObj.get(0).toString());
			double lat = Double.parseDouble(coordsObj.get(1).toString());
			
			if (bbox != null && !bbox.contains(lon, lat))
				continue;		

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
				gr.longitude = lon;
				gr.latitude = lat;
				
				res.add(gr);
			}
		}
		
		return res;
	}
	
	private String getJSONValue(JSONObject obj, String propName)
	{
		String value = null;
		if (obj.has(propName)) {
			Object obj2 = obj.get(propName);
			if (obj2 != null)
				value = obj2.toString();
		}
		
		return value;
	}
}
