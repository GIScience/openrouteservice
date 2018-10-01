/*
 *
 *  *
 *  *  *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *  *  *
 *  *  *   http://www.giscience.uni-hd.de
 *  *  *   http://www.heigit.org
 *  *  *
 *  *  *  under one or more contributor license agreements. See the NOTICE file
 *  *  *  distributed with this work for additional information regarding copyright
 *  *  *  ownership. The GIScience licenses this file to you under the Apache License,
 *  *  *  Version 2.0 (the "License"); you may not use this file except in compliance
 *  *  *  with the License. You may obtain a copy of the License at
 *  *  *
 *  *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  *  Unless required by applicable law or agreed to in writing, software
 *  *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  *  See the License for the specific language governing permissions and
 *  *  *  limitations under the License.
 *  *
 *
 */

package heigit.ors.globalResponseProcessor.geoJson;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import heigit.ors.isochrones.IsochroneRequest;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RoutingRequest;
import heigit.ors.services.routing.requestprocessors.json.JsonRoutingResponseWriter;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;

import static heigit.ors.util.JsonUtility.objectToJSONArray;
import static heigit.ors.util.JsonUtility.objectToJSONObject;

/**
 * The {@link GeoJsonResponseWriter} class handles the global GeoJSON export or should do this in the future.
 * The idea is to write easy to use code that can/should be reused as much as possible.
 * "homegrown" export solutions, that anyway use the same export ideas, can be combined this way.
 * The general access to this class should be through a toGeoJson function that does the magic.
 *
 * @author Julian Psotta, julian@openrouteservice.org
 */
public class GeoJsonResponseWriter {

    // Create static feature JSON with 6 decimals precision (never less than 6)
    private static FeatureJSON fjson = new FeatureJSON(new GeometryJSON(6));

    /**
     * The function transforms {@link RouteResult}'s in a ready-to-be-shipped {@link DefaultFeatureCollection} enriched with ORS specific information
     * The return value is a {@link JSONObject}.
     * The function is ready to process RouteResults[] Arrays with multiple Routes.
     * Results will always be {@link DefaultFeatureCollection} nevertheless the input consists of one ore more Routes.
     *
     * @param rreq        A {@link RoutingRequest} holding the initial Request.
     * @param routeResult A {@link RouteResult}.
     * @return It will always return a {@link DefaultFeatureCollection} in a {@link JSONObject} representation.
     * @throws Exception Throws an error if the JsonRoute could not be calculated
     */
    public static JSONObject toGeoJson(RoutingRequest rreq, RouteResult[] routeResult) throws Exception {
        HashMap<String, HashMap<String, Object>> featurePropertiesMap = new HashMap<>();
        HashMap<String, Object> defaultFeatureCollectionProperties = new HashMap<>();
        SimpleFeatureType ROUTINGFEATURETYPE = new SimpleFeatureTypes(SimpleFeatureTypes.RouteFeatureType.routeFeature).create();
        DefaultFeatureCollection defaultFeatureCollection = new DefaultFeatureCollection("routing", ROUTINGFEATURETYPE);

        GeometryFactory geometryFactory = new GeometryFactory();
        JSONObject jsonRoutes = JsonRoutingResponseWriter.toJson(rreq, routeResult);
        for (int i = 0; i < jsonRoutes.getJSONArray("routes").length(); i++) {
            JSONObject route = jsonRoutes.getJSONArray("routes").getJSONObject(i);
            SimpleFeatureBuilder routingFeatureBuilder = new SimpleFeatureBuilder(ROUTINGFEATURETYPE);
            SimpleFeature routingFeature = null;
            HashMap<String, Object> routingFeatureProperties = new HashMap<>();
            Coordinate[] coordinate_array = routeResult[i].getGeometry();
            LineString lineString = geometryFactory.createLineString(coordinate_array);

            routingFeatureBuilder.set("geometry", lineString);
            JSONArray route_keys = route.names();
            for (int j = 0; j < route_keys.length(); j++) {
                String key = route_keys.getString(j);
                if (!key.equals("geometry_format") && !key.equals("geometry")) {
                    routingFeatureProperties.put(key, route.get(key));
                }
            }

            routingFeature = routingFeatureBuilder.buildFeature(null);
            defaultFeatureCollection.add(routingFeature);
            featurePropertiesMap.put(routingFeature.getID(), routingFeatureProperties);

            JSONArray json_route_keys = jsonRoutes.names();
            for (int j = 0; j < json_route_keys.length(); j++) {
                String key = json_route_keys.getString(j);
                if (!key.equals("routes")) {
                    defaultFeatureCollectionProperties.put(key, jsonRoutes.get(key));
                }
            }
        }
        return addProperties(defaultFeatureCollection, featurePropertiesMap, defaultFeatureCollectionProperties);

    }

