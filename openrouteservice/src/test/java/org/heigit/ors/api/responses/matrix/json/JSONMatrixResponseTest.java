package org.heigit.ors.api.responses.matrix.json;

import org.heigit.ors.api.requests.common.APIEnums;
import org.heigit.ors.api.requests.matrix.MatrixRequest;
import org.heigit.ors.api.requests.matrix.MatrixRequestEnums;
import org.heigit.ors.api.responses.matrix.MatrixResponseInfo;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.matrix.MatrixMetricsType;
import org.heigit.ors.matrix.MatrixResult;
import org.heigit.ors.matrix.ResolvedLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import static org.junit.jupiter.api.Assertions.*;

class JSONMatrixResponseTest {
    private final Double[][] bareCoordinates = new Double[3][];
    private final Double[] bareCoordinate1 = new Double[2];
    private final Double[] bareCoordinate2 = new Double[2];
    private final Double[] bareCoordinate3 = new Double[2];
    private JSONMatrixResponse jsonMatrixDurationsResponse;
    private JSONMatrixResponse jsonMatrixDistancesResponse;
    private JSONMatrixResponse jsonMatrixCombinedResponse;

    @BeforeEach
    void setUp() throws StatusCodeException {

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
        matrixResultCombined.setTable(MatrixMetricsType.DURATION, new float[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        matrixResultCombined.setTable(MatrixMetricsType.DISTANCE, new float[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});

        matrixResultDistance = new MatrixResult(resolvedLocations, resolvedLocations);
        matrixResultDistance.setTable(MatrixMetricsType.DISTANCE, new float[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});

        matrixResultDuration = new MatrixResult(resolvedLocations, resolvedLocations);
        matrixResultDuration.setTable(MatrixMetricsType.DURATION, new float[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});


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
    void getMatrixDurationsResponse() {
        JSONIndividualMatrixResponse durationMatrix = jsonMatrixDurationsResponse.getMatrix();
        assertNotNull(durationMatrix.getDurations());
        assertNull(durationMatrix.getDistances());
        assertEquals(3, durationMatrix.getDestinations().size());
        assertEquals(3, durationMatrix.getSources().size());
        assertEquals(8.681495, durationMatrix.getSources().get(0).location.x, 0);
        assertEquals(49.41461, durationMatrix.getSources().get(0).location.y, 0);
        assertEquals(Double.NaN, durationMatrix.getSources().get(0).location.z, 0);
        assertNotNull(durationMatrix.getSources().get(0).name);
        assertEquals(0.0, durationMatrix.getSources().get(0).getSnappedDistance(), 0);
    }

    @Test
    void getMatrixDistancesResponse() {
        JSONIndividualMatrixResponse distanceMatrix = jsonMatrixDistancesResponse.getMatrix();
        assertNotNull(distanceMatrix.getDistances());
        assertNull(distanceMatrix.getDurations());
        assertEquals(3, distanceMatrix.getDestinations().size());
        assertEquals(3, distanceMatrix.getSources().size());
        assertEquals(8.681495, distanceMatrix.getSources().get(0).location.x, 0);
        assertEquals(49.41461, distanceMatrix.getSources().get(0).location.y, 0);
        assertEquals(Double.NaN, distanceMatrix.getSources().get(0).location.z, 0);
        assertNull(distanceMatrix.getSources().get(0).name);
        assertEquals(0.0, distanceMatrix.getSources().get(0).getSnappedDistance(), 0);
    }

    @Test
    void getMatrixCombinedResponse() {
        JSONIndividualMatrixResponse combinedMatrix = jsonMatrixCombinedResponse.getMatrix();
        assertNotNull(combinedMatrix.getDistances());
        assertNotNull(combinedMatrix.getDurations());
        assertEquals(3, combinedMatrix.getDestinations().size());
        assertEquals(3, combinedMatrix.getSources().size());
        assertEquals(8.681495, combinedMatrix.getSources().get(0).location.x, 0);
        assertEquals(49.41461, combinedMatrix.getSources().get(0).location.y, 0);
        assertEquals(Double.NaN, combinedMatrix.getSources().get(0).location.z, 0);
        assertNotNull(combinedMatrix.getSources().get(0).name);
        assertEquals(0.0, combinedMatrix.getSources().get(0).getSnappedDistance(), 0);
    }

    @Test
    void getInfo() {
        assertEquals(MatrixResponseInfo.class, jsonMatrixDurationsResponse.getInfo().getClass());
        assertNotNull(jsonMatrixDurationsResponse.getInfo().getEngineInfo());
        assertNotNull(jsonMatrixDurationsResponse.getInfo().getAttribution());
        assertNotNull(jsonMatrixDurationsResponse.getInfo().getRequest());
        assertNotNull(jsonMatrixDurationsResponse.getInfo().getService());
        assertTrue(jsonMatrixDurationsResponse.getInfo().getTimeStamp() > 0);
    }
}
