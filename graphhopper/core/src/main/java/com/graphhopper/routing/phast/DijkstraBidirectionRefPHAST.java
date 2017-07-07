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
import com.graphhopper.routing.PathBidirRef;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.CHGraphImpl.CHEdgeIteratorImpl;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.CHEdgeExplorer;
import com.graphhopper.util.Parameters;

/**
 * Calculates distances one-to-all.
 * <p>
 * 'Ref' stands for reference implementation and is using the normal
 * Java-'reference'-way.
 * <p>
 *
 * @author Peter Karich, Hendrik Leuschner
 */
public class DijkstraBidirectionRefPHAST extends AbstractBidirAlgoPHAST {
	public IntObjectMap<SPTEntry> bestWeightMapFrom;
	public IntObjectMap<SPTEntry> bestWeightMapOther;

	protected SPTEntry currFrom;
	protected SPTEntry currTo;
	protected PathBidirRef bestPath;
	PriorityQueue<SPTEntry> openSetFrom;
	PriorityQueue<SPTEntry> openSetTo;
	
	private CHGraph chGraph;

	public DijkstraBidirectionRefPHAST(Graph graph, Weighting weighting, TraversalMode tMode) {
		super(graph, weighting, tMode);
		int size = Math.min(Math.max(200, graph.getNodes() / 10), 150_000);
		chGraph = getCHGraph(graph);
		initCollections(size);
	}

	protected void initCollections(int size) {
		openSetFrom = new PriorityQueue<SPTEntry>(size);
		bestWeightMapFrom = new GHIntObjectHashMap<SPTEntry>(size);

		openSetTo = new PriorityQueue<SPTEntry>(size);
	}

	@Override
	public IntObjectMap<SPTEntry> init(int from, double weight) {
		currFrom = createSPTEntry(from, weight);
		openSetFrom.add(currFrom);

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
	public void initDownwardPHAST(int to, double weight) {
		currTo = createSPTEntry(to, weight);
		openSetTo.add(currTo);
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

	@Override
	protected double getCurrentFromWeight() {
		return currFrom.weight;
	}

	@Override
	public boolean fillEdgesFrom() {
		if (openSetFrom.isEmpty()) {

			return false;
		}
		currFrom = openSetFrom.poll();
		fillEdges(currFrom, openSetFrom, bestWeightMapFrom, outEdgeExplorer, false);
		visitedCountFrom++;
		return true;
	}

	@Override
	public boolean downwardPHAST() {
		if (openSetTo.isEmpty()) {

			return false;
		}
		currTo = openSetTo.poll();
		fillEdgesDownwards(currTo, openSetTo, bestWeightMapFrom, inEdgeExplorer, false);
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
		// return currFrom.weight + currTo.weight >= bestPath.getWeight();
	}

	void fillEdges(SPTEntry currEdge, PriorityQueue<SPTEntry> prioQueue, IntObjectMap<SPTEntry> shortestWeightMap,
			CHEdgeExplorer explorer, boolean reverse) {
		CHEdgeIteratorImpl iter = (CHEdgeIteratorImpl) explorer.setBaseNode(currEdge.adjNode);

		while (iter.next()) {

			if (!additionalEdgeFilter.accept(iter)) {
				continue;
			}
			int traversalId = traversalMode.createTraversalId(iter, reverse);
			double tmpWeight = weighting.calcWeight(iter, reverse, currEdge.edge) + currEdge.weight;

			if (Double.isInfinite(tmpWeight)) {
				continue;
			}
			// Keep track of the currently found highest CH level node
			if (chGraph.getLevel(additionalEdgeFilter.getHighestNode()) < chGraph.getLevel(iter.getAdjNode()))
				additionalEdgeFilter.setHighestNode(iter.getAdjNode());
			
			SPTEntry ee = shortestWeightMap.get(traversalId);
			if (ee == null) {
				ee = new SPTEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
				ee.parent = currEdge;
				shortestWeightMap.put(traversalId, ee);
				prioQueue.add(ee);
			} else if (ee.weight > tmpWeight) {
				prioQueue.remove(ee);
				ee.edge = iter.getEdge();
				ee.weight = tmpWeight;
				ee.parent = currEdge;
				prioQueue.add(ee);
			} else
				continue;

		}
	}

	void fillEdgesDownwards(SPTEntry currEdge, PriorityQueue<SPTEntry> prioQueue,
			IntObjectMap<SPTEntry> shortestWeightMap, CHEdgeExplorer explorer, boolean reverse) {
		CHEdgeIteratorImpl iter = (CHEdgeIteratorImpl) explorer.setBaseNode(currEdge.adjNode);

		while (iter.next()) {
			if (!additionalEdgeFilter.accept(iter)) {
				continue;
			}

			int traversalId = traversalMode.createTraversalId(iter, reverse);
			SPTEntry ee = shortestWeightMap.get(traversalId);
			double tmpWeight = weighting.calcWeight(iter, reverse, currEdge.edge) + currEdge.weight;

			if (Double.isInfinite(tmpWeight))
				continue;

			if (ee == null) {
				ee = new SPTEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
				ee.parent = currEdge;
				shortestWeightMap.put(traversalId, ee);
				prioQueue.add(ee);
			} else if (ee.weight > tmpWeight) {
				prioQueue.remove(ee);
				ee.edge = iter.getEdge();
				ee.weight = tmpWeight;
				ee.parent = currEdge;
				prioQueue.add(ee);
			} else if (ee.visited == false) {
				// This is the case if the node has been assigned a weight in
				// the upwards pass (fillEdges). We need to use it in the
				// downwards pass to access lower level nodes, though the weight
				// does not have to be reset necessarily
				prioQueue.remove(ee);
				// ee.edge = iter.getEdge();
				// ee.parent = currEdge;
				prioQueue.add(ee);
				ee.visited = true;
			} else
				continue;
		}
	}

	IntObjectMap<SPTEntry> getBestFromMap() {
		return bestWeightMapFrom;
	}

	void setBestOtherMap(IntObjectMap<SPTEntry> other) {
		bestWeightMapOther = other;
	}

	@Override
	public String getName() {
		return Parameters.Algorithms.DIJKSTRA_BI;
	}

}