    /**
     * This is an example class and not yet integrated. It is reflecting the way additional GeoJSON exports should be integrated.
     *
     * @param rreq         A {@link IsochroneRequest}.
     * @param routeResults A {@link RouteResult}.
     * @return It will always return a {@link DefaultFeatureCollection} in a {@link JSONObject} representation.
     */
    public JSONObject toGeoJson(IsochroneRequest rreq, RouteResult[] routeResults) {
        return null;
    }

    /**
     * The function works as a simple access to add Properties to a single {@link SimpleFeature} from a {@link HashMap}.
     *
     * @param simpleFeature        A single and simple {@link SimpleFeature}.
     * @param featurePropertiesMap A {@link HashMap} in a {@link HashMap} ({@link HashMap} inception ;) holding the properties for each {@link SimpleFeature} referenced by a FeatureID. E.g. HashMap<String FeatureID, HashMap<String key, JSONArray value>>.
     * @return A complete {@link SimpleFeature} in a {@link JSONObject} representation with all necessary information will be returned
     * @throws IOException Throws an {@link IOException} if the {@link FeatureJSON} could not be processed.
     */
    public static JSONObject addProperties(SimpleFeature simpleFeature, HashMap<String, HashMap<String, Object>> featurePropertiesMap) throws IOException {
        StringWriter stringWriter = new StringWriter();
        fjson.writeFeature(simpleFeature, stringWriter);
        JSONObject featureAsJSON = new JSONObject(stringWriter.toString());
        stringWriter.close();
        return FeatureProperties(featureAsJSON, featurePropertiesMap);
    }

    /**
     * The function works as a simple access to add Properties to each {@link SimpleFeature} in a {@link DefaultFeatureCollection} and the surrounding {@link DefaultFeatureCollection} from {@link HashMap}'s.
     * The featurePropertiesMap is accessible through FeatureIDs.
     *
     * @param defaultFeatureCollection           {@link DefaultFeatureCollection} holding a set of {@link SimpleFeature} elements.
     * @param featurePropertiesMap               A {@link HashMap} in a {@link HashMap} ({@link HashMap} inception ;) holding the properties for each {@link SimpleFeature} referenced by a FeatureID. E.g. HashMap<String FeatureID, HashMap<String key, JSONArray value>>.
     * @param defaultFeatureCollectionProperties A simple {@link HashMap} holding additional information that should be added to the root of the {@link DefaultFeatureCollection}.
     * @return A complete {@link DefaultFeatureCollection} in a {@link JSONObject} representation with all necessary information will be returned
     * @throws IOException: Returns an IOException.
     */
    public static JSONObject addProperties(DefaultFeatureCollection defaultFeatureCollection, HashMap<String, HashMap<String, Object>> featurePropertiesMap, HashMap<String, Object> defaultFeatureCollectionProperties) throws IOException {
        Writer stringWriter = new StringWriter();
        fjson.writeFeatureCollection(defaultFeatureCollection, stringWriter);
        JSONObject featureCollectionAsJSON = new JSONObject(stringWriter.toString());
        stringWriter.close();
        JSONObject addFeatureProperties = FeatureProperties(featureCollectionAsJSON, featurePropertiesMap);
        return FeatureCollectionProperties(addFeatureProperties, defaultFeatureCollectionProperties);
    }

