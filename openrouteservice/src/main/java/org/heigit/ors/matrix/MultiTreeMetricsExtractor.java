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
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
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

	private int metrics;
	private Graph graph;
	private CHGraph chGraph;
	private Weighting weighting;
	private Weighting timeWeighting;
	private double edgeDistance;
	private double edgeWeight;
	private double edgeTime;
	private DistanceUnit distUnits;
	private boolean reverseOrder = true;
	private GHLongObjectHashMap<MetricsItem> edgeMetrics;
	private long maxEdgeId;
	private boolean swap;

	public MultiTreeMetricsExtractor(int metrics, Graph graph, FlagEncoder encoder, Weighting weighting,
			DistanceUnit units) {
		this.metrics = metrics;
		this.graph = graph;
		this.weighting = weighting;
		timeWeighting = new FastestWeighting(encoder);
		distUnits = units;
		edgeMetrics = new GHLongObjectHashMap<>();

		if (graph instanceof CHGraph)
			chGraph = (CHGraph) graph;
		else if (graph instanceof QueryGraph) {
			QueryGraph qGraph = (QueryGraph) graph;
			Graph mainGraph = qGraph.getMainGraph();
			if (mainGraph instanceof CHGraph)
				chGraph = (CHGraph) mainGraph;
		}

		assert chGraph != null;
		maxEdgeId = chGraph.getAllEdges().length();
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
										CHEdgeIteratorState iterState = (CHEdgeIteratorState) graph
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
													? iterState.getDistance()
													: DistanceUnitUtil.convert(iterState.getDistance(),
													DistanceUnit.METERS, distUnits);
									} else {
										EdgeIteratorState iter = graph.getEdgeIteratorState(sptItem.getEdge(),
												targetEntry.getAdjNode());

										if (calcDistance)
											edgeDistance = (distUnits == DistanceUnit.METERS) ? iter.getDistance()
													: DistanceUnitUtil.convert(iter.getDistance(), DistanceUnit.METERS,
													distUnits);

										if (calcTime)
											edgeTime = timeWeighting.calcMillis(iter, false, EdgeIterator.NO_EDGE)
													/ 1000.0;

										if (calcWeight)
											edgeWeight = weighting.calcWeight(iter, false, EdgeIterator.NO_EDGE);
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

	private void extractEdgeValues(CHEdgeIteratorState iterState, boolean reverse) {
		if (iterState.isShortcut()) {
			edgeDistance = 0.0;
			edgeTime = 0.0;
			edgeWeight = 0.0;

			if ((chGraph.getLevel(iterState.getBaseNode()) < chGraph.getLevel(iterState.getAdjNode())))
				reverse = !reverse;

			expandEdge(iterState, reverse);
		} else {
			if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.DISTANCE))
				edgeDistance = iterState.getDistance();
			if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.DURATION))
				edgeTime = weighting.calcMillis(iterState, reverse, EdgeIterator.NO_EDGE) / 1000.0;
			if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.WEIGHT))
				edgeWeight = weighting.calcWeight(iterState, reverse, EdgeIterator.NO_EDGE);
		}
	}

	private void expandEdge(CHEdgeIteratorState iterState, boolean reverse) {
		if (!iterState.isShortcut()) {
			if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.DISTANCE))
				edgeDistance += iterState.getDistance();
			if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.DURATION))
				edgeTime += weighting.calcMillis(iterState, reverse, EdgeIterator.NO_EDGE) / 1000.0;
			if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.WEIGHT))
				edgeWeight += weighting.calcWeight(iterState, reverse, EdgeIterator.NO_EDGE);
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
			CHEdgeIteratorState edgeState = chGraph.getEdgeIteratorState(skippedEdge1, to);
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
			CHEdgeIteratorState iter = chGraph.getEdgeIteratorState(skippedEdge1, from);
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
