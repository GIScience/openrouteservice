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

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.CHEdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;

/**
 * Common subclass for bidirectional restricted PHAST.
 * <p>
 *
 * @author Peter Karich
 */
public abstract class AbstractBidirAlgoRPHAST extends AbstractRoutingAlgorithmPHAST {
	protected boolean finishedFrom;
	protected boolean finishedTo;
	int visitedCountFrom;
	int visitedCountTo;

	public AbstractBidirAlgoRPHAST(Graph graph, FlagEncoder encoder, Weighting weighting, TraversalMode tMode) {
		super(graph, weighting, tMode, true);
	}

	abstract IntObjectMap<SPTEntry> init(int from, double dist);

	abstract void initDownwardSearch(int from);

	abstract SubGraph createTargetGraph(int[] targets);

	abstract boolean upwardSearch();

	abstract boolean downwardSearch();

	@Override
	public Path calcPath(int from, int to) {
		throw new IllegalStateException("No single path defined for RPHAST");
	}

	/**
	 * Calculate Matrix using existing targetTree
	 * 
	 * @param from
	 * @param intTargetMap
	 * @param times
	 * @return
	 */
	public SPTEntry[] calcPaths(int from, int[] to, SubGraph targetGraph) {
		checkAlreadyRun();
		IntObjectMap<SPTEntry> bestWeightMapFrom = init(from, 0);
		
		setEdgeFilter(additionalEdgeFilter);
		
		runAlgo(); 
		initDownwardSearch(additionalEdgeFilter.getHighestNode());
  
		this.setEdgeFilter(CHEdgeFilter.ALL_EDGES);
	
		outEdgeExplorer = targetGraph.createExplorer();
		
		runDownwardSearch();

		SPTEntry[] targets = new SPTEntry[to.length];

		for (int i = 0; i < to.length; i++)
			targets[i] = bestWeightMapFrom.get(to[i]);
		
		return targets;
	}

	protected void runAlgo() {
		while (!isMaxVisitedNodesExceeded() && !finishedFrom) {
			if (!finishedFrom)
				finishedFrom = !upwardSearch();
		}
	}

	protected void runDownwardSearch() {
		while (!finishedTo) {
			finishedTo = !downwardSearch();
		}
	}

	@Override
	public int getVisitedNodes() {
		return visitedCountFrom + visitedCountTo;
	}
}
