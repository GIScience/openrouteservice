package org.heigit.ors.centrality.algorithms;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import org.heigit.ors.centrality.CentralityRequest;
import org.heigit.ors.centrality.CentralityResult;

public interface CentralityAlgorithm {
    public void init(CentralityRequest req, GraphHopper gh, Graph graph, FlagEncoder encoder, Weighting weighting);

    public CentralityResult compute(int metrics) throws Exception;

}
