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
package heigit.ors.services.locations.requestprocessors.json;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import heigit.ors.geojson.GeometryJSON;
import heigit.ors.locations.LocationDetailsType;
import heigit.ors.locations.LocationRequestType;
import heigit.ors.locations.LocationsErrorCodes;
import heigit.ors.locations.LocationsRequest;
import heigit.ors.locations.LocationsResultSortType;
import heigit.ors.locations.LocationsSearchFilter;
import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.MissingParameterException;
import heigit.ors.exceptions.ParameterOutOfRangeException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.services.locations.LocationsServiceSettings;
import heigit.ors.util.GeomUtility;
import heigit.ors.util.StreamUtility;

public class JsonLocationsRequestParser {

	public static LocationsRequest parseFromStream(HttpServletRequest request) throws Exception 
	{
		InputStream stream = request.getInputStream();

		LocationsRequest req = null;

		try 
		{
			JSONObject obj = new JSONObject(StreamUtility.readStream(stream));

			req = new LocationsRequest();		 

			String value = request.getParameter("request");
			if (!Helper.isEmpty(value))
				req.setType(LocationRequestType.fromString(value));

			if (req.getType() == LocationRequestType.UNKNOWN)
				throw new UnknownParameterValueException(LocationsErrorCodes.INVALID_PARAMETER_VALUE, "request", value);

			if (req.getType() == LocationRequestType.CATEGORIESLIST)
				return req;

			if (obj.has("filter"))
			{
				JSONObject jFilter = obj.getJSONObject("filter");
				LocationsSearchFilter query = req.getSearchFilter();

				int[] ids = null;
				String paramIdsName = "";
				if (jFilter.has("category_group_ids"))
				{
					paramIdsName = "category_group_ids";
					JSONArray jArr = jFilter.getJSONArray(paramIdsName);
					ids = parseIntArray(jArr, paramIdsName);
					query.setCategoryGroupIds(ids);
				}
				else
				{
					if (jFilter.has("category_ids"))
					{
						paramIdsName = "category_ids";
						JSONArray jArr = jFilter.getJSONArray(paramIdsName);
						ids = parseIntArray(jArr, paramIdsName);
						query.setCategoryIds(ids);
					}
				}

				if (req.getType() == LocationRequestType.POIS)
				{
					if (ids != null && LocationsServiceSettings.getMaximumCategories() > 0 && LocationsServiceSettings.getMaximumCategories() < ids.length)
					{
						throw new ParameterOutOfRangeException(LocationsErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, paramIdsName, "category_ids (or category_group_ids)", Integer.toString(LocationsServiceSettings.getMaximumCategories()));
					}

					value = jFilter.optString("name");
					if (!Helper.isEmpty(value))
						query.setName(value);
				}

				query.setWheelchair(jFilter.optString("wheelchair"));
				query.setSmoking(jFilter.optString("smoking"));
				query.setFee(parseBooleanFlag(jFilter.optString("fee")));
			}

			req.setLanguage(obj.optString("lang"));

			value = obj.optString("bbox");
			if (!Helper.isEmpty(value))
			{
				String[] coords = value.split(",");
				if (coords == null || coords.length != 4)
					throw new StatusCodeException(StatusCode.BAD_REQUEST, LocationsErrorCodes.INVALID_PARAMETER_FORMAT, "BBox parameter is either empty or has wrong number of values.");

				Envelope bbox = null;
				try
				{
					bbox = new Envelope(Double.parseDouble(coords[0]),  Double.parseDouble(coords[2]), Double.parseDouble(coords[1]), Double.parseDouble(coords[3]));
				}
				catch(NumberFormatException ex)
				{
					throw new StatusCodeException(StatusCode.BAD_REQUEST, LocationsErrorCodes.INVALID_PARAMETER_FORMAT, "Unable to parse bbox value.");
				}

				req.setBBox(bbox);
			}

			value = obj.optString("geometry");
			if (!Helper.isEmpty(value))
			{
				Geometry geom = parseGeometry(value);
				if (geom == null)
					throw new StatusCodeException(StatusCode.BAD_REQUEST, LocationsErrorCodes.INVALID_PARAMETER_VALUE, "'geometry' parameter is incorrect.");

				req.setGeometry(geom);
			}
			else 
			{
				if (req.getBBox() == null)
					throw new MissingParameterException(LocationsErrorCodes.MISSING_PARAMETER, "geometry");
			}

			value = obj.optString("radius");
			if (!Helper.isEmpty(value))
			{
				double dvalue = Double.parseDouble(value);
				checkSearchRadius(req.getGeometry(), dvalue);
				req.setRadius(dvalue);
			}
			else if (req.getGeometry() instanceof Point || req.getGeometry() instanceof LineString)
				throw new MissingParameterException(LocationsErrorCodes.MISSING_PARAMETER, "radius");

			if (req.getType() == LocationRequestType.POIS)
			{
				value = obj.optString("limit");
				if (!Helper.isEmpty(value))
				{
					int ivalue = Integer.parseInt(value);
					if (LocationsServiceSettings.getResponseLimit() > 0 && LocationsServiceSettings.getResponseLimit() < ivalue)
						throw new ParameterOutOfRangeException(LocationsErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "limit", value, Integer.toString(LocationsServiceSettings.getResponseLimit()));

					req.setLimit(ivalue);
				}
			}
			else
			{
				req.setLimit(-1);
			}

			value = obj.optString("sortby");
			if (!Helper.isEmpty(value))
			{
				LocationsResultSortType sortType = LocationsResultSortType.fromString(value);
				if (sortType == LocationsResultSortType.NONE)
					throw new UnknownParameterValueException("sortby", value);
				
				req.setSortType(sortType);
			}

			value = obj.optString("details");
			if (!Helper.isEmpty(value))
			{
				int detailsType = LocationDetailsType.fromString(value);
				if (detailsType == LocationDetailsType.NONE)
					throw new UnknownParameterValueException(LocationsErrorCodes.INVALID_PARAMETER_VALUE, "details", value);

				req.setDetails(detailsType);
			}

			value = obj.optString("id");
			if (!Helper.isEmpty(value))
				req.setId(value);			
		}
		catch(JSONException jex)
		{
			throw new StatusCodeException(StatusCode.BAD_REQUEST, LocationsErrorCodes.INVALID_JSON_FORMAT, "Unable to parse JSON document. " + jex.getMessage());
		}
		catch (Exception ex) 
		{
			throw ex;
		}

		return req;
	}

