package org.heigit.ors.util;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PolyLineEncoderTest {
    @Test
    void encodePolyLine() {
        Coordinate[] coords = {
                new Coordinate(-120.2,38.5),
                new Coordinate(-120.95,40.7),
                new Coordinate(-126.453, 43.252)};
        String encodedPolyline = PolylineEncoder.encode(coords, false, new StringBuilder());
        assertEquals("_p~iF~ps|U_ulLnnqC_mqNvxq`@", encodedPolyline);
    }

    @Test
    void encodePolyLineWithElevation() {
        Coordinate[] coords = {
                new Coordinate(-120.2,38.5, 110),
                new Coordinate(-120.95,40.7, 120),
                new Coordinate(-126.453, 43.252, 100)};
        String encodedPolyline = PolylineEncoder.encode(coords, true, new StringBuilder());
        assertEquals("_p~iF~ps|UonT_ulLnnqCo}@_mqNvxq`@~{B", encodedPolyline);
    }
}
