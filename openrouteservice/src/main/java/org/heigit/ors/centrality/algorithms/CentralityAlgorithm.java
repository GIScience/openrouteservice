package org.heigit.ors.centrality.algorithms;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;

import java.util.List;
import java.util.Map;

public interface CentralityAlgorithm {
    void init(Graph graph, Weighting weighting);

    Map<Integer, Double> compute(List<Integer> nodes) throws Exception;
}
