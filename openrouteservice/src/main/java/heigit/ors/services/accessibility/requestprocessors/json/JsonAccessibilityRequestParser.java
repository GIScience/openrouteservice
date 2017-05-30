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
package heigit.ors.services.accessibility.requestprocessors.json;

import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;

import com.graphhopper.util.Helper;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import heigit.ors.accessibility.AccessibilityErrorCodes;
import heigit.ors.common.StatusCode;
import heigit.ors.common.TravelRangeType;
import heigit.ors.exceptions.MissingParameterException;
import heigit.ors.exceptions.ParameterOutOfRangeException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.localization.LocalizationManager;
import heigit.ors.locations.LocationDetailsType;
import heigit.ors.locations.LocationRequestType;
import heigit.ors.locations.LocationsRequest;
import heigit.ors.locations.LocationsResultSortType;
import heigit.ors.locations.LocationsSearchFilter;
import heigit.ors.routing.RouteExtraInfoFlag;
import heigit.ors.routing.RouteSearchParameters;
import heigit.ors.routing.RoutingErrorCodes;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.WeightingMethod;
import heigit.ors.services.accessibility.AccessibilityRequest;
import heigit.ors.services.accessibility.AccessibilityServiceSettings;
import heigit.ors.services.routing.RouteInstructionsFormat;
import heigit.ors.services.routing.RoutingRequest;
import heigit.ors.util.CoordTools;
import heigit.ors.util.DistanceUnit;
import heigit.ors.util.DistanceUnitUtil;
import heigit.ors.util.JsonUtility;

public class JsonAccessibilityRequestParser {
	public static AccessibilityRequest parseFromStream(InputStream stream) throws Exception 
	{
		return null;
	}

