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
package heigit.ors.services.routing.requestprocessors;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.common.DistanceUnit;
import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.MissingParameterException;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.localization.LocalizationManager;
import heigit.ors.routing.RouteExtraInfoFlag;
import heigit.ors.routing.RouteInstructionsFormat;
import heigit.ors.routing.RouteSearchParameters;
import heigit.ors.routing.RoutingErrorCodes;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.RoutingRequest;
import heigit.ors.routing.WayPointBearing;
import heigit.ors.routing.WeightingMethod;
import heigit.ors.util.ArraysUtility;
import heigit.ors.util.CoordTools;
import heigit.ors.util.DistanceUnitUtil;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;

public class RoutingRequestParser
{
	public static RoutingRequest parseFromRequestParams(HttpServletRequest request) throws Exception
	{
		RoutingRequest req = new RoutingRequest();
		RouteSearchParameters searchParams = req.getSearchParameters();

		String value = request.getParameter("profile");
		if (!Helper.isEmpty(value))
		{
			int profileType = RoutingProfileType.getFromString(value);

			if (profileType == RoutingProfileType.UNKNOWN)
				throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "profile", value);
			searchParams.setProfileType(profileType);
		}
		else
			throw new MissingParameterException(RoutingErrorCodes.MISSING_PARAMETER, "profile");

		value = request.getParameter("preference");
		if (!Helper.isEmpty(value))
		{
			int weightingMethod = WeightingMethod.getFromString(value);
			if (weightingMethod == WeightingMethod.UNKNOWN)
				throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "preference", value);

