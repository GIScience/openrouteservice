package org.heigit.ors.api.requests.matrix;

import org.heigit.ors.api.requests.common.APIEnums;
import org.heigit.ors.config.MatrixServiceSettings;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.util.HelperFunctions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class MatrixRequestTest {
    private MatrixRequest matrixLocationsRequest;
    private MatrixRequest matrixLocationsListRequest;
    private final List<List<Double>> listOfBareCoordinatesList = new ArrayList<>();
    private final Double[][] bareCoordinates = new Double[3][];
    private final Double[] bareCoordinate1 = new Double[2];
    private final Double[] bareCoordinate2 = new Double[2];
    private final Double[] bareCoordinate3 = new Double[2];
    private Double[][] maximumLocationsArray;
    private Double[][] minimalLocationsArray;

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

        maximumLocationsArray = HelperFunctions.fakeArrayLocations(MatrixServiceSettings.getMaximumRoutes(false) + 1, 2);
        minimalLocationsArray = HelperFunctions.fakeArrayLocations(1, 2);


    }

    @Test
    void tooMuchLocationsErrorTest() {
        assertThrows(ParameterValueException.class, () -> {
            matrixLocationsListRequest = new MatrixRequest(maximumLocationsArray);

        });

    }

    @Test
    void tooLittleLocationsErrorTest() {
        assertThrows(ParameterValueException.class, () -> {
            matrixLocationsRequest = new MatrixRequest(minimalLocationsArray);
        });
    }

    @Test
    void invalidLocationsErrorTest() {
        assertThrows(ParameterValueException.class, () -> {
            Double[][] listOfFaultyBareCoordinatesList = HelperFunctions.fakeArrayLocations(3, 1);
            matrixLocationsRequest = new MatrixRequest(listOfFaultyBareCoordinatesList);
        });
    }


    @Test
    void getIdTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        assertNull(matrixLocationsRequest.getId());
        assertNull(matrixLocationsListRequest.getId());
    }

    @Test
    void setIdTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        matrixLocationsRequest.setId("foo1");
        matrixLocationsListRequest.setId("foo2");
        assertEquals("foo1", matrixLocationsRequest.getId());
        assertEquals("foo2", matrixLocationsListRequest.getId());

    }

    @Test
    void hasIdTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        assertFalse(matrixLocationsRequest.hasId());
        assertFalse(matrixLocationsListRequest.hasId());
        matrixLocationsRequest.setId("foo1");
        matrixLocationsListRequest.setId("foo2");
        assertTrue(matrixLocationsRequest.hasId());
        assertTrue(matrixLocationsListRequest.hasId());
    }

    @Test
    void getProfileTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        assertNull(matrixLocationsRequest.getProfile());
        assertNull(matrixLocationsListRequest.getProfile());
    }

    @Test
    void setProfileTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        matrixLocationsRequest.setProfile(APIEnums.Profile.DRIVING_CAR);
        assertEquals(APIEnums.Profile.DRIVING_CAR, matrixLocationsRequest.getProfile());
        matrixLocationsListRequest.setProfile(APIEnums.Profile.DRIVING_HGV);
        assertEquals(APIEnums.Profile.DRIVING_HGV, matrixLocationsListRequest.getProfile());
    }

    @Test
    void getLocationsTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        assertEquals(listOfBareCoordinatesList, matrixLocationsRequest.getLocations());
        assertEquals(listOfBareCoordinatesList, matrixLocationsListRequest.getLocations());
    }

    @Test
    void setLocationsTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        matrixLocationsRequest.setLocations(listOfBareCoordinatesList);
        matrixLocationsListRequest.setLocations(listOfBareCoordinatesList);
        assertEquals(listOfBareCoordinatesList, matrixLocationsRequest.getLocations());
        assertEquals(listOfBareCoordinatesList, matrixLocationsListRequest.getLocations());
    }

    @Test
    void setSourcesTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        matrixLocationsRequest.setSources(new String[]{"foo"});
        matrixLocationsListRequest.setSources(new String[]{"foo"});
        assertArrayEquals(new String[]{"foo"}, matrixLocationsRequest.getSources());
        assertArrayEquals(new String[]{"foo"}, matrixLocationsListRequest.getSources());
    }

    @Test
    void setAndGetDestinationsTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        matrixLocationsRequest.setDestinations(new String[]{"all"});
        matrixLocationsListRequest.setDestinations(new String[]{"1","2"});
        assertArrayEquals(new String[]{"all"}, matrixLocationsRequest.getDestinations());
        assertArrayEquals(new String[]{"1","2"}, matrixLocationsListRequest.getDestinations());
    }

    @Test
    void setAndGetMetricsTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        matrixLocationsRequest.setMetrics(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DURATION});
        matrixLocationsListRequest.setMetrics(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DISTANCE});
        assertArrayEquals(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DURATION}, matrixLocationsRequest.getMetrics());
        assertArrayEquals(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DISTANCE}, matrixLocationsListRequest.getMetrics());
    }

    @Test
    void setAndGetResolveLocationsTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        matrixLocationsRequest.setResolveLocations(true);
        matrixLocationsListRequest.setResolveLocations(false);
        assertTrue(matrixLocationsRequest.getResolveLocations());
        assertFalse(matrixLocationsListRequest.getResolveLocations());
    }

    @Test
    void setAndGetUnitsTest() {
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        matrixLocationsListRequest.setUnits(APIEnums.Units.KILOMETRES);
        assertEquals(APIEnums.Units.KILOMETRES, matrixLocationsListRequest.getUnits());
    }

    @Test
    void setAndGetOptimizedTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        matrixLocationsRequest.setOptimized(true);
        matrixLocationsListRequest.setOptimized(false);
        assertTrue(matrixLocationsRequest.getOptimized());
        assertFalse(matrixLocationsListRequest.getOptimized());

    }

    @Test
    void setAndGetResponseTypeTest() {
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        matrixLocationsListRequest.setResponseType(APIEnums.MatrixResponseType.JSON);
        assertEquals(APIEnums.MatrixResponseType.JSON, matrixLocationsListRequest.getResponseType());
    }
}
