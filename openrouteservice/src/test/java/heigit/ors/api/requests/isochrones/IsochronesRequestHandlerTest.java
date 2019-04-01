package heigit.ors.api.requests.isochrones;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.api.requests.routing.RequestProfileParams;
import heigit.ors.api.requests.routing.RequestProfileParamsRestrictions;
import heigit.ors.api.requests.routing.RequestProfileParamsWeightings;
import heigit.ors.api.requests.routing.RouteRequestOptions;
import heigit.ors.common.DistanceUnit;
import heigit.ors.common.TravelRangeType;
import heigit.ors.common.TravellerInfo;
import heigit.ors.exceptions.ParameterOutOfRangeException;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.isochrones.IsochroneRequest;
import heigit.ors.routing.*;
import heigit.ors.routing.pathprocessors.BordersExtractor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class IsochronesRequestHandlerTest {
    IsochronesRequest request;

    IsochronesRequestHandler handler;
    private RequestProfileParamsRestrictions vehicleParams;
    private RequestProfileParamsRestrictions cyclingParams;
    private RequestProfileParamsRestrictions walkingParams;
    private RequestProfileParamsRestrictions wheelchairParams;
    private JSONObject geoJsonPolygon;


    private JSONObject constructGeoJson() {
        JSONObject geoJsonPolygon = new JSONObject();
        geoJsonPolygon.put("type", "Polygon");
        JSONArray coordsArray = new JSONArray();
        coordsArray.add(new Double[]{123.0, 100.0});
        coordsArray.add(new Double[]{150.0, 138.0});
        coordsArray.add(new Double[]{140.0, 115.0});
        coordsArray.add(new Double[]{123.0, 100.0});
        JSONArray coordinates = new JSONArray();

        coordinates.add(coordsArray);
        geoJsonPolygon.put("coordinates", coordinates);

        return geoJsonPolygon;
    }

    @Before
    public void init() throws Exception {
        handler = new IsochronesRequestHandler();
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
        wheelchairParams.setSmoothnessType("good");
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
    public void convertSmoothing() throws ParameterValueException {
        Float smoothing = handler.convertSmoothing(10.234);
        Assert.assertEquals(10.234, smoothing, 0.01);
    }

    @Test(expected = ParameterValueException.class)
    public void convertSmoothingFailWhenTooHigh() throws ParameterValueException {
        handler.convertSmoothing(105.0);
    }

    @Test(expected = ParameterValueException.class)
    public void convertSmoothingFailWhenTooLow() throws ParameterValueException {
        handler.convertSmoothing(-5.0);
    }

    @Test
    public void convertLocationType() throws ParameterValueException {
        String locationType = handler.convertLocationType(IsochronesRequestEnums.LocationType.DESTINATION);
        Assert.assertEquals("destination", locationType);
        locationType = handler.convertLocationType(IsochronesRequestEnums.LocationType.START);
        Assert.assertEquals("start", locationType);
    }

    @Test
    public void convertRangeType() throws ParameterValueException {
        TravelRangeType rangeType = handler.convertRangeType(IsochronesRequestEnums.RangeType.DISTANCE);
        Assert.assertEquals(TravelRangeType.Distance, rangeType);
        rangeType = handler.convertRangeType(IsochronesRequestEnums.RangeType.TIME);
        Assert.assertEquals(TravelRangeType.Time, rangeType);
    }

    @Test
    public void convertAreaUnit() throws ParameterValueException {
        String unit = handler.convertAreaUnit(APIEnums.Units.KILOMETRES);
        Assert.assertEquals("km", unit);
        unit = handler.convertAreaUnit(APIEnums.Units.METRES);
        Assert.assertEquals("m", unit);
        unit = handler.convertAreaUnit(APIEnums.Units.MILES);
        Assert.assertEquals("mi", unit);
    }

    @Test
    public void convertRangeUnit() throws ParameterValueException {
        String unit = handler.convertRangeUnit(APIEnums.Units.KILOMETRES);
        Assert.assertEquals("km", unit);
        unit = handler.convertRangeUnit(APIEnums.Units.METRES);
        Assert.assertEquals("m", unit);
        unit = handler.convertRangeUnit(APIEnums.Units.MILES);
        Assert.assertEquals("mi", unit);
    }

    @Test
    public void convertSingleCoordinate() throws ParameterValueException {
        Coordinate coord = handler.convertSingleCoordinate(new Double[]{123.4, 321.0});
        Assert.assertEquals(123.4, coord.x, 0.0001);
        Assert.assertEquals(321.0, coord.y, 0.0001);
    }

    @Test(expected = ParameterValueException.class)
    public void convertSingleCoordinateInvalidLengthShort() throws ParameterValueException {
        handler.convertSingleCoordinate(new Double[]{123.4});
    }

    @Test(expected = ParameterValueException.class)
    public void convertSingleCoordinateInvalidLengthLong() throws ParameterValueException {
        handler.convertSingleCoordinate(new Double[]{123.4, 123.4, 123.4});
    }

    @Test
    public void setRangeAndIntervals() throws ParameterValueException {
        TravellerInfo info = new TravellerInfo();
        List<Double> rangeValues = new ArrayList<>();
        rangeValues.add(20.0);
        double intervalValue = 10;

        handler.setRangeAndIntervals(info, rangeValues, intervalValue);

        Assert.assertEquals(10.0, info.getRanges()[0], 0.0f);
        Assert.assertEquals(20.0, info.getRanges()[1], 0.0f);

        info = new TravellerInfo();
        rangeValues = new ArrayList<>();
        rangeValues.add(15.0);
        rangeValues.add(30.0);
        handler.setRangeAndIntervals(info, rangeValues, intervalValue);
        Assert.assertEquals(15.0, info.getRanges()[0], 0.0f);
        Assert.assertEquals(30.0, info.getRanges()[1], 0.0f);

    }

    @Test
    public void convertAttributes() {
        IsochronesRequestEnums.Attributes[] atts = new IsochronesRequestEnums.Attributes[]{IsochronesRequestEnums.Attributes.AREA, IsochronesRequestEnums.Attributes.REACH_FACTOR, IsochronesRequestEnums.Attributes.TOTAL_POPULATION};
        String[] attStr = handler.convertAttributes(atts);
        Assert.assertEquals("area", attStr[0]);
        Assert.assertEquals("reachfactor", attStr[1]);
        Assert.assertEquals("total_pop", attStr[2]);
    }

    @Test
    public void convertCalcMethod() throws ParameterValueException {
        String calcMethod = handler.convertCalcMethod(IsochronesRequestEnums.CalculationMethod.CONCAVE_BALLS);
        Assert.assertEquals("concaveballs", calcMethod);
        calcMethod = handler.convertCalcMethod(IsochronesRequestEnums.CalculationMethod.GRID);
        Assert.assertEquals("grid", calcMethod);
    }

    @Test
    public void convertIsochroneRequest() throws Exception {
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
        IsochroneRequest isochroneRequest = handler.convertIsochroneRequest(request);
        Assert.assertNotNull(isochroneRequest);
        Assert.assertFalse(isochroneRequest.getIncludeIntersections());
        Assert.assertNull(request.getAttributes());
        Assert.assertFalse(request.hasSmoothing());
        Assert.assertNull(request.getSmoothing());
        Assert.assertNull(request.getId());
        Assert.assertEquals(coord0.x, isochroneRequest.getLocations()[0].x, 0);
        Assert.assertEquals(coord0.y, isochroneRequest.getLocations()[0].y, 0);
        Assert.assertEquals(coord0.x, isochroneRequest.getLocations()[1].x, 0);
        Assert.assertEquals(coord0.y, isochroneRequest.getLocations()[1].y, 0);
        Assert.assertEquals(2, isochroneRequest.getTravellers().size());
        for (int i = 0; i < isochroneRequest.getTravellers().size(); i++) {
            TravellerInfo travellerInfo = isochroneRequest.getTravellers().get(i);
            Assert.assertEquals(String.valueOf(i), travellerInfo.getId());
            Assert.assertEquals(coord0, travellerInfo.getLocation());
            Assert.assertEquals(IsochronesRequestEnums.LocationType.START.toString(), travellerInfo.getLocationType());
            Assert.assertNotNull(travellerInfo.getRanges());
            Assert.assertEquals(TravelRangeType.Time, travellerInfo.getRangeType());
            Assert.assertNotNull(travellerInfo.getRouteSearchParameters());
        }

    }

    @Test
    public void constructTravellerInfo() throws Exception {
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
        IsochronesRequestHandler isochronesRequestHandler = new IsochronesRequestHandler();
        TravellerInfo travellerInfo = isochronesRequestHandler.constructTravellerInfo(coordinate, request);
        Assert.assertEquals(String.valueOf(0), travellerInfo.getId());
        Assert.assertEquals(realCoordinate, travellerInfo.getLocation());
        Assert.assertEquals("start", travellerInfo.getLocationType());
        Assert.assertEquals(range.toString(), Arrays.toString(travellerInfo.getRanges()));
        Assert.assertEquals(TravelRangeType.Time, travellerInfo.getRangeType());
    }

    @Test
    public void constructRouteSearchParametersTest() throws Exception {
        Double[][] coordinates = {{1.0, 3.0}, {1.0, 3.0}};
        IsochronesRequestHandler isochronesRequestHandler = new IsochronesRequestHandler();
        IsochronesRequest request = new IsochronesRequest();
        request.setProfile(APIEnums.Profile.DRIVING_CAR);
        request.setLocations(coordinates);
        RouteSearchParameters routeSearchParameters = isochronesRequestHandler.constructRouteSearchParameters(request);
        Assert.assertEquals(RoutingProfileType.DRIVING_CAR, routeSearchParameters.getProfileType());
        Assert.assertEquals(WeightingMethod.FASTEST, routeSearchParameters.getWeightingMethod());
        Assert.assertFalse(routeSearchParameters.getConsiderTraffic());
        Assert.assertFalse(routeSearchParameters.getConsiderTurnRestrictions());
        Assert.assertNull(routeSearchParameters.getAvoidAreas());
        Assert.assertEquals(0, routeSearchParameters.getAvoidFeatureTypes());
        Assert.assertEquals(0, routeSearchParameters.getVehicleType());
        Assert.assertFalse(routeSearchParameters.getFlexibleMode());
        Assert.assertEquals(BordersExtractor.Avoid.NONE, routeSearchParameters.getAvoidBorders());
        Assert.assertNull(routeSearchParameters.getProfileParameters());
        Assert.assertNull(routeSearchParameters.getBearings());
        Assert.assertNull(routeSearchParameters.getMaximumRadiuses());
        Assert.assertNull(routeSearchParameters.getAvoidCountries());
        Assert.assertNull(routeSearchParameters.getOptions());
    }

    @Test
    public void processIsochronesRequestOptionsTest() throws Exception {
        IsochronesRequestHandler isochronesRequestHandler = new IsochronesRequestHandler();
        RouteSearchParameters routeSearchParameters = isochronesRequestHandler.constructRouteSearchParameters(request);

        Assert.assertEquals(RoutingProfileType.DRIVING_CAR, routeSearchParameters.getProfileType());
        Assert.assertEquals(WeightingMethod.FASTEST, routeSearchParameters.getWeightingMethod());
        Assert.assertFalse(routeSearchParameters.getConsiderTraffic());
        Assert.assertFalse(routeSearchParameters.getConsiderTurnRestrictions());
        checkPolygon(routeSearchParameters.getAvoidAreas(), geoJsonPolygon);
        Assert.assertEquals(16, routeSearchParameters.getAvoidFeatureTypes());
        Assert.assertEquals(0, routeSearchParameters.getVehicleType());
        Assert.assertFalse(routeSearchParameters.getFlexibleMode());
        Assert.assertEquals(BordersExtractor.Avoid.CONTROLLED, routeSearchParameters.getAvoidBorders());
        Assert.assertNull(routeSearchParameters.getBearings());
        Assert.assertNull(routeSearchParameters.getMaximumRadiuses());
        Assert.assertNull(routeSearchParameters.getOptions());
        Assert.assertEquals(115, routeSearchParameters.getAvoidCountries()[0]);

        ProfileWeightingCollection weightings = routeSearchParameters.getProfileParameters().getWeightings();
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
    public void getIsoMapsTest() {
        IsochronesRequestHandler isochronesRequestHandler = new IsochronesRequestHandler();
        Assert.assertNull(isochronesRequestHandler.getIsoMaps());
    }

    @Test
    public void getIsochroneRequestTest() {
        IsochronesRequestHandler isochronesRequestHandler = new IsochronesRequestHandler();
        Assert.assertNull(isochronesRequestHandler.getIsochroneRequest());
    }

    private void checkPolygon(Polygon[] requestPolys, JSONObject apiPolys) {
        Assert.assertEquals(1, requestPolys.length);

        JSONArray jsonCoords = (JSONArray) ((JSONArray) apiPolys.get("coordinates")).get(0);
        for (int i = 0; i < jsonCoords.size(); i++) {
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