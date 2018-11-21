package heigit.ors.api.responses.matrix.JSONMatrixResponseObjects;

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.api.requests.matrix.MatrixRequest;
import heigit.ors.api.requests.matrix.MatrixRequestHandler;
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
    private JSONMatrixResponse jsonMatrixWeightsResponse;

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
        Coordinate coordinate = new Coordinate(8.681495, 49.41461);
        ResolvedLocation resolvedLocation = new ResolvedLocation(coordinate, "foo", 0.0);
        ResolvedLocation[] resolvedLocations = new ResolvedLocation[1];
        resolvedLocations[0] = resolvedLocation;

        MatrixResult matrixResult = new MatrixResult(resolvedLocations, resolvedLocations);
        matrixResult.setTable(MatrixMetricsType.Duration, new float[]{0, 1});
        matrixResult.setTable(MatrixMetricsType.Distance, new float[]{0, 1});
        matrixResult.setTable(MatrixMetricsType.Weight, new float[]{0, 1});
        List<MatrixResult> matrixResults = new ArrayList<>();
        matrixResults.add(matrixResult);


        MatrixRequest springMatrixDurationsRequest = new MatrixRequest(bareCoordinates);
        springMatrixDurationsRequest.setProfile(APIEnums.Profile.DRIVING_CAR);
        MatrixRequest springMatrixDistancesRequest = new MatrixRequest(bareCoordinates);
        springMatrixDistancesRequest.setProfile(APIEnums.Profile.DRIVING_CAR);
        springMatrixDistancesRequest.setMetrics(new String[]{"distance"});
        MatrixRequest springMatrixWeightsRequest = new MatrixRequest(bareCoordinates);
        springMatrixWeightsRequest.setProfile(APIEnums.Profile.DRIVING_CAR);
        springMatrixWeightsRequest.setMetrics(new String[]{"weight"});


        List<heigit.ors.matrix.MatrixRequest> matrixDurationsRequests = MatrixRequestHandler.convertMatrixRequest(springMatrixDurationsRequest);
        List<heigit.ors.matrix.MatrixRequest> matrixDistancesRequests = MatrixRequestHandler.convertMatrixRequest(springMatrixDistancesRequest);
        List<heigit.ors.matrix.MatrixRequest> matrixWeightsRequests = MatrixRequestHandler.convertMatrixRequest(springMatrixWeightsRequest);

        jsonMatrixDurationsResponse = new JSONMatrixResponse(matrixResults, matrixDurationsRequests, springMatrixDurationsRequest);

        jsonMatrixDistancesResponse = new JSONMatrixResponse(matrixResults, matrixDistancesRequests, springMatrixDistancesRequest);
        jsonMatrixWeightsResponse = new JSONMatrixResponse(matrixResults, matrixWeightsRequests, springMatrixWeightsRequest);

    }

    @Test
    public void getRoutes() {
        JSONIndividualMatrixResponse[] durationsRoutes = jsonMatrixDurationsResponse.getRoutes();
        Assert.assertNotNull(durationsRoutes[0].getDurations());
        Assert.assertNull(durationsRoutes[0].getDistances());
        Assert.assertNull(durationsRoutes[0].getWeights());
        Assert.assertEquals(1, durationsRoutes[0].getDestinations().size());
        Assert.assertEquals(1, durationsRoutes[0].getSources().size());
        Assert.assertEquals(8.681495, durationsRoutes[0].getSources().get(0).location.x, 0);
        Assert.assertEquals(49.41461, durationsRoutes[0].getSources().get(0).location.y, 0);
        Assert.assertEquals(Double.NaN, durationsRoutes[0].getSources().get(0).location.z, 0);
        Assert.assertNull(durationsRoutes[0].getSources().get(0).name);
        Assert.assertEquals(0.0, durationsRoutes[0].getSources().get(0).getSnapped_distance(), 0);

        JSONIndividualMatrixResponse[] distancesRoutes = jsonMatrixDistancesResponse.getRoutes();
        Assert.assertNotNull(distancesRoutes[0].getDistances());
        Assert.assertNull(distancesRoutes[0].getDurations());
        Assert.assertNull(distancesRoutes[0].getWeights());
        Assert.assertEquals(1, distancesRoutes[0].getDestinations().size());
        Assert.assertEquals(1, distancesRoutes[0].getSources().size());
        Assert.assertEquals(8.681495, distancesRoutes[0].getSources().get(0).location.x, 0);
        Assert.assertEquals(49.41461, distancesRoutes[0].getSources().get(0).location.y, 0);
        Assert.assertEquals(Double.NaN, distancesRoutes[0].getSources().get(0).location.z, 0);
        Assert.assertNull(distancesRoutes[0].getSources().get(0).name);
        Assert.assertEquals(0.0, distancesRoutes[0].getSources().get(0).getSnapped_distance(), 0);


        JSONIndividualMatrixResponse[] weightsRoutes = jsonMatrixWeightsResponse.getRoutes();
        Assert.assertNotNull(weightsRoutes[0].getWeights());
        Assert.assertNull(weightsRoutes[0].getDurations());
        Assert.assertNull(weightsRoutes[0].getDistances());
        Assert.assertEquals(1, weightsRoutes[0].getDestinations().size());
        Assert.assertEquals(1, weightsRoutes[0].getSources().size());
        Assert.assertEquals(8.681495, weightsRoutes[0].getSources().get(0).location.x, 0);
        Assert.assertEquals(49.41461, weightsRoutes[0].getSources().get(0).location.y, 0);
        Assert.assertEquals(Double.NaN, weightsRoutes[0].getSources().get(0).location.z, 0);
        Assert.assertNull(weightsRoutes[0].getSources().get(0).name);
        Assert.assertEquals(0.0, weightsRoutes[0].getSources().get(0).getSnapped_distance(), 0);
    }

    @Test
    public void getInfo() {
        Assert.assertEquals(MatrixResponseInfo.class, jsonMatrixDurationsResponse.getInfo().getClass());
        Assert.assertNotNull(jsonMatrixDurationsResponse.getInfo().getEngineInfo());
        Assert.assertNotNull(jsonMatrixDurationsResponse.getInfo().getAttribution());
        Assert.assertNotNull(jsonMatrixDurationsResponse.getInfo().getRequest());
        Assert.assertNotNull(jsonMatrixDurationsResponse.getInfo().getService());
        Assert.assertTrue(jsonMatrixDurationsResponse.getInfo().getTimeStamp() > 0);
        Assert.assertEquals(MatrixResponseInfo.class, jsonMatrixDistancesResponse.getInfo().getClass());
        Assert.assertNotNull(jsonMatrixDistancesResponse.getInfo().getEngineInfo());
        Assert.assertNotNull(jsonMatrixDistancesResponse.getInfo().getAttribution());
        Assert.assertNotNull(jsonMatrixDistancesResponse.getInfo().getRequest());
        Assert.assertNotNull(jsonMatrixDistancesResponse.getInfo().getService());
        Assert.assertTrue(jsonMatrixDistancesResponse.getInfo().getTimeStamp() > 0);
        Assert.assertEquals(MatrixResponseInfo.class, jsonMatrixWeightsResponse.getInfo().getClass());
        Assert.assertNotNull(jsonMatrixWeightsResponse.getInfo().getEngineInfo());
        Assert.assertNotNull(jsonMatrixWeightsResponse.getInfo().getAttribution());
        Assert.assertNotNull(jsonMatrixWeightsResponse.getInfo().getRequest());
        Assert.assertNotNull(jsonMatrixWeightsResponse.getInfo().getService());
        Assert.assertTrue(jsonMatrixWeightsResponse.getInfo().getTimeStamp() > 0);
    }
}