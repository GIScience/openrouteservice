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
package heigit.ors.services.isochrones.requestprocessors.json;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.common.StatusCode;
import heigit.ors.common.TravelRangeType;
import heigit.ors.common.TravellerInfo;
import heigit.ors.exceptions.MissingParameterException;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.isochrones.IsochroneRequest;
import heigit.ors.isochrones.IsochronesErrorCodes;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.services.isochrones.IsochronesServiceSettings;
import heigit.ors.util.CoordTools;
import heigit.ors.util.StreamUtility;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.Arrays;

public class JsonIsochroneRequestParser {

	public static IsochroneRequest parseFromStream(InputStream stream) throws Exception 
	{
		JSONObject json = null;
		try {
			String body = StreamUtility.readStream(stream);
			json = new JSONObject(body);
		} catch (Exception ex) {
			throw new StatusCodeException(StatusCode.BAD_REQUEST, IsochronesErrorCodes.INVALID_JSON_FORMAT, "Unable to parse JSON document.");
		}

		IsochroneRequest req = new IsochroneRequest();
		
		String value = null;
		
		if (json.has("travellers"))
		{
			JSONArray jTravellers = json.getJSONArray("travellers");
			
			if (jTravellers.length() == 0)
				throw new MissingParameterException(IsochronesErrorCodes.INVALID_JSON_FORMAT, "'travellers' array is empty.");
			
			for (int j = 0; j < jTravellers.length(); ++j)
			{
				JSONObject jTraveller = jTravellers.getJSONObject(j);
				
				TravellerInfo travellerInfo = new TravellerInfo();
				
				value = jTraveller.optString("profile");
				if (!Helper.isEmpty(value))
				{
					int profileType = RoutingProfileType.getFromString(value);
					if (profileType == RoutingProfileType.UNKNOWN)
						throw new UnknownParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "profile", value);
					travellerInfo.getRouteSearchParameters().setProfileType(profileType);
				}
				else
				{
					throw new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, "profile");
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
						throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, "location");
					}
				}
				else
				{
					throw new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, "location");
				}

				value = jTraveller.optString("location_type");
				if (!Helper.isEmpty(value))
				{
					if (!"start".equalsIgnoreCase(value) && !"destination".equalsIgnoreCase(value))
						throw new UnknownParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "location_type", value);

					travellerInfo.setLocationType(value);
				}
				
				if (jTraveller.has("range"))
				{
					JSONArray jRanges = jTraveller.getJSONArray("range");
					
					if (jRanges.length() == 0)
						throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, "range");

					double[] ranges = new double[jRanges.length()];

					try
					{
						for (int i = 0; i < ranges.length; i++)
							ranges[i] = jRanges.getDouble(i);
					}
					catch(Exception ex)
					{
						throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, "range");
					}

					Arrays.sort(ranges);

					travellerInfo.setRanges(ranges);
				}
				else
					throw new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, "range");
				
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
						throw new UnknownParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "range_type", value);
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
						throw new ParameterValueException(IsochronesErrorCodes.INVALID_JSON_FORMAT, "options", value);
					}
				}
				
				req.addTraveller(travellerInfo);
			}
		}
		else
			throw new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, "travellers");
		
		value = json.optString("units");
		if (!Helper.isEmpty(value))
		{
			if (!("m".equals(value) || "km".equals(value) || "mi".equals(value)))
					throw new UnknownParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "units", value);

			req.setUnits(value.toLowerCase());
		}


        value = json.optString("area_units");
        if (!Helper.isEmpty(value)) {
            if (!("m".equals(value) || "km".equals(value) || "mi".equals(value)))
                throw new UnknownParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "area_units", value);

            req.setUnits(value.toLowerCase());
        }
		
		value = json.optString("calc_method");
		if (!Helper.isEmpty(value))
			req.setCalcMethod(value);

		value = json.optString("attributes");
		if (!Helper.isEmpty(value))
		{
			String[] values = value.split("\\|");
			for (int i = 0; i < values.length; i++)
			{
				String attr = values[i];
				if (!(attr.equalsIgnoreCase("area") || attr.equalsIgnoreCase("reachfactor") || IsochronesServiceSettings.isStatsAttributeSupported(attr)))
					throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "attributes", attr);
			}
			
			req.setAttributes(values);
		}

		value = json.optString("intersections");
		if (!Helper.isEmpty(value))
		{
			try
			{
				req.setIncludeIntersections(Boolean.parseBoolean(value));
			}
			catch(Exception ex)
			{
				throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "intersections", value);
			}
		}

		setIsochroneSmoothing(req, json.optString("smoothing"));
		
		value = json.optString("id");
		if (!Helper.isEmpty(value))
			req.setId(value);

		return req;
	}

	public static IsochroneRequest parseFromRequestParams(HttpServletRequest request) throws Exception
	{
		IsochroneRequest req = new IsochroneRequest();

		TravellerInfo travellerInfo = new TravellerInfo();
		
		String value = request.getParameter("profile");
		if (!Helper.isEmpty(value))
		{
			int profileType = RoutingProfileType.getFromString(value);
			if (profileType == RoutingProfileType.UNKNOWN)
				throw new UnknownParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "profile", value);
			travellerInfo.getRouteSearchParameters().setProfileType(profileType);
		}
		else
		{
			throw new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, "profile");
		}

		double rangeValue = -1.0;
		boolean skipInterval = false;
		value = request.getParameter("range");
		if (Helper.isEmpty(value))
			throw new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, "range");
		else
		{
			String[] rangeValues = value.split(",");

			if (rangeValues.length == 1)
			{
				try
				{
					rangeValue = Double.parseDouble(value);
					travellerInfo.setRanges(new double[] { rangeValue});
				}
				catch(NumberFormatException ex)
				{
					throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, "range");
				}
			}
			else
			{
				double[] ranges = new double[rangeValues.length];
				double maxRange = Double.MIN_VALUE;
				for (int i = 0; i < ranges.length; i++)
				{
					double dv = Double.parseDouble(rangeValues[i]);
					if (dv > maxRange)
						maxRange = dv;
					ranges[i] = dv;
				}

				Arrays.sort(ranges);

				travellerInfo.setRanges(ranges);
				
				skipInterval = true;
			}
		}

		if (!skipInterval)
		{
			value = request.getParameter("interval");
			if (!Helper.isEmpty(value))
			{
				if (rangeValue != -1)
				{
					travellerInfo.setRanges(rangeValue, Double.parseDouble(value));
				}
			}
		}

		value = request.getParameter("range_type");
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
				throw new UnknownParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "range_type", value);
			}
		}

        value = request.getParameter("area_units");
        if (!Helper.isEmpty(value)) {


            if (!("m".equals(value) || "km".equals(value) || "mi".equals(value)))
                throw new UnknownParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "area_units", value);


            req.setAreaUnits(value.toLowerCase());
        }

        value = request.getParameter("units");
        if (!Helper.isEmpty(value)) {


            if (!("m".equals(value) || "km".equals(value) || "mi".equals(value)))
                throw new UnknownParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "units", value);


            req.setUnits(value.toLowerCase());
        }


        boolean inverseXY = false;
		value = request.getParameter("locations");

		if (Helper.isEmpty(value))
		{
			value = request.getParameter("latlng");
			inverseXY = true;
		}

		Coordinate[] coords = null;
		if (!Helper.isEmpty(value))
		{
			try
			{
				coords = CoordTools.parse(value, "\\|", false, inverseXY);						
			}
			catch(NumberFormatException nfex)
			{
				throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, "locations");
			}
		}
		else
		{
			throw new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, "locations");
		}

		value = request.getParameter("location_type");
		if (!Helper.isEmpty(value))
		{
			if (!"start".equalsIgnoreCase(value) && !"destination".equalsIgnoreCase(value))
				throw new UnknownParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "location_type", value);

			travellerInfo.setLocationType(value);
		}

		value = request.getParameter("calc_method");
		if (!Helper.isEmpty(value))
			req.setCalcMethod(value);

		value = request.getParameter("attributes");
		if (!Helper.isEmpty(value))
		{
			String[] values = value.split("\\|");
			for (int i = 0; i < values.length; i++)
			{
				String attr = values[i];
				if (!(attr.equalsIgnoreCase("area") || attr.equalsIgnoreCase("reachfactor") || IsochronesServiceSettings.isStatsAttributeSupported(attr)))
					throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "attributes", attr);
			}
			
			req.setAttributes(values);
		}

		value = request.getParameter("intersections");
		if (!Helper.isEmpty(value))
		{
			try
			{
				req.setIncludeIntersections(Boolean.parseBoolean(value));
			}
			catch(Exception ex)
			{
				throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "intersections", value);
			}
		}

		value = request.getParameter("options");
		if (!Helper.isEmpty(value))
		{
			try
			{
				travellerInfo.getRouteSearchParameters().setOptions(value);
			}
			catch(Exception ex)
			{
				throw new ParameterValueException(IsochronesErrorCodes.INVALID_JSON_FORMAT, "options", value);
			}
		}

		setIsochroneSmoothing(req, request.getParameter("smoothing"));
		
		if (coords.length == 1)
		{
			travellerInfo.setLocation(coords[0]);
			req.addTraveller(travellerInfo);
		}
		else
		{
			travellerInfo.setLocation(coords[0]);
			req.addTraveller(travellerInfo);

			for (int i = 1; i < coords.length; i++)
			{
				TravellerInfo ti = travellerInfo.clone();
				ti.setLocation(coords[i]);
				req.addTraveller(ti);
			}
		}

		value = request.getParameter("id");
		if (!Helper.isEmpty(value))
			req.setId(value);

		return req;
	}

	private static void setIsochroneSmoothing(IsochroneRequest isochroneRequest, String requestSmoothingValue) throws ParameterValueException {
		if (!Helper.isEmpty(requestSmoothingValue)) {
			float smoothingValue;
			try {
				smoothingValue = Float.parseFloat(requestSmoothingValue);
			} catch (Exception e) {
				throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "smoothing", requestSmoothingValue);
			}

			if(smoothingValue < 0 || smoothingValue > 100)
				throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "smoothing", requestSmoothingValue);

			isochroneRequest.setSmoothingFactor(smoothingValue);
		}
	}
}
