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

import java.util.Collections;
import java.util.List;

import com.graphhopper.routing.util.CHEdgeFilter;
import com.graphhopper.routing.util.CHLevelEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.CHEdgeExplorer;
import com.graphhopper.util.CHEdgeIterator;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

/**
 * @author Peter Karich
 */
public abstract class AbstractRoutingAlgorithmCH implements RoutingAlgorithm {
	protected final CHGraph graph;
	protected final Weighting weighting;
	protected final FlagEncoder flagEncoder;
	protected final TraversalMode traversalMode;
	protected NodeAccess nodeAccess;
	protected CHEdgeExplorer inEdgeExplorer;
	protected CHEdgeExplorer outEdgeExplorer;
	protected CHEdgeExplorer targetEdgeExplorer;
	protected int maxVisitedNodes = Integer.MAX_VALUE;
	protected CHEdgeFilter additionalEdgeFilter;
	protected CHLevelEdgeFilter targetEdgeFilter;

	private boolean alreadyRun;

	/**
	 * @param graph
	 *            specifies the graph where this algorithm will run on
	 * @param weighting
	 *            set the used weight calculation (e.g. fastest, shortest).
	 * @param traversalMode
	 *            how the graph is traversed e.g. if via nodes or edges.
	 */

	public AbstractRoutingAlgorithmCH(CHGraph chGraph, Weighting weighting, TraversalMode traversalMode, boolean downwardSearchAllowed) {
		this.weighting = weighting;
		this.flagEncoder = weighting.getFlagEncoder();
		this.traversalMode = traversalMode;
		this.graph = chGraph;
		this.nodeAccess = chGraph.getNodeAccess();

		if (!downwardSearchAllowed)
		{
			outEdgeExplorer = chGraph.createEdgeExplorer();
			inEdgeExplorer = chGraph.createEdgeExplorer();
			targetEdgeExplorer = chGraph.createEdgeExplorer();
		}
		else
		{
			outEdgeExplorer = chGraph.createEdgeExplorer(EdgeFilter.ALL_EDGES, true);
			inEdgeExplorer = chGraph.createEdgeExplorer(EdgeFilter.ALL_EDGES, true);
			targetEdgeExplorer = chGraph.createEdgeExplorer(EdgeFilter.ALL_EDGES, true);
		}
	}

	@Override
	public void setMaxVisitedNodes(int numberOfNodes) {
		this.maxVisitedNodes = numberOfNodes;
	}

	public RoutingAlgorithm setEdgeFilter(CHEdgeFilter edgeFilter) {
		this.additionalEdgeFilter = edgeFilter;
		return this;
	}

	public RoutingAlgorithm setTargetEdgeFilter(CHLevelEdgeFilter edgeFilter) {
		this.targetEdgeFilter = edgeFilter;
		return this;
	}

	protected boolean accept(CHEdgeIterator iter, int prevOrNextEdgeId) {
		if (!traversalMode.hasUTurnSupport() && iter.getEdge() == prevOrNextEdgeId)
			return false;
		// return false;

		return additionalEdgeFilter == null || additionalEdgeFilter.accept(iter);
	}

	protected void updateBestPath(EdgeIteratorState edgeState, SPTEntry bestSPTEntry, int traversalId) {
	}

	protected void checkAlreadyRun() {
		if (alreadyRun)
			throw new IllegalStateException("Create a new instance per call");

		alreadyRun = true;
	}

	public SPTEntry createSPTEntry(int node, double weight) {
		return new SPTEntry(EdgeIterator.NO_EDGE, node, weight);
	}

	/**
	 * To be overwritten from extending class. Should we make this available in
	 * RoutingAlgorithm interface?
	 * <p>
	 *
	 * @return true if finished.
	 */
	protected abstract boolean finished();

	/**
	 * To be overwritten from extending class. Should we make this available in
	 * RoutingAlgorithm interface?
	 * <p>
	 *
	 * @return true if finished.
	 */

	// public abstract IntObjectMap<SPTEntry> calcMatrix(int from);

	@Override
	public List<Path> calcPaths(int from, int to) {
		return Collections.singletonList(calcPath(from, to));
	}

	protected Path createEmptyPath() {
		return new Path(graph, weighting, -1);
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
