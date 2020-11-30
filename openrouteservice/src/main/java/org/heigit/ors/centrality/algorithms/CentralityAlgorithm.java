package org.heigit.ors.centrality.algorithms;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import org.heigit.ors.centrality.CentralityRequest;
import org.heigit.ors.centrality.CentralityResult;

import java.util.ArrayList;

public interface CentralityAlgorithm {
    public void init(Graph graph, Weighting weighting);

    public CentralityResult compute(ArrayList<Integer> nodes) throws Exception;

}
