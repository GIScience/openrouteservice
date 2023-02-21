package org.heigit.ors.api.responses.matrix.json;

import com.vividsolutions.jts.geom.Coordinate;
import org.heigit.ors.matrix.ResolvedLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class JSON2DSourcesTest {
    private ResolvedLocation resolvedLocation;


    @BeforeEach
    void setUp() {
        Coordinate coordinate = new Coordinate(8.681495, 49.41461);
        resolvedLocation = new ResolvedLocation(coordinate, "foo", 0.0);
    }

    @Test
    void getLocation() {
        JSON2DSources json2DSourcesWithLocation = new JSON2DSources(resolvedLocation, true);
        JSON2DSources json2DSourcesWoLocation = new JSON2DSources(resolvedLocation, false);
        assertArrayEquals(new Double[]{8.681495, 49.41461}, json2DSourcesWithLocation.getLocation());
        assertArrayEquals(new Double[]{8.681495, 49.41461}, json2DSourcesWoLocation.getLocation());
    }
}