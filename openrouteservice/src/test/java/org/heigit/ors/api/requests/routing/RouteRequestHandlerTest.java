/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package org.heigit.ors.api.requests.routing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import org.heigit.ors.api.requests.common.APIEnums;
import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.exceptions.*;
import org.heigit.ors.routing.*;
import org.heigit.ors.routing.graphhopper.extensions.VehicleLoadCharacteristicsFlags;
import org.heigit.ors.routing.graphhopper.extensions.WheelchairTypesEncoder;
import org.heigit.ors.routing.parameters.VehicleParameters;
import org.heigit.ors.routing.parameters.WheelchairParameters;
import org.heigit.ors.routing.pathprocessors.BordersExtractor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class RouteRequestHandlerTest {
    RouteRequest request;

    private RequestProfileParamsRestrictions vehicleParams;
    private RequestProfileParamsRestrictions cyclingParams;
    private RequestProfileParamsRestrictions walkingParams;
    private RequestProfileParamsRestrictions wheelchairParams;

    private JSONObject geoJsonPolygon;

    public RouteRequestHandlerTest() throws Exception {
        init();
        geoJsonPolygon = constructGeoJson();
    }

    private JSONObject constructGeoJson() {
        JSONObject geoJsonPolygon = new JSONObject();
        geoJsonPolygon.put("type", "Polygon");
        JSONArray coordsArray = new JSONArray();
        coordsArray.add(new Double[] { 49.0, 8.0});
        coordsArray.add(new Double[] { 49.005, 8.01});
        coordsArray.add(new Double[] { 49.01, 8.0});
        coordsArray.add(new Double[] { 49.0, 8.0});
        JSONArray coordinates = new JSONArray();

        coordinates.add(coordsArray);
        geoJsonPolygon.put("coordinates", coordinates);

        return geoJsonPolygon;
    }

    @Before
    public void init() throws Exception {
        System.setProperty("ors_config", "target/test-classes/ors-config-test.json");

        /*List<Double[]> coords = new ArrayList<>();
        coords.add(new Double[] {24.5,39.2});
        coords.add(new Double[] {27.4,38.6});
        coords.add(new Double[] {26.5,37.2});
        List<Double> coord1 = new ArrayList<>();
        coord1.add(24.5);
        coord1.add(39.2);
        coords.add(coord1);
        List<Double> coord2 = new ArrayList<>();
        coord2.add(27.4);
        coord2.add(38.6);
        coords.add(coord2);
        List<Double> coord3 = new ArrayList<>();
        coord3.add(26.5);
        coord3.add(37.2);
        coords.add(coord3);*/

        Double[][] coords = new Double[3][2];
        coords[0] = new Double[] {24.5,39.2};
        coords[1] = new Double[] {27.4,38.6};
        coords[2] = new Double[] {26.5,37.2};

        request = new RouteRequest(coords);

        request.setProfile(APIEnums.Profile.DRIVING_CAR);
        request.setAttributes(new APIEnums.Attributes[] { APIEnums.Attributes.AVERAGE_SPEED, APIEnums.Attributes.DETOUR_FACTOR});
        request.setContinueStraightAtWaypoints(true);
        request.setExtraInfo(new APIEnums.ExtraInfo[] { APIEnums.ExtraInfo.OSM_ID});
        request.setIncludeGeometry(true);
        request.setIncludeInstructionsInResponse(true);
        request.setIncludeRoundaboutExitInfo(true);
        request.setIncludeManeuvers(true);
        request.setInstructionsFormat(APIEnums.InstructionsFormat.HTML);
        request.setLanguage(APIEnums.Languages.DE);
        request.setResponseType(APIEnums.RouteResponseType.GEOJSON);
        request.setUseElevation(true);
        request.setRoutePreference(APIEnums.RoutePreference.FASTEST);
        request.setUnits(APIEnums.Units.METRES);
        request.setUseContractionHierarchies(false);

        RouteRequestOptions options = new RouteRequestOptions();
        options.setAvoidBorders(APIEnums.AvoidBorders.CONTROLLED);
        options.setAvoidCountries(new String[] { "115" });
        options.setAvoidFeatures(new APIEnums.AvoidFeatures[] {APIEnums.AvoidFeatures.FORDS});

        options.setAvoidPolygonFeatures(geoJsonPolygon);

        vehicleParams = new RequestProfileParamsRestrictions();

        vehicleParams.setAxleLoad(10.0f);
        vehicleParams.setHazardousMaterial(true);
        vehicleParams.setHeight(5.0f);
        vehicleParams.setLength(15.0f);
        vehicleParams.setWeight(30.0f);
        vehicleParams.setWidth(4.5f);

        wheelchairParams = new RequestProfileParamsRestrictions();
        wheelchairParams.setMaxIncline(3);
        wheelchairParams.setMaxSlopedKerb(1.0f);
        wheelchairParams.setMinWidth(2.0f);
        wheelchairParams.setSmoothnessType(APIEnums.SmoothnessTypes.SMOOTHNESS_GOOD);
        wheelchairParams.setSurfaceType("asphalt");

        RequestProfileParams params = new RequestProfileParams();

        RequestProfileParamsWeightings weightings = new RequestProfileParamsWeightings();
        weightings.setGreenIndex(0.5f);
        weightings.setQuietIndex(0.2f);
        weightings.setSteepnessDifficulty(3);

        params.setWeightings(weightings);
        params.setSurfaceQualityKnown(true);
        params.setAllowUnsuitable(true);

        options.setProfileParams(params);
        request.setRouteOptions(options);
    }

    @Test
    public void convertRouteRequestTest() throws Exception {
        RoutingRequest routingRequest;

        routingRequest = request.convertRouteRequest();

        Assert.assertEquals(3, routingRequest.getCoordinates().length);

        Assert.assertEquals(RoutingProfileType.getFromString("driving-car"), routingRequest.getSearchParameters().getProfileType());
        Assert.assertArrayEquals(new String[] {"avgspeed", "detourfactor"}, routingRequest.getAttributes());

        Assert.assertTrue(routingRequest.getContinueStraight());

        Assert.assertEquals(RouteExtraInfoFlag.getFromString("osmid"), routingRequest.getExtraInfo());

        Assert.assertEquals("geojson", routingRequest.getGeometryFormat());
        Assert.assertTrue(routingRequest.getIncludeGeometry());
        Assert.assertTrue(routingRequest.getIncludeInstructions());
        Assert.assertTrue(routingRequest.getIncludeRoundaboutExits());
        Assert.assertTrue(routingRequest.getIncludeManeuvers());
        Assert.assertEquals(RouteInstructionsFormat.HTML, routingRequest.getInstructionsFormat());
        Assert.assertEquals("de", routingRequest.getLanguage());
        Assert.assertEquals("geojson", routingRequest.getGeometryFormat());
        Assert.assertTrue(routingRequest.getIncludeElevation());
        Assert.assertEquals(WeightingMethod.FASTEST, routingRequest.getSearchParameters().getWeightingMethod());
        Assert.assertEquals(DistanceUnit.METERS, routingRequest.getUnits());
        Assert.assertTrue(routingRequest.getSearchParameters().getFlexibleMode());

        Assert.assertEquals(BordersExtractor.Avoid.CONTROLLED, routingRequest.getSearchParameters().getAvoidBorders());
        Assert.assertArrayEquals(new int[] {115}, routingRequest.getSearchParameters().getAvoidCountries());
        Assert.assertEquals(AvoidFeatureFlags.getFromString("fords"), routingRequest.getSearchParameters().getAvoidFeatureTypes());

        checkPolygon(routingRequest.getSearchParameters().getAvoidAreas(), geoJsonPolygon);

        ProfileWeightingCollection weightings = routingRequest.getSearchParameters().getProfileParameters().getWeightings();
        ProfileWeighting weighting;
        Iterator<ProfileWeighting> iter = weightings.getIterator();
        while (iter.hasNext() && (weighting = iter.next()) != null) {
            if (weighting.getName().equals("green")) {
                Assert.assertEquals(0.5, weighting.getParameters().getDouble("factor", -1), 0);
            }
            if (weighting.getName().equals("quiet")) {
                Assert.assertEquals(0.2, weighting.getParameters().getDouble("factor", -1), 0);
            }
            if (weighting.getName().equals("steepness_difficulty")) {
                Assert.assertEquals(3, weighting.getParameters().getInt("level", -1), 0);
            }
        }
    }

    @Test
    public void TestVehicleParameters() throws Exception {
        request.setProfile(APIEnums.Profile.DRIVING_HGV);
        request.getRouteOptions().getProfileParams().setRestrictions(vehicleParams);
        request.getRouteOptions().setVehicleType(APIEnums.VehicleType.AGRICULTURAL);

        RoutingRequest routingRequest;
        routingRequest = request.convertRouteRequest();

        VehicleParameters params = (VehicleParameters) routingRequest.getSearchParameters().getProfileParameters();
        Assert.assertEquals(30.0, params.getWeight(), 0);
        Assert.assertEquals(10.0, params.getAxleload(), 0);
        Assert.assertEquals(5.0, params.getHeight(), 0);
        Assert.assertEquals(15.0, params.getLength(), 0);
        Assert.assertEquals(4.5, params.getWidth(), 0);
        Assert.assertEquals(new VehicleLoadCharacteristicsFlags().getFromString("hazmat"), params.getLoadCharacteristics());
    }

    @Test
    public void TestWheelchairParameters() throws Exception {
        request.setProfile(APIEnums.Profile.WHEELCHAIR);
        request.getRouteOptions().getProfileParams().setRestrictions(wheelchairParams);

        RoutingRequest routingRequest;

        routingRequest = request.convertRouteRequest();

        WheelchairParameters params = (WheelchairParameters) routingRequest.getSearchParameters().getProfileParameters();
        Assert.assertEquals(WheelchairTypesEncoder.getSmoothnessType(APIEnums.SmoothnessTypes.SMOOTHNESS_GOOD), params.getSmoothnessType());
        Assert.assertEquals(3.0f, params.getMaximumIncline(), 0);
        Assert.assertEquals(1.0f, params.getMaximumSlopedKerb(), 0);
        Assert.assertEquals(2.0f, params.getMinimumWidth(), 0);
        Assert.assertEquals(WheelchairTypesEncoder.getSurfaceType("asphalt"), params.getSurfaceType());
        Assert.assertEquals(true, params.isRequireSurfaceQualityKnown());
        Assert.assertEquals(true, params.allowUnsuitable());
    }

    @Test
    public void testBearings() throws StatusCodeException {
        request.setBearings(new Double[][] {{10.0,10.0},{260.0, 90.0},{45.0, 30.0}});

        RoutingRequest routingRequest = request.convertRouteRequest();

        WayPointBearing[] bearings = routingRequest.getSearchParameters().getBearings();
        Assert.assertEquals(10.0, bearings[0].getValue(), 0);
        Assert.assertEquals(10.0, bearings[0].getDeviation(), 0);
        Assert.assertEquals(260.0, bearings[1].getValue(), 0);
        Assert.assertEquals(90.0, bearings[1].getDeviation(), 0);
        Assert.assertEquals(45.0, bearings[2].getValue(), 0);
        Assert.assertEquals(30.0, bearings[2].getDeviation(), 0);
    }

    @Test
    public void skippedBearingTest() throws Exception {
        request.setBearings(new Double[][] {{120.0, 90.0}, { , }, {90.0, 30.0}});
        RoutingRequest routingRequest;

        routingRequest = request.convertRouteRequest();

        Assert.assertEquals(3, routingRequest.getSearchParameters().getBearings().length);
    }

    @Test(expected = ParameterValueException.class)
    public void invalidBearingLength() throws Exception {
        request.setBearings(new Double[][] {{123.0,123.0}});
        request.convertRouteRequest();
    }

    @Test
    public void testRadius() throws StatusCodeException {
        request.setMaximumSearchRadii(new Double[] { 50.0, 20.0, 100.0});

        RoutingRequest routingRequest = request.convertRouteRequest();
        Assert.assertTrue(Arrays.equals(new double[] { 50.0, 20.0, 100.0 }, routingRequest.getSearchParameters().getMaximumRadiuses()));
    }

    @Test(expected = ParameterValueException.class)
    public void invalidRadiusLength() throws Exception {
        request.setMaximumSearchRadii(new Double[] {10.0, 20.0});
        request.convertRouteRequest();
    }

    @Test
    public void testSingleRadius() throws Exception {
        request.setMaximumSearchRadii(new Double[]{50d});

        RoutingRequest routingRequest = request.convertRouteRequest();
        Assert.assertTrue(Arrays.equals(new double[] {50.0, 50.0, 50.0}, routingRequest.getSearchParameters().getMaximumRadiuses()));
    }

    @Test(expected = ParameterValueException.class)
    public void onlySetOptimizationToFalse() throws Exception {
        request.setUseContractionHierarchies(true);
        request.convertRouteRequest();
    }

    @Test
    public void vehicleType() throws Exception{
        RouteRequestOptions opts = request.getRouteOptions();
        opts.setVehicleType(APIEnums.VehicleType.AGRICULTURAL);

        for (APIEnums.Profile profile : APIEnums.Profile.values()) {
            request.setProfile(profile);
            request.setRouteOptions(opts);
            if (profile != APIEnums.Profile.DRIVING_HGV) {
                try {
                    request.convertRouteRequest();
                } catch (Exception e) {
                    Assert.assertTrue(e instanceof IncompatibleParameterException);
                }
            } else {
                request.convertRouteRequest();
            }
        }
    }

    @Test
    public void testSkippedSegments() throws StatusCodeException {

        List<Integer> skipSegments = new ArrayList<>();
        skipSegments.add(0, 1);
        skipSegments.add(1, 2);
        request.setSkipSegments(skipSegments);

        RoutingRequest routingRequest = request.convertRouteRequest();

        Assert.assertEquals(2, routingRequest.getSkipSegments().size());
        Assert.assertEquals(Integer.valueOf(1), routingRequest.getSkipSegments().get(0));
        Assert.assertEquals(Integer.valueOf(2), routingRequest.getSkipSegments().get(1));

    }

    @Test(expected = ParameterValueException.class)
    public void invalidSkipSegmentsLength() throws StatusCodeException {
        List<Integer> skip_segments = new ArrayList<>();
        skip_segments.add(0, 1);
        skip_segments.add(0, 2);
        skip_segments.add(0, 2);
        request.setSkipSegments(skip_segments);
        request.convertRouteRequest();
    }

    @Test(expected = EmptyElementException.class)
    public void emptySkipSegments() throws StatusCodeException {
        List<Integer> skip_segments = new ArrayList<>();
        request.setSkipSegments(skip_segments);
        request.convertRouteRequest();
    }

    @Test(expected = ParameterOutOfRangeException.class)
    public void skipSegmentsValueTooBig() throws StatusCodeException {
        List<Integer> skip_segments = new ArrayList<>();
        skip_segments.add(0, 99);
        request.setSkipSegments(skip_segments);
        request.convertRouteRequest();
    }

    @Test(expected = ParameterValueException.class)
    public void skipSegmentsValueTooSmall() throws StatusCodeException {
        List<Integer> skip_segments = new ArrayList<>();
        skip_segments.add(0, -99);
        request.setSkipSegments(skip_segments);
        request.convertRouteRequest();
    }

    @Test
    public void convertRouteRequestTestForAlternativeRoutes() throws Exception {
        Double[][] coords = new Double[2][2];
        coords[0] = new Double[] {24.5,39.2};
        coords[1] = new Double[] {26.5,37.2};
        RouteRequest arRequest = new RouteRequest(coords);
        arRequest.setProfile(APIEnums.Profile.DRIVING_CAR);

        RouteRequestAlternativeRoutes ar = new RouteRequestAlternativeRoutes();
        ar.setTargetCount(3);
        ar.setShareFactor(0.9);
        ar.setWeightFactor(1.8);
        arRequest.setAlternativeRoutes(ar);

        RoutingRequest routingRequest = arRequest.convertRouteRequest();
        Assert.assertEquals(3, routingRequest.getSearchParameters().getAlternativeRoutesCount());
        Assert.assertEquals(0.9, routingRequest.getSearchParameters().getAlternativeRoutesShareFactor(), 0);
        Assert.assertEquals(1.8, routingRequest.getSearchParameters().getAlternativeRoutesWeightFactor(), 0);
    }

    @Test(expected = MissingParameterException.class)
    public void testRoundTripNeedsLength() throws StatusCodeException {
        List<List<Double>> coordinates = new ArrayList<>();
        coordinates.add(new ArrayList<>(Arrays.asList(12.1234, 34.3456)));
        request.setCoordinates(coordinates);

        RouteRequestRoundTripOptions rtOptions = new RouteRequestRoundTripOptions();
        rtOptions.setPoints(4);
        RouteRequestOptions options = new RouteRequestOptions();
        options.setRoundTripOptions(rtOptions);
        request.setRouteOptions(options);

        request.convertRouteRequest();
    }

    @Test(expected = ParameterValueException.class)
    public void testSingleCoordinateNotValidForNonRoundTrip() throws StatusCodeException {
        List<List<Double>> coordinates = new ArrayList<>();
        coordinates.add(new ArrayList<>(Arrays.asList(12.1234, 34.3456)));
        request.setCoordinates(coordinates);

        request.convertRouteRequest();
    }

    @Test
    public void testSingleCoordinateValidForRoundTrip() throws StatusCodeException {
        List<List<Double>> coordinates = new ArrayList<>();
        coordinates.add(new ArrayList<>(Arrays.asList(12.1234, 34.3456)));
        request.setCoordinates(coordinates);

        RouteRequestRoundTripOptions rtOptions = new RouteRequestRoundTripOptions();
        rtOptions.setLength(400f);
        RouteRequestOptions options = new RouteRequestOptions();
        options.setRoundTripOptions(rtOptions);
        request.setRouteOptions(options);

        RoutingRequest generatedRoutingRequest = request.convertRouteRequest();
        Assert.assertEquals(1, generatedRoutingRequest.getCoordinates().length);
    }

    private void checkPolygon(Polygon[] requestPolys, JSONObject apiPolys) {
        Assert.assertEquals(1, requestPolys.length);

        JSONArray jsonCoords = (JSONArray)((JSONArray)apiPolys.get("coordinates")).get(0);
        for (int i=0; i<jsonCoords.size(); i++) {
            Double[] coordPair = (Double[]) jsonCoords.get(i);
            Coordinate c = new Coordinate(coordPair[0], coordPair[1]);

            compareCoordinates(c, requestPolys[0].getCoordinates()[i]);
        }
    }

    private void compareCoordinates(Coordinate c1, Coordinate c2) {
        Assert.assertEquals(c1.x, c2.x, 0);
        Assert.assertEquals(c1.y, c2.y, 0);
    }
}