	public static AccessibilityRequest parseFromRequestParams(HttpServletRequest request) throws Exception
	{
		AccessibilityRequest req = new AccessibilityRequest();

		Coordinate[] locations = null;

		String value = request.getParameter("id");
		if (!Helper.isEmpty(value))
			req.setId(value);

		// ************ Parsing LocationsRequest parameters ************

		LocationsRequest reqLocations = req.getLocationsRequest();
		reqLocations.setType(LocationRequestType.POIS);

		LocationsSearchFilter query = reqLocations.getSearchFilter();

		value = request.getParameter("category_group_ids");
		if (!Helper.isEmpty(value))
			query.setCategoryGroupIds(JsonUtility.parseIntArray(value, "category_group_ids"));
		else
		{
			value = request.getParameter("category_ids");
			if (!Helper.isEmpty(value))
				query.setCategoryIds(JsonUtility.parseIntArray(value, "category_ids"));
		}

		if (query.getCategoryGroupIds() == null && query.getCategoryIds() == null)
			throw new MissingParameterException(AccessibilityErrorCodes.MISSING_PARAMETER, "category_ids/category_group_ids");

		value = request.getParameter("name");
		if (!Helper.isEmpty(value))
			query.setName(value);

		value = request.getParameter("wheelchair");
		if (!Helper.isEmpty(value))
			query.setWheelchair(value);

		value = request.getParameter("smoking");
		if (!Helper.isEmpty(value))
			query.setSmoking(value);

		value = request.getParameter("fee");
		if (!Helper.isEmpty(value))
			query.setFee(parseBooleanFlag(value));

		reqLocations.setLanguage(request.getParameter("lang"));

		value = request.getParameter("bbox");
		if (!Helper.isEmpty(value))
		{
			String[] coords = value.split(",");
			if (coords == null || coords.length != 4)
				throw new StatusCodeException(StatusCode.BAD_REQUEST, AccessibilityErrorCodes.INVALID_PARAMETER_FORMAT, "BBox parameter is either empty or has wrong number of values.");

			Envelope bbox = null;
			try
			{
				bbox = new Envelope(Double.parseDouble(coords[0]),  Double.parseDouble(coords[2]), Double.parseDouble(coords[1]), Double.parseDouble(coords[3]));
			}
			catch(NumberFormatException ex)
			{
				throw new StatusCodeException(StatusCode.BAD_REQUEST, AccessibilityErrorCodes.INVALID_PARAMETER_FORMAT, "Unable to parse bbox value.");
			}

			reqLocations.setBBox(bbox);
		}

		// skip radius as it is overwritten by range property in AccessibilityRequest
		/*value = request.getParameter("radius");
		if (!Helper.isEmpty(value))
		{
			double dvalue = Double.parseDouble(value);
			checkSearchRadius(reqLocations.getGeometry(), dvalue);
			reqLocations.setRadius(dvalue);
		}
		 */

		value = request.getParameter("limit");
		if (!Helper.isEmpty(value))
		{
			int ivalue = Integer.parseInt(value);
			if (AccessibilityServiceSettings.getResponseLimit() > 0 && AccessibilityServiceSettings.getResponseLimit() < ivalue)
				throw new ParameterOutOfRangeException(AccessibilityErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "limit", value, Integer.toString(AccessibilityServiceSettings.getResponseLimit()));

			req.setLimit(ivalue);
		}

		value = request.getParameter("sortby");
		if (!Helper.isEmpty(value))
		{
			LocationsResultSortType sortType = LocationsResultSortType.fromString(value);
			if (sortType == LocationsResultSortType.NONE)
				throw new UnknownParameterValueException(AccessibilityErrorCodes.INVALID_PARAMETER_VALUE, "sortby", value);

			reqLocations.setSortType(sortType);
		}

		value = request.getParameter("details");
		if (!Helper.isEmpty(value))
		{
			int detailsType = LocationDetailsType.fromString(value);

			if (detailsType == LocationDetailsType.NONE)
				throw new UnknownParameterValueException(AccessibilityErrorCodes.INVALID_PARAMETER_VALUE, "details", value);

			reqLocations.setDetails(detailsType);
		}

		// ************ Parsing RoutingRequest parameters ************
		RoutingRequest reqRouting = req.getRoutingRequest();

		RouteSearchParameters searchParams = reqRouting.getSearchParameters();

		reqRouting.setCoordinates(locations);

		value = request.getParameter("profile");
		if (!Helper.isEmpty(value))
		{
			int profileType = RoutingProfileType.getFromString(value);

			if (profileType == RoutingProfileType.UNKNOWN)
				throw new UnknownParameterValueException(AccessibilityErrorCodes.INVALID_PARAMETER_VALUE, "profile", value);
			searchParams.setProfileType(profileType);
		}
		else
			throw new MissingParameterException(AccessibilityErrorCodes.MISSING_PARAMETER, "profile");

		value = request.getParameter("preference");
		if (!Helper.isEmpty(value))
		{
			int weightingMethod = WeightingMethod.getFromString(value);
			if (weightingMethod == WeightingMethod.UNKNOWN)
				throw new UnknownParameterValueException(AccessibilityErrorCodes.INVALID_PARAMETER_VALUE, "preference", value);

			searchParams.setWeightingMethod(weightingMethod);
		}

		value = request.getParameter("units");
		if (!Helper.isEmpty(value))
			reqRouting.setUnits(DistanceUnitUtil.getFromString(value, DistanceUnit.Meters));	

		if (AccessibilityServiceSettings.getRouteDetailsAllowed())
		{
			value = request.getParameter("language");
			if (!Helper.isEmpty(value))
			{
				if(!LocalizationManager.getInstance().isLanguageSupported(value))
					throw new StatusCodeException(StatusCode.BAD_REQUEST, AccessibilityErrorCodes.INVALID_PARAMETER_VALUE, "Specified language '" +  value + "' is not supported.");

				reqRouting.setLanguage(value);
			}

			value = request.getParameter("geometry");
			if (!Helper.isEmpty(value))
				reqRouting.setIncludeGeometry(Boolean.parseBoolean(value));

			value = request.getParameter("geometry_format");
			if (!Helper.isEmpty(value))
			{
				if (!("geojson".equalsIgnoreCase(value) || "polyline".equalsIgnoreCase(value) || "encodedpolyline".equalsIgnoreCase(value)))
					throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "geometry_format", value);

				reqRouting.setGeometryFormat(value);
			}

			value = request.getParameter("geometry_simplify");
			if (!Helper.isEmpty(value))
				reqRouting.setSimplifyGeometry(Boolean.parseBoolean(value));

			value = request.getParameter("instructions");
			if (!Helper.isEmpty(value))
				reqRouting.setIncludeInstructions(Boolean.parseBoolean(value));

			value = request.getParameter("elevation");
			if (!Helper.isEmpty(value))
				reqRouting.setIncludeElevation(Boolean.parseBoolean(value));

			value = request.getParameter("instructions_format");
			if (!Helper.isEmpty(value))
			{
				RouteInstructionsFormat instrFormat = RouteInstructionsFormat.fromString(value);
				if (instrFormat == RouteInstructionsFormat.UNKNOWN)
					throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "instructions_format", value);

				reqRouting.setInstructionsFormat(instrFormat);
			}

