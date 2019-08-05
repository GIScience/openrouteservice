package heigit.ors.api.responses.matrix.JSONMatrixResponseObjects;

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.api.requests.matrix.MatrixRequest;
import heigit.ors.api.requests.matrix.MatrixRequestEnums;
import heigit.ors.api.requests.matrix.MatrixRequestHandler;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.matrix.MatrixResult;
import heigit.ors.matrix.ResolvedLocation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class JSONIndividualMatrixResponseTest {
    private Double[][] bareCoordinates = new Double[3][];
    private Double[] bareCoordinate1 = new Double[2];
    private Double[] bareCoordinate2 = new Double[2];
    private Double[] bareCoordinate3 = new Double[2];
    private JSONIndividualMatrixResponse durationsMatrixResponse;
    private JSONIndividualMatrixResponse distancesMatrixResponse;
    private JSONIndividualMatrixResponse combinedMatrixResponse;

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
        apiRequestDistance.setResolveLocations(true);

        MatrixRequest apiRequestCombined = new MatrixRequest(bareCoordinates);
        apiRequestCombined.setProfile(APIEnums.Profile.DRIVING_CAR);
        apiRequestCombined.setMetrics(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DISTANCE, MatrixRequestEnums.Metrics.DURATION});
        apiRequestCombined.setResolveLocations(true);

        durationsMatrixResponse = new JSONIndividualMatrixResponse(matrixResultDuration, apiRequestDuration);
        distancesMatrixResponse = new JSONIndividualMatrixResponse(matrixResultDistance, apiRequestDistance);
        combinedMatrixResponse = new JSONIndividualMatrixResponse(matrixResultCombined, apiRequestCombined);
    }


    @Test
    public void getDistances() {
        Assert.assertNull(durationsMatrixResponse.getDistances());
        Assert.assertArrayEquals(new Double[]{0.0, 1.0, 2.0}, distancesMatrixResponse.getDistances()[0]);

        Assert.assertArrayEquals(new Double[]{3.0,4.0,5.0}, combinedMatrixResponse.getDistances()[1]);
    }

    @Test
    public void setDistances() {
        distancesMatrixResponse.setDistances(new Double[][]{{1.0, 2.0, 3.0},{1.0, 2.0, 3.0},{1.0, 2.0, 3.0}});
        Assert.assertEquals(3, distancesMatrixResponse.getDistances().length);
        Assert.assertArrayEquals(new Double[]{1.0, 2.0, 3.0}, distancesMatrixResponse.getDistances()[0]);
        Assert.assertNull(durationsMatrixResponse.getDistances());
    }

    @Test
    public void getDurations() {
        Assert.assertEquals(3, durationsMatrixResponse.getDurations().length);
        Assert.assertArrayEquals(new Double[]{0.0, 1.0, 2.0}, durationsMatrixResponse.getDurations()[0]);
        Assert.assertNull(distancesMatrixResponse.getDurations());

        Assert.assertArrayEquals(new Double[]{3.0,4.0,5.0}, combinedMatrixResponse.getDurations()[1]);
    }

    @Test
    public void setDurations() {
        durationsMatrixResponse.setDurations(new Double[][]{{1.0, 2.0, 3.0},{1.0, 2.0, 3.0},{1.0, 2.0, 3.0}});
        Assert.assertEquals(3, durationsMatrixResponse.getDurations().length);
        Assert.assertArrayEquals(new Double[]{1.0, 2.0, 3.0}, durationsMatrixResponse.getDurations()[0]);
        Assert.assertNull(distancesMatrixResponse.getDurations());

    }

    @Test
    public void getDestinations() {
        Assert.assertEquals(3, distancesMatrixResponse.getDestinations().size());
        Assert.assertArrayEquals(new Double[]{8.681495, 49.41461}, distancesMatrixResponse.getDestinations().get(0).getLocation());
    }

    @Test
    public void setDestinations() {
        Coordinate coordinate = new Coordinate(9.681495, 50.41461);
        ResolvedLocation resolvedLocation = new ResolvedLocation(coordinate, "foo", 0.0);


        List<JSON2DDestinations> json2DDestinations = new ArrayList<>();
        JSON2DDestinations json2DDestination = new JSON2DDestinations(resolvedLocation, false);
        json2DDestinations.add(json2DDestination);

        durationsMatrixResponse.setDestinations(json2DDestinations);
        distancesMatrixResponse.setDestinations(json2DDestinations);

        Assert.assertEquals(1, durationsMatrixResponse.getDestinations().size());
        Assert.assertArrayEquals(new Double[]{9.681495, 50.41461}, durationsMatrixResponse.getDestinations().get(0).getLocation());
        Assert.assertEquals(1, distancesMatrixResponse.getDestinations().size());
        Assert.assertArrayEquals(new Double[]{9.681495, 50.41461}, distancesMatrixResponse.getDestinations().get(0).getLocation());

    }

    @Test
    public void getSources() {
        Assert.assertEquals(3, durationsMatrixResponse.getSources().size());
        Assert.assertArrayEquals(new Double[]{8.681495, 49.41461}, durationsMatrixResponse.getSources().get(0).getLocation());
    }

    @Test
    public void setSources() {
        Coordinate coordinate = new Coordinate(9.681495, 50.41461);
        ResolvedLocation resolvedLocation = new ResolvedLocation(coordinate, "foo", 0.0);


        List<JSON2DSources> json2DSources = new ArrayList<>();
        JSON2DSources json2DSource = new JSON2DSources(resolvedLocation, false);
        json2DSources.add(json2DSource);

        durationsMatrixResponse.setSources(json2DSources);
        distancesMatrixResponse.setSources(json2DSources);

        Assert.assertEquals(1, durationsMatrixResponse.getSources().size());
        Assert.assertArrayEquals(new Double[]{9.681495, 50.41461}, durationsMatrixResponse.getSources().get(0).getLocation());
        Assert.assertEquals(1, distancesMatrixResponse.getSources().size());
        Assert.assertArrayEquals(new Double[]{9.681495, 50.41461}, distancesMatrixResponse.getSources().get(0).getLocation());

    }
}