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
package heigit.ors.services.isochrones.requestprocessors.json;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import heigit.ors.common.AttributeValue;
import heigit.ors.common.Pair;
import heigit.ors.common.StatusCode;
import heigit.ors.config.AppConfig;
import heigit.ors.exceptions.ParameterOutOfRangeException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.geojson.GeometryJSON;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.services.isochrones.IsochronesServiceSettings;
import heigit.ors.services.isochrones.requestprocessors.json.JsonIsochroneRequestParser;
import heigit.ors.isochrones.IsochroneSearchParameters;
import heigit.ors.isochrones.IsochroneUtility;
import heigit.ors.isochrones.IsochronesErrorCodes;
import heigit.ors.isochrones.IsochronesIntersection;
import heigit.ors.common.TravelRangeType;
import heigit.ors.common.TravellerInfo;
import heigit.ors.isochrones.Isochrone;
import heigit.ors.isochrones.IsochroneMap;
import heigit.ors.isochrones.IsochroneMapCollection;
import heigit.ors.isochrones.IsochroneRequest;
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
		case "POST":  
			req = JsonIsochroneRequestParser.parseFromStream(_request.getInputStream());  
			break;
		default:
			throw new StatusCodeException(StatusCode.METHOD_NOT_ALLOWED, IsochronesErrorCodes.UNKNOWN);
		}

		if (req == null)
			throw new StatusCodeException(StatusCode.BAD_REQUEST, IsochronesErrorCodes.UNKNOWN, "IsochronesRequest object is null.");

		if (!req.isValid())
			throw new StatusCodeException(StatusCode.BAD_REQUEST, IsochronesErrorCodes.UNKNOWN, "IsochronesRequest is not valid.");

		List<TravellerInfo> travellers = req.getTravellers();
		 
		if (IsochronesServiceSettings.getAllowComputeArea() == false && req.hasAttribute("area"))
			throw new StatusCodeException(StatusCode.BAD_REQUEST, IsochronesErrorCodes.FEATURE_NOT_SUPPORTED, "Area computation is not enabled.");

		if (travellers.size() > IsochronesServiceSettings.getMaximumLocations())
			throw new ParameterOutOfRangeException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "locations", Integer.toString(travellers.size()), Integer.toString(IsochronesServiceSettings.getMaximumLocations()));

		for (int i = 0;i < travellers.size(); ++i){
			TravellerInfo traveller = travellers.get(i);
			int maxAllowedRange = IsochronesServiceSettings.getMaximumRange(traveller.getRouteSearchParameters().getProfileType(), traveller.getRangeType());
			double maxRange = traveller.getMaximumRange();
			if (maxRange > maxAllowedRange)
				throw new ParameterOutOfRangeException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "range", Double.toString(maxRange), Integer.toString(maxAllowedRange));

			if (IsochronesServiceSettings.getMaximumIntervals() > 0)
			{
				if (IsochronesServiceSettings.getMaximumIntervals() < traveller.getRanges().length)
					throw new ParameterOutOfRangeException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "range", Integer.toString(traveller.getRanges().length), Integer.toString(IsochronesServiceSettings.getMaximumIntervals()));
			}
		}

		if (travellers.size() > 0)
		{
			String[] nonDefaultAttrs = req.getNonDefaultAttributes();
			
			IsochroneMapCollection isoMaps = new IsochroneMapCollection();

			for (int i = 0;i < travellers.size(); ++i){
				IsochroneSearchParameters searchParams = req.getSearchParameters(i);
				IsochroneMap isochroneMap = RoutingProfileManager.getInstance().buildIsochrone(searchParams, nonDefaultAttrs);
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

		TravellerInfo traveller = null;
		int groupIndex = 0;
		boolean hasAttributes = request.getAttributes() != null;
		boolean includeArea = request.hasAttribute("area");
		boolean includeReachFactor = request.hasAttribute("reachfactor");
		String units = request.getUnits() != null ? request.getUnits().toLowerCase() : null;
		String sourceAttribution = IsochronesServiceSettings.getAttribution();
		List<String> attributeSources = null;

		for (IsochroneMap isoMap : isochroneMaps.getIsochroneMaps())
		{
			traveller = request.getTravellers().get(isoMap.getTravellerId());
			
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
					if (includeReachFactor && traveller.getRangeType() == TravelRangeType.Time)
					{
						double r  = isoLine.getMaxRadius(units);
						double maxArea = Math.PI * r * r;

						jProperties.put("reachfactor", FormatUtility.roundToDecimals(area/maxArea, 4));
					}
				}
				
				if (hasAttributes && isoLine.getAttributes() != null)
				{
					List<AttributeValue> attrStats = isoLine.getAttributes();
					for(AttributeValue attrValue : attrStats)
					{
						jProperties.put(attrValue.getName(), FormatUtility.roundToDecimals(attrValue.getValue(), 4));
						
						if (attrValue.getSource() != null)
						{
							if (attributeSources == null)
								attributeSources = new ArrayList<String>();
							if (!attributeSources.contains(attrValue.getSource()))
							{
								attributeSources.add(attrValue.getSource());
								sourceAttribution += " | " + attrValue.getSource();
							}
						}
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

		traveller = request.getTravellers().get(0);
		
		JSONObject jInfo = new JSONObject();
		jInfo.put("service", "isochrones");
		jInfo.put("engine", AppInfo.getEngineInfo());
		if (!Helper.isEmpty(sourceAttribution))
			jInfo.put("attribution", sourceAttribution);
		jInfo.put("timestamp", System.currentTimeMillis());

		if (AppConfig.hasValidMD5Hash())
			jInfo.put("osm_file_md5_hash", AppConfig.getMD5Hash());

		JSONObject jQuery = new JSONObject();

		jQuery.put("profile", RoutingProfileType.getName(traveller.getRouteSearchParameters().getProfileType()));

		if (traveller.getRangeType() != null)
			jQuery.put("range_type", traveller.getRangeType().toString().toLowerCase());

		jQuery.put("ranges", StringUtility.arrayToString(traveller.getRangesInUnit(request.getUnits()), ","));

		jQuery.put("locations", GeometryJSON.toJSON(request.getLocations(), false));

		if (request.getUnits() != null)
			jQuery.put("units", request.getUnits());

		if (traveller.getLocationType() != null)
			jQuery.put("location_type", traveller.getLocationType());

		if (request.getAttributes() != null)
			jQuery.put("attributes", StringUtility.combine(request.getAttributes(), "|"));

		if (request.getCalcMethod() != null)
			jQuery.put("calc_method", request.getCalcMethod());

		if (!Helper.isEmpty(traveller.getRouteSearchParameters().getOptions()))
			jQuery.put("options", new JSONObject(traveller.getRouteSearchParameters().getOptions()));

		if (request.getId() != null)
			jQuery.put("id", request.getId());

		jInfo.put("query", jQuery);

		jResp.put("info", jInfo);

		ServletUtility.write(response, jResp);
	}
}
