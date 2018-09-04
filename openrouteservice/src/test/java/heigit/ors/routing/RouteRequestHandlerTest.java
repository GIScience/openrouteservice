package heigit.ors.routing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import heigit.ors.api.requests.routing.*;
import heigit.ors.common.DistanceUnit;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributes;
import heigit.ors.routing.graphhopper.extensions.VehicleLoadCharacteristicsFlags;
import heigit.ors.routing.graphhopper.extensions.WheelchairTypesEncoder;
import heigit.ors.routing.parameters.CyclingParameters;
import heigit.ors.routing.parameters.VehicleParameters;
import heigit.ors.routing.parameters.WalkingParameters;
import heigit.ors.routing.parameters.WheelchairParameters;
import heigit.ors.routing.pathprocessors.BordersExtractor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;

public class RouteRequestHandlerTest {
    RouteRequest request;

    private RequestProfileParamsRestrictions vehicleParams;
    private RequestProfileParamsRestrictions cyclingParams;
    private RequestProfileParamsRestrictions walkingParams;
    private RequestProfileParamsRestrictions wheelchairParams;

    private JSONObject geoJsonPolygon;

    public RouteRequestHandlerTest() {
        init();
        geoJsonPolygon = constructGeoJson();
    }

    private JSONObject constructGeoJson() {
        JSONObject geoJsonPolygon = new JSONObject();
        geoJsonPolygon.put("type", "Polygon");
        JSONArray coordsArray = new JSONArray();
        coordsArray.add(new Double[] { 123.0, 100.0});
        coordsArray.add(new Double[] { 150.0, 138.0});
        coordsArray.add(new Double[] { 140.0, 115.0});
        coordsArray.add(new Double[] { 123.0, 100.0});
        JSONArray coordinates = new JSONArray();

        coordinates.add(coordsArray);
        geoJsonPolygon.put("coordinates", coordinates);

        return geoJsonPolygon;
    }

    @Before
    public void init() {
        request = new RouteRequest(new Double[] {24.5, 39.2}, new Double[] {26.5, 37.2});

        request.setVia(new Double[][] {{27.4, 38.6}});

        request.setProfile(APIRoutingEnums.RoutingProfile.DRIVING_CAR);
        request.setAttributes(new APIRoutingEnums.Attributes[] { APIRoutingEnums.Attributes.AVERAGE_SPEED, APIRoutingEnums.Attributes.DETOUR_FACTOR});
        request.setBearings(new Double[][] {{10.0,10.0},{260.0, 90.0},{45.0, 30.0}});
        request.setContinueStraightAtWaypoints(true);
        request.setExtraInfo(new APIRoutingEnums.ExtraInfo[] { APIRoutingEnums.ExtraInfo.OSM_ID});
        request.setGeometryType(APIRoutingEnums.RouteResponseGeometryType.GEOJSON);
        request.setIncludeGeometry(true);
        request.setIncludeInstructionsInResponse(true);
        request.setIncludeRoundaboutExitInfo(true);
        request.setIncĺudeManeuvers(true);
        request.setInstructionsFormat(APIRoutingEnums.InstructionsFormat.HTML);
        request.setLanguage(APIRoutingEnums.Languages.DE);
        request.setMaximumSearchRadii(new Double[] { 50.0, 20.0, 100.0});
        request.setResponseType(APIRoutingEnums.RouteResponseType.GEOJSON);
        request.setReturnElevationForPoints(true);
        request.setRoutePreference(APIRoutingEnums.RoutePreference.FASTEST);
        request.setSimplifyGeometry(false);
        request.setUnits(APIRoutingEnums.Units.METRES);
        request.setUseContractionHierarchies(false);

        RouteRequestOptions options = new RouteRequestOptions();
        options.setAvoidBorders(APIRoutingEnums.AvoidBorders.CONTROLLED);
        options.setAvoidCountries(new int[] { 115 });
        options.setAvoidFeatures(new APIRoutingEnums.AvoidFeatures[] {APIRoutingEnums.AvoidFeatures.FORDS});

        options.setAvoidPolygonFeatures(geoJsonPolygon);
        options.setMaximumSpeed(120.0);
        options.setVehicleType(APIRoutingEnums.VehicleType.AGRICULTURAL);

        vehicleParams = new RequestProfileParamsRestrictions();

        vehicleParams.setAxleLoad(10.0f);
        vehicleParams.setGradient(5);
        vehicleParams.setHazardousMaterial(true);
        vehicleParams.setHeight(5.0f);
        vehicleParams.setLength(15.0f);
        vehicleParams.setWeight(30.0f);
        vehicleParams.setWidth(4.5f);

        cyclingParams = new RequestProfileParamsRestrictions();
        cyclingParams.setGradient(6);
        cyclingParams.setTrailDifficulty(2);

        walkingParams = new RequestProfileParamsRestrictions();
        walkingParams.setGradient(6);
        walkingParams.setTrailDifficulty(2);

        wheelchairParams = new RequestProfileParamsRestrictions();
        wheelchairParams.setMaxIncline(3);
        wheelchairParams.setMaxSlopedKerb(1.0f);
        wheelchairParams.setMinWidth(200);
        wheelchairParams.setSmoothnessType("good");
        wheelchairParams.setSurfaceType("asphalt");

        RequestProfileParams params = new RequestProfileParams();

        RequestProfileParamsWeightings weightings = new RequestProfileParamsWeightings();
        weightings.setGreenIndex(0.5f);
        weightings.setQuietIndex(0.2f);
        weightings.setSteepnessDifficulty(3);

        params.setWeightings(weightings);

        options.setProfileParams(params);
        request.setRouteOptions(options);
    }

