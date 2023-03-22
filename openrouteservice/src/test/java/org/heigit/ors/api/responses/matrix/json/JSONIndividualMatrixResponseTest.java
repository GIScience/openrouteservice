package org.heigit.ors.api.responses.matrix.json;

import org.heigit.ors.api.requests.common.APIEnums;
import org.heigit.ors.api.requests.matrix.MatrixRequest;
import org.heigit.ors.api.requests.matrix.MatrixRequestEnums;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.matrix.MatrixMetricsType;
import org.heigit.ors.matrix.MatrixResult;
import org.heigit.ors.matrix.ResolvedLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JSONIndividualMatrixResponseTest {
    private final Double[][] bareCoordinates = new Double[3][];
    private final Double[] bareCoordinate1 = new Double[2];
    private final Double[] bareCoordinate2 = new Double[2];
    private final Double[] bareCoordinate3 = new Double[2];
    private JSONIndividualMatrixResponse durationsMatrixResponse;
    private JSONIndividualMatrixResponse distancesMatrixResponse;
    private JSONIndividualMatrixResponse combinedMatrixResponse;

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
    void getDistances() {
        assertNull(durationsMatrixResponse.getDistances());
        assertArrayEquals(new Double[]{0.0, 1.0, 2.0}, distancesMatrixResponse.getDistances()[0]);

        assertArrayEquals(new Double[]{3.0,4.0,5.0}, combinedMatrixResponse.getDistances()[1]);
    }

    @Test
    void setDistances() {
        distancesMatrixResponse.setDistances(new Double[][]{{1.0, 2.0, 3.0},{1.0, 2.0, 3.0},{1.0, 2.0, 3.0}});
        assertEquals(3, distancesMatrixResponse.getDistances().length);
        assertArrayEquals(new Double[]{1.0, 2.0, 3.0}, distancesMatrixResponse.getDistances()[0]);
        assertNull(durationsMatrixResponse.getDistances());
    }

    @Test
    void getDurations() {
        assertEquals(3, durationsMatrixResponse.getDurations().length);
        assertArrayEquals(new Double[]{0.0, 1.0, 2.0}, durationsMatrixResponse.getDurations()[0]);
        assertNull(distancesMatrixResponse.getDurations());

        assertArrayEquals(new Double[]{3.0,4.0,5.0}, combinedMatrixResponse.getDurations()[1]);
    }

    @Test
    void setDurations() {
        durationsMatrixResponse.setDurations(new Double[][]{{1.0, 2.0, 3.0},{1.0, 2.0, 3.0},{1.0, 2.0, 3.0}});
        assertEquals(3, durationsMatrixResponse.getDurations().length);
        assertArrayEquals(new Double[]{1.0, 2.0, 3.0}, durationsMatrixResponse.getDurations()[0]);
        assertNull(distancesMatrixResponse.getDurations());

    }

    @Test
    void getDestinations() {
        assertEquals(3, distancesMatrixResponse.getDestinations().size());
        assertArrayEquals(new Double[]{8.681495, 49.41461}, distancesMatrixResponse.getDestinations().get(0).getLocation());
    }

    @Test
    void setDestinations() {
        Coordinate coordinate = new Coordinate(9.681495, 50.41461);
        ResolvedLocation resolvedLocation = new ResolvedLocation(coordinate, "foo", 0.0);


        List<JSON2DDestinations> json2DDestinations = new ArrayList<>();
        JSON2DDestinations json2DDestination = new JSON2DDestinations(resolvedLocation, false);
        json2DDestinations.add(json2DDestination);

        durationsMatrixResponse.setDestinations(json2DDestinations);
        distancesMatrixResponse.setDestinations(json2DDestinations);

        assertEquals(1, durationsMatrixResponse.getDestinations().size());
        assertArrayEquals(new Double[]{9.681495, 50.41461}, durationsMatrixResponse.getDestinations().get(0).getLocation());
        assertEquals(1, distancesMatrixResponse.getDestinations().size());
        assertArrayEquals(new Double[]{9.681495, 50.41461}, distancesMatrixResponse.getDestinations().get(0).getLocation());

    }

    @Test
    void getSources() {
        assertEquals(3, durationsMatrixResponse.getSources().size());
        assertArrayEquals(new Double[]{8.681495, 49.41461}, durationsMatrixResponse.getSources().get(0).getLocation());
    }

    @Test
    void setSources() {
        Coordinate coordinate = new Coordinate(9.681495, 50.41461);
        ResolvedLocation resolvedLocation = new ResolvedLocation(coordinate, "foo", 0.0);


        List<JSON2DSources> json2DSources = new ArrayList<>();
        JSON2DSources json2DSource = new JSON2DSources(resolvedLocation, false);
        json2DSources.add(json2DSource);

        durationsMatrixResponse.setSources(json2DSources);
        distancesMatrixResponse.setSources(json2DSources);

        assertEquals(1, durationsMatrixResponse.getSources().size());
        assertArrayEquals(new Double[]{9.681495, 50.41461}, durationsMatrixResponse.getSources().get(0).getLocation());
        assertEquals(1, distancesMatrixResponse.getSources().size());
        assertArrayEquals(new Double[]{9.681495, 50.41461}, distancesMatrixResponse.getSources().get(0).getLocation());

    }
}
