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

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import org.heigit.ors.partitioning.CellStorage;
import org.heigit.ors.partitioning.EccentricityStorage;
import org.heigit.ors.partitioning.IsochroneNodeStorage;

import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Calculates best path using CH routing outside core and ALT inside core.
 *
 * This code is based on that from GraphHopper GmbH.
 *
 * @author Peter Karich
 * @author jansoe
 * @author Andrzej Oles
 */

public class FastIsochroneAlgorithm extends AbstractIsochroneAlgorithm {
    protected IntObjectMap<SPTEntry> bestWeightMap;

    protected Map<Integer, Integer> previousNodeMap, processedBorderNodesMap;
    protected Set<Integer> isochroneNodes, isochroneEdges, isochroneEdgeNodes, processedBorderNodes, activeCells, activeBorderNodes, fullyReachableCells;
    protected Map<Integer, Map<Integer, Double>> upAndCoreGraphDistMap;

    int from, originalFrom;

    public FastIsochroneAlgorithm(Graph graph,
                                  Weighting weighting,
                                  TraversalMode tMode,
                                  CellStorage cellStorage,
                                  IsochroneNodeStorage isochroneNodeStorage,
                                  EccentricityStorage eccentricityStorage,
                                  EdgeFilter additionalEdgeFilter) {
        super(graph, weighting, tMode, cellStorage, isochroneNodeStorage, eccentricityStorage, additionalEdgeFilter);
    }

    @Override
    protected void initCollections(int size) {
        bestWeightMap = new GHIntObjectHashMap<SPTEntry>(size);
    }

    @Override
    public void init(int from, double isochroneLimit) {
        this.from = from;
        this.isochroneLimit = isochroneLimit;

        this.activeCells = new HashSet<>();
        this.activeBorderNodes = new HashSet<>();
        this.isochroneNodes = new HashSet<>();
        this.isochroneEdges = new HashSet<>();
        this.previousNodeMap = new HashMap<>();
        this.processedBorderNodesMap = new HashMap<>();
        this.isochroneEdgeNodes = new HashSet<>();
        this.fullyReachableCells = new HashSet<>();
        this.processedBorderNodes = new HashSet<>();
        this.upAndCoreGraphDistMap = new HashMap<>();
    }

    @Override
    public void createIsochroneNodeSet() {

    }

    @Override
    void runPhase1() {
        CoreRangeDijkstra rangeSweepToAndInCore = new CoreRangeDijkstra(this);
        EdgeFilterSequence efs = new EdgeFilterSequence();
        efs.add(this.additionalEdgeFilter);
        efs.add(
                new CellAndLevelFilter(this.isochroneNodeStorage,
                        isochroneNodeStorage.getCellId(originalFrom),
                        graph.getNodes(),
                        chGraph
                        )
        );
        rangeSweepToAndInCore.setEdgeFilter(efs);
//        rangeSweepToAndInCore.setEdgeFilter(new FixedCellEdgeFilter(this.isochroneNodeStorage, isochroneNodeStorage.getCellId(originalFrom), graph.getNodes()));
        rangeSweepToAndInCore.setIsochroneLimit(isochroneLimit);
        rangeSweepToAndInCore.initFrom(from);
        rangeSweepToAndInCore.runAlgo();
        this.bestWeightMap = rangeSweepToAndInCore.fromMap;

        //as far as I can tell this is only duplicate information of bordernodes->weight
        for (int sweepEndNode : activeBorderNodes) {
            double dist = rangeSweepToAndInCore.fromMap.get(sweepEndNode).getWeightOfVisitedPath();
            int cell = isochroneNodeStorage.getCellId(sweepEndNode);

            if (!upAndCoreGraphDistMap.containsKey(cell))
                upAndCoreGraphDistMap.put(cell, new HashMap<>());
            upAndCoreGraphDistMap.get(cell).put(sweepEndNode, dist);
        }
    }



    @Override
    public boolean finishedPhase1() {
        return true;
    }

    /**
     * Run the ALT algo in the core
     */
    @Override
    void runPhase2() {
        //This implementation combines phases 1 and 2 in phase 1

    }

    @Override
    public boolean finishedPhase2() {
        //TODO when do we need this?
        return true;
    }

    @Override
    void runPhase3() {
        for (Map.Entry<Integer, Map<Integer, Double>> entry : upAndCoreGraphDistMap.entrySet()) {
            ActiveCellDijkstra activeCellDijkstra = new ActiveCellDijkstra(this);
            //Add a filter that stays within the cell.
            //TODO maybe also add a level(base)<=level(adj) filter to make it PHAST?
            activeCellDijkstra.setEdgeFilter(new FixedCellEdgeFilter(this.isochroneNodeStorage, entry.getKey(), graph.getNodes()));
            activeCellDijkstra.setIsochroneLimit(isochroneLimit);
            //Add all the start points with their respective already visited weight
            for (int nodeId : entry.getValue().keySet())
                activeCellDijkstra.addInitialBordernode(nodeId, entry.getValue().get(nodeId));
            activeCellDijkstra.fromMap = this.bestWeightMap;
            activeCellDijkstra.init();
            activeCellDijkstra.runAlgo();

        }
    }

    @Override
    public boolean finishedPhase3() {
        return false;
    }


    protected void addActiveCell(int cellId) {
        activeCells.add(cellId);
    }

    protected void addActiveBorderNode(int nodeId) {
        activeBorderNodes.add(nodeId);
    }


    public Set<Integer> getFullyReachableCells(){
        return fullyReachableCells;
    }

    public IntObjectMap<SPTEntry> getBestWeightMap() {
        return bestWeightMap;
    }

    public String getName() {
        return "FastIsochrone";
    }

    public void setOriginalFrom(int originalFrom){
        this.originalFrom = originalFrom;
    }


}
