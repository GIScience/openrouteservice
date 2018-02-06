package heigit.ors.util.GlobalResponseProcessor.geoJsonUtil;


import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RoutingRequest;
import heigit.ors.services.routing.requestprocessors.json.JsonRoutingResponseWriter;
import heigit.ors.util.GlobalResponseProcessor.geoJsonUtil.beans.FeatureTypes;
import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureReader;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.StringWriter;
import java.util.HashMap;

public class GeoJsonResponseWriter {
    private static RouteResult[] routeResults;
    private static RoutingRequest request;


    /**
     * @param rreq
     * @param routeResults
     * @return
     * @throws Exception
     */
    public static JSONObject toGeoJson(RoutingRequest rreq, RouteResult[] routeResults) throws Exception {
        request = rreq;
        GeoJsonResponseWriter.routeResults = routeResults;

        // The GEOJSON Tool cant handle the JSONArray's properly and malformes them
        // So the RouteResult properties are stored in a HashMap with its id as an identifier
        // The values are added later
        HashMap<String, HashMap<String, JSONArray>> featurePropertiesMap = new HashMap<>();
        // create GeometryFactory for reuse purposes
        GeometryFactory geometryFactory = new GeometryFactory();
        // Create a new SimpleFeatureType to create a SimpleFeature from it
        // its written capital because a custom SimpleFeatureType is a static and immutable object once created
        SimpleFeatureType ROUTINGFEATURETYPE = FeatureTypes.createRouteFeatureType();
        DefaultFeatureCollection defaultFeatureCollection = new DefaultFeatureCollection("routing", ROUTINGFEATURETYPE);
        JSONObject temporaryJsonRoute = JsonRoutingResponseWriter.toJson(request, routeResults);
        SimpleFeature routingFeature;
        SimpleFeature routingFeature2;
        for (RouteResult route : routeResults) {
            HashMap<String, JSONArray> featureProperties = new HashMap<>();
            // Get the route as LineString
            LineString lineString = geometryFactory.createLineString(route.getGeometry());
            // Create a SimpleFeature from the ROUTINGFEATURETYPE template
            SimpleFeatureBuilder routingFeatureBuilder = new SimpleFeatureBuilder(ROUTINGFEATURETYPE);
            SimpleFeatureBuilder routingFeatureBuilder2 = new SimpleFeatureBuilder(ROUTINGFEATURETYPE);
            // Add content to the SimpleFeature
            //Geometry
            routingFeatureBuilder.set("geometry", lineString);
            routingFeatureBuilder2.set("geometry", lineString);
            // BBox
            featureProperties.put("bbox", temporaryJsonRoute.getJSONArray("routes").getJSONObject(0).getJSONArray("bbox"));
            //Way Points
            featureProperties.put("way_points", temporaryJsonRoute.getJSONArray("routes").getJSONObject(0).getJSONArray("way_points"));
            // Segments
            featureProperties.put("segments", temporaryJsonRoute.getJSONArray("routes").getJSONObject(0).getJSONArray("segments"));
            // send the feature to builder
            routingFeature = routingFeatureBuilder.buildFeature(null);
            routingFeature2 = routingFeatureBuilder2.buildFeature(null);
            defaultFeatureCollection.add(routingFeature);
            defaultFeatureCollection.add(routingFeature2);
            // TODO get the values in the featureProperties List and remove them from the routingFeatureBuilder first, add them later -->
            featurePropertiesMap.put(routingFeature.getID(), featureProperties);
        }


        // Create proper JSONObject from stringWriter to add properties and
        // query the defaultFeatureCollection with the internal addProperties and get the final string as a return
        // TODO Add general addProperties here that does all the magic itseld and just returns the ready to use JSONObject
        JSONObject addFeatureProperties = addFeatureProperties(defaultFeatureCollection, featurePropertiesMap);
        JSONObject addFeatureCollectionProperties = addProperties(defaultFeatureCollection, featurePropertiesMap);
        //System.out.print(featureCollectionAsJSON);

        // LineString lineString = jsonTest.readLine(reader);
        // Integrate creation of GeoJSONs into GeometryJSON.class. Can be done for each object to provide generified conversions
        // JSONObject internalGeoJson = GeometryJSON.toGeoJSON(lineString);

        //
        // TODO: Write function to add "properties" to the geojson as an optional route feature that is only available for routes
        return null;

    }

    static JSONObject addProperties(DefaultFeatureCollection defaultFeatureCollection, HashMap<String, HashMap<String, JSONArray>> featurePropertiesMap){
        JSONObject JSONObject = new JSONObject();

        return JSONObject;
    }
    /**
     * The function adds home made properties the {@link JSONObject} as long as it is a {@link FeatureJSON} that is filled up with features
     *
     * @param featureCollection
     * @param featurePropertiesMap
     * @return
     * @throws Exception
     */
    // TODO Change first Parameter to feature object see in comparison addFeatureCollectionProperties
    static JSONObject addFeatureProperties(DefaultFeatureCollection featureCollection, HashMap<String, HashMap<String, JSONArray>> featurePropertiesMap) throws Exception {
        // TODO add properties of the collection to the featurePropertiesMap here…?!
        // Create feature JSON
        FeatureJSON fjson = new FeatureJSON();
        // Create the StringWriter to catch the JSON output
        StringWriter stringWriter = new StringWriter();
        // convert the geojson element to a JSONObject in a string representation
        fjson.writeFeatureCollection(featureCollection, stringWriter);
        JSONObject featureCollectionAsJSON = new JSONObject(stringWriter.toString());

        for (int featureCount = 0; featureCount < featureCollectionAsJSON.getJSONArray("features").length(); featureCount++) {
            // Get Feature Id to look for it in the featurePropertiesMap
            String featureId = featureCollectionAsJSON.getJSONArray("features").getJSONObject(featureCount).get("id").toString();
            // Look for the feature Id
            if (featurePropertiesMap.containsKey(featureId)) {
                // Get properties
                HashMap<String, JSONArray> featureProperties = featurePropertiesMap.get(featureId);
                // Create
                // set properties
                for (String key : featureProperties.keySet()) {
                    // Get the specific value by key
                    JSONArray value = featureProperties.get(key);
                    featureCollectionAsJSON.getJSONArray("features").getJSONObject(featureCount).getJSONObject("properties").put(key, value);
                }
            }

        }
        return featureCollectionAsJSON;
    }

    /**
     * @param featureCollection
     * @param featurePropertiesMap
     * @return
     * @throws Exception
     */

    static JSONObject addFeatureCollectionProperties(DefaultFeatureCollection featureCollection) throws Exception {
        // This function will call addFeatureProperties together with the HashMap that contains the property values
        return null;
    }


}
