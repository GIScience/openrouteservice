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

import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;

import heigit.ors.routing.graphhopper.extensions.storages.MultiTreeSPEntry;

public abstract class AbstractManyToManyRoutingAlgorithm implements ManyToManyRoutingAlgorithm {
	protected final Graph _graph;
	protected final Weighting _weighting;
	protected final FlagEncoder _flagEncoder;
	protected final TraversalMode _traversalMode;
	protected NodeAccess _nodeAccess;
	protected EdgeExplorer _inEdgeExplorer;
	protected EdgeExplorer _outEdgeExplorer;
	protected int _maxVisitedNodes = Integer.MAX_VALUE;
	private EdgeFilter _additionalEdgeFilter;

	/**
	 * @param graph
	 *            specifies the graph where this algorithm will run on
	 * @param weighting
	 *            set the used weight calculation (e.g. fastest, shortest).
	 * @param traversalMode
	 *            how the graph is traversed e.g. if via nodes or edges.
	 */
	public AbstractManyToManyRoutingAlgorithm(Graph graph, Weighting weighting, TraversalMode traversalMode) {
		_weighting = weighting;
		_flagEncoder = weighting.getFlagEncoder();
		_traversalMode = traversalMode;
		_graph = graph;
		_nodeAccess = graph.getNodeAccess();
		_outEdgeExplorer = graph.createEdgeExplorer(new DefaultEdgeFilter(_flagEncoder, false, true));
		_inEdgeExplorer = graph.createEdgeExplorer(new DefaultEdgeFilter(_flagEncoder, true, false));
	}

	@Override
	public void setMaxVisitedNodes(int numberOfNodes) {
		_maxVisitedNodes = numberOfNodes;
	}

	public AbstractManyToManyRoutingAlgorithm setEdgeFilter(EdgeFilter additionalEdgeFilter) {
		_additionalEdgeFilter = additionalEdgeFilter;
		return this;
	}

	protected boolean accept(EdgeIterator iter, int prevOrNextEdgeId) {
		if (!_traversalMode.hasUTurnSupport() && iter.getEdge() == prevOrNextEdgeId)
			return false;

		return _additionalEdgeFilter == null || _additionalEdgeFilter.accept(iter);
	}

	// protected MultiTreeSPEntry createMultiTreeSPEntry(int node, double
	// weight) {
	// return new MultiTreeSPEntry(EdgeIterator.NO_EDGE, node, weight);
	// }

	public abstract MultiTreeSPEntry[] calcPaths(int[] from, int[] to);

	public abstract void reset();

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public String toString() {
		return getName() + "|" + _weighting;
	}

	protected boolean isMaxVisitedNodesExceeded() {
		return _maxVisitedNodes < getVisitedNodes();
	}
}
