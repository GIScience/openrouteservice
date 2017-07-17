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

import java.util.Iterator;

import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.carrotsearch.hppc.procedures.IntObjectProcedure;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.util.CHLevelEdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.RPHASTEdgeFilter;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.EdgeIteratorState;

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
	FlagEncoder encoder;
	RPHASTEdgeFilter rphastEdgeFilter;

	public AbstractBidirAlgoRPHAST(Graph graph, FlagEncoder encoder, Weighting weighting, TraversalMode tMode) {
		super(graph, weighting, tMode, true);
		
		this.encoder = encoder;
		
		CHGraph chGraph = null;
		if (graph instanceof CHGraph)
			chGraph = (CHGraph)graph;
		else if (graph instanceof QueryGraph)
		{
			QueryGraph qGraph = (QueryGraph)graph;
			chGraph = (CHGraph)qGraph.getMainGraph();
		}
		
		rphastEdgeFilter = new RPHASTEdgeFilter(chGraph, encoder);
	}

	abstract IntObjectMap<SPTEntry> init(int from, double dist);

	abstract void initDownwardPHAST(int from, double dist);

	abstract IntObjectMap<SPTEntry> createTargetTree(IntObjectMap<SPTEntry> targets);

	abstract IntObjectMap<SPTEntry> createTargetTree(int[] targets);

	protected abstract double getCurrentFromWeight();

	abstract boolean fillEdgesFrom();

	abstract boolean downwardPHAST();

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
	 * @param pos
	 * @return
	 */
	public IntObjectMap<SPTEntry> calcMatrix(int from, int[] intTargetMap, IntObjectMap<SPTEntry> tree, int pos) {
		checkAlreadyRun();
		IntObjectMap<SPTEntry> bestWeightMapFrom = init(from, 0);
		IntObjectMap<SPTEntry> targetMap = new GHIntObjectHashMap<SPTEntry>(intTargetMap.length);
		
		setEdgeFilter(additionalEdgeFilter);
		
		runAlgo(); 
		initDownwardPHAST(additionalEdgeFilter.getHighestNode(), bestWeightMapFrom.get(additionalEdgeFilter.getHighestNode()).weight);
  
		rphastEdgeFilter.setTargetTree(tree);
		rphastEdgeFilter.setHighestNode(additionalEdgeFilter.getHighestNode());
		this.setEdgeFilter(rphastEdgeFilter);
	
		runDownwardsAlgo();
		// From all checked nodes extract only the ones requested via targetMap
		// and set their weight
 
		for (int target : intTargetMap) 
			targetMap.put(target, bestWeightMapFrom.get(target));
		
		return targetMap;
	}

	protected void runAlgo() {
		while (!isMaxVisitedNodesExceeded() && !finishedFrom) {
			if (!finishedFrom)
				finishedFrom = !fillEdgesFrom();
		}

	}

	protected void runDownwardsAlgo() {
		while (!finishedTo) {
			finishedTo = !downwardPHAST();
		}
	}

	@Override
	public int getVisitedNodes() {
		return visitedCountFrom + visitedCountTo;
	}

	public void setEncoder(FlagEncoder encoder) {
		this.encoder = encoder;
	}

	@Override
	protected void updateBestPath(EdgeIteratorState edgeState, SPTEntry entryCurrent, int traversalId) {
		throw new IllegalStateException("No path defined for RPHAST");
	}

}