    /**
     * The function adds the properties from the featurePropertiesMap to each {@link SimpleFeature} stored in the {@link JSONObject} representation.
     * For that the {@link JSONObject} must be a {@link SimpleFeature} or {@link DefaultFeatureCollection} in {@link JSONObject} representation.
     * The Properties must be stored in a {@link HashMap} in a {@link HashMap}. E.g. HashMap<String FeatureID, HashMap<String key, JSONArray value>>.
     * The method FeatureProperties compares the FeatureIDs of the {@link DefaultFeatureCollection} or the {@link SimpleFeature} with those stores in the HashMap.
     * If matching FeatureIDs are found, the key value pairs are added to the property part of each {@link SimpleFeature}.
     *
     * @param featureOrFeatureCollection The input must to be a single {@link SimpleFeature} or a collection of {@link SimpleFeature} in a {@link DefaultFeatureCollection} represented by a {@link JSONObject}.
     * @param featurePropertiesMap       The input must be a {@link HashMap} in a {@link HashMap}. E.g. HashMap<String FeatureID, HashMap<String key, JSONArray value>>.
     * @return A single {@link SimpleFeature} or a collection of {@link SimpleFeature} in a {@link DefaultFeatureCollection} enriched with {@link SimpleFeature} properties will be returned.
     */
    private static JSONObject FeatureProperties(JSONObject featureOrFeatureCollection, HashMap<String, HashMap<String, Object>> featurePropertiesMap) {
        if (featureOrFeatureCollection.get("type").equals("FeatureCollection")) {
            JSONArray features = featureOrFeatureCollection.getJSONArray("features");
            for (int featureCount = 0; featureCount < features.length(); featureCount++) {
                String featureId = features.getJSONObject(featureCount).get("id").toString();
                if (featurePropertiesMap.containsKey(featureId)) {
                    HashMap<String, Object> featureProperties = featurePropertiesMap.get(featureId);
                    for (String key : featureProperties.keySet()) {
                        JSONObject jsonObj = objectToJSONObject(featureProperties.get(key));
                        JSONArray jsonArr = objectToJSONArray(featureProperties.get(key));

                        // To preserve backwards compatibility, we need to pass the summary object as an array of summary objects
                        if(key.equals("summary") && jsonObj != null) {
                            jsonArr = new JSONArray( new JSONObject[] { jsonObj });
                            jsonObj = null;
                        }

                        if (jsonObj != null) {
                            featureOrFeatureCollection.getJSONArray("features").getJSONObject(featureCount).getJSONObject("properties").put(key, jsonObj);
                        } else if (jsonArr != null) {
                            featureOrFeatureCollection.getJSONArray("features").getJSONObject(featureCount).getJSONObject("properties").put(key, jsonArr);
                        }
                    }
                }
            }
        } else if (featureOrFeatureCollection.get("type").equals("Feature")) {
            String featureId = featureOrFeatureCollection.get("id").toString();
            if (featurePropertiesMap.containsKey(featureId)) {
                HashMap<String, Object> featureProperties = featurePropertiesMap.get(featureId);
                for (String key : featureProperties.keySet()) {
                    JSONObject jsonObj = objectToJSONObject(featureProperties.get(key));
                    JSONArray jsonArr = objectToJSONArray(featureProperties.get(key));
                    if (jsonObj != null) {
                        featureOrFeatureCollection.getJSONObject("properties").put(key, jsonObj);
                    } else if (jsonArr != null) {
                        featureOrFeatureCollection.getJSONObject("properties").put(key, jsonArr);
                    }
                }
            }
        }
        return featureOrFeatureCollection;
    }

    /**
     * The function adds the properties from the featureCollectionProperties to the {@link DefaultFeatureCollection} in {@link JSONObject} representation.
     * For that the {@link JSONObject} must be a {@link DefaultFeatureCollection} in {@link JSONObject} representation.
     * The Properties must be stored in a simple {@link HashMap}. E.g. HashMap<String key, JSONArray value>.
     * The method adds every key value pair from the {@link HashMap} to the root of the {@link DefaultFeatureCollection}.
     *
     * @param featureCollection           The input must to be a {@link DefaultFeatureCollection} in a {@link JSONObject} representation
     * @param featureCollectionProperties The input must be a {@link HashMap}. E.g. HashMap<String key, JSONArray value>.
     * @return A {@link DefaultFeatureCollection} in {@link JSONObject} representation, enriched with properties, will be returned.
     */
    private static JSONObject FeatureCollectionProperties(JSONObject featureCollection, HashMap<String, Object> featureCollectionProperties) {
        if (featureCollection.get("type").equals("FeatureCollection")) {
            for (String key : featureCollectionProperties.keySet()) {
                JSONObject jsonObj = objectToJSONObject(featureCollectionProperties.get(key));
                JSONArray jsonArr = objectToJSONArray(featureCollectionProperties.get(key));
                if (jsonObj != null) {
                    featureCollection.put(key, jsonObj);
                } else if (jsonArr != null) {
                    featureCollection.put(key, jsonArr);
                }
            }
        }
        return featureCollection;
    }


}
