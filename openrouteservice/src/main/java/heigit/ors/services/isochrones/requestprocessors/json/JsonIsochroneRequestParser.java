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

import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.exceptions.UnsetParameterException;
import heigit.ors.isochrones.IsochronesRangeType;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.services.isochrones.IsochroneRequest;

public class JsonIsochroneRequestParser {

	public static IsochroneRequest parseFromStream(InputStream stream) throws Exception 
	{
		IsochroneRequest req = null;

		try {
		
		} catch (Exception ex) {
			throw new Exception("Unable to parse JSON document.");
		}

		return req;
	}
	
	public static IsochroneRequest parseFromRequestParams(HttpServletRequest request) throws Exception
	{
		IsochroneRequest req = new IsochroneRequest();

		String value = request.getParameter("profile");
		if (!Helper.isEmpty(value))
			req.getRouteSearchParameters().setProfileType(RoutingProfileType.getFromString(value));

		double rangeValue= - 1;
		value = request.getParameter("range");
		if (Helper.isEmpty(value))
			throw new UnsetParameterException("range");
		else
		{
			String[] rangeValues = value.split(",");
			
			if (rangeValues.length == 1)
			{
				rangeValue = Double.parseDouble(value);
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
			switch (value)
			{
			case "distance":
				req.setRangeType(IsochronesRangeType.Distance);
				break;
			case "time":
				req.setRangeType(IsochronesRangeType.Time);
				break;
			default:
				throw new UnknownParameterValueException("range_type", value);
			}
		}
		
		value = request.getParameter("units");
		if (!Helper.isEmpty(value))
		{
			if (req.getRangeType() == IsochronesRangeType.Distance)
			{
				if (!("m".equals(value) || "km".equals(value) || "mi".equals(value)))
					throw new UnknownParameterValueException("units", value);
			}
			else
			{
				throw new Exception("Parameter 'units' must only be set together with 'range_type=distance'.");
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
			String[] coordValues = value.split("\\|");
			Coordinate[] coords = new Coordinate[coordValues.length];

			for (int i = 0; i < coordValues.length; i++)
			{
				String[] locations = coordValues[i].split(",");
				if (inverseXY)
					coords[i] = new Coordinate(Double.parseDouble(locations[1]),Double.parseDouble(locations[0]));
				else
					coords[i] = new Coordinate(Double.parseDouble(locations[0]),Double.parseDouble(locations[1]));
			}

			req.setLocations(coords);
		}
		else
		{
			throw new UnsetParameterException("locations");
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
			req.getRouteSearchParameters().setOptions(value);
		
		return req;
	}
}
