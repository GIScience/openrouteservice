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
package org.heigit.ors.services.mapmatching.requestprocessors.json;

import javax.servlet.http.HttpServletRequest;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;

import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.exceptions.MissingParameterException;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.exceptions.UnknownParameterValueException;
import org.heigit.ors.localization.LocalizationManager;
import org.heigit.ors.mapmatching.MapMatchingErrorCodes;
import org.heigit.ors.mapmatching.MapMatchingRequest;
import org.heigit.ors.routing.RouteExtraInfoFlag;
import org.heigit.ors.routing.RouteInstructionsFormat;
import org.heigit.ors.routing.RouteSearchParameters;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.WeightingMethod;
import org.heigit.ors.services.mapmatching.MapMatchingServiceSettings;
import org.heigit.ors.util.CoordTools;
import org.heigit.ors.util.DistanceUnitUtil;

public class JsonMapMatchingRequestParser {

	public static final String KEY_PROFILE = "profile";

	private JsonMapMatchingRequestParser() {}

	public static MapMatchingRequest parseFromRequestParams(HttpServletRequest request) throws Exception {
		MapMatchingRequest req = new MapMatchingRequest();
		RouteSearchParameters searchParams = req.getSearchParameters();

		String value = request.getParameter(KEY_PROFILE);
		if (!Helper.isEmpty(value)) {
			int profileType = RoutingProfileType.getFromString(value);

			if (profileType == RoutingProfileType.UNKNOWN)
				throw new UnknownParameterValueException(MapMatchingErrorCodes.INVALID_PARAMETER_VALUE, KEY_PROFILE, value);
			searchParams.setProfileType(profileType);
		} else {
			throw new MissingParameterException(MapMatchingErrorCodes.MISSING_PARAMETER, KEY_PROFILE);
		}

		value = request.getParameter("preference");
		if (!Helper.isEmpty(value)) {
			int weightingMethod = WeightingMethod.getFromString(value);
			if (weightingMethod == WeightingMethod.UNKNOWN)
				throw new UnknownParameterValueException(MapMatchingErrorCodes.INVALID_PARAMETER_VALUE, "preference", value);

			searchParams.setWeightingMethod(weightingMethod);
		}

		value = request.getParameter("coordinates");
		if (!Helper.isEmpty(value)) {
			Coordinate[] coords = null;

			try {
				coords = CoordTools.parse(value, "\\|", true, false);		
			} catch(NumberFormatException ex) {
				throw new ParameterValueException(MapMatchingErrorCodes.INVALID_PARAMETER_FORMAT, "coordinates");
			}

			if (coords.length < 2)
				throw new ParameterValueException(MapMatchingErrorCodes.INVALID_PARAMETER_VALUE, "coordinates parameter must contain at least two locations");

			if (coords.length > MapMatchingServiceSettings.getMaximumLocations())
				throw new ParameterValueException(MapMatchingErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "coordinates parameter must contain at least two locations");
			
			req.setCoordinates(coords);
		}		

		value = request.getParameter("units");
		if (!Helper.isEmpty(value)) {
			DistanceUnit units = DistanceUnitUtil.getFromString(value, DistanceUnit.UNKNOWN);
			
			if (units == DistanceUnit.UNKNOWN)
				throw new ParameterValueException(MapMatchingErrorCodes.INVALID_PARAMETER_VALUE, "units", value);
			
			req.setUnits(units);
		}

		value = request.getParameter("language");
		if (!Helper.isEmpty(value)) {
			if(!LocalizationManager.getInstance().isLanguageSupported(value))
				throw new ParameterValueException(MapMatchingErrorCodes.INVALID_PARAMETER_VALUE, "Specified language '" +  value + "' is not supported.");

			req.setLanguage(value);
		}

		value = request.getParameter("geometry");
		if (!Helper.isEmpty(value))
			req.setIncludeGeometry(Boolean.parseBoolean(value));

		value = request.getParameter("geometry_format");
		if (!Helper.isEmpty(value)) {
			if (!("geojson".equalsIgnoreCase(value) || "polyline".equalsIgnoreCase(value) || "encodedpolyline".equalsIgnoreCase(value)))
				throw new UnknownParameterValueException(MapMatchingErrorCodes.INVALID_PARAMETER_VALUE, "geometry_format", value);

			req.setGeometryFormat(value);
		}

		value = request.getParameter("instructions");
		if (!Helper.isEmpty(value))
			req.setIncludeInstructions(Boolean.parseBoolean(value));

		value = request.getParameter("elevation");
		if (!Helper.isEmpty(value))
			req.setIncludeElevation(Boolean.parseBoolean(value));

		value = request.getParameter("instructions_format");
		if (!Helper.isEmpty(value)) {
			RouteInstructionsFormat instrFormat = RouteInstructionsFormat.fromString(value);
			if (instrFormat == RouteInstructionsFormat.UNKNOWN)
				throw new UnknownParameterValueException(MapMatchingErrorCodes.INVALID_PARAMETER_VALUE, "instructions_format", value);
			
			req.setInstructionsFormat(instrFormat);
		}

		value = request.getParameter("extra_info");
		if (!Helper.isEmpty(value))
			req.setExtraInfo(RouteExtraInfoFlag.getFromString(value));

		value = request.getParameter("attributes");
		if (!Helper.isEmpty(value))
			req.setAttributes(value.split("\\|"));

		value = request.getParameter("id");
		if (!Helper.isEmpty(value))
			req.setId(value);

		return req;		
	}
}
