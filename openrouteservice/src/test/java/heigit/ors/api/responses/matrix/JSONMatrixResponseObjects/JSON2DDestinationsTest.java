package heigit.ors.api.responses.matrix.JSONMatrixResponseObjects;

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.matrix.ResolvedLocation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JSON2DDestinationsTest {
    private ResolvedLocation resolvedLocation;

    @Before
    public void setUp() {
        Coordinate coordinate = new Coordinate(8.681495, 49.41461);
        resolvedLocation = new ResolvedLocation(coordinate, "foo", 0.0);
    }

    @Test
    public void getLocation() {
        JSON2DDestinations json2DDestinationsWithLocation = new JSON2DDestinations(resolvedLocation, true);
        JSON2DDestinations json2DDestinationsWoLocation = new JSON2DDestinations(resolvedLocation, false);
        Assert.assertArrayEquals(new Double[]{8.681495, 49.41461}, json2DDestinationsWithLocation.getLocation());
        Assert.assertArrayEquals(new Double[]{8.681495, 49.41461}, json2DDestinationsWoLocation.getLocation());
    }
}