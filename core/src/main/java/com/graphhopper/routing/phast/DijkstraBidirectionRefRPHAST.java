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
package com.graphhopper.routing.phast;

import java.util.PriorityQueue;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.util.CHLevelEdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.Parameters;

/**
 * Calculates distances one-to-many.
 * <p>
 * 'Ref' stands for reference implementation and is using the normal
 * Java-'reference'-way.
 * <p>
 *
 * @author Peter Karich, Hendrik Leuschner
 */
public class DijkstraBidirectionRefRPHAST extends AbstractBidirAlgoRPHAST {
	public IntObjectMap<SPTEntry> bestWeightMapFrom;

	protected SPTEntry currFrom;
	protected SPTEntry currTo;
	private PriorityQueue<SPTEntry> prioQueue;

	public DijkstraBidirectionRefRPHAST(Graph graph, FlagEncoder encoder, Weighting weighting, TraversalMode tMode) {
		super(graph, encoder, weighting, tMode);
		int size = Math.min(Math.max(200, graph.getNodes() / 10), 2000);

		initCollections(size);
		
		CHGraph chGraph = null;
		if (graph instanceof CHGraph)
			chGraph = (CHGraph)graph;
		else if (graph instanceof QueryGraph)
		{
			QueryGraph qGraph = (QueryGraph)graph;
			chGraph = (CHGraph)qGraph.getMainGraph();
		}
					
		setMaxVisitedNodes(Integer.MAX_VALUE);
		setEdgeFilter(new CHLevelEdgeFilter(chGraph, encoder));
		
		targetEdgeFilter = new CHLevelEdgeFilter(chGraph, encoder);
		targetEdgeFilter.setBackwardSearch(true);
	}

	protected void initCollections(int size) {
		prioQueue = new PriorityQueue<SPTEntry>(size);
		bestWeightMapFrom = new GHIntObjectHashMap<SPTEntry>(size);
	}

	@Override
	public IntObjectMap<SPTEntry> init(int from, double weight) {
		currFrom = createSPTEntry(from, weight);
		currFrom.visited = true;
		prioQueue.add(currFrom);

		if (!traversalMode.isEdgeBased()) {
			bestWeightMapFrom.put(from, currFrom);
		} else if (currTo != null && currTo.adjNode == from) {
			// special case of identical start and end
			// bestPath.sptEntry = currFrom;
			// bestPath.edgeTo = currTo;
			finishedFrom = true;
			finishedTo = true;
		}
		return bestWeightMapFrom;
	}

	@Override
	public void initDownwardSearch(int to) {
		// Set the highest node from the upwards pass as the start node for the
		// downwards pass. Keep parent refs for future use in distance
		// extraction.
		currTo = bestWeightMapFrom.get(to);
		currTo.visited = true;
		prioQueue.clear();
		prioQueue.add(currTo);
		if (!traversalMode.isEdgeBased()) {
			bestWeightMapFrom.put(to, currTo);
		} else if (currFrom != null && currFrom.adjNode == to) {
			// special case of identical start and end
			// bestPath.sptEntry = currFrom;
			// bestPath.edgeTo = currTo;
			finishedFrom = true;
			finishedTo = true;
		}
	}

	// Create target SubGraph from node ids
	@Override
	public SubGraph createTargetGraph(int[] targetNodes) {
		SubGraph targetGraph = new SubGraph(graph);
		
		PriorityQueue<Integer> prioQueue = new PriorityQueue<>(200);

		// At the start, the targetGraph and the prioQueue both equal the given
		// targets 

		for (int i = 0; i < targetNodes.length; i++) 
		{
			int nodeId = targetNodes[i];
			if (nodeId > 0)
			{
				targetGraph.addEdge(nodeId, null);
				prioQueue.add(nodeId);
			}
		}

     	while (!prioQueue.isEmpty()) {
			int adjNode = prioQueue.poll();
			EdgeIterator iter = targetEdgeExplorer.setBaseNode(adjNode);
			 
			while (iter.next()) {
				if (!targetEdgeFilter.accept(iter))
					continue;

				if (targetGraph.addEdge(adjNode, iter))
					prioQueue.add(iter.getAdjNode());
			}
		}

		return targetGraph;
	}

