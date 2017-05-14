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
package heigit.ors.services.geocoding.requestprocessors.json;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import heigit.ors.geojson.GeometryJSON;
import heigit.ors.services.geocoding.GeocodingServiceSettings;
import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.InternalServerException;
import heigit.ors.exceptions.MissingParameterException;
import heigit.ors.exceptions.ParameterOutOfRangeException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.geocoding.geocoders.Geocoder;
import heigit.ors.geocoding.geocoders.GeocoderFactory;
import heigit.ors.geocoding.geocoders.GeocodingErrorCodes;
import heigit.ors.geocoding.geocoders.GeocodingResult;
import heigit.ors.geocoding.geocoders.GeocodingUtils;
import heigit.ors.services.geocoding.requestprocessors.GeocodingRequest;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.servlet.util.ServletUtility;
import heigit.ors.util.FormatUtility;
import heigit.ors.util.AppInfo; 

public class JsonGeocodingRequestProcessor extends AbstractHttpRequestProcessor {

	public JsonGeocodingRequestProcessor(HttpServletRequest request) throws Exception {
		super(request);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void process(HttpServletResponse response) throws Exception {
		String reqMethod = _request.getMethod();

		GeocodingRequest req = null;
		switch (reqMethod)
		{
		case "GET":
			req = new GeocodingRequest();

			String value = _request.getParameter("query");

			if (!Helper.isEmpty(value))
				req.setQuery(value);

			req.setLanguage(_request.getParameter("lang"));

			boolean inverseXY = true;
			value = _request.getParameter("latlng");

			if (Helper.isEmpty(value))
			{
				value = _request.getParameter("location");
				inverseXY = false;
			}

			if (!Helper.isEmpty(value))
			{
				String[] coords = value.split(",");
				if (coords.length != 2)
					throw new StatusCodeException(StatusCode.BAD_REQUEST, GeocodingErrorCodes.MISSING_PARAMETER,  "location parameter is either empty or has wrong number of values.");

				try
				{
					if (inverseXY)
						req.setLocation(new Coordinate(Double.parseDouble(coords[1]),Double.parseDouble(coords[0])));
					else
						req.setLocation(new Coordinate(Double.parseDouble(coords[0]),Double.parseDouble(coords[1])));
				}
				catch(NumberFormatException ex)
				{
					throw new StatusCodeException(StatusCode.BAD_REQUEST, GeocodingErrorCodes.INVALID_PARAMETER_FORMAT, "Unable to parse location coordinates.");
				}

				req.setLanguage(null);
				req.setLimit(1);
			}

			value = _request.getParameter("limit");
			if (!Helper.isEmpty(value))
			{
				int limit = Integer.parseInt(value);
				if (limit > GeocodingServiceSettings.getResponseLimit())
					throw new ParameterOutOfRangeException(GeocodingErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "limit", value, Integer.toString(GeocodingServiceSettings.getResponseLimit()));

				req.setLimit(limit);
			}

			value = _request.getParameter("bbox");
			if (!Helper.isEmpty(value))
			{
				String[] coords = value.split(",");
				if (coords == null || coords.length != 4)
					throw new StatusCodeException(StatusCode.BAD_REQUEST, GeocodingErrorCodes.INVALID_PARAMETER_VALUE, "BBox parameter is either empty or has wrong number of values.");

				Envelope bbox = null;
				
				try
				{
					bbox = new Envelope(Double.parseDouble(coords[0]),  Double.parseDouble(coords[2]), Double.parseDouble(coords[1]), Double.parseDouble(coords[3]));
				}
				catch(NumberFormatException ex)
				{
					throw new StatusCodeException(StatusCode.BAD_REQUEST, GeocodingErrorCodes.INVALID_PARAMETER_FORMAT, "Unable to parse bbox coordinates.");
				}

				req.setBBox(bbox);
			}

			value = _request.getParameter("id");
			if (!Helper.isEmpty(value))
				req.setId(value);
			break;
		case "POST":
			throw new StatusCodeException(StatusCode.METHOD_NOT_ALLOWED, "POST request is not supported.");  
		default:
			throw new StatusCodeException(StatusCode.METHOD_NOT_ALLOWED, "POST request is not supported.");
		}

		if (req == null)
			throw new StatusCodeException(StatusCode.BAD_REQUEST, "GeocodingRequest object is null.");
		else if (!req.isValid())
			throw new StatusCodeException(StatusCode.BAD_REQUEST, "Geocoding request parameters are missing or invalid.");

		try
		{
			Geocoder geocoder = GeocoderFactory.createGeocoder(GeocodingServiceSettings.getGeocoderName(), GeocodingServiceSettings.getGeocodingURL(), GeocodingServiceSettings.getReverseGeocodingURL(), GeocodingServiceSettings.getUserAgent()); 

			if (req.getLocation() != null)
			{
				Coordinate c = req.getLocation();
				GeocodingResult[] gresults = geocoder.reverseGeocode(c.x, c.y, req.getLimit(), req.getBBox());
				writeGeocodingResponse(response, req,  gresults);
			}
			else
			{
				if (Helper.isEmpty(req.getQuery()))
					throw new MissingParameterException(GeocodingErrorCodes.MISSING_PARAMETER, "query");
				
				GeocodingResult[] gresults = geocoder.geocode(req.getQuery(), req.getLanguage(), req.getLimit(), req.getBBox());
				writeGeocodingResponse(response, req, gresults);			
			}
		}
		catch(Exception ex)
		{
			throw new InternalServerException(GeocodingErrorCodes.UKNOWN);
		}
	}

	private void writeGeocodingResponse(HttpServletResponse response, GeocodingRequest request, GeocodingResult[] result) throws Exception
	{
		JSONObject resp = new JSONObject(true);

		JSONArray features = new JSONArray(result.length);
		resp.put("type", "FeatureCollection");        
		resp.put("features", features);

		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;

		Coordinate pos = request.getLocation();
		DistanceCalc dc = (pos != null) ? new com.graphhopper.util.DistanceCalcEarth() : null;

		int nResults = 0;

		for (int j = 0; j < result.length; j++) 
		{
			GeocodingResult gr = result[j];

			if (gr == null)
				continue;

			JSONObject feature = new JSONObject(true);
			feature.put("type", "Feature");

			JSONObject point = new JSONObject(true);
			point.put("type", "Point");
			JSONArray arrCoord = new JSONArray(2);

			arrCoord.put(FormatUtility.roundToDecimals(gr.longitude, 6));
			arrCoord.put(FormatUtility.roundToDecimals(gr.latitude, 6));
			point.put("coordinates", arrCoord);

			feature.put("geometry", point);

			JSONObject properties = new JSONObject(true);
			if (!Helper.isEmpty(gr.country))
				properties.put("country", gr.country);

			if (!Helper.isEmpty(gr.county))
				properties.put("county", gr.county);

			if (!Helper.isEmpty(gr.state))
				properties.put("state", gr.state);

			if (!Helper.isEmpty(gr.stateDistrict))
				properties.put("state_district", gr.stateDistrict);

			if (!Helper.isEmpty(gr.city))
				properties.put("city", gr.city);

			if (!Helper.isEmpty(gr.city))
				properties.put("city", gr.city);

			if (!Helper.isEmpty(gr.postalCode))
				properties.put("postal_code", gr.postalCode);

			if (!Helper.isEmpty(gr.street))
				properties.put("street", gr.street);

			if (!Helper.isEmpty(gr.houseNumber))
				properties.put("house_number", gr.houseNumber);

			if (!Helper.isEmpty(gr.objectName))
				properties.put("name", gr.objectName);
			else if (!Helper.isEmpty(gr.name))
				properties.put("name", gr.name);

			if (pos != null)
			{
				Coordinate loc = request.getLocation();
				double dist = dc.calcDist(gr.latitude, gr.longitude, loc.y, loc.x);
				properties.put("distance", FormatUtility.roundToDecimals(dist, 2));
				properties.put("confidence", GeocodingUtils.getDistanceAccuracyScore(dist)); 
			}

			feature.put("properties", properties);

			features.put(feature);

			if (minX > gr.longitude)
				minX = gr.longitude;
			if (minY > gr.latitude)
				minY = gr.latitude;
			if (maxX < gr.longitude)
				maxX = gr.longitude;
			if (maxY < gr.latitude)
				maxY = gr.latitude;

			nResults++;
		}

		if (nResults > 0)
			resp.put("bbox", GeometryJSON.toJSON(minX, minY, maxX, maxY));

		JSONObject info = new JSONObject();
		info.put("service", "geocoding");
		info.put("version", AppInfo.VERSION);
		if (!Helper.isEmpty( GeocodingServiceSettings.getAttribution()))
			info.put("attribution", GeocodingServiceSettings.getAttribution());
		info.put("timestamp", System.currentTimeMillis());

		JSONObject query = new JSONObject();
		query.put("query", request.getQuery());
		if (request.getLimit() > 0)
			query.put("limit", request.getLimit());
		if (request.getLanguage() != null)
			query.put("lang", request.getLanguage());
		if (request.getLocation() != null)
			query.put("location", GeometryJSON.toJSON(request.getLocation()));
		if (request.getId()!= null)
			query.put("id", request.getId());

		info.put("query", query);

		resp.put("info", info);

		ServletUtility.write(response, resp);
	}
}