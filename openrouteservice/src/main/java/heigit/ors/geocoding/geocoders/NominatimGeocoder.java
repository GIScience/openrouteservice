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

import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.graphhopper.util.Helper;

import heigit.ors.geocoding.geocoders.AbstractGeocoder;
import heigit.ors.util.HTTPUtility;

public class NominatimGeocoder extends AbstractGeocoder {
	
	// http://wiki.openstreetmap.org/wiki/DE:Key:amenity
	private static String[] OSM_TAGS = new String[] {   // amenities
		                                                      "bar", "bbq",  "biergarten", "cafe", "drinking_water", "fast_food", "food_court", "ice_cream", "pub", "restaurant",
		                                                      "college", "kindergarten", "library", "public_bookcase", "school", "music_school", "driving_school", "language_school", "university",
		                                                      "bicycle_parking", "bicycle_repair_station", "bicycle_rental", "boat_sharing", "bus_station", "car_rental", "car_sharing", "car_wash", "charging_station", "ferry_terminal", "fuel", "grit_bin", "motorcycle_parking", "parking", " 	parking_entrance", "parking_space", "taxi",
		                                                      "atm", "bank", "bureau_de_change",
                                                              "baby_hatch", "clinic", "dentist", "doctors", "hospital", "nusing_home", "pharmacy", "social_facility", "veterinary", "blood_donation",
                                                              "arts_centre", "brothel", "casino", "cinema", "community_centre", "fountain", "gambling", "nightclub", "planetarium", "social_centre", "stripclub", "studio",  "swingerclub", "theatre",
                                                              "animal_boarding", "animal_shelter", "bench", "clock", "courthouse", "coworking_space", "crematorium", "crypt", "dive_centre", "dojo", "embassy", "fire_station", "game_feeding", "grave_yard", "gym", "hunting_stand", "internet_cafe" ,"kneipp_water_cure", "marketplace", "photo_booth", "place_of_worship", "police", "post_box", "post_office", "prison", "ranger_station", "recycling", "rescue_station", "sauna", "shelter", "shower", "telephone", "toilets", "townhall", "vending_machine", "waste_basket", "waste_disposal", "waste_transfer_station", "watering_place", "water_point",
                                                        // shops
                                                              "alcohol", "bakery", "beverages", "brewing_supplies", "butcher", "cheese", "chocolate", "coffee", "confectionery", "convenience", "deli", "dairy", "farm", "greengrocer", "pasta", "pastry", "seafood", "tea", "wine", "department_store", "general", "kiosk", "mall", "supermarket",
                                                              "baby_goods", "bag", "boutique", "clothes", "fabric", "fashion", "jewelry", "leather", "shoes", "tailor", "watches", "charity", "second_hand", "variety_store", "beauty", "chemist", "cosmetics", "erotic", "hairdresser", "hearing_aids", "herbalist", "massage", "medical_supply", "nutrition_supplements", "optician", "perfumery", "tattoo", 
                                                              "bathroom_furnishing", "doityourself", "electrical", "energy", "fireplace", "florist", "garden_centre", "garden_furniture", "gas", "glaziery", "hardware", "houseware", "locksmith", "paint", "trade", "antiques", "bed", "candles", "carpet", "curtain", "furniture", "interior_decoration", "kitchen", "lamps", "window_blind", "computer", 
                                                              "electronics", "hifi", "mobile_phone", "radiotechnics", "vacuum_cleaner", "bicycle", "car", "car_repair", "car_parts", "fishing", "free_flying", "hunting", "motorcycle", "outdoor", "scuba_diving", "sports", "tyres", "swimming_pool", "art", "craft", "frame", "games", "model", "music", "musical_instrument", "photo", "trophy", "video", 
                                                              "video_games", "anime", "books", "gift", "lottery", "newsagent", "stationery", "ticket",  "copyshop", "dry_cleaning", "e-cigarette", "funeral_directors", "laundry", "money_lender", "pawnbroker", "pet", "pyrotechnics", "religion", "tobacco", "toys", "travel_agency", "vacant", "weapons"
                                                              
	                                                        };

