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
    private static MatrixRequest bareMatrixRequest;
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

        maximumLocationsArray = HelperFunctions.fakeArrayLocations(MatrixServiceSettings.getMaximumLocations(false) + 1, 2);
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
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        Assert.assertNull(bareMatrixRequest.getId());
        Assert.assertNull(matrixLocationsRequest.getId());
        Assert.assertNull(matrixLocationsListRequest.getId());
    }

    @Test
    public void setIdTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        bareMatrixRequest.setId("foo");
        matrixLocationsRequest.setId("foo1");
        matrixLocationsListRequest.setId("foo2");
        Assert.assertEquals("foo", bareMatrixRequest.getId());
        Assert.assertEquals("foo1", matrixLocationsRequest.getId());
        Assert.assertEquals("foo2", matrixLocationsListRequest.getId());

    }

    @Test
    public void hasIdTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        Assert.assertFalse(bareMatrixRequest.hasId());
        Assert.assertFalse(matrixLocationsRequest.hasId());
        Assert.assertFalse(matrixLocationsListRequest.hasId());
        bareMatrixRequest.setId("foo");
        matrixLocationsRequest.setId("foo1");
        matrixLocationsListRequest.setId("foo2");
        Assert.assertTrue(bareMatrixRequest.hasId());
        Assert.assertTrue(matrixLocationsRequest.hasId());
        Assert.assertTrue(matrixLocationsListRequest.hasId());
    }

    @Test
    public void getProfileTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        Assert.assertNull(bareMatrixRequest.getProfile());
        Assert.assertNull(matrixLocationsRequest.getProfile());
        Assert.assertNull(matrixLocationsListRequest.getProfile());
    }

    @Test
    public void setProfileTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        bareMatrixRequest.setProfile(APIEnums.Profile.DRIVING_HGV);
        matrixLocationsRequest.setProfile(APIEnums.Profile.DRIVING_CAR);
        Assert.assertEquals(APIEnums.Profile.DRIVING_HGV, bareMatrixRequest.getProfile());
        Assert.assertEquals(APIEnums.Profile.DRIVING_CAR, matrixLocationsRequest.getProfile());
    }

    @Test
    public void getLocationsTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        Assert.assertNull(bareMatrixRequest.getLocations());
        Assert.assertEquals(listOfBareCoordinatesList, matrixLocationsRequest.getLocations());
        Assert.assertEquals(listOfBareCoordinatesList, matrixLocationsListRequest.getLocations());
    }

    @Test
    public void setLocationsTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        bareMatrixRequest.setLocations(listOfBareCoordinatesList);
        matrixLocationsRequest.setLocations(listOfBareCoordinatesList);
        matrixLocationsListRequest.setLocations(listOfBareCoordinatesList);
        Assert.assertEquals(listOfBareCoordinatesList, bareMatrixRequest.getLocations());
        Assert.assertEquals(listOfBareCoordinatesList, matrixLocationsRequest.getLocations());
        Assert.assertEquals(listOfBareCoordinatesList, matrixLocationsListRequest.getLocations());
    }

    @Test
    public void getSourcesTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        Assert.assertArrayEquals(new String[]{"all"}, bareMatrixRequest.getSources());
        Assert.assertArrayEquals(new String[]{"all"}, matrixLocationsRequest.getSources());
        Assert.assertArrayEquals(new String[]{"all"}, matrixLocationsListRequest.getSources());
    }

    @Test
    public void setSourcesTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        bareMatrixRequest.setSources(new String[]{"foo"});
        matrixLocationsRequest.setSources(new String[]{"foo"});
        matrixLocationsListRequest.setSources(new String[]{"foo"});
        Assert.assertArrayEquals(new String[]{"foo"}, bareMatrixRequest.getSources());
        Assert.assertArrayEquals(new String[]{"foo"}, matrixLocationsRequest.getSources());
        Assert.assertArrayEquals(new String[]{"foo"}, matrixLocationsListRequest.getSources());
    }

    @Test
    public void getDestinationsTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        Assert.assertArrayEquals(new String[]{"all"}, bareMatrixRequest.getDestinations());
        Assert.assertArrayEquals(new String[]{"all"}, matrixLocationsRequest.getDestinations());
        Assert.assertArrayEquals(new String[]{"all"}, matrixLocationsListRequest.getDestinations());
    }

    @Test
    public void setDestinationsTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        bareMatrixRequest.setDestinations(new String[]{"foo"});
        matrixLocationsRequest.setDestinations(new String[]{"foo"});
        matrixLocationsListRequest.setDestinations(new String[]{"foo"});
        Assert.assertArrayEquals(new String[]{"foo"}, bareMatrixRequest.getDestinations());
        Assert.assertArrayEquals(new String[]{"foo"}, matrixLocationsRequest.getDestinations());
        Assert.assertArrayEquals(new String[]{"foo"}, matrixLocationsListRequest.getDestinations());
    }

    @Test
    public void getMetricsTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        Assert.assertArrayEquals(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DURATION}, bareMatrixRequest.getMetrics());
        Assert.assertArrayEquals(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DURATION}, matrixLocationsRequest.getMetrics());
        Assert.assertArrayEquals(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DURATION}, matrixLocationsListRequest.getMetrics());
    }

    @Test
    public void setMetricsTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        bareMatrixRequest.setMetrics(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DURATION});
        matrixLocationsRequest.setMetrics(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DURATION});
        matrixLocationsListRequest.setMetrics(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DURATION});
        Assert.assertArrayEquals(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DURATION}, bareMatrixRequest.getMetrics());
        Assert.assertArrayEquals(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DURATION}, matrixLocationsRequest.getMetrics());
        Assert.assertArrayEquals(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DURATION}, matrixLocationsListRequest.getMetrics());
    }

    @Test
    public void isResolveLocationsTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        Assert.assertFalse(bareMatrixRequest.isResolveLocations());
        Assert.assertFalse(matrixLocationsRequest.isResolveLocations());
        Assert.assertFalse(matrixLocationsListRequest.isResolveLocations());
    }

    @Test
    public void setResolveLocationsTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        bareMatrixRequest.setResolve_Locations(true);
        matrixLocationsRequest.setResolve_Locations(true);
        matrixLocationsListRequest.setResolve_Locations(true);
        Assert.assertTrue(bareMatrixRequest.isResolveLocations());
        Assert.assertTrue(matrixLocationsRequest.isResolveLocations());
        Assert.assertTrue(matrixLocationsListRequest.isResolveLocations());
    }

    @Test
    public void getUnitsTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        Assert.assertEquals(APIEnums.Units.METRES, bareMatrixRequest.getUnits());
        Assert.assertEquals(APIEnums.Units.METRES, matrixLocationsRequest.getUnits());
        Assert.assertEquals(APIEnums.Units.METRES, matrixLocationsListRequest.getUnits());
    }

    @Test
    public void setUnitsTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        bareMatrixRequest.setUnits(APIEnums.Units.KILOMETRES);
        matrixLocationsRequest.setUnits(APIEnums.Units.KILOMETRES);
        matrixLocationsListRequest.setUnits(APIEnums.Units.KILOMETRES);
        Assert.assertEquals(APIEnums.Units.KILOMETRES, bareMatrixRequest.getUnits());
        Assert.assertEquals(APIEnums.Units.KILOMETRES, matrixLocationsRequest.getUnits());
        Assert.assertEquals(APIEnums.Units.KILOMETRES, matrixLocationsListRequest.getUnits());
    }

    @Test
    public void isOptimizedTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        Assert.assertFalse(bareMatrixRequest.isOptimized());
        Assert.assertFalse(matrixLocationsRequest.isOptimized());
        Assert.assertFalse(matrixLocationsListRequest.isOptimized());
    }

    @Test
    public void setOptimizedTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        bareMatrixRequest.setOptimized(true);
        matrixLocationsRequest.setOptimized(true);
        matrixLocationsListRequest.setOptimized(true);
        Assert.assertTrue(bareMatrixRequest.isOptimized());
        Assert.assertTrue(matrixLocationsRequest.isOptimized());
        Assert.assertTrue(matrixLocationsListRequest.isOptimized());

    }

    @Test
    public void getResponseTypeTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        Assert.assertNull(bareMatrixRequest.getResponseType());
        Assert.assertNull(matrixLocationsRequest.getResponseType());
        Assert.assertNull(matrixLocationsListRequest.getResponseType());
    }

    @Test
    public void setResponseTypeTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        bareMatrixRequest.setResponseType(APIEnums.MatrixResponseType.JSON);
        matrixLocationsRequest.setResponseType(APIEnums.MatrixResponseType.JSON);
        matrixLocationsListRequest.setResponseType(APIEnums.MatrixResponseType.JSON);
        Assert.assertEquals(APIEnums.MatrixResponseType.JSON, bareMatrixRequest.getResponseType());
        Assert.assertEquals(APIEnums.MatrixResponseType.JSON, matrixLocationsRequest.getResponseType());
        Assert.assertEquals(APIEnums.MatrixResponseType.JSON, matrixLocationsListRequest.getResponseType());
    }

    @Test
    public void getWeightingMethodTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        Assert.assertNull(bareMatrixRequest.getWeightingMethod());
        Assert.assertNull(matrixLocationsRequest.getWeightingMethod());
        Assert.assertNull(matrixLocationsListRequest.getWeightingMethod());
    }

    @Test
    public void setWeightingMethodTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        bareMatrixRequest.setWeightingMethod("foo");
        matrixLocationsRequest.setWeightingMethod("foo");
        matrixLocationsListRequest.setWeightingMethod("foo");
        Assert.assertEquals("foo", bareMatrixRequest.getWeightingMethod());
        Assert.assertEquals("foo", matrixLocationsRequest.getWeightingMethod());
        Assert.assertEquals("foo", matrixLocationsListRequest.getWeightingMethod());
    }

    @Test
    public void getAlgorithmTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        Assert.assertNull(bareMatrixRequest.getAlgorithm());
        Assert.assertNull(matrixLocationsRequest.getAlgorithm());
        Assert.assertNull(matrixLocationsListRequest.getAlgorithm());
    }

    @Test
    public void setAlgorithmTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        bareMatrixRequest.setAlgorithm("foo");
        matrixLocationsRequest.setAlgorithm("foo");
        matrixLocationsListRequest.setAlgorithm("foo");
        Assert.assertEquals("foo", bareMatrixRequest.getAlgorithm());
        Assert.assertEquals("foo", matrixLocationsRequest.getAlgorithm());
        Assert.assertEquals("foo", matrixLocationsListRequest.getAlgorithm());
    }

    @Test
    public void hasValidSourceIndexTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        Assert.assertTrue(bareMatrixRequest.hasValidSourceIndex());
        Assert.assertTrue(matrixLocationsRequest.hasValidSourceIndex());
        Assert.assertTrue(matrixLocationsListRequest.hasValidSourceIndex());
        bareMatrixRequest.setSources(new String[]{"foo"});
        matrixLocationsRequest.setSources(new String[]{"foo"});
        matrixLocationsListRequest.setSources(new String[]{"foo"});
        Assert.assertFalse(bareMatrixRequest.hasValidSourceIndex());
        Assert.assertFalse(matrixLocationsRequest.hasValidSourceIndex());
        Assert.assertFalse(matrixLocationsListRequest.hasValidSourceIndex());
    }

    @Test
    public void hasValidDestinationIndexTest() throws ParameterValueException {
        bareMatrixRequest = new MatrixRequest();
        matrixLocationsRequest = new MatrixRequest(bareCoordinates);
        matrixLocationsListRequest = new MatrixRequest(listOfBareCoordinatesList);
        Assert.assertTrue(bareMatrixRequest.hasValidDestinationIndex());
        Assert.assertTrue(matrixLocationsRequest.hasValidDestinationIndex());
        Assert.assertTrue(matrixLocationsListRequest.hasValidDestinationIndex());
        bareMatrixRequest.setDestinations(new String[]{"foo"});
        matrixLocationsRequest.setDestinations(new String[]{"foo"});
        matrixLocationsListRequest.setDestinations(new String[]{"foo"});
        Assert.assertFalse(bareMatrixRequest.hasValidDestinationIndex());
        Assert.assertFalse(matrixLocationsRequest.hasValidDestinationIndex());
        Assert.assertFalse(matrixLocationsListRequest.hasValidDestinationIndex());
    }
}