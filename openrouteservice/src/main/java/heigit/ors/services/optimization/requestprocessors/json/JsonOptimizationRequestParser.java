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
package heigit.ors.services.optimization.requestprocessors.json;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.common.DistanceUnit;
import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.MissingParameterException;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.localization.LocalizationManager;
import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.optimization.OptimizationErrorCodes;
import heigit.ors.optimization.RouteOptimizationRequest;
import heigit.ors.routing.RouteInstructionsFormat;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.util.CoordTools;
import heigit.ors.util.DistanceUnitUtil;
import heigit.ors.util.StreamUtility;

public class JsonOptimizationRequestParser {

	public static RouteOptimizationRequest parseFromStream(InputStream stream) throws Exception 
	{
		String body = StreamUtility.readStream(stream);

		if (Helper.isEmpty(body))
			throw new StatusCodeException(StatusCode.BAD_REQUEST, OptimizationErrorCodes.INVALID_JSON_FORMAT, "Unable to parse JSON document.");

		JSONObject json = null;
		
		try
		{
			json = new JSONObject(body);
		}
		catch(Exception ex)
		{
			throw new StatusCodeException(StatusCode.BAD_REQUEST, OptimizationErrorCodes.INVALID_JSON_FORMAT, "Unable to parse JSON document." + ex.getMessage());
		}

		RouteOptimizationRequest req = new RouteOptimizationRequest();
		 
		String value = json.optString("id");
		if (!Helper.isEmpty(value))
			req.setId(value);

		throw new Exception("POST REQUEST IS NOT SUPPORTED");

//		return req;
	}

