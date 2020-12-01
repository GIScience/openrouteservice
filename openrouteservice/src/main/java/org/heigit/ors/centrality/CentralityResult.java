package org.heigit.ors.centrality;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.HashMap;

public class CentralityResult {
    private HashMap<Coordinate, Double> centralityScores;

    public CentralityResult() {
        this.centralityScores = new HashMap<>();
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

    public void addCentralityScore(Coordinate coord, Double score) {
        System.out.println("Received coord and score");
        this.centralityScores.put(coord, score);
        System.out.println("Put into Hashmap");
    }
}
