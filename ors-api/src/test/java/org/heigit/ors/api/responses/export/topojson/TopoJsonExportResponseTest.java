package org.heigit.ors.api.responses.export.topojson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.common.Pair;
import org.heigit.ors.export.ExportResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import java.util.*;

class TopoJsonExportResponseTest {

    TopoJsonExportResponse topoJsonExportResponse;

    // setup function
    @BeforeEach
    void setUp() {
        Map<String, Object> properties1 = new LinkedHashMap<>();
        properties1.put("OBJECTID", 41106);
        Map<String, Object> properties2 = new LinkedHashMap<>();
        properties2.put("OBJECTID", 41107);
        Geometry geometry1 = Geometry.builder()
                .type("LineString")
                .arcs(List.of(0, 1))
                .properties(properties1)
                .build();
        Geometry geometry2 = Geometry.builder()
                .type("LineString")
                .arcs(List.of(2))
                .properties(properties2)
                .build();
        Layer layer = Layer.builder()
                .type("GeometryCollection")
                .geometries(List.of(geometry1, geometry2))
                .build();
        Layers objects = Layers.builder()
                .layer(layer)
                .build();
        Arc arc1 = Arc.builder()
                .coordinates(List.of(
                        List.of(-72.816497, 19.948588),
                        List.of(-72.816354, 19.948234),
                        List.of(-72.816335, 19.948205),
                        List.of(-72.816297, 19.948193),
                        List.of(-72.816213, 19.948215),
                        List.of(-72.816098, 19.948245),
                        List.of(-72.816021, 19.948248)
                ))
                .build();
        Arc arc2 = Arc.builder()
                .coordinates(List.of(
                        List.of(-72.816021, 19.948248),
                        List.of(-72.815938, 19.948247),
                        List.of(-72.815861, 19.948224),
                        List.of(-72.815746, 19.948186),
                        List.of(-72.815574, 19.948146)
                ))
                .build();
        Arc arc3 = Arc.builder()
                .coordinates(List.of(
                        List.of(-72.816021, 19.948248),
                        List.of(-72.815574, 19.948146)
                ))
                .build();
        topoJsonExportResponse = TopoJsonExportResponse.builder()
                .type("Topology")
                .objects(objects)
                .bbox(List.of(-72.822573, 19.947123, -72.81259, 19.952703))
                .arcs(List.of(arc1, arc2, arc3))
                .build();

    }

    @Test
    void testTopoJsonSerialization() throws JsonProcessingException {
        // Serialization with jackson
        ObjectMapper objectMapper = new ObjectMapper();

        String jsonString = objectMapper.writeValueAsString(topoJsonExportResponse);

        // Test the serialization
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        Assertions.assertEquals(jsonNode.get("type").asText(), topoJsonExportResponse.getType());
        for (int i = 0; i < topoJsonExportResponse.getBbox().size(); i++) {
            Assertions.assertEquals(jsonNode.get("bbox").get(i).asDouble(), topoJsonExportResponse.getBbox().get(i));
        }
        for (int i = 0; i < topoJsonExportResponse.getArcs().size(); i++) {
            for (int j = 0; j < topoJsonExportResponse.getArcs().get(i).getCoordinates().size(); j++) {
                for (int k = 0; k < topoJsonExportResponse.getArcs().get(i).getCoordinates().get(j).size(); k++) {
                    Assertions.assertEquals(jsonNode.get("arcs").get(i).get(j).get(k).asDouble(), topoJsonExportResponse.getArcs().get(i).getCoordinates().get(j).get(k));
                }
            }
        }
        Assertions.assertEquals(jsonNode.get("objects").get("layer").get("type").asText(), topoJsonExportResponse.getObjects().getLayer().getType());
        for (int i = 0; i < topoJsonExportResponse.getObjects().getLayer().getGeometries().size(); i++) {
            Assertions.assertEquals(jsonNode.get("objects").get("layer").get("geometries").get(i).get("type").asText(), topoJsonExportResponse.getObjects().getLayer().getGeometries().get(i).getType());
            for (int j = 0; j < topoJsonExportResponse.getObjects().getLayer().getGeometries().get(i).getArcs().size(); j++) {
                Assertions.assertEquals(jsonNode.get("objects").get("layer").get("geometries").get(i).get("arcs").get(j).asInt(), (int) topoJsonExportResponse.getObjects().getLayer().getGeometries().get(i).getArcs().get(j));
            }
            for (String key : topoJsonExportResponse.getObjects().getLayer().getGeometries().get(i).getProperties().keySet()) {
                Assertions.assertEquals(jsonNode.get("objects").get("layer").get("geometries").get(i).get("properties").get(key).asInt(), (int) topoJsonExportResponse.getObjects().getLayer().getGeometries().get(i).getProperties().get(key));
            }
        }
    }