	public static LocationsRequest parseFromRequestParams(HttpServletRequest request) throws Exception
	{
		LocationsRequest req = new LocationsRequest();

		req = new LocationsRequest();

		String value = request.getParameter("request");
		if (!Helper.isEmpty(value))
			req.setType(LocationRequestType.fromString(value));

		if (req.getType() == LocationRequestType.UNKNOWN)
			throw new UnknownParameterValueException("request", value);

		if (req.getType() == LocationRequestType.CATEGORIESLIST)
			return req;

		LocationsSearchFilter query = req.getSearchFilter();

		value = request.getParameter("category_group_ids");
		if (!Helper.isEmpty(value))
			query.setCategoryGroupIds(parseIntArray(value, "category_group_ids"));
		else
		{
			value = request.getParameter("category_ids");
			if (!Helper.isEmpty(value))
				query.setCategoryIds(parseIntArray(value, "category_ids"));
		}

		if (req.getType() == LocationRequestType.POIS)
		{
			value = request.getParameter("name");
			if (!Helper.isEmpty(value))
				query.setName(value);
		}

		value = request.getParameter("wheelchair");
		if (!Helper.isEmpty(value))
			query.setWheelchair(value);

		value = request.getParameter("smoking");
		if (!Helper.isEmpty(value))
			query.setSmoking(value);

		value = request.getParameter("fee");
		if (!Helper.isEmpty(value))
			query.setFee(parseBooleanFlag(value));

		req.setLanguage(request.getParameter("lang"));

		value = request.getParameter("bbox");
		if (!Helper.isEmpty(value))
		{
			String[] coords = value.split(",");
			if (coords == null || coords.length != 4)
				throw new StatusCodeException(StatusCode.BAD_REQUEST, LocationsErrorCodes.INVALID_PARAMETER_FORMAT, "BBox parameter is either empty or has wrong number of values.");

			Envelope bbox = null;
			try
			{
				bbox = new Envelope(Double.parseDouble(coords[0]),  Double.parseDouble(coords[2]), Double.parseDouble(coords[1]), Double.parseDouble(coords[3]));
			}
			catch(NumberFormatException ex)
			{
				throw new StatusCodeException(StatusCode.BAD_REQUEST, LocationsErrorCodes.INVALID_PARAMETER_FORMAT, "Unable to parse bbox value.");
			}
			
			req.setBBox(bbox);
		}

		value = request.getParameter("geometry");
		if (!Helper.isEmpty(value))
		{
			Geometry geom = parseGeometry(value);
			if (geom == null)
				throw new StatusCodeException(StatusCode.BAD_REQUEST, LocationsErrorCodes.INVALID_PARAMETER_FORMAT, "'geometry' parameter is incorrect.");

			req.setGeometry(geom);
		}
		else
		{
			if (req.getBBox() == null)
				throw new MissingParameterException(LocationsErrorCodes.MISSING_PARAMETER, "geometry");
		}

		value = request.getParameter("radius");
		if (!Helper.isEmpty(value))
		{
			double dvalue = Double.parseDouble(value);
			checkSearchRadius(req.getGeometry(), dvalue);
			req.setRadius(dvalue);
		}
		else if (req.getGeometry() instanceof Point || req.getGeometry() instanceof LineString)
			throw new MissingParameterException(LocationsErrorCodes.MISSING_PARAMETER, "radius");

		if (req.getType() == LocationRequestType.POIS)
		{
			value = request.getParameter("limit");
			if (!Helper.isEmpty(value))
			{
				int ivalue = Integer.parseInt(value);
				if (LocationsServiceSettings.getResponseLimit() > 0 && LocationsServiceSettings.getResponseLimit() < ivalue)
					throw new ParameterOutOfRangeException(LocationsErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "limit", value, Integer.toString(LocationsServiceSettings.getResponseLimit()));

				req.setLimit(ivalue);
			}
		}
		else
		{
			req.setLimit(-1);
		}

		value = request.getParameter("sortby");
		if (!Helper.isEmpty(value))
		{
			LocationsResultSortType sortType = LocationsResultSortType.fromString(value);
			if (sortType == LocationsResultSortType.NONE)
				throw new UnknownParameterValueException(LocationsErrorCodes.INVALID_PARAMETER_VALUE, "sortby", value);

			req.setSortType(sortType);
		}

		value = request.getParameter("details");
		if (!Helper.isEmpty(value))
		{
			int detailsType = LocationDetailsType.fromString(value);

			if (detailsType == LocationDetailsType.NONE)
				throw new UnknownParameterValueException(LocationsErrorCodes.INVALID_PARAMETER_VALUE, "details", value);

			req.setDetails(detailsType);
		}

		value = request.getParameter("id");
		if (!Helper.isEmpty(value))
			req.setId(value);

		return req;
	}

