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

package org.heigit.ors.api.services;

import org.heigit.ors.api.APIEnums;
import org.heigit.ors.api.EndpointsProperties;
import org.heigit.ors.api.requests.routing.*;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("unittest")
class RoutingServiceTest {

    @Autowired
    RoutingService routingService;
    @Autowired
    EndpointsProperties endpointsProperties = new EndpointsProperties();
    RouteRequest request;
    private RequestProfileParamsRestrictions vehicleParams;
    private RequestProfileParamsRestrictions wheelchairParams;

    private final JSONObject geoJsonPolygon;

    public RoutingServiceTest() throws Exception {
        init();
        geoJsonPolygon = constructGeoJson();
    }

    private JSONObject constructGeoJson() {
        JSONObject geoJsonPolygon = new JSONObject();
        geoJsonPolygon.put("type", "Polygon");
        JSONArray coordsArray = new JSONArray();
        coordsArray.add(new Double[]{49.0, 8.0});
        coordsArray.add(new Double[]{49.005, 8.01});
        coordsArray.add(new Double[]{49.01, 8.0});
        coordsArray.add(new Double[]{49.0, 8.0});
        JSONArray coordinates = new JSONArray();

        coordinates.add(coordsArray);
        geoJsonPolygon.put("coordinates", coordinates);

        return geoJsonPolygon;
    }

