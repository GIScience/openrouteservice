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
package org.heigit.ors.services.isochrones.requestprocessors.json;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;
import org.heigit.ors.common.StatusCode;
import org.heigit.ors.common.TravelRangeType;
import org.heigit.ors.common.TravellerInfo;
import org.heigit.ors.exceptions.MissingParameterException;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.exceptions.UnknownParameterValueException;
import org.heigit.ors.isochrones.IsochroneRequest;
import org.heigit.ors.isochrones.IsochronesErrorCodes;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.services.isochrones.IsochronesServiceSettings;
import org.heigit.ors.util.CoordTools;
import org.heigit.ors.util.StreamUtility;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.Arrays;

public class JsonIsochroneRequestParser {

	public static final String KEY_TRAVELLERS = "travellers";
	public static final String KEY_PROFILE = "profile";
	public static final String KEY_LOCATION = "location";
	public static final String KEY_LOCATION_TYPE = "location_type";
	public static final String KEY_RANGE = "range";
	public static final String KEY_RANGE_TYPE = "range_type";
	public static final String KEY_OPTIONS = "options";
	public static final String KEY_UNITS = "units";
	public static final String KEY_AREA_UNITS = "area_units";
	public static final String KEY_CALC_METHOD = "calc_method";
	public static final String KEY_ATTRIBUTES = "attributes";
	public static final String KEY_INTERSECTIONS = "intersections";
	public static final String KEY_SMOOTHING = "smoothing";
	public static final String KEY_LOCATIONS = "locations";

	private  JsonIsochroneRequestParser() {}

