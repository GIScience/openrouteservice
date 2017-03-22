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

import java.util.List;
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
import heigit.ors.locations.LocationsCategory;
import heigit.ors.locations.LocationsCategoryClassifier;
import heigit.ors.locations.LocationsRequest;
import heigit.ors.locations.LocationsResult;
import heigit.ors.locations.LocationsSearchFilter;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.servlet.util.ServletUtility;
import heigit.ors.util.AppInfo;
import heigit.ors.util.OrderedJSONObjectFactory;

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
			req = JsonLocationsRequestParser.parseFromStream(_request);
			break;
		}

		if (req == null)
			throw new Exception("LocationRequest object is null.");

		if (!req.isValid())
			throw new Exception("Location request parameters are missing or invalid.");

		LocationsDataProvider provider = LocationsDataProviderFactory.getProvider(LocationsServiceSettings.getProviderName(), LocationsServiceSettings.getProviderParameters());

		switch(req.getType())
		{
		case  POIS:
			writeLocationsResponse(response, req, provider.findLocations(req));			
			break;
		case CATEGORIES:
			writeCategoriesResponse(response, req, provider.findCategories(req));
			break;
		case CATEGORIESLIST:
			writeCategoriesListResponse(response);
			break;
		case UNKNOWN:
			break;
		}
	}
	
	private void writeCategoriesListResponse(HttpServletResponse response) throws Exception
	{
		JSONObject resp = new JSONObject();
		
		resp.put("categories", LocationsCategoryClassifier.getCategoriesList());
		
		writeInfoSection(resp, null);
		
		byte[] bytes = resp.toString().getBytes("UTF-8");
		ServletUtility.write(response, bytes, "text/json", "UTF-8");
	}
	
	private void writeCategoriesResponse(HttpServletResponse response, LocationsRequest request, List<LocationsCategory> categories) throws Exception
	{
		JSONObject resp = new JSONObject();

		JSONObject jLocations = OrderedJSONObjectFactory.create();
		resp.put("places", jLocations);

		if (categories != null)
		{
			long totalCount = 0;

			for (int j = 0; j < categories.size(); j++) 
			{
				JSONObject jCategory = OrderedJSONObjectFactory.create();
				
				LocationsCategory cat = categories.get(j);
				
				for(Map.Entry<Integer, Long> stats : cat.getStats().entrySet())
				{
					jCategory.put(stats.getKey().toString(), stats.getValue());
				}
				
				jCategory.put("total_count", cat.getTotalCount());
				jLocations.put(cat.getCategoryName(), jCategory);
				
				totalCount += cat.getTotalCount(); 
			}
			
			jLocations.put("total_count", totalCount);
		}

		writeInfoSection(resp, request);

		byte[] bytes = resp.toString().getBytes("UTF-8");
		ServletUtility.write(response, bytes, "text/json", "UTF-8");
	}

	private void writeLocationsResponse(HttpServletResponse response, LocationsRequest request, List<LocationsResult> locations) throws Exception
	{
		JSONObject resp = OrderedJSONObjectFactory.create();

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

			for (int j = 0; j < locations.size(); j++) 
			{
				LocationsResult lr = locations.get(j);

				if (lr == null)
					continue;

				Geometry geom = lr.getGeometry();

				JSONObject feature = OrderedJSONObjectFactory.create();
				feature.put("type", "Feature");

				JSONObject point = OrderedJSONObjectFactory.create();
				point.put("type", geom.getClass().getSimpleName());

				point.put("coordinates", GeometryJSON.toJSON(geom, buffer));

				feature.put("geometry", point);

				JSONObject properties = OrderedJSONObjectFactory.create();

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

		writeInfoSection(resp, request);

		byte[] bytes = resp.toString().getBytes("UTF-8");
		ServletUtility.write(response, bytes, "text/json", "UTF-8");
	}
	
	private void writeInfoSection(JSONObject jResponse, LocationsRequest request)
	{
		JSONObject jInfo = OrderedJSONObjectFactory.create();
		jInfo.put("service", "locations");
		jInfo.put("version", AppInfo.VERSION);
		if (!Helper.isEmpty(LocationsServiceSettings.getAttribution()))
			jInfo.put("attribution", LocationsServiceSettings.getAttribution());
		jInfo.put("timestamp", System.currentTimeMillis());

		if (request != null)
		{
			JSONObject jQuery = OrderedJSONObjectFactory.create();

			writeFilterSection(jQuery, request.getSearchFilter());

			if (request.getRadius() > 0)
				jQuery.put("radius", request.getRadius());
			if (request.getLimit() > 0)
				jQuery.put("limit", request.getLimit());
			if (!Helper.isEmpty(request.getLanguage()))
				jQuery.put("lang", request.getLanguage());
			if (request.getId() != null)
				jQuery.put("id", request.getId());

			jInfo.put("query", jQuery);
		}

		jResponse.put("info", jInfo);
	}
	
	private void writeFilterSection(JSONObject jQuery, LocationsSearchFilter query)
	{
		JSONObject jFilter = OrderedJSONObjectFactory.create();
		if (query.getCategoryGroupIds() != null)
			jFilter.put("category_group_ids", new JSONArray(query.getCategoryGroupIds()));
		if (query.getCategoryIds() != null)
			jFilter.put("category_ids", new JSONArray(query.getCategoryIds()));
		if (!Helper.isEmpty(query.getName()))
			jFilter.put("name", query.getName());
		if (!Helper.isEmpty(query.getWheelchair()))
			jFilter.put("wheelchair", query.getWheelchair());
		if (!Helper.isEmpty(query.getSmoking()))
			jFilter.put("smoking", query.getSmoking());
		if (query.getFee() != null)
			jFilter.put("fee", query.getFee());
		
		jQuery.put("filter", jFilter);
	}
}
