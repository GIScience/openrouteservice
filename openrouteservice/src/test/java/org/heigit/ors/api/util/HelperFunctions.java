  package org.heigit.ors.api.util;

import org.heigit.ors.geojson.PolygonJSON;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelperFunctions {
    private static void compareCoordinates(Coordinate c1, Coordinate c2) {
        assertEquals(c1.x, c2.x, 0);
        assertEquals(c1.y, c2.y, 0);
    }

    public static void checkPolygon(Polygon[] requestPolys, PolygonJSON apiPolys) {
        assertEquals(1, requestPolys.length);
        for (List<List<Double>> coordinates: apiPolys.getCoordinates()){
            for (int i = 0; i < coordinates.size(); i++) {
                List<Double> coordPair = coordinates.get(i);
                Coordinate c = new Coordinate(coordPair.get(0), coordPair.get(1));

                compareCoordinates(c, requestPolys[0].getCoordinates()[i]);
            }
        }

    }



}
