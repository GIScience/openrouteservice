/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for 
 *  additional information regarding copyright ownership.
 * 
 *  GraphHopper GmbH licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in 
 *  compliance with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.routing.graphhopper.extensions.core;

import com.graphhopper.routing.*;
import com.graphhopper.routing.ch.Path4CH;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.*;

import java.util.PriorityQueue;

/**
 * Calculates best path using core routing algorithm.
 *
 * @author Andrzej Oles
 */

public abstract class AbstractCoreRoutingAlgorithm extends AbstractRoutingAlgorithm {
    protected boolean finishedFrom;
    protected boolean finishedTo;
    int visitedCountFrom1;
    int visitedCountTo1;
    int visitedCountFrom2;
    int visitedCountTo2;
    int visitedEdgesALTCount;

    protected PathBidirRef bestPath;
    protected boolean updateBestPath = true;

    CHGraph chGraph;
    int coreNodeLevel;

    private CoreDijkstraFilter additionalEdgeFilter;

    boolean inCore;

    public AbstractCoreRoutingAlgorithm(Graph graph, Weighting weighting, TraversalMode tMode) {
        super(graph, weighting, tMode);

        int size = Math.min(2000, Math.max(200, graph.getNodes() / 10));
        initCollections(size);

        chGraph = (CHGraph) ((QueryGraph) graph).getMainGraph();
        coreNodeLevel = chGraph.getNodes() + 1;
    }

    protected abstract void initCollections(int size);

    public abstract void initFrom(int from, double weight);

    public abstract void initTo(int to, double weight);

    public abstract boolean fillEdgesFrom();

    public abstract boolean fillEdgesTo();

    public abstract boolean finishedPhase1();

    abstract void runPhase2();

    public abstract boolean finishedPhase2();

    void runPhase1() {
        while (!finishedPhase1() && !isMaxVisitedNodesExceeded()) {
            if (!finishedFrom)
                finishedFrom = !fillEdgesFrom();

            if (!finishedTo)
                finishedTo = !fillEdgesTo();
        }
    }

    @Override
    protected boolean finished() {
        return finishedPhase2();
    }

    protected Path createAndInitPath() {
        bestPath = new Path4CH(graph, graph.getBaseGraph(), weighting);
        return bestPath;
    }

    @Override
    protected Path extractPath() {
        if (finished())
            return bestPath.extract();

        return bestPath;
    }

    protected void setUpdateBestPath(boolean b) {
        updateBestPath = b;
    }

    protected void runAlgo() {
        // PHASE 1: run modified CH outside of core to find entry points
        inCore = false;
        additionalEdgeFilter.setInCore(false);
        runPhase1();

        // PHASE 2 Perform routing in core with the restrictions filter
        additionalEdgeFilter.setInCore(true);
        inCore = true;
        runPhase2();
    }

    @Override
    public Path calcPath(int from, int to) {
        checkAlreadyRun();
        createAndInitPath();
        initFrom(from, 0);
        initTo(to, 0);
        runAlgo();
        //System.out.println("Visited edges Core-ALT: " + visitedEdgesALTCount );
        return extractPath();
    }

    @Override
    public int getVisitedNodes() {
        return getVisitedNodesPhase1() + getVisitedNodesPhase2();
    }

    public int getVisitedNodesPhase1() {
        return visitedCountFrom1 + visitedCountTo1;
    }

    public int getVisitedNodesPhase2() {
        return visitedCountFrom2 + visitedCountTo2;
    }

    public RoutingAlgorithm setEdgeFilter(CoreDijkstraFilter additionalEdgeFilter) {
        this.additionalEdgeFilter = additionalEdgeFilter;
        return this;
    }

    protected boolean accept(EdgeIterator iter, int prevOrNextEdgeId) {
        if (!traversalMode.hasUTurnSupport() && iter.getEdge() == prevOrNextEdgeId)
            return false;

        return additionalEdgeFilter == null || additionalEdgeFilter.accept(iter);
    }
}
