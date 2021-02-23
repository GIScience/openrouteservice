package org.heigit.ors.centrality;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.HashMap;

public class CentralityResult {
    private HashMap<Coordinate, Double> centralityScores;
    private HashMap<Coordinate, Integer> nodes;

    public CentralityResult() {
        this.centralityScores = new HashMap<>();
        this.nodes = new HashMap<>();
    }

    public CentralityResult(HashMap<Coordinate, Double> centralityScores) {
        this.setCentralityScores(centralityScores);
    }

    public HashMap<Coordinate, Double> getCentralityScores() {
        return centralityScores;
    }

    public void setCentralityScores(HashMap<Coordinate, Double> centralityScores) {
        this.centralityScores = centralityScores;
    }

    public HashMap<Coordinate, Integer> getNodes() {return nodes; }

    public void setNodes(HashMap<Coordinate, Integer> nodes) {
        this.nodes = nodes;
    }

    public void addCentralityScore(Coordinate coord, Double score) {
        this.centralityScores.put(coord, score);
    }

    public void addNode(Integer node, Coordinate coord) {
        this.nodes.put(coord, node);
    }
}
