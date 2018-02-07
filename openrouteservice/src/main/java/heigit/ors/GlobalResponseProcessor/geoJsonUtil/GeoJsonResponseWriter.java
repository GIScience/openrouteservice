package heigit.ors.GlobalResponseProcessor.geoJsonUtil;


import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import heigit.ors.isochrones.IsochroneRequest;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RoutingRequest;
import heigit.ors.services.routing.requestprocessors.json.JsonRoutingResponseWriter;
import jdk.nashorn.internal.parser.JSONParser;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import javax.json.*;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@link GeoJsonResponseWriter} class handles the global GeoJSON export or should do this in the future.
 * The idea is to write easy to use code that can/should be reused as much as possible.
 * "homegrown" export solutions, that anyway use the same export ideas, can be combined this way.
 * The general access to this class should be through a toGeoJson function that does the magic.
 *
 * @author Julian Psotta
 * @version 1.0
 */
public class GeoJsonResponseWriter {

    /**
     * The function transforms {@link RouteResult}'s in a ready-to-be-shipped {@link DefaultFeatureCollection} enriched with ORS specific information
     * The return value will always be a {@link DefaultFeatureCollection}.
     * The reason is that it is the only way additional information can be added, that shouldn't go into the single {@link SimpleFeature}.
     *
     * @param rreq         A {@link RoutingRequest} holding the initial Request.
     * @param routeResults A {@link RouteResult[]}.
     * @return It will always return a {@link DefaultFeatureCollection} in a {@link JSONObject} representation.
     * @throws Exception
     */
    public static JSONObject toGeoJson(RoutingRequest rreq, RouteResult[] routeResults) throws Exception {

        // The GEOJSON Tool cant handle the JSONArray's properly and malformes them
        // Create HashMap of Hashmaps to store properties for individual Features in separated HashMaps, accessible through unique identifiers
        HashMap<String, HashMap<String, JSONArray>> featurePropertiesMap = new HashMap<>();
        // Create HashMap to store FeatureCollection properties. No identifier necessary because there will be just one FeatureCollection at a time
        HashMap<String, JsonValue> defaultFeatureCollectionProperties = new HashMap<>();
        // Create GeometryFactory for reuse purposes
        GeometryFactory geometryFactory = new GeometryFactory();
        // Create a new SimpleFeatureType to create a SimpleFeature from it
        // its written capital because a custom SimpleFeatureType is a static and immutable object once created
        SimpleFeatureType ROUTINGFEATURETYPE = SimpleFeatureTypes.createRouteFeatureType();
        DefaultFeatureCollection defaultFeatureCollection = new DefaultFeatureCollection("routing", ROUTINGFEATURETYPE);
        JSONObject temporaryJsonRoute = JsonRoutingResponseWriter.toJson(rreq, routeResults);
        SimpleFeature routingFeature = null;
        // SimpleFeature routingFeature2 = null;
        for (RouteResult route : routeResults) {
            HashMap<String, JSONArray> featureProperties = new HashMap<>();
            // Get the route as LineString
            LineString lineString = geometryFactory.createLineString(route.getGeometry());
            // Create a SimpleFeature from the ROUTINGFEATURETYPE template
            SimpleFeatureBuilder routingFeatureBuilder = new SimpleFeatureBuilder(ROUTINGFEATURETYPE);
            // SimpleFeatureBuilder routingFeatureBuilder2 = new SimpleFeatureBuilder(ROUTINGFEATURETYPE);
            // Add content to the SimpleFeature
            //Geometry
            routingFeatureBuilder.set("geometry", lineString);
            // routingFeatureBuilder2.set("geometry", lineString);
            // BBox
            featureProperties.put("bbox", temporaryJsonRoute.getJSONArray("routes").getJSONObject(0).getJSONArray("bbox"));
            //Way Points
            featureProperties.put("way_points", temporaryJsonRoute.getJSONArray("routes").getJSONObject(0).getJSONArray("way_points"));
            // Segments
            featureProperties.put("segments", temporaryJsonRoute.getJSONArray("routes").getJSONObject(0).getJSONArray("segments"));
            // send the feature to builder
            routingFeature = routingFeatureBuilder.buildFeature(null);
            // routingFeature2 = routingFeatureBuilder2.buildFeature(null);
            defaultFeatureCollection.add(routingFeature);
            // defaultFeatureCollection.add(routingFeature2);
            featurePropertiesMap.put(routingFeature.getID(), featureProperties);
        }
        StringReader stringReader = new StringReader(temporaryJsonRoute.toString());
        JsonReader jsonReader = Json.createReader(stringReader);
        JsonObject jsonObject = jsonReader.readObject();
        // Add the feature properties through a generalized class
        // TODO Add general addProperties here that does all the magic itseld and just returns the ready to use JSONObject
        //JSONObject geoJSON2 = addProperties(routingFeature, featurePropertiesMap);
        defaultFeatureCollectionProperties.put("bbox", jsonObject.get("bbox"));
        defaultFeatureCollectionProperties.put("info", jsonObject.get("info"));
        //System.out.print(featureCollectionAsJSON);

        // LineString lineString = jsonTest.readLine(reader);
        // Integrate creation of GeoJSONs into GeometryJSON.class. Can be done for each object to provide generified conversions
        // JSONObject internalGeoJson = GeometryJSON.toGeoJSON(lineString);

        //
        // TODO: Write function to add "properties" to the geojson as an optional route feature that is only available for routes
        return addProperties(defaultFeatureCollection, featurePropertiesMap, defaultFeatureCollectionProperties);

    }