	public static IsochroneRequest parseFromStream(InputStream stream) throws Exception {
		JSONObject json;
		try {
			String body = StreamUtility.readStream(stream);
			json = new JSONObject(body);
		} catch (Exception ex) {
			throw new StatusCodeException(StatusCode.BAD_REQUEST, IsochronesErrorCodes.INVALID_JSON_FORMAT, "Unable to parse JSON document.");
		}

		IsochroneRequest req = new IsochroneRequest();
		
		String value;
		
		if (json.has(KEY_TRAVELLERS)) {
			JSONArray jTravellers = json.getJSONArray(KEY_TRAVELLERS);
			
			if (jTravellers.length() == 0)
				throw new MissingParameterException(IsochronesErrorCodes.INVALID_JSON_FORMAT, "'travellers' array is empty.");
			
			for (int j = 0; j < jTravellers.length(); ++j) {
				JSONObject jTraveller = jTravellers.getJSONObject(j);
				
				TravellerInfo travellerInfo = new TravellerInfo();
				
				value = jTraveller.optString(KEY_PROFILE);
				if (!Helper.isEmpty(value)) {
					int profileType = RoutingProfileType.getFromString(value);
					if (profileType == RoutingProfileType.UNKNOWN)
						throw new UnknownParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, KEY_PROFILE, value);
					travellerInfo.getRouteSearchParameters().setProfileType(profileType);
				} else {
					throw new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, KEY_PROFILE);
				}
				
				if (jTraveller.has(KEY_LOCATION)) {
					try {
						JSONArray jLocation = jTraveller.getJSONArray(KEY_LOCATION);
						travellerInfo.setLocation(new Coordinate(jLocation.getDouble(0), jLocation.getDouble(1)));						
					} catch(Exception nfex) {
						throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, KEY_LOCATION);
					}
				} else {
					throw new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, KEY_LOCATION);
				}

				value = jTraveller.optString(KEY_LOCATION_TYPE);
				if (!Helper.isEmpty(value)) {
					if (!"start".equalsIgnoreCase(value) && !"destination".equalsIgnoreCase(value))
						throw new UnknownParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, KEY_LOCATION_TYPE, value);
					travellerInfo.setLocationType(value);
				}
				
				if (jTraveller.has(KEY_RANGE)) {
					JSONArray jRanges = jTraveller.getJSONArray(KEY_RANGE);
					
					if (jRanges.length() == 0)
						throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, KEY_RANGE);

					double[] ranges = new double[jRanges.length()];

					try {
						for (int i = 0; i < ranges.length; i++)
							ranges[i] = jRanges.getDouble(i);
					} catch(Exception ex) {
						throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, KEY_RANGE);
					}

					Arrays.sort(ranges);
					travellerInfo.setRanges(ranges);
				} else {
					throw new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, KEY_RANGE);
				}

				value = jTraveller.optString(KEY_RANGE_TYPE);
				if (!Helper.isEmpty(value)) {
					switch (value.toLowerCase()) {
						case "distance":
							travellerInfo.setRangeType(TravelRangeType.DISTANCE);
							break;
						case "time":
							travellerInfo.setRangeType(TravelRangeType.TIME);
							break;
						default:
							throw new UnknownParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, KEY_RANGE_TYPE, value);
					}
				}
				
				value = jTraveller.optString(KEY_OPTIONS);
				if (!Helper.isEmpty(value)) {
					try {
						travellerInfo.getRouteSearchParameters().setOptions(value);
					} catch(Exception ex) {
						throw new ParameterValueException(IsochronesErrorCodes.INVALID_JSON_FORMAT, KEY_OPTIONS, value);
					}
				}
				req.addTraveller(travellerInfo);
			}
		} else {
			throw new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, KEY_TRAVELLERS);
		}

		value = json.optString(KEY_UNITS);
		if (!Helper.isEmpty(value)) {
			if (!("m".equals(value) || "km".equals(value) || "mi".equals(value)))
					throw new UnknownParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, KEY_UNITS, value);
			req.setUnits(value.toLowerCase());
		}

        value = json.optString(KEY_AREA_UNITS);
        if (!Helper.isEmpty(value)) {
            if (!("m".equals(value) || "km".equals(value) || "mi".equals(value)))
                throw new UnknownParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, KEY_AREA_UNITS, value);
            req.setUnits(value.toLowerCase());
        }
		
		value = json.optString(KEY_CALC_METHOD);
		if (!Helper.isEmpty(value))
			req.setCalcMethod(value);

		value = json.optString(KEY_ATTRIBUTES);
		if (!Helper.isEmpty(value)) {
			String[] values = value.split("\\|");
			for (int i = 0; i < values.length; i++) {
				String attr = values[i];
				if (!(attr.equalsIgnoreCase("area") || attr.equalsIgnoreCase("reachfactor") || IsochronesServiceSettings.isStatsAttributeSupported(attr)))
					throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, KEY_ATTRIBUTES, attr);
			}
			req.setAttributes(values);
		}

		value = json.optString(KEY_INTERSECTIONS);
		if (!Helper.isEmpty(value)) {
			try {
				req.setIncludeIntersections(Boolean.parseBoolean(value));
			} catch(Exception ex) {
				throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, KEY_INTERSECTIONS, value);
			}
		}
		setIsochroneSmoothing(req, json.optString(KEY_SMOOTHING));
		
		value = json.optString("id");
		if (!Helper.isEmpty(value))
			req.setId(value);

		return req;
	}

	public static IsochroneRequest parseFromRequestParams(HttpServletRequest request) throws Exception {
		IsochroneRequest req = new IsochroneRequest();

		TravellerInfo travellerInfo = new TravellerInfo();
		
		String value = request.getParameter(KEY_PROFILE);
		if (!Helper.isEmpty(value)) {
			int profileType = RoutingProfileType.getFromString(value);
			if (profileType == RoutingProfileType.UNKNOWN)
				throw new UnknownParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, KEY_PROFILE, value);
			travellerInfo.getRouteSearchParameters().setProfileType(profileType);
		} else {
			throw new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, KEY_PROFILE);
		}

		double rangeValue = -1.0;
		boolean skipInterval = false;
		value = request.getParameter(KEY_RANGE);
		if (Helper.isEmpty(value))
			throw new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, KEY_RANGE);
		else {
			String[] rangeValues = value.split(",");

			if (rangeValues.length == 1) {
				try {
					rangeValue = Double.parseDouble(value);
					travellerInfo.setRanges(new double[] { rangeValue});
				} catch(NumberFormatException ex) {
					throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, KEY_RANGE);
				}
			} else {
				double[] ranges = new double[rangeValues.length];
				double maxRange = Double.MIN_VALUE;
				for (int i = 0; i < ranges.length; i++) {
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

		if (!skipInterval) {
			value = request.getParameter("interval");
			if (!Helper.isEmpty(value) && rangeValue != -1) {
				travellerInfo.setRanges(rangeValue, Double.parseDouble(value));
			}
		}

		value = request.getParameter(KEY_RANGE_TYPE);
		if (!Helper.isEmpty(value)) {
			switch (value.toLowerCase()) {
				case "distance":
					travellerInfo.setRangeType(TravelRangeType.DISTANCE);
					break;
				case "time":
					travellerInfo.setRangeType(TravelRangeType.TIME);
					break;
				default:
					throw new UnknownParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, KEY_RANGE_TYPE, value);
			}
		}

        value = request.getParameter(KEY_AREA_UNITS);
        if (!Helper.isEmpty(value)) {
            if (!("m".equals(value) || "km".equals(value) || "mi".equals(value)))
                throw new UnknownParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, KEY_AREA_UNITS, value);

            req.setAreaUnits(value.toLowerCase());
        }

        value = request.getParameter(KEY_UNITS);
        if (!Helper.isEmpty(value)) {
            if (!("m".equals(value) || "km".equals(value) || "mi".equals(value)))
                throw new UnknownParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, KEY_UNITS, value);

            req.setUnits(value.toLowerCase());
        }

        boolean inverseXY = false;
		value = request.getParameter(KEY_LOCATIONS);

		if (Helper.isEmpty(value)) {
			value = request.getParameter("latlng");
			inverseXY = true;
		}

		Coordinate[] coords = null;
		if (!Helper.isEmpty(value)) {
			try {
				coords = CoordTools.parse(value, "\\|", false, inverseXY);
			} catch(NumberFormatException nfex) {
				throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, KEY_LOCATIONS);
			}
		} else {
			throw new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, KEY_LOCATIONS);
		}

		value = request.getParameter(KEY_LOCATION_TYPE);
		if (!Helper.isEmpty(value)) {
			if (!"start".equalsIgnoreCase(value) && !"destination".equalsIgnoreCase(value))
				throw new UnknownParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, KEY_LOCATION_TYPE, value);

			travellerInfo.setLocationType(value);
		}

		value = request.getParameter(KEY_CALC_METHOD);
		if (!Helper.isEmpty(value))
			req.setCalcMethod(value);

		value = request.getParameter(KEY_ATTRIBUTES);
		if (!Helper.isEmpty(value)) {
			String[] values = value.split("\\|");
			for (int i = 0; i < values.length; i++) {
				String attr = values[i];
				if (!(attr.equalsIgnoreCase("area") || attr.equalsIgnoreCase("reachfactor") || IsochronesServiceSettings.isStatsAttributeSupported(attr)))
					throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, KEY_ATTRIBUTES, attr);
			}
			
			req.setAttributes(values);
		}

		value = request.getParameter(KEY_INTERSECTIONS);
		if (!Helper.isEmpty(value)) {
			try {
				req.setIncludeIntersections(Boolean.parseBoolean(value));
			} catch(Exception ex) {
				throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, KEY_INTERSECTIONS, value);
			}
		}

		value = request.getParameter(KEY_OPTIONS);
		if (!Helper.isEmpty(value)) {
			try {
				travellerInfo.getRouteSearchParameters().setOptions(value);
				req.setCalcMethod("ConcaveBalls");
			} catch(Exception ex) {
				throw new ParameterValueException(IsochronesErrorCodes.INVALID_JSON_FORMAT, KEY_OPTIONS, value);
			}
		}
		else req.setCalcMethod("FastIsochrone");

		setIsochroneSmoothing(req, request.getParameter(KEY_SMOOTHING));
		
		if (coords.length == 1) {
			travellerInfo.setLocation(coords[0]);
			req.addTraveller(travellerInfo);
		} else {
			travellerInfo.setLocation(coords[0]);
			req.addTraveller(travellerInfo);

			for (int i = 1; i < coords.length; i++) {
				TravellerInfo ti = new TravellerInfo(travellerInfo);
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
				throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, KEY_SMOOTHING, requestSmoothingValue);
			}

			if(smoothingValue < 0 || smoothingValue > 100)
				throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, KEY_SMOOTHING, requestSmoothingValue);

			isochroneRequest.setSmoothingFactor(smoothingValue);
		}
	}
}
