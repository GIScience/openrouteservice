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

import java.util.List;

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
import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.MissingParameterException;
import heigit.ors.exceptions.ParameterOutOfRangeException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.geojson.GeometryJSON;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.services.isochrones.IsochronesServiceSettings;
import heigit.ors.services.isochrones.requestprocessors.json.JsonIsochroneRequestParser;
import heigit.ors.services.isochrones.IsochroneRequest;
import heigit.ors.isochrones.IsochroneSearchParameters;
import heigit.ors.isochrones.IsochroneUtility;
import heigit.ors.isochrones.IsochronesErrorCodes;
import heigit.ors.isochrones.IsochronesIntersection;
import heigit.ors.common.TravelRangeType;
import heigit.ors.isochrones.Isochrone;
import heigit.ors.isochrones.IsochroneMap;
import heigit.ors.isochrones.IsochroneMapCollection;
import heigit.ors.routing.RoutingProfileManager;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.servlet.util.ServletUtility;
import heigit.ors.util.FormatUtility;
import heigit.ors.util.AppInfo;
import heigit.ors.util.StringUtility;

public class JsonIsochronesRequestProcessor extends AbstractHttpRequestProcessor 
{

	public JsonIsochronesRequestProcessor(HttpServletRequest request) throws Exception
	{
		super(request);
	}

	@Override
	public void process(HttpServletResponse response) throws Exception 
	{
		String reqMethod = _request.getMethod();

		IsochroneRequest req = null;
		switch (reqMethod)
		{
		case "GET":
			req = JsonIsochroneRequestParser.parseFromRequestParams(_request);
			break;
		///case "POST":  needs to be implemented
		//	req = JsonIsochroneRequestParser.parseFromStream(_request.getInputStream());  
		//	break;
		default:
			throw new StatusCodeException(StatusCode.METHOD_NOT_ALLOWED);
		}

		if (req == null)
			throw new StatusCodeException(StatusCode.BAD_REQUEST, IsochronesErrorCodes.UNKNOWN, "IsochronesRequest object is null.");

		if (!req.isValid())
			throw new StatusCodeException(StatusCode.BAD_REQUEST, IsochronesErrorCodes.UNKNOWN, "IsochronesRequest is not valid.");

		if (IsochronesServiceSettings.getAllowComputeArea() == false && req.hasAttribute("area"))
			throw new StatusCodeException(StatusCode.BAD_REQUEST, IsochronesErrorCodes.FEATURE_NOT_SUPPORTED, "Area computation is not enabled.");

		if (req.getLocations().length > IsochronesServiceSettings.getMaximumLocations())
			throw new ParameterOutOfRangeException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "locations", Integer.toString(req.getLocations().length), Integer.toString(IsochronesServiceSettings.getMaximumLocations()));

		if (req.getMaximumRange() > IsochronesServiceSettings.getMaximumRange(req.getRangeType()))
			throw new ParameterOutOfRangeException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "range", Integer.toString(IsochronesServiceSettings.getMaximumRange(req.getRangeType())), Double.toString(req.getMaximumRange()));

		if (IsochronesServiceSettings.getMaximumIntervals() > 0)
		{
			if (IsochronesServiceSettings.getMaximumIntervals() < req.getRanges().length)
				throw new ParameterOutOfRangeException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "range", Integer.toString(req.getRanges().length), Integer.toString(IsochronesServiceSettings.getMaximumIntervals()));
		}

		Coordinate[] coords = req.getLocations();
		if (coords != null)
		{
			IsochroneMapCollection isoMaps = new IsochroneMapCollection();

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

	private void writeResponse(HttpServletResponse response, IsochroneRequest request, IsochroneMapCollection isochroneMaps) throws Exception
	{
		JSONObject jResp = new JSONObject(true);

		jResp.put("type", "FeatureCollection");
		
		JSONArray jFeatures = new JSONArray(isochroneMaps.getIsochronesCount());
		jResp.put("features", jFeatures);

		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;

		int groupIndex = 0;
		boolean includeArea = request.hasAttribute("area");
		boolean includeReachFactor = request.getRangeType() == TravelRangeType.Time && request.hasAttribute("reachfactor");
		String units = request.getUnits() != null ? request.getUnits().toLowerCase() : null;

		for (IsochroneMap isoMap : isochroneMaps.getIsochroneMaps())
		{
			for (Isochrone isoLine : isoMap.getIsochrones()) 
			{
				Polygon isoPoly = (Polygon)isoLine.getGeometry();
				LineString shell = isoPoly.getExteriorRing();
				JSONObject jFeature = new JSONObject(true);
				jFeature.put("type", "Feature");

				JSONObject jPolygon = new JSONObject(true);
				jPolygon.put("type", "Polygon");

				jPolygon.put("coordinates", GeometryJSON.toJSON(isoPoly));

				jFeature.put("geometry", jPolygon);

				JSONObject jProperties = new JSONObject(true);

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
					JSONObject jFeature = new JSONObject(true);
					jFeature.put("type", "Feature");

					JSONObject jGeometry = new JSONObject(true);
					jGeometry.put("type", geom.getGeometryType());
					jGeometry.put("coordinates", GeometryJSON.toJSON(geom, null));

					jFeature.put("geometry", jGeometry);

					JSONObject jProperties = new JSONObject(true);

					JSONArray jContours = new JSONArray(isoIntersection.getContourRefs().size());
					jProperties.put("contours", jContours);

					for(Pair<Integer, Integer> ref : isoIntersection.getContourRefs())
					{
						JSONArray jRef = new JSONArray(2);
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

		jQuery.put("locations", GeometryJSON.toJSON(request.getLocations(), false));

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

		if (request.getId() != null)
			jQuery.put("id", request.getId());

		jInfo.put("query", jQuery);

		jResp.put("info", jInfo);

		ServletUtility.write(response, jResp);
	}
}
