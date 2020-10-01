package org.heigit.ors.centrality;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.HashMap;

public class CentralityResult {
    private HashMap<Coordinate, Float> centralityScores;

    public CentralityResult() {};

    public CentralityResult(HashMap<Coordinate, Float> centralityScores) {
        this.setCentralityScores(centralityScores);
    }

    public HashMap<Coordinate, Float> getCentralityScores() {
        return centralityScores;
    }

    public void setCentralityScores(HashMap<Coordinate, Float> centralityScores) {
        this.centralityScores = centralityScores;
    }
}
