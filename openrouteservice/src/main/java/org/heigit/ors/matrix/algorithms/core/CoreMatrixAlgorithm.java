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
package org.heigit.ors.matrix.algorithms.core;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.ch.PreparationWeighting;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import org.heigit.ors.matrix.*;
import org.heigit.ors.matrix.algorithms.AbstractMatrixAlgorithm;
import org.heigit.ors.routing.algorithms.DijkstraManyToManyMultiTreeAlgorithm;
import org.heigit.ors.routing.algorithms.RPHASTAlgorithm;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreDijkstraFilter;
import org.heigit.ors.routing.graphhopper.extensions.storages.MultiTreeSPEntry;

public class CoreMatrixAlgorithm extends AbstractMatrixAlgorithm {
    private MultiTreeMetricsExtractor pathMetricsExtractor;
    private CoreDijkstraFilter additionalCoreEdgeFilter;


    @Override
    public void init(MatrixRequest req, GraphHopper gh, Graph graph, FlagEncoder encoder, Weighting weighting) {
        super.init(req, gh, graph, encoder, weighting);

        pathMetricsExtractor = new MultiTreeMetricsExtractor(req.getMetrics(), graph, this.encoder, weighting, req.getUnits());
    }

    public void init(MatrixRequest req, GraphHopper gh, Graph graph, FlagEncoder encoder, Weighting weighting, EdgeFilter additionalEdgeFilter) {
        this.init(req, gh, graph, encoder, weighting);
        if (!(graph instanceof CHGraph))
            throw new IllegalArgumentException("Incorrect graph provided. Must be CHGraph, but is " + graph.getClass().getSimpleName());
        CoreDijkstraFilter levelFilter = new CoreDijkstraFilter((CHGraph) graph);
        if (additionalEdgeFilter != null)
            levelFilter.addRestrictionFilter(additionalEdgeFilter);

        this.setEdgeFilter(levelFilter);
    }

    public void init(MatrixRequest req, Graph graph, FlagEncoder encoder, Weighting weighting, EdgeFilter additionalEdgeFilter) {
        this.init(req, null, graph, encoder, weighting);
        if (!(graph instanceof CHGraph))
            throw new IllegalArgumentException("Incorrect graph provided. Must be CHGraph, but is " + graph.getClass().getSimpleName());
        CoreDijkstraFilter levelFilter = new CoreDijkstraFilter((CHGraph) graph);
        if (additionalEdgeFilter != null)
            levelFilter.addRestrictionFilter(additionalEdgeFilter);

        this.setEdgeFilter(levelFilter);
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

        if (!isValid(srcData, dstData)) {
            for (int srcIndex = 0; srcIndex < srcData.size(); srcIndex++)
                pathMetricsExtractor.setEmptyValues(srcIndex, dstData, times, distances, weights);
        } else {
            this.additionalCoreEdgeFilter.setInCore(false);
            runPhaseOutsideCore();
            this.additionalCoreEdgeFilter.setInCore(true);
            runPhaseInsideCore(srcData, dstData, times, distances, weights);

        }

        if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.DURATION))
            mtxResult.setTable(MatrixMetricsType.DURATION, times);
        if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.DISTANCE))
            mtxResult.setTable(MatrixMetricsType.DISTANCE, distances);
        if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.WEIGHT))
            mtxResult.setTable(MatrixMetricsType.WEIGHT, weights);

        return mtxResult;
    }

    private boolean isValid(MatrixLocations srcData, MatrixLocations dstData) {
        return !(!srcData.hasValidNodes() || !dstData.hasValidNodes());
    }

    private void runPhaseOutsideCore() {
    }

    private void runPhaseInsideCore(MatrixLocations srcData, MatrixLocations dstData, float[] times, float[] distances, float[] weights) throws Exception {
        DijkstraManyToManyMultiTreeAlgorithm algorithm = new DijkstraManyToManyMultiTreeAlgorithm(graph, new PreparationWeighting(weighting), TraversalMode.NODE_BASED);
        algorithm.setEdgeFilter(this.additionalCoreEdgeFilter);

        MultiTreeSPEntry[] destTrees = algorithm.calcPaths(srcData.getNodeIds(), dstData.getNodeIds());

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

        pathMetricsExtractor.calcValues(originalDestTrees, srcData, dstData, times, distances, weights);
    }

    public void setEdgeFilter(CoreDijkstraFilter additionalEdgeFilter) {
        this.additionalCoreEdgeFilter = additionalEdgeFilter;
    }
}
