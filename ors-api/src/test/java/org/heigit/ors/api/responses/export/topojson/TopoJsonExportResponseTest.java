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
import org.locationtech.jts.geom.GeometryFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TopoJsonExportResponseTest {

    TopoJsonExportResponse topoJsonExportResponse;
    String topologyLayerName = "network";
    private static final GeometryFactory geometryFactory = new GeometryFactory();

    // setup function
    @BeforeEach
    void setUp() {
        Geometry geometry1 = Geometry.builder()
                .type("LineString")
                .arcs(List.of(0, 1))
                .properties(Properties.builder().osmId(41106L).build())
                .build();
        Geometry geometry2 = Geometry.builder()
                .type("LineString")
                .arcs(List.of(2))
                .properties(Properties.builder().osmId(41107L).build())
                .build();
        Network network = Network.builder()
                .type("GeometryCollection")
                .geometries(List.of(geometry1, geometry2))
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
                .objects(Objects.builder().network(network).build())
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
        Assertions.assertEquals(1, jsonNode.get("objects").size());
        Assertions.assertEquals(jsonNode.get("objects").get("network").get("type").asText(), topoJsonExportResponse.getObjects().getNetwork().getType());
        for (int j = 0; j < topoJsonExportResponse.getObjects().getNetwork().getGeometries().size(); j++) {
            Assertions.assertEquals(jsonNode.get("objects").get("network").get("geometries").get(j).get("type").asText(), topoJsonExportResponse.getObjects().getNetwork().getGeometries().get(j).getType());
            for (int k = 0; k < topoJsonExportResponse.getObjects().getNetwork().getGeometries().get(j).getArcs().size(); k++) {
                Assertions.assertEquals(jsonNode.get("objects").get("network").get("geometries").get(j).get("arcs").get(k).asInt(), topoJsonExportResponse.getObjects().getNetwork().getGeometries().get(j).getArcs().get(k));
            }
            Assertions.assertEquals(jsonNode.get("objects").get("network").get("geometries").get(j).get("properties").get("osm_id").asText(), topoJsonExportResponse.getObjects().getNetwork().getGeometries().get(j).getProperties().getOsmId().toString());
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
    }

    @Test
    void testFromExportResult() {
        ExportResult exportResult = new ExportResult();
        exportResult.addLocation(0, new Coordinate(0.0, 0.0));
        exportResult.addLocation(1, new Coordinate(1.0, 1.0));
        exportResult.addLocation(2, new Coordinate(2.0, 2.0));
        exportResult.addLocation(3, new Coordinate(3.0, 3.0));
        exportResult.addLocation(4, new Coordinate(4.0, 4.0));

        exportResult.addEdge(new Pair<>(0, 1), 1.0);
        exportResult.addEdge(new Pair<>(1, 2), 2.0);
        exportResult.addEdge(new Pair<>(2, 3), 2.0);
        exportResult.addEdgeExtra(new Pair<>(0, 1), new HashMap<>(Map.of("osm_id", 1L, "foo", "baz")));
        exportResult.addEdgeExtra(new Pair<>(1, 2), new HashMap<>(Map.of("osm_id", 1L, "foo", "bar")));
        exportResult.addEdgeExtra(new Pair<>(2, 3), new HashMap<>(Map.of("osm_id", 1L, "foo", "bla")));
        exportResult.addEdgeExtra(new Pair<>(0, 4), new HashMap<>(Map.of("osm_id", 2L, "foo", "bub")));

        ExportResult.TopoGeometry topoGeometry1 = new ExportResult.TopoGeometry(1.0F, 1.0F);
        topoGeometry1.getArcs().put(1, new ExportResult.TopoArc(geometryFactory.createLineString(new Coordinate[]{new Coordinate(0.0, 0.0), new Coordinate(1.0, 1.0)}), 1.0, 0, 1));
        topoGeometry1.getArcs().put(2, new ExportResult.TopoArc(geometryFactory.createLineString(new Coordinate[]{new Coordinate(1.0, 1.0), new Coordinate(2.0, 2.0)}), 2.0, 1, 2));
        topoGeometry1.getArcs().put(3, new ExportResult.TopoArc(geometryFactory.createLineString(new Coordinate[]{new Coordinate(2.0, 2.0), new Coordinate(3.0, 3.0)}), 3.0, 2, 3));
        topoGeometry1.setBothDirections(true);
        exportResult.getTopoGeometries().put(1L, topoGeometry1);
        ExportResult.TopoGeometry topoGeometry2 = new ExportResult.TopoGeometry(2.0F, 2.0F);
        topoGeometry2.getArcs().put(4, new ExportResult.TopoArc(geometryFactory.createLineString(new Coordinate[]{new Coordinate(0.0, 0.0), new Coordinate(4.0, 5.0)}), 4.0, 0, 4));
        exportResult.getTopoGeometries().put(2L, topoGeometry2);

        TopoJsonExportResponse exportResultToTopoJson = TopoJsonExportResponse.fromExportResult(exportResult);
        Network network = exportResultToTopoJson.getObjects().getNetwork();
        Assertions.assertEquals("GeometryCollection", network.getType());
        Assertions.assertEquals(2, network.getGeometries().size());
        Geometry geometry1 = network.getGeometries().get(0);
        Assertions.assertEquals("LineString", geometry1.getType());
        Assertions.assertEquals(List.of(0, 1, 2), geometry1.getArcs());
        Assertions.assertEquals(1.0, geometry1.getProperties().getSpeed());
        Assertions.assertEquals(1.0, geometry1.getProperties().getSpeedReverse());
        Assertions.assertEquals(1L, geometry1.getProperties().getOsmId());
        Geometry geometry2 = network.getGeometries().get(1);
        Assertions.assertEquals("LineString", geometry2.getType());
        Assertions.assertEquals(List.of(3), geometry2.getArcs());
        Assertions.assertEquals(2.0, geometry2.getProperties().getSpeed());
        Assertions.assertNull(geometry2.getProperties().getSpeedReverse());
        Assertions.assertEquals(2L, geometry2.getProperties().getOsmId());
        List<Arc> arcs = exportResultToTopoJson.getArcs();
        Assertions.assertEquals(4, arcs.size());
        Assertions.assertEquals(List.of(List.of(0.0, 0.0), List.of(1.0, 1.0)), arcs.get(0).getCoordinates());
        Assertions.assertEquals(List.of(List.of(1.0, 1.0), List.of(2.0, 2.0)), arcs.get(1).getCoordinates());
        Assertions.assertEquals(List.of(List.of(2.0, 2.0), List.of(3.0, 3.0)), arcs.get(2).getCoordinates());
        Assertions.assertEquals(List.of(List.of(0.0, 0.0), List.of(4.0, 5.0)), arcs.get(3).getCoordinates());
        List<Double> bbox = exportResultToTopoJson.getBbox();
        Assertions.assertEquals(4, bbox.size());
        Assertions.assertEquals(0.0, bbox.get(0));
        Assertions.assertEquals(0.0, bbox.get(1));
        Assertions.assertEquals(4.0, bbox.get(2));
        Assertions.assertEquals(5.0, bbox.get(3));
    }
}