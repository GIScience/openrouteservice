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
package org.heigit.ors.services.isochrones.requestprocessors.json;

import com.graphhopper.util.Helper;
import com.graphhopper.util.shapes.BBox;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import org.heigit.ors.api.util.SystemMessage;
import org.heigit.ors.common.AttributeValue;
import org.heigit.ors.common.Pair;
import org.heigit.ors.common.StatusCode;
import org.heigit.ors.common.TravellerInfo;
import org.heigit.ors.config.AppConfig;
import org.heigit.ors.exceptions.ParameterOutOfRangeException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.geojson.GeometryJSON;
import org.heigit.ors.isochrones.*;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.services.isochrones.IsochronesServiceSettings;
import org.heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import org.heigit.ors.servlet.util.ServletUtility;
import org.heigit.ors.util.AppInfo;
import org.heigit.ors.util.FormatUtility;
import org.heigit.ors.util.StringUtility;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

public class JsonIsochronesRequestProcessor extends AbstractHttpRequestProcessor {
    public JsonIsochronesRequestProcessor(HttpServletRequest request) throws Exception {
        super(request);
    }

    @Override
    public void process(HttpServletResponse response) throws Exception {
        String reqMethod = request.getMethod();

        IsochroneRequest req;
        switch (reqMethod) {
            case "GET":
                req = JsonIsochroneRequestParser.parseFromRequestParams(request);
                break;
            case "POST":
                req = JsonIsochroneRequestParser.parseFromStream(request.getInputStream());
                break;
            default:
                throw new StatusCodeException(StatusCode.METHOD_NOT_ALLOWED, IsochronesErrorCodes.UNKNOWN);
        }

        if (!req.isValid())
            throw new StatusCodeException(StatusCode.BAD_REQUEST, IsochronesErrorCodes.UNKNOWN, "IsochronesRequest is not valid.");

        List<TravellerInfo> travellers = req.getTravellers();

        if (!IsochronesServiceSettings.getAllowComputeArea() && req.hasAttribute("area"))
            throw new StatusCodeException(StatusCode.BAD_REQUEST, IsochronesErrorCodes.FEATURE_NOT_SUPPORTED, "Area computation is not enabled.");

        if (travellers.size() > IsochronesServiceSettings.getMaximumLocations())
            throw new ParameterOutOfRangeException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "locations", Integer.toString(travellers.size()), Integer.toString(IsochronesServiceSettings.getMaximumLocations()));

        for (int i = 0; i < travellers.size(); ++i) {
            TravellerInfo traveller = travellers.get(i);
            int maxAllowedRange = IsochronesServiceSettings.getMaximumRange(traveller.getRouteSearchParameters().getProfileType(), req.getCalcMethod(), traveller.getRangeType());
            double maxRange = traveller.getMaximumRange();
            if (maxRange > maxAllowedRange)
                throw new ParameterOutOfRangeException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "range", Double.toString(maxRange), Integer.toString(maxAllowedRange));