	private static Geometry parseGeometry(String geomText) throws JSONException, Exception
	{
		Geometry geometry = GeometryJSON.parse(new JSONObject(geomText));

		if (geometry instanceof LineString && LocationsServiceSettings.getMaximumFeatureLength() > 0)
		{
			double length = GeomUtility.getLength(geometry, true);
			if (length > LocationsServiceSettings.getMaximumFeatureLength())
				throw new ParameterOutOfRangeException(LocationsErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "geometry", String.format("LineString length (%.1f) is greater than allowed maximum value (%.1f)", length, LocationsServiceSettings.getMaximumFeatureLength()), Double.toString(LocationsServiceSettings.getMaximumFeatureLength()));
		}
		else if (geometry instanceof Polygon && LocationsServiceSettings.getMaximumFeatureArea() > 0)
		{
			double area = GeomUtility.getArea(geometry, true);
			if (area > LocationsServiceSettings.getMaximumFeatureArea())
				throw new ParameterOutOfRangeException(LocationsErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "geometry", String.format("Polygon area (%.1f) is greater than allowed maximum value (%.1f)", area, LocationsServiceSettings.getMaximumFeatureArea()), Double.toString(LocationsServiceSettings.getMaximumFeatureArea()));
		}

		return geometry;
	}

	private static void checkSearchRadius(Geometry geom, double value) throws Exception
	{
		if (geom instanceof Point)
		{
			if (LocationsServiceSettings.getMaximumSearchRadiusForPoints() > 0 && LocationsServiceSettings.getMaximumSearchRadiusForPoints() < value)
				throw new ParameterOutOfRangeException(LocationsErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "radius", Double.toString(value), Double.toString(LocationsServiceSettings.getMaximumSearchRadiusForPoints()));
		}
		else if (geom instanceof LineString)
		{
			if (LocationsServiceSettings.getMaximumSearchRadiusForLinestrings() > 0 && LocationsServiceSettings.getMaximumSearchRadiusForLinestrings() < value)
				throw new ParameterOutOfRangeException(LocationsErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "radius", Double.toString(value), Double.toString(LocationsServiceSettings.getMaximumSearchRadiusForLinestrings()));
		}
		else if (geom instanceof Polygon)
		{
			if (LocationsServiceSettings.getMaximumSearchRadiusForPolygons() > 0 && LocationsServiceSettings.getMaximumSearchRadiusForPolygons() < value)
				throw new ParameterOutOfRangeException(LocationsErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "radius", Double.toString(value), Double.toString(LocationsServiceSettings.getMaximumSearchRadiusForPolygons()));
		}
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

	private static int[] parseIntArray(JSONArray array, String elemName) throws Exception
	{
		if (array.length() <= 0)
			return null;

		try
		{
			int[] res = new int[array.length()];
			for (int i = 0; i < array.length(); i++)
				res [i] = array.getInt(i);
			return res;
		}
		catch(Exception ex)
		{
			throw new StatusCodeException(StatusCode.BAD_REQUEST, LocationsErrorCodes.INVALID_PARAMETER_FORMAT,  "Unable to parse the element '" + elemName + "'. " + ex.getMessage());
		}
	}

	private static int[] parseIntArray(String strArray, String elemName) throws Exception
	{
		if (Helper.isEmpty(strArray))
			return null;

		try
		{
			String[] array = strArray.split(",");
			int[] res = new int[array.length];
			for (int i = 0; i < array.length; i++)
				res [i] = Integer.parseInt(array[i]);

			return res;
		}
		catch(Exception ex)
		{
			throw new StatusCodeException(StatusCode.BAD_REQUEST, LocationsErrorCodes.INVALID_PARAMETER_FORMAT,  "Unable to parse the element '" + elemName + "'. " + ex.getMessage());
		}
	}
}
