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
package com.graphhopper.routing;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIteratorState;

import java.util.Collections;
import java.util.List;

/**
 * @author Peter Karich
 */
public abstract class AbstractRoutingAlgorithm implements RoutingAlgorithm {
    protected final Graph graph;
    protected final Weighting weighting;
    protected final TraversalMode traversalMode;
    protected final NodeAccess nodeAccess;
    protected final EdgeExplorer edgeExplorer;
    protected int maxVisitedNodes = Integer.MAX_VALUE;
    private boolean alreadyRun;
    // ORS-GH MOD START - new field
    protected EdgeFilter additionalEdgeFilter;
    // ORS-GH MOS END
    /**
     * @param graph         specifies the graph where this algorithm will run on
     * @param weighting     set the used weight calculation (e.g. fastest, shortest).
     * @param traversalMode how the graph is traversed e.g. if via nodes or edges.
     */
    public AbstractRoutingAlgorithm(Graph graph, Weighting weighting, TraversalMode traversalMode) {
        if (weighting.hasTurnCosts() && !traversalMode.isEdgeBased())
            throw new IllegalStateException("Weightings supporting turn costs cannot be used with node-based traversal mode");
        this.weighting = weighting;
        this.traversalMode = traversalMode;
        this.graph = graph;
        this.nodeAccess = graph.getNodeAccess();
        edgeExplorer = graph.createEdgeExplorer();
    }

    @Override
    public void setMaxVisitedNodes(int numberOfNodes) {
        this.maxVisitedNodes = numberOfNodes;
    }

    // ORS-GH MOD START - additional method for passing additionalEdgeFilter to any algo
    public RoutingAlgorithm setEdgeFilter(EdgeFilter additionalEdgeFilter) {
        this.additionalEdgeFilter = additionalEdgeFilter;
        return this;
    }
    // ORS-GH MOD END

    protected boolean accept(EdgeIteratorState iter, int prevOrNextEdgeId) {
        // for edge-based traversal we leave it for TurnWeighting to decide whether or not a u-turn is acceptable,
        // but for node-based traversal we exclude such a turn for performance reasons already here
        // ORS-GH MOD START - apply additional filters
        // return traversalMode.isEdgeBased() || iter.getEdge() != prevOrNextEdgeId;
        if (!traversalMode.isEdgeBased() && iter.getEdge() == prevOrNextEdgeId)
            return false;
        return additionalEdgeFilter == null || additionalEdgeFilter.accept(iter);
    }

    protected void checkAlreadyRun() {
        if (alreadyRun)
            throw new IllegalStateException("Create a new instance per call");

        alreadyRun = true;
    }

    /**
     * To be overwritten from extending class. Should we make this available in RoutingAlgorithm
     * interface?
     * <p>
     *
     * @return true if finished.
     */
    protected abstract boolean finished();

    /**
     * To be overwritten from extending class. Should we make this available in RoutingAlgorithm
     * interface?
     * <p>
     *
     * @return true if finished.
     */
    protected abstract Path extractPath();

    // ORS-GH MOD START - additional method
    @Override
    public Path calcPath(int from, int to, long at) {
        return calcPath(from, to);
    }
    // ORS-GH MOD END

    @Override
    public List<Path> calcPaths(int from, int to) {
        return Collections.singletonList(calcPath(from, to));
    }

    // ORS-GH MOD START - additional method
    public List<Path> calcPaths(int from, int to, long at) {
        return Collections.singletonList(calcPath(from, to, at));
    }
    // ORS-GH MOD END

    protected Path createEmptyPath() {
        return new Path(graph);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return getName() + "|" + weighting;
    }

    protected boolean isMaxVisitedNodesExceeded() {
        return maxVisitedNodes < getVisitedNodes();
    }
}