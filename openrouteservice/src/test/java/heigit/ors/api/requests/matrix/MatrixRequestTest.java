package heigit.ors.api.requests.matrix;

import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.services.matrix.MatrixServiceSettings;
import heigit.ors.util.HelperFunctions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class MatrixRequestTest {
    private MatrixRequest matrixLocationsRequest;
    private MatrixRequest matrixLocationsListRequest;
    private List<List<Double>> listOfBareCoordinatesList = new ArrayList<>();
    private Double[][] bareCoordinates = new Double[3][];
    private Double[] bareCoordinate1 = new Double[2];
    private Double[] bareCoordinate2 = new Double[2];
    private Double[] bareCoordinate3 = new Double[2];
    private Double[][] maximumLocationsArray;
    private Double[][] minimalLocationsArray;

    @Before
    public void setUp() {
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

    @Test(expected = ParameterValueException.class)
    public void tooMuchLocationsErrorTest() throws ParameterValueException {
        matrixLocationsListRequest = new MatrixRequest(maximumLocationsArray);

    }

    @Test(expected = ParameterValueException.class)
    public void tooLittleLocationsErrorTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(minimalLocationsArray);
    }

    @Test(expected = ParameterValueException.class)
    public void invalidLocationsErrorTest() throws ParameterValueException {
        Double[][] listOfFaultyBareCoordinatesList = HelperFunctions.fakeArrayLocations(3, 1);
        matrixLocationsRequest = new MatrixRequest(listOfFaultyBareCoordinatesList);
    }


    @Test
    public void getIdTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        Assert.assertNull(matrixLocationsRequest.getId());
        Assert.assertNull(matrixLocationsListRequest.getId());
    }

    @Test
    public void setIdTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        matrixLocationsRequest.setId("foo1");
        matrixLocationsListRequest.setId("foo2");
        Assert.assertEquals("foo1", matrixLocationsRequest.getId());
        Assert.assertEquals("foo2", matrixLocationsListRequest.getId());

    }

    @Test
    public void hasIdTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        Assert.assertFalse(matrixLocationsRequest.hasId());
        Assert.assertFalse(matrixLocationsListRequest.hasId());
        matrixLocationsRequest.setId("foo1");
        matrixLocationsListRequest.setId("foo2");
        Assert.assertTrue(matrixLocationsRequest.hasId());
        Assert.assertTrue(matrixLocationsListRequest.hasId());
    }

    @Test
    public void getProfileTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        Assert.assertNull(matrixLocationsRequest.getProfile());
        Assert.assertNull(matrixLocationsListRequest.getProfile());
    }

    @Test
    public void setProfileTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        matrixLocationsRequest.setProfile(APIEnums.Profile.DRIVING_CAR);
        Assert.assertEquals(APIEnums.Profile.DRIVING_CAR, matrixLocationsRequest.getProfile());
        matrixLocationsListRequest.setProfile(APIEnums.Profile.DRIVING_HGV);
        Assert.assertEquals(APIEnums.Profile.DRIVING_HGV, matrixLocationsListRequest.getProfile());
    }

    @Test
    public void getLocationsTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        Assert.assertEquals(listOfBareCoordinatesList, matrixLocationsRequest.getLocations());
        Assert.assertEquals(listOfBareCoordinatesList, matrixLocationsListRequest.getLocations());
    }

    @Test
    public void setLocationsTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        matrixLocationsRequest.setLocations(listOfBareCoordinatesList);
        matrixLocationsListRequest.setLocations(listOfBareCoordinatesList);
        Assert.assertEquals(listOfBareCoordinatesList, matrixLocationsRequest.getLocations());
        Assert.assertEquals(listOfBareCoordinatesList, matrixLocationsListRequest.getLocations());
    }

    @Test
    public void setSourcesTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        matrixLocationsRequest.setSources(new String[]{"foo"});
        matrixLocationsListRequest.setSources(new String[]{"foo"});
        Assert.assertArrayEquals(new String[]{"foo"}, matrixLocationsRequest.getSources());
        Assert.assertArrayEquals(new String[]{"foo"}, matrixLocationsListRequest.getSources());
    }

    @Test
    public void setAndGetDestinationsTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        matrixLocationsRequest.setDestinations(new String[]{"all"});
        matrixLocationsListRequest.setDestinations(new String[]{"1","2"});
        Assert.assertArrayEquals(new String[]{"all"}, matrixLocationsRequest.getDestinations());
        Assert.assertArrayEquals(new String[]{"1","2"}, matrixLocationsListRequest.getDestinations());
    }

    @Test
    public void setAndGetMetricsTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        matrixLocationsRequest.setMetrics(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DURATION});
        matrixLocationsListRequest.setMetrics(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DISTANCE});
        Assert.assertArrayEquals(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DURATION}, matrixLocationsRequest.getMetrics());
        Assert.assertArrayEquals(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DISTANCE}, matrixLocationsListRequest.getMetrics());
    }

    @Test
    public void setAndGetResolveLocationsTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        matrixLocationsRequest.setResolveLocations(true);
        matrixLocationsListRequest.setResolveLocations(false);
        Assert.assertTrue(matrixLocationsRequest.getResolveLocations());
        Assert.assertFalse(matrixLocationsListRequest.getResolveLocations());
    }

    @Test
    public void setAndGetUnitsTest() {
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        matrixLocationsListRequest.setUnits(APIEnums.Units.KILOMETRES);
        Assert.assertEquals(APIEnums.Units.KILOMETRES, matrixLocationsListRequest.getUnits());
    }

    @Test
    public void setAndGetOptimizedTest() throws ParameterValueException {
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        matrixLocationsRequest.setOptimized(true);
        matrixLocationsListRequest.setOptimized(false);
        Assert.assertTrue(matrixLocationsRequest.getOptimized());
        Assert.assertFalse(matrixLocationsListRequest.getOptimized());

    }

    @Test
    public void setAndGetResponseTypeTest() {
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        matrixLocationsListRequest.setResponseType(APIEnums.MatrixResponseType.JSON);
        Assert.assertEquals(APIEnums.MatrixResponseType.JSON, matrixLocationsListRequest.getResponseType());
    }
}