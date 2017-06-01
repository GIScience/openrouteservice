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
package heigit.ors.services.isochrones.requestprocessors.json;

import java.io.InputStream;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.common.StatusCode;
import heigit.ors.common.TravelRangeType;
import heigit.ors.exceptions.MissingParameterException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.isochrones.IsochronesErrorCodes;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.services.isochrones.IsochroneRequest;
import heigit.ors.util.CoordTools;

public class JsonIsochroneRequestParser {

	public static IsochroneRequest parseFromStream(InputStream stream) throws Exception 
	{
		IsochroneRequest req = null;

		try {

		} catch (Exception ex) {
			throw new StatusCodeException(StatusCode.BAD_REQUEST, IsochronesErrorCodes.INVALID_JSON_FORMAT, "Unable to parse JSON document.");
		}

		return req;
	}

	public static IsochroneRequest parseFromRequestParams(HttpServletRequest request) throws Exception
	{
		IsochroneRequest req = new IsochroneRequest();

		String value = request.getParameter("profile");
		if (!Helper.isEmpty(value))
		{
			int profileType = RoutingProfileType.getFromString(value);
			if (profileType == RoutingProfileType.UNKNOWN)
				throw new UnknownParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "profile", value);
			req.getRouteSearchParameters().setProfileType(profileType);
		}
		else
		{
			throw new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, "profile");
		}

		double rangeValue= - 1;
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
				}
				catch(NumberFormatException ex)
				{
					throw new StatusCodeException(StatusCode.BAD_REQUEST, IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, "Unable to parse range value.");
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

				req.setRanges(ranges);
				req.setMaximumRange(maxRange);
			}
		}

		value = request.getParameter("interval");
		if (!Helper.isEmpty(value))
		{
			if (rangeValue != -1)
				req.setRanges(rangeValue, Double.parseDouble(value));
		}
		else
		{
			req.setRanges(new double[] { rangeValue});
		}

		value = request.getParameter("range_type");
		if (!Helper.isEmpty(value))
		{
			switch (value.toLowerCase())
			{
			case "distance":
				req.setRangeType(TravelRangeType.Distance);
				break;
			case "time":
				req.setRangeType(TravelRangeType.Time);
				break;
			default:
				throw new UnknownParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "range_type", value);
			}
		}

		value = request.getParameter("units");
		if (!Helper.isEmpty(value))
		{
			if (req.getRangeType() == TravelRangeType.Distance)
			{
				if (!("m".equals(value) || "km".equals(value) || "mi".equals(value)))
					throw new UnknownParameterValueException("units", value);
			}
			else
			{
				throw new StatusCodeException(StatusCode.BAD_REQUEST, IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "Parameter 'units' must only be set together with 'range_type=distance'.");
			}

			req.setUnits(value.toLowerCase());
		}


		boolean inverseXY = false;
		value = request.getParameter("locations");

		if (Helper.isEmpty(value))
		{
			value = request.getParameter("latlng");
			inverseXY = true;
		}

		if (!Helper.isEmpty(value))
		{
			Coordinate[] coords = null;

			try
			{
				coords = CoordTools.parse(value, "\\|", false, inverseXY);						
			}
			catch(NumberFormatException nfex)
			{
				throw new StatusCodeException(StatusCode.BAD_REQUEST, IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, "Unable to parse location coordinates." + nfex.getMessage());
			}
			catch(Exception ex)
			{
				throw new StatusCodeException(StatusCode.BAD_REQUEST, IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, "Unable to parse location coordinates." + ex.getMessage());
			}

			req.setLocations(coords);
		}
		else
		{
			throw new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, "locations");
		}

		value = request.getParameter("location_type");
		if (!Helper.isEmpty(value))
		{
			if (!"start".equalsIgnoreCase(value) && !"destination".equalsIgnoreCase(value))
				throw new UnknownParameterValueException("location_type", value);

			req.setLocationType(value);
		}

		value = request.getParameter("calc_method");
		if (!Helper.isEmpty(value))
			req.setCalcMethod(value);

		value = request.getParameter("attributes");
		if (!Helper.isEmpty(value))
			req.setAttributes(value.split("\\|"));

		value = request.getParameter("intersections");
		if (!Helper.isEmpty(value))
			req.setIncludeIntersections(Boolean.parseBoolean(value));

		value = request.getParameter("options");
		if (!Helper.isEmpty(value))
		{
			try
			{
				req.getRouteSearchParameters().setOptions(value);
			}
			catch(Exception ex)
			{
				throw new StatusCodeException(StatusCode.BAD_REQUEST, IsochronesErrorCodes.INVALID_JSON_FORMAT, "Unable to parse 'options' value."  + ex.getMessage());
			}
		}

		value = request.getParameter("id");
		if (!Helper.isEmpty(value))
			req.setId(value);

		return req;
	}
}
