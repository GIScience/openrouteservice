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

import com.graphhopper.coll.GHLongObjectHashMap;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import com.graphhopper.util.CHEdgeIteratorState;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.routing.graphhopper.extensions.storages.MultiTreeSPEntry;
import org.heigit.ors.routing.graphhopper.extensions.storages.MultiTreeSPEntryItem;
import org.heigit.ors.util.DistanceUnitUtil;

public class MultiTreeMetricsExtractor {
	private class MetricsItem {
		private double time;
		private double distance;
		private double weight;

		public double getTime() {
			return time;
		}

		public void setTime(double time) {
			this.time = time;
		}

		public double getDistance() {
			return distance;
		}

		public void setDistance(double distance) {
			this.distance = distance;
		}

		public double getWeight() {
			return weight;
		}

		public void setWeight(double weight) {
			this.weight = weight;
		}
	}

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
	private final GHLongObjectHashMap<MetricsItem> edgeMetrics;
	private final long maxEdgeId;
	private boolean swap;

	public MultiTreeMetricsExtractor(int metrics, Graph graph, FlagEncoder encoder, Weighting weighting,
			DistanceUnit units) {
		this.metrics = metrics;
		this.graph = graph;
		this.weighting = weighting;
		timeWeighting = new FastestWeighting(encoder);
		distUnits = units;
		edgeMetrics = new GHLongObjectHashMap<>();

		if (graph instanceof RoutingCHGraph)
			chGraph = (RoutingCHGraph) graph;
		else if (graph instanceof QueryGraph) {
			QueryGraph qGraph = (QueryGraph) graph;
			Graph mainGraph = qGraph.getBaseGraph();
			if (mainGraph instanceof RoutingCHGraph)
				chGraph = (RoutingCHGraph) mainGraph;
		}

		assert chGraph != null;
		maxEdgeId = chGraph.getEdges();
	}

	public void setSwap(boolean swap){
		this.swap = swap;
	}

	public void setEmptyValues(int sourceIndex, MatrixLocations dstData, float[] times, float[] distances, float[] weights) {
		int i = sourceIndex * dstData.size();
		int[] targetNodes = dstData.getNodeIds();

		for (int c = 0; c < targetNodes.length; c++) {
			if (times != null)
				times[i] = -1;
			if (distances != null)
				distances[i] = -1;
			if (weights != null)
				weights[i] = -1;
			i++;
		}
	}

