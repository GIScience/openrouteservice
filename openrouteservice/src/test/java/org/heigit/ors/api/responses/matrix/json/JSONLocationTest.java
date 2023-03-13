package org.heigit.ors.api.responses.matrix.json;

import org.heigit.ors.matrix.ResolvedLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JSONLocationTest {
    private ResolvedLocation resolvedLocation;
    private JSONLocation jsonLocationWithLocation;
    private JSONLocation jsonLocationWoLocation;

    @BeforeEach
    void setUp() {

        Coordinate coordinate = new Coordinate(8.681495, 49.41461);
        resolvedLocation = new ResolvedLocation(coordinate, "foo", 0.0);
        jsonLocationWithLocation = new JSONLocation(resolvedLocation, true);
        jsonLocationWoLocation = new JSONLocation(resolvedLocation, false);
    }

    @Test
    void getSnapped_distance() {
        assertEquals("foo", jsonLocationWithLocation.name);
        assertEquals(Double.valueOf(0.0), jsonLocationWithLocation.getSnappedDistance());
    }

    @Test
    void getLocation() {
        assertEquals(new Coordinate(8.681495, 49.41461, Double.NaN), jsonLocationWithLocation.location);
        assertArrayEquals(new Double[0], jsonLocationWithLocation.getLocation());
    }
}