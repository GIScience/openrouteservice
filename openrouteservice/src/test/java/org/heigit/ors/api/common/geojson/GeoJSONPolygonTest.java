package org.heigit.ors.api.common.geojson;

import org.heigit.ors.geojson.GeoJSONPolygon;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GeoJSONPolygonTest {

    List<List<Double>> testCoordinates = Arrays.asList(
            Arrays.asList(1.1, 2.2),
            Arrays.asList(1.2, 2.3),
            Arrays.asList(1.3, 2.4),
            Arrays.asList(1.1, 2.2)
    );

    @Test
    void getType() {
        GeoJSONPolygon polygon = new GeoJSONPolygon();
        polygon.setType("Polygon");
        assertEquals("Polygon", polygon.getType());
    }

    @Test
    void setType() {
        GeoJSONPolygon polygon = new GeoJSONPolygon();
        polygon.setType("MultiPolygon");
        assertEquals("MultiPolygon", polygon.getType());
    }

    @Test
    void getCoordinates() {
        GeoJSONPolygon polygon = new GeoJSONPolygon();
        List<List<List<Double>>> coordinates = new ArrayList<>();
        coordinates.add(List.copyOf(testCoordinates));
        polygon.setCoordinates(coordinates);

        assertArrayEquals(coordinates.toArray(), polygon.getCoordinates().toArray());
    }

    @Test
    void setCoordinates() {
        GeoJSONPolygon polygon = new GeoJSONPolygon();
        List<List<List<Double>>> coordinates = new ArrayList<>();
        List<List<Double>> linearRingCoords = new ArrayList<>();
        linearRingCoords.add(List.of(testCoordinates.get(0).get(0), testCoordinates.get(0).get(1)));
        linearRingCoords.add(List.of(testCoordinates.get(1).get(0), testCoordinates.get(1).get(1)));
        linearRingCoords.add(List.of(testCoordinates.get(2).get(0), testCoordinates.get(2).get(1)));
        linearRingCoords.add(List.of(testCoordinates.get(3).get(0), testCoordinates.get(3).get(1)));
        coordinates.add(linearRingCoords);
        polygon.setCoordinates(coordinates);
        List<List<List<Double>>> expectedCoordinates = new ArrayList<>();
        expectedCoordinates.add(List.copyOf(testCoordinates));

        assertArrayEquals(expectedCoordinates.toArray(), polygon.getCoordinates().toArray());
    }

    @Test
    void toJsonObject() throws Exception {
        GeoJSONPolygon polygon = new GeoJSONPolygon();
        List<List<List<Double>>> coordinates = new ArrayList<>();
        coordinates.add(List.copyOf(testCoordinates));
        polygon.setCoordinates(coordinates);
        polygon.setType("Polygon");

        JSONObject expectedJson = new JSONObject("{\"coordinates\":[[[1.1,2.2],[1.2,2.3],[1.3,2.4],[1.1,2.2]]],\"type\":\"Polygon\"}");
        assertEquals(expectedJson.toString(), polygon.toJsonObject().toString());
    }

    @Test
    void convertToJTSPolygon() {
        List<List<List<Double>>> polygonCoords = new ArrayList<>();
        polygonCoords.add(List.copyOf(testCoordinates));
        GeoJSONPolygon polygon = new GeoJSONPolygon();
        polygon.setType("Polygon");
        polygon.setCoordinates(polygonCoords);

        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] jtsCoords = new Coordinate[]{
                new Coordinate(testCoordinates.get(0).get(0), testCoordinates.get(0).get(1)),
                new Coordinate(testCoordinates.get(1).get(0), testCoordinates.get(1).get(1)),
                new Coordinate(testCoordinates.get(2).get(0), testCoordinates.get(2).get(1)),
                new Coordinate(testCoordinates.get(3).get(0), testCoordinates.get(3).get(1))
        };
        LinearRing linearRing = geometryFactory.createLinearRing(jtsCoords);
        Polygon jtsPolygon = geometryFactory.createPolygon(linearRing);

        Polygon[] expectedJtsPolygons = new Polygon[]{jtsPolygon};
        assertArrayEquals(expectedJtsPolygons, polygon.convertToJTS());
    }

    @Test
    void convertToJTSMultiPolygon() {
        List<List<List<Double>>>  multiPolygonCoords = new ArrayList<>();
        multiPolygonCoords.add(List.copyOf(testCoordinates));
        multiPolygonCoords.add(List.copyOf(testCoordinates));
        GeoJSONPolygon polygon = new GeoJSONPolygon();
        polygon.setType("MultiPolygon");
        polygon.setCoordinates(multiPolygonCoords);

        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] jtsCoords = new Coordinate[]{
                new Coordinate(testCoordinates.get(0).get(0), testCoordinates.get(0).get(1)),
                new Coordinate(testCoordinates.get(1).get(0), testCoordinates.get(1).get(1)),
                new Coordinate(testCoordinates.get(2).get(0), testCoordinates.get(2).get(1)),
                new Coordinate(testCoordinates.get(3).get(0), testCoordinates.get(3).get(1))
        };
        LinearRing linearRing = geometryFactory.createLinearRing(jtsCoords);
        Polygon jtsPolygon = geometryFactory.createPolygon(linearRing);

        Polygon[] expectedJtsPolygons = new Polygon[]{jtsPolygon, jtsPolygon};
        assertArrayEquals(expectedJtsPolygons, polygon.convertToJTS());
    }
}