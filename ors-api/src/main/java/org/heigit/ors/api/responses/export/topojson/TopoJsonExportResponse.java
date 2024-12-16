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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.heigit.ors.export.ExportResult.TopoGeometry;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"type", "transform", "objects", "arcs", "bbox"})
@Getter
@Builder
public class TopoJsonExportResponse implements Serializable {
    @JsonProperty(value = "type")
    @Builder.Default
    private String type = "Topology";
    @JsonProperty("objects")
    @Builder.Default
    private Objects objects = null;
    @JsonProperty("arcs")
    @Builder.Default
    private List<Arc> arcs = new LinkedList<>();
    @JsonProperty("bbox")
    @Builder.Default
    private List<Double> bbox = new ArrayList<>(4);

    public static TopoJsonExportResponse fromExportResult(ExportResult exportResult) {
        BBox bbox = new BBox();
        LinkedList<Geometry> geometries = new LinkedList<>();
        LinkedList<Arc> arcsLocal = new LinkedList<>();
        if (exportResult.hasTopoGeometries()) {
            buildGeometriesFromTopoGeometries(exportResult, bbox, geometries, arcsLocal);
        } else {
            buildGeometriesFromEdges(exportResult, bbox, geometries, arcsLocal);
        }
        return TopoJsonExportResponse.builder()
                .type("Topology")
                .objects(Objects.builder().network(Network.builder()
                        .type("GeometryCollection")
                        .geometries(geometries)
                        .build()).build())
                .arcs(arcsLocal)
                .bbox(bbox.toList())
                .build();
    }

    private static void buildGeometriesFromEdges(ExportResult exportResult, BBox bbox, LinkedList<Geometry> geometries, LinkedList<Arc> arcsLocal) {
        Map<Pair<Integer, Integer>, Double> edgeWeights = exportResult.getEdgeWeights();
        Map<Pair<Integer, Integer>, LineString> edgeGeometries = exportResult.getEdgeGeometries();
        int arcCount = 0;
        for (Map.Entry<Pair<Integer, Integer>, Double> edgeWeight : edgeWeights.entrySet()) {
            arcsLocal.add(Arc.builder().coordinates(makeCoordinateList(edgeGeometries.get(edgeWeight.getKey()), bbox)).build());
            List<Integer> arcList = List.of(arcCount);
            arcCount++;

            Properties properties = Properties.builder()
                    .weight(edgeWeight.getValue())
                    .build();
            Geometry geometry = Geometry.builder()
                    .type("LineString")
                    .properties(properties)
                    .arcs(arcList)
                    .build();
            geometries.add(geometry);
        }
    }

    private static void buildGeometriesFromTopoGeometries(ExportResult exportResult, BBox bbox, List<Geometry> geometries, List<Arc> arcsLocal) {
        Map<Long, TopoGeometry> topoGeometries = exportResult.getTopoGeometries();
        int arcCount = 0;
        for (long osmId : topoGeometries.keySet()) {
            TopoGeometry topoGeometry = topoGeometries.get(osmId);
            Map<Integer, ExportResult.TopoArc> arcs = topoGeometry.getArcs();
            List<Integer> orsIdList = arcs.keySet().stream().sorted().toList();
            List<Integer> arcList = new LinkedList<>();
            List<Integer> nodeList = new LinkedList<>();
            List<Double> distanceList = new LinkedList<>();
            for (int orsId : orsIdList) {
                ExportResult.TopoArc currentArc = arcs.get(orsId);
                arcsLocal.add(Arc.builder().coordinates(makeCoordinateList(currentArc.geometry(), bbox)).build());
                arcList.add(arcCount);
                if (nodeList.isEmpty()) {
                    nodeList.add(currentArc.from());
                }
                nodeList.add(currentArc.to());
                distanceList.add(currentArc.length());
                arcCount++;
            }

            Properties properties = Properties.builder()
                    .osmId(osmId)
                    .bothDirections(topoGeometry.isBothDirections())
                    .speed(topoGeometry.getSpeed())
                    .speedReverse(topoGeometry.isBothDirections() ? topoGeometry.getSpeedReverse() : null)
                    .orsIds(orsIdList)
                    .orsNodes(nodeList)
                    .distances(distanceList)
                    .build();
            Geometry geometry = Geometry.builder()
                    .type("LineString")
                    .properties(properties)
                    .arcs(arcList)
                    .build();
            geometries.add(geometry);
        }
    }

    private static List<List<Double>> makeCoordinateList(LineString geometry, BBox bbox) {
        List<List<Double>> coordinates = new LinkedList<>();
        for (Coordinate coordinate : geometry.getCoordinates()) {
            coordinates.add(List.of(coordinate.x, coordinate.y));
            bbox.update(coordinate.x, coordinate.y);
        }
        return coordinates;
    }

    private static class BBox {
        private double[] coords = {Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};

        public void update(double x, double y) {
            coords[0] = Math.min(coords[0], x);
            coords[1] = Math.min(coords[1], y);
            coords[2] = Math.max(coords[2], x);
            coords[3] = Math.max(coords[3], y);
        }

        public List<Double> toList() {
            return List.of(coords[0], coords[1], coords[2], coords[3]);
        }
    }
}
