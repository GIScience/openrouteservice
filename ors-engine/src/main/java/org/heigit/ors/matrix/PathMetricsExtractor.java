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
import com.graphhopper.routing.SPTEntry;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.util.DistanceUnitUtil;

public class PathMetricsExtractor {
    private final int metrics;
    private final Graph graph;
    private final Weighting weighting;
    private final DistanceUnit distUnits;
    private final GHLongObjectHashMap<MetricsItem> edgeMetrics = new GHLongObjectHashMap<>();

    private static class MetricsItem {
        protected double distance;
        protected double time;
        protected double weight;
    }

    public PathMetricsExtractor(int metrics, Graph graph, Weighting weighting, DistanceUnit units) {
        this.metrics = metrics;
        this.graph = graph;
        this.weighting = weighting;
        this.distUnits = units;
    }

    public void setEmptyValues(int sourceIndex, MatrixLocations dstData, float[] times, float[] distances, float[] weights) {
        int offset = sourceIndex * dstData.size();
        for (int i = 0; i < dstData.getNodeIds().length; i++) {
            if (times != null)
                times[offset + i] = -1;
            if (distances != null)
                distances[offset + i] = -1;
            if (weights != null)
                weights[offset + i] = -1;
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
                    EdgeIteratorState iter = graph.getEdgeIteratorState(goalEdge.edge, goalEdge.adjNode);
                    long edgeKey = iter.getEdgeKey();
                    MetricsItem edgeMetricsItem = edgeMetrics.get(edgeKey);

                    if (edgeMetricsItem == null) {
                        edgeMetricsItem = new MetricsItem();
                        if (calcDistance)
                            edgeMetricsItem.distance = (distUnits == DistanceUnit.METERS) ? iter.getDistance() : DistanceUnitUtil.convert(iter.getDistance(), DistanceUnit.METERS, distUnits);
                        if (calcTime)
                            edgeMetricsItem.time = weighting.calcEdgeMillis(iter, false, EdgeIterator.NO_EDGE) / 1000.0;
                        if (calcWeight)
                            edgeMetricsItem.weight = weighting.calcEdgeWeight(iter, false, EdgeIterator.NO_EDGE);
                        edgeMetrics.put(edgeKey, edgeMetricsItem);
                    }

                    pathDistance += edgeMetricsItem.distance;
                    pathTime += edgeMetricsItem.time;
                    pathWeight += edgeMetricsItem.weight;

                    goalEdge = goalEdge.parent;

                    if (goalEdge == null)
                        break;
                }
            } else {
                pathTime = -1;
                pathDistance = -1;
                pathWeight = -1;
            }

            if (calcTime)
                times[index] = (float) pathTime;

            if (calcDistance)
                distances[index] = (float) pathDistance;

            if (calcWeight)
                weights[index] = (float) pathWeight;

            index++;
        }
    }
}
