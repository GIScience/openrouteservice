/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
 */
package heigit.ors.services.geocoding.requestprocessors.json;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import heigit.ors.geojson.GeometryJSON;
import heigit.ors.config.AppConfig;
import heigit.ors.services.geocoding.GeocodingServiceSettings;
import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.InternalServerException;
import heigit.ors.exceptions.MissingParameterException;
import heigit.ors.exceptions.ParameterOutOfRangeException;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.geocoding.geocoders.Address;
import heigit.ors.geocoding.geocoders.CircleSearchBoundary;
import heigit.ors.geocoding.geocoders.Geocoder;
import heigit.ors.geocoding.geocoders.GeocoderFactory;
import heigit.ors.geocoding.geocoders.GeocodingErrorCodes;
import heigit.ors.geocoding.geocoders.GeocodingResult;
import heigit.ors.geocoding.geocoders.RectSearchBoundary;
import heigit.ors.services.geocoding.requestprocessors.GeocodingRequest;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.servlet.util.ServletUtility;
import heigit.ors.util.FormatUtility;
import heigit.ors.util.AppInfo;


public class JsonGeocodingRequestProcessor extends AbstractHttpRequestProcessor {
	private static final Logger LOGGER = Logger.getLogger(JsonGeocodingRequestProcessor.class.getName());
	
	public JsonGeocodingRequestProcessor(HttpServletRequest request) throws Exception {
		super(request);
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
			{
				if (value.contains("{"))
				{
					// we expect structured query with json
					try
					{
						JSONObject jAddress = new JSONObject(value);
						req.setQueryAddress(Address.fromJson(jAddress));
					}
					catch(Exception ex)
					{
						throw new ParameterValueException(GeocodingErrorCodes.INVALID_PARAMETER_FORMAT, "query");
					}
					
					if (!req.getQueryAddress().isValid())
						throw new ParameterValueException(GeocodingErrorCodes.INVALID_PARAMETER_VALUE, "query");						
				}
				else
					req.setQueryString(value);
			}

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
					throw new ParameterValueException(GeocodingErrorCodes.INVALID_PARAMETER_FORMAT,  "location");

				try
				{
					if (inverseXY)
						req.setLocation(new Coordinate(Double.parseDouble(coords[1]),Double.parseDouble(coords[0])));
					else
						req.setLocation(new Coordinate(Double.parseDouble(coords[0]),Double.parseDouble(coords[1])));
				}
				catch(NumberFormatException ex)
				{
					throw new ParameterValueException(GeocodingErrorCodes.INVALID_PARAMETER_VALUE, "location");
				}

				req.setLanguage(null);
				req.setLimit(1);
			}
			else if (Helper.isEmpty(req.getQueryString()) && (req.getQueryAddress() == null || !req.getQueryAddress().isValid()))
			{
				throw new MissingParameterException(GeocodingErrorCodes.MISSING_PARAMETER, "query/location");
			}

			value = _request.getParameter("limit");
			if (!Helper.isEmpty(value))
			{
				int limit = 1;
				
				try
				{
					limit = Integer.parseInt(value);
				}
				catch(NumberFormatException nfex)
				{
					throw new ParameterValueException(GeocodingErrorCodes.INVALID_PARAMETER_VALUE, "limit");
				}
				
				if (limit > GeocodingServiceSettings.getResponseLimit())
					throw new ParameterOutOfRangeException(GeocodingErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "limit", value, Integer.toString(GeocodingServiceSettings.getResponseLimit()));

				req.setLimit(limit);
			}

