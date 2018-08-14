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
    List<heigit.ors.matrix.MatrixRequest> matrixDurationsRequests;
    List<heigit.ors.matrix.MatrixRequest> matrixDistancesRequests;
    List<heigit.ors.matrix.MatrixRequest> matrixWeightsRequests;
    private MatrixResult matrixResult;
    private Double[][] bareCoordinates = new Double[3][];
    private Double[] bareCoordinate1 = new Double[2];
    private Double[] bareCoordinate2 = new Double[2];
    private Double[] bareCoordinate3 = new Double[2];
    private JSONIndividualMatrixResponse durationsMatrixResponse;
    private JSONIndividualMatrixResponse distancesMatrixResponse;

    @Before
    public void setUp() throws StatusCodeException {


        bareCoordinate1[0] = 8.681495;
        bareCoordinate1[1] = 49.41461;
        bareCoordinate2[0] = 8.686507;
        bareCoordinate2[1] = 49.41943;
        bareCoordinate3[0] = 8.687872;
        bareCoordinate3[1] = 49.420318;
        bareCoordinates[0] = bareCoordinate1;
        bareCoordinates[1] = bareCoordinate2;
        bareCoordinates[2] = bareCoordinate3;

        Coordinate coordinate = new Coordinate(8.681495, 49.41461);
        ResolvedLocation resolvedLocation = new ResolvedLocation(coordinate, "foo", 0.0);
        ResolvedLocation[] resolvedLocations = new ResolvedLocation[1];
        resolvedLocations[0] = resolvedLocation;

        matrixResult = new MatrixResult(resolvedLocations, resolvedLocations);
        matrixResult.setTable(MatrixMetricsType.Duration, new float[]{0, 1});
        matrixResult.setTable(MatrixMetricsType.Distance, new float[]{0, 1});
        matrixResult.setTable(MatrixMetricsType.Weight, new float[]{0, 1});
        List<MatrixResult> matrixResults = new ArrayList<>();
        matrixResults.add(matrixResult);

        MatrixRequest springMatrixDurationsRequest = new MatrixRequest(bareCoordinates);
        springMatrixDurationsRequest.setProfile(APIEnums.Profile.DRIVING_CAR);
        MatrixRequest springMatrixDistancesRequest = new MatrixRequest(bareCoordinates);
        springMatrixDistancesRequest.setProfile(APIEnums.Profile.DRIVING_CAR);
        springMatrixDistancesRequest.setMetrics(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DISTANCE});
        MatrixRequest springMatrixWeightsRequest = new MatrixRequest(bareCoordinates);
        springMatrixWeightsRequest.setProfile(APIEnums.Profile.DRIVING_CAR);
        matrixDurationsRequests = MatrixRequestHandler.convertMatrixRequest(springMatrixDurationsRequest);
        matrixDistancesRequests = MatrixRequestHandler.convertMatrixRequest(springMatrixDistancesRequest);
        matrixWeightsRequests = MatrixRequestHandler.convertMatrixRequest(springMatrixWeightsRequest);
        durationsMatrixResponse = new JSONIndividualMatrixResponse(matrixResult, matrixDurationsRequests.get(0));
        distancesMatrixResponse = new JSONIndividualMatrixResponse(matrixResult, matrixDistancesRequests.get(0));
    }


    @Test
    public void getDistances() {
        Assert.assertNull(durationsMatrixResponse.getDistances());
        Assert.assertArrayEquals(new Double[]{0.0, 1.0}, distancesMatrixResponse.getDistances());
    }

    @Test
    public void setDistances() {
        distancesMatrixResponse.setDistances(new Double[]{1.0, 2.0, 3.0});
        Assert.assertEquals(3, distancesMatrixResponse.getDistances().length);
        Assert.assertArrayEquals(new Double[]{1.0, 2.0, 3.0}, distancesMatrixResponse.getDistances());
        Assert.assertNull(durationsMatrixResponse.getDistances());
    }

    @Test
    public void getDurations() {
        Assert.assertEquals(2, durationsMatrixResponse.getDurations().length);
        Assert.assertArrayEquals(new Double[]{0.0, 1.0}, durationsMatrixResponse.getDurations());
        Assert.assertNull(distancesMatrixResponse.getDurations());
    }

    @Test
    public void setDurations() {
        durationsMatrixResponse.setDurations(new Double[]{1.0, 2.0, 3.0});
        Assert.assertEquals(3, durationsMatrixResponse.getDurations().length);
        Assert.assertArrayEquals(new Double[]{1.0, 2.0, 3.0}, durationsMatrixResponse.getDurations());
        Assert.assertNull(distancesMatrixResponse.getDurations());

    }

    @Test
    public void getDestinations() {
        Assert.assertEquals(1, durationsMatrixResponse.getDestinations().size());
        Assert.assertArrayEquals(new Double[]{8.681495, 49.41461}, durationsMatrixResponse.getDestinations().get(0).getLocation());
        Assert.assertEquals(1, distancesMatrixResponse.getDestinations().size());
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
        Assert.assertEquals(1, durationsMatrixResponse.getSources().size());
        Assert.assertArrayEquals(new Double[]{8.681495, 49.41461}, durationsMatrixResponse.getSources().get(0).getLocation());
        Assert.assertEquals(1, distancesMatrixResponse.getSources().size());
        Assert.assertArrayEquals(new Double[]{8.681495, 49.41461}, distancesMatrixResponse.getSources().get(0).getLocation());
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