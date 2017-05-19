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
package heigit.ors.services.routing.requestprocessors.json;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.graphhopper.util.Helper;
import com.graphhopper.util.shapes.BBox;
import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.geojson.GeometryJSON;
import heigit.ors.routing.RouteExtraInfo;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RouteSegment;
import heigit.ors.routing.RouteSegmentItem;
import heigit.ors.routing.RouteStep;
import heigit.ors.routing.RouteSummary;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.WeightingMethod;
import heigit.ors.services.routing.RoutingRequest;
import heigit.ors.services.routing.RoutingServiceSettings;
import heigit.ors.util.AppInfo;
import heigit.ors.util.FormatUtility;
import heigit.ors.util.PolylineEncoder;

public class JsonRoutingResponseWriter {

	public static JSONObject toJson(RoutingRequest request, RouteResult[] routeResult) throws Exception
	{
		JSONObject jResp = new JSONObject(true, 1);
		BBox bbox = new BBox(0, 0, 0, 0);
		JSONArray jRoutes = toJsonArray(request, routeResult, bbox);
		jResp.put("routes", jRoutes);


		// *************** bbox ***************

		if (bbox != null)
			jResp.put("bbox", GeometryJSON.toJSON(bbox.minLon, bbox.minLat, bbox.maxLon, bbox.maxLat));

		// *************** info ***************

		JSONObject jInfo = new JSONObject(3);
		jInfo.put("service", "routing");
		jInfo.put("version", AppInfo.VERSION);
		if (!Helper.isEmpty(RoutingServiceSettings.getAttribution()))
			jInfo.put("attribution", RoutingServiceSettings.getAttribution());
		jInfo.put("timestamp", System.currentTimeMillis());

		JSONObject jQuery = new JSONObject(true);

		jQuery.put("profile", RoutingProfileType.getName(request.getSearchParameters().getProfileType()));

		jQuery.put("preference", WeightingMethod.getName(request.getSearchParameters().getWeightingMethod()));

		jQuery.put("coordinates", GeometryJSON.toJSON(request.getCoordinates(), request.getIncludeElevation()));

		if (request.getLanguage() != null)
			jQuery.put("language", request.getLanguage());

		if (request.getUnits() != null)
			jQuery.put("units", request.getUnits().toString().toLowerCase());

		jQuery.put("geometry", request.getIncludeGeometry());
		if (request.getIncludeGeometry())
		{
			jQuery.put("geometry_format", Helper.isEmpty(request.getGeometryFormat()) ? "encodedpolyline" : request.getGeometryFormat());
			jQuery.put("geometry_simplify", request.getSimplifyGeometry());

			if (request.getIncludeInstructions())
				jQuery.put("instructions_format", request.getInstructionsFormat().toString().toLowerCase());

			jQuery.put("instructions", request.getIncludeInstructions());
			jQuery.put("elevation", request.getIncludeElevation());
		}

		if (!Helper.isEmpty(request.getSearchParameters().getOptions()))
			jQuery.put("options", new JSONObject(request.getSearchParameters().getOptions()));

		if (!Helper.isEmpty(request.getId()))
			jQuery.put("id", request.getId());

		jInfo.put("query", jQuery);

		jResp.put("info", jInfo);

		return jResp;
	}

	public static JSONArray toJsonArray(RoutingRequest request, RouteResult[] routeResult, BBox bbox) throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		// *************** routes ***************

		boolean attrDetourFactor = request.hasAttribute("detourfactor");
		boolean attrPercentage = request.hasAttribute("percentage");

		int nRoutes = routeResult.length;

		JSONArray jRoutes = new JSONArray(nRoutes);