    @Test
    public void convertRouteRequestTest() throws Exception {
        RoutingRequest routingRequest;

        routingRequest = RouteRequestHandler.convertRouteRequest(request);

        Assert.assertEquals(3, routingRequest.getCoordinates().length);

        Assert.assertEquals(RoutingProfileType.getFromString("driving-car"), routingRequest.getSearchParameters().getProfileType());
        Assert.assertArrayEquals(new String[] {"avgspeed", "detourfactor"}, routingRequest.getAttributes());

        WayPointBearing[] bearings = routingRequest.getSearchParameters().getBearings();
        Assert.assertEquals(bearings[0].getValue(), 10.0, 0);
        Assert.assertEquals(bearings[0].getDeviation(), 10.0, 0);
        Assert.assertEquals(bearings[1].getValue(), 260.0, 0);
        Assert.assertEquals(bearings[1].getDeviation(), 90.0, 0);
        Assert.assertEquals(bearings[2].getValue(), 45.0, 0);
        Assert.assertEquals(bearings[2].getDeviation(), 30.0, 0);

        Assert.assertTrue(routingRequest.getContinueStraight());

        Assert.assertEquals(RouteExtraInfoFlag.getFromString("osmid"), routingRequest.getExtraInfo());

        Assert.assertEquals("geojson", routingRequest.getGeometryFormat());
        Assert.assertTrue(routingRequest.getIncludeGeometry());
        Assert.assertTrue(routingRequest.getIncludeInstructions());
        Assert.assertTrue(routingRequest.getIncludeRoundaboutExits());
        Assert.assertTrue(routingRequest.getIncludeManeuvers());
        Assert.assertEquals(RouteInstructionsFormat.HTML, routingRequest.getInstructionsFormat());
        Assert.assertEquals("de", routingRequest.getLanguage());
        Assert.assertTrue(Arrays.equals(new double[] { 50.0, 20.0, 100.0 }, routingRequest.getSearchParameters().getMaximumRadiuses()));
        Assert.assertEquals("geojson", routingRequest.getGeometryFormat());
        Assert.assertTrue(routingRequest.getIncludeElevation());
        Assert.assertEquals(WeightingMethod.FASTEST, routingRequest.getSearchParameters().getWeightingMethod());
        Assert.assertFalse(routingRequest.getSimplifyGeometry());
        Assert.assertEquals(DistanceUnit.Meters, routingRequest.getUnits());
        Assert.assertTrue(routingRequest.getSearchParameters().getFlexibleMode());

        Assert.assertEquals(BordersExtractor.Avoid.CONTROLLED, routingRequest.getSearchParameters().getAvoidBorders());
        Assert.assertArrayEquals(new int[] {115}, routingRequest.getSearchParameters().getAvoidCountries());
        Assert.assertEquals(AvoidFeatureFlags.getFromString("fords"), routingRequest.getSearchParameters().getAvoidFeatureTypes());

        checkPolygon(routingRequest.getSearchParameters().getAvoidAreas(), geoJsonPolygon);

        Assert.assertEquals(120.0, routingRequest.getSearchParameters().getMaximumSpeed(), 0);
        Assert.assertEquals(HeavyVehicleAttributes.AGRICULTURE, routingRequest.getSearchParameters().getVehicleType());

        ProfileWeightingCollection weightings = routingRequest.getSearchParameters().getProfileParameters().getWeightings();
        ProfileWeighting weighting;
        Iterator<ProfileWeighting> iter = weightings.getIterator();
        while(iter.hasNext() && (weighting = iter.next()) != null) {
            if(weighting.getName().equals("green")) {
                Assert.assertEquals(0.5, weighting.getParameters().getDouble("factor", -1), 0);
            }
            if(weighting.getName().equals("quiet")) {
                Assert.assertEquals(0.2, weighting.getParameters().getDouble("factor", -1), 0);
            }
            if(weighting.getName().equals("steepness_difficulty")) {
                Assert.assertEquals(3, weighting.getParameters().getInt("level", -1), 0);
            }
        }
    }