    /**
     * This is an example class and not yet integrated. It is reflecting the way additional GeoJSON exports should be integrated.
     *
     * @param rreq         A {@link IsochroneRequest}.
     * @param routeResults A {@link RouteResult[]}.
     * @return It will always return a {@link DefaultFeatureCollection} in a {@link JSONObject} representation.
     * @throws Exception
     */
    protected JSONObject toGeoJson(IsochroneRequest rreq, RouteResult[] routeResults) throws Exception {
        return null;
    }

    /**
     * The function works as a simple access to add Properties to a single {@link SimpleFeature} from a {@link HashMap}.
     *
     * @param simpleFeature        A single and simple {@link SimpleFeature}.
     * @param featurePropertiesMap A {@link HashMap} in a {@link HashMap} ({@link HashMap} inception ;) holding the properties for each {@link SimpleFeature} referenced by a FeatureID. E.g. HashMap<String FeatureID, HashMap<String key, JSONArray value>>.
     * @return A complete {@link SimpleFeature} in a {@link JSONObject} representation with all necessary information will be returned
     * @throws Exception
     */
    private static JSONObject addProperties(SimpleFeature simpleFeature, HashMap<String, HashMap<String, JSONArray>> featurePropertiesMap) throws Exception {
        FeatureJSON fjson = new FeatureJSON();
        StringWriter stringWriter = new StringWriter();
        fjson.writeFeature(simpleFeature, stringWriter);
        JSONObject featureAsJSON = new JSONObject(stringWriter.toString());


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
     * @throws Exception
     */
    private static JSONObject addProperties(DefaultFeatureCollection defaultFeatureCollection, HashMap<String, HashMap<String, JSONArray>> featurePropertiesMap, HashMap<String, JsonValue> defaultFeatureCollectionProperties) throws Exception {
        // Create feature JSON
        FeatureJSON fjson = new FeatureJSON();
        // Create the StringWriter to catch the JSON output
        StringWriter stringWriter = new StringWriter();
        // convert the geojson element to a JSONObject in a string representation using the FeatureJSON
        fjson.writeFeatureCollection(defaultFeatureCollection, stringWriter);
        JSONObject featureCollectionAsJSON = new JSONObject(stringWriter.toString());
        JsonReader jsonReader = Json.createReader(new StringReader(stringWriter.toString()));
        JsonObject jsonObject = jsonReader.readObject();
        jsonReader.close();
        //JsonReader jsonReader2 = Json.createReader(new StringReader(stringWriter.toString()));
        //JsonStructure jsonStructure = jsonReader2.read();
        //jsonReader2.close();
        //String temp1 = jsonObject.toString();
        //String temp2 = jsonStructure.toString();
        //JSONObject addFeatureProperties = FeatureProperties(jsonObject, featurePropertiesMap);

        FeatureCollectionProperties(jsonObject, defaultFeatureCollectionProperties);
        return null;
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
     * @throws Exception
     */
    private static JSONObject FeatureProperties(JSONObject featureOrFeatureCollection, HashMap<String, HashMap<String, JSONArray>> featurePropertiesMap) throws Exception {
        // Check if the the JSONObject is a FeatureCollection
        if (featureOrFeatureCollection.get("type").toString().equals("FeatureCollection")) {
            for (int featureCount = 0; featureCount < featureOrFeatureCollection.getJSONArray("features").length(); featureCount++) {
                // Get Feature Id to look for it in the featureProperties
                String featureId = featureOrFeatureCollection.getJSONArray("features").getJSONObject(featureCount).get("id").toString();
                // Look for the feature Id
                if (featurePropertiesMap.containsKey(featureId)) {
                    // Get properties
                    HashMap<String, JSONArray> featureProperties = featurePropertiesMap.get(featureId);
                    // set properties
                    for (String key : featureProperties.keySet()) {
                        // Get the specific value by key
                        JSONArray value = featureProperties.get(key);
                        featureOrFeatureCollection.getJSONArray("features").getJSONObject(featureCount).getJSONObject("properties").put(key, value);
                    }
                }
            }
        }
        // Check if the JSONObject is a SimpleFeature
        else if (featureOrFeatureCollection.get("type").equals("Feature")) {
            // Get Feature Id to look for it in the featureProperties
            String featureId = featureOrFeatureCollection.get("id").toString();
            // Look for the feature Id
            if (featurePropertiesMap.containsKey(featureId)) {
                // Get properties
                HashMap<String, JSONArray> featureProperties = featurePropertiesMap.get(featureId);
                // Iterate over the HashMap and add each property to the Feature
                for (String key : featureProperties.keySet()) {
                    // Get the specific value by key
                    JSONArray value = featureProperties.get(key);
                    featureOrFeatureCollection.getJSONObject("properties").put(key, value);
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
     * @throws Exception
     */
    private static JsonObject FeatureCollectionProperties(JsonObject featureCollection, HashMap<String, JsonValue> featureCollectionProperties) throws Exception {
        // Check if the JSONObject is a FeatureCollection
        if (featureCollection.get("type").toString().contains("FeatureCollection")) {
            // Iterate over HashMap and add each Property to the FeatureCollection
            for (String key : featureCollectionProperties.keySet()
                    ) {
                // Get the specific value by key
                JsonValue value = featureCollectionProperties.get(key);
                JsonObjectBuilder jsonObjectBuilder = jsonObjectToBuilder(featureCollection);
                jsonObjectBuilder.add(key, value);
                featureCollection = jsonObjectBuilder.build();
            }
        }
        return featureCollection;
    }

    private static JsonObjectBuilder jsonObjectToBuilder(JsonObject jsonObject) {
        JsonObjectBuilder job = Json.createObjectBuilder();

        for (Map.Entry<String, JsonValue> entry : jsonObject.entrySet()) {
            job.add(entry.getKey(), entry.getValue());
        }

        return job;
    }
}