	public static RouteOptimizationRequest parseFromRequestParams(HttpServletRequest request) throws Exception
	{
		RouteOptimizationRequest req = new RouteOptimizationRequest();

		String value = request.getParameter("profile");
		if (!Helper.isEmpty(value))
		{
			int profileType = RoutingProfileType.getFromString(value);
			if (profileType == RoutingProfileType.UNKNOWN)
				throw new UnknownParameterValueException(OptimizationErrorCodes.INVALID_PARAMETER_VALUE, "profile", value);
			req.setProfileType(profileType);
		}
		else
		{
			throw new MissingParameterException(OptimizationErrorCodes.MISSING_PARAMETER, "profile");
		}
		
		Coordinate[] locations = null;
		
		value = request.getParameter("locations");
		if (!Helper.isEmpty(value))
		{
			try
			{
				locations = CoordTools.parse(value, "\\|", false, false);		
				if (locations.length < 2)
					throw new ParameterValueException(OptimizationErrorCodes.INVALID_PARAMETER_VALUE, "locations");
				
				req.setLocations(locations);
			}
			catch(NumberFormatException nfex)
			{
				throw new ParameterValueException(OptimizationErrorCodes.INVALID_PARAMETER_FORMAT, "locations");
			}
		}
		else
		{
			throw new MissingParameterException(OptimizationErrorCodes.MISSING_PARAMETER, "locations");
		}
		
		value = request.getParameter("source");
		if (!Helper.isEmpty(value))
		{
			int sourceIndex = 0;
			
			try
			{
				String paramSource = request.getParameter("source");
				if ("any".equalsIgnoreCase(paramSource))
					sourceIndex = -1;
				else
					sourceIndex = Integer.parseInt(paramSource);
			}
			catch(NumberFormatException ex)
			{
				throw new ParameterValueException(OptimizationErrorCodes.INVALID_PARAMETER_FORMAT, "source");
			}

			if (sourceIndex >= locations.length)
				throw new ParameterValueException(OptimizationErrorCodes.INVALID_PARAMETER_VALUE, "source");

			req.setSourceIndex(sourceIndex);
		}
		else
			req.setSourceIndex(-1);
		
		value = request.getParameter("destination");
		if (!Helper.isEmpty(value))
		{
			int destIndex = locations.length - 1;
			
			try
			{
				String paramSource = request.getParameter("destination");
				if ("any".equalsIgnoreCase(paramSource))
					destIndex = -1;
				else
					destIndex = Integer.parseInt(paramSource);
			}
			catch(NumberFormatException ex)
			{
				throw new ParameterValueException(OptimizationErrorCodes.INVALID_PARAMETER_FORMAT, "destination");
			}

			if (destIndex >= locations.length)
				throw new ParameterValueException(OptimizationErrorCodes.INVALID_PARAMETER_VALUE, "destination");
			
			req.setDestinationIndex(destIndex);
		}
		else
			req.setDestinationIndex(-1);
		
		value = request.getParameter("roundtrip");
		if (!Helper.isEmpty(value))
		{
		   try
		   {
			   Boolean b = Boolean.parseBoolean(value);
			   if (!b && !value.equalsIgnoreCase("false"))
				   throw new ParameterValueException(OptimizationErrorCodes.INVALID_PARAMETER_FORMAT, "roundtrip");
			   req.setRoundTrip(b);
		   }
		   catch(Exception ex)
		   {
			   throw new ParameterValueException(OptimizationErrorCodes.INVALID_PARAMETER_FORMAT, "roundtrip", value);
		   }
		}
		
		if (req.isRoundTrip() && req.getDestinationIndex() > 0 && req.getSourceIndex() != req.getDestinationIndex())
			   throw new ParameterValueException(OptimizationErrorCodes.INVALID_PARAMETER_VALUE, "destination");
		
		value = request.getParameter("metric");
		if (!Helper.isEmpty(value))
		{
		   int metrics =  MatrixMetricsType.getFromString(value);
		   
		   if (metrics == MatrixMetricsType.Unknown)
			 throw new ParameterValueException(OptimizationErrorCodes.INVALID_PARAMETER_VALUE, "metric", value);
		   
		   req.setMetric(metrics);
		}

		value = request.getParameter("units");
		if (!Helper.isEmpty(value))
		{
			DistanceUnit units = DistanceUnitUtil.getFromString(value, DistanceUnit.Unknown);

			if (units == DistanceUnit.Unknown)
				throw new ParameterValueException(OptimizationErrorCodes.INVALID_PARAMETER_VALUE, "units", value);

			req.setUnits(units);
		}
	 
		value = request.getParameter("language");
		if (!Helper.isEmpty(value))
		{
			if(!LocalizationManager.getInstance().isLanguageSupported(value))
				throw new StatusCodeException(StatusCode.BAD_REQUEST, OptimizationErrorCodes.INVALID_PARAMETER_VALUE, "Specified language '" +  value + "' is not supported.");

			req.setLanguage(value);
		}

		value = request.getParameter("geometry");
		if (!Helper.isEmpty(value))
			req.setIncludeGeometry(Boolean.parseBoolean(value));

		value = request.getParameter("geometry_format");
		if (!Helper.isEmpty(value))
		{
			if (!("geojson".equalsIgnoreCase(value) || "polyline".equalsIgnoreCase(value) || "encodedpolyline".equalsIgnoreCase(value)))
				throw new UnknownParameterValueException(OptimizationErrorCodes.INVALID_PARAMETER_VALUE, "geometry_format", value);

			req.setGeometryFormat(value);
		}

		value = request.getParameter("geometry_simplify");
		if (!Helper.isEmpty(value))
			req.setSimplifyGeometry(Boolean.parseBoolean(value));

		value = request.getParameter("instructions");
		if (!Helper.isEmpty(value))
			req.setIncludeInstructions(Boolean.parseBoolean(value));

		value = request.getParameter("elevation");
		if (!Helper.isEmpty(value))
			req.setIncludeElevation(Boolean.parseBoolean(value));

		value = request.getParameter("instructions_format");
		if (!Helper.isEmpty(value))
		{
			RouteInstructionsFormat instrFormat = RouteInstructionsFormat.fromString(value);
			if (instrFormat == RouteInstructionsFormat.UNKNOWN)
				throw new UnknownParameterValueException(OptimizationErrorCodes.INVALID_PARAMETER_VALUE, "instructions_format", value);
			
			req.setInstructionsFormat(instrFormat);
		}
		
		value = request.getParameter("id");
		if (!Helper.isEmpty(value))
			req.setId(value);

		return req;
	}
}
