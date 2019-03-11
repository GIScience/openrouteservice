package heigit.ors.api.responses.matrix.JSONMatrixResponseObjects;

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.api.requests.matrix.MatrixRequestEnums;
import heigit.ors.common.DistanceUnit;
import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.api.requests.matrix.MatrixRequest;
import heigit.ors.matrix.MatrixResult;
import heigit.ors.matrix.ResolvedLocation;
import heigit.ors.routing.RoutingProfileType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class JSONBasedIndividualMatrixResponseTest {

    private MatrixRequest matrixRequest = new MatrixRequest(new ArrayList<>());
    private MatrixResult matrixResult;
    private JSONBasedIndividualMatrixResponse jsonBasedIndividualMatrixResponse;

    private Coordinate[] coordinates = new Coordinate[3];

    @Before
    public void setUp() {
        matrixRequest.setResolveLocations(true);
        matrixRequest.setMetrics(new MatrixRequestEnums.Metrics[] {MatrixRequestEnums.Metrics.DISTANCE});
        matrixRequest.setSources(new String[] {"all"});
        matrixRequest.setDestinations(new String[] {"all"});
        matrixRequest.setProfile(APIEnums.Profile.CYCLING_REGULAR);
        matrixRequest.setUnits(APIEnums.Units.METRES);
        jsonBasedIndividualMatrixResponse = new JSONBasedIndividualMatrixResponse(matrixRequest);

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
    }

    @Test
    public void constructDestinations() {
        List<JSON2DDestinations> json2DDestinations = jsonBasedIndividualMatrixResponse.constructDestinations(matrixResult);
        Assert.assertEquals(1, json2DDestinations.size());
        Assert.assertEquals("foo", json2DDestinations.get(0).name);
        Assert.assertEquals(new Coordinate(8.681495, 49.41461, Double.NaN), json2DDestinations.get(0).location);
        Double[] location = json2DDestinations.get(0).getLocation();
        Assert.assertEquals(2, location.length);
        Assert.assertEquals(0, location[0].compareTo(8.681495));
        Assert.assertEquals(0, location[1].compareTo(49.41461));
    }

    @Test
    public void constructSources() {
        List<JSON2DSources> json2DSources = jsonBasedIndividualMatrixResponse.constructSources(matrixResult);
        Assert.assertEquals(1, json2DSources.size());
        Assert.assertEquals("foo", json2DSources.get(0).name);
        Assert.assertEquals(new Coordinate(8.681495, 49.41461, Double.NaN), json2DSources.get(0).location);
        Double[] location = json2DSources.get(0).getLocation();
        Assert.assertEquals(2, location.length);
        Assert.assertEquals(0, location[0].compareTo(8.681495));
        Assert.assertEquals(0, location[1].compareTo(49.41461));
    }
}