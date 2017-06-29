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
import com.graphhopper.routing.AbstractRoutingAlgorithmCH;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.DownLevelEdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.SPTEntry;

/**
 * Common subclass for bidirectional PHAST.
 * <p>
 *
 * @author Peter Karich
 */
public abstract class AbstractBidirAlgoPHAST extends AbstractRoutingAlgorithmCH {
	protected boolean finishedFrom;
	protected boolean finishedTo;
	int visitedCountFrom;
	int visitedCountTo;
	CHGraph chGraph;
	FlagEncoder encoder;

	public AbstractBidirAlgoPHAST(CHGraph chGraph, Weighting weighting, TraversalMode tMode) {
		super(chGraph, weighting, tMode, true);
		this.chGraph = chGraph;
	}

	abstract IntObjectMap<SPTEntry> init(int from, double dist);

	abstract void initDownwardPHAST(int from, double dist);

	protected abstract double getCurrentFromWeight();

	abstract boolean fillEdgesFrom();

	abstract boolean downwardPHAST();

	@Override
	public Path calcPath(int from, int to) {
		throw new IllegalStateException("No single path defined for RPHAST");
	}

	public IntObjectMap<SPTEntry> calcMatrix(int from) {
		checkAlreadyRun();
		IntObjectMap<SPTEntry> bestWeightMapFrom = init(from, 0);

		runAlgo();

		initDownwardPHAST(additionalEdgeFilter.getHighestNode(),
				bestWeightMapFrom.get(additionalEdgeFilter.getHighestNode()).weight);
		System.out.println("Highest node: " + additionalEdgeFilter.getHighestNode() + ", weight: "
				+ bestWeightMapFrom.get(additionalEdgeFilter.getHighestNode()).weight);
		DownLevelEdgeFilter downFilter = new DownLevelEdgeFilter(chGraph, encoder);
		downFilter.setHighestNode(additionalEdgeFilter.getHighestNode());
		this.setEdgeFilter(downFilter);
		runDownwardsAlgo();
		return bestWeightMapFrom;
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
}