            if (IsochronesServiceSettings.getMaximumIntervals() > 0 && IsochronesServiceSettings.getMaximumIntervals() < traveller.getRanges().length) {
                throw new ParameterOutOfRangeException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "range", Integer.toString(traveller.getRanges().length), Integer.toString(IsochronesServiceSettings.getMaximumIntervals()));
            }
        }

        if (!travellers.isEmpty()) {
            IsochroneMapCollection isoMaps = new IsochroneMapCollection();

            for (int i = 0; i < travellers.size(); ++i) {
                IsochroneSearchParameters searchParams = req.getSearchParameters(i);
                IsochroneMap isochroneMap = RoutingProfileManager.getInstance().buildIsochrone(searchParams);
                isoMaps.add(isochroneMap);
            }
            writeResponse(response, req, isoMaps);
        }
    }

    private void writeResponse(HttpServletResponse response, IsochroneRequest request, IsochroneMapCollection isochroneMaps) throws Exception {
        JSONObject jResp = new JSONObject(true);

        jResp.put("type", "FeatureCollection");

        JSONArray jFeatures = new JSONArray(isochroneMaps.getIsochronesCount());
        jResp.put("features", jFeatures);

        BBox bbox = new BBox(0, 0, 0, 0);


        int groupIndex = 0;
        boolean hasAttributes = request.getAttributes() != null;
        boolean includeArea = request.hasAttribute("area");
        String units = request.getUnits() != null ? request.getUnits().toLowerCase() : null;
        String areaUnits = request.getAreaUnits() != null ? request.getAreaUnits().toLowerCase() : null;
        StringBuilder sourceAttribution = new StringBuilder(IsochronesServiceSettings.getAttribution());
        List<String> attributeSources = null;

        for (IsochroneMap isoMap : isochroneMaps.getIsochroneMaps()) {

            for (Isochrone isoLine : isoMap.getIsochrones()) {
                Polygon isoPoly = (Polygon) isoLine.getGeometry();
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

                // using units for distance mode determines the reach in m/km/mi
                // using units for time mode determines the area calculation unit m/km/mi
                // this is misleading which is why we are introducing area_units
                // to calculate the area of an isochrone in m/km/mi
                if (areaUnits != null) units = areaUnits;

                if (isoLine.hasArea()) jProperties.put("area", FormatUtility.roundToDecimals(isoLine.getArea(), 4));
                if (isoLine.hasReachfactor()) jProperties.put("reachfactor", isoLine.getReachfactor());

                if (hasAttributes && isoLine.getAttributes() != null) {
                    List<AttributeValue> attrStats = isoLine.getAttributes();
                    for (AttributeValue attrValue : attrStats) {
                        jProperties.put(attrValue.getName(), FormatUtility.roundToDecimals(attrValue.getValue(), 4));

                        if (attrValue.getSource() != null) {
                            if (attributeSources == null)
                                attributeSources = new ArrayList<>();
                            if (!attributeSources.contains(attrValue.getSource())) {
                                attributeSources.add(attrValue.getSource());
                                sourceAttribution.append(" | " + attrValue.getSource());
                            }
                        }
                    }
                }

                jFeature.put("properties", jProperties);

                jFeatures.put(jFeature);

                Envelope env = shell.getEnvelopeInternal();
                bbox = constructIsochroneBBox(env);

            }

            groupIndex++;
        }

        if (request.getIncludeIntersections()) {
            List<IsochronesIntersection> isoIntersections = IsochroneUtility.computeIntersections(isochroneMaps);
            if (isoIntersections != null && !isoIntersections.isEmpty()) {
                for (IsochronesIntersection isoIntersection : isoIntersections) {
                    Geometry geom = isoIntersection.getGeometry();
                    JSONObject jFeature = new JSONObject(true);
                    jFeature.put("type", "Feature");

                    JSONObject jGeometry = new JSONObject(true);
                    jGeometry.put("type", geom.getGeometryType());
                    jGeometry.put("coordinates", GeometryJSON.toJSON(geom));

                    jFeature.put("geometry", jGeometry);

                    JSONObject jProperties = new JSONObject(true);

                    JSONArray jContours = new JSONArray(isoIntersection.getContourRefs().size());
                    jProperties.put("contours", jContours);

                    for (Pair<Integer, Integer> ref : isoIntersection.getContourRefs()) {
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

        jResp.put("bbox", GeometryJSON.toJSON(bbox.minLon, bbox.minLat, bbox.maxLon, bbox.maxLat));

        TravellerInfo traveller = request.getTravellers().get(0);

        JSONObject jInfo = new JSONObject();
        jInfo.put("service", "isochrones");
        jInfo.put("engine", AppInfo.getEngineInfo());
        if (!Helper.isEmpty(sourceAttribution.toString()))
            jInfo.put("attribution", sourceAttribution.toString());
        jInfo.put("timestamp", System.currentTimeMillis());

        jInfo.put("system_message", SystemMessage.getSystemMessage(request));

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

        if (request.getAreaUnits() != null)
            jQuery.put("area_units", request.getAreaUnits());

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

    public static BBox constructIsochroneBBox(Envelope env){
        BBox bbox = new BBox(0,0,0,0);
        if (Double.isFinite(env.getMinX()))
            bbox.minLon = env.getMinX();
        if (Double.isFinite(env.getMinY()))
            bbox.minLat = env.getMinY();
        if (Double.isFinite(env.getMaxX()))
            bbox.maxLon = env.getMaxX();
        if (Double.isFinite(env.getMaxY()))
            bbox.maxLat = env.getMaxY();
        if (!bbox.isValid())
            bbox = new BBox(0, 0, 0, 0);
        return bbox;
    }
}