	@Override
	public boolean upwardSearch() {
		if (prioQueue.isEmpty()) {
			return false;
		}
		currFrom = prioQueue.poll();
		fillEdgesUpward(currFrom, prioQueue, bestWeightMapFrom, outEdgeExplorer, false);
		visitedCountFrom++;

		return true;
	}

	@Override
	public boolean downwardSearch() {
		if (prioQueue.isEmpty()) {
			return false;
		}
		 
		currTo = prioQueue.poll();
		fillEdgesDownward(currTo , prioQueue, bestWeightMapFrom, outEdgeExplorer, false);
		visitedCountTo++;

		return true;
	}

	// http://www.cs.princeton.edu/courses/archive/spr06/cos423/Handouts/EPP%20shortest%20path%20algorithms.pdf
	// a node from overlap may not be on the best path!
	// => when scanning an arc (v, w) in the forward search and w is scanned in
	// the reverseOrder
	// search, update extractPath = μ if df (v) + (v, w) + dr (w) < μ
	@Override
	public boolean finished() {
		if (finishedFrom) {
			return true;
		}

		return false;
	}
	
	private void fillEdgesUpward(SPTEntry currEdge, PriorityQueue<SPTEntry> prioQueue, IntObjectMap<SPTEntry> shortestWeightMap,
			EdgeExplorer explorer, boolean reverse) {
		EdgeIterator iter = explorer.setBaseNode(currEdge.adjNode);

		while (iter.next()) {
			if (!additionalEdgeFilter.accept(iter)) 
				continue;

			double tmpWeight = weighting.calcWeight(iter, reverse, currEdge.edge) + currEdge.weight;

			if (Double.isInfinite(tmpWeight)) 
				continue;

			// Keep track of the currently found highest CH level node
			additionalEdgeFilter.updateHighestNode(iter);

			SPTEntry ee = shortestWeightMap.get(iter.getAdjNode());

			if (ee == null) {
				ee = new SPTEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
				ee.parent = currEdge;
				shortestWeightMap.put(iter.getAdjNode(), ee);
				prioQueue.add(ee);
			} else if (ee.weight > tmpWeight) {
				prioQueue.remove(ee);
				ee.edge = iter.getEdge();
				ee.weight = tmpWeight;
				ee.parent = currEdge;
				prioQueue.add(ee);
			} 
		}
	}

	private void fillEdgesDownward(SPTEntry currEdge, PriorityQueue<SPTEntry> prioQueue,
			IntObjectMap<SPTEntry> shortestWeightMap, EdgeExplorer explorer, boolean reverse) {

		EdgeIterator iter = explorer.setBaseNode(currEdge.adjNode);
		
		if (iter == null) // we reach one of the target nodes
			return;

		while (iter.next())
		{
			// no need in filter, since all edges in targetGraph are valid and acceptable
			//if (!additionalEdgeFilter.accept(iter)) 
			//	continue;
			
			double tmpWeight = weighting.calcWeight(iter, false, currEdge.edge) + currEdge.weight;
			if (Double.isInfinite(tmpWeight))
				continue;
			
			SPTEntry ee = shortestWeightMap.get(iter.getAdjNode());

			if (ee == null) {
				ee = new SPTEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
				ee.parent = currEdge;
				shortestWeightMap.put(iter.getAdjNode(), ee);
				prioQueue.add(ee);
				ee.visited = true; 
			} else if (ee.weight >= tmpWeight) {
				prioQueue.remove(ee);
				ee.edge = iter.getEdge();
				ee.weight = tmpWeight;
				ee.parent = currEdge; 
				prioQueue.add(ee);
			} else if (ee.visited == false) {
				// // This is the case if the node has been assigned a weight in
				// // the upwards pass (fillEdges). We need to use it in the
				// // downwards pass to access lower level nodes, though the
				// weight
				// // does not have to be reset necessarily
				// prioQueue.remove(ee);
				//
				prioQueue.add(ee);
				ee.visited = true;
			}
		}
	}

	@Override
	public String getName() {
		return Parameters.Algorithms.DIJKSTRA_BI;
	}
}
