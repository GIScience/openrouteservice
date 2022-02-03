/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
 */
package org.heigit.ors.routing.algorithms;

import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.TurnWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import org.heigit.ors.services.matrix.MatrixServiceSettings;

public abstract class AbstractManyToManyRoutingAlgorithm implements ManyToManyRoutingAlgorithm {
	protected final Graph graph;
	protected final Weighting weighting;
	protected final FlagEncoder flagEncoder;
	protected final TraversalMode traversalMode;
	protected NodeAccess nodeAccess;
	protected EdgeExplorer inEdgeExplorer;
	protected EdgeExplorer outEdgeExplorer;
	protected int maxVisitedNodes = Integer.MAX_VALUE;
	private EdgeFilter additionalEdgeFilter;

	/**
	 * @param graph
	 *            specifies the graph where this algorithm will run on
	 * @param weighting
	 *            set the used weight calculation (e.g. fastest, shortest).
	 * @param traversalMode
	 *            how the graph is traversed e.g. if via nodes or edges.
	 */
	public AbstractManyToManyRoutingAlgorithm(Graph graph, Weighting weighting, TraversalMode traversalMode) {
		this.weighting = weighting;
		flagEncoder = weighting.getFlagEncoder();
		this.traversalMode = traversalMode;
		this.graph = graph;
		nodeAccess = graph.getNodeAccess();
		outEdgeExplorer = graph.createEdgeExplorer(DefaultEdgeFilter.outEdges(flagEncoder));
		inEdgeExplorer = graph.createEdgeExplorer(DefaultEdgeFilter.inEdges(flagEncoder));
	}

	@Override
	public void setMaxVisitedNodes(int numberOfNodes) {
		maxVisitedNodes = numberOfNodes;
	}

	public AbstractManyToManyRoutingAlgorithm setEdgeFilter(EdgeFilter additionalEdgeFilter) {
		this.additionalEdgeFilter = additionalEdgeFilter;
		return this;
	}

	protected boolean accept(EdgeIterator iter, int prevOrNextEdgeId) {
		if (MatrixServiceSettings.getUTurnCost() == TurnWeighting.INFINITE_U_TURN_COSTS && iter.getEdge() == prevOrNextEdgeId) {
			return false;
		} else {
			return additionalEdgeFilter == null || additionalEdgeFilter.accept(iter);
		}
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
