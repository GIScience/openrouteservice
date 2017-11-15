/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.services.routing.requestprocessors.json;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.graphhopper.util.Helper;
import com.graphhopper.util.shapes.BBox;
import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.common.DistanceUnit;
import heigit.ors.geojson.GeometryJSON;
import heigit.ors.routing.ExtraSummaryItem;
import heigit.ors.routing.RouteExtraInfo;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RouteSegment;
import heigit.ors.routing.RouteSegmentItem;
import heigit.ors.routing.RouteStep;
import heigit.ors.routing.RouteStepManeuver;
import heigit.ors.routing.RouteSummary;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.RoutingRequest;
import heigit.ors.routing.WeightingMethod;
import heigit.ors.services.routing.RoutingServiceSettings;
import heigit.ors.util.AppInfo;
import heigit.ors.util.DistanceUnitUtil;
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
		jInfo.put("engine", AppInfo.getEngineInfo());
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
			jQuery.put("units", DistanceUnitUtil.toString(request.getUnits()));

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


	public static JSONObject toGeoJson(RoutingRequest request, RouteResult[] routeResult) throws Exception
	{
		// TODO
		return null;
	}
    public static String toGPX(RoutingRequest rreq, RouteResult[] routeResults) {
	    // TODO Do the magic here
    return null;
	}
	public static JSONArray toJsonArray(RoutingRequest request, RouteResult[] routeResult, BBox bbox) throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		// *************** routes ***************

		boolean attrDetourFactor = request.hasAttribute("detourfactor");
		boolean attrPercentage = request.hasAttribute("percentage");
		boolean attrAvgSpeed = request.hasAttribute("avgspeed");

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

			if (attrAvgSpeed)
				jSummary.put("avgspeed", rSummary.getAverageSpeed());

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

					for (int j = 0; j < nSegments; ++j)
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
						if (attrAvgSpeed)
						{
							double distFactor = request.getUnits() == DistanceUnit.Meters ? 1000 : 1;
							jSegment.put("avgspeed", FormatUtility.roundToDecimals(seg.getDistance() / distFactor / (seg.getDuration() / 3600) , 2));
						}

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

							if (request.getIncludeManeuvers())
							{
								RouteStepManeuver maneuver = step.getManeuver();
								if (maneuver != null)
								{
									JSONObject jManeuver = new JSONObject(true);
									jManeuver.put("bearing_before", maneuver.getBearingBefore());
									jManeuver.put("bearing_after", maneuver.getBearingAfter());
									if (maneuver.getLocation() != null)
										jManeuver.put("location", GeometryJSON.toJSON(maneuver.getLocation()));

									jStep.put("maneuver", jManeuver);
								}
							}

							if (request.getIncludeRoundaboutExits() && step.getRoundaboutExitBearings() != null)
							{
								jStep.put("exit_bearings", new JSONArray(step.getRoundaboutExitBearings()));
							}

							// add mode: driving, cycling, etc.

							jStep.put("way_points", new JSONArray(step.getWayPoints()));

							jSteps.put(jStep);
						}

						jSegment.put("steps", jSteps);
						jSegments.put(jSegment);
					}

					jRoute.put("segments", jSegments);
				}

				//if (route.getLocationIndex() >= 0)
				//	jRoute.put("location_index", route.getLocationIndex());

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

								if (extraInfo.getFactor() == 1.0)
									jExtraItemValue.put(segExtra.getValue());
								else
									jExtraItemValue.put(FormatUtility.roundToDecimals(segExtra.getValue()/extraInfo.getFactor(), 1));

								jExtraItemValues.put(jExtraItemValue);
							}

							jExtraItem.put("values", jExtraItemValues);

							// ---------- summary ----------

							List<ExtraSummaryItem> summaryItems = extraInfo.getSummary(request.getUnits(), rSummary.getDistance(), true);

							if (summaryItems.size() > 0)
							{
								JSONArray jExtraItemSummary = new JSONArray(summaryItems.size());

								for (ExtraSummaryItem esi : summaryItems)
								{
									JSONObject jExtraItemSummaryType = new JSONObject(true);

									jExtraItemSummaryType.put("value", esi.getValue());
									jExtraItemSummaryType.put("distance", esi.getDistance());
									jExtraItemSummaryType.put("amount", esi.getAmount());

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
