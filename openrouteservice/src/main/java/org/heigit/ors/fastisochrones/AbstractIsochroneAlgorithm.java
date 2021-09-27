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
package org.heigit.ors.fastisochrones;

import com.graphhopper.routing.util.AccessFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import org.heigit.ors.fastisochrones.storage.BorderNodeDistanceStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.storage.EccentricityStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;

/**
 * Calculates an isochrone using a partitioned and graph.
 * The algorithm works in 3 phases
 * 1. Go upwards in start cell to find all distances to all nodes within that cell
 * 2. Take all bordernodes of the initial cell and find all reachable other bordernodes
 * 3. Go downwards in active cells
 *
 * @author Hendrik Leuschner
 */
public abstract class AbstractIsochroneAlgorithm {
    protected final Graph graph;
    protected final Weighting weighting;
    protected final FlagEncoder flagEncoder;
    protected final TraversalMode traversalMode;
    protected NodeAccess nodeAccess;
    protected CellStorage cellStorage;
    protected IsochroneNodeStorage isochroneNodeStorage;
    protected EccentricityStorage eccentricityStorage;
    protected BorderNodeDistanceStorage borderNodeDistanceStorage;
    protected EdgeExplorer outEdgeExplorer;
    protected EdgeFilter additionalEdgeFilter;
    int visitedCountStartCellPhase;
    int visitedCountBorderNodesPhase;
    int visitedCountActiveCellPhase;
    double isochroneLimit;
    private boolean alreadyRun;

    protected AbstractIsochroneAlgorithm(Graph graph,
                                      Weighting weighting,
                                      TraversalMode tMode,
                                      CellStorage cellStorage,
                                      IsochroneNodeStorage isochroneNodeStorage,
                                      EccentricityStorage eccentricityStorage,
                                      BorderNodeDistanceStorage borderNodeDistanceStorage,
                                      EdgeFilter additionalEdgeFilter) {
        this.weighting = weighting;
        this.flagEncoder = weighting.getFlagEncoder();
        this.traversalMode = tMode;
        this.graph = graph;
        this.cellStorage = cellStorage;
        this.isochroneNodeStorage = isochroneNodeStorage;
        this.eccentricityStorage = eccentricityStorage;
        this.borderNodeDistanceStorage = borderNodeDistanceStorage;
        this.additionalEdgeFilter = additionalEdgeFilter;
        this.nodeAccess = graph.getNodeAccess();
        outEdgeExplorer = graph.createEdgeExplorer(AccessFilter.outEdges(flagEncoder.getAccessEnc()));

        int size = Math.min(2000, Math.max(200, graph.getNodes() / 10));
        initCollections(size);
    }

    protected abstract void initCollections(int size);

    public void init(int from, double isochroneLimit) {
        init(from, from, isochroneLimit);
    }

    public abstract void init(int from, int fromNonVirtual, double isochroneLimit);


    protected void checkAlreadyRun() {
        if (alreadyRun)
            throw new IllegalStateException("Create a new instance per call");

        alreadyRun = true;
    }


    /**
     * Run phase from start to border nodes
     */
    abstract void runStartCellPhase();

    /**
     * Stopping criterion for phase outside core
     *
     * @return should stop
     */
    public abstract boolean finishedStartCellPhase();

    /**
     * Run algorithm on border nodes
     */
    abstract void runBorderNodePhase();

    /**
     * Stopping criterion for phase inside core
     *
     * @return should stop
     */
    public abstract boolean finishedBorderNodePhase();

    /**
     * Begin running of the algorithm phase in the active cells
     */
    abstract void runActiveCellPhase();

    /**
     * Stopping criterion for phase in active cells
     *
     * @return should stop
     */
    public abstract boolean finishedActiveCellPhase();

    protected boolean finished() {
        return finishedActiveCellPhase();
    }

    protected void runAlgo() {
        runStartCellPhase();

        runBorderNodePhase();

        runActiveCellPhase();
    }

    public void calcIsochroneNodes(int from, double isochroneLimit) {
        checkAlreadyRun();
        init(from, isochroneLimit);
        runAlgo();
    }

    /**
     * Calculate nodes for a given set of (virtual) from node and non-virtual basenode. Necessary for precomputed information that cannot process virtual nodes.
     * @param from virtual start node
     * @param fromNonVirtual real node closest to virtual start node
     * @param isochroneLimit limit
     */
    public void calcIsochroneNodes(int from, int fromNonVirtual, double isochroneLimit) {
        checkAlreadyRun();
        init(from, fromNonVirtual, isochroneLimit);
        runAlgo();
    }

    public int getVisitedNodes() {
        return getVisitedNodesPhase1() + getVisitedNodesPhase2() + getVisitedNodesPhase3();
    }

    public int getVisitedNodesPhase1() {
        return visitedCountStartCellPhase;
    }

    public int getVisitedNodesPhase2() {
        return visitedCountBorderNodesPhase;
    }

    public int getVisitedNodesPhase3() {
        return visitedCountActiveCellPhase;
    }
}