	public NominatimGeocoder(String geocodingURL, String reverseGeocodingURL, String userAgent) {
		super(geocodingURL, reverseGeocodingURL, userAgent);
		// TODO Auto-generated constructor stub
	}
	
	public GeocodingResult[] geocode(Address address, String languages, SearchBoundary boundary, int limit) throws Exception
	{
		throw new Exception("Structured geocoding is not supported.");
	}

    public GeocodingResult[] geocode(String address, String languages, SearchBoundary boundary, int limit) throws Exception
    {
		ArrayList<GeocodingResult> res = new ArrayList<GeocodingResult>();

    	String code = languages;
    	if (code == null) {
    		code = "de,en,nl";
		}
		code = code.toLowerCase();
		String reqParams = "";

		if (code.contains("eu,")) {
			// http://nominatim.openstreetmap.org/search?format=json&polygon=0&addressdetails=1&viewbox=-31.303,71.869,50.455,34.09&bounded=1&q=[...]
			code = code.replace("eu,", "");
			reqParams = "?limit=" + limit + "&accept-language=" + code
					+ "&addressdetails=1&format=json&viewbox=-31.303,71.869,50.455,34.09&bounded=1&q=";
		} else {
			reqParams = "?limit=" + limit + "&accept-language=" + code
					+ "&addressdetails=1&format=json&q=";
		}
		
		String freeFormAddress = GeocodingUtils.sanitizeAddress(address);
		reqParams += URLEncoder.encode(freeFormAddress, "UTF-8");

		String respContent = getResponseFromNominatim(geocodingURL, "http://open.mapquestapi.com/nominatim/v1/search.php", reqParams);

		if (!Helper.isEmpty(respContent) && !respContent.equals("[]")) {
			JSONArray arr = new JSONArray(respContent);
			for (int j = 0; j < arr.length(); j++) {
				JSONObject obj = arr.getJSONObject(j);

				// Get Lon/Lat
				String lon = obj.get("lon").toString();
				String lat = obj.get("lat").toString();
				
				if (boundary != null && !boundary.contains(Double.parseDouble(lon), Double.parseDouble(lat)))
					continue;					

				// Get Address Details
				JSONObject addressObj = (JSONObject) ((JSONObject) obj).get("address");

				String country_code = getJSONValue(addressObj, "country_code");
				String country = getJSONValue(addressObj, "country");
				String county = getJSONValue(addressObj, "county");
				String region = getJSONValue(addressObj, "state");
				String state_district = getJSONValue(addressObj, "state_district");
				// String boundary = null;
				// if(address.containsKey("boundary")){ boundary =
				// address.get("boundary").toString(); }
				String postal_code = getJSONValue(addressObj, "postcode");
				String locality = getJSONValue(addressObj, "city");
				
				if (Helper.isEmpty(locality)) {
					locality = getJSONValue(addressObj, "town");
				}
				if (Helper.isEmpty(locality)) {
					locality = getJSONValue(addressObj, "village");
				}
				if (Helper.isEmpty(locality)) {
					locality = getJSONValue(addressObj, "hamlet");
				} 
				//String suburb = getJSONValue(addressObj, "suburb");
				String road = getJSONValue(addressObj, "road");
				
				if (Helper.isEmpty(road))
					road = getJSONValue(addressObj, "footway");
				else if (Helper.isEmpty(road))
					road = getJSONValue(addressObj, "pedestrian");
				else if (Helper.isEmpty(road))
					road = getJSONValue(addressObj, "cycleway");
				else if (Helper.isEmpty(road))
					road = getJSONValue(addressObj, "bridleway");
			
				String house_number = getJSONValue(addressObj, "house_number");
				String house = getJSONValue(addressObj, "house");
				String object_name = null;

				String classType = null;
				if (((JSONObject) obj).get("class").toString() != null) {
					classType = "class:" + ((JSONObject) obj).get("class").toString();
				}
				if (((JSONObject) obj).get("type").toString() != null) {
					String type =  ((JSONObject) obj).get("type").toString();

					object_name = getJSONValue(addressObj, type);
					
					if (classType != null) {
						classType = classType + ";type:" + type;
					} else {
						classType = "type:" + type;
					}
				}
				// Only return the address, if a city or street name
				// is available
				if (country != null || region != null || state_district != null || locality != null || road != null) {
					GeocodingResult gr = new GeocodingResult();
					gr.locality = locality;
					gr.country = country;
					gr.countryCode = country_code;
					gr.county = county;
					gr.region = region;
					//gr.stateDistrict = state_district;
					gr.postalCode = postal_code;
					gr.street = road;
					gr.name = house;
					gr.houseNumber = house_number;
					gr.objectName = object_name;
					gr.longitude = Double.parseDouble(lon);
					gr.latitude = Double.parseDouble(lat);
					
					res.add(gr);
				}
			}
		}
		
		return res.toArray(new GeocodingResult[res.size()]);
    }
    
