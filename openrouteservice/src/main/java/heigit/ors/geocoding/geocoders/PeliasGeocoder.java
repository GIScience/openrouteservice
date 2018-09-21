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
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import heigit.ors.exceptions.MissingParameterException;
import heigit.ors.util.HTTPUtility;
import heigit.ors.util.LocaleUtility;
import heigit.ors.util.StringUtility;

public class PeliasGeocoder extends AbstractGeocoder
{	
	private static final int RESPONSE_TIMEOUT = 1000;

	public PeliasGeocoder(String geocodingURL, String reverseGeocodingURL, String userAgent) {
		super(geocodingURL, reverseGeocodingURL, userAgent);
	}

	@Override
	public GeocodingResult[] geocode(Address address, String languages, SearchBoundary searchBoundary, int limit)
			throws Exception {
		String lang = Helper.isEmpty(languages) ? "en" : languages;
		String reqParams = "/structured?";
		// Now look at the address element and get the properties from it
		String addrCriteria = "";

		// Get the parts so we dont have to call the address object multiple times for each
		String addAddress = address.getAddress();
		String addNeighb = address.getNeighbourhood();
		String addBorough = address.getBorough();
		String addLocality = address.getLocality();
		String addCounty = address.getCounty();
		String addRegion = address.getRegion();
		String addPostal = address.getPostalcode();
		String addCountry = address.getCountry();

		if(!Helper.isEmpty(addAddress))
			addrCriteria = "address="
			.concat(URLEncoder.encode(GeocodingUtils.sanitizeAddress(addAddress), "UTF-8"));
		if(!Helper.isEmpty(addNeighb))
			addrCriteria = addrCriteria.concat("&neighbourhood=")
			.concat(URLEncoder.encode(GeocodingUtils.sanitizeAddress(addNeighb), "UTF-8"));
		if(!Helper.isEmpty(addBorough))
			addrCriteria = addrCriteria.concat("&borough=")
			.concat(URLEncoder.encode(GeocodingUtils.sanitizeAddress(addBorough), "UTF-8"));
		if(!Helper.isEmpty(addLocality))
			addrCriteria = addrCriteria.concat("&locality=")
			.concat(URLEncoder.encode(GeocodingUtils.sanitizeAddress(addLocality), "UTF-8"));
		if(!Helper.isEmpty(addCounty))
			addrCriteria = addrCriteria.concat("&county=")
			.concat(URLEncoder.encode(GeocodingUtils.sanitizeAddress(addCounty), "UTF-8"));
		if(!Helper.isEmpty(addRegion))
			addrCriteria = addrCriteria.concat("&region=")
			.concat(URLEncoder.encode(GeocodingUtils.sanitizeAddress(addRegion), "UTF-8"));
		if(!Helper.isEmpty(addPostal))
			addrCriteria = addrCriteria.concat("&postalcode=")
			.concat(URLEncoder.encode(GeocodingUtils.sanitizeAddress(addPostal), "UTF-8"));
		if(!Helper.isEmpty(addCountry))
			addrCriteria = addrCriteria.concat("&country=")
			.concat(URLEncoder.encode(GeocodingUtils.sanitizeAddress(addCountry), "UTF-8"));

		// remove the leading &
		if(addrCriteria.startsWith("&"))
			addrCriteria = addrCriteria.substring(1);

		if(!addrCriteria.isEmpty() && addrCriteria != "")
			reqParams = reqParams + addrCriteria + "&size=" + limit + "&lang=" + lang;
		else
			throw new MissingParameterException(GeocodingErrorCodes.INVALID_PARAMETER_VALUE, "address, neighbourhood, borough, locality, county, region, postalcode or country");

		reqParams = applySearchBoundary(reqParams, searchBoundary);

		String respContent = HTTPUtility.getResponse(geocodingURL + reqParams, RESPONSE_TIMEOUT, userAgent);
		if (!Helper.isEmpty(respContent) && !respContent.equals("[]")) 
			return getGeocodeResults(respContent, searchBoundary, null);
		else
			return null;
	}

