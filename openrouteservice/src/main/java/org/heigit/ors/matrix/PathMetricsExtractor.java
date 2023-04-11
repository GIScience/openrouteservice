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
package org.heigit.ors.matrix;

import com.graphhopper.routing.SPTEntry;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.RoutingCHEdgeIteratorState;
import com.graphhopper.storage.RoutingCHGraph;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.util.DistanceUnitUtil;

public class PathMetricsExtractor {
	private final int metrics;
	private final Graph graph;
	private RoutingCHGraph chGraph;
	private final Weighting weighting;
	private final Weighting timeWeighting;
	private double edgeDistance;
	private double edgeWeight;
	private double edgeTime;
	private final DistanceUnit distUnits;
	private boolean reverseOrder = true;
	private static final boolean UNPACK_DISTANCE = false;

	public PathMetricsExtractor(int metrics, Graph graph, FlagEncoder encoder, Weighting weighting, DistanceUnit units) {
		this.metrics = metrics;
		this.graph = graph;
		this.weighting = weighting;
		timeWeighting = new FastestWeighting(encoder);
		distUnits = units;

		if (graph instanceof RoutingCHGraph)
			chGraph = (RoutingCHGraph)graph;
		else if (graph instanceof QueryGraph) {
			QueryGraph qGraph = (QueryGraph)graph;
			Graph mainGraph = qGraph.getBaseGraph();
			if (mainGraph instanceof RoutingCHGraph)
				chGraph = (RoutingCHGraph)mainGraph;
		}
	}

	public void setEmptyValues(int sourceIndex, MatrixLocations dstData, float[] times, float[] distances, float[] weights) {
		int offset = sourceIndex * dstData.size();
		for (int i = 0; i < dstData.getNodeIds().length; i++) {
			if (times != null)
				times[offset+i] = -1;
			if (distances != null)
				distances[offset+i] = -1;
			if (weights != null)
				weights[offset+i] = -1;
		}
	}

	public void calcValues(int sourceIndex, SPTEntry[] targets, MatrixLocations dstData, float[] times, float[] distances, float[] weights) throws IllegalStateException, StatusCodeException {
		if (targets == null)
			throw new IllegalStateException("Target destinations not set"); 

		int index = sourceIndex * dstData.size();
		double pathTime;
		double pathDistance;
		double pathWeight;
		boolean calcTime = MatrixMetricsType.isSet(metrics, MatrixMetricsType.DURATION);
		boolean calcDistance = MatrixMetricsType.isSet(metrics, MatrixMetricsType.DISTANCE);
		boolean calcWeight = MatrixMetricsType.isSet(metrics, MatrixMetricsType.WEIGHT);

		for (int i = 0; i < targets.length; ++i) {
			SPTEntry goalEdge = targets[i];

			if (goalEdge != null) {
				pathTime = 0.0;
				pathDistance = 0.0; 
				pathWeight = 0.0;

				while (EdgeIterator.Edge.isValid(goalEdge.edge)) {

						if (chGraph != null) {
							RoutingCHEdgeIteratorState iterState = (RoutingCHEdgeIteratorState) graph.getEdgeIteratorState(goalEdge.edge, goalEdge.adjNode);
							EdgeIteratorState baseIterator = chGraph.getBaseGraph().getEdgeIteratorState(iterState.getOrigEdge(), iterState.getAdjNode());

							if (calcWeight || calcTime || UNPACK_DISTANCE) {
								if (iterState.isShortcut()) {
									if (chGraph.getLevel(iterState.getBaseNode()) > chGraph.getLevel(iterState.getAdjNode())) {
										reverseOrder = true;
										extractEdgeValues(iterState, false);
									} else {
										reverseOrder = false;
										extractEdgeValues(iterState, true);
									}
								} else {
									extractEdgeValues(iterState, false);
								}

								if (UNPACK_DISTANCE)
									edgeDistance = (distUnits == DistanceUnit.METERS) ? edgeDistance : DistanceUnitUtil.convert(edgeDistance, DistanceUnit.METERS, distUnits);
							}

							if (!UNPACK_DISTANCE && calcDistance)
								edgeDistance = (distUnits == DistanceUnit.METERS)
										? baseIterator.getDistance()
							            : DistanceUnitUtil.convert(baseIterator.getDistance(), DistanceUnit.METERS, distUnits);
						} else {
							EdgeIteratorState iter = graph.getEdgeIteratorState(goalEdge.edge, goalEdge.adjNode);
							if (calcDistance)
								edgeDistance = (distUnits == DistanceUnit.METERS) ? iter.getDistance(): DistanceUnitUtil.convert(iter.getDistance(), DistanceUnit.METERS, distUnits);

							if (calcTime)
								edgeTime = timeWeighting.calcEdgeMillis(iter, false, EdgeIterator.NO_EDGE) / 1000.0;

							if (calcWeight)
								edgeWeight = weighting.calcEdgeWeight(iter, false, EdgeIterator.NO_EDGE);
						}

						pathDistance += edgeDistance;
						pathTime += edgeTime;
						pathWeight += edgeWeight;


					goalEdge = goalEdge.parent;

					if (goalEdge == null)
						break;
				}
			} else {
				pathTime = -1;
				pathDistance= -1;
				pathWeight = -1;
			}

			if (calcTime)
				times[index] = (float)pathTime;

			if (calcDistance)
				distances[index] = (float)pathDistance;

			if (calcWeight)
				weights[index] = (float)pathWeight;

			index++;
		}
	}

