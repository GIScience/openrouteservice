package org.heigit.ors.api.responses.export.topojson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import org.heigit.ors.common.Pair;
import org.heigit.ors.export.ExportResult;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

import java.io.Serializable;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"type", "transform", "objects", "arcs", "bbox"})
@Getter
@Builder
public class TopoJsonExportResponse implements Serializable {

    @JsonProperty("type")
    @Builder.Default
    private String type = "Topology";
    @JsonProperty("objects")
    @Builder.Default
    private Layers objects = new Layers(new Layer("GeometryCollection", new ArrayList<>()));
    @JsonProperty("arcs")
    @Builder.Default
    private List<Arc> arcs = new LinkedList<>();
    @JsonProperty("bbox")
    @Builder.Default
    private List<Double> bbox = new ArrayList<>();

    public static TopoJsonExportResponse fromExportResult(ExportResult exportResult) {
        List<Double> bbox = initializeBbox();
        LinkedList<Geometry> geometries = new LinkedList<>();
        LinkedList<Arc> arcsLocal = new LinkedList<>();
        int arcCount = 0;

        Map<Integer, Coordinate> nodes = exportResult.getLocations();
        for (Map.Entry<Pair<Integer, Integer>, Double> edgeWeight : exportResult.getEdgeWeights().entrySet()) {
            Pair<Integer, Integer> fromTo = edgeWeight.getKey();
            List<Double> from = getXY(nodes, fromTo.first);
            List<Double> to = getXY(nodes, fromTo.second);
            bbox = updateBbox(bbox, from, to);

            // TODO: Add the correct geometry to the export result
            LineString lineString = (LineString) exportResult.getEdgeExtras().get(fromTo).get("geometry");
            // TODO: Can we decide to merge LineStrings into a single LineString based on osm_id?
            // This would allow us to later just store two arcs for a single LineString for both directions.
            long osmId = (long) exportResult.getEdgeExtras().get(fromTo).get("osm_id");

            List<List<Double>> coordinates = List.of(from, to);
            Arc arc = Arc.builder().coordinates(coordinates).build();
            arcsLocal.add(arc);

            List<Integer> arcList = List.of(arcCount);
            Map<String, Object> properties = new HashMap<>();
            properties.put("weight", edgeWeight.getValue());
            properties.put("osm_id", osmId);

            Geometry geometry = Geometry.builder()
                    .type("LineString")
                    .properties(properties)
                    .arcs(arcList)
                    .build();
            geometries.add(geometry);

            arcCount++;
        }

        Layer layer = Layer.builder()
                .type("GeometryCollection")
                .geometries(geometries)
                .build();

        Layers layers = Layers.builder()
                .layer(layer)
                .build();

        return TopoJsonExportResponse.builder()
                .type("Topology")
                .objects(layers)
                .arcs(arcsLocal)
                .bbox(bbox)
                .build();
    }

    private static List<Double> initializeBbox() {
        return List.of(Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
    }

    private static List<Double> getXY(Map<Integer, Coordinate> nodes, int id) {
        Coordinate coordinate = nodes.get(id);
        return List.of(coordinate.x, coordinate.y);
    }

    private static List<Double> updateBbox(List<Double> bbox, List<Double> node1, List<Double> node2) {
        double minX = Math.min(bbox.get(0), Math.min(node1.get(0), node2.get(0)));
        double minY = Math.min(bbox.get(1), Math.min(node1.get(1), node2.get(1)));
        double maxX = Math.max(bbox.get(2), Math.max(node1.get(0), node2.get(0)));
        double maxY = Math.max(bbox.get(3), Math.max(node1.get(1), node2.get(1)));
        return List.of(minX, minY, maxX, maxY);
    }
}
