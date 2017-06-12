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
package heigit.ors.services.matrix.requestprocessors.json;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.MissingParameterException;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.matrix.MatrixErrorCodes;
import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.matrix.MatrixRequest;
import heigit.ors.routing.RoutingProfileType;
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
		
		value = request.getParameter("sources");
		if (!Helper.isEmpty(value))
		{
			try
			{
				Coordinate[] coords = CoordTools.parse(value, "\\|", false, false);						
				req.setSources(coords);
			}
			catch(NumberFormatException nfex)
			{
				throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, "sources");
			}
		}
		else
		{
			throw new MissingParameterException(MatrixErrorCodes.MISSING_PARAMETER, "sources");
		}

		value = request.getParameter("destinations");
		if (!Helper.isEmpty(value))
		{
			try
			{
				Coordinate[] coords = CoordTools.parse(value, "\\|", false, false);						
				req.setDestinations(coords);
			}
			catch(NumberFormatException nfex)
			{
				throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, "destinations");
			}
		}
		else
		{
			throw new MissingParameterException(MatrixErrorCodes.MISSING_PARAMETER, "destinations");
		}

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
			   req.setResolveLocations(Boolean.getBoolean(value));
		   }
		   catch(Exception ex)
		   {
			   throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, "resolve_locations");
		   }
		}
		
		value = request.getParameter("id");
		if (!Helper.isEmpty(value))
			req.setId(value);

		return req;
	}
}
