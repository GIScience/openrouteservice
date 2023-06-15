package org.heigit.ors.centrality;

import org.locationtech.jts.geom.Coordinate;
import org.heigit.ors.common.Pair;

import java.util.HashMap;
import java.util.Map;

public class CentralityResult {
    private Map<Integer, Coordinate> locations;
    private Map<Integer, Double> nodeCentralityScores;
    private Map<Pair<Integer, Integer>, Double> edgeCentralityScores;
    private CentralityWarning warning;

    public CentralityResult() {
        this.locations = new HashMap<>();
        this.nodeCentralityScores = null;
        this.edgeCentralityScores = null;
        this.warning = null;
    }

    public Map<Integer, Double> getNodeCentralityScores() {
        return nodeCentralityScores;
    }

    public void setNodeCentralityScores(Map<Integer, Double> nodeCentralityScores) {
        this.nodeCentralityScores = nodeCentralityScores;
    }

    public boolean hasNodeCentralityScores() {
        return this.nodeCentralityScores != null;
    }

    public Map<Pair<Integer, Integer>, Double> getEdgeCentralityScores() {
        return edgeCentralityScores;
    }

    public void setEdgeCentralityScores(Map<Pair<Integer, Integer>, Double> edgeCentralityScores) {
        this.edgeCentralityScores = edgeCentralityScores;
    }

    public boolean hasEdgeCentralityScores() {
        return this.edgeCentralityScores != null;
    }

    public Map<Integer, Coordinate> getLocations() {return locations; }

    public void setLocations(Map<Integer, Coordinate> locations) {
        this.locations = locations;
    }

    public void addLocation(Integer node, Coordinate coord) {
        this.locations.put(node, coord);
    }

    public CentralityWarning getWarning() { return warning;}

    public void setWarning(CentralityWarning warning) { this.warning = warning; }

    public boolean hasWarning() {return this.warning != null; }



}
