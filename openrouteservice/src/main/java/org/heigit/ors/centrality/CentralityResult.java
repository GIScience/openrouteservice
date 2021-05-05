package org.heigit.ors.centrality;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.HashMap;
import java.util.Map;

public class CentralityResult {
    private Map<Coordinate, Double> centralityScores;
    private Map<Coordinate, Integer> nodes;

    public CentralityResult() {
        this.centralityScores = new HashMap<>();
        this.nodes = new HashMap<>();
    }

    public CentralityResult(Map<Coordinate, Double> centralityScores) {
        this.setCentralityScores(centralityScores);
    }

    public Map<Coordinate, Double> getCentralityScores() {
        return centralityScores;
    }

    public void setCentralityScores(Map<Coordinate, Double> centralityScores) {
        this.centralityScores = centralityScores;
    }

    public Map<Coordinate, Integer> getNodes() {return nodes; }

    public void setNodes(Map<Coordinate, Integer> nodes) {
        this.nodes = nodes;
    }

    public void addCentralityScore(Coordinate coord, Double score) {
        this.centralityScores.put(coord, score);
    }

    public void addNode(Integer node, Coordinate coord) {
        this.nodes.put(coord, node);
    }
}