    @Test
    public void TestVehicleParameters() throws Exception {
        request.setProfile(APIRoutingEnums.RoutingProfile.DRIVING_HGV);
        request.getRouteOptions().getProfileParams().setRestrictions(vehicleParams);

        RoutingRequest routingRequest;
        routingRequest = RouteRequestHandler.convertRouteRequest(request);

        VehicleParameters params = (VehicleParameters) routingRequest.getSearchParameters().getProfileParameters();
        Assert.assertEquals(30.0, params.getWeight(), 0);
        Assert.assertEquals(10.0, params.getAxleload(), 0);
        Assert.assertEquals(5.0, params.getHeight(), 0);
        Assert.assertEquals(15.0, params.getLength(), 0);
        Assert.assertEquals(4.5, params.getWidth(), 0);
        Assert.assertEquals(new VehicleLoadCharacteristicsFlags().getFromString("hazmat"), params.getLoadCharacteristics());
    }

    @Test
    public void TestCyclingParameters() throws Exception {
        request.setProfile(APIRoutingEnums.RoutingProfile.CYCLING_REGULAR);
        request.getRouteOptions().getProfileParams().setRestrictions(cyclingParams);

        RoutingRequest routingRequest;
        routingRequest = RouteRequestHandler.convertRouteRequest(request);

        CyclingParameters params = (CyclingParameters) routingRequest.getSearchParameters().getProfileParameters();
        Assert.assertEquals(2, params.getMaximumTrailDifficulty());
        Assert.assertEquals(6, params.getMaximumGradient());
    }

    @Test
    public void TestWalkingParameters() throws Exception {
        request.setProfile(APIRoutingEnums.RoutingProfile.FOOT_WALKING);
        request.getRouteOptions().getProfileParams().setRestrictions(walkingParams);

        RoutingRequest routingRequest;
        routingRequest = RouteRequestHandler.convertRouteRequest(request);

        WalkingParameters params = (WalkingParameters) routingRequest.getSearchParameters().getProfileParameters();
        Assert.assertEquals(2, params.getMaximumTrailDifficulty());
        Assert.assertEquals(6, params.getMaximumGradient());
    }

    @Test
    public void TestWheelchairParameters() throws Exception {
        request.setProfile(APIRoutingEnums.RoutingProfile.WHEELCHAIR);
        request.getRouteOptions().getProfileParams().setRestrictions(wheelchairParams);

        RoutingRequest routingRequest;

        routingRequest = RouteRequestHandler.convertRouteRequest(request);

        WheelchairParameters params = (WheelchairParameters) routingRequest.getSearchParameters().getProfileParameters();
        Assert.assertEquals(WheelchairTypesEncoder.getSmoothnessType("good"), params.getSmoothnessType());
        Assert.assertEquals(3.0f, params.getMaximumIncline(), 0);
        Assert.assertEquals(1.0f, params.getMaximumSlopedKerb(), 0);
        Assert.assertEquals(200.0f, params.getMinimumWidth(), 0);
        Assert.assertEquals(WheelchairTypesEncoder.getSurfaceType("asphalt"), params.getSurfaceType());
    }

    @Test
    public void skippedBearingTest() throws Exception {
        request.setBearings(new Double[][] {{120.0, 90.0}, { , }, {90.0, 30.0}});
        RoutingRequest routingRequest;

        routingRequest = RouteRequestHandler.convertRouteRequest(request);

        Assert.assertEquals(3, routingRequest.getSearchParameters().getBearings().length);
    }

    @Test(expected = ParameterValueException.class)
    public void invalidBearingLength() throws Exception {
        request.setBearings(new Double[][] {{123.0,123.0}});
        RouteRequestHandler.convertRouteRequest(request);
    }

    @Test(expected = ParameterValueException.class)
    public void invalidRadiusLength() throws Exception {
        request.setMaximumSearchRadii(new Double[] {10.0});
        RouteRequestHandler.convertRouteRequest(request);
    }

    @Test(expected = ParameterValueException.class)
    public void onlySetOptimizationToFalse() throws Exception {
        request.setUseContractionHierarchies(true);
        RouteRequestHandler.convertRouteRequest(request);
    }

    private void checkPolygon(Polygon[] requestPolys, JSONObject apiPolys) {
        Assert.assertEquals(1, requestPolys.length);

        JSONArray jsonCoords = (JSONArray)((JSONArray)apiPolys.get("coordinates")).get(0);
        for(int i=0; i<jsonCoords.size(); i++) {
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