		for (int i = 0; i < nRoutes; ++i)
		{
			RouteResult route = routeResult[i];
			JSONObject jRoute = new JSONObject(true);

			if (request.getIncludeElevation())
				jRoute.put("elevation", true);

			JSONObject jSummary = new JSONObject(true, 6);

			RouteSummary rSummary = route.getSummary();
			jSummary.put("distance", rSummary.getDistance());
			jSummary.put("duration", rSummary.getDuration());

			if (rSummary.getDistanceActual() != 0.0 && Math.abs(rSummary.getDistance() - rSummary.getDistanceActual()) > 1.0)
				jSummary.put("distance_actual", rSummary.getDistanceActual());

			if (rSummary.getAscent() != 0.0 || rSummary.getDescent() != 0.0)
			{
				jSummary.put("ascent", rSummary.getAscent());
				jSummary.put("descent", rSummary.getDescent());
			}

			jRoute.put("summary", jSummary);

			if (request.getIncludeGeometry())
			{
				if (request.getGeometryFormat() != null)
					jRoute.put("geometry_format", request.getGeometryFormat());

				jRoute.put("geometry", getGeometry(route.getGeometry(), request.getIncludeElevation(), request.getGeometryFormat(), buffer));

				if (request.getIncludeInstructions() && route.getSegments().size() > 0)
				{
					int nSegments = route.getSegments().size();
					JSONArray jSegments = new JSONArray(nSegments);
					for (int j = 0; j < route.getSegments().size(); ++j)
					{
						JSONObject jSegment = new JSONObject(true);

						RouteSegment seg = route.getSegments().get(j);

						jSegment.put("distance", seg.getDistance());
						jSegment.put("duration", seg.getDuration());

						if (request.getIncludeElevation() && (seg.getAscent() !=0.0 || seg.getDescent() != 0.0))
						{
							jSegment.put("ascent", seg.getAscent());
							jSegment.put("descent", seg.getDescent());
						}

						if (attrDetourFactor)
							jSegment.put("detour_factor", seg.getDetourFactor());
						if (attrPercentage)
							jSegment.put("percentage", FormatUtility.roundToDecimals(seg.getDistance() * 100 / route.getSummary().getDistance(), 2));

						int nSteps = seg.getSteps().size();
						JSONArray jSteps = new JSONArray(nSteps);

						for (int k = 0; k < seg.getSteps().size(); ++k)
						{
							RouteStep step = seg.getSteps().get(k);

							JSONObject jStep = new JSONObject(true);
							jStep.put("distance", step.getDistance());
							jStep.put("duration", step.getDuration());
							jStep.put("type", step.getType());
							jStep.put("instruction", step.getInstruction());
							if (step.getName() != null)
								jStep.put("name", step.getName());
							if (step.getMessage() != null)
							{
								jStep.put("message", step.getMessage());
								jStep.put("message_type", step.getMessageType());
							}

							if (step.getExitNumber() != -1)
								jStep.put("exit_number", step.getExitNumber());

							// add mode: driving, cycling, etc.

							jStep.put("way_points", new JSONArray(step.getWayPoints()));

							jSteps.put(jStep);
						}

						jSegment.put("steps", jSteps);
						jSegments.put(jSegment);
					}

					jRoute.put("segments", jSegments);
				}

				if (route.getWayPointsIndices() != null)
					jRoute.put("way_points", new JSONArray(route.getWayPointsIndices()));

				List<RouteExtraInfo> extras = route.getExtraInfo();

				if (extras != null && extras.size() > 0)
				{
					JSONObject jExtras = new JSONObject(true);

					for (int j = 0; j < extras.size(); ++j)
					{
						RouteExtraInfo extraInfo = extras.get(j);

						if (!extraInfo.isEmpty())
						{
							JSONObject jExtraItem = new JSONObject(true);

							// ---------- values ---------- 
							int nExtraValues = extraInfo.getSegments().size();
							JSONArray jExtraItemValues = new JSONArray(nExtraValues);

							for (int k = 0; k < nExtraValues; ++k)
							{
								RouteSegmentItem segExtra = extraInfo.getSegments().get(k); 

								JSONArray jExtraItemValue = new JSONArray(3);
								jExtraItemValue.put(segExtra.getFrom());
								jExtraItemValue.put(segExtra.getTo());
								jExtraItemValue.put(segExtra.getValue());

								jExtraItemValues.put(jExtraItemValue);
							}

							jExtraItem.put("values", jExtraItemValues);

							// ---------- summary ---------- 

							Map<String, Map<String, Object>> summary = extraInfo.getSummary(request.getUnits());

							if (summary.size() > 0)
							{
								JSONArray jExtraItemSummary = new JSONArray(summary.size());

								for (Map.Entry<String, Map<String, Object>> kv : summary.entrySet())
								{
									JSONObject jExtraItemSummaryType = new JSONObject(true);
									for (Map.Entry<String, Object> kv2 : kv.getValue().entrySet())
									{
										jExtraItemSummaryType.put(kv2.getKey(), kv2.getValue());
									}

									jExtraItemSummary.put(jExtraItemSummaryType);
								}

								jExtraItem.put("summary", jExtraItemSummary);
							}

							jExtras.put(extraInfo.getName(), jExtraItem);
						}
					}

					jRoute.put("extras", jExtras);
				}
			}

			// *************** bbox ***************
			BBox bboxRoute = rSummary.getBBox();
			if (bboxRoute != null)
			{
				jRoute.put("bbox", GeometryJSON.toJSON(bboxRoute.minLon, bboxRoute.minLat, bboxRoute.maxLon, bboxRoute.maxLat));
				if (!bbox.isValid())
				{
					bbox.minLat = bboxRoute.minLat;
					bbox.maxLat = bboxRoute.maxLat;
					bbox.minLon = bboxRoute.minLon;
					bbox.maxLon = bboxRoute.maxLon;
				}
				else
				{
					bbox.update(bboxRoute.minLat, bboxRoute.minLon);
					bbox.update(bboxRoute.maxLat, bboxRoute.maxLon);
				}
			}

			jRoutes.put(jRoute);
		}

		return jRoutes;
	}  

	private static Object getGeometry(Coordinate[] points, boolean includeElevation, String format, StringBuffer buffer)
	{
		if (points == null)
			return "";

		if (Helper.isEmpty(format) || "encodedpolyline".equalsIgnoreCase(format))
		{
			return PolylineEncoder.encode(points, includeElevation, buffer);
		}
		else if ("geojson".equalsIgnoreCase(format))
		{
			JSONObject json = new JSONObject(true);

			/*
			 *{
			    "type": "LineString",
                "coordinates": [ [102.0, 0.0], [103.0, 1.0], [104.0, 0.0], [105.0, 1.0] ]
             }
			 */
			json.put("type", "LineString");
			json.put("coordinates", GeometryJSON.toJSON(points, includeElevation));

			return json;
		}
		else if ("polyline".equalsIgnoreCase(format))
		{
			return GeometryJSON.toJSON(points, includeElevation);
		}

		return "";
	}
}
