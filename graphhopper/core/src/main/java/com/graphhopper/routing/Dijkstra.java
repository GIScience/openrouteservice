/*
 *  Licensed to GraphHopper and Peter Karich under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for 
 *  additional information regarding copyright ownership.
 * 
 *  GraphHopper licenses this file to you under the Apache License, 
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

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.PriorityQueue;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.storage.EdgeEntry;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;

/**
 * Implements a single source shortest path algorithm
 * http://en.wikipedia.org/wiki/Dijkstra's_algorithm
 * <p>
 * @author Peter Karich
 */
public class Dijkstra extends AbstractRoutingAlgorithm
{
    protected TIntObjectMap<EdgeEntry> fromMap;
    protected PriorityQueue<EdgeEntry> fromHeap;
    protected EdgeEntry currEdge;
    protected int visitedNodes;
    protected int to = -1;
    
    protected Boolean reverseDirection = false;
    
    // Runge: this flag has been update to support reverse direction in isochrones
    public void setReverseDirection(Boolean reverse)
    {
    	reverseDirection = reverse;	
    }

    public Dijkstra( Graph g, FlagEncoder encoder, Weighting weighting, TraversalMode tMode )
    {
        super(g, encoder, weighting, tMode);
        initCollections(1000);
    }

    protected void initCollections( int size )
    {
        fromHeap = new PriorityQueue<EdgeEntry>(size);
        fromMap = new TIntObjectHashMap<EdgeEntry>(size);
    }

    @Override
    public Path calcPath( int from, int to, double maxSpeed )
    {
        checkAlreadyRun();
        this.to = to;
        currEdge = createEdgeEntry(from, 0);
        if (!traversalMode.isEdgeBased())
        {
            fromMap.put(from, currEdge);
        }
        runAlgo(maxSpeed);
        return extractPath(maxSpeed);
    }

    protected void runAlgo(double maxSpeed)
    {
        EdgeExplorer explorer = outEdgeExplorer;
        while (true)
        {
            visitedNodes++;
            if (isWeightLimitExceeded() || finished())
                break;

            int startNode = currEdge.adjNode;
            EdgeIterator iter = explorer.setBaseNode(startNode);
            while (iter.next())
            {
                if (!accept(iter, currEdge.edge))
                    continue;

                int traversalId = traversalMode.createTraversalId(iter, false);
                double tmpWeight = weighting.calcWeight(iter, reverseDirection, currEdge.originalEdge) + currEdge.weight; // Runge: substitute  currEdge.edge for currEdge.originalEdge
                if (Double.isInfinite(tmpWeight))
                    continue;

                EdgeEntry nEdge = fromMap.get(traversalId);
                if (nEdge == null)
                {
                    nEdge = new EdgeEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
                    nEdge.parent = currEdge;
                    fromMap.put(traversalId, nEdge);
                    fromHeap.add(nEdge);
                } else if (nEdge.weight > tmpWeight)
                {
                    fromHeap.remove(nEdge);
                    nEdge.edge = iter.getEdge();
                    nEdge.weight = tmpWeight;
                    nEdge.parent = currEdge;
                    fromHeap.add(nEdge);
                } else
                    continue;

                updateBestPath(iter, nEdge, traversalId);
            }

            if (fromHeap.isEmpty())
                break;

            currEdge = fromHeap.poll();
            if (currEdge == null)
                throw new AssertionError("Empty edge cannot happen");
        }
    }

    @Override
    protected boolean finished()
    {
        return currEdge.adjNode == to;
    }

    @Override
    protected Path extractPath(double maxSpeed)
    {
        if (currEdge == null || isWeightLimitExceeded() || !finished())
            return createEmptyPath();

        return new Path(graph, flagEncoder, maxSpeed).setWeight(currEdge.weight).setEdgeEntry(currEdge).extract();
    }

    @Override
    public int getVisitedNodes()
    {
        return visitedNodes;
    }

    @Override
    protected boolean isWeightLimitExceeded()
    {
        return currEdge.weight > weightLimit;
    }

    @Override
    public String getName()
    {
        return AlgorithmOptions.DIJKSTRA;
    }
}