	private String getResponseFromNominatim(String service, String service2, String reqParams) throws Exception
	{
		String result = null;
		
		try
		{
			result = HTTPUtility.getResponse(service + reqParams, 5000, userAgent, "UTF-8");
		}
		catch(Exception ex)
		{
			
		}
		
		if (result == null && !Helper.isEmpty(service2))
		{
			result = HTTPUtility.getResponse(service2 + reqParams, 5000, userAgent, "UTF-8"); 
		}
		
		return result;
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
	
	private String getJSONValue(JSONObject obj, String[] props)
	{
		for (int i = 0; i < props.length; i++)
		{
			String value = getJSONValue(obj, props[i]);
			if (!Helper.isEmpty(value))
				return value;
		}
		
		return null;
	}
	
	public GeocodingResult[] reverseGeocode(double lon, double lat, int limit) throws Exception
	{
		GeocodingResult gr = new GeocodingResult();		

		String reqParams = "?limit=" + limit + "&addressdetails=1&format=json&zoom=18&accept-language=en";
		reqParams += "&lon=" + lon + "&lat=" + lat;

		String respContent = getResponseFromNominatim(reverseGeocodingURL, "http://open.mapquestapi.com/nominatim/v1/reverse.php", reqParams);

		// Parse Nominatim Response
		JSONObject obj = new JSONObject(respContent);

		// Get Lon/Lat
		String slon = ((JSONObject) obj).get("lon").toString();
		String slat = ((JSONObject) obj).get("lat").toString();
		
		gr.latitude = Double.parseDouble(slat);
		gr.longitude = Double.parseDouble(slon);

		// Get Address Details
		JSONObject address = (JSONObject) ((JSONObject) obj).get("address");
		gr.countryCode = getJSONValue(address, "country_code");
		gr.country = getJSONValue(address, "country");
		gr.postalCode = getJSONValue(address, "postcode");
		gr.county = getJSONValue(address, "county");
		gr.region = getJSONValue(address, "state");
		//gr.stateDistrict = getJSONValue(address, "state_district");
		//String boundary = getJSONValue(address, "boundary");
		gr.locality = getJSONValue(address, "city");

		if (Helper.isEmpty(gr.locality)) {
			gr.locality = getJSONValue(address, "town");
		}

		if (Helper.isEmpty(gr.locality)) {
			gr.locality = getJSONValue(address, "village");
		}
		if (Helper.isEmpty(gr.locality)) {
			gr.locality = getJSONValue(address, "hamlet");
		}
		gr.neighbourhood = getJSONValue(address, "suburb");
		gr.street = getJSONValue(address, "road");
		gr.houseNumber = getJSONValue(address, "house_number");
		gr.name = getJSONValue(address, "house");
		gr.objectName = getJSONValue(address, OSM_TAGS);
		
		double dist = Helper.DIST_EARTH.calcDist(lat, lon, gr.latitude, gr.longitude);
		gr.confidence = (float)GeocodingUtils.getDistanceAccuracyScore(dist);

		return new GeocodingResult[] { gr };
	}
}
