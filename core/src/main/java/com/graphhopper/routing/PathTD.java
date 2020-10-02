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

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

/**
 * This class creates a path from a time-dependent Dijkstra
 * <p>
 *
 * @author Andrzej Oles
 */
public class PathTD extends Path {
    private boolean reverse = false;

    public PathTD(Graph g, Weighting weighting) {
        super(g, weighting);
    }

    public PathTD setReverse(boolean reverse) {
        this.reverse = reverse;
        return this;
    }

    /**
     * Extracts path from two shortest-path-tree
     */
    @Override
    public Path extract() {
        if (isFound())
            throw new IllegalStateException("Extract can only be called once");

        extractSW.start();
        SPTEntry currEdge = sptEntry;
        int target = currEdge.adjNode;
        boolean nextEdgeValid = EdgeIterator.Edge.isValid(currEdge.edge);
        while (nextEdgeValid) {
            // the reverse search needs the next edge
            nextEdgeValid = EdgeIterator.Edge.isValid(currEdge.parent.edge);
            processEdge(currEdge);
            currEdge = currEdge.parent;
        }
        int source = currEdge.adjNode;
        setFromToNode(source, target);
        if (!reverse) reverseOrder();
        extractSW.stop();
        return setFound(true);
    }

    void setFromToNode(int source, int target) {
        setFromNode(reverse ? target : source);
        setEndNode(reverse ? source : target);
    }

    protected void processEdge(SPTEntry currEdge) {
        int edgeId = currEdge.edge;
        EdgeIteratorState iter = graph.getEdgeIteratorState(edgeId, currEdge.adjNode);
        distance += iter.getDistance();
        addTime((reverse ? -1 : 1) * (currEdge.time - currEdge.parent.time));
        addEdge(edgeId);
    }

}
