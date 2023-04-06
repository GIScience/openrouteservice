package org.heigit.ors.geojson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeoJSONPolygonTest {
    private static final GeometryFactory geometryFactory = new GeometryFactory();
    private final ObjectMapper mapper = new ObjectMapper();
    private List<List<List<Double>>> coordinates;
    private List<List<List<String>>> stringCoordinates;
    private GeoJSONPolygon polygon;
    private Polygon[] expectedPolygons;

    @BeforeEach
    void setUp() {
        String type = "Polygon";
        coordinates = Arrays.asList(
                Arrays.asList(
                        Arrays.asList(35.0, 10.0),
                        Arrays.asList(45.0, 45.0),
                        Arrays.asList(15.0, 40.0),
                        Arrays.asList(10.0, 20.0),
                        Arrays.asList(35.0, 10.0)
                ),
                Arrays.asList(
                        Arrays.asList(20.0, 30.0),
                        Arrays.asList(35.0, 35.0),
                        Arrays.asList(30.0, 20.0),
                        Arrays.asList(20.0, 30.0)
                )
        );
        stringCoordinates = Arrays.asList(
                Arrays.asList(
                        Arrays.asList("35.0", "10.0"),
                        Arrays.asList("45.0", "45.0"),
                        Arrays.asList("15.0", "40.0"),
                        Arrays.asList("10.0", "20.0"),
                        Arrays.asList("35.0", "10.0")
                ),
                Arrays.asList(
                        Arrays.asList("20.0", "30.0"),
                        Arrays.asList("35.0", "35.0"),
                        Arrays.asList("30.0", "20.0"),
                        Arrays.asList("20.0", "30.0")
                )
        );
        this.polygon = new GeoJSONPolygon(type, coordinates);

        // Expected JTS polygons
        Coordinate[] coords1 = new Coordinate[]{
                new Coordinate(35.0, 10.0),
                new Coordinate(45.0, 45.0),
                new Coordinate(15.0, 40.0),
                new Coordinate(10.0, 20.0),
                new Coordinate(35.0, 10.0)
        };
        LinearRing linearRing1 = geometryFactory.createLinearRing(coords1);
        Polygon jtsPolygon1 = geometryFactory.createPolygon(linearRing1);


        Coordinate[] coords2 = {
                new Coordinate(20.0, 30.0),
                new Coordinate(35.0, 35.0),
                new Coordinate(30.0, 20.0),
                new Coordinate(20.0, 30.0)
        };
        LinearRing linearRing2 = geometryFactory.createLinearRing(coords2);
        Polygon jtsPolygon2 = geometryFactory.createPolygon(linearRing2);
        this.expectedPolygons = new Polygon[]{jtsPolygon1, jtsPolygon2};
    }

    @Test
    @DisplayName("Test GeoJSONPolygon type")
    void testType() {
        assertEquals("Polygon", this.polygon.getType());
    }

    @Test
    @DisplayName("Test GeoJSONPolygon coordinates")
    void testCoordinates() {
        assertEquals(coordinates, this.polygon.getCoordinates());
    }

    @Test
    @DisplayName("Test GeoJSONPolygon to JTS polygons conversion")
    void testConvertToJTS() {
        Polygon[] actualPolygons = this.polygon.convertToJTS();
        assertNotNull(actualPolygons);
        assertArrayEquals(expectedPolygons, actualPolygons);
    }

    @Test
    void serialize_shouldReturnJsonString() throws JsonProcessingException {
        String json = mapper.writeValueAsString(polygon);

        String expectedJson = "{\"type\":\"Polygon\",\"coordinates\":[[[35.0,10.0],[45.0,45.0],[15.0,40.0],[10.0,20.0],[35.0,10.0]],[[20.0,30.0],[35.0,35.0],[30.0,20.0],[20.0,30.0]]]}";
        assertEquals(expectedJson, json);
    }

    @Test
    void deserialize_stringCoordinates_shouldReturnJsonString() throws JsonProcessingException {
        String json  = "{\"type\":\"Polygon\",\"coordinates\":[[[\"35.0\",\"10.0\"],[\"45.0\",\"45.0\"],[\"15.0\",\"40.0\"],[\"10.0\",\"20.0\"],[\"35.0\",\"10.0\"]],[[\"20.0\",\"30.0\"],[\"35.0\",\"35.0\"],[\"30.0\",\"20.0\"],[\"20.0\",\"30.0\"]]]}";
        GeoJSONPolygon polygon = mapper.readValue(json, GeoJSONPolygon.class);
        assertEquals("Polygon", polygon.getType());
        assertEquals(coordinates, polygon.getCoordinates());
    }

    @Test
    void deserialize_shouldReturnGeoJSONPolygonObject() throws JsonProcessingException {
        String json = "{\"type\":\"Polygon\",\"coordinates\":[[[35.0,10.0],[45.0,45.0],[15.0,40.0],[10.0,20.0],[35.0,10.0]],[[20.0,30.0],[35.0,35.0],[30.0,20.0],[20.0,30.0]]]}";
        GeoJSONPolygon polygon = mapper.readValue(json, GeoJSONPolygon.class);
        assertEquals("Polygon", polygon.getType());
        assertEquals(coordinates, polygon.getCoordinates());
    }
}
