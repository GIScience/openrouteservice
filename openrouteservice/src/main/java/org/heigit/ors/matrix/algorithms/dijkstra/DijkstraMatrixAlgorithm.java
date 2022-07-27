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
package org.heigit.ors.matrix.algorithms.dijkstra;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.SPTEntry;
import com.graphhopper.routing.util.AccessFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import org.heigit.ors.config.MatrixServiceSettings;
import org.heigit.ors.matrix.*;
import org.heigit.ors.matrix.algorithms.AbstractMatrixAlgorithm;
import org.heigit.ors.routing.algorithms.DijkstraOneToManyAlgorithm;

public class DijkstraMatrixAlgorithm extends AbstractMatrixAlgorithm {
    private PathMetricsExtractor pathMetricsExtractor;

    @Override
    public void init(MatrixRequest req, GraphHopper gh, Graph graph, FlagEncoder encoder, Weighting weighting) {
        weighting = graph.wrapWeighting(weighting);
        super.init(req, gh, graph, encoder, weighting);

        pathMetricsExtractor = new PathMetricsExtractor(req.getMetrics(), this.graph, this.encoder, this.weighting, req.getUnits());
    }

    @Override
    public MatrixResult compute(MatrixLocations srcData, MatrixLocations dstData, int metrics) throws Exception {
        MatrixResult mtxResult = new MatrixResult(srcData.getLocations(), dstData.getLocations());

        float[] times = null;
        float[] distances = null;
        float[] weights = null;

        int tableSize = srcData.size() * dstData.size();
        if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.DURATION))
            times = new float[tableSize];
        if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.DISTANCE))
            distances = new float[tableSize];
        if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.WEIGHT))
            weights = new float[tableSize];

        if (!srcData.hasValidNodes() || !dstData.hasValidNodes()) {
            for (int srcIndex = 0; srcIndex < srcData.size(); srcIndex++)
                pathMetricsExtractor.setEmptyValues(srcIndex, dstData, times, distances, weights);
        } else {
            DijkstraOneToManyAlgorithm algorithm = new DijkstraOneToManyAlgorithm(graph, weighting, TraversalMode.NODE_BASED);
            //TODO Refactoring : Check whether this access filter is unnecessary
            algorithm.setEdgeFilter(AccessFilter.allEdges(this.encoder.getAccessEnc()));
            algorithm.prepare(srcData.getNodeIds(), dstData.getNodeIds());
            algorithm.setMaxVisitedNodes(MatrixServiceSettings.getMaximumVisitedNodes());

            int sourceId = -1;

            for (int srcIndex = 0; srcIndex < srcData.size(); srcIndex++) {
                sourceId = srcData.getNodeId(srcIndex);

                if (sourceId == -1) {
                    pathMetricsExtractor.setEmptyValues(srcIndex, dstData, times, distances, weights);
                } else {
                    algorithm.reset();
                    SPTEntry[] targets = algorithm.calcPaths(sourceId, dstData.getNodeIds());

                    if (algorithm.getFoundTargets() != algorithm.getTargetsCount())
                        throw new Exception("Search exceeds the limit of visited nodes.");

                    if (targets != null) {
                        pathMetricsExtractor.calcValues(srcIndex, targets, dstData, times, distances, weights);
                    }
                }
            }
        }

        if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.DURATION))
            mtxResult.setTable(MatrixMetricsType.DURATION, times);
        if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.DISTANCE))
            mtxResult.setTable(MatrixMetricsType.DISTANCE, distances);
        if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.WEIGHT))
            mtxResult.setTable(MatrixMetricsType.WEIGHT, weights);

        return mtxResult;
    }
}
