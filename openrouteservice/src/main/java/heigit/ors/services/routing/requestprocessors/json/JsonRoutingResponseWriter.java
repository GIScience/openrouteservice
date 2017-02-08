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

import java.util.LinkedHashMap;
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

	public static String toString(RoutingRequest request, RouteResult[] routeResult) throws Exception
	{
		JSONObject jResp = createJsonObject();

		StringBuffer buffer = new StringBuffer();
		// *************** routes ***************

		JSONArray jRoutes = new JSONArray();

		jResp.put("routes", jRoutes);

		int nRoutes = routeResult.length;
		BBox bboxResp = null;

		for (int i = 0; i < nRoutes; ++i)
		{
			RouteResult route = routeResult[i];
			JSONObject jRoute = createJsonObject();

			if (request.getIncludeElevation())
				jRoute.put("elevation", true);

			JSONObject jSummary = createJsonObject();

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

				jRoute.put("geometry", getGeometry(route.getGeometry(), request.getGeometryFormat(), buffer));

				if (request.getIncludeElevation())
					jRoute.put("elevations", getElevations(route.getGeometry()));

				if (request.getIncludeInstructions() && route.getSegments().size() > 0)
				{
					JSONArray jSegments = new JSONArray();
					for (int j = 0; j < route.getSegments().size(); ++j)
					{
						JSONObject jSegment = createJsonObject();

						RouteSegment seg = route.getSegments().get(j);

						jSegment.put("distance", seg.getDistance());
						jSegment.put("duration", seg.getDuration());
						
						if (request.getIncludeElevation() && (seg.getAscent() !=0.0 || seg.getDescent() != 0.0))
						{
							jSegment.put("ascent", seg.getAscent());
							jSegment.put("descent", seg.getDescent());
						}

						if (seg.getDetourFactor() != 0.0)
							jSegment.put("detour_factor", seg.getDetourFactor());

						JSONArray jSteps = new JSONArray();

						for (int k = 0; k < seg.getSteps().size(); ++k)
						{
							RouteStep step = seg.getSteps().get(k);

							JSONObject jStep = createJsonObject();
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
					JSONObject jExtras = createJsonObject();

					for (int j = 0; j < extras.size(); ++j)
					{
						RouteExtraInfo extraInfo = extras.get(j);

						if (!extraInfo.isEmpty())
						{
							JSONObject jExtraItem = createJsonObject();

							// ---------- values ---------- 
							JSONArray jExtraItemValues = new JSONArray();

							for (int k = 0; k < extraInfo.getSegments().size(); ++k)
							{
								RouteSegmentItem segExtra = extraInfo.getSegments().get(k); 

								JSONArray jExtraItemValue = new JSONArray();
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
								JSONArray jExtraItemSummary = new JSONArray();
								
								for (Map.Entry<String, Map<String, Object>> kv : summary.entrySet())
								{
									JSONObject jExtraItemSummaryType = createJsonObject();
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

				if (bboxResp == null)
					bboxResp = bboxRoute.clone();
				else
				{
					bboxResp.update(bboxRoute.minLat, bboxRoute.minLon);
					bboxResp.update(bboxRoute.maxLat, bboxRoute.maxLon);
				}
			}

			jRoutes.put(jRoute);
		}


		// *************** bbox ***************

		if (bboxResp != null)
			jResp.put("bbox", GeometryJSON.toJSON(bboxResp.minLon, bboxResp.minLat, bboxResp.maxLon, bboxResp.maxLat));

		// *************** info ***************

		JSONObject jInfo = new JSONObject();
		jInfo.put("service", "routing");
		jInfo.put("version", AppInfo.VERSION);
		if (!Helper.isEmpty(RoutingServiceSettings.getAttribution()))
			jInfo.put("attribution", RoutingServiceSettings.getAttribution());
		jInfo.put("timestamp", System.currentTimeMillis());

		JSONObject jQuery = createJsonObject();

		jQuery.put("profile", RoutingProfileType.getName(request.getSearchParameters().getProfileType()));

		jQuery.put("preference", WeightingMethod.getName(request.getSearchParameters().getWeightingMethod()));

		jQuery.put("coordinates", GeometryJSON.toJSON(request.getCoordinates()));

		if (request.getLanguage() != null)
			jQuery.put("language", request.getLanguage());

		if (request.getUnits() != null)
			jQuery.put("units", request.getUnits().toString().toLowerCase());

		jQuery.put("geometry", request.getIncludeGeometry());
		if (request.getIncludeGeometry())
		{
			jQuery.put("geometry_format", Helper.isEmpty(request.getGeometryFormat()) ? "encodedpolyline" : request.getGeometryFormat());

			if (request.getIncludeInstructions() && request.getPrettifyInstructions() != null)
				jQuery.put("prettify_instructions", request.getPrettifyInstructions());

			jQuery.put("instructions", request.getIncludeInstructions());
			jQuery.put("elevation", request.getIncludeElevation());
		}

		if (!Helper.isEmpty(request.getSearchParameters().getOptions()))
			jQuery.put("options", new JSONObject(request.getSearchParameters().getOptions()));

		jInfo.put("query", jQuery);

		jResp.put("info", jInfo);

		return jResp.toString();
	}  

	private static JSONObject createJsonObject()
	{
		Map<String,String > map =  new LinkedHashMap<String, String>();
		return new JSONObject(map);
	}

	private static JSONArray getElevations(Coordinate[] points)
	{
		JSONArray elevations = new JSONArray();
		for (int i = 0; i < points.length; ++i)
			elevations.put(i, FormatUtility.roundToDecimals(points[i].z, 1));

		return elevations;
	}

	private static Object getGeometry(Coordinate[] points, String format, StringBuffer buffer)
	{
		if (points == null)
			return "";

		if (Helper.isEmpty(format) || "encodedpolyline".equalsIgnoreCase(format))
		{
			return PolylineEncoder.encode(points, buffer);
		}
		else if ("geojson".equalsIgnoreCase(format))
		{
			JSONObject json = createJsonObject();

			/*
			 *{
			    "type": "LineString",
                "coordinates": [ [102.0, 0.0], [103.0, 1.0], [104.0, 0.0], [105.0, 1.0] ]
             }
			 */
			json.put("type", "LineString");
			json.put("coordinates", GeometryJSON.toJSON(points));

			return json;
		}
		else if ("polyline".equalsIgnoreCase(format))
		{
			return GeometryJSON.toJSON(points);
		}

		return "";
	}
}
