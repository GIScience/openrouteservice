/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.routing.algorithms;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.Parameters;

import java.util.PriorityQueue;

public class DijkstraOneToManyAlgorithm extends AbstractOneToManyRoutingAlgorithm {
    protected IntObjectMap<SPTEntry> _fromMap;
    protected PriorityQueue<SPTEntry> _fromHeap;
    protected SPTEntry _currEdge;
    private int _visitedNodes;
    
    private int _targetsFound = 0;
    private IntObjectMap<SPTEntry> _targets;
    private int _targetsCount = 0;

    public DijkstraOneToManyAlgorithm(Graph graph, Weighting weighting, TraversalMode tMode) {
        super(graph, weighting, tMode);
        int size = Math.min(Math.max(200, graph.getNodes() / 10), 2000);
        initCollections(size);
    }

    protected void initCollections(int size) {
        _fromHeap = new PriorityQueue<SPTEntry>(size);
        _fromMap = new GHIntObjectHashMap<SPTEntry>(size);
        _targets = new GHIntObjectHashMap<SPTEntry>();
    }
    
    public void reset()
    {
    	_fromHeap.clear();
    	_fromMap.clear();
    	_targetsFound = 0;
    }
    
    public int getFoundTargets()
    {
    	return _targetsFound;
    }
    
    public int getTargetsCount()
    {
    	return _targetsCount;	
    }
    
    public void prepare(int[] from, int[] to)
    {
    	this._targets.clear();
    	
    	for (int i = 0; i < to.length; ++i)
    	{
    		int nodeId = to[i];
    		if (nodeId >= 0)
    			this._targets.put(nodeId, new SPTEntry(EdgeIterator.NO_EDGE, nodeId, 1));
    	}
    }
    
    @Override
    public SPTEntry[] calcPaths(int from, int[] to) {
    	_targetsCount = _targets.containsKey(from) ? _targets.size() - 1 : _targets.size();
    	
    	if (_targetsCount > 0)
    	{
    		_currEdge = createSPTEntry(from, 0);
    		if (!traversalMode.isEdgeBased()) {
    			_fromMap.put(from, _currEdge);
    		}
    		
    		runAlgo();
    	}
    	
    	SPTEntry[] res = new SPTEntry[to.length];
    	
    	for (int i = 0; i < to.length; i++)
    	{
    		int nodeId = to[i];
    		if (nodeId >= 0)
    			res[i] = _fromMap.get(to[i]);
    	}
    	
        return res;
    }

    protected void runAlgo() {
        EdgeExplorer explorer = outEdgeExplorer;
        while (true) {
            _visitedNodes++;
            if (isMaxVisitedNodesExceeded() || finished())
                break;

            int startNode = _currEdge.adjNode;
            EdgeIterator iter = explorer.setBaseNode(startNode);
            while (iter.next()) {
                if (!accept(iter, _currEdge.edge))
                    continue;

                int traversalId = traversalMode.createTraversalId(iter, false);
                double tmpWeight = weighting.calcWeight(iter, false, _currEdge.edge) + _currEdge.weight;
                if (Double.isInfinite(tmpWeight))
                    continue;

                SPTEntry nEdge = _fromMap.get(traversalId);
                if (nEdge == null) {
                    nEdge = new SPTEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
                    nEdge.parent = _currEdge;
                    _fromMap.put(traversalId, nEdge);
                    _fromHeap.add(nEdge);
                } else if (nEdge.weight > tmpWeight) {
                    _fromHeap.remove(nEdge);
                    nEdge.edge = iter.getEdge();
                    nEdge.weight = tmpWeight;
                    nEdge.parent = _currEdge;
                    _fromHeap.add(nEdge);
                } else
                    continue;
            }

            if (_fromHeap.isEmpty())
                break;

            _currEdge = _fromHeap.poll();
            if (_currEdge == null)
                throw new AssertionError("Empty edge cannot happen");
        }
    }

    private boolean finished() {
    	if (_currEdge.edge != -1)
    	{
    		SPTEntry entry = _targets.get(_currEdge.adjNode);
    		if (entry != null)
    		{
    			entry.adjNode = _currEdge.adjNode;
    			entry.weight = _currEdge.weight;
    			entry.edge = _currEdge.edge;
    			entry.parent = _currEdge.parent;

    			// MARQ24 - this looks quite strange to me!
                // ORG CODE START
    			// entry.visited = entry.visited;
                // ORG CODE END
    			// IMHO this should be: [and also take over the originalEdge value!
                entry.visited = _currEdge.visited;
                entry.originalEdge = _currEdge.originalEdge;

    			_targetsFound++;
    		}
    	}
    	
    	return _targetsFound == _targetsCount;
    }


    @Override
    public int getVisitedNodes() {
        return _visitedNodes;
    }

    @Override
    public String getName() {
        return Parameters.Algorithms.DIJKSTRA;
    }
}
