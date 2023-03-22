package org.heigit.ors.api.requests.isochrones;

import org.heigit.ors.api.requests.common.APIEnums;
import org.heigit.ors.api.requests.routing.RequestProfileParams;
import org.heigit.ors.api.requests.routing.RequestProfileParamsRestrictions;
import org.heigit.ors.api.requests.routing.RequestProfileParamsWeightings;
import org.heigit.ors.api.requests.routing.RouteRequestOptions;
import org.heigit.ors.common.TravelRangeType;
import org.heigit.ors.common.TravellerInfo;
import org.heigit.ors.exceptions.ParameterOutOfRangeException;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.isochrones.IsochroneRequest;
import org.heigit.ors.routing.*;
import org.heigit.ors.routing.pathprocessors.BordersExtractor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IsochronesRequestHandlerTest {
    IsochronesRequest request;

    private RequestProfileParamsRestrictions vehicleParams;
    private RequestProfileParamsRestrictions cyclingParams;
    private RequestProfileParamsRestrictions walkingParams;
    private RequestProfileParamsRestrictions wheelchairParams;
    private JSONObject geoJsonPolygon;


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

    @BeforeEach
    void init() throws Exception {

        geoJsonPolygon = constructGeoJson();

        Double[][] coords = new Double[2][2];
        coords[0] = new Double[]{24.5, 39.2};
        coords[1] = new Double[]{27.4, 38.6};

        request = new IsochronesRequest();
        request.setLocations(coords);

        request.setProfile(APIEnums.Profile.DRIVING_CAR);
        request.setAttributes(new IsochronesRequestEnums.Attributes[]{IsochronesRequestEnums.Attributes.AREA, IsochronesRequestEnums.Attributes.REACH_FACTOR});
        request.setResponseType(APIEnums.RouteResponseType.GEOJSON);

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

        options.setProfileParams(params);
        request.setIsochronesOptions(options);
    }

    @Test
    void convertSmoothing() throws ParameterValueException {
        Float smoothing = request.convertSmoothing(10.234);
        assertEquals(10.234, smoothing, 0.01);
    }

    @Test
    void convertSmoothingFailWhenTooHigh() throws ParameterValueException {
        assertThrows(ParameterValueException.class, () -> {
            request.convertSmoothing(105.0);
        });
    }

    @Test
    void convertSmoothingFailWhenTooLow() throws ParameterValueException {
        assertThrows(ParameterValueException.class, () -> {
            request.convertSmoothing(-5.0);
        });
    }

    @Test
    void convertLocationType() throws ParameterValueException {
        String locationType = request.convertLocationType(IsochronesRequestEnums.LocationType.DESTINATION);
        assertEquals("destination", locationType);
        locationType = request.convertLocationType(IsochronesRequestEnums.LocationType.START);
        assertEquals("start", locationType);
    }

    @Test
    void convertRangeType() throws ParameterValueException {
        TravelRangeType rangeType = request.convertRangeType(IsochronesRequestEnums.RangeType.DISTANCE);
        assertEquals(TravelRangeType.DISTANCE, rangeType);
        rangeType = request.convertRangeType(IsochronesRequestEnums.RangeType.TIME);
        assertEquals(TravelRangeType.TIME, rangeType);
    }

    @Test
    void convertAreaUnit() throws ParameterValueException {
        String unit = request.convertAreaUnit(APIEnums.Units.KILOMETRES);
        assertEquals("km", unit);
        unit = request.convertAreaUnit(APIEnums.Units.METRES);
        assertEquals("m", unit);
        unit = request.convertAreaUnit(APIEnums.Units.MILES);
        assertEquals("mi", unit);
    }

    @Test
    void convertRangeUnit() throws ParameterValueException {
        String unit = request.convertRangeUnit(APIEnums.Units.KILOMETRES);
        assertEquals("km", unit);
        unit = request.convertRangeUnit(APIEnums.Units.METRES);
        assertEquals("m", unit);
        unit = request.convertRangeUnit(APIEnums.Units.MILES);
        assertEquals("mi", unit);
    }

    @Test
    void convertSingleCoordinate() throws ParameterValueException {
        Coordinate coord = request.convertSingleCoordinate(new Double[]{123.4, 321.0});
        assertEquals(123.4, coord.x, 0.0001);
        assertEquals(321.0, coord.y, 0.0001);
    }

    @Test
    void convertSingleCoordinateInvalidLengthShort() throws ParameterValueException {
        assertThrows(ParameterValueException.class, () -> {
            request.convertSingleCoordinate(new Double[]{123.4});
        });
    }

    @Test
    void convertSingleCoordinateInvalidLengthLong() throws ParameterValueException {
        assertThrows(ParameterValueException.class, () -> {
            request.convertSingleCoordinate(new Double[]{123.4, 123.4, 123.4});
        });
    }

    @Test
    void setRangeAndIntervals() throws ParameterValueException, ParameterOutOfRangeException {
        TravellerInfo info = new TravellerInfo();
        List<Double> rangeValues = new ArrayList<>();
        rangeValues.add(20.0);
        double intervalValue = 10;

        request.setRangeAndIntervals(info, rangeValues, intervalValue);

        assertEquals(10.0, info.getRanges()[0], 0.0f);
        assertEquals(20.0, info.getRanges()[1], 0.0f);

        info = new TravellerInfo();
        rangeValues = new ArrayList<>();
        rangeValues.add(15.0);
        rangeValues.add(30.0);
        request.setRangeAndIntervals(info, rangeValues, intervalValue);
        assertEquals(15.0, info.getRanges()[0], 0.0f);
        assertEquals(30.0, info.getRanges()[1], 0.0f);

    }

    @Test
    void convertAttributes() {
        IsochronesRequestEnums.Attributes[] atts = new IsochronesRequestEnums.Attributes[]{IsochronesRequestEnums.Attributes.AREA, IsochronesRequestEnums.Attributes.REACH_FACTOR, IsochronesRequestEnums.Attributes.TOTAL_POPULATION};
        String[] attStr = request.convertAttributes(atts);
        assertEquals("area", attStr[0]);
        assertEquals("reachfactor", attStr[1]);
        assertEquals("total_pop", attStr[2]);
    }

    @Test
    void convertCalcMethod() throws ParameterValueException {
        String calcMethod = request.convertCalcMethod(IsochronesRequestEnums.CalculationMethod.CONCAVE_BALLS);
        assertEquals("concaveballs", calcMethod);
        calcMethod = request.convertCalcMethod(IsochronesRequestEnums.CalculationMethod.GRID);
        assertEquals("grid", calcMethod);
    }

    @Test
    void convertIsochroneRequest() throws Exception {
        IsochronesRequest request = new IsochronesRequest();
        Double[][] locations = {{9.676034, 50.409675}, {9.676034, 50.409675}};
        Coordinate coord0 = new Coordinate();
        coord0.x = 9.676034;
        coord0.y = 50.409675;

        request.setLocations(locations);
        request.setProfile(APIEnums.Profile.DRIVING_CAR);
        List<Double> range = new ArrayList<>();
        range.add(300.0);
        range.add(600.0);
        request.setRange(range);
        IsochroneRequest isochroneRequest = request.convertIsochroneRequest();
        assertNotNull(isochroneRequest);
        assertFalse(isochroneRequest.getIncludeIntersections());
        assertNull(request.getAttributes());
        assertFalse(request.hasSmoothing());
        assertNull(request.getSmoothing());
        assertNull(request.getId());
        assertEquals(coord0.x, isochroneRequest.getLocations()[0].x, 0);
        assertEquals(coord0.y, isochroneRequest.getLocations()[0].y, 0);
        assertEquals(coord0.x, isochroneRequest.getLocations()[1].x, 0);
        assertEquals(coord0.y, isochroneRequest.getLocations()[1].y, 0);
        assertEquals(2, isochroneRequest.getTravellers().size());
        for (int i = 0; i < isochroneRequest.getTravellers().size(); i++) {
            TravellerInfo travellerInfo = isochroneRequest.getTravellers().get(i);
            assertEquals(String.valueOf(i), travellerInfo.getId());
            assertEquals(coord0, travellerInfo.getLocation());
            assertEquals(IsochronesRequestEnums.LocationType.START.toString(), travellerInfo.getLocationType());
            assertNotNull(travellerInfo.getRanges());
            assertEquals(TravelRangeType.TIME, travellerInfo.getRangeType());
            assertNotNull(travellerInfo.getRouteSearchParameters());
        }

    }

    @Test
    void constructTravellerInfo() throws Exception {
        Double[][] coordinates = {{1.0, 3.0}, {1.0, 3.0}};
        Double[] coordinate = {1.0, 3.0};
        Coordinate realCoordinate = new Coordinate();
        realCoordinate.x = 1.0;
        realCoordinate.y = 3.0;
        IsochronesRequest request = new IsochronesRequest();
        request.setProfile(APIEnums.Profile.DRIVING_CAR);
        request.setLocations(coordinates);
        List<Double> range = new ArrayList<>();
        range.add(300.0);
        range.add(600.0);
        request.setRange(range);
        TravellerInfo travellerInfo = request.constructTravellerInfo(coordinate);
        assertEquals(String.valueOf(0), travellerInfo.getId());
        assertEquals(realCoordinate, travellerInfo.getLocation());
        assertEquals("start", travellerInfo.getLocationType());
        assertEquals(range.toString(), Arrays.toString(travellerInfo.getRanges()));
        assertEquals(TravelRangeType.TIME, travellerInfo.getRangeType());
    }

    @Test
    void constructRouteSearchParametersTest() throws Exception {
        Double[][] coordinates = {{1.0, 3.0}, {1.0, 3.0}};
        IsochronesRequest request = new IsochronesRequest();
        request.setProfile(APIEnums.Profile.DRIVING_CAR);
        request.setLocations(coordinates);
        RouteSearchParameters routeSearchParameters = request.constructRouteSearchParameters();
        assertEquals(RoutingProfileType.DRIVING_CAR, routeSearchParameters.getProfileType());
        assertEquals(WeightingMethod.RECOMMENDED, routeSearchParameters.getWeightingMethod());
        assertFalse(routeSearchParameters.getConsiderTurnRestrictions());
        assertNull(routeSearchParameters.getAvoidAreas());
        assertEquals(0, routeSearchParameters.getAvoidFeatureTypes());
        assertEquals(0, routeSearchParameters.getVehicleType());
        assertFalse(routeSearchParameters.hasFlexibleMode());
        assertEquals(BordersExtractor.Avoid.NONE, routeSearchParameters.getAvoidBorders());
        assertNull(routeSearchParameters.getProfileParameters());
        assertNull(routeSearchParameters.getBearings());
        assertNull(routeSearchParameters.getMaximumRadiuses());
        assertNull(routeSearchParameters.getAvoidCountries());
        assertNull(routeSearchParameters.getOptions());
    }

    @Test
    void processIsochronesRequestOptionsTest() throws Exception {
        RouteSearchParameters routeSearchParameters = request.constructRouteSearchParameters();

        assertEquals(RoutingProfileType.DRIVING_CAR, routeSearchParameters.getProfileType());
        assertEquals(WeightingMethod.RECOMMENDED, routeSearchParameters.getWeightingMethod());
        assertFalse(routeSearchParameters.getConsiderTurnRestrictions());
        checkPolygon(routeSearchParameters.getAvoidAreas(), geoJsonPolygon);
        assertEquals(16, routeSearchParameters.getAvoidFeatureTypes());
        assertEquals(0, routeSearchParameters.getVehicleType());
        assertFalse(routeSearchParameters.hasFlexibleMode());
        assertEquals(BordersExtractor.Avoid.CONTROLLED, routeSearchParameters.getAvoidBorders());
        assertNull(routeSearchParameters.getBearings());
        assertNull(routeSearchParameters.getMaximumRadiuses());
        assertNull(routeSearchParameters.getOptions());
        assertEquals(115, routeSearchParameters.getAvoidCountries()[0]);

        ProfileWeightingCollection weightings = routeSearchParameters.getProfileParameters().getWeightings();
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
    void getIsoMapsTest() {
        assertNull(request.getIsoMaps());
    }

    @Test
    void getIsochroneRequestTest() {
        assertNull(request.getIsochroneRequest());
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
