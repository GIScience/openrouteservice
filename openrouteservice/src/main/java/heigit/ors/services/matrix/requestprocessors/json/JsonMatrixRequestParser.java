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
package heigit.ors.services.matrix.requestprocessors.json;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.MissingParameterException;
import heigit.ors.exceptions.ParameterOutOfRangeException;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.matrix.MatrixErrorCodes;
import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.matrix.MatrixRequest;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.WeightingMethod;
import heigit.ors.util.ArraysUtility;
import heigit.ors.util.CoordTools;
import heigit.ors.util.DistanceUnit;
import heigit.ors.util.DistanceUnitUtil;

public class JsonMatrixRequestParser {

	public static MatrixRequest parseFromStream(InputStream stream) throws Exception 
	{
		throw new StatusCodeException(StatusCode.BAD_REQUEST, MatrixErrorCodes.INVALID_JSON_FORMAT, "Unable to parse JSON document.");
	}

	public static MatrixRequest parseFromRequestParams(HttpServletRequest request) throws Exception
	{
		MatrixRequest req = new MatrixRequest();

		String value = request.getParameter("profile");
		if (!Helper.isEmpty(value))
		{
			int profileType = RoutingProfileType.getFromString(value);
			if (profileType == RoutingProfileType.UNKNOWN)
				throw new UnknownParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, "profile", value);
			req.setProfileType(profileType);
		}
		else
		{
			throw new MissingParameterException(MatrixErrorCodes.MISSING_PARAMETER, "profile");
		}
		
		Coordinate[] locations = null;
		
		value = request.getParameter("locations");
		if (!Helper.isEmpty(value))
		{
			try
			{
				locations = CoordTools.parse(value, "\\|", false, false);		
				if (locations.length < 2)
					throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, "locations");
			}
			catch(NumberFormatException nfex)
			{
				throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, "locations");
			}
		}
		else
		{
			throw new MissingParameterException(MatrixErrorCodes.MISSING_PARAMETER, "locations");
		}
		
		value = request.getParameter("preference");
		if (!Helper.isEmpty(value))
		{
			int weightingMethod = WeightingMethod.getFromString(value);
			if (weightingMethod == WeightingMethod.UNKNOWN)
				throw new UnknownParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, "preference", value);
			
		  req.setWeightingMethod(value);
		}
		
		req.setSources(getLocations(locations, request.getParameter("sources"), "sources"));
		req.setDestinations(getLocations(locations, request.getParameter("destinations"), "destinations"));

		value = request.getParameter("metrics");
		if (!Helper.isEmpty(value))
		{
		   int metrics =  MatrixMetricsType.getFromString(value);
		   
		   if (metrics == MatrixMetricsType.Unknown)
			 throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, "metrics");
		   
		   req.setMetrics(metrics);
		}

		if (MatrixMetricsType.isSet(req.getMetrics(), MatrixMetricsType.Distance))
		{
			value = request.getParameter("units");
			if (!Helper.isEmpty(value))
			{
				DistanceUnit units = DistanceUnitUtil.getFromString(value, DistanceUnit.Unknown);

				if (units == DistanceUnit.Unknown)
					throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, "units");

				req.setUnits(units);
			}
		}
	
		value = request.getParameter("resolve_locations");
		if (!Helper.isEmpty(value))
		{
		   try
		   {
			   Boolean b = Boolean.parseBoolean(value);
			   if (!b && !value.equalsIgnoreCase("false"))
				   throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, "resolve_locations");
			   req.setResolveLocations(b);
		   }
		   catch(Exception ex)
		   {
			   throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, "resolve_locations");
		   }
		}
		
		value = request.getParameter("id");
		if (!Helper.isEmpty(value))
			req.setId(value);

		return req;
	}
	
	private static Coordinate[] getLocations(Coordinate[] locations, String strIndex, String elemName) throws Exception
	{
		if (Helper.isEmpty(strIndex) || "all".equalsIgnoreCase(strIndex))
			return locations;
		
		int[] index = ArraysUtility.parseIntArray(strIndex, elemName, MatrixErrorCodes.INVALID_PARAMETER_FORMAT);
		
		Coordinate[] res = new Coordinate[index.length];
		for (int i = 0; i < index.length; i++)
		{
			int idx = index[i];
			if (idx < 0 || idx >= locations.length)
				throw new ParameterOutOfRangeException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, elemName, Integer.toString(idx), Integer.toString(locations.length));
			
			res[i] = locations[idx];
		}
			
		return res;
	}
}
