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
/*

package heigit.ors.services.accessibility.requestprocessors.json;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.graphhopper.util.Helper;
import com.graphhopper.util.shapes.BBox;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import heigit.ors.accessibility.AccessibilityAnalyzer;
import heigit.ors.accessibility.AccessibilityErrorCodes;
import heigit.ors.accessibility.AccessibilityRequest;
import heigit.ors.accessibility.AccessibilityResult;
import heigit.ors.common.StatusCode;
import heigit.ors.common.TravellerInfo;
import heigit.ors.config.AppConfig;
import heigit.ors.exceptions.ParameterOutOfRangeException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.geojson.GeometryJSON;
import heigit.ors.services.accessibility.AccessibilityServiceSettings;
import heigit.ors.services.locations.LocationsServiceSettings;
import heigit.ors.services.routing.requestprocessors.json.JsonRoutingResponseWriter;
import heigit.ors.locations.LocationsResult;
import heigit.ors.locations.LocationsSearchFilter;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RoutingRequest;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.servlet.util.ServletUtility;
import heigit.ors.util.AppInfo;
import heigit.ors.util.GeomUtility;


public class JsonAccessibilityRequestProcessor extends AbstractHttpRequestProcessor {
    public JsonAccessibilityRequestProcessor(HttpServletRequest request) throws Exception {
        super(request);
    }

    @Override
    public void process(HttpServletResponse response) throws Exception {
        AccessibilityRequest req = null;

        switch (_request.getMethod()) {
            case "GET":
                throw new StatusCodeException(StatusCode.METHOD_NOT_ALLOWED);
                //req = JsonAccessibilityRequestParser.parseFromRequestParams(_request);
            default:
                req = JsonAccessibilityRequestParser.parseFromStream(_request.getInputStream());
                break;
        }

        if (req == null)
            throw new StatusCodeException(StatusCode.BAD_REQUEST, "AccessibilityRequest object is null.");

        List<TravellerInfo> travellers = req.getTravellers();

        if (travellers.size() > AccessibilityServiceSettings.getMaximumRoutes())
            throw new ParameterOutOfRangeException(AccessibilityErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "locations", Integer.toString(travellers.size()), Integer.toString(AccessibilityServiceSettings.getMaximumRoutes()));

        for (int i = 0; i < travellers.size(); ++i) {
            TravellerInfo traveller = travellers.get(i);
            int maxAllowedRange = AccessibilityServiceSettings.getMaximumRange(traveller.getRouteSearchParameters().getProfileType(), traveller.getRangeType());
            double maxRange = traveller.getMaximumRange();
            if (maxRange > maxAllowedRange)
                throw new ParameterOutOfRangeException(AccessibilityErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "range", Integer.toString(maxAllowedRange), Double.toString(maxRange));
        }


        AccessibilityResult accesibilityResult = AccessibilityAnalyzer.computeAccessibility(req);

        writeAccessibilityResponse(response, req, accesibilityResult);
    }

    private void writeAccessibilityResponse(HttpServletResponse response, AccessibilityRequest request, AccessibilityResult result) throws Exception {
        JSONObject jResp = new JSONObject(4);

        int nResults = result.getLocations().size();
        boolean geoJsonFormat = AccessibilityServiceSettings.getRouteDetailsAllowed() && request.getRoutesFormat() != null && "geojson".equalsIgnoreCase(request.getRoutesFormat());

        if (nResults > 0) {
            RoutingRequest reqRouting = new RoutingRequest();
            reqRouting.setIncludeElevation(request.getIncludeElevation());
            reqRouting.setIncludeGeometry(request.getIncludeGeometry());
            reqRouting.setGeometryFormat(request.getGeometryFormat());

            StringBuffer buffer = new StringBuffer();

            List<LocationsResult> locations = result.getLocations();
            List<RouteResult> routes = result.getRoutes();

            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE;
            double maxY = Double.MIN_VALUE;

            // **************** Locations **********************

            JSONObject jLocations = new JSONObject(true, nResults);
            jResp.put("places", jLocations);

            JSONArray jLocationFeatures = new JSONArray(nResults);
            jLocations.put("type", "FeatureCollection");
            jLocations.put("features", jLocationFeatures);

            for (int j = 0; j < nResults; j++) {
                LocationsResult lr = locations.get(j);

                // skip locations that don't have routes
                if (routes.get(j) == null)
                    continue;

                Geometry geom = lr.getGeometry();

                JSONObject feature = new JSONObject(true, 3);
                feature.put("type", "Feature");

                JSONObject point = new JSONObject(true);
                point.put("type", geom.getClass().getSimpleName());
                point.put("coordinates", GeometryJSON.toJSON(geom, buffer));

                feature.put("geometry", point);

                Map<String, Object> props = lr.getProperties();

                JSONObject properties = new JSONObject(true, props.size());

                if (props.size() > 0) {
                    for (Map.Entry<String, Object> entry : props.entrySet())
                        properties.put(entry.getKey(), entry.getValue());
                }

                feature.put("properties", properties);
                jLocationFeatures.put(feature);
            }


            // **************** Routes **********************
            JSONArray jRoutes = new JSONArray();
            jResp.put("routes", jRoutes);

            if (geoJsonFormat) {
                Map<Integer, JSONObject> routesByLocationIndex = new LinkedHashMap<Integer, JSONObject>();

                for (int i = 0; i < result.getRoutes().size(); ++i) {
                    RouteResult route = result.getRoutes().get(i);
                    if (route != null) {
                        JSONObject feature = new JSONObject(true, 3);
                        feature.put("type", "Feature");

                        Geometry geom = GeomUtility.createLinestring(route.getGeometry());

                        JSONObject jGeometry = new JSONObject(true);
                        jGeometry.put("type", geom.getClass().getSimpleName());
                        jGeometry.put("coordinates", GeometryJSON.toJSON(geom, buffer));
                        feature.put("geometry", jGeometry);

                        JSONObject properties = new JSONObject(true, 3);
                        properties.put("duration", route.getSummary().getDuration());
                        properties.put("distance", route.getSummary().getDistance());
                        feature.put("properties", properties);

                        JSONArray jRouteFeatures = null;
                        JSONObject jRouteForLocIndex = routesByLocationIndex.get(route.getLocationIndex());
                        if (jRouteForLocIndex == null) {
                            jRouteForLocIndex = new JSONObject(true);
                            jRouteForLocIndex.put("type", "FeatureCollection");

                            jRouteFeatures = new JSONArray();
                            jRouteForLocIndex.put("features", jRouteFeatures);

                            routesByLocationIndex.put(route.getLocationIndex(), jRouteForLocIndex);
                        } else {
                            jRouteFeatures = jRouteForLocIndex.getJSONArray("features");
                        }

                        jRouteFeatures.put(feature);

                        Envelope env = geom.getEnvelopeInternal();

                        if (minX > env.getMinX())
                            minX = env.getMinX();
                        if (minY > env.getMinY())
                            minY = env.getMinY();
                        if (maxX < env.getMaxX())
                            maxX = env.getMaxX();
                        if (maxY < env.getMaxY())
                            maxY = env.getMaxY();
                    }
                }

                for (Map.Entry<Integer, JSONObject> entry : routesByLocationIndex.entrySet())
                    jRoutes.put(entry.getValue());
            } else {
                Map<Integer, JSONArray> routesByLocationIndex = new LinkedHashMap<Integer, JSONArray>();

                for (int i = 0; i < nResults; ++i) {
                    RouteResult route = result.getRoutes().get(i);
                    if (route != null) {
                        JSONArray jRouteForLocIndex = routesByLocationIndex.get(route.getLocationIndex());
                        if (jRouteForLocIndex == null) {
                            jRouteForLocIndex = new JSONArray();
                            routesByLocationIndex.put(route.getLocationIndex(), jRouteForLocIndex);
                        }

                        BBox bbox = new BBox(0, 0, 0, 0);
                        JSONArray jRoute = JsonRoutingResponseWriter.toJsonArray(reqRouting, new RouteResult[]{route}, bbox);
                        jRouteForLocIndex.put(jRoute.get(0));

                        if (minX > bbox.minLon)
                            minX = bbox.minLon;
                        if (minY > bbox.minLat)
                            minY = bbox.minLat;
                        if (maxX < bbox.maxLon)
                            maxX = bbox.maxLon;
                        if (maxY < bbox.maxLat)
                            maxY = bbox.maxLat;
                    }
                }

                for (Map.Entry<Integer, JSONArray> entry : routesByLocationIndex.entrySet())
                    jRoutes.put(entry.getValue());
            }

            jResp.put("bbox", GeometryJSON.toJSON(minX, minY, maxX, maxY));
        } else {
            JSONObject jLocations = new JSONObject(true);
            jResp.put("places", jLocations);

            JSONObject jRoutes = new JSONObject(true);
            jResp.put("routes", jRoutes);
        }

        // **************************************************

        writeInfoSection(jResp, request);

        ServletUtility.write(response, jResp);
    }

    private void writeInfoSection(JSONObject jResponse, AccessibilityRequest request) {
        JSONObject jInfo = new JSONObject(true);
        jInfo.put("service", "accessibility");
        jInfo.put("engine", AppInfo.getEngineInfo());
        if (!Helper.isEmpty(LocationsServiceSettings.getAttribution()))
            jInfo.put("attribution", LocationsServiceSettings.getAttribution());
        jInfo.put("timestamp", System.currentTimeMillis());

        if (AppConfig.hasValidMD5Hash())
            jInfo.put("osm_file_md5_hash", AppConfig.getMD5Hash());

        if (request != null) {
            JSONObject jQuery = new JSONObject(true);


            writeFilterSection(jQuery, request.getLocationsRequest().getSearchFilter());
            */
/*
			if (request.getRadius() > 0)
				jQuery.put("radius", request.getRadius());
			if (request.getLimit() > 0)
				jQuery.put("limit", request.getLimit());
			if (!Helper.isEmpty(request.getLanguage()))
				jQuery.put("lang", request.getLanguage());
			jQuery.put("locations", value) *//*


            if (request.getId() != null)
                jQuery.put("id", request.getId());


            jInfo.put("query", jQuery);
        }

        jResponse.put("info", jInfo);
    }

    private void writeFilterSection(JSONObject jQuery, LocationsSearchFilter query) {
        JSONObject jFilter = new JSONObject(true);
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

        if (jFilter.length() > 0)
            jQuery.put("filter", jFilter);
    }
}
*/
