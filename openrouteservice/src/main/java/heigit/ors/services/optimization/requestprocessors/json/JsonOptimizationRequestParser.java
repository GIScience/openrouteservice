/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.services.optimization.requestprocessors.json;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.MissingParameterException;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.optimization.OptimizationErrorCodes;
import heigit.ors.optimization.RouteOptimizationRequest;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.util.CoordTools;
import heigit.ors.util.DistanceUnit;
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
				//if (paramSource)
			}
			catch(Exception ex)
			{
				
			}
			
			req.setSourceIndex(sourceIndex);
		}
		
		value = request.getParameter("destination");
		if (!Helper.isEmpty(value))
		{
			
		}
		
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
			   throw new ParameterValueException(OptimizationErrorCodes.INVALID_PARAMETER_FORMAT, "roundtrip");
		   }
		}
		
		if (req.isRoundTrip() && req.getDestinationIndex() > 0 && req.getSourceIndex() != req.getDestinationIndex())
			   throw new ParameterValueException(OptimizationErrorCodes.INVALID_PARAMETER_VALUE, "destination");
		
		value = request.getParameter("metric");
		if (!Helper.isEmpty(value))
		{
		   int metrics =  MatrixMetricsType.getFromString(value);
		   
		   if (metrics == MatrixMetricsType.Unknown)
			 throw new ParameterValueException(OptimizationErrorCodes.INVALID_PARAMETER_VALUE, "metric");
		   
		   req.setMetric(metrics);
		}

		value = request.getParameter("units");
		if (!Helper.isEmpty(value))
		{
			DistanceUnit units = DistanceUnitUtil.getFromString(value, DistanceUnit.Unknown);

			if (units == DistanceUnit.Unknown)
				throw new ParameterValueException(OptimizationErrorCodes.INVALID_PARAMETER_VALUE, "units");

			req.setUnits(units);
		}
	 
		value = request.getParameter("id");
		if (!Helper.isEmpty(value))
			req.setId(value);

		return req;
	}
}