			value = _request.getParameter("boundary_type");
			if (!Helper.isEmpty(value))
			{
				if ("rect".equalsIgnoreCase(value))
				{
					value = _request.getParameter("rect");
					if (Helper.isEmpty(value))
						throw new MissingParameterException(GeocodingErrorCodes.MISSING_PARAMETER, "rect");
					
					String[] coords = value.split(",");
					if (coords == null || coords.length != 4)
						throw new ParameterValueException(GeocodingErrorCodes.INVALID_PARAMETER_VALUE, "rect");

					Envelope bbox = null;

					try
					{
						bbox = new Envelope(Double.parseDouble(coords[0]),  Double.parseDouble(coords[2]), Double.parseDouble(coords[1]), Double.parseDouble(coords[3]));
					}
					catch(NumberFormatException ex)
					{
						throw new ParameterValueException(GeocodingErrorCodes.INVALID_PARAMETER_FORMAT, "rect");
					}

					RectSearchBoundary rsb = new RectSearchBoundary(bbox);
					req.setBoundary(rsb);
				}
				else if ("circle".equalsIgnoreCase(value))
				{
					value = _request.getParameter("circle");
					
					if (Helper.isEmpty(value))
						throw new MissingParameterException(GeocodingErrorCodes.MISSING_PARAMETER, "circle");
					
					String[] values = value.split(",");
					if (values == null || values.length != 3)
						throw new ParameterValueException(GeocodingErrorCodes.INVALID_PARAMETER_VALUE, "circle");

					CircleSearchBoundary csb = null;
					try
					{
						csb = new CircleSearchBoundary(Double.parseDouble(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]));
					}
					catch(NumberFormatException nfex)
					{
						throw new ParameterValueException(GeocodingErrorCodes.INVALID_PARAMETER_FORMAT, "circle");
					}
					
					req.setBoundary(csb);
				}
				else
				{
					throw new ParameterValueException(GeocodingErrorCodes.INVALID_PARAMETER_VALUE, "boundary_type");
				}
			}
			
			value = _request.getParameter("minimum_confidence");
			if (!Helper.isEmpty(value))
			{
				try
				{
					req.setMinimumConfidence(Float.parseFloat(value));
				}
				catch(NumberFormatException nfex)
				{
					throw new ParameterValueException(GeocodingErrorCodes.INVALID_PARAMETER_FORMAT, "minimum_confidence");
				}
			}
			
			value = _request.getParameter("id");
			if (!Helper.isEmpty(value))
				req.setId(value);
			break;
		case "POST":
			throw new StatusCodeException(StatusCode.METHOD_NOT_ALLOWED, GeocodingErrorCodes.UNKNOWN, "POST request is not supported.");  
		default:
			throw new StatusCodeException(StatusCode.METHOD_NOT_ALLOWED, GeocodingErrorCodes.UNKNOWN, "Unknown request type.");
		}

        if (!req.isValid())
			throw new StatusCodeException(StatusCode.BAD_REQUEST, GeocodingErrorCodes.UNKNOWN, "Geocoding request parameters are missing or invalid.");

		try
		{
			Geocoder geocoder = GeocoderFactory.createGeocoder(GeocodingServiceSettings.getGeocoderName(), GeocodingServiceSettings.getGeocodingURL(), GeocodingServiceSettings.getReverseGeocodingURL(), GeocodingServiceSettings.getUserAgent()); 

			if (req.getLocation() != null)
			{
				Coordinate c = req.getLocation();
				GeocodingResult[] gresults = geocoder.reverseGeocode(c.x, c.y, req.getLimit());
				writeGeocodingResponse(response, req,  gresults);
			}
			else
			{
				if (Helper.isEmpty(req.getQueryString()) && req.getQueryAddress() == null)
					throw new MissingParameterException(GeocodingErrorCodes.MISSING_PARAMETER, "query");
				
				GeocodingResult[] gresults = null;
				
				if (req.getQueryAddress() != null)
					gresults = geocoder.geocode(req.getQueryAddress(), req.getLanguage(), req.getBoundary(), req.getLimit());
				else
					gresults = geocoder.geocode(req.getQueryString(), req.getLanguage(), req.getBoundary(), req.getLimit());
				
				writeGeocodingResponse(response, req, gresults);			
			}
		}
		catch(StatusCodeException sce) {
			// Do not log, just throw
			throw sce;
		}
		catch(Exception ex)
		{
			LOGGER.error(ex);
			throw new InternalServerException(GeocodingErrorCodes.UNKNOWN);
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
		int nResults = 0;

		for (int j = 0; j < result.length; j++) 
		{
			GeocodingResult gr = result[j];

			if (gr == null || gr.confidence < request.getMinimumConfidence())
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

			if (!Helper.isEmpty(gr.countryCode))
				properties.put("country_code", gr.countryCode);

			if (!Helper.isEmpty(gr.county))
				properties.put("county", gr.county);

			if (!Helper.isEmpty(gr.region))
				properties.put("region", gr.region);
			
			if (!Helper.isEmpty(gr.municipality))
				properties.put("municipality", gr.municipality);

			if (!Helper.isEmpty(gr.locality))
				properties.put("locality", gr.locality);

			if (!Helper.isEmpty(gr.postalCode))
				properties.put("postal_code", gr.postalCode);

			if (!Helper.isEmpty(gr.borough))
				properties.put("borough", gr.borough);

			if (!Helper.isEmpty(gr.neighbourhood))
				properties.put("neighbourhood", gr.neighbourhood);
			
			if (!Helper.isEmpty(gr.street))
				properties.put("street", gr.street);

			if (!Helper.isEmpty(gr.houseNumber))
				properties.put("house_number", gr.houseNumber);

			if (!Helper.isEmpty(gr.objectName))
				properties.put("name", gr.objectName);
			else if (!Helper.isEmpty(gr.name))
				properties.put("name", gr.name);
			
			if (!Helper.isEmpty(gr.placeType))
				properties.put("place_type", gr.placeType);

			if (pos != null)
				properties.put("distance", FormatUtility.roundToDecimals(gr.distance, 2));

			properties.put("confidence", FormatUtility.roundToDecimals(gr.confidence, 2));

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
		info.put("engine", AppInfo.getEngineInfo());
		if (!Helper.isEmpty( GeocodingServiceSettings.getAttribution()))
			info.put("attribution", GeocodingServiceSettings.getAttribution());
		info.put("timestamp", System.currentTimeMillis());

		if (AppConfig.hasValidMD5Hash())
			info.put("osm_file_md5_hash", AppConfig.getMD5Hash());

		JSONObject query = new JSONObject();
		if (request.getQueryAddress() != null)
			query.put("query", request.getQueryAddress().toString());
		else
			query.put("query", request.getQueryString());
		
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

		// Check if there were problems
		if(nResults == 0) {
			throw new StatusCodeException(StatusCode.NOT_FOUND, GeocodingErrorCodes.ADDRESS_NOT_FOUND, "No address found.");
		} else {
			ServletUtility.write(response, resp);
		}
	}
}