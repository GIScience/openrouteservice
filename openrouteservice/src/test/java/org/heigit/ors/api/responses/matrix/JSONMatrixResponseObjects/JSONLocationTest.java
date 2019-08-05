package heigit.ors.api.responses.matrix.JSONMatrixResponseObjects;

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.matrix.ResolvedLocation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JSONLocationTest {
    private ResolvedLocation resolvedLocation;
    private JSONLocation jsonLocationWithLocation;
    private JSONLocation jsonLocationWoLocation;

    @Before
    public void setUp() {

        Coordinate coordinate = new Coordinate(8.681495, 49.41461);
        resolvedLocation = new ResolvedLocation(coordinate, "foo", 0.0);
        jsonLocationWithLocation = new JSONLocation(resolvedLocation, true);
        jsonLocationWoLocation = new JSONLocation(resolvedLocation, false);
    }

    @Test
    public void getSnapped_distance() {
        Assert.assertEquals("foo", jsonLocationWithLocation.name);
        Assert.assertEquals(new Double(0.0), jsonLocationWithLocation.getSnappedDistance());
    }

    @Test
    public void getLocation() {
        Assert.assertEquals(new Coordinate(8.681495, 49.41461, Double.NaN), jsonLocationWithLocation.location);
        Assert.assertArrayEquals(new Double[0], jsonLocationWithLocation.getLocation());
    }
}