    @Test
    void testEmptyTopoJsonObjectSerialization() throws JsonProcessingException {
        TopoJsonExportResponse emptyTopoJsonExportResponse = TopoJsonExportResponse.builder().build();
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(emptyTopoJsonExportResponse);
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        Assertions.assertEquals("Topology", emptyTopoJsonExportResponse.getType());
        Assertions.assertEquals(0, jsonNode.get("bbox").size());
        Assertions.assertEquals(0, jsonNode.get("arcs").size());
        Assertions.assertEquals(1, jsonNode.get("objects").size());
    }

    @Test
    void testFromExportResult() {
        ExportResult exportResult = new ExportResult();
        exportResult.addLocation(0, new Coordinate(0.0, 0.0));
        exportResult.addLocation(1, new Coordinate(1.0, 1.0));
        exportResult.addLocation(2, new Coordinate(2.0, 9.0));

        exportResult.addEdge(new Pair<>(0, 1), 1.0);
        exportResult.addEdge(new Pair<>(1, 2), 2.0);
        exportResult.addEdgeExtra(new Pair<>(0, 1), new HashMap<>(Map.of("osm_id", 1L, "foo", "baz")));
        exportResult.addEdgeExtra(new Pair<>(1, 2), new HashMap<>(Map.of("osm_id", 2L, "foo", "bar")));

        TopoJsonExportResponse exportResultToTopoJson = TopoJsonExportResponse.fromExportResult(exportResult);
        Layers layers = exportResultToTopoJson.getObjects();
        Assertions.assertEquals("GeometryCollection", layers.getLayer().getType());
        Assertions.assertEquals(2, layers.getLayer().getGeometries().size());
        Geometry geometry1 = layers.getLayer().getGeometries().get(0);
        Assertions.assertEquals("LineString", geometry1.getType());
        Assertions.assertEquals(List.of(0), geometry1.getArcs());
        Assertions.assertEquals(1.0, geometry1.getProperties().get("weight"));
        Assertions.assertEquals(1L, geometry1.getProperties().get("osm_id"));
        Geometry geometry2 = layers.getLayer().getGeometries().get(1);
        Assertions.assertEquals("LineString", geometry2.getType());
        Assertions.assertEquals(List.of(1), geometry2.getArcs());
        Assertions.assertEquals(2.0, geometry2.getProperties().get("weight"));
        Assertions.assertEquals(2L, geometry2.getProperties().get("osm_id"));
        List<Arc> arcs = exportResultToTopoJson.getArcs();
        Assertions.assertEquals(2, arcs.size());
        Arc arc1 = arcs.get(0);
        Assertions.assertEquals(List.of(List.of(0.0, 0.0), List.of(1.0, 1.0)), arc1.getCoordinates());
        Arc arc2 = arcs.get(1);
        Assertions.assertEquals(List.of(List.of(1.0, 1.0), List.of(2.0, 9.0)), arc2.getCoordinates());
        List<Double> bbox = exportResultToTopoJson.getBbox();
        Assertions.assertEquals(4, bbox.size());
        Assertions.assertEquals(0.0, bbox.get(0));
        Assertions.assertEquals(0.0, bbox.get(1));
        Assertions.assertEquals(2.0, bbox.get(2));
        Assertions.assertEquals(9.0, bbox.get(3));
    }
}