	@Override
	public GeocodingResult[] geocode(String address, String languages, SearchBoundary searchBoundary, int limit)
			throws Exception {
		String lang = Helper.isEmpty(languages) ? "en": languages;

		String reqParams = "?text=" + URLEncoder.encode(GeocodingUtils.sanitizeAddress(address), "UTF-8") + "&size=" + limit + "&lang=" + lang; 
		reqParams = applySearchBoundary(reqParams, searchBoundary);

		GeocodingResult[] result = null;

		String respContent = HTTPUtility.getResponse(geocodingURL + reqParams, RESPONSE_TIMEOUT, userAgent);
		if (!Helper.isEmpty(respContent) && !respContent.equals("[]")) 
			result = getGeocodeResults(respContent, searchBoundary, null);

		if (result != null && result.length > 0)
		{ 
			// if the result of a request with all layers is quite poor, we try to narrow down the search by specifying layers
			// Example: http://localhost:8082/openrouteservice-4.2.0/geocode?lang=en&limit=20&query=Hauptwasen,+Balingen
			if (result[0].confidence <= 0.75)
			{
				boolean hasNumbers = StringUtility.containsDigit(address);

				GeocodingResult[] result2 = null;
				String layers = "venue,street";
				if (hasNumbers)
					layers += ",address";

				respContent = HTTPUtility.getResponse(geocodingURL + reqParams + "&layers=" + layers, RESPONSE_TIMEOUT, userAgent, "UTF-8");
				if (!Helper.isEmpty(respContent) && !respContent.equals("[]")) 
					result2 = getGeocodeResults(respContent, searchBoundary, null);

				if (result2 != null && result2.length > 0)
				{
					if (result[0].confidence < result2[0].confidence)
					{
						String locality = result[0].locality;
						String postalCode = result[0].postalCode;
						// assign locality and postal code from less fine search 
						if (locality != null && locality.length() > 0 || (postalCode != null && postalCode.length() > 0))
						{
							double locLon = result[0].longitude;
							double locLat = result[0].latitude;
							DistanceCalc distCalc = Helper.DIST_EARTH;

							for (int i = 0; i < result2.length; i++)
							{
								GeocodingResult gr = result2[i];
								if (Helper.isEmpty(gr.locality))
								{
									double dist = distCalc.calcDist(locLat, locLon, gr.latitude, gr.longitude);
									if (dist < 5000)
									{
										gr.locality = locality;
										if (gr.postalCode.length() == 0)
											gr.postalCode = postalCode;
									}
								}
							}
						}

						result = result2;
					}
				}
			}
		}

		return result;
	}

	private String applySearchBoundary(String reqParams, SearchBoundary searchBoundary)
	{
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

		return reqParams;
	}

	@Override
	public GeocodingResult[] reverseGeocode(double lon, double lat, int limit) throws Exception {
		String reqParams = "?point.lat=" + lat  + "&point.lon=" + lon + "&size=" + limit;
		String respContent = HTTPUtility.getResponse(reverseGeocodingURL + reqParams, RESPONSE_TIMEOUT, userAgent);

		if (!Helper.isEmpty(respContent) && !respContent.equals("[]")) 
			return getGeocodeResults(respContent, null, new Coordinate(lon, lat));
		else
			return null;
	}

	private GeocodingResult[] getGeocodeResults(String respContent, SearchBoundary searchBoundary, Coordinate loc)
	{
		JSONObject features = new JSONObject(respContent);
		JSONArray arr = (JSONArray)features.get("features");

		GeocodingResult[] results = new GeocodingResult[arr.length()];
		int k = 0;
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

			String country = props.optString("country"); // places that issue passports, nations, nation-states
			String region = props.optString("region");   //states and provinces
			String county = props.optString("county"); // official governmental area; usually bigger than a locality, almost always smaller than a region
			String municipality = props.optString("localadmin");  // localadmin 	local administrative boundaries
			String street = props.optString("street");
			String locality = props.optString("locality");

			if (!Helper.isEmpty(region) && Helper.isEmpty(county) && props.has("macroregion")) // a related group of regions. Mostly in Europe
			{
				county = region;
				region = props.optString("macroregion");
			}

			if (locality != null && locality.equals(county))
				county = props.optString("macrocounty");

			if (region != null || county != null || locality != null || street != null)
			{
				GeocodingResult gr = new GeocodingResult();

				gr.confidence = (float)props.getDouble("confidence");
				
				if (loc != null)
				{
					gr.distance = Helper.DIST_EARTH.calcDist(lat, lon, loc.y, loc.x);
					gr.confidence = (float)GeocodingUtils.getDistanceAccuracyScore(gr.distance);
				}

				gr.locality = locality;
				gr.municipality = municipality;
				gr.country = country;
				gr.countryCode = LocaleUtility.getISO2CountryFromISO3(props.optString("country_a"));
				gr.region = region;
				gr.county = county;
				gr.postalCode = props.optString("postalcode");
				gr.street = street;
				gr.neighbourhood = props.optString("neighbourhood");  // social communities, neighbourhoods
				gr.borough = props.optString("borough"); // a local administrative boundary, currently only used for New York City. also in Berlin
				gr.name = props.optString("name");
				gr.houseNumber = props.optString("housenumber");
				gr.longitude = lon;
				gr.latitude = lat;
				gr.placeType = props.optString("layer");

				results[k] = gr;
			}
			
			k++;
		}

		Arrays.sort(results, new GeocodingResultComparator());

		return results;
	}
}