	public void calcValues(MultiTreeSPEntry[] targets, MatrixLocations srcData, MatrixLocations dstData, float[] times,
						   float[] distances, float[] weights) throws Exception {
		if (targets == null)
			throw new IllegalStateException("Target destinations not set");

		int index;
		double pathTime;
		double pathDistance;
		double pathWeight;
		long entryHash = 0;
		boolean calcTime = MatrixMetricsType.isSet(metrics, MatrixMetricsType.DURATION);
		boolean calcDistance = MatrixMetricsType.isSet(metrics, MatrixMetricsType.DISTANCE);
		boolean calcWeight = MatrixMetricsType.isSet(metrics, MatrixMetricsType.WEIGHT);
		MetricsItem edgeMetricsItem;
		MultiTreeSPEntryItem sptItem;

		for (int i = 0; i < targets.length; ++i) {
			int srcNode = 0;
			for (int j = 0; j < srcData.size(); ++j) {
				pathTime = -1;
				pathDistance = -1;
				pathWeight = -1;

				index = j * dstData.size() + i;

				if (srcData.getNodeId(j) != -1) {
					MultiTreeSPEntry targetEntry = targets[i];

					if (targetEntry != null) {
						//Only set values to 0 if target and start node are the same
						sptItem = targetEntry.getItem(srcNode);

						if(srcData.getNodeId(j) == targetEntry.getAdjNode() || sptItem.getParent() != null) {
							pathTime = 0.0;
							pathDistance = 0.0;
							pathWeight = 0.0;
						}

						if (sptItem.getParent() != null) {
							while (EdgeIterator.Edge.isValid(sptItem.getEdge())) {
								edgeMetricsItem = null;
								if (edgeMetrics != null) {
									entryHash = getMultiTreeSPEntryHash(targetEntry, srcNode);
									edgeMetricsItem = edgeMetrics.get(entryHash);
								}

								if (edgeMetricsItem == null) {
									if (chGraph != null) {
										RoutingCHEdgeIteratorState iterState = (RoutingCHEdgeIteratorState) graph
												.getEdgeIteratorState(sptItem.getEdge(), targetEntry.getAdjNode());

										boolean unpackDistance = true;
										if (calcWeight || calcTime || unpackDistance) {
											if (iterState.isShortcut()) {
												if (chGraph.getLevel(iterState.getBaseNode()) >= chGraph
														.getLevel(iterState.getAdjNode())) {
													reverseOrder = true;
													extractEdgeValues(iterState, swap);
												} else {
													reverseOrder = false;
													extractEdgeValues(iterState, !swap);
												}
											} else {
												extractEdgeValues(iterState, swap);
											}

											if (unpackDistance)
												edgeDistance = (distUnits == DistanceUnit.METERS) ? edgeDistance
														: DistanceUnitUtil.convert(edgeDistance, DistanceUnit.METERS,
														distUnits);
										}

										if (!unpackDistance && calcDistance)
											edgeDistance = (distUnits == DistanceUnit.METERS)
													? 0 // TODO: find out where to get this from: iterState.getDistance()
													: DistanceUnitUtil.convert(0, // TODO: find out where to get this from: iterState.getDistance(),
													DistanceUnit.METERS, distUnits);
									} else {
										EdgeIteratorState iter = graph.getEdgeIteratorState(sptItem.getEdge(),
												targetEntry.getAdjNode());

										if (calcDistance)
											edgeDistance = (distUnits == DistanceUnit.METERS) ? iter.getDistance()
													: DistanceUnitUtil.convert(iter.getDistance(), DistanceUnit.METERS,
													distUnits);

										if (calcTime)
											edgeTime = timeWeighting.calcEdgeMillis(iter, false, EdgeIterator.NO_EDGE)
													/ 1000.0;

										if (calcWeight)
											edgeWeight = weighting.calcEdgeWeight(iter, false, EdgeIterator.NO_EDGE);
									}

									if (edgeMetrics != null) {
										edgeMetricsItem = new MetricsItem();
										edgeMetricsItem.distance = edgeDistance;
										edgeMetricsItem.time = edgeTime;
										edgeMetricsItem.weight = edgeWeight;
										edgeMetrics.put(entryHash, edgeMetricsItem);
									}

									pathDistance += edgeDistance;
									pathTime += edgeTime;
									pathWeight += edgeWeight;
								} else {
									if (calcDistance)
										pathDistance += edgeMetricsItem.distance;
									if (calcTime)
										pathTime += edgeMetricsItem.time;
									if (calcWeight)
										pathWeight += edgeMetricsItem.weight;
								}
								targetEntry = sptItem.getParent();

								if (targetEntry == null)
									break;

								sptItem = targetEntry.getItem(srcNode);
							}
						}
					}
					srcNode++;
				}

				if (calcTime)
					times[index] = (float) pathTime;

				if (calcDistance)
					distances[index] = (float) pathDistance;

				if (calcWeight)
					weights[index] = (float) pathWeight;
			}
		}
	}

	private long getMultiTreeSPEntryHash(MultiTreeSPEntry entry, int sptEntry) {
		return entry.getAdjNode() * maxEdgeId + entry.getItem(sptEntry).getEdge();
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
			if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.DISTANCE))
				edgeDistance = 0; // TODO: find out where to get this from: iterState.getDistance();
			if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.DURATION))
				edgeTime = iterState.getTime(reverse, 0) / 1000.0;
			if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.WEIGHT))
				edgeWeight = iterState.getWeight(reverse);
		}
	}

	private void expandEdge(RoutingCHEdgeIteratorState iterState, boolean reverse) {
		if (!iterState.isShortcut()) {
			if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.DISTANCE))
				edgeDistance += 0; // TODO: find out to get this from: iterState.getDistance();
			if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.DURATION))
				edgeTime += iterState.getTime(reverse, 0) / 1000.0;
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

		// getEdgeProps could possibly return an empty edge if the shortcut is
		// available for both directions
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
		} else {
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