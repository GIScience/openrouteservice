package org.heigit.ors.api.responses.matrix.json;

import org.heigit.ors.api.requests.matrix.MatrixRequest;
import org.heigit.ors.api.requests.matrix.MatrixRequestEnums;
import org.heigit.ors.matrix.MatrixMetricsType;
import org.heigit.ors.matrix.MatrixResult;
import org.heigit.ors.matrix.ResolvedLocation;
import org.heigit.ors.api.APIEnums;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JSONBasedIndividualMatrixResponseTest {

    private final MatrixRequest matrixRequest = new MatrixRequest(new ArrayList<>());
    private MatrixResult matrixResult;
    private JSONBasedIndividualMatrixResponse jsonBasedIndividualMatrixResponse;

    private final Coordinate[] coordinates = new Coordinate[3];

    @BeforeEach
    void setUp() {
        matrixRequest.setResolveLocations(true);
        matrixRequest.setMetrics(new MatrixRequestEnums.Metrics[]{MatrixRequestEnums.Metrics.DISTANCE});
        matrixRequest.setSources(new String[]{"all"});
        matrixRequest.setDestinations(new String[]{"all"});
        matrixRequest.setProfile(APIEnums.Profile.CYCLING_REGULAR);
        matrixRequest.setUnits(APIEnums.Units.METRES);
        jsonBasedIndividualMatrixResponse = new JSONBasedIndividualMatrixResponse(matrixRequest);

        Coordinate coordinate = new Coordinate(8.681495, 49.41461);
        ResolvedLocation resolvedLocation = new ResolvedLocation(coordinate, "foo", 0.0);
        ResolvedLocation[] resolvedLocations = new ResolvedLocation[1];
        resolvedLocations[0] = resolvedLocation;
        matrixResult = new MatrixResult(resolvedLocations, resolvedLocations);
        matrixResult.setTable(MatrixMetricsType.DURATION, new float[]{0, 1});
        matrixResult.setTable(MatrixMetricsType.DISTANCE, new float[]{0, 1});
        matrixResult.setTable(MatrixMetricsType.WEIGHT, new float[]{0, 1});
        List<MatrixResult> matrixResults = new ArrayList<>();
        matrixResults.add(matrixResult);
    }

    @Test
    void constructDestinations() {
        List<JSON2DDestinations> json2DDestinations = jsonBasedIndividualMatrixResponse.constructDestinations(matrixResult);
        assertEquals(1, json2DDestinations.size());
        assertEquals("foo", json2DDestinations.get(0).name);
        assertEquals(new Coordinate(8.681495, 49.41461, Double.NaN), json2DDestinations.get(0).location);
        Double[] location = json2DDestinations.get(0).getLocation();
        assertEquals(2, location.length);
        assertEquals(0, location[0].compareTo(8.681495));
        assertEquals(0, location[1].compareTo(49.41461));
    }

    @Test
    void constructSources() {
        List<JSON2DSources> json2DSources = jsonBasedIndividualMatrixResponse.constructSources(matrixResult);
        assertEquals(1, json2DSources.size());
        assertEquals("foo", json2DSources.get(0).name);
        assertEquals(new Coordinate(8.681495, 49.41461, Double.NaN), json2DSources.get(0).location);
        Double[] location = json2DSources.get(0).getLocation();
        assertEquals(2, location.length);
        assertEquals(0, location[0].compareTo(8.681495));
        assertEquals(0, location[1].compareTo(49.41461));
    }
}