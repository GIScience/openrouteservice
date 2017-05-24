package heigit.ors.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.ParameterOutOfRangeException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.geojson.GeometryJSON;
import heigit.ors.locations.LocationsErrorCodes;
import heigit.ors.services.locations.LocationsServiceSettings;

public class JsonUtility {
	public static Geometry parseGeometry(String geomText) throws JSONException, Exception
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
	
	public static int[] parseIntArray(JSONArray array, String elemName) throws Exception
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

	public static int[] parseIntArray(String strArray, String elemName) throws Exception
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
