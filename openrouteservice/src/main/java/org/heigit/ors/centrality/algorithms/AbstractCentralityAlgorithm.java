package org.heigit.ors.centrality.algorithms;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import org.heigit.ors.centrality.CentralityRequest;

public abstract class AbstractCentralityAlgorithm implements CentralityAlgorithm {
    protected GraphHopper graphHopper;
    protected Graph graph;
    protected FlagEncoder encoder;
    protected Weighting weighting;

    public void init(CentralityRequest req, GraphHopper gh, Graph graph, FlagEncoder encoder, Weighting weighting)
    {
        this.graphHopper = gh;
        this.graph = graph;
        this.encoder = encoder;
        this.weighting = weighting;
    }

}
