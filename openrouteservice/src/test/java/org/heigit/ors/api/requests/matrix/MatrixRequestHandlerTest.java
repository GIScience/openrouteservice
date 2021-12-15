package org.heigit.ors.api.requests.matrix;

import com.vividsolutions.jts.geom.Coordinate;
import org.heigit.ors.api.requests.common.APIEnums;
import org.heigit.ors.api.requests.common.APIRequest;
import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.exceptions.ServerLimitExceededException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.matrix.MatrixMetricsType;
import org.heigit.ors.matrix.MatrixRequest;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.WeightingMethod;
import org.heigit.ors.services.matrix.MatrixServiceSettings;
import org.heigit.ors.util.HelperFunctions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MatrixRequestHandlerTest {
    private MatrixRequest bareMatrixRequest = new MatrixRequest();
    private MatrixRequest matrixRequest = new MatrixRequest();
    private Coordinate[] coordinates = new Coordinate[3];
    private Double[][] bareCoordinates = new Double[3][];
    private Double[] bareCoordinate1 = new Double[2];
    private Double[] bareCoordinate2 = new Double[2];
    private Double[] bareCoordinate3 = new Double[2];
    private List<List<Double>> listOfBareCoordinatesList = new ArrayList<>();

    private Coordinate coordinate1 = new Coordinate();
    private Coordinate coordinate2 = new Coordinate();
    private Coordinate coordinate3 = new Coordinate();

    private List<List<Double>> minimalLocations;
    private int maximumRoutes;

    @Before
    public void setUp() {
        System.setProperty("ors_config", "target/test-classes/ors-config-test.json");

        List<Double> bareCoordinatesList = new ArrayList<>();
        bareCoordinatesList.add(8.681495);
        bareCoordinatesList.add(49.41461);
        listOfBareCoordinatesList.add(bareCoordinatesList);
        bareCoordinatesList = new ArrayList<>();
        bareCoordinatesList.add(8.686507);
        bareCoordinatesList.add(49.41943);
        listOfBareCoordinatesList.add(bareCoordinatesList);
        bareCoordinatesList = new ArrayList<>();
        bareCoordinatesList.add(8.687872);
        bareCoordinatesList.add(49.420318);
        listOfBareCoordinatesList.add(bareCoordinatesList);

        bareCoordinate1[0] = 8.681495;
        bareCoordinate1[1] = 49.41461;
        bareCoordinate2[0] = 8.686507;
        bareCoordinate2[1] = 49.41943;
        bareCoordinate3[0] = 8.687872;
        bareCoordinate3[1] = 49.420318;
        bareCoordinates[0] = bareCoordinate1;
        bareCoordinates[1] = bareCoordinate2;
        bareCoordinates[2] = bareCoordinate3;
        coordinate1.x = 8.681495;
        coordinate1.y = 49.41461;
        coordinate2.x = 8.686507;
        coordinate2.y = 49.41943;
        coordinate3.x = 8.687872;
        coordinate3.y = 49.420318;
        coordinates[0] = coordinate1;
        coordinates[1] = coordinate2;
        coordinates[2] = coordinate3;
        matrixRequest.setResolveLocations(true);
        matrixRequest.setMetrics(MatrixMetricsType.DURATION);
        matrixRequest.setSources(coordinates);
        matrixRequest.setDestinations(coordinates);
        matrixRequest.setProfileType(RoutingProfileType.CYCLING_REGULAR);
        matrixRequest.setUnits(DistanceUnit.METERS);
        bareMatrixRequest.setSources(coordinates);
        bareMatrixRequest.setDestinations(coordinates);

        // Fake locations to test maximum exceedings

        minimalLocations = HelperFunctions.fakeListLocations(1, 2);
        maximumRoutes = MatrixServiceSettings.getMaximumRoutes(false) + 1;
    }

    @Test
    public void convertMatrixRequestTest() throws StatusCodeException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(bareCoordinates);
        springMatrixRequest.setProfile(APIEnums.Profile.DRIVING_CAR);
        springMatrixRequest.setSources(new String[]{"all"});
        springMatrixRequest.setDestinations(new String[]{"all"});
        MatrixRequest matrixRequest = springMatrixRequest.convertMatrixRequest();
        Assert.assertEquals(1, matrixRequest.getProfileType());
        Assert.assertEquals(3, matrixRequest.getSources().length);
        Assert.assertEquals(3, matrixRequest.getDestinations().length);
        Assert.assertEquals(1, matrixRequest.getMetrics());
        Assert.assertEquals(WeightingMethod.UNKNOWN, matrixRequest.getWeightingMethod());
        Assert.assertEquals(DistanceUnit.METERS, matrixRequest.getUnits());
        Assert.assertFalse(matrixRequest.getResolveLocations());
        Assert.assertFalse(matrixRequest.getFlexibleMode());
        Assert.assertNull(matrixRequest.getId());

        springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(bareCoordinates);
        springMatrixRequest.setProfile(APIEnums.Profile.DRIVING_CAR);
        springMatrixRequest.setSources(new String[]{"all"});
        springMatrixRequest.setDestinations(new String[]{"all"});
        MatrixRequestEnums.Metrics[] metrics = new MatrixRequestEnums.Metrics[2];
        metrics[0] = MatrixRequestEnums.Metrics.DURATION;
        metrics[1] = MatrixRequestEnums.Metrics.DISTANCE;
        springMatrixRequest.setMetrics(metrics);
        matrixRequest = springMatrixRequest.convertMatrixRequest();

        Assert.assertEquals(3, matrixRequest.getMetrics());
    }

    @Test(expected = ParameterValueException.class)
    public void invalidLocationsTest() throws StatusCodeException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());
        springMatrixRequest.setProfile(APIEnums.Profile.DRIVING_CAR);
        springMatrixRequest.setSources(new String[]{"foo"});
        springMatrixRequest.setDestinations(new String[]{"bar"});
        springMatrixRequest.convertMatrixRequest();
    }

    @Test(expected = ParameterValueException.class)
    public void invalidMetricsTest() throws StatusCodeException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());
        springMatrixRequest.setProfile(APIEnums.Profile.DRIVING_CAR);
        springMatrixRequest.setLocations(listOfBareCoordinatesList);
        springMatrixRequest.setMetrics(new MatrixRequestEnums.Metrics[0]);
        springMatrixRequest.setSources(new String[]{"foo"});
        springMatrixRequest.setDestinations(new String[]{"bar"});
        springMatrixRequest.convertMatrixRequest();
    }

    @Test(expected = ParameterValueException.class)
    public void invalidSourceIndexTest() throws StatusCodeException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());
        springMatrixRequest.setProfile(APIEnums.Profile.DRIVING_CAR);
        springMatrixRequest.setLocations(listOfBareCoordinatesList);
        springMatrixRequest.setSources(new String[]{"foo"});
        springMatrixRequest.setDestinations(new String[]{"bar"});
        springMatrixRequest.convertMatrixRequest();
    }

    @Test(expected = ParameterValueException.class)
    public void invalidDestinationIndexTest() throws StatusCodeException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());
        springMatrixRequest.setProfile(APIEnums.Profile.DRIVING_CAR);
        springMatrixRequest.setLocations(listOfBareCoordinatesList);
        springMatrixRequest.setSources(new String[]{"all"});
        springMatrixRequest.setDestinations(new String[]{"foo"});
        springMatrixRequest.convertMatrixRequest();
    }

    @Test
    public void convertMetricsTest() throws ParameterValueException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());
        Assert.assertEquals(1, springMatrixRequest.convertMetrics(new MatrixRequestEnums.Metrics[] {MatrixRequestEnums.Metrics.DURATION}));
        Assert.assertEquals(2, springMatrixRequest.convertMetrics(new MatrixRequestEnums.Metrics[] {MatrixRequestEnums.Metrics.DISTANCE}));
        Assert.assertEquals(3, springMatrixRequest.convertMetrics(new MatrixRequestEnums.Metrics[] {MatrixRequestEnums.Metrics.DURATION, MatrixRequestEnums.Metrics.DISTANCE}));
    }

    @Test(expected = ParameterValueException.class)
    public void notEnoughLocationsTest() throws ParameterValueException, ServerLimitExceededException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());

        springMatrixRequest.convertLocations(minimalLocations, 5);
    }

    @Test(expected = ServerLimitExceededException.class)
    public void maximumExceedingLocationsTest() throws ParameterValueException, ServerLimitExceededException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());

        springMatrixRequest.convertLocations(listOfBareCoordinatesList, maximumRoutes);
    }

    @Test
    public void convertLocationsTest() throws ParameterValueException, ServerLimitExceededException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());

        Coordinate[] coordinates = springMatrixRequest.convertLocations(listOfBareCoordinatesList, 3);
        Assert.assertEquals(8.681495, coordinates[0].x, 0);
        Assert.assertEquals(49.41461, coordinates[0].y, 0);
        Assert.assertEquals(Double.NaN, coordinates[0].z, 0);
        Assert.assertEquals(8.686507, coordinates[1].x, 0);
        Assert.assertEquals(49.41943, coordinates[1].y, 0);
        Assert.assertEquals(Double.NaN, coordinates[1].z, 0);
        Assert.assertEquals(8.687872, coordinates[2].x, 0);
        Assert.assertEquals(49.420318, coordinates[2].y, 0);
        Assert.assertEquals(Double.NaN, coordinates[2].z, 0);
    }

    @Test
    public void convertSingleLocationCoordinateTest() throws ParameterValueException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());

        List<Double> locationsList = new ArrayList<>();
        locationsList.add(8.681495);
        locationsList.add(49.41461);
        Coordinate coordinates = springMatrixRequest.convertSingleLocationCoordinate(locationsList);
        Assert.assertEquals(8.681495, coordinates.x, 0);
        Assert.assertEquals(49.41461, coordinates.y, 0);
        Assert.assertEquals(Double.NaN, coordinates.z, 0);
    }

    @Test(expected = ParameterValueException.class)
    public void convertWrongSingleLocationCoordinateTest() throws ParameterValueException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());

        List<Double> locationsList = new ArrayList<>();
        locationsList.add(8.681495);
        locationsList.add(49.41461);
        locationsList.add(123.0);
        springMatrixRequest.convertSingleLocationCoordinate(locationsList);

    }

    @Test
    public void convertSourcesTest() throws ParameterValueException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());

        String[] emptySources = new String[0];
        Coordinate[] convertedSources = springMatrixRequest.convertSources(emptySources, this.coordinates);
        Assert.assertEquals(8.681495, convertedSources[0].x, 0);
        Assert.assertEquals(49.41461, convertedSources[0].y, 0);
        Assert.assertEquals(Double.NaN, convertedSources[0].z, 0);
        Assert.assertEquals(8.686507, convertedSources[1].x, 0);
        Assert.assertEquals(49.41943, convertedSources[1].y, 0);
        Assert.assertEquals(Double.NaN, convertedSources[1].z, 0);
        Assert.assertEquals(8.687872, convertedSources[2].x, 0);
        Assert.assertEquals(49.420318, convertedSources[2].y, 0);
        Assert.assertEquals(Double.NaN, convertedSources[2].z, 0);

        String[] allSources = new String[]{"all"};
        convertedSources = springMatrixRequest.convertSources(allSources, this.coordinates);
        Assert.assertEquals(8.681495, convertedSources[0].x, 0);
        Assert.assertEquals(49.41461, convertedSources[0].y, 0);
        Assert.assertEquals(Double.NaN, convertedSources[0].z, 0);
        Assert.assertEquals(8.686507, convertedSources[1].x, 0);
        Assert.assertEquals(49.41943, convertedSources[1].y, 0);
        Assert.assertEquals(Double.NaN, convertedSources[1].z, 0);
        Assert.assertEquals(8.687872, convertedSources[2].x, 0);
        Assert.assertEquals(49.420318, convertedSources[2].y, 0);
        Assert.assertEquals(Double.NaN, convertedSources[2].z, 0);

        String[] secondSource = new String[]{"1"};
        convertedSources = springMatrixRequest.convertSources(secondSource, this.coordinates);
        Assert.assertEquals(8.686507, convertedSources[0].x, 0);
        Assert.assertEquals(49.41943, convertedSources[0].y, 0);
        Assert.assertEquals(Double.NaN, convertedSources[0].z, 0);
    }

    @Test(expected = ParameterValueException.class)
    public void convertWrongSourcesTest() throws ParameterValueException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());

        String[] wrongSource = new String[]{"foo"};
        springMatrixRequest.convertSources(wrongSource, this.coordinates);
    }

    @Test
    public void convertDestinationsTest() throws ParameterValueException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());

        String[] emptyDestinations = new String[0];
        Coordinate[] convertedDestinations = springMatrixRequest.convertDestinations(emptyDestinations, this.coordinates);
        Assert.assertEquals(8.681495, convertedDestinations[0].x, 0);
        Assert.assertEquals(49.41461, convertedDestinations[0].y, 0);
        Assert.assertEquals(Double.NaN, convertedDestinations[0].z, 0);
        Assert.assertEquals(8.686507, convertedDestinations[1].x, 0);
        Assert.assertEquals(49.41943, convertedDestinations[1].y, 0);
        Assert.assertEquals(Double.NaN, convertedDestinations[1].z, 0);
        Assert.assertEquals(8.687872, convertedDestinations[2].x, 0);
        Assert.assertEquals(49.420318, convertedDestinations[2].y, 0);
        Assert.assertEquals(Double.NaN, convertedDestinations[2].z, 0);

        String[] allDestinations = new String[]{"all"};
        convertedDestinations = springMatrixRequest.convertDestinations(allDestinations, this.coordinates);
        Assert.assertEquals(8.681495, convertedDestinations[0].x, 0);
        Assert.assertEquals(49.41461, convertedDestinations[0].y, 0);
        Assert.assertEquals(Double.NaN, convertedDestinations[0].z, 0);
        Assert.assertEquals(8.686507, convertedDestinations[1].x, 0);
        Assert.assertEquals(49.41943, convertedDestinations[1].y, 0);
        Assert.assertEquals(Double.NaN, convertedDestinations[1].z, 0);
        Assert.assertEquals(8.687872, convertedDestinations[2].x, 0);
        Assert.assertEquals(49.420318, convertedDestinations[2].y, 0);
        Assert.assertEquals(Double.NaN, convertedDestinations[2].z, 0);

        String[] secondDestination = new String[]{"1"};
        convertedDestinations = springMatrixRequest.convertDestinations(secondDestination, this.coordinates);
        Assert.assertEquals(8.686507, convertedDestinations[0].x, 0);
        Assert.assertEquals(49.41943, convertedDestinations[0].y, 0);
        Assert.assertEquals(Double.NaN, convertedDestinations[0].z, 0);
    }

    @Test(expected = ParameterValueException.class)
    public void convertWrongDestinationsTest() throws ParameterValueException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());

        String[] wrongDestinations = new String[]{"foo"};
        springMatrixRequest.convertDestinations(wrongDestinations, this.coordinates);
    }

    @Test
    public void convertIndexToLocationsTest() throws Exception {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());

        ArrayList<Coordinate> coordinate = springMatrixRequest.convertIndexToLocations(new String[]{"1"}, this.coordinates);
        Assert.assertEquals(8.686507, coordinate.get(0).x, 0);
        Assert.assertEquals(49.41943, coordinate.get(0).y, 0);
        Assert.assertEquals(Double.NaN, coordinate.get(0).z, 0);
    }

    @Test(expected = Exception.class)
    public void convertWrongIndexToLocationsTest() throws Exception {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());

        springMatrixRequest.convertIndexToLocations(new String[]{"foo"}, this.coordinates);
    }

    @Test
    public void convertUnitsTest() throws ParameterValueException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());

        Assert.assertEquals(DistanceUnit.METERS, APIRequest.convertUnits(APIEnums.Units.METRES));
        Assert.assertEquals(DistanceUnit.KILOMETERS, APIRequest.convertUnits(APIEnums.Units.KILOMETRES));
        Assert.assertEquals(DistanceUnit.MILES, APIRequest.convertUnits(APIEnums.Units.MILES));
    }

    @Test
    public void convertToProfileTypeTest() throws ParameterValueException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());

        Assert.assertEquals(1, springMatrixRequest.convertToMatrixProfileType(APIEnums.Profile.DRIVING_CAR));
        Assert.assertEquals(2, springMatrixRequest.convertToMatrixProfileType(APIEnums.Profile.DRIVING_HGV));
        Assert.assertEquals(10, springMatrixRequest.convertToMatrixProfileType(APIEnums.Profile.CYCLING_REGULAR));
        Assert.assertEquals(12, springMatrixRequest.convertToMatrixProfileType(APIEnums.Profile.CYCLING_ROAD));
        Assert.assertEquals(11, springMatrixRequest.convertToMatrixProfileType(APIEnums.Profile.CYCLING_MOUNTAIN));
        Assert.assertEquals(17, springMatrixRequest.convertToMatrixProfileType(APIEnums.Profile.CYCLING_ELECTRIC));
        Assert.assertEquals(20, springMatrixRequest.convertToMatrixProfileType(APIEnums.Profile.FOOT_WALKING));
        Assert.assertEquals(21, springMatrixRequest.convertToMatrixProfileType(APIEnums.Profile.FOOT_HIKING));
        Assert.assertEquals(30, springMatrixRequest.convertToMatrixProfileType(APIEnums.Profile.WHEELCHAIR));
    }

    @Test(expected = ParameterValueException.class)
    public void convertToWrongMatrixProfileTypeTest() throws ParameterValueException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());

        springMatrixRequest.convertToMatrixProfileType(APIEnums.Profile.forValue("foo"));
    }

}
