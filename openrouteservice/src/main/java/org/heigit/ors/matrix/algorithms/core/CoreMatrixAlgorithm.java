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

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.GraphHopper;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.ch.PreparationWeighting;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.TurnWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import org.heigit.ors.matrix.*;
import org.heigit.ors.matrix.algorithms.AbstractMatrixAlgorithm;
import org.heigit.ors.matrix.algorithms.dijkstra.DijkstraManyToManyMultiTreeAlgorithm;
import org.heigit.ors.routing.algorithms.SubGraph;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreDijkstraFilter;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.ch.DownwardSearchEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.storages.MinimumWeightMultiTreeSPEntry;
import org.heigit.ors.routing.graphhopper.extensions.storages.MultiTreeSPEntryItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class CoreMatrixAlgorithm extends AbstractMatrixAlgorithm {
    private MultiTreeMetricsExtractor pathMetricsExtractor;
    private CoreDijkstraFilter additionalCoreEdgeFilter;
    private int treeEntrySize;

    private CHGraph chGraph;
    private SubGraph targetGraph;

    protected int coreNodeLevel;
    protected int turnRestrictedNodeLevel;
    private boolean hasTurnWeighting = false;
    protected boolean approximate = true;

    protected int highestNodeLevel = -1;
    protected int highestNode = -1;
    protected int maxNodes;
    protected int maxVisitedNodes = Integer.MAX_VALUE;
    protected int visitedNodes;

    protected boolean finishedFrom;
    protected boolean finishedTo;

    protected EdgeExplorer outEdgeExplorer;

    PriorityQueue<MinimumWeightMultiTreeSPEntry> upwardQueue;
    IntHashSet coreEntryPoints;
    IntHashSet coreExitPoints;

    IntObjectMap<MinimumWeightMultiTreeSPEntry> bestWeightMap;
    IntObjectMap<List<MinimumWeightMultiTreeSPEntry>> bestWeightMapCore;

    @Override
    public void init(MatrixRequest req, GraphHopper gh, Graph graph, FlagEncoder encoder, Weighting weighting) {
        if (weighting instanceof TurnWeighting) {
            hasTurnWeighting = true;
        }
        weighting = new PreparationWeighting(weighting);
        super.init(req, gh, graph, encoder, weighting);

        this.outEdgeExplorer = graph.createEdgeExplorer(DefaultEdgeFilter.outEdges(encoder));

        //TODO Check how this needs to be QueryGraph instead. ClassCastException occurs
        chGraph = (CHGraph) graph;
        coreNodeLevel = chGraph.getNodes() + 1;
        turnRestrictedNodeLevel = coreNodeLevel + 1;
        maxNodes = graph.getNodes();
        pathMetricsExtractor = new MultiTreeMetricsExtractor(req.getMetrics(), graph, this.encoder, weighting, req.getUnits());
        initCollections(10);
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

    protected void initCollections(int size) {
        upwardQueue = new PriorityQueue<>(size);
        coreEntryPoints = new IntHashSet(size);
        coreExitPoints = new IntHashSet(size);
        bestWeightMap = new GHIntObjectHashMap<>(size);
        bestWeightMapCore = new GHIntObjectHashMap<>(size);
    }

    @Override
    public MatrixResult compute(MatrixLocations srcData, MatrixLocations dstData, int metrics) throws Exception {
        this.treeEntrySize = srcData.size();
        prepareTargetGraph(dstData.getNodeIds());
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
            runPhaseOutsideCore(srcData);
            this.additionalCoreEdgeFilter.setInCore(true);
            PriorityQueue<MinimumWeightMultiTreeSPEntry> downwardQueue = runPhaseInsideCore();
            this.additionalCoreEdgeFilter.setInCore(false);
            outEdgeExplorer = targetGraph.createExplorer();

            //Case if there was no core reached
            if (downwardQueue.isEmpty())
                downwardQueue = createDownwardQueueFromHighestNode();
            runDownwardSearch(downwardQueue);
            extractMetrics(srcData, dstData, times, distances, weights);
        }

        if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.DURATION))
            mtxResult.setTable(MatrixMetricsType.DURATION, times);
        if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.DISTANCE))
            mtxResult.setTable(MatrixMetricsType.DISTANCE, distances);
        if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.WEIGHT))
            mtxResult.setTable(MatrixMetricsType.WEIGHT, weights);

        return mtxResult;
    }

    private PriorityQueue<MinimumWeightMultiTreeSPEntry> createDownwardQueueFromHighestNode() {
        PriorityQueue<MinimumWeightMultiTreeSPEntry> queue = new PriorityQueue<>(1);
        queue.add(bestWeightMap.get(highestNode));
        return queue;
    }

    private boolean isValid(MatrixLocations srcData, MatrixLocations dstData) {
        return !(!srcData.hasValidNodes() || !dstData.hasValidNodes());
    }

    private void extractMetrics(MatrixLocations srcData, MatrixLocations dstData, float[] times, float[] distances, float[] weights) throws Exception {
        MinimumWeightMultiTreeSPEntry[] destTrees = new MinimumWeightMultiTreeSPEntry[dstData.size()];
        for (int i = 0; i < dstData.size(); i++)
            destTrees[i] = bestWeightMap.get(dstData.getNodeIds()[i]);


        MinimumWeightMultiTreeSPEntry[] originalDestTrees = new MinimumWeightMultiTreeSPEntry[dstData.size()];

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

    /**
     * /
     * /
     * __________OUT-CORE
     * /
     * /
     **/
    private void prepareSourceNodes(int[] from) {
        for (int i = 0; i < from.length; i++) {
            if (from[i] == -1)
                continue;

            //If two queried points are on the same node, this case can occur
            MinimumWeightMultiTreeSPEntry existing = bestWeightMap.get(from[i]);
            if (existing != null) {
                existing.getItem(i).setWeight(0.0);
                continue;
            }

            MinimumWeightMultiTreeSPEntry newFrom = new MinimumWeightMultiTreeSPEntry(from[i], EdgeIterator.NO_EDGE, 0.0, true, null, from.length);
            newFrom.getItem(i).setWeight(0.0);
            newFrom.updateWeights();
            upwardQueue.add(newFrom);
            updateHighestNode(from[i]);

            bestWeightMap.put(from[i], newFrom);
        }
    }

    public void prepareTargetGraph(int[] targets) {
        PriorityQueue<Integer> localPrioQueue = new PriorityQueue<>(100);

        // Phase I: build shortest path tree from all target nodes to the
        // highest node
        targetGraph = new SubGraph(graph);

        addNodes(targetGraph, localPrioQueue, targets);
        DownwardSearchEdgeFilter downwardEdgeFilter = new DownwardSearchEdgeFilter(chGraph, encoder, true, this.hasTurnWeighting);
        while (!localPrioQueue.isEmpty()) {
            int adjNode = localPrioQueue.poll();
            EdgeIterator iter = outEdgeExplorer.setBaseNode(adjNode);
            downwardEdgeFilter.setBaseNode(adjNode);

            while (iter.next()) {
                if (!downwardEdgeFilter.accept(iter))
                    continue;

                if (targetGraph.addEdge(adjNode, iter, true)) {
                    if (isCoreNode(iter.getAdjNode()) && !isCoreNode(iter.getBaseNode()))
                        coreExitPoints.add(iter.getAdjNode());
                    else
                        localPrioQueue.add(iter.getAdjNode());
                }
            }
        }
    }

    private void addNodes(SubGraph graph, PriorityQueue<Integer> prioQueue, int[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            int nodeId = nodes[i];
            if (nodeId >= 0) {
                if (graph != null)
                    graph.addEdge(nodeId, null, true);
                prioQueue.add(nodeId);
                if (isCoreNode(nodeId)) {
                    coreExitPoints.add(nodeId);
                }
            }
        }
    }

    private void runPhaseOutsideCore(MatrixLocations srcData) {
        prepareSourceNodes(srcData.getNodeIds());
        while (!finishedPhase1() && !isMaxVisitedNodesExceeded()) {
            if (!finishedFrom)
                finishedFrom = !fillEdgesOutsideCore();
        }
    }


    public boolean finishedPhase1() {
        return finishedFrom;
    }

    public boolean fillEdgesOutsideCore() {
        if (upwardQueue.isEmpty())
            return false;

        MinimumWeightMultiTreeSPEntry currFrom = upwardQueue.poll();

        if (isCoreNode(currFrom.getAdjNode())) {
            // core entry point, do not relax its edges
            coreEntryPoints.add(currFrom.getAdjNode());
            // for regular CH Dijkstra we don't expect an entry to exist because the picked node is supposed to be already settled
            if (considerTurnRestrictions(currFrom.getAdjNode())) {
                List<MinimumWeightMultiTreeSPEntry> existingEntryList = bestWeightMapCore.get(currFrom.getAdjNode());
                if (existingEntryList == null)
                    initBestWeightMapEntryList(bestWeightMapCore, currFrom.getAdjNode()).add(currFrom);
                else
                    existingEntryList.add(currFrom);
            }
        } else {
            fillEdgesUpward(currFrom, upwardQueue, bestWeightMap, outEdgeExplorer);
        }

        visitedNodes++;

        return true;
    }

    List<MinimumWeightMultiTreeSPEntry> initBestWeightMapEntryList(IntObjectMap<List<MinimumWeightMultiTreeSPEntry>> bestWeightMap, int traversalId) {
        if (bestWeightMap.get(traversalId) != null)
            throw new IllegalStateException("Core entry point already exists in best weight map.");

        List<MinimumWeightMultiTreeSPEntry> entryList = new ArrayList<>(5);
        bestWeightMap.put(traversalId, entryList);

        return entryList;
    }

    boolean considerTurnRestrictions(int node) {
        if (!hasTurnWeighting)
            return false;
        if (approximate)
            return isTurnRestrictedNode(node);
        return true;
    }

    boolean isTurnRestrictedNode(int node) {
        return chGraph.getLevel(node) == turnRestrictedNodeLevel;
    }

    void fillEdgesUpward(MinimumWeightMultiTreeSPEntry currEdge, PriorityQueue<MinimumWeightMultiTreeSPEntry> prioQueue, IntObjectMap<MinimumWeightMultiTreeSPEntry> bestWeightMap,
                         EdgeExplorer explorer) {
        EdgeIterator iter = explorer.setBaseNode(currEdge.getAdjNode());

        while (iter.next()) {
            if (!additionalCoreEdgeFilter.accept(iter)) {
                continue;
            }

            updateHighestNode(iter.getAdjNode());

            double edgeWeight = weighting.calcWeight(iter, false, currEdge.getEdge());
            System.out.println("Fill upward from edge " + currEdge.getEdge() + " via node " + currEdge.getAdjNode() + " to edge " + iter.getEdge() + " with weight " + edgeWeight);
            if (!Double.isInfinite(edgeWeight)) {
                MinimumWeightMultiTreeSPEntry entry = bestWeightMap.get(iter.getAdjNode());

                if (entry == null) {
                    entry = new MinimumWeightMultiTreeSPEntry(iter.getAdjNode(), iter.getEdge(), edgeWeight, true, currEdge, currEdge.getSize());
                    entry.setOriginalEdge(EdgeIteratorStateHelper.getOriginalEdge(iter));
                    bestWeightMap.put(iter.getAdjNode(), entry);
                    prioQueue.add(entry);
                } else {
                    boolean addToQueue = iterateMultiTree(currEdge, iter, edgeWeight, entry, true);

                    if (addToQueue) {
                        entry.updateWeights();
                        prioQueue.remove(entry);
                        prioQueue.add(entry);
                    }
                }
            }
        }
    }

    /**
     * /
     * /
     * __________IN-CORE
     * /
     * /
     **/
    private PriorityQueue<MinimumWeightMultiTreeSPEntry> runPhaseInsideCore() {
        // Calculate all paths only inside core
        DijkstraManyToManyMultiTreeAlgorithm algorithm = new DijkstraManyToManyMultiTreeAlgorithm(graph, chGraph, bestWeightMap, bestWeightMapCore, weighting, TraversalMode.NODE_BASED);
        //TODO Add restriction filter or do this differently
        algorithm.setEdgeFilter(this.additionalCoreEdgeFilter);
        algorithm.setTreeEntrySize(this.treeEntrySize);
        algorithm.setHasTurnWeighting(this.hasTurnWeighting);
        algorithm.setMaxVisitedNodes(this.maxVisitedNodes);
        algorithm.setVisitedNodes(this.visitedNodes);

        int[] entryPoints = coreEntryPoints.toArray();
        int[] exitPoints = coreExitPoints.toArray();
        MinimumWeightMultiTreeSPEntry[] destTrees = algorithm.calcPaths(entryPoints, exitPoints);
        // Set all found core exit points as start points of the downward search phase
        PriorityQueue<MinimumWeightMultiTreeSPEntry> downwardQueue = new PriorityQueue<>(destTrees.length > 0 ? destTrees.length : 1);
        Collections.addAll(downwardQueue, destTrees);
        return downwardQueue;
    }

    /**
     * /
     * /
     * __________OUT-CORE 2nd PHASE
     * /
     * /
     **/

    protected void runDownwardSearch(PriorityQueue<MinimumWeightMultiTreeSPEntry> downwardQueue) {
        while (!finishedTo) {
            finishedTo = !downwardSearch(downwardQueue);
        }
    }

    private boolean downwardSearch(PriorityQueue<MinimumWeightMultiTreeSPEntry> downwardQueue) {
        if (downwardQueue.isEmpty())
            return false;

        MinimumWeightMultiTreeSPEntry currTo = downwardQueue.poll();
        fillEdgesDownward(currTo, downwardQueue, bestWeightMap, outEdgeExplorer);
        return true;
    }

    private void fillEdgesDownward(MinimumWeightMultiTreeSPEntry currEdge, PriorityQueue<MinimumWeightMultiTreeSPEntry> prioQueue,
                                   IntObjectMap<MinimumWeightMultiTreeSPEntry> shortestWeightMap, EdgeExplorer explorer) {

        EdgeIterator iter = explorer.setBaseNode(currEdge.getAdjNode());

        if (iter == null)
            return;

        while (iter.next()) {
            double edgeWeight = weighting.calcWeight(iter, false, currEdge.getOriginalEdge());
            System.out.println("Fill downward from edge " + currEdge.getOriginalEdge() + " via node " + currEdge.getAdjNode() + " to edge " + iter.getEdge() + " with weight " + edgeWeight);

            if (!Double.isInfinite(edgeWeight)) {
                MinimumWeightMultiTreeSPEntry adjEntry = shortestWeightMap.get(iter.getAdjNode());

                if (adjEntry == null) {
                    adjEntry = new MinimumWeightMultiTreeSPEntry(iter.getAdjNode(), iter.getEdge(), edgeWeight, true, currEdge, currEdge.getSize());
                    adjEntry.setVisited(true);

                    shortestWeightMap.put(iter.getAdjNode(), adjEntry);
                    prioQueue.add(adjEntry);
                } else {
                    boolean addToQueue = iterateMultiTree(currEdge, iter, edgeWeight, adjEntry, false);

                    adjEntry.updateWeights();

                    if (!adjEntry.isVisited()) {
                        // This is the case if the node has been assigned a weight in
                        // the upwards pass (fillEdges). We need to use it in the
                        // downwards pass to access lower level nodes, though
                        // the weight does not have to be reset necessarily
                        adjEntry.setVisited(true);
                        prioQueue.add(adjEntry);
                    } else if (addToQueue) {
                        adjEntry.setVisited(true);
                        prioQueue.remove(adjEntry);
                        prioQueue.add(adjEntry);
                    }
                }
            }
        }
    }

    private boolean iterateMultiTree(MinimumWeightMultiTreeSPEntry currEdge, EdgeIterator iter, double edgeWeight, MinimumWeightMultiTreeSPEntry adjEntry, boolean checkUpdate) {
        boolean addToQueue = false;
        for (int i = 0; i < treeEntrySize; ++i) {
            MultiTreeSPEntryItem msptItem = currEdge.getItem(i);
            double entryWeight = msptItem.getWeight();

            if (entryWeight == Double.POSITIVE_INFINITY || (checkUpdate && !msptItem.isUpdate()))
                continue;

            double tmpWeight = edgeWeight + entryWeight;

            MultiTreeSPEntryItem eeItem = adjEntry.getItem(i);

            if (eeItem.getWeight() > tmpWeight) {
                eeItem.setWeight(tmpWeight);
                eeItem.setEdge(iter.getEdge());
                eeItem.setParent(currEdge);
                eeItem.setUpdate(true);
                addToQueue = true;
            }
        }
        return addToQueue;
    }

    public void setEdgeFilter(CoreDijkstraFilter additionalEdgeFilter) {
        this.additionalCoreEdgeFilter = additionalEdgeFilter;
    }

    boolean isCoreNode(int node) {
        return chGraph.getLevel(node) >= coreNodeLevel;
    }

    void updateHighestNode(int adjNode) {
        //We have already reached the core. No need to keep track of the highest node anymore.
        if (highestNodeLevel == coreNodeLevel)
            return;

        if (adjNode < maxNodes) {
            if (highestNode == -1 || highestNodeLevel < chGraph.getLevel(adjNode)) {
                highestNode = adjNode;
                highestNodeLevel = chGraph.getLevel(highestNode);
            }
        } else {
            if (highestNode == -1)
                highestNode = adjNode;
        }
    }

    //TODO integrate into algorithm creation
    public void setMaxVisitedNodes(int numberOfNodes) {
        this.maxVisitedNodes = numberOfNodes;
    }

    protected boolean isMaxVisitedNodesExceeded() {
        return this.maxVisitedNodes < this.visitedNodes;
    }
}
