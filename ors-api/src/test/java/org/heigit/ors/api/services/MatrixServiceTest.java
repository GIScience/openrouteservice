package org.heigit.ors.api.services;

import org.heigit.ors.api.EndpointsProperties;
import org.heigit.ors.api.requests.matrix.MatrixRequestEnums;
import org.heigit.ors.api.util.HelperFunctions;
import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.exceptions.ServerLimitExceededException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.matrix.MatrixMetricsType;
import org.heigit.ors.matrix.MatrixRequest;
import org.heigit.ors.api.APIEnums;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.WeightingMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("unittest")
class MatrixServiceTest {
    private MatrixRequest bareMatrixRequest;
    private MatrixRequest matrixRequest;
    private final Coordinate[] coordinates = new Coordinate[3];
    private final Double[][] bareCoordinates = new Double[3][];
    private final Double[] bareCoordinate1 = new Double[2];
    private final Double[] bareCoordinate2 = new Double[2];
    private final Double[] bareCoordinate3 = new Double[2];
    private final List<List<Double>> listOfBareCoordinatesList = new ArrayList<>();

    private final Coordinate coordinate1 = new Coordinate();
    private final Coordinate coordinate2 = new Coordinate();
    private final Coordinate coordinate3 = new Coordinate();

    private List<List<Double>> minimalLocations;
    private int maximumRoutes;

    @Autowired
    private EndpointsProperties endpointsProperties = new EndpointsProperties();

    @Autowired
    private MatrixService matrixService;

    @BeforeEach
    void setUp() {

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

        matrixRequest = new MatrixRequest(
                endpointsProperties.getMatrix().getMaximumSearchRadius(),
                endpointsProperties.getMatrix().getMaximumVisitedNodes(),
                endpointsProperties.getMatrix().getUTurnCost());
        matrixRequest.setResolveLocations(true);
        matrixRequest.setMetrics(MatrixMetricsType.DURATION);
        matrixRequest.setSources(coordinates);
        matrixRequest.setDestinations(coordinates);
        matrixRequest.setProfileType(RoutingProfileType.CYCLING_REGULAR);
        matrixRequest.setUnits(DistanceUnit.METERS);

        bareMatrixRequest = new MatrixRequest(
                endpointsProperties.getMatrix().getMaximumSearchRadius(),
                endpointsProperties.getMatrix().getMaximumVisitedNodes(),
                endpointsProperties.getMatrix().getUTurnCost());
        bareMatrixRequest.setSources(coordinates);
        bareMatrixRequest.setDestinations(coordinates);

        // Fake locations to test maximum exceedings

        minimalLocations = HelperFunctions.fakeListLocations(1, 2);
        maximumRoutes = endpointsProperties.getMatrix().getMaximumRoutes(false) + 1;
    }

    @Test
    void convertMatrixRequestTest() throws StatusCodeException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(bareCoordinates, endpointsProperties);
        springMatrixRequest.setProfile(APIEnums.Profile.DRIVING_CAR);
        springMatrixRequest.setSources(new String[]{"all"});
        springMatrixRequest.setDestinations(new String[]{"all"});
        MatrixRequest matrixRequest = matrixService.convertMatrixRequest(springMatrixRequest);
        assertEquals(1, matrixRequest.getProfileType());
        assertEquals(3, matrixRequest.getSources().length);
        assertEquals(3, matrixRequest.getDestinations().length);
        assertEquals(1, matrixRequest.getMetrics());
        assertEquals(WeightingMethod.UNKNOWN, matrixRequest.getWeightingMethod());
        assertEquals(DistanceUnit.METERS, matrixRequest.getUnits());
        assertFalse(matrixRequest.getResolveLocations());
        assertFalse(matrixRequest.getFlexibleMode());
        assertNull(matrixRequest.getId());

        springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(bareCoordinates, endpointsProperties);
        springMatrixRequest.setProfile(APIEnums.Profile.DRIVING_CAR);
        springMatrixRequest.setSources(new String[]{"all"});
        springMatrixRequest.setDestinations(new String[]{"all"});
        MatrixRequestEnums.Metrics[] metrics = new MatrixRequestEnums.Metrics[2];
        metrics[0] = MatrixRequestEnums.Metrics.DURATION;
        metrics[1] = MatrixRequestEnums.Metrics.DISTANCE;
        springMatrixRequest.setMetrics(metrics);
        matrixRequest = matrixService.convertMatrixRequest(springMatrixRequest);

        assertEquals(3, matrixRequest.getMetrics());
    }

    @Test
    void invalidLocationsTest() {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());
        springMatrixRequest.setProfile(APIEnums.Profile.DRIVING_CAR);
        springMatrixRequest.setSources(new String[]{"foo"});
        springMatrixRequest.setDestinations(new String[]{"bar"});
        assertThrows(ParameterValueException.class, () -> {
            matrixService.convertMatrixRequest(springMatrixRequest);
        });
    }

    @Test
    void invalidMetricsTest() {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());
        springMatrixRequest.setProfile(APIEnums.Profile.DRIVING_CAR);
        springMatrixRequest.setLocations(listOfBareCoordinatesList);
        springMatrixRequest.setMetrics(new MatrixRequestEnums.Metrics[0]);
        springMatrixRequest.setSources(new String[]{"foo"});
        springMatrixRequest.setDestinations(new String[]{"bar"});
        assertThrows(ParameterValueException.class, () -> {
            matrixService.convertMatrixRequest(springMatrixRequest);
        });
    }

    @Test
    void invalidSourceIndexTest() {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());
        springMatrixRequest.setProfile(APIEnums.Profile.DRIVING_CAR);
        springMatrixRequest.setLocations(listOfBareCoordinatesList);
        springMatrixRequest.setSources(new String[]{"foo"});
        springMatrixRequest.setDestinations(new String[]{"bar"});
        assertThrows(ParameterValueException.class, () -> {
            matrixService.convertMatrixRequest(springMatrixRequest);
        });
    }

    @Test
    void invalidDestinationIndexTest() {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());
        springMatrixRequest.setProfile(APIEnums.Profile.DRIVING_CAR);
        springMatrixRequest.setLocations(listOfBareCoordinatesList);
        springMatrixRequest.setSources(new String[]{"all"});
        springMatrixRequest.setDestinations(new String[]{"foo"});
        assertThrows(ParameterValueException.class, () -> {
            matrixService.convertMatrixRequest(springMatrixRequest);
        });
    }

    @Test
    void convertMetricsTest() throws ParameterValueException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());
        assertEquals(1, matrixService.convertMetrics(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DURATION}));
        assertEquals(2, matrixService.convertMetrics(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DISTANCE}));
        assertEquals(3, matrixService.convertMetrics(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DURATION, MatrixRequestEnums.Metrics.DISTANCE}));
    }

    @Test
    void notEnoughLocationsTest() {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());
        assertThrows(ParameterValueException.class, () -> matrixService.convertLocations(minimalLocations, 5, endpointsProperties));
    }

    @Test
    void maximumExceedingLocationsTest() {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());
        assertThrows(ServerLimitExceededException.class, () -> matrixService.convertLocations(listOfBareCoordinatesList, maximumRoutes, endpointsProperties));
    }

    @Test
    void convertLocationsTest() throws ParameterValueException, ServerLimitExceededException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());

        Coordinate[] coordinates = matrixService.convertLocations(listOfBareCoordinatesList, 3, endpointsProperties);
        assertEquals(8.681495, coordinates[0].x, 0);
        assertEquals(49.41461, coordinates[0].y, 0);
        assertEquals(Double.NaN, coordinates[0].z, 0);
        assertEquals(8.686507, coordinates[1].x, 0);
        assertEquals(49.41943, coordinates[1].y, 0);
        assertEquals(Double.NaN, coordinates[1].z, 0);
        assertEquals(8.687872, coordinates[2].x, 0);
        assertEquals(49.420318, coordinates[2].y, 0);
        assertEquals(Double.NaN, coordinates[2].z, 0);
    }

    @Test
    void convertSingleLocationCoordinateTest() throws ParameterValueException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());
        List<Double> locationsList = new ArrayList<>();
        locationsList.add(8.681495);
        locationsList.add(49.41461);
        Coordinate coordinates = matrixService.convertSingleLocationCoordinate(locationsList);
        assertEquals(8.681495, coordinates.x, 0);
        assertEquals(49.41461, coordinates.y, 0);
        assertEquals(Double.NaN, coordinates.z, 0);
    }

    @Test
    void convertWrongSingleLocationCoordinateTest() {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());
        List<Double> locationsList = new ArrayList<>();
        locationsList.add(8.681495);
        locationsList.add(49.41461);
        locationsList.add(123.0);
        assertThrows(ParameterValueException.class, () -> matrixService.convertSingleLocationCoordinate(locationsList));

    }

    @Test
    void convertSourcesTest() throws ParameterValueException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());

        String[] emptySources = new String[0];
        Coordinate[] convertedSources = matrixService.convertSources(emptySources, this.coordinates);
        assertEquals(8.681495, convertedSources[0].x, 0);
        assertEquals(49.41461, convertedSources[0].y, 0);
        assertEquals(Double.NaN, convertedSources[0].z, 0);
        assertEquals(8.686507, convertedSources[1].x, 0);
        assertEquals(49.41943, convertedSources[1].y, 0);
        assertEquals(Double.NaN, convertedSources[1].z, 0);
        assertEquals(8.687872, convertedSources[2].x, 0);
        assertEquals(49.420318, convertedSources[2].y, 0);
        assertEquals(Double.NaN, convertedSources[2].z, 0);

        String[] allSources = new String[]{"all"};
        convertedSources = matrixService.convertSources(allSources, this.coordinates);
        assertEquals(8.681495, convertedSources[0].x, 0);
        assertEquals(49.41461, convertedSources[0].y, 0);
        assertEquals(Double.NaN, convertedSources[0].z, 0);
        assertEquals(8.686507, convertedSources[1].x, 0);
        assertEquals(49.41943, convertedSources[1].y, 0);
        assertEquals(Double.NaN, convertedSources[1].z, 0);
        assertEquals(8.687872, convertedSources[2].x, 0);
        assertEquals(49.420318, convertedSources[2].y, 0);
        assertEquals(Double.NaN, convertedSources[2].z, 0);

        String[] secondSource = new String[]{"1"};
        convertedSources = matrixService.convertSources(secondSource, this.coordinates);
        assertEquals(8.686507, convertedSources[0].x, 0);
        assertEquals(49.41943, convertedSources[0].y, 0);
        assertEquals(Double.NaN, convertedSources[0].z, 0);
    }

    @Test
    void convertWrongSourcesTest() {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());
        String[] wrongSource = new String[]{"foo"};
        assertThrows(ParameterValueException.class, () -> matrixService.convertSources(wrongSource, this.coordinates));
    }

    @Test
    void convertDestinationsTest() throws ParameterValueException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());

        String[] emptyDestinations = new String[0];
        Coordinate[] convertedDestinations = matrixService.convertDestinations(emptyDestinations, this.coordinates);
        assertEquals(8.681495, convertedDestinations[0].x, 0);
        assertEquals(49.41461, convertedDestinations[0].y, 0);
        assertEquals(Double.NaN, convertedDestinations[0].z, 0);
        assertEquals(8.686507, convertedDestinations[1].x, 0);
        assertEquals(49.41943, convertedDestinations[1].y, 0);
        assertEquals(Double.NaN, convertedDestinations[1].z, 0);
        assertEquals(8.687872, convertedDestinations[2].x, 0);
        assertEquals(49.420318, convertedDestinations[2].y, 0);
        assertEquals(Double.NaN, convertedDestinations[2].z, 0);

        String[] allDestinations = new String[]{"all"};
        convertedDestinations = matrixService.convertDestinations(allDestinations, this.coordinates);
        assertEquals(8.681495, convertedDestinations[0].x, 0);
        assertEquals(49.41461, convertedDestinations[0].y, 0);
        assertEquals(Double.NaN, convertedDestinations[0].z, 0);
        assertEquals(8.686507, convertedDestinations[1].x, 0);
        assertEquals(49.41943, convertedDestinations[1].y, 0);
        assertEquals(Double.NaN, convertedDestinations[1].z, 0);
        assertEquals(8.687872, convertedDestinations[2].x, 0);
        assertEquals(49.420318, convertedDestinations[2].y, 0);
        assertEquals(Double.NaN, convertedDestinations[2].z, 0);

        String[] secondDestination = new String[]{"1"};
        convertedDestinations = matrixService.convertDestinations(secondDestination, this.coordinates);
        assertEquals(8.686507, convertedDestinations[0].x, 0);
        assertEquals(49.41943, convertedDestinations[0].y, 0);
        assertEquals(Double.NaN, convertedDestinations[0].z, 0);
    }

    @Test
    void convertWrongDestinationsTest() {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());
        String[] wrongDestinations = new String[]{"foo"};
        assertThrows(ParameterValueException.class, () -> matrixService.convertDestinations(wrongDestinations, this.coordinates));
    }

    @Test
    void convertIndexToLocationsTest() {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());

        ArrayList<Coordinate> coordinate = matrixService.convertIndexToLocations(new String[]{"1"}, this.coordinates);
        assertEquals(8.686507, coordinate.get(0).x, 0);
        assertEquals(49.41943, coordinate.get(0).y, 0);
        assertEquals(Double.NaN, coordinate.get(0).z, 0);
    }

    @Test
    void convertWrongIndexToLocationsTest() {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());
        assertThrows(Exception.class, () -> matrixService.convertIndexToLocations(new String[]{"foo"}, this.coordinates));
    }

    @Test
    void convertUnitsTest() throws ParameterValueException {
        assertEquals(DistanceUnit.METERS, ApiService.convertUnits(APIEnums.Units.METRES));
        assertEquals(DistanceUnit.KILOMETERS, ApiService.convertUnits(APIEnums.Units.KILOMETRES));
        assertEquals(DistanceUnit.MILES, ApiService.convertUnits(APIEnums.Units.MILES));
    }

    @Test
    void convertToProfileTypeTest() throws ParameterValueException {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());

        assertEquals(1, matrixService.convertToMatrixProfileType(APIEnums.Profile.DRIVING_CAR));
        assertEquals(2, matrixService.convertToMatrixProfileType(APIEnums.Profile.DRIVING_HGV));
        assertEquals(10, matrixService.convertToMatrixProfileType(APIEnums.Profile.CYCLING_REGULAR));
        assertEquals(12, matrixService.convertToMatrixProfileType(APIEnums.Profile.CYCLING_ROAD));
        assertEquals(11, matrixService.convertToMatrixProfileType(APIEnums.Profile.CYCLING_MOUNTAIN));
        assertEquals(17, matrixService.convertToMatrixProfileType(APIEnums.Profile.CYCLING_ELECTRIC));
        assertEquals(20, matrixService.convertToMatrixProfileType(APIEnums.Profile.FOOT_WALKING));
        assertEquals(21, matrixService.convertToMatrixProfileType(APIEnums.Profile.FOOT_HIKING));
        assertEquals(30, matrixService.convertToMatrixProfileType(APIEnums.Profile.WHEELCHAIR));
    }

    @Test
    void convertToEmptyMatrixProfileTypeTest() {
        org.heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new org.heigit.ors.api.requests.matrix.MatrixRequest(new ArrayList<>());
        assertThrows(ParameterValueException.class, () -> matrixService.convertToMatrixProfileType(null));
    }

}
