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
import com.graphhopper.routing.SPTEntry;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.heigit.ors.fastisochrones.storage.BorderNodeDistanceStorage;
import org.heigit.ors.fastisochrones.storage.EccentricityStorage;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;

import java.util.*;

/**
 * Implementation of Fast Isochrones
 * <p>
 *
 * @author Hendrik Leuschner
 */
public class FastIsochroneAlgorithm extends AbstractIsochroneAlgorithm {
    private static final String NAME = "FastIsochrone";
    protected IntObjectMap<SPTEntry> startCellMap;
    protected Set<Integer> activeBorderNodes;
    protected Set<Integer> inactiveBorderNodes;
    protected Set<Integer> fullyReachableCells;
    protected Map<Integer, Map<Integer, Double>> upAndCoreGraphDistMap;
    protected Map<Integer, IntObjectMap<SPTEntry>> activeCellMaps;
    int from;
    int fromNonVirtual;

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
        startCellMap = new GHIntObjectHashMap<>(size);
    }

    @Override
    public void init(int from, int fromNonVirtual, double isochroneLimit) {
        this.from = from;
        this.fromNonVirtual = fromNonVirtual;
        this.isochroneLimit = isochroneLimit;
        activeBorderNodes = new HashSet<>();
        inactiveBorderNodes = new HashSet<>();
        fullyReachableCells = new HashSet<>();
        upAndCoreGraphDistMap = new HashMap<>();
    }

    @Override
    void runStartCellPhase() {
        int startCell = isochroneNodeStorage.getCellId(fromNonVirtual);
        CoreRangeDijkstra coreRangeDijkstra = new CoreRangeDijkstra(graph, weighting, isochroneNodeStorage, borderNodeDistanceStorage);
        EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();
        if (additionalEdgeFilter != null)
            edgeFilterSequence.add(additionalEdgeFilter);
        edgeFilterSequence.add(
                new CellAndBorderNodeFilter(isochroneNodeStorage,
                        startCell,
                        graph.getNodes())
        );
        coreRangeDijkstra.setEdgeFilter(edgeFilterSequence);
        coreRangeDijkstra.setIsochroneLimit(isochroneLimit);
        coreRangeDijkstra.initFrom(from);
        coreRangeDijkstra.runAlgo();
        startCellMap = coreRangeDijkstra.getFromMap();
        findFullyReachableCells(startCellMap);

        for (int inactiveBorderNode : inactiveBorderNodes) {
            startCellMap.remove(inactiveBorderNode);
            activeBorderNodes.remove(inactiveBorderNode);
        }

        for (int sweepEndNode : activeBorderNodes) {
            double dist = coreRangeDijkstra.fromMap.get(sweepEndNode).getWeightOfVisitedPath();
            int cell = isochroneNodeStorage.getCellId(sweepEndNode);
            if (cell == startCell)
                continue;
            if (!upAndCoreGraphDistMap.containsKey(cell))
                upAndCoreGraphDistMap.put(cell, new HashMap<>());
            upAndCoreGraphDistMap.get(cell).put(sweepEndNode, dist);
            startCellMap.remove(sweepEndNode);
        }
    }

    @Override
    public boolean finishedStartCellPhase() {
        return true;
    }

    /**
     * Run the ALT algo in the core
     */
    @Override
    void runBorderNodePhase() {
        //This implementation combines running in the start cell and running on the border nodes in one phase
    }

    @Override
    public boolean finishedBorderNodePhase() {
        //This implementation combines running in the start cell and running on the border nodes in one phase
        return true;
    }

    @Override
    void runActiveCellPhase() {
        activeCellMaps = new HashMap<>(upAndCoreGraphDistMap.entrySet().size());
        activeCellMaps.put(isochroneNodeStorage.getCellId(fromNonVirtual), startCellMap);
        for (Map.Entry<Integer, Map<Integer, Double>> entry : upAndCoreGraphDistMap.entrySet()) {
            ActiveCellDijkstra activeCellDijkstra = new ActiveCellDijkstra(graph, weighting, isochroneNodeStorage, entry.getKey());
            activeCellDijkstra.setIsochroneLimit(isochroneLimit);
            //Add all the start points with their respective already visited weight
            for (int nodeId : entry.getValue().keySet()) {
                activeCellDijkstra.addInitialBordernode(nodeId, entry.getValue().get(nodeId));
            }
            activeCellDijkstra.init();
            activeCellDijkstra.runAlgo();
            activeCellMaps.put(entry.getKey(), activeCellDijkstra.getFromMap());
        }
    }

    @Override
    public boolean finishedActiveCellPhase() {
        return false;
    }

    private void findFullyReachableCells(IntObjectMap<SPTEntry> entryMap) {
        for (IntObjectCursor<SPTEntry> entry : entryMap) {
            int baseNode = entry.key;
            if (!isochroneNodeStorage.getBorderness(baseNode))
                continue;
            SPTEntry sptEntry = entry.value;
            int baseCell = isochroneNodeStorage.getCellId(baseNode);
            int eccentricity = eccentricityStorage.getEccentricity(baseNode);
            if (isWithinLimit(sptEntry, eccentricity)
                    && eccentricityStorage.getFullyReachable(baseNode)) {
                addFullyReachableCell(baseCell);
                addInactiveBorderNode(baseNode);
            } else {
                if (!getFullyReachableCells().contains(baseCell)) {
                    addActiveBorderNode(baseNode);
                }
            }
        }
    }

    /**
     * Consider all active cells that have a percentage of *approximation* of their nodes visited to be fully reachable.
     *
     * @param approximation factor of approximation. 1 means all nodes must be found, 0 means no nodes have to be found.
     */
    public void approximateActiveCells(double approximation) {
        Iterator<Map.Entry<Integer, IntObjectMap<SPTEntry>>> activeCellIterator = getActiveCellMaps().entrySet().iterator();
        while (activeCellIterator.hasNext()) {
            Map.Entry<Integer, IntObjectMap<SPTEntry>> activeCell = activeCellIterator.next();
            if (activeCell.getValue().size() / (double) cellStorage.getNodesOfCell(activeCell.getKey()).size() > approximation) {
                activeCellIterator.remove();
                getFullyReachableCells().add(activeCell.getKey());
            }
        }
    }

    private boolean isWithinLimit(SPTEntry sptEntry, int eccentricity) {
        return sptEntry.getWeightOfVisitedPath() + eccentricity <= isochroneLimit;
    }

    private void addFullyReachableCell(int cellId) {
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

    public IntObjectMap<SPTEntry> getStartCellMap() {
        return startCellMap;
    }

    public String getName() {
        return NAME;
    }

    public Map<Integer, IntObjectMap<SPTEntry>> getActiveCellMaps() {
        return activeCellMaps;
    }
}
