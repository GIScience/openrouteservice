package heigit.ors.api.responses.matrix.JSONMatrixResponseObjects;

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.api.requests.matrix.MatrixRequest;
import heigit.ors.api.requests.matrix.MatrixRequestEnums;
import heigit.ors.api.responses.matrix.MatrixResponseInfo;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.matrix.MatrixResult;
import heigit.ors.matrix.ResolvedLocation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class JSONMatrixResponseTest {
    private Double[][] bareCoordinates = new Double[3][];
    private Double[] bareCoordinate1 = new Double[2];
    private Double[] bareCoordinate2 = new Double[2];
    private Double[] bareCoordinate3 = new Double[2];
    private JSONMatrixResponse jsonMatrixDurationsResponse;
    private JSONMatrixResponse jsonMatrixDistancesResponse;
    private JSONMatrixResponse jsonMatrixCombinedResponse;

    @Before
    public void setUp() throws StatusCodeException {
        MatrixResult matrixResultCombined;
        MatrixResult matrixResultDistance;
        MatrixResult matrixResultDuration;

        bareCoordinate1[0] = 8.681495;
        bareCoordinate1[1] = 49.41461;
        bareCoordinate2[0] = 8.686507;
        bareCoordinate2[1] = 49.41943;
        bareCoordinate3[0] = 8.687872;
        bareCoordinate3[1] = 49.420318;
        bareCoordinates[0] = bareCoordinate1;
        bareCoordinates[1] = bareCoordinate2;
        bareCoordinates[2] = bareCoordinate3;

        List<Double> bareCoordinatesList = new ArrayList<>();
        bareCoordinatesList.add(8.681495);
        bareCoordinatesList.add(49.41461);
        List<List<Double>> listOfBareCoordinatesList = new ArrayList<>();
        listOfBareCoordinatesList.add(bareCoordinatesList);
        bareCoordinatesList = new ArrayList<>();
        bareCoordinatesList.add(8.686507);
        bareCoordinatesList.add(49.41943);
        listOfBareCoordinatesList.add(bareCoordinatesList);
        bareCoordinatesList = new ArrayList<>();
        bareCoordinatesList.add(8.687872);
        bareCoordinatesList.add(49.420318);
        listOfBareCoordinatesList.add(bareCoordinatesList);

        Coordinate coordinate1 = new Coordinate(8.681495, 49.41461);
        ResolvedLocation resolvedLocation1 = new ResolvedLocation(coordinate1, "foo", 0.0);
        Coordinate coordinate2 = new Coordinate(8.686507, 49.41943);
        ResolvedLocation resolvedLocation2 = new ResolvedLocation(coordinate2, "foo", 0.0);
        Coordinate coordinate3 = new Coordinate(8.687872, 49.420318);
        ResolvedLocation resolvedLocation3 = new ResolvedLocation(coordinate3, "foo", 0.0);
        ResolvedLocation[] resolvedLocations = new ResolvedLocation[3];
        resolvedLocations[0] = resolvedLocation1;
        resolvedLocations[1] = resolvedLocation2;
        resolvedLocations[2] = resolvedLocation3;

        matrixResultCombined = new MatrixResult(resolvedLocations, resolvedLocations);
        matrixResultCombined.setTable(MatrixMetricsType.Duration, new float[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        matrixResultCombined.setTable(MatrixMetricsType.Distance, new float[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});

        matrixResultDistance = new MatrixResult(resolvedLocations, resolvedLocations);
        matrixResultDistance.setTable(MatrixMetricsType.Distance, new float[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});

        matrixResultDuration = new MatrixResult(resolvedLocations, resolvedLocations);
        matrixResultDuration.setTable(MatrixMetricsType.Duration, new float[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});


        MatrixRequest apiRequestDuration = new MatrixRequest(bareCoordinates);
        apiRequestDuration.setProfile(APIEnums.Profile.DRIVING_CAR);
        apiRequestDuration.setMetrics(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DURATION});
        apiRequestDuration.setResolveLocations(true);

        MatrixRequest apiRequestDistance = new MatrixRequest(bareCoordinates);
        apiRequestDistance.setProfile(APIEnums.Profile.DRIVING_CAR);
        apiRequestDistance.setMetrics(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DISTANCE});
        apiRequestDistance.setResolveLocations(false);

        MatrixRequest apiRequestCombined = new MatrixRequest(bareCoordinates);
        apiRequestCombined.setProfile(APIEnums.Profile.DRIVING_CAR);
        apiRequestCombined.setMetrics(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DISTANCE, MatrixRequestEnums.Metrics.DURATION});
        apiRequestCombined.setResolveLocations(true);

        jsonMatrixDurationsResponse = new JSONMatrixResponse(matrixResultDuration, apiRequestDuration);

        jsonMatrixDistancesResponse = new JSONMatrixResponse(matrixResultDistance, apiRequestDistance);

        jsonMatrixCombinedResponse = new JSONMatrixResponse(matrixResultCombined, apiRequestCombined);
    }

    @Test
    public void getMatrix() {
        JSONIndividualMatrixResponse durationMatrix = jsonMatrixDurationsResponse.getMatrix();
        Assert.assertNotNull(durationMatrix.getDurations());
        Assert.assertNull(durationMatrix.getDistances());
        Assert.assertEquals(3, durationMatrix.getDestinations().size());
        Assert.assertEquals(3, durationMatrix.getSources().size());
        Assert.assertEquals(8.681495, durationMatrix.getSources().get(0).location.x, 0);
        Assert.assertEquals(49.41461, durationMatrix.getSources().get(0).location.y, 0);
        Assert.assertEquals(Double.NaN, durationMatrix.getSources().get(0).location.z, 0);
        Assert.assertNotNull(durationMatrix.getSources().get(0).name);
        Assert.assertEquals(0.0, durationMatrix.getSources().get(0).getSnappedDistance(), 0);

        JSONIndividualMatrixResponse distanceMatrix = jsonMatrixDistancesResponse.getMatrix();
        Assert.assertNotNull(distanceMatrix.getDistances());
        Assert.assertNull(distanceMatrix.getDurations());
        Assert.assertEquals(3, distanceMatrix.getDestinations().size());
        Assert.assertEquals(3, distanceMatrix.getSources().size());
        Assert.assertEquals(8.681495, distanceMatrix.getSources().get(0).location.x, 0);
        Assert.assertEquals(49.41461, distanceMatrix.getSources().get(0).location.y, 0);
        Assert.assertEquals(Double.NaN, distanceMatrix.getSources().get(0).location.z, 0);
        Assert.assertNull(distanceMatrix.getSources().get(0).name);
        Assert.assertEquals(0.0, distanceMatrix.getSources().get(0).getSnappedDistance(), 0);


        JSONIndividualMatrixResponse combinedMatrix = jsonMatrixCombinedResponse.getMatrix();
        Assert.assertNotNull(combinedMatrix.getDistances());
        Assert.assertNotNull(combinedMatrix.getDurations());
        Assert.assertEquals(3, combinedMatrix.getDestinations().size());
        Assert.assertEquals(3, combinedMatrix.getSources().size());
        Assert.assertEquals(8.681495, combinedMatrix.getSources().get(0).location.x, 0);
        Assert.assertEquals(49.41461, combinedMatrix.getSources().get(0).location.y, 0);
        Assert.assertEquals(Double.NaN, combinedMatrix.getSources().get(0).location.z, 0);
        Assert.assertNotNull(combinedMatrix.getSources().get(0).name);
        Assert.assertEquals(0.0, combinedMatrix.getSources().get(0).getSnappedDistance(), 0);
    }

    @Test
    public void getInfo() {
        Assert.assertEquals(MatrixResponseInfo.class, jsonMatrixDurationsResponse.getInfo().getClass());
        Assert.assertNotNull(jsonMatrixDurationsResponse.getInfo().getEngineInfo());
        Assert.assertNotNull(jsonMatrixDurationsResponse.getInfo().getAttribution());
        Assert.assertNotNull(jsonMatrixDurationsResponse.getInfo().getRequest());
        Assert.assertNotNull(jsonMatrixDurationsResponse.getInfo().getService());
        Assert.assertTrue(jsonMatrixDurationsResponse.getInfo().getTimeStamp() > 0);
    }
}