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
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import org.heigit.ors.fastisochrones.partitioning.storage.BorderNodeDistanceStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.EccentricityStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of Fast Isochrones
 * <p>
 *
 * @author Hendrik Leuschner
 */
public class FastIsochroneAlgorithm extends AbstractIsochroneAlgorithm {
    private static final String NAME = "FastIsochrone";
    protected IntObjectMap<SPTEntry> bestWeightMap;
    protected Set<Integer> activeCells;
    protected Set<Integer> activeBorderNodes;
    protected Set<Integer> inactiveBorderNodes;
    protected Set<Integer> fullyReachableCells;
    protected Map<Integer, Map<Integer, Double>> upAndCoreGraphDistMap;
    protected Map<Integer, IntObjectMap<SPTEntry>> activeCellMaps;
    int from;
    int originalFrom;

    public FastIsochroneAlgorithm(Graph graph,
                                  Weighting weighting,
                                  TraversalMode tMode,
                                  CellStorage cellStorage,
                                  IsochroneNodeStorage isochroneNodeStorage,
                                  EccentricityStorage eccentricityStorage,
                                  BorderNodeDistanceStorage borderNodeDistanceStorage,
                                  EdgeFilter additionalEdgeFilter) {
        super(graph, weighting, tMode, cellStorage, isochroneNodeStorage, eccentricityStorage, borderNodeDistanceStorage, additionalEdgeFilter);
    }

    @Override
    protected void initCollections(int size) {
        bestWeightMap = new GHIntObjectHashMap<>(size);
    }

    @Override
    public void init(int from, double isochroneLimit) {
        this.from = from;
        this.isochroneLimit = isochroneLimit;
        activeCells = new HashSet<>();
        activeBorderNodes = new HashSet<>();
        inactiveBorderNodes = new HashSet<>();
        fullyReachableCells = new HashSet<>();
        upAndCoreGraphDistMap = new HashMap<>();
    }

    @Override
    void runPhase1() {
        int startCell = isochroneNodeStorage.getCellId(originalFrom);
        CoreRangeDijkstra rangeSweepToAndInCore = new CoreRangeDijkstra(graph, weighting, isochroneNodeStorage, borderNodeDistanceStorage);
        EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();
        edgeFilterSequence.add(additionalEdgeFilter);
        edgeFilterSequence.add(
                new CellAndBorderNodeFilter(isochroneNodeStorage,
                        startCell,
                        graph.getNodes())
        );
        rangeSweepToAndInCore.setEdgeFilter(edgeFilterSequence);
        rangeSweepToAndInCore.setIsochroneLimit(isochroneLimit);
        rangeSweepToAndInCore.initFrom(from);
        rangeSweepToAndInCore.runAlgo();
        bestWeightMap = rangeSweepToAndInCore.getFromMap();
        findFullyReachableCells(bestWeightMap);

        for (int inactiveBorderNode : inactiveBorderNodes) {
            bestWeightMap.remove(inactiveBorderNode);
            activeBorderNodes.remove(inactiveBorderNode);
        }

        for (int sweepEndNode : activeBorderNodes) {
            double dist = rangeSweepToAndInCore.fromMap.get(sweepEndNode).getWeightOfVisitedPath();
            int cell = isochroneNodeStorage.getCellId(sweepEndNode);
            if (cell == startCell)
                continue;
            if (!upAndCoreGraphDistMap.containsKey(cell))
                upAndCoreGraphDistMap.put(cell, new HashMap<>());
            upAndCoreGraphDistMap.get(cell).put(sweepEndNode, dist);
            bestWeightMap.remove(sweepEndNode);
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
        //This implementation combines phases 1 and 2 in phase 1
        return true;
    }

    @Override
    void runPhase3() {
        activeCellMaps = new HashMap<>(upAndCoreGraphDistMap.entrySet().size());
        activeCellMaps.put(isochroneNodeStorage.getCellId(originalFrom), bestWeightMap);
        for (Map.Entry<Integer, Map<Integer, Double>> entry : upAndCoreGraphDistMap.entrySet()) {
            ActiveCellDijkstra activeCellDijkstra = new ActiveCellDijkstra(this);
            //Add a filter that stays within the cell.
            EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();
            edgeFilterSequence.add(new FixedCellEdgeFilter(isochroneNodeStorage, entry.getKey(), graph.getNodes()));
            activeCellDijkstra.setEdgeFilter(edgeFilterSequence);
            activeCellDijkstra.setIsochroneLimit(isochroneLimit);
            //Add all the start points with their respective already visited weight
            for (int nodeId : entry.getValue().keySet()) {
                activeCellDijkstra.addInitialBordernode(nodeId, entry.getValue().get(nodeId));
            }
            activeCellDijkstra.init();
            activeCellDijkstra.runAlgo();
            activeCellMaps.put(entry.getKey(), activeCellDijkstra.fromMap);
        }
    }

    @Override
    public boolean finishedPhase3() {
        return false;
    }

    private void findFullyReachableCells(IntObjectMap<SPTEntry> entryMap) {
        for(IntObjectCursor<SPTEntry> entry : entryMap){
            int baseNode = entry.key;
            if(!isochroneNodeStorage.getBorderness(baseNode))
                continue;
            SPTEntry sptEntry = entry.value;
            int baseCell = isochroneNodeStorage.getCellId(baseNode);
            double baseNodeEccentricity = eccentricityStorage.getEccentricity(baseNode);
            if (sptEntry.getWeightOfVisitedPath() + baseNodeEccentricity < isochroneLimit
                    && eccentricityStorage.getFullyReachable(baseNode)) {
                addFullyReachableCell(baseCell);
                addInactiveBorderNode(baseNode);
                if (getActiveCells().contains(baseCell))
                    getActiveCells().remove(baseCell);
            } else {
                if (!getFullyReachableCells().contains(baseCell)) {
                    addActiveCell(baseCell);
                    addActiveBorderNode(baseNode);
                }
            }
        }
    }

    protected void addActiveCell(int cellId) {
        activeCells.add(cellId);
    }

    private void addFullyReachableCell(int cellId){
        fullyReachableCells.add(cellId);
    }

    protected void addActiveBorderNode(int nodeId) {
        activeBorderNodes.add(nodeId);
    }

    protected void addInactiveBorderNode(int nodeId) {
        inactiveBorderNodes.add(nodeId);
    }

    public Set<Integer> getFullyReachableCells() {
        return fullyReachableCells;
    }
    private Set<Integer> getActiveCells(){
        return activeCells;
    }

    public IntObjectMap<SPTEntry> getBestWeightMap() {
        return bestWeightMap;
    }

    public String getName() {
        return NAME;
    }

    public void setOriginalFrom(int originalFrom) {
        this.originalFrom = originalFrom;
    }

    public Map<Integer, IntObjectMap<SPTEntry>> getActiveCellMaps() {
        return activeCellMaps;
    }
}
