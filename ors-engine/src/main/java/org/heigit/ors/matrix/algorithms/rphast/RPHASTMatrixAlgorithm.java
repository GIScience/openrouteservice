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
package org.heigit.ors.matrix.algorithms.rphast;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.RoutingCHGraph;
import org.heigit.ors.matrix.*;
import org.heigit.ors.matrix.algorithms.AbstractMatrixAlgorithm;
import org.heigit.ors.routing.algorithms.RPHASTAlgorithm;
import org.heigit.ors.routing.graphhopper.extensions.storages.MultiTreeSPEntry;

import java.util.ArrayList;
import java.util.List;

public class RPHASTMatrixAlgorithm extends AbstractMatrixAlgorithm {
    private MultiTreeMetricsExtractor pathMetricsExtractor;
    private RoutingCHGraph chGraph;

    //        @Override
    public void init(MatrixRequest req, GraphHopper gh, RoutingCHGraph chGraph, FlagEncoder encoder, Weighting weighting) {
        //TODO Refactoring : check if base graph necessary. Probably not.
        super.init(req, gh, chGraph.getBaseGraph(), encoder, weighting);
        this.chGraph = chGraph;

        pathMetricsExtractor = new MultiTreeMetricsExtractor(req.getMetrics(), chGraph, this.encoder, weighting,
                req.getUnits());
    }

    @Override
    public MatrixResult compute(MatrixLocations srcData, MatrixLocations dstData, int metrics) throws Exception {
        // Search is more efficient for dstData.size > srcData.size, so check if they should be swapped
        boolean swap = checkSwapSrcDst(srcData, dstData);
        if (swap) {
            MatrixLocations tmp = srcData;
            srcData = dstData;
            dstData = tmp;
        }

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
            RPHASTAlgorithm algorithm = new RPHASTAlgorithm(chGraph, chGraph.getWeighting(), TraversalMode.NODE_BASED, swap);
            algorithm.setMaxVisitedNodes(this.maxVisitedNodes);

            int[] srcIds = getValidNodeIds(srcData.getNodeIds());
            int[] destIds = getValidNodeIds(dstData.getNodeIds());

            algorithm.prepare(srcIds, destIds);

            MultiTreeSPEntry[] destTrees = algorithm.calcPaths(srcIds, destIds);

            MultiTreeSPEntry[] originalDestTrees = new MultiTreeSPEntry[dstData.size()];

            int j = 0;
            for (int i = 0; i < dstData.size(); i++) {
                if (dstData.getNodeIds()[i] != -1) {
                    originalDestTrees[i] = destTrees[j];
                    ++j;
                } else {
                    originalDestTrees[i] = null;
                }
            }
            pathMetricsExtractor.setSwap(swap);
            pathMetricsExtractor.calcValues(originalDestTrees, srcData, dstData, times, distances, weights);
        }

        // Need to remap sources and destinations if we swapped before
        if (swap) {
            MatrixLocations tmp = srcData;
            srcData = dstData;
            dstData = tmp;
            float[][] results = swapResults(srcData, dstData, times, distances, weights);
            times = results[0];
            distances = results[1];
            weights = results[2];
        }

        MatrixResult mtxResult = new MatrixResult(srcData.getLocations(), dstData.getLocations());

        if (graphHopper != null)
            mtxResult.setGraphDate(graphHopper.getGraphHopperStorage().getProperties().get("datareader.import.date"));

        if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.DURATION))
            mtxResult.setTable(MatrixMetricsType.DURATION, times);
        if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.DISTANCE))
            mtxResult.setTable(MatrixMetricsType.DISTANCE, distances);
        if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.WEIGHT))
            mtxResult.setTable(MatrixMetricsType.WEIGHT, weights);

        return mtxResult;
    }

    private int[] getValidNodeIds(int[] nodeIds) {
        List<Integer> nodeList = new ArrayList<>();
        for (int dst : nodeIds) {
            if (dst != -1)
                nodeList.add(dst);

        }

        int[] res = new int[nodeList.size()];
        for (int i = 0; i < nodeList.size(); i++)
            res[i] = nodeList.get(i);

        return res;
    }

    /**
     * Search is more efficient for low source count and high destination count than the other way around.
     * If there are more sources than destinations, they get swapped and all calculations are done backwards.
     * The final result gets unswapped to return correct results.
     *
     * @param srcData original Source data
     * @param dstData original Destination data
     * @return boolean whether more sources than destinations
     */
    private boolean checkSwapSrcDst(MatrixLocations srcData, MatrixLocations dstData) {
        return (srcData.size() > dstData.size());
    }

    /**
     * Invert the results matrix (represented by flattened array) in case src and dst were swapped
     *
     * @param srcData   the original unswapped source data
     * @param dstData   the original unswapped destination data
     * @param times     the swapped array of results
     * @param distances the swapped array of results
     * @param weights   the swapped array of results
     * @return array of unswapped result arrays [times, distances, weights]
     */
    private float[][] swapResults(MatrixLocations srcData, MatrixLocations dstData, float[] times, float[] distances, float[] weights) {
        boolean hasTimes = times != null;
        boolean hasDistances = distances != null;
        boolean hasWeights = weights != null;
        float[] newTimes = new float[hasTimes ? times.length : 0];
        float[] newDistances = new float[hasDistances ? distances.length : 0];
        float[] newWeights = new float[hasWeights ? weights.length : 0];

        int i = 0;
        int srcSize = srcData.size();
        int dstSize = dstData.size();
        for (int dst = 0; dst < dstSize; dst++) {
            for (int src = 0; src < srcSize; src++) {
                int index = dst + src * dstSize;
                if (hasTimes)
                    newTimes[index] = times[i];
                if (hasDistances)
                    newDistances[index] = distances[i];
                if (hasWeights)
                    newWeights[index] = weights[i];
                i++;
            }
        }
        return new float[][]{newTimes, newDistances, newWeights};
    }
}
