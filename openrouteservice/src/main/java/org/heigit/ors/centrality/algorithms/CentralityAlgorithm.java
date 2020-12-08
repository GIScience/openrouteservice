package org.heigit.ors.centrality.algorithms;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;

import java.util.ArrayList;
import java.util.HashMap;

public interface CentralityAlgorithm {
    void init(Graph graph, Weighting weighting);

    HashMap<Integer, Double> compute(ArrayList<Integer> nodes) throws Exception;

    void writeNetworkxGraph(ArrayList<Integer> nodes) throws Exception;
}
