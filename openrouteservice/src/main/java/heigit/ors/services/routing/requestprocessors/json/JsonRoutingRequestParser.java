package heigit.ors.services.routing.requestprocessors.json;

import javax.servlet.http.HttpServletRequest;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.routing.RouteExtraInformationFlag;
import heigit.ors.routing.RouteSearchParameters;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.WeightingMethod;
import heigit.ors.services.routing.RoutingRequest;
import heigit.ors.util.DistanceUnit;
import heigit.ors.util.DistanceUnitUtil;

public class JsonRoutingRequestParser {
	public static RoutingRequest parseFromRequestParams(HttpServletRequest request) throws Exception
	{
		RoutingRequest req = new RoutingRequest();
		RouteSearchParameters searchParams = req.getSearchParameters();
		
		String value = request.getParameter("profile");
		if (!Helper.isEmpty(value))
			searchParams.setProfileType(RoutingProfileType.getFromString(value));
		
		value = request.getParameter("preference");
		if (!Helper.isEmpty(value))
			searchParams.setWeightingMethod(WeightingMethod.getFromString(value));
		
		value = request.getParameter("coordinates");
		if (!Helper.isEmpty(value))
		{
			String[] coordValues = value.split("\\|");
			Coordinate[] coords = new Coordinate[coordValues.length];

			for (int i = 0; i < coordValues.length; i++)
			{
				String[] locations = coordValues[i].split(",");
				if (locations.length == 3)
					coords[i] = new Coordinate(Double.parseDouble(locations[0]), Double.parseDouble(locations[1]), Integer.parseInt(locations[2]));
				else
					coords[i] = new Coordinate(Double.parseDouble(locations[0]),Double.parseDouble(locations[1]));
			} 
			
			req.setCoordinates(coords);
		}		
		
		value = request.getParameter("units");
		if (!Helper.isEmpty(value))
			req.setUnits(DistanceUnitUtil.getFromString(value, DistanceUnit.Meters));		

		value = request.getParameter("language");
		if (!Helper.isEmpty(value))
			req.setLanguage(value);
		
		value = request.getParameter("geometry");
		if (!Helper.isEmpty(value))
			req.setIncludeGeometry(Boolean.parseBoolean(value));
		
		value = request.getParameter("geometry_format");
		if (!Helper.isEmpty(value))
		{
			if (!("geojson".equalsIgnoreCase(value) || "polyline".equalsIgnoreCase(value) || "encodedpolyline".equalsIgnoreCase(value)))
				throw new UnknownParameterValueException("geometry_format", value);

			req.setGeometryFormat(value);
		}

		value = request.getParameter("instructions");
		if (!Helper.isEmpty(value))
			req.setIncludeInstructions(Boolean.parseBoolean(value));
		
		value = request.getParameter("elevation");
		if (!Helper.isEmpty(value))
			req.setIncludeElevation(Boolean.parseBoolean(value));
		
		value = request.getParameter("prettify_instructions");
		if (!Helper.isEmpty(value))
			req.setPrettifyInstructions(Boolean.parseBoolean(value));
		
		value = request.getParameter("extra_info");
		if (!Helper.isEmpty(value))
			req.setExtraInfo(RouteExtraInformationFlag.getFromString(value));
		
		value = request.getParameter("attributes");
		if (!Helper.isEmpty(value))
			req.setAttributes(value.split("\\|"));
	
		value = request.getParameter("options");
		if (!Helper.isEmpty(value))
			searchParams.setOptions(value);
			
		return req;		
	}
}
