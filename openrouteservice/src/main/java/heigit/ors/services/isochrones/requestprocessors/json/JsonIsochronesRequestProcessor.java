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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import heigit.ors.common.Pair;
import heigit.ors.geojson.GeometryJSON;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.services.isochrones.IsochronesServiceSettings;
import heigit.ors.services.isochrones.requestprocessors.json.JsonIsochroneRequestParser;
import heigit.ors.services.isochrones.IsochroneRequest;
import heigit.ors.isochrones.IsochroneSearchParameters;
import heigit.ors.isochrones.IsochroneUtility;
import heigit.ors.isochrones.IsochronesIntersection;
import heigit.ors.isochrones.Isochrone;
import heigit.ors.isochrones.IsochroneMap;
import heigit.ors.routing.RoutingProfileManager;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.servlet.util.ServletUtility;
import heigit.ors.util.FormatUtility;
import heigit.ors.util.AppInfo;
import heigit.ors.util.StringUtility;

public class JsonIsochronesRequestProcessor extends AbstractHttpRequestProcessor {

	public JsonIsochronesRequestProcessor(HttpServletRequest request) {
		super(request);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void process(HttpServletResponse response) throws Exception {
		String reqMethod = _request.getMethod();

		IsochroneRequest req = null;
		switch (reqMethod)
		{
		case "GET":
			req = JsonIsochroneRequestParser.parseFromRequestParams(_request);
			break;
		case "POST":
			req = JsonIsochroneRequestParser.parseFromStream(_request.getInputStream());  
			break;
		}

		if (req == null)
			throw new Exception("IsochronesRequest object is null.");

		if (req.getRouteSearchParameters().getProfileType() == RoutingProfileType.UNKNOWN)
		{
			String profileName = _request.getParameter("profile");
			if (Helper.isEmpty(profileName))
				throw new Exception("Unknown profile name.");
			else
				throw new Exception("Unknown profile name '" + profileName +"'.");
		}

		if (!req.isValid())
			throw new Exception("IsochronesRequest is not valid.");

		if (IsochronesServiceSettings.getAllowComputeArea() == false && req.hasAttribute("area"))
			throw new Exception("Area computation is not allowed.");

		if (req.getLocations().length > IsochronesServiceSettings.getMaximumLocations())
			throw new Exception("Number of requested locations is greater than allowed.");

		if (req.getMaximumRange() > IsochronesServiceSettings.getMaximumRange(req.getRangeType()))
			throw new Exception("Requested range is greater than allowed. Maximum value is "+ IsochronesServiceSettings.getMaximumRange(req.getRangeType()) +".");

		if (IsochronesServiceSettings.getMaximumIntervals() > 0)
		{
			if (IsochronesServiceSettings.getMaximumIntervals() < req.getRanges().length)
				throw new Exception("Number of intervals is greater than allowed. Maximum value is " + IsochronesServiceSettings.getMaximumIntervals() + ".");
		}

		Coordinate[] coords = req.getLocations();
		if (coords != null)
		{
			List<IsochroneMap> isoMaps = new ArrayList<IsochroneMap>();

			IsochroneSearchParameters searchParams = req.getSearchParameters(coords[0]);
			searchParams.setRouteParameters(req.getRouteSearchParameters());

			for (int i = 0;i < coords.length; ++i){
				searchParams.setLocation(coords[i]);
				IsochroneMap isochroneMap = RoutingProfileManager.getInstance().buildIsochrone(searchParams);
				isoMaps.add(isochroneMap);
			}

			writeResponse(response, req, isoMaps);
		}
	}

	private void writeResponse(HttpServletResponse response, IsochroneRequest request, List<IsochroneMap> isochroneMaps) throws Exception
	{
		JSONObject jResp = createJsonObject();

		jResp.put("type", "FeatureCollection");        
		JSONArray jFeatures = new JSONArray();
		jResp.put("features", jFeatures);

		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;

		int groupIndex = 0;
		boolean includeArea = request.hasAttribute("area");
		boolean includeReachFactor = request.hasAttribute("reachfactor");
		String units = request.getUnits() != null ? request.getUnits().toLowerCase() : null;

		for (IsochroneMap isoMap : isochroneMaps)
		{
			for (Isochrone isoLine : isoMap.getIsochrones()) 
			{
				Polygon isoPoly = (Polygon)isoLine.getGeometry();
				LineString shell = isoPoly.getExteriorRing();
				JSONObject jFeature = createJsonObject();
				jFeature.put("type", "Feature");

				JSONObject jPolygon = createJsonObject();
				jPolygon.put("type", "Polygon");

				jPolygon.put("coordinates", GeometryJSON.toJSON(isoPoly));

				jFeature.put("geometry", jPolygon);

				JSONObject jProperties = createJsonObject();

				jProperties.put("group_index", groupIndex);
				jProperties.put("value", isoLine.getValue());

				jProperties.put("center", GeometryJSON.toJSON(isoMap.getCenter()));

				if (includeArea || includeReachFactor)
				{
					double area = isoLine.getArea(units);
					if (includeArea)
						jProperties.put("area", FormatUtility.roundToDecimals(area, 4));
					if (includeReachFactor)
					{
						double r  = isoLine.getMaxRadius(units);
						double maxArea = Math.PI * r * r;

						jProperties.put("reachfactor", FormatUtility.roundToDecimals(area/maxArea, 4));
					}
				}

				jFeature.put("properties", jProperties);

				jFeatures.put(jFeature);

				Envelope env = shell.getEnvelopeInternal();
				if (minX > env.getMinX())
					minX = env.getMinX();
				if (minY > env.getMinY())
					minY = env.getMinY();
				if (maxX < env.getMaxX())
					maxX = env.getMaxX();
				if (maxY < env.getMaxY())
					maxY = env.getMaxY();
			}

			groupIndex++;
		}

		if (request.getIncludeIntersections())
		{
			List<IsochronesIntersection> isoIntersections = IsochroneUtility.computeIntersections(isochroneMaps);
			if (isoIntersections != null && !isoIntersections.isEmpty())
			{
				for (IsochronesIntersection isoIntersection : isoIntersections)
				{
					Geometry geom = isoIntersection.getGeometry();
					JSONObject jFeature = createJsonObject();
					jFeature.put("type", "Feature");

					JSONObject jGeometry = createJsonObject();
					jGeometry.put("type", geom.getGeometryType());
					jGeometry.put("coordinates", GeometryJSON.toJSON(geom, null));

					jFeature.put("geometry", jGeometry);

					JSONObject jProperties = createJsonObject();

					JSONArray jContours = new JSONArray();
					jProperties.put("contours", jContours);

					for(Pair<Integer, Integer> ref : isoIntersection.getContourRefs())
					{
						JSONArray jRef = new JSONArray();
						jRef.put(ref.first);
						jRef.put(ref.second);
						jContours.put(jRef);
					}

					if (includeArea)
						jProperties.put("area", FormatUtility.roundToDecimals(isoIntersection.getArea(units), 4));

					jFeature.put("properties", jProperties);

					jFeatures.put(jFeature);
				}
			}
		}

		jResp.put("bbox", GeometryJSON.toJSON(minX, minY, maxX, maxY));

		JSONObject jInfo = new JSONObject();
		jInfo.put("service", "isochrones");
		jInfo.put("version", AppInfo.VERSION);
		if (!Helper.isEmpty(IsochronesServiceSettings.getAttribution()))
			jInfo.put("attribution", IsochronesServiceSettings.getAttribution());
		jInfo.put("timestamp", System.currentTimeMillis());

		JSONObject jQuery = new JSONObject();

		jQuery.put("profile", RoutingProfileType.getName(request.getRouteSearchParameters().getProfileType()));

		if (request.getRangeType() != null)
			jQuery.put("range_type", request.getRangeType().toString().toLowerCase());

		jQuery.put("ranges", StringUtility.arrayToString(request.getRanges(), ","));

		jQuery.put("locations", GeometryJSON.toJSON(request.getLocations()));

		if (request.getUnits() != null)
			jQuery.put("units", request.getUnits());
		
		if (request.getLocationType() != null)
			jQuery.put("location_type", request.getLocationType());

		if (request.getAttributes() != null)
			jQuery.put("attributes", StringUtility.combine(request.getAttributes(), "|"));

		if (request.getCalcMethod() != null)
			jQuery.put("calc_method", request.getCalcMethod());

		if (!Helper.isEmpty(request.getRouteSearchParameters().getOptions()))
			jQuery.put("options", new JSONObject(request.getRouteSearchParameters().getOptions()));

		jInfo.put("query", jQuery);

		jResp.put("info", jInfo);

		byte[] bytes = jResp.toString().getBytes("UTF-8");
		ServletUtility.write(response, bytes, "text/json", "UTF-8");
	}

	private JSONObject createJsonObject()
	{
		Map<String,String > map =  new LinkedHashMap<String, String>();
		return new JSONObject(map);
	}
}
