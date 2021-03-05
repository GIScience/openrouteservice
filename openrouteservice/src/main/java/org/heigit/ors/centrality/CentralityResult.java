package org.heigit.ors.centrality;

import com.vividsolutions.jts.geom.Coordinate;
import org.heigit.ors.common.Pair;

import java.util.HashMap;
import java.util.Map;

public class CentralityResult {
    private Map<Coordinate, Double> nodeCentralityScores;
    private Map<Pair<Coordinate, Coordinate>, Double> edgeCentralityScores;
    private Map<Coordinate, Integer> nodes;
    private Map<Pair<Coordinate, Coordinate>, Pair<Integer, Integer>> edges;


    public CentralityResult() {
        this.nodeCentralityScores = new HashMap<>();
        this.nodes = new HashMap<>();
    }

    public CentralityResult(Map<Coordinate, Double> nodeCentralityScores) {
        this.setNodeCentralityScores(nodeCentralityScores);
    }

    public Map<Coordinate, Double> getNodeCentralityScores() {
        return nodeCentralityScores;
    }

    public void setNodeCentralityScores(Map<Coordinate, Double> nodeCentralityScores) {
        this.nodeCentralityScores = nodeCentralityScores;

    } public Map<Pair<Coordinate, Coordinate>, Double> getEdgeCentralityScores() {
        return edgeCentralityScores;
    }

    public void setEdgeCentralityScores(Map<Pair<Coordinate, Coordinate>, Double> edgeCentralityScores) {
        this.edgeCentralityScores = edgeCentralityScores;
    }

    public Map<Pair<Coordinate, Coordinate>, Pair<Integer, Integer>> getEdges() {
        return edges;
    }

    public void setEdges(Map<Pair<Coordinate, Coordinate>, Pair<Integer, Integer>> edges) {
        this.edges = edges;
    }

    public Map<Coordinate, Integer> getNodes() {return nodes; }

    public void setNodes(Map<Coordinate, Integer> nodes) {
        this.nodes = nodes;
    }

    public void addNodeCentralityScore(Coordinate coord, Double score) {
        this.nodeCentralityScores.put(coord, score);
    }

    public void addEdgeCentralityScore(Pair<Coordinate, Coordinate> coord, Double score) {
        this.edgeCentralityScores.put(coord, score);
    }

    public void addNode(Integer node, Coordinate coord) {
        this.nodes.put(coord, node);
    }

    public void addEdge(Pair<Integer, Integer> node, Pair<Coordinate, Coordinate> coord) {
        this.edges.put(coord, node);
    }
}
