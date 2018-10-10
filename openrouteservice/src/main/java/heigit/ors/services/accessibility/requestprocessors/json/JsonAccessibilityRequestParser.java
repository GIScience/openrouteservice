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
 *//*

package heigit.ors.services.accessibility.requestprocessors.json;

import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.accessibility.AccessibilityErrorCodes;
import heigit.ors.accessibility.AccessibilityRequest;
import heigit.ors.common.NamedLocation;
import heigit.ors.common.StatusCode;
import heigit.ors.common.TravelRangeType;
import heigit.ors.common.TravellerInfo;
import heigit.ors.exceptions.MissingParameterException;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.locations.LocationsRequest;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.services.accessibility.AccessibilityServiceSettings;
import heigit.ors.util.StreamUtility;

public class JsonAccessibilityRequestParser {
	public static AccessibilityRequest parseFromStream(InputStream stream) throws Exception 
	{
		JSONObject json = null;
		try {
			String body = StreamUtility.readStream(stream);
			json = new JSONObject(body);
		} catch (Exception ex) {
			throw new StatusCodeException(StatusCode.BAD_REQUEST, AccessibilityErrorCodes.INVALID_JSON_FORMAT, "Unable to parse JSON document.");
		}
		
		AccessibilityRequest req = new AccessibilityRequest();
		
		String value = null;
		
		if (json.has("places"))
		{
			JSONObject jPlaces = json.getJSONObject("places");
			
			if (jPlaces.has("pois"))
			{
				JSONObject jPoiSearch = jPlaces.getJSONObject("pois");
				LocationsRequest locRequest = parseLocationRequest(jPoiSearch);
				
                req.setLocationsRequest(locRequest);
			}
			else if (jPlaces.has("custom"))
			{
				JSONObject jCustomPoints = jPlaces.getJSONObject("custom");
				NamedLocation[] userLocations = parsedUserPoints(jCustomPoints);
				
				if (userLocations.length == 0)
					throw new MissingParameterException(AccessibilityErrorCodes.INVALID_PARAMETER_VALUE, "places->custom is empty.");
				
				req.setUserLocations(userLocations);
			}
			else
				throw new MissingParameterException(AccessibilityErrorCodes.MISSING_PARAMETER, "pois/custom");
		}
		else
			throw new MissingParameterException(AccessibilityErrorCodes.MISSING_PARAMETER, "travellers");
		
		if (json.has("travellers"))
		{
			JSONArray jTravellers = json.getJSONArray("travellers");
			
			if (jTravellers.length() == 0)
				throw new MissingParameterException(AccessibilityErrorCodes.INVALID_JSON_FORMAT, "'travellers' array is empty.");
			
			for (int j = 0; j < jTravellers.length(); ++j)
			{
				JSONObject jTraveller = jTravellers.getJSONObject(j);
				
				TravellerInfo travellerInfo = new TravellerInfo();
				
				value = jTraveller.optString("profile");
				if (!Helper.isEmpty(value))
				{
					int profileType = RoutingProfileType.getFromString(value);
					if (profileType == RoutingProfileType.UNKNOWN)
						throw new UnknownParameterValueException(AccessibilityErrorCodes.INVALID_PARAMETER_VALUE, "profile", value);
					travellerInfo.getRouteSearchParameters().setProfileType(profileType);
				}
				else
				{
					throw new MissingParameterException(AccessibilityErrorCodes.MISSING_PARAMETER, "profile");
				}
				
				if (jTraveller.has("location"))
				{
					try
					{
						JSONArray jLocation = jTraveller.getJSONArray("location");
						travellerInfo.setLocation(new Coordinate(jLocation.getDouble(0), jLocation.getDouble(1)));						
					}
					catch(Exception nfex)
					{
						throw new ParameterValueException(AccessibilityErrorCodes.INVALID_PARAMETER_FORMAT, "location");
					}
				}
				else
				{
					throw new MissingParameterException(AccessibilityErrorCodes.MISSING_PARAMETER, "location");
				}

				value = jTraveller.optString("location_type");
				if (!Helper.isEmpty(value))
				{
					if (!"start".equalsIgnoreCase(value) && !"destination".equalsIgnoreCase(value))
						throw new UnknownParameterValueException(AccessibilityErrorCodes.INVALID_PARAMETER_VALUE, "location_type", value);

					travellerInfo.setLocationType(value);
				}
				
				value = json.optString("range");
				if (!Helper.isEmpty(value))
				{
					try
					{
						double range = Double.parseDouble(value);
						travellerInfo.setRanges(new double[] { range });
					}
					catch(NumberFormatException ex)
					{
						throw new ParameterValueException(AccessibilityErrorCodes.INVALID_PARAMETER_FORMAT, "range");
					}
				}
				else
					throw new MissingParameterException(AccessibilityErrorCodes.MISSING_PARAMETER, "range");
				
				value = jTraveller.optString("range_type");
				if (!Helper.isEmpty(value))
				{
					switch (value.toLowerCase())
					{
					case "distance":
						travellerInfo.setRangeType(TravelRangeType.Distance);
						break;
					case "time":
						travellerInfo.setRangeType(TravelRangeType.Time);
						break;
					default:
						throw new UnknownParameterValueException(AccessibilityErrorCodes.INVALID_PARAMETER_VALUE, "range_type", value);
					}
				}
				
				value = jTraveller.optString("options");
				if (!Helper.isEmpty(value))
				{
					try
					{
						travellerInfo.getRouteSearchParameters().setOptions(value);
					}
					catch(Exception ex)
					{
						throw new ParameterValueException(AccessibilityErrorCodes.INVALID_JSON_FORMAT, "options", value);
					}
				}
				
				req.addTraveller(travellerInfo);
			}
		}
		else
			throw new MissingParameterException(AccessibilityErrorCodes.MISSING_PARAMETER, "travellers");
		
		if (AccessibilityServiceSettings.getRouteDetailsAllowed())
		{
			value = json.optString("geometry");
			if (!Helper.isEmpty(value))
				req.setIncludeGeometry(Boolean.parseBoolean(value));

			value = json.optString("geometry_format");
			if (!Helper.isEmpty(value))
			{
				if (!("geojson".equalsIgnoreCase(value) || "polyline".equalsIgnoreCase(value) || "encodedpolyline".equalsIgnoreCase(value)))
					throw new UnknownParameterValueException(AccessibilityErrorCodes.INVALID_PARAMETER_VALUE, "geometry_format", value);

				req.setGeometryFormat(value);
			}
			
			value = json.optString("elevation");
			if (!Helper.isEmpty(value))
				req.setIncludeElevation(Boolean.parseBoolean(value));
		}
		
		value = json.optString("id");
		if (!Helper.isEmpty(value))
			req.setId(value);

		return req;
	}
	
	private static LocationsRequest parseLocationRequest(JSONObject jsonObj)
	{
		return null;
	}
	
	private static NamedLocation[] parsedUserPoints(JSONObject jsonObj)
	{
		return null;
	}
}
*/
