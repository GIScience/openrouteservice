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

import org.json.JSONException;
import org.json.JSONObject;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import heigit.ors.geojson.GeometryJSON;
import heigit.ors.locations.LocationsRequest;
import heigit.ors.services.MissingParameterException;
import heigit.ors.services.ParameterOutOfRangeException;
import heigit.ors.services.locations.LocationsServiceSettings;
import heigit.ors.util.GeomUtility;
import heigit.ors.util.StreamUtility;

public class JsonLocationsRequestParser {

	public static LocationsRequest parseFromStream(InputStream stream) throws Exception 
	{
		LocationsRequest req = null;

		try 
		{
			JSONObject obj = new JSONObject(StreamUtility.readStream(stream));

			req = new LocationsRequest();		  

			String value = obj.optString("query");
			if (!Helper.isEmpty(value))
				req.setQuery(value);
			else
				throw new MissingParameterException("query");

			req.setLanguage(obj.optString("lang"));
			
			value = obj.optString("geometry");
			if (!Helper.isEmpty(value))
			{
				Geometry geom = parseGeometry(value);
				if (geom == null)
					throw new Exception("'geometry' parameter is incorrect.");	
				req.setGeometry(geom);
			}
			else
				throw new MissingParameterException("geometry");
			
			value = obj.optString("radius");
			if (!Helper.isEmpty(value))
			{
				double dvalue = Double.parseDouble(value);
				if (LocationsServiceSettings.getMaximumSearchRadius() > 0 && LocationsServiceSettings.getMaximumSearchRadius() < dvalue)
					throw new ParameterOutOfRangeException("radius", value, Double.toString(LocationsServiceSettings.getMaximumSearchRadius()));
				
				req.setRadius(dvalue);
			}
			else if (req.getGeometry() instanceof Point || req.getGeometry() instanceof LineString)
				throw new MissingParameterException("radius");
				
			value = obj.optString("limit");
			if (!Helper.isEmpty(value))
			{
				int ivalue = Integer.parseInt(value);
				if (LocationsServiceSettings.getResponseLimit() > 0 && LocationsServiceSettings.getResponseLimit() < ivalue)
					throw new ParameterOutOfRangeException("limit", value, Integer.toString(LocationsServiceSettings.getResponseLimit()));

				req.setLimit(ivalue);
			}

			value = obj.optString("id");
			if (!Helper.isEmpty(value))
				req.setId(value);			
		}
		catch(JSONException jex)
		{
			throw new Exception("Unable to parse JSON document. " + jex.getMessage());
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

		String value = request.getParameter("query");
		if (!Helper.isEmpty(value))
			req.setQuery(value);
		else
			throw new MissingParameterException("query");

		req.setLanguage(request.getParameter("lang"));

		value = request.getParameter("geometry");
		if (!Helper.isEmpty(value))
		{
			Geometry geom = parseGeometry(value);
			if (geom == null)
				throw new Exception("'geometry' parameter is incorrect.");

			req.setGeometry(geom);
		}
		else
			throw new MissingParameterException("geometry");
		
		value = request.getParameter("radius");
		if (!Helper.isEmpty(value))
		{
			double dvalue = Double.parseDouble(value);
			if (LocationsServiceSettings.getMaximumSearchRadius() > 0 && LocationsServiceSettings.getMaximumSearchRadius() < dvalue)
				throw new ParameterOutOfRangeException("radius", value, Double.toString(LocationsServiceSettings.getMaximumSearchRadius()));
		}
		else if (req.getGeometry() instanceof Point || req.getGeometry() instanceof LineString)
			throw new MissingParameterException("radius");

		value = request.getParameter("limit");
		if (!Helper.isEmpty(value))
		{
			int ivalue = Integer.parseInt(value);
			if (LocationsServiceSettings.getResponseLimit() > 0 && LocationsServiceSettings.getResponseLimit() < ivalue)
				throw new ParameterOutOfRangeException("limit", value, Integer.toString(LocationsServiceSettings.getResponseLimit()));
			
			req.setLimit(ivalue);
		}

		value = request.getParameter("bbox");
		if (!Helper.isEmpty(value))
		{
			String[] coords = value.split(",");
			if (coords == null || coords.length != 4)
				throw new Exception("BBox parameter is either empty or has wrong number of values.");

			Envelope bbox = new Envelope(Double.parseDouble(coords[0]),  Double.parseDouble(coords[2]), Double.parseDouble(coords[1]), Double.parseDouble(coords[3]));
			req.setBBox(bbox);
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
				throw new Exception(String.format("LineString length (%.1f) is greater than allowed maximum value (%.1f)", length, LocationsServiceSettings.getMaximumFeatureLength()));
		}
		else if (geometry instanceof Polygon && LocationsServiceSettings.getMaximumFeatureArea() > 0)
		{
			double area = GeomUtility.getArea(geometry, true);
			if (area > LocationsServiceSettings.getMaximumFeatureArea())
				throw new Exception(String.format("Polygon area (%.1f) is greater than allowed maximum value (%.1f)", area, LocationsServiceSettings.getMaximumFeatureArea()));
		}
		
		return geometry;
	}
}
