package org.heigit.ors.export;

import com.vividsolutions.jts.geom.Coordinate;
import org.heigit.ors.export.ExportWarning;
import org.heigit.ors.common.Pair;

import java.util.HashMap;
import java.util.Map;

public class ExportResult {
    private Map<Integer, Coordinate> locations;
    private Map<Pair<Integer, Integer>, Double> edgeWeigths;
        private ExportWarning warning;


        public ExportResult() {
            this.locations = new HashMap<>();
            this.edgeWeigths = new HashMap<>();
            this.warning = null;
        }

        public Map<Pair<Integer, Integer>, Double> getEdgeWeigths() {
            return edgeWeigths;
        }

        public void setEdgeWeigths(Map<Pair<Integer, Integer>, Double> edgeWeigths) {
            this.edgeWeigths = edgeWeigths;
        }

        public void addEdge(Pair<Integer, Integer> edge, Double weight) {
            this.edgeWeigths.put(edge, weight);
        }

        public Map<Integer, Coordinate> getLocations() {return locations; }

        public void setLocations(Map<Integer, Coordinate> locations) {
            this.locations = locations;
        }

        public void addLocation(Integer node, Coordinate coord) {
            this.locations.put(node, coord);
        }

        public ExportWarning getWarning() { return warning;}

        public void setWarning(ExportWarning warning) { this.warning = warning; }

        public boolean hasWarning() {return this.warning != null; }

}
