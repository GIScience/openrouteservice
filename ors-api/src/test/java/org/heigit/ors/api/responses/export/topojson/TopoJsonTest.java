package org.heigit.ors.api.responses.export.topojson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class TopoJsonTest {

    TopoJson topoJson;

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
        topoJson = TopoJson.builder()
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

        String jsonString = objectMapper.writeValueAsString(topoJson);

        // Test the serialization
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        Assertions.assertEquals(jsonNode.get("type").asText(), topoJson.getType());
        for (int i = 0; i < topoJson.getBbox().size(); i++) {
            Assertions.assertEquals(jsonNode.get("bbox").get(i).asDouble(), topoJson.getBbox().get(i));
        }
        for (int i = 0; i < topoJson.getArcs().size(); i++) {
            for (int j = 0; j < topoJson.getArcs().get(i).getCoordinates().size(); j++) {
                for (int k = 0; k < topoJson.getArcs().get(i).getCoordinates().get(j).size(); k++) {
                    Assertions.assertEquals(jsonNode.get("arcs").get(i).get(j).get(k).asDouble(), topoJson.getArcs().get(i).getCoordinates().get(j).get(k));
                }
            }
        }
        Assertions.assertEquals(jsonNode.get("objects").get("layer").get("type").asText(), topoJson.getObjects().getLayer().getType());
        for (int i = 0; i < topoJson.getObjects().getLayer().getGeometries().size(); i++) {
            Assertions.assertEquals(jsonNode.get("objects").get("layer").get("geometries").get(i).get("type").asText(), topoJson.getObjects().getLayer().getGeometries().get(i).getType());
            for (int j = 0; j < topoJson.getObjects().getLayer().getGeometries().get(i).getArcs().size(); j++) {
                Assertions.assertEquals(jsonNode.get("objects").get("layer").get("geometries").get(i).get("arcs").get(j).asInt(), (int) topoJson.getObjects().getLayer().getGeometries().get(i).getArcs().get(j));
            }
            for (String key : topoJson.getObjects().getLayer().getGeometries().get(i).getProperties().keySet()) {
                Assertions.assertEquals(jsonNode.get("objects").get("layer").get("geometries").get(i).get("properties").get(key).asInt(), (int) topoJson.getObjects().getLayer().getGeometries().get(i).getProperties().get(key));
            }
        }
    }
}