			value = request.getParameter("extra_info");
			if (!Helper.isEmpty(value))
				reqRouting.setExtraInfo(RouteExtraInfoFlag.getFromString(value));

			value = request.getParameter("attributes");
			if (!Helper.isEmpty(value))
				reqRouting.setAttributes(value.split("\\|"));
			
			req.setRoutesFormat(request.getParameter("routes_format"));
		}
		else
		{
			reqRouting.setIncludeGeometry(false);
			reqRouting.setIncludeInstructions(false);
			req.setRoutesFormat("simple");
		}

		value = request.getParameter("options");
		if (!Helper.isEmpty(value))
		{
			try
			{
				searchParams.setOptions(value);
			}
			catch(Exception ex)
			{
				throw new StatusCodeException(StatusCode.BAD_REQUEST, AccessibilityErrorCodes.INVALID_JSON_FORMAT, "Unable to parse 'options' value.");
			}
		}

		// ************ Parsing other search parameters ************

		value = request.getParameter("locations");
		if (!Helper.isEmpty(value))
		{
			try
			{
				locations = CoordTools.parse(value, "\\|", false, false);	
			}
			catch(NumberFormatException ex)
			{
				throw new StatusCodeException(StatusCode.BAD_REQUEST, AccessibilityErrorCodes.INVALID_PARAMETER_FORMAT, "Unable to parse coordinates value.");
			}

			if (AccessibilityServiceSettings.getMaximumLocations() > 0 && locations.length > AccessibilityServiceSettings.getMaximumLocations())
				throw new ParameterOutOfRangeException(AccessibilityErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "locations", Integer.toString(locations.length), Integer.toString(AccessibilityServiceSettings.getMaximumLocations()));

			req.setLocations(locations);
		}
		else
		{
			throw new MissingParameterException(AccessibilityErrorCodes.MISSING_PARAMETER, "location");
		}

		value = request.getParameter("location_type");
		if (!Helper.isEmpty(value))
		{
			if (!"start".equalsIgnoreCase(value) && !"destination".equalsIgnoreCase(value))
				throw new UnknownParameterValueException(AccessibilityErrorCodes.INVALID_PARAMETER_VALUE, "location_type", value);

			req.setLocationType(value);
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
				throw new UnknownParameterValueException(AccessibilityErrorCodes.INVALID_PARAMETER_VALUE, "range_type", value);
			}
		}

		value = request.getParameter("range");
		if (!Helper.isEmpty(value))
		{
			req.setRange(Double.parseDouble(value));

			if (req.getRange() > AccessibilityServiceSettings.getMaximumRange(req.getRangeType()))
				throw new ParameterOutOfRangeException(AccessibilityErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "range", Integer.toString(AccessibilityServiceSettings.getMaximumRange(req.getRangeType())), Double.toString(req.getRange()));
		}
		else
			throw new MissingParameterException(AccessibilityErrorCodes.MISSING_PARAMETER, "range");

		return req;
	}

	private static Boolean parseBooleanFlag(String value)
	{
		if (value == null)
			return null;

		if ("yes".equalsIgnoreCase(value))
			return true;
		else if ("no".equalsIgnoreCase(value))
			return false;

		return null;
	}
}