	private void extractEdgeValues(RoutingCHEdgeIteratorState iterState, boolean reverse) {
		if (iterState.isShortcut()) {
			edgeDistance = 0.0;
			edgeTime = 0.0;
			edgeWeight = 0.0;

			if ((chGraph.getLevel(iterState.getBaseNode()) < chGraph.getLevel(iterState.getAdjNode())))
				reverse = !reverse;

			expandEdge(iterState, reverse);  
		} else {
			EdgeIteratorState baseIterator = chGraph.getBaseGraph().getEdgeIteratorState(iterState.getOrigEdge(), iterState.getAdjNode());
			if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.DISTANCE))
				edgeDistance = baseIterator.getDistance();
			if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.DURATION))
				edgeTime = iterState.getTime(reverse) / 1000.0;
			if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.WEIGHT))
				edgeWeight = iterState.getWeight(reverse);
		}
	}

	private void expandEdge(RoutingCHEdgeIteratorState iterState, boolean reverse) {
		if (!iterState.isShortcut()) {
			EdgeIteratorState baseIterator = chGraph.getBaseGraph().getEdgeIteratorState(iterState.getOrigEdge(), iterState.getAdjNode());
			if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.DISTANCE))
				edgeDistance += baseIterator.getDistance();
			if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.DURATION))
				edgeTime += iterState.getTime(reverse) / 1000.0;
			if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.WEIGHT))
				edgeWeight += iterState.getWeight(reverse);
			return;
		}

		int skippedEdge1 = iterState.getSkippedEdge1();
		int skippedEdge2 = iterState.getSkippedEdge2();
		int from = iterState.getBaseNode();
		int to = iterState.getAdjNode();

		// get properties like speed of the edge in the correct direction
		if (reverse) {
			int tmp = from;
			from = to;
			to = tmp;
		}

		// getEdgeProps could possibly return an empty edge if the shortcut is available for both directions
		if (reverseOrder) {
			RoutingCHEdgeIteratorState edgeState = chGraph.getEdgeIteratorState(skippedEdge1, to);
			boolean empty = edgeState == null;
			if (empty)
				edgeState = chGraph.getEdgeIteratorState(skippedEdge2, to);

			expandEdge(edgeState, false);

			if (empty)
				edgeState = chGraph.getEdgeIteratorState(skippedEdge1, from);
			else
				edgeState = chGraph.getEdgeIteratorState(skippedEdge2, from);

			expandEdge(edgeState, true);
		} 
		else
		{
			RoutingCHEdgeIteratorState iter = chGraph.getEdgeIteratorState(skippedEdge1, from);
			boolean empty = iter == null;
			if (empty)
				iter = chGraph.getEdgeIteratorState(skippedEdge2, from);

			expandEdge(iter, true);

			if (empty)
				iter = chGraph.getEdgeIteratorState(skippedEdge1, to);
			else
				iter = chGraph.getEdgeIteratorState(skippedEdge2, to);

			expandEdge(iter, false);
		} 
	}
}
