package heigit.ors.services.routing.requestprocessors.geojson;


import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RoutingProfileManager;
import heigit.ors.routing.RoutingRequest;
import heigit.ors.services.routing.requestprocessors.json.JsonRoutingResponseWriter;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geojson.GeoJSON;
import org.geotools.geojson.GeoJSONUtil;
import org.geotools.geojson.feature.FeatureCollectionHandler;
import org.geotools.geojson.feature.FeatureJSON;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Objects;

public class GeoJsonResponseWriter {
    private static RouteResult[] routeResults;
    private static RoutingRequest request;

    public static String toGeoJson(Object rreq, RouteResult[] routeResults) throws Exception {
        // Check if rreq is instanceof RoutingRequest --> If so calculate the GeoJsonExportWriter output
        // TODO Add different rreq types here
        if (rreq instanceof RoutingRequest){
            GeoJsonResponseWriter.request = (RoutingRequest) rreq;
            RouteResult result = RoutingProfileManager.getInstance().computeRoute((RoutingRequest) rreq);
            GeoJsonResponseWriter.routeResults = new RouteResult[]{result};
            //routeToGeoJson();
        }
        GeoJsonResponseWriter.routeResults = routeResults;
        // The GEOJSON Tool cant handle the JSONArray's properly and malformes them
        // So the RouteResult properties are stored in a HashMap with its id as an identifier
        // The values are added later
        HashMap<String, HashMap<String, JSONArray>> featurePropertiesMap = new HashMap<>();
        // create GeometryFactory for reuse purposes
        GeometryFactory geometryFactory = new GeometryFactory();
        // TODO: create FeatureCollection to add the single SimpleFeature's recursively
        FeatureCollectionHandler featureCollectionHandler = new FeatureCollectionHandler();
        // Create a new SimpleFeatureType to create a SimpleFeature from it
        // its written capital because a custom SimpleFeatureType is a static and immutable object
        SimpleFeatureType ROUTINGFEATURETYPE = FeatureParser.createRouteFeatureType();
        DefaultFeatureCollection defaultFeatureCollection = new DefaultFeatureCollection("routing", ROUTINGFEATURETYPE);
        JSONObject temporaryJsonRoute = JsonRoutingResponseWriter.toJson(request, routeResults);
        SimpleFeature routingFeature = null;
        SimpleFeature routingFeature2 = null;
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

        // TODO add properties of the collection to the featurePropertiesMap here…?!
        // Create feature JSON
        FeatureJSON fjson = new FeatureJSON();
        // Create the StringWriter to catch the JSON output
        StringWriter stringWriter = new StringWriter();
        // convert the geojson element to a JSONObject in a string representation
        fjson.writeFeatureCollection(defaultFeatureCollection, stringWriter);
        // try different json approaches --> JSONObject is enough
        // Create proper JSONObject from stringWriter to add properties and
        // query the defaultFeatureCollection with the internal addProperties and get the final string as a return
        JSONObject featureAsJSON = addFeatureProperties(new JSONObject(stringWriter.toString()), featurePropertiesMap);
        System.out.print(featureAsJSON);

        // LineString lineString = jsonTest.readLine(reader);
        // Integrate creation of GeoJSONs into GeometryJSON.class. Can be done for each object to provide generified conversions
        // JSONObject internalGeoJson = GeometryJSON.toGeoJSON(lineString);

        //
        // TODO: Write function to add "properties" to the geojson as an optional route feature that is only available for routes
        return null;

    }
    static JSONObject addFeatureCollectionProperties (DefaultFeatureCollection featureCollection, HashMap<String, HashMap<String, JSONArray>> featurePropertiesMap) throws Exception{
        // This function will call addFeatureProperties together with the HashMap that contains the property values
        return null;
    }

    /**
     * The function adds home made properties the {@link JSONObject} as long as it is a {@link FeatureJSON} that is filled up with features
     * @param featureAsJSON
     * @param featurePropertiesMap
     * @return
     * @throws Exception
     */
    // TODO Change first Parameter to feature object see in comparison addFeatureCollectionProperties
    static JSONObject addFeatureProperties(JSONObject featureAsJSON, HashMap<String, HashMap<String, JSONArray>> featurePropertiesMap) throws Exception {

        for (int featureCount = 0; featureCount < featureAsJSON.getJSONArray("features").length(); featureCount++) {
            // Get Feature Id to look for it in the featurePropertiesMap
            String featureId = featureAsJSON.getJSONArray("features").getJSONObject(featureCount).get("id").toString();
            // Look for the feature Id
            if (featurePropertiesMap.containsKey(featureId)) {
                // Get properties
                HashMap<String, JSONArray> featureProperties = featurePropertiesMap.get(featureId);
                // Create
                // set properties
                for (String key : featureProperties.keySet()) {
                    // Get the specific value by key
                    JSONArray value = featureProperties.get(key);
                    featureAsJSON.getJSONArray("features").getJSONObject(featureCount).getJSONObject("properties").put(key, value);
                }
            }

        }
        return featureAsJSON;
    }

    private SimpleFeatureType createSimpleFeatureType() {

        return null;
    }
}