			searchParams.setWeightingMethod(weightingMethod);
		}

		value = request.getParameter("coordinates");
		if (!Helper.isEmpty(value))
		{
			Coordinate[] coords = null;

			try
			{
				coords = CoordTools.parse(value, "\\|", true, false);
			}
			catch(NumberFormatException ex)
			{
				throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, "coordinates");
			}

			if (coords.length < 2)
				throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "coordinates parameter must contain at least two locations");

			req.setCoordinates(coords);
		}	
		else
			throw new MissingParameterException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "coordinates");
		
		value = request.getParameter("bearings");
		if (!Helper.isEmpty(value))
		{
			WayPointBearing[] bearings = null;

			try
			{
				String[] array = value.split("\\|");
				bearings = new WayPointBearing[array.length];
				
				for (int i = 0; i < array.length; i++)
				{
					value = array[i].trim();
					if (value.contains(","))
					{
						String[] bd = value.split("\\,");
						if (bd.length >= 2)
							bearings[i] = new WayPointBearing(Double.parseDouble(bd[0]), Double.parseDouble(bd[1]));
						else
							bearings[i] = new WayPointBearing(Double.parseDouble(bd[0]), Double.NaN);
					}
					else {
						if (Helper.isEmpty(value))
							bearings[i] = new WayPointBearing(Double.NaN, Double.NaN);
						else 
							bearings[i] = new WayPointBearing(Double.parseDouble(value), 0.0);
						
					}
						
				}
			}
			catch(Exception ex)
			{
				throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "bearings", value);
			}

			if (bearings == null || bearings.length < req.getCoordinates().length - 1 || bearings.length > req.getCoordinates().length)
				throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "bearings", value);

			req.getSearchParameters().setBearings(bearings);
		}
		
		value = request.getParameter("radiuses");
		if (!Helper.isEmpty(value))
		{
			double[] radiuses = ArraysUtility.parseDoubleArray(value, "radiuses", "\\|", RoutingErrorCodes.INVALID_PARAMETER_VALUE);
			
			if (radiuses == null || radiuses.length != req.getCoordinates().length)
				throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "radiuses", value);

			req.getSearchParameters().setMaximumRadiuses(radiuses);
		} else {
			if(searchParams.getProfileType() == RoutingProfileType.WHEELCHAIR) {
				// As there are generally less ways that can be used as pedestrian ways, we need to restrict search
				// radii else we end up with starting and ending ways really far from the actual points. This is
				// especially a problem for wheechair users as the restrictions are stricter
				final int coordinateCount = req.getCoordinates().length;
				double maxRadii[] = new double[coordinateCount];
				for(int i=0; i<coordinateCount; i++) {
					maxRadii[i] = 50;
				}
				
				req.getSearchParameters().setMaximumRadiuses(maxRadii);
			}
		}
		
		value = request.getParameter("units");
		if (!Helper.isEmpty(value))
		{
			DistanceUnit units = DistanceUnitUtil.getFromString(value, DistanceUnit.Unknown);
			
			if (units == DistanceUnit.Unknown)
				throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "units", value);
			
			req.setUnits(units);
		}

		value = request.getParameter("language");
		if (!Helper.isEmpty(value))
		{
			if(!LocalizationManager.getInstance().isLanguageSupported(value))
				throw new StatusCodeException(StatusCode.BAD_REQUEST, RoutingErrorCodes.INVALID_PARAMETER_VALUE, "Specified language '" +  value + "' is not supported.");

			req.setLanguage(value);
		}

		value = request.getParameter("geometry");
		if (!Helper.isEmpty(value))
			req.setIncludeGeometry(Boolean.parseBoolean(value));

		value = request.getParameter("geometry_format");
		if (!Helper.isEmpty(value))
		{
			if (!("geojson".equalsIgnoreCase(value) || "polyline".equalsIgnoreCase(value) || "encodedpolyline".equalsIgnoreCase(value)))
				throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "geometry_format", value);

			req.setGeometryFormat(value);
		}

		value = request.getParameter("instructions");
		if (!Helper.isEmpty(value))
			req.setIncludeInstructions(Boolean.parseBoolean(value));
		
		value = request.getParameter("maneuvers");
		if (!Helper.isEmpty(value))
			req.setIncludeManeuvers(Boolean.parseBoolean(value));

		value = request.getParameter("elevation");
		if (!Helper.isEmpty(value))
			req.setIncludeElevation(Boolean.parseBoolean(value));
		
		value = request.getParameter("continue_straight");
		if (!Helper.isEmpty(value))
			req.setContinueStraight(Boolean.parseBoolean(value));
		
		value = request.getParameter("roundabout_exits");
		if (!Helper.isEmpty(value))
			req.setIncludeRoundaboutExits(Boolean.parseBoolean(value));

		value = request.getParameter("instructions_format");
		if (!Helper.isEmpty(value))
		{
			RouteInstructionsFormat instrFormat = RouteInstructionsFormat.fromString(value);
			if (instrFormat == RouteInstructionsFormat.UNKNOWN)
				throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "instructions_format", value);
			
			req.setInstructionsFormat(instrFormat);
		}

		value = request.getParameter("extra_info");
		if (!Helper.isEmpty(value))
			req.setExtraInfo(RouteExtraInfoFlag.getFromString(value));

		value = request.getParameter("attributes");
		if (!Helper.isEmpty(value))
			req.setAttributes(value.split("\\|"));

		value = request.getParameter("options");
		if (!Helper.isEmpty(value))
		{
			try
			{
				searchParams.setOptions(value);
			}
			catch(ParseException ex)
			{
				throw new ParameterValueException(RoutingErrorCodes.INVALID_JSON_FORMAT, "Unable to parse 'options' value." + ex.getMessage());
			}
			catch(StatusCodeException scex)
			{
				throw scex;
			}
		}
		
		value = request.getParameter("optimized");
		if (!Helper.isEmpty(value))
		{
		   try
		   {
			   Boolean b = Boolean.parseBoolean(value);
			   if (!b && !value.equalsIgnoreCase("false"))
				   throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, "optimized");
			   
			   searchParams.setFlexibleMode(!b);
		   }
		   catch(Exception ex)
		   {
			   throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, "optimized");
		   }
		}

		value = request.getParameter("id");
		if (!Helper.isEmpty(value))
			req.setId(value);

		return req;		
	}
}
