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

import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import org.heigit.ors.partitioning.BorderNodeDistanceStorage;
import org.heigit.ors.partitioning.CellStorage;
import org.heigit.ors.partitioning.EccentricityStorage;
import org.heigit.ors.partitioning.IsochroneNodeStorage;

/**
 * Calculates an isochrone using a partitioned and core-contracted graph.
 * The algorithm works in 3 phases
 * 1. Go upwards in start cell to find all distances to all nodes within that cell
 * 2. Traverse core graph
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
    private boolean alreadyRun;
    int visitedCountPhase1;
    int visitedCountPhase2;
    int visitedCountPhase3;

    CHGraph chGraph;
    double isochroneLimit;

    protected EdgeFilter additionalEdgeFilter;

    boolean inCore;

    public AbstractIsochroneAlgorithm(Graph graph,
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
        outEdgeExplorer = graph.createEdgeExplorer(DefaultEdgeFilter.outEdges(flagEncoder));

        int size = Math.min(2000, Math.max(200, graph.getNodes() / 10));
        initCollections(size);
    }

    protected abstract void initCollections(int size);

    public abstract void init(int from, double isochroneLimit);

    protected void checkAlreadyRun() {
        if (alreadyRun)
            throw new IllegalStateException("Create a new instance per call");

        alreadyRun = true;
    }

    public abstract void createIsochroneNodeSet();


    /**
     * Begin the phase that runs outside of the core
     */
    abstract void runPhase1();

    /**
     * Stopping criterion for phase outside core
     * @return should stop
     */
    public abstract boolean finishedPhase1();

    /**
     * Begin running of the algorithm phase inside the core
     */
    abstract void runPhase2();

    /**
     * Stopping criterion for phase inside core
     * @return should stop
     */
    public abstract boolean finishedPhase2();

    /**
     * Begin running of the algorithm phase in the active cells
     */
    abstract void runPhase3();

    /**
     * Stopping criterion for phase in active cells
     * @return should stop
     */
    public abstract boolean finishedPhase3();

    protected boolean finished() {
        return finishedPhase3();
    }


    protected void runAlgo() {
        // PHASE 1: run modified CH outside of core to find entry points
        inCore = false;
//        additionalEdgeFilter.setInCore(false);
        runPhase1();

        // PHASE 2 Perform routing in core with the restrictions filter
//        additionalEdgeFilter.setInCore(true);
        inCore = true;
        runPhase2();

        // PHASE 3 Perform routing in active cells
//        additionalEdgeFilter.setInCore(false);
        inCore = false;
        runPhase3();
    }

    public void calcIsochroneNodes(int from, double isochroneLimit){
        checkAlreadyRun();
        init(from, isochroneLimit);
        runAlgo();
        createIsochroneNodeSet();
    }

    public int getVisitedNodes() {
        return getVisitedNodesPhase1() + getVisitedNodesPhase2() + getVisitedNodesPhase3();
    }

    public int getVisitedNodesPhase1() {
        return visitedCountPhase1;
    }

    public int getVisitedNodesPhase2() {
        return visitedCountPhase2;
    }

    public int getVisitedNodesPhase3() {
        return visitedCountPhase3;
    }
}
