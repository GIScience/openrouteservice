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

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import heigit.ors.geojson.GeometryJSON;
import heigit.ors.services.locations.requestprocessors.json.JsonLocationsRequestParser;
import heigit.ors.services.locations.LocationsServiceSettings;
import heigit.ors.locations.providers.LocationsDataProvider;
import heigit.ors.locations.providers.LocationsDataProviderFactory;
import heigit.ors.locations.LocationsRequest;
import heigit.ors.locations.LocationsResult;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.servlet.util.ServletUtility;
import heigit.ors.util.AppInfo;

public class JsonLocationsRequestProcessor extends AbstractHttpRequestProcessor
{

	public JsonLocationsRequestProcessor(HttpServletRequest request) 
	{
		super(request);
	}

	@Override
	public void process(HttpServletResponse response) throws Exception
	{
		String reqMethod = _request.getMethod();

		LocationsRequest req = null;
		switch (reqMethod)
		{
		case "GET":
			req = JsonLocationsRequestParser.parseFromRequestParams(_request);
			break;
		case "POST":
			req = JsonLocationsRequestParser.parseFromStream(_request.getInputStream());  
		}

		if (req == null)
			throw new Exception("LocationRequest object is null.");

		if (!req.isValid())
			throw new Exception("Location request parameters are missing or invalid.");

		LocationsDataProvider provider = LocationsDataProviderFactory.getProvider(LocationsServiceSettings.getProviderName(), LocationsServiceSettings.getProviderParameters());

		LocationsResult[] locations = provider.findLocations(req);

		writeLocationsResponse(response, req, locations);			
	}

	private void writeLocationsResponse(HttpServletResponse response, LocationsRequest request, LocationsResult[] locations) throws Exception
	{
		JSONObject resp = createJsonObject();

		JSONArray features = new JSONArray();
		resp.put("type", "FeatureCollection");        
		resp.put("features", features);

		if (locations != null)
		{
			StringBuffer buffer = new StringBuffer();

			double minX = Double.MAX_VALUE;
			double minY = Double.MAX_VALUE;
			double maxX = Double.MIN_VALUE;
			double maxY = Double.MIN_VALUE;

			int nResults = 0;

			for (int j = 0; j < locations.length; j++) 
			{
				LocationsResult lr = locations[j];

				if (lr == null)
					continue;

				Geometry geom = lr.getGeometry();

				JSONObject feature = createJsonObject();
				feature.put("type", "Feature");

				JSONObject point = createJsonObject();
				point.put("type", geom.getClass().getSimpleName());

				point.put("coordinates", GeometryJSON.toJSON(geom, buffer));

				feature.put("geometry", point);

				JSONObject properties = createJsonObject();

				Map<String, String> props = lr.getProperties();
				if (props.size() > 0)
				{
					for(Map.Entry<String, String> entry : props.entrySet())
						properties.put(entry.getKey(), entry.getValue());
				}

				feature.put("properties", properties);

				features.put(feature);

				Envelope env = geom.getEnvelopeInternal();

				if (minX > env.getMinX())
					minX =  env.getMinX();
				if (minY > env.getMinY())
					minY = env.getMinY();
				if (maxX < env.getMaxX())
					maxX = env.getMaxX();
				if (maxY < env.getMaxY())
					maxY = env.getMaxY();

				nResults++;
			}

			if (nResults > 0)
				resp.put("bbox", GeometryJSON.toJSON(minX, minY, maxX, maxY));
		}

		JSONObject info = new JSONObject();
		info.put("service", "location");
		info.put("version", AppInfo.VERSION);
		if (!Helper.isEmpty(LocationsServiceSettings.getAttribution()))
			info.put("attribution", LocationsServiceSettings.getAttribution());
		info.put("timestamp", System.currentTimeMillis());

		JSONObject query = new JSONObject();
		query.put("query", request.getQuery());
		if (request.getRadius() > 0)
			query.put("radius", request.getLimit());
		if (request.getLimit() > 0)
			query.put("limit", request.getLimit());
		if (!Helper.isEmpty(request.getLanguage()))
			query.put("lang", request.getLanguage());
		if (request.getId() != null)
			query.put("id", request.getId());

		info.put("query", query);

		resp.put("info", info);

		byte[] bytes = resp.toString().getBytes("UTF-8");
		ServletUtility.write(response, bytes, "text/json", "UTF-8");
	}

	private JSONObject createJsonObject()
	{
		Map<String,String > map =  new LinkedHashMap<String, String>();
		return new JSONObject(map);
	}
}
