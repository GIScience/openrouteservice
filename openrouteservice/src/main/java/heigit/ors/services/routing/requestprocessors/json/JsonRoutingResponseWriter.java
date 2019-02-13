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
package heigit.ors.services.routing.requestprocessors.json;


import java.util.List;

import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.routing.*;
import org.json.JSONArray;
import org.json.JSONObject;

import com.graphhopper.util.Helper;
import com.graphhopper.util.shapes.BBox;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.common.DistanceUnit;
import heigit.ors.config.AppConfig;
import heigit.ors.geojson.GeometryJSON;
import heigit.ors.services.routing.RoutingServiceSettings;
import heigit.ors.util.AppInfo;
import heigit.ors.util.DistanceUnitUtil;
import heigit.ors.util.FormatUtility;
import heigit.ors.util.PolylineEncoder;

@Deprecated
public class JsonRoutingResponseWriter {

	public static JSONObject toJson(RoutingRequest request, RouteResult[] routeResult) throws StatusCodeException {
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

		if (AppConfig.hasValidMD5Hash())
			jInfo.put("osm_file_md5_hash", AppConfig.getMD5Hash());

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

	public static JSONArray toJsonArray(RoutingRequest request, RouteResult[] routeResult, BBox bbox) throws StatusCodeException {
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

			if(route.getWarnings().size() != 0) {
				JSONArray jWarnings = new JSONArray();
				for(RouteWarning warning : route.getWarnings()) {
					JSONObject jWarning = new JSONObject();
					jWarning.put("code", warning.getWarningCode());
					jWarning.put("message", warning.getWarningMessage());
					jWarnings.put(jWarning);
				}

				jRoute.put("warnings", jWarnings);
			}

			if (request.getIncludeElevation())
				jRoute.put("elevation", true);

			JSONObject jSummary = new JSONObject(true, 6);

			RouteSummary rSummary = route.getSummary();
			jSummary.put("distance", rSummary.getDistance());
			jSummary.put("duration", rSummary.getDuration());

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

						if (request.getIncludeElevation() && (seg.getAscentRounded() !=0.0 || seg.getDescentRounded() != 0.0))
						{
							jSegment.put("ascent", seg.getAscentRounded());
							jSegment.put("descent", seg.getDescentRounded());
						}

						if (attrDetourFactor)
							jSegment.put("detourfactor", seg.getDetourFactor());
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
