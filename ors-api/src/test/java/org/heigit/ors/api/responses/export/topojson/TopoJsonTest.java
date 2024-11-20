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
        topoJson = new TopoJson();
        topoJson.withType("Topology");
        Objects objects = new Objects();
        Layer layer = new Layer();
        Geometry geometry1 = new Geometry();
        geometry1.withType("LineString");
        geometry1.withArcs(List.of(0, 1));
        Map<String, Object> properties1 = new LinkedHashMap<>();
        properties1.put("OBJECTID", 41106);
        geometry1.withProperties(properties1);
        Geometry geometry2 = new Geometry();
        geometry2.withType("LineString");
        geometry2.withArcs(List.of(2));
        Map<String, Object> properties2 = new LinkedHashMap<>();
        properties2.put("OBJECTID", 41107);
        geometry2.withProperties(properties2);
        layer.withType("GeometryCollection");
        layer.withGeometries(List.of(geometry1, geometry2));
        objects.withLayer(layer);
        topoJson.withObjects(objects);
        topoJson.withBbox(List.of(-72.822573, 19.947123, -72.81259, 19.952703));
        List<List<List<Double>>> arcs = List.of(
                List.of(
                        List.of(-72.816497, 19.948588),
                        List.of(-72.816354, 19.948234),
                        List.of(-72.816335, 19.948205),
                        List.of(-72.816297, 19.948193),
                        List.of(-72.816213, 19.948215),
                        List.of(-72.816098, 19.948245),
                        List.of(-72.816021, 19.948248)
                ),
                List.of(
                        List.of(-72.816021, 19.948248),
                        List.of(-72.815938, 19.948247),
                        List.of(-72.815861, 19.948224),
                        List.of(-72.815746, 19.948186),
                        List.of(-72.815574, 19.948146)
                ),
                List.of(
                        List.of(-72.816021, 19.948248),
                        List.of(-72.815574, 19.948146)
                )
        );
        topoJson.withArcs(arcs);

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
            for (int j = 0; j < topoJson.getArcs().get(i).size(); j++) {
                for (int k = 0; k < topoJson.getArcs().get(i).get(j).size(); k++) {
                    Assertions.assertEquals(jsonNode.get("arcs").get(i).get(j).get(k).asDouble(), topoJson.getArcs().get(i).get(j).get(k));
                }
            }
        }
        Assertions.assertEquals(jsonNode.get("objects").get("layer").get("type").asText(), topoJson.getObjects().layer.type);
        for (int i = 0; i < topoJson.getObjects().layer.getGeometries().size(); i++) {
            Assertions.assertEquals(jsonNode.get("objects").get("layer").get("geometries").get(i).get("type").asText(), topoJson.getObjects().layer.getGeometries().get(i).getType());
            for (int j = 0; j < topoJson.getObjects().layer.getGeometries().get(i).getArcs().size(); j++) {
                Assertions.assertEquals(jsonNode.get("objects").get("layer").get("geometries").get(i).get("arcs").get(j).asInt(), (int) topoJson.getObjects().layer.getGeometries().get(i).getArcs().get(j));
            }
            for (String key : topoJson.getObjects().layer.getGeometries().get(i).getProperties().keySet()) {
                Assertions.assertEquals(jsonNode.get("objects").get("layer").get("geometries").get(i).get("properties").get(key).asInt(), (int) topoJson.getObjects().layer.getGeometries().get(i).getProperties().get(key));
            }
        }
    }
}