    @BeforeEach
    void init() throws Exception {

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
        coords[0] = new Double[]{24.5, 39.2};
        coords[1] = new Double[]{27.4, 38.6};
        coords[2] = new Double[]{26.5, 37.2};

        request = new RouteRequest(coords);

        request.setProfile(APIEnums.Profile.DRIVING_CAR);
        request.setAttributes(new APIEnums.Attributes[]{APIEnums.Attributes.AVERAGE_SPEED, APIEnums.Attributes.DETOUR_FACTOR});
        request.setContinueStraightAtWaypoints(true);
        request.setExtraInfo(new APIEnums.ExtraInfo[]{APIEnums.ExtraInfo.OSM_ID});
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
        options.setAvoidCountries(new String[]{"115"});
        options.setAvoidFeatures(new APIEnums.AvoidFeatures[]{APIEnums.AvoidFeatures.FORDS});

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
    void convertRouteRequestTest() throws Exception {
        RoutingRequest routingRequest;

        routingRequest = routingService.convertRouteRequest(request);

        assertEquals(3, routingRequest.getCoordinates().length);

        assertEquals(RoutingProfileType.getFromString("driving-car"), routingRequest.getSearchParameters().getProfileType());
        assertArrayEquals(new String[]{"avgspeed", "detourfactor"}, routingRequest.getAttributes());

        assertTrue(routingRequest.getContinueStraight());

        assertEquals(RouteExtraInfoFlag.getFromString("osmid"), routingRequest.getExtraInfo());

        assertEquals("geojson", routingRequest.getGeometryFormat());
        assertTrue(routingRequest.getIncludeGeometry());
        assertTrue(routingRequest.getIncludeInstructions());
        assertTrue(routingRequest.getIncludeRoundaboutExits());
        assertTrue(routingRequest.getIncludeManeuvers());
        assertEquals(RouteInstructionsFormat.HTML, routingRequest.getInstructionsFormat());
        assertEquals("de", routingRequest.getLanguage());
        assertEquals("geojson", routingRequest.getGeometryFormat());
        assertTrue(routingRequest.getIncludeElevation());
        assertEquals(WeightingMethod.FASTEST, routingRequest.getSearchParameters().getWeightingMethod());
        assertEquals(DistanceUnit.METERS, routingRequest.getUnits());
        assertTrue(routingRequest.getSearchParameters().hasFlexibleMode());

        assertEquals(BordersExtractor.Avoid.CONTROLLED, routingRequest.getSearchParameters().getAvoidBorders());
        assertArrayEquals(new int[]{115}, routingRequest.getSearchParameters().getAvoidCountries());
        assertEquals(AvoidFeatureFlags.getFromString("fords"), routingRequest.getSearchParameters().getAvoidFeatureTypes());
        assertTrue(AvoidFeatureFlags.isValid(18,32));

        checkPolygon(routingRequest.getSearchParameters().getAvoidAreas(), geoJsonPolygon);

        ProfileWeightingCollection weightings = routingRequest.getSearchParameters().getProfileParameters().getWeightings();
        ProfileWeighting weighting;
        Iterator<ProfileWeighting> iter = weightings.getIterator();
        while (iter.hasNext() && (weighting = iter.next()) != null) {
            if (weighting.getName().equals("green")) {
                assertEquals(0.5, weighting.getParameters().getDouble("factor", -1), 0.0001);
            }
            if (weighting.getName().equals("quiet")) {
                assertEquals(0.2, weighting.getParameters().getDouble("factor", -1), 0.0001);
            }
            if (weighting.getName().equals("steepness_difficulty")) {
                assertEquals(3, weighting.getParameters().getInt("level", -1), 0.0001);
            }
        }
    }

    @Test
    void TestVehicleParameters() throws Exception {
        request.setProfile(APIEnums.Profile.DRIVING_HGV);
        request.getRouteOptions().getProfileParams().setRestrictions(vehicleParams);
        request.getRouteOptions().setVehicleType(APIEnums.VehicleType.AGRICULTURAL);

        RoutingRequest routingRequest = routingService.convertRouteRequest(request);

        VehicleParameters params = (VehicleParameters) routingRequest.getSearchParameters().getProfileParameters();
        assertEquals(30.0, params.getWeight(), 0);
        assertEquals(10.0, params.getAxleload(), 0);
        assertEquals(5.0, params.getHeight(), 0);
        assertEquals(15.0, params.getLength(), 0);
        assertEquals(4.5, params.getWidth(), 0);
        assertEquals(new VehicleLoadCharacteristicsFlags().getFromString("hazmat"), params.getLoadCharacteristics());
    }

    @Test
    void TestWheelchairParameters() throws Exception {
        request.setProfile(APIEnums.Profile.WHEELCHAIR);
        request.getRouteOptions().getProfileParams().setRestrictions(wheelchairParams);

        RoutingRequest routingRequest = routingService.convertRouteRequest(request);

        WheelchairParameters params = (WheelchairParameters) routingRequest.getSearchParameters().getProfileParameters();
        assertEquals(WheelchairTypesEncoder.getSmoothnessType(APIEnums.SmoothnessTypes.SMOOTHNESS_GOOD.toString()), params.getSmoothnessType());
        assertEquals(3.0f, params.getMaximumIncline(), 0);
        assertEquals(1.0f, params.getMaximumSlopedKerb(), 0);
        assertEquals(2.0f, params.getMinimumWidth(), 0);
        assertEquals(WheelchairTypesEncoder.getSurfaceType("asphalt"), params.getSurfaceType());
        assertTrue(params.isRequireSurfaceQualityKnown());
        assertTrue(params.allowUnsuitable());
    }

    @Test
    void TestCargoBikeParameters() throws Exception {
        request.setProfile(APIEnums.Profile.CYCLING_CARGO);

        request.getRouteOptions().getProfileParams().setRestrictions(vehicleParams);

        RoutingRequest routingRequest = routingService.convertRouteRequest(request);

        WheelchairParameters params = (WheelchairParameters) routingRequest.getSearchParameters().getProfileParameters();
        assertEquals(WheelchairTypesEncoder.getSmoothnessType(APIEnums.SmoothnessTypes.SMOOTHNESS_GOOD), params.getSmoothnessType());
        assertEquals(3.0f, params.getMaximumIncline(), 0);
        assertEquals(1.0f, params.getMaximumSlopedKerb(), 0);
        assertEquals(2.0f, params.getMinimumWidth(), 0);
        assertEquals(WheelchairTypesEncoder.getSurfaceType("asphalt"), params.getSurfaceType());
        assertTrue(params.isRequireSurfaceQualityKnown());
        assertTrue(params.allowUnsuitable());
    }

    @Test
    void testBearings() throws StatusCodeException {
        request.setBearings(new Double[][]{{10.0, 10.0}, {260.0, 90.0}, {45.0, 30.0}});

        RoutingRequest routingRequest = routingService.convertRouteRequest(request);

        WayPointBearing[] bearings = routingRequest.getSearchParameters().getBearings();
        assertEquals(10.0, bearings[0].getValue(), 0);
        assertEquals(260.0, bearings[1].getValue(), 0);
        assertEquals(45.0, bearings[2].getValue(), 0);
    }

    @Test
    void skippedBearingTest() throws Exception {
        request.setBearings(new Double[][]{{120.0, 90.0}, {,}, {90.0, 30.0}});

        RoutingRequest routingRequest = routingService.convertRouteRequest(request);

        assertEquals(3, routingRequest.getSearchParameters().getBearings().length);
    }

    @Test
    void invalidBearingLength() throws Exception {
        assertThrows(ParameterValueException.class, () -> {
            request.setBearings(new Double[][]{{123.0, 123.0}});
            routingService.convertRouteRequest(request);
        });
    }

    @Test
    void testRadius() throws StatusCodeException {
        request.setMaximumSearchRadii(new Double[]{50.0, 20.0, 100.0});

        RoutingRequest routingRequest = routingService.convertRouteRequest(request);
        assertArrayEquals(new double[]{50.0, 20.0, 100.0}, routingRequest.getSearchParameters().getMaximumRadiuses());
    }

    @Test
    void invalidRadiusLength() throws Exception {
        assertThrows(ParameterValueException.class, () -> {
            request.setMaximumSearchRadii(new Double[]{10.0, 20.0});
            routingService.convertRouteRequest(request);
        });
    }

    @Test
    void testSingleRadius() throws Exception {
        request.setMaximumSearchRadii(new Double[]{50d});

        RoutingRequest routingRequest = routingService.convertRouteRequest(request);
        assertArrayEquals(new double[]{50.0, 50.0, 50.0}, routingRequest.getSearchParameters().getMaximumRadiuses());
    }

    @Test
    void onlySetOptimizationToFalse() throws Exception {
        assertThrows(ParameterValueException.class, () -> {
            request.setUseContractionHierarchies(true);
            routingService.convertRouteRequest(request);
        });
    }

    @Test
    void vehicleType() throws Exception {
        RouteRequestOptions opts = request.getRouteOptions();
        opts.setVehicleType(APIEnums.VehicleType.AGRICULTURAL);

        for (APIEnums.Profile profile : APIEnums.Profile.values()) {
            request.setProfile(profile);
            request.setRouteOptions(opts);
            if (profile != APIEnums.Profile.DRIVING_HGV) {
                try {
                    routingService.convertRouteRequest(request);
                } catch (Exception e) {
                    assertTrue(e instanceof IncompatibleParameterException);
                }
            } else {
                routingService.convertRouteRequest(request);
            }
        }
    }

    @Test
    void testSkippedSegments() throws StatusCodeException {

        List<Integer> skipSegments = new ArrayList<>();
        skipSegments.add(0, 1);
        skipSegments.add(1, 2);
        request.setSkipSegments(skipSegments);

        RoutingRequest routingRequest = routingService.convertRouteRequest(request);

        assertEquals(2, routingRequest.getSkipSegments().size());
        assertEquals(Integer.valueOf(1), routingRequest.getSkipSegments().get(0));
        assertEquals(Integer.valueOf(2), routingRequest.getSkipSegments().get(1));

    }

    @Test
    void invalidSkipSegmentsLength() throws StatusCodeException {
        assertThrows(ParameterValueException.class, () -> {
            List<Integer> skip_segments = new ArrayList<>();
            skip_segments.add(0, 1);
            skip_segments.add(0, 2);
            skip_segments.add(0, 2);
            request.setSkipSegments(skip_segments);
            routingService.convertRouteRequest(request);
        });
    }

    @Test
    void emptySkipSegments() throws StatusCodeException {
        assertThrows(EmptyElementException.class, () -> {
            List<Integer> skip_segments = new ArrayList<>();
            request.setSkipSegments(skip_segments);
            routingService.convertRouteRequest(request);
        });
    }

    @Test
    void skipSegmentsValueTooBig() throws StatusCodeException {
        assertThrows(ParameterOutOfRangeException.class, () -> {
            List<Integer> skip_segments = new ArrayList<>();
            skip_segments.add(0, 99);
            request.setSkipSegments(skip_segments);
            routingService.convertRouteRequest(request);
        });
    }

    @Test
    void skipSegmentsValueTooSmall() throws StatusCodeException {
        assertThrows(ParameterValueException.class, () -> {
            List<Integer> skip_segments = new ArrayList<>();
            skip_segments.add(0, -99);
            request.setSkipSegments(skip_segments);
            routingService.convertRouteRequest(request);
        });
    }

    @Test
    void convertRouteRequestTestForAlternativeRoutes() throws Exception {
        Double[][] coords = new Double[2][2];
        coords[0] = new Double[]{24.5, 39.2};
        coords[1] = new Double[]{26.5, 37.2};
        RouteRequest arRequest = new RouteRequest(coords);
        arRequest.setProfile(APIEnums.Profile.DRIVING_CAR);

        RouteRequestAlternativeRoutes ar = new RouteRequestAlternativeRoutes();
        ar.setTargetCount(3);
        ar.setShareFactor(0.9);
        ar.setWeightFactor(1.8);
        arRequest.setAlternativeRoutes(ar);

        RoutingRequest routingRequest = routingService.convertRouteRequest(arRequest);
        assertEquals(3, routingRequest.getSearchParameters().getAlternativeRoutesCount());
        assertEquals(0.9, routingRequest.getSearchParameters().getAlternativeRoutesShareFactor(), 0);
        assertEquals(1.8, routingRequest.getSearchParameters().getAlternativeRoutesWeightFactor(), 0);
    }

    @Test
    void testRoundTripNeedsLength() throws StatusCodeException {
        assertThrows(MissingParameterException.class, () -> {
            List<List<Double>> coordinates = new ArrayList<>();
            coordinates.add(new ArrayList<>(Arrays.asList(12.1234, 34.3456)));
            request.setCoordinates(coordinates);

            RouteRequestRoundTripOptions rtOptions = new RouteRequestRoundTripOptions();
            rtOptions.setPoints(4);
            RouteRequestOptions options = new RouteRequestOptions();
            options.setRoundTripOptions(rtOptions);
            request.setRouteOptions(options);

            routingService.convertRouteRequest(request);
        });
    }

    @Test
    void testSingleCoordinateNotValidForNonRoundTrip() throws StatusCodeException {
        assertThrows(ParameterValueException.class, () -> {
            List<List<Double>> coordinates = new ArrayList<>();
            coordinates.add(new ArrayList<>(Arrays.asList(12.1234, 34.3456)));
            request.setCoordinates(coordinates);

            routingService.convertRouteRequest(request);
        });
    }

    @Test
    void testSingleCoordinateValidForRoundTrip() throws StatusCodeException {
        List<List<Double>> coordinates = new ArrayList<>();
        coordinates.add(new ArrayList<>(Arrays.asList(12.1234, 34.3456)));
        request.setCoordinates(coordinates);

        RouteRequestRoundTripOptions rtOptions = new RouteRequestRoundTripOptions();
        rtOptions.setLength(400f);
        RouteRequestOptions options = new RouteRequestOptions();
        options.setRoundTripOptions(rtOptions);
        request.setRouteOptions(options);

        RoutingRequest generatedRoutingRequest = routingService.convertRouteRequest(request);
        assertEquals(1, generatedRoutingRequest.getCoordinates().length);
    }

    private void checkPolygon(Polygon[] requestPolys, JSONObject apiPolys) {
        assertEquals(1, requestPolys.length);

        JSONArray jsonCoords = (JSONArray) ((JSONArray) apiPolys.get("coordinates")).get(0);
        for (int i = 0; i < jsonCoords.size(); i++) {
            Double[] coordPair = (Double[]) jsonCoords.get(i);
            Coordinate c = new Coordinate(coordPair[0], coordPair[1]);

            compareCoordinates(c, requestPolys[0].getCoordinates()[i]);
        }
    }

    private void compareCoordinates(Coordinate c1, Coordinate c2) {
        assertEquals(c1.x, c2.x, 0);
        assertEquals(c1.y, c2.y, 0);
    }
}
