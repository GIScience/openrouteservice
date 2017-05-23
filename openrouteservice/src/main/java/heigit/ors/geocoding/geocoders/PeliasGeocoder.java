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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Envelope;

import heigit.ors.util.HTTPUtility;

public class PeliasGeocoder extends AbstractGeocoder
{
	public PeliasGeocoder(String geocodingURL, String reverseGeocodingURL, String userAgent) {
		super(geocodingURL, reverseGeocodingURL, userAgent);

	}

	@Override
	public GeocodingResult[] geocode(String address, String languages, SearchBoundary searchBoundary, int limit)
			throws UnsupportedEncodingException, IOException {
		String reqParams = "?text=" + URLEncoder.encode(GeocodingUtils.sanitizeAddress(address), "UTF-8") + "&size=" + limit + "&lang=" + "en"; // fix language
		if (searchBoundary != null)
		{
			if (searchBoundary instanceof RectSearchBoundary)
			{
				RectSearchBoundary rsb = (RectSearchBoundary)searchBoundary;
				Envelope env = rsb.getRectangle();
				reqParams += "&boundary.rect.min_lat=" + env.getMinY() + "&boundary.rect.min_lon=" + env.getMinX() + "&boundary.rect.max_lat=" + env.getMaxY() + "&boundary.rect.max_lon=" + env.getMaxX();
			}
			else if (searchBoundary instanceof CircleSearchBoundary)
			{
				CircleSearchBoundary csb = (CircleSearchBoundary)searchBoundary;
				reqParams += "&boundary.circle.lat=" + csb.getLatitude() + "&boundary.circle.lon=" + csb.getLongitude() + "&boundary.circle.radius=" + csb.getRadius();
			}
		}
		String respContent = HTTPUtility.getResponse(geocodingURL + reqParams, 10000, userAgent, "UTF-8");

		if (!Helper.isEmpty(respContent) && !respContent.equals("[]")) {

			return getGeocodeResults(respContent, searchBoundary);
		}

		return null;
	}

	@Override
	public GeocodingResult[] reverseGeocode(double lon, double lat, int limit) throws IOException {
		String reqParams = "?point.lat=" + lat  + "&point.lon=" + lon + "&size=" + limit;
		String respContent = HTTPUtility.getResponse(reverseGeocodingURL + reqParams, 10000, userAgent, "UTF-8");

		if (!Helper.isEmpty(respContent) && !respContent.equals("[]")) {
			return getGeocodeResults(respContent, null);
		}

		return null;
	}

	private GeocodingResult[] getGeocodeResults(String respContent, SearchBoundary searchBoundary)
	{
		JSONObject features = new JSONObject(respContent);
		JSONArray arr = (JSONArray)features.get("features");

		GeocodingResult[] results = new GeocodingResult[arr.length()];

		for (int j = 0; j < arr.length(); j++) 
		{
			JSONObject feature = arr.getJSONObject(j);
			JSONObject geom = feature.getJSONObject("geometry");
			JSONArray coords = geom.getJSONArray("coordinates");
			double lon = Double.parseDouble(coords.get(0).toString());
			double lat = Double.parseDouble(coords.get(1).toString());
			
			if (searchBoundary != null && !searchBoundary.contains(lon, lat))
				continue;

			JSONObject props = feature.getJSONObject("properties");
			
			String country = props.optString("country");
			String state = props.optString("region");
			String county = props.optString("county");
			String street = props.optString("street");
			String city = props.optString("locality");
			
			float accuracy = (float)props.getDouble("confidence");
			
			if (state != null || county != null || city != null || street != null)
            {
				GeocodingResult gr = new GeocodingResult();
				gr.city = city;
				gr.country = country;
				gr.countryCode = props.optString("country_a");
				gr.state = state;
				//gr.stateDistrict = state_district;
				gr.county = county;
				gr.postalCode = props.optString("postalcode");
				gr.street = street;
				gr.neighbourhood = props.optString("neighbourhood");
				gr.name = props.optString("name");
				gr.houseNumber = props.optString("housenumber");
				gr.longitude = lon;
				gr.latitude = lat;
				gr.accuracy = accuracy;
				
				results[j] = gr;
			}
		}

		Arrays.sort(results, new GeocodingResultComparator());
		
		return results;
	}
}
