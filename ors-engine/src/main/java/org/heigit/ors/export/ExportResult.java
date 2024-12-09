package org.heigit.ors.export;

import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.common.Pair;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ExportResult {
    private final Map<Integer, Coordinate> locations;
    private final Map<Pair<Integer, Integer>, Double> edgeWeights;
    private final Map<Long, TopoGeometry> topoGeometries;
    private Map<Pair<Integer, Integer>, Map<String, Object>> edgeExtras;
    @Setter
    private ExportWarning warning;


    public ExportResult() {
        this.locations = new HashMap<>();
        this.edgeWeights = new HashMap<>();
        this.topoGeometries = new HashMap<>();
        this.warning = null;
    }

    public void addEdge(Pair<Integer, Integer> edge, Double weight) {
        this.edgeWeights.put(edge, weight);
    }

    public void addLocation(Integer node, Coordinate coord) {
        this.locations.put(node, coord);
    }

    public boolean hasWarning() {
        return this.warning != null;
    }

    public void addEdgeExtra(Pair<Integer, Integer> edge, Map<String, Object> extra) {
        if (edgeExtras == null) {
            edgeExtras = new HashMap<>();
        }
        this.edgeExtras.put(edge, extra);
    }

    public boolean hasEdgeExtras() {
        return edgeExtras != null;
    }

    @Getter
    public static class TopoGeometry {
        private final double speed;
        private final double speedReverse;
        private final Map<Integer, TopoArc> arcs = new HashMap<>();
        @Setter
        private boolean bothDirections;

        public TopoGeometry(double speed, double speedReverse) {
            this.speed = speed;
            this.speedReverse = speedReverse;
        }
    }

    public record TopoArc(LineString geometry, double length, int from, int to) {
    }
}
