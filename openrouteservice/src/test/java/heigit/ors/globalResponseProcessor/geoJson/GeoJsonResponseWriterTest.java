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
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.json.JSONArray;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.util.HashMap;

/**
 * This class tests the methods of {@link GeoJsonResponseWriter}.
 * The main entry method cannot be tested due to limited tomcat functions while JUnit-Testing.
 * Only the shared methods, that are shared among toGeoJSON conversions, are tested.
 *
 * @author Julian Psotta, julian@openrouteservice.org
 */
public class GeoJsonResponseWriterTest {
    // private static RoutingRequest routingRequest;
    // private static RouteResult[] routeResult;
    private static HashMap<String, HashMap<String, JSONArray>> featurePropertiesMap;
    private static HashMap<String, Object> defaultFeatureCollectionProperties;
    private static DefaultFeatureCollection defaultFeatureCollection;
    private static SimpleFeature routingFeature = null;
    private static String routingFeatureID;

    /**
     * This method sets up the test environment.
     *
     * @throws Exception If something goes wrong, the function will raise an {@link Exception}.
     */
    @BeforeClass
    public static void setUp() {
        // routingRequest = new RoutingRequestMockup().create(RoutingRequestMockup.routeProfile.standardHeidelberg2d);
        // routeResult = RouteResultMockup.create(RouteResultMockup.routeResultProfile.standardHeidelberg);
        // Create Line coordinate
        Coordinate[] coords2d = new Coordinate[3];
        // Fill the two-dimensional coordinate
        coords2d[0] = new Coordinate(Double.parseDouble("0.0"), Double.parseDouble("0.0"));
        coords2d[1] = new Coordinate(Double.parseDouble("0.0"), Double.parseDouble("0.0"));
        coords2d[2] = new Coordinate(Double.parseDouble("0.0"), Double.parseDouble("0.0"));
        // Create HashMap of HashMaps to store properties for individual Features in it, accessible through unique identifiers
        featurePropertiesMap = new HashMap<>();
        // Create HashMap to store FeatureCollection properties. No identifier necessary because there will be just one FeatureCollection at a time
        defaultFeatureCollectionProperties = new HashMap<>();
        // Create GeometryFactory for reuse purposes
        GeometryFactory geometryFactory = new GeometryFactory();
        // Create a new SimpleFeatureType to create a SimpleFeature from it
        // its written capital because a custom SimpleFeatureType is a static and immutable object once created
        SimpleFeatureType ROUTINGFEATURETYPE = SimpleFeatureTypes.createRouteFeatureType();
        // Create DefaultFeatureCollection to store the SimpleFeature
        defaultFeatureCollection = new DefaultFeatureCollection("routing", ROUTINGFEATURETYPE);
        // Create a HashMap for the individual feature properties
        HashMap<String, JSONArray> featureProperties = new HashMap<>();
        // Get the route specific Geometry as LineString
        LineString lineString = geometryFactory.createLineString(coords2d);
        // Create a SimpleFeature from the ROUTINGFEATURETYPE template
        SimpleFeatureBuilder routingFeatureBuilder = new SimpleFeatureBuilder(ROUTINGFEATURETYPE);
        // Add route specific Geometry
        routingFeatureBuilder.set("geometry", lineString);
        // Add route specific BBox
        featureProperties.put("bbox", new JSONArray().put(0.0).put(0.0).put(0.0).put(0.0));
        // Add route specific Way_Points
        featureProperties.put("way_points", new JSONArray().put(0.0).put(0.0));
        // Add route specific Segments
        featureProperties.put("segments", new JSONArray().put(0.0));
        // Build the SimpleFeature
        routingFeature = routingFeatureBuilder.buildFeature(null);
        routingFeatureID = routingFeature.getID();
        defaultFeatureCollection.add(routingFeature);
        featurePropertiesMap.put(routingFeature.getID(), featureProperties);


        // Add the feature properties through a generalized class
        defaultFeatureCollectionProperties.put("bbox", new JSONArray().put(0.0).put(0.0).put(0.0).put(0.0));
        defaultFeatureCollectionProperties.put("info", new JSONArray().put(0));
    }

    /**
     * This method tests the addProperties() function and makes sure that {@link SimpleFeature} extensions are set properly.
     *
     * @throws Exception If something goes wrong, the function will raise an {@link Exception}.
     */
    @Test
    public void testAddProperties() throws Exception {
        // JSONObject addProperties = GeoJsonResponseWriter.addProperties(routingFeature, featurePropertiesMap);
        Assert.assertEquals("{\"geometry\":{\"coordinates\":[[0,0],[0,0],[0,0]],\"type\":\"LineString\"},\"id\":\"" + routingFeatureID + "\",\"type\":\"Feature\",\"properties\":{\"bbox\":[0,0,0,0],\"way_points\":[0,0],\"segments\":[0]}}", GeoJsonResponseWriter.addProperties(routingFeature, featurePropertiesMap).toString());
    }

    /**
     * This method tests the addProperties() function and makes sure that {@link SimpleFeature} and {@link DefaultFeatureCollection} extensions are set properly.
     *
     * @throws Exception If something goes wrong, the function will raise an {@link Exception}.
     */
    @Test
    public void testAddProperties1() throws Exception {
        // JSONObject addProperties = GeoJsonResponseWriter.addProperties(defaultFeatureCollection, featurePropertiesMap, defaultFeatureCollectionProperties);
        Assert.assertEquals("{\"features\":[{\"geometry\":{\"coordinates\":[[0,0],[0,0],[0,0]],\"type\":\"LineString\"},\"id\":\"" + routingFeatureID + "\",\"type\":\"Feature\",\"properties\":{\"bbox\":[0,0,0,0],\"way_points\":[0,0],\"segments\":[0]}}],\"bbox\":[0,0,0,0],\"type\":\"FeatureCollection\",\"info\":[0]}", GeoJsonResponseWriter.addProperties(defaultFeatureCollection, featurePropertiesMap, defaultFeatureCollectionProperties).toString());
    }
}