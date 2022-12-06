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
package org.heigit.ors.routing.graphhopper.extensions.core;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.predicates.IntObjectPredicate;
import com.carrotsearch.hppc.procedures.IntObjectProcedure;
import com.graphhopper.coll.MapEntry;
import com.graphhopper.routing.DijkstraBidirectionCHNoSOD;
import com.graphhopper.routing.SPTEntry;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.Subnetwork;
import com.graphhopper.routing.lm.LandmarkStorage;
import com.graphhopper.routing.lm.SplitArea;
import com.graphhopper.routing.subnetwork.SubnetworkStorage;
import com.graphhopper.routing.util.AreaIndex;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import com.graphhopper.util.Helper;
import com.graphhopper.util.StopWatch;
import com.graphhopper.util.shapes.GHPoint;
import org.apache.log4j.Logger;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopperStorage;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.LMEdgeFilterSequence;
import org.heigit.ors.routing.graphhopper.extensions.util.GraphUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Store Landmark distances for core nodes
 *
 * This code is based on that from GraphHopper GmbH.
 *
 * @author Peter Karich
 * @author Hendrik Leuschner
 * @author Andrzej Oles
 */
public class CoreLandmarkStorage extends LandmarkStorage {
    private static final Logger logger = Logger.getLogger(CoreLandmarkStorage.class);
    private RoutingCHGraphImpl core;
    private final LMEdgeFilterSequence landmarksFilter;
    private Map<Integer, Integer> coreNodeIdMap;
    private final ORSGraphHopperStorage graph;
    private final CoreLMConfig lmConfig;
    private IntHashSet subnetworkNodes;

    public CoreLandmarkStorage(Directory dir, ORSGraphHopperStorage graph, final CoreLMConfig lmConfig, int landmarks) {
        this(dir, graph, (RoutingCHGraphImpl) graph.getCoreGraph(lmConfig.getSuperName()), lmConfig, landmarks);
    }

    //needed primarily for unit tests
    public CoreLandmarkStorage(Directory dir, ORSGraphHopperStorage graph, RoutingCHGraph core, final CoreLMConfig lmConfig, int landmarks) {
        super(graph, dir, lmConfig, landmarks);
        this.graph = graph;
        this.lmConfig = lmConfig;
        this.core = (RoutingCHGraphImpl) core;
        this.landmarksFilter = lmConfig.getEdgeFilter();
        setMinimumNodes(Math.min(getBaseNodes() / 2, 10000));
    }

    public void setCoreNodeIdMap (Map<Integer, Integer> coreNodeIdMap) {
        this.coreNodeIdMap = coreNodeIdMap;
    }

    @Override
    public String getLandmarksFileName() {
        return "landmarks_core_";
    }
    /**
     * This method calculates the landmarks and initial weightings to & from them.
     */
    public void createLandmarks() {
        if (isInitialized())
            throw new IllegalStateException("Initialize the landmark storage only once!");

        int minimumNodes = getMinimumNodes();
        int landmarks = getLandmarkCount();
        DataAccess landmarkWeightDA = getLandmarkWeightDA();
        List<int[]> landmarkIDs = getLandmarkIDs();
        AreaIndex<SplitArea> areaIndex = getAreaIndex();
        boolean logDetails = true;//isLogDetails();
        SubnetworkStorage subnetworkStorage = getSubnetworkStorage();
        int coreNodes = getBaseNodes();

        // fill 'from' and 'to' weights with maximum value
        long maxBytes = (long) coreNodes * LM_ROW_LENGTH;
        landmarkWeightDA.create(2000);
        landmarkWeightDA.ensureCapacity(maxBytes);

        for (long pointer = 0; pointer < maxBytes; pointer += 2) {
            landmarkWeightDA.setShort(pointer, (short) SHORT_INFINITY);
        }

        int[] empty = new int[landmarks];
        Arrays.fill(empty, UNSET_SUBNETWORK);
        landmarkIDs.add(empty);

        byte[] subnetworks = new byte[coreNodes];
        Arrays.fill(subnetworks, (byte) UNSET_SUBNETWORK);

        String snKey = Subnetwork.key(lmConfig.getSuperName());
        // TODO We could use EdgeBasedTarjanSCC instead of node-based TarjanSCC here to get the small networks directly,
        //  instead of using the subnetworkEnc from PrepareRoutingSubnetworks.
        if (!graph.getEncodingManager().hasEncodedValue(snKey))
            throw new IllegalArgumentException("EncodedValue '" + snKey + "' does not exist. For Landmarks this is " +
                    "currently required (also used in PrepareRoutingSubnetworks). See #2256");

        // Exclude edges that we previously marked in PrepareRoutingSubnetworks to avoid problems like "connection not found".
        final BooleanEncodedValue edgeInSubnetworkEnc = graph.getEncodingManager().getBooleanEncodedValue(snKey);
        final IntHashSet blockedEdges;
        // We use the areaIndex to split certain areas from each other but do not permanently change the base graph
        // so that other algorithms still can route through these regions. This is done to increase the density of
        // landmarks for an area like Europe+Asia, which improves the query speed.
        if (areaIndex != null) {
            StopWatch sw = new StopWatch().start();
            blockedEdges = findBorderEdgeIds(areaIndex);
            if (logDetails)
                logger.info(configName() + "Made " + blockedEdges.size() + " edges inaccessible. Calculated country cut in " + sw.stop().getSeconds() + "s, " + Helper.getMemInfo());
        } else {
            blockedEdges = new IntHashSet();
        }

        EdgeFilter blockedEdgesFilter = edge -> !edge.get(edgeInSubnetworkEnc) && !blockedEdges.contains(edge.getEdge());
        EdgeFilter accessFilter = edge -> blockedEdgesFilter.accept(edge) && landmarksFilter.accept(edge);

        StopWatch sw = new StopWatch().start();
        TarjansCoreSCCAlgorithm tarjanAlgo = new TarjansCoreSCCAlgorithm(graph, core, accessFilter, false);
        List<IntArrayList> graphComponents = tarjanAlgo.findComponents();
        if (logDetails)
            logger.info(configName() + "Calculated " + graphComponents.size() + " subnetworks via tarjan in " + sw.stop().getSeconds() + "s, " + Helper.getMemInfo());

        String additionalInfo = "";
        // guess the factor
        if (getFactor() <= 0) {
            // A 'factor' is necessary to store the weight in just a short value but without losing too much precision.
            // This factor is rather delicate to pick, we estimate it from an exploration with some "test landmarks",
            // see estimateMaxWeight. If we pick the distance too big for small areas this could lead to (slightly)
            // suboptimal routes as there will be too big rounding errors. But picking it too small is bad for performance
            // e.g. for Germany at least 1500km is very important otherwise speed is at least twice as slow e.g. for 1000km
            double maxWeight = estimateMaxWeight(graphComponents, accessFilter);
            setMaximumWeight(maxWeight);
            additionalInfo = ", maxWeight:" + maxWeight + " from quick estimation";
        }
        double factor = getFactor();

        if (logDetails)
            logger.info(configName() + "init landmarks for subnetworks with node count greater than " + minimumNodes + " with factor:" + factor + additionalInfo);

        int nodes = 0;
        for (IntArrayList subnetworkIds : graphComponents) {
            nodes += subnetworkIds.size();
            if (subnetworkIds.size() < minimumNodes)
                continue;
            if (factor <= 0)
                throw new IllegalStateException("factor wasn't initialized " + factor + ", subnetworks:"
                        + graphComponents.size() + ", minimumNodes:" + minimumNodes + ", current size:" + subnetworkIds.size());

            subnetworkNodes = new IntHashSet(subnetworkIds);
            int index = subnetworkIds.size() - 1;
            for (; index >= 0; index--) {
                int nextStartNode = subnetworkIds.get(index);
                if (subnetworks[getIndex(nextStartNode)] == UNSET_SUBNETWORK) {
                    if (logDetails) {
                        GHPoint p = createPoint(graph, nextStartNode);
                        logger.info(configName() + "start node: " + nextStartNode + " (" + p + ") subnetwork " + index + ", subnetwork size: " + subnetworkIds.size()
                                + ", " + Helper.getMemInfo() + ((areaIndex == null) ? "" : " area:" + areaIndex.query(p.lat, p.lon)));
                    }
                    if (createLandmarksForSubnetwork(nextStartNode, subnetworks, accessFilter))
                        break;
                }
            }
            if (index < 0)
                logger.warn("next start node not found in big enough network of size " + subnetworkIds.size() + ", first element is " + subnetworkIds.get(0) + ", " + createPoint(graph, subnetworkIds.get(0)));
        }

        int subnetworkCount = landmarkIDs.size();
        // store all landmark node IDs and one int for the factor itself.
        landmarkWeightDA.ensureCapacity(maxBytes /* landmark weights */ + subnetworkCount * landmarks /* landmark mapping per subnetwork */);

        // calculate offset to point into landmark mapping
        long bytePos = maxBytes;
        for (int[] lms : landmarkIDs) {
            for (int lmNodeId : lms) {
                landmarkWeightDA.setInt(bytePos, lmNodeId);
                bytePos += 4L;
            }
        }

        landmarkWeightDA.setHeader(0 * 4, coreNodes);
        landmarkWeightDA.setHeader(1 * 4, landmarks);
        landmarkWeightDA.setHeader(2 * 4, subnetworkCount);
        if (factor * DOUBLE_MLTPL > Integer.MAX_VALUE)
            throw new UnsupportedOperationException("landmark weight factor cannot be bigger than Integer.MAX_VALUE " + factor * DOUBLE_MLTPL);
        landmarkWeightDA.setHeader(3 * 4, (int) Math.round(factor * DOUBLE_MLTPL));

        // serialize fast byte[] into DataAccess
        subnetworkStorage.create(coreNodes);
        for (int nodeId = 0; nodeId < subnetworks.length; nodeId++) {
            subnetworkStorage.setSubnetwork(nodeId, subnetworks[nodeId]);
        }

        if (logDetails)
            logger.info(configName() + "Finished landmark creation. Subnetwork node count sum " + nodes + " vs. nodes " + coreNodes);
        setInitialized(true);
    }

    private String configName() {
        return "[" + lmConfig.getName() + "] ";
    }

    @Override
    public int getIndex(int node) {
        return coreNodeIdMap.get(node);
    }

    @Override
    protected int getBaseNodes() {
        return core.getCoreNodes();
    }

    protected static class CoreEdgeFilter implements CHEdgeFilter {
        private final RoutingCHGraph graph;
        EdgeFilter edgeFilter;
        private final int coreNodeLevel;

        public CoreEdgeFilter(RoutingCHGraph graph, EdgeFilter edgeFilter) {
            this.graph = graph;
            this.edgeFilter = edgeFilter;
            coreNodeLevel = GraphUtils.getBaseGraph(graph).getNodes();
        }

        @Override
        public boolean accept(RoutingCHEdgeIteratorState edgeState) {
            if (isCoreEdge(edgeState))
                return acceptEdge(edgeState);
            else
                return false;
        }

        private boolean isCoreEdge(RoutingCHEdgeIteratorState edgeState) {
            int base = edgeState.getBaseNode();
            int adj = edgeState.getAdjNode();

            return graph.getLevel(base) >= coreNodeLevel && graph.getLevel(adj) >= coreNodeLevel;
        }

        private boolean acceptEdge(RoutingCHEdgeIteratorState edgeState) {
            if (edgeFilter==null)
                return true;
            if (edgeState.isShortcut())
                return true;

            return edgeFilter.accept(((RoutingCHEdgeIteratorStateImpl) edgeState).getBaseGraphEdgeState());
        }
    }

    @Override
    public LandmarkExplorer getLandmarkExplorer(EdgeFilter accessFilter, Weighting weighting, boolean reverse) {
        return new CoreLandmarkExplorer(core, accessFilter, reverse, this.subnetworkNodes);
    }

    @Override
    public LandmarkExplorer getLandmarkSelector(EdgeFilter accessFilter) {
        return new CoreLandmarkSelector(core, accessFilter, false, this.subnetworkNodes);
    }

    /**
     * This class is used to calculate landmark location (equally distributed).
     * It derives from DijkstraBidirectionRef, but is only used as forward or backward search.
     */
    private class CoreLandmarkExplorer extends DijkstraBidirectionCHNoSOD implements LandmarkExplorer {
        private final boolean reverse;
        private SPTEntry lastEntry;

        public CoreLandmarkExplorer(RoutingCHGraph g, EdgeFilter accessFilter, boolean reverse, IntHashSet subnetworkNodes) {
            super(g);
            //TODO: implement a better solution to the issue of picking nodes from outside of the strongly connected
            // component. Provided that the edge filters are set up properly and work as intended the additional check
            // shouldn't be in principle neccessary.
            CHEdgeFilter subnetworkFilter = edge -> subnetworkNodes == null || subnetworkNodes.contains(edge.getAdjNode());
            CHEdgeFilter coreEdgeFilter = new CoreEdgeFilter(g, accessFilter);
            this.levelEdgeFilter = edge -> subnetworkFilter.accept(edge) && coreEdgeFilter.accept(edge);
            this.reverse = reverse;
            // set one of the bi directions as already finished
            if (reverse)
                finishedFrom = true;
            else
                finishedTo = true;

            // no path should be calculated
            setUpdateBestPath(false);
        }

        @Override
        public void setStartNode(int startNode) {
            if (reverse)
                initTo(startNode, 0);
            else
                initFrom(startNode, 0);
        }

        @Override
        public int getFromCount() {
            return bestWeightMapFrom.size();
        }

        @Override
        public void runAlgo() {
            super.runAlgo();
        }

        // Need to override the DijkstraBidirectionCHNoSOD method as it uses the graphs weighting instead of the CoreLandmarkStorage one.
        // The graph uses a turn cost based weighting, though, which is not allowed for LM distance calculation.
        @Override
        protected double calcWeight(RoutingCHEdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
            double edgeWeight = edgeState.getWeight(reverse);
            return edgeWeight;
        }

        @Override
        public SPTEntry getLastEntry() {
            if (!finished())
                throw new IllegalStateException("Cannot get max weight if not yet finished");
            return lastEntry;
        }

        @Override
        public boolean finished() {
            if (reverse) {
                lastEntry = currTo;
                return finishedTo;
            } else {
                lastEntry = currFrom;
                return finishedFrom;
            }
        }

        @Override
        public boolean setSubnetworks(final byte[] subnetworks, final int subnetworkId) {
            if (subnetworkId > 127)
                throw new IllegalStateException("Too many subnetworks " + subnetworkId);

            final AtomicBoolean failed = new AtomicBoolean(false);
            IntObjectMap<SPTEntry> map = reverse ? bestWeightMapTo : bestWeightMapFrom;
            map.forEach(new IntObjectPredicate<SPTEntry>() {
                @Override
                public boolean apply(int nodeId, SPTEntry value) {
                    nodeId = getIndex(nodeId);
                    int sn = subnetworks[nodeId];
                    if (sn != subnetworkId) {
                        if (sn != UNSET_SUBNETWORK && sn != UNCLEAR_SUBNETWORK) {
                            // this is ugly but can happen in real world, see testWithOnewaySubnetworks
                            logger.error("subnetworkId for node " + nodeId
                            + " (" + createPoint(graph.getBaseGraph(), nodeId) + ") already set (" + sn + "). " + "Cannot change to " + subnetworkId);

                            failed.set(true);
                            return false;
                        }

                        subnetworks[nodeId] = (byte) subnetworkId;
                    }
                    return true;
                }
            });
            return failed.get();
        }

        @Override
        public void initLandmarkWeights(final int lmIdx, int lmNodeId, final long rowSize, final int offset) {
            IntObjectMap<SPTEntry> map = reverse ? bestWeightMapTo : bestWeightMapFrom;
            final AtomicInteger maxedout = new AtomicInteger(0);
            final Map.Entry<Double, Double> finalMaxWeight = new MapEntry<>(0d, 0d);

            map.forEach(new IntObjectProcedure<SPTEntry>() {
                @Override
                public void apply(int nodeId, SPTEntry b) {
                    nodeId = getIndex(nodeId);
                    if (!setWeight(nodeId * rowSize + lmIdx * 4 + offset, b.weight)) {
                        maxedout.incrementAndGet();
                        finalMaxWeight.setValue(Math.max(b.weight, finalMaxWeight.getValue()));
                    }
                }
            });

            if ((double) maxedout.get() / map.size() > 0.1) {
                logger.warn("landmark " + lmIdx + " (" + nodeAccess.getLat(lmNodeId) + "," + nodeAccess.getLon(lmNodeId) + "): " +
                        "too many weights were maxed out (" + maxedout.get() + "/" + map.size() + "). Use a bigger factor than " + getFactor()
                        + ". For example use maximum_lm_weight: " + finalMaxWeight.getValue() * 1.2 + " in your LM profile definition");
            }
        }
    }

    private class CoreLandmarkSelector extends CoreLandmarkExplorer {

        public CoreLandmarkSelector(RoutingCHGraph g, EdgeFilter accessFilter, boolean reverse, IntHashSet subnetworkNodes) {
            super(g, accessFilter, reverse, subnetworkNodes);
        }

        @Override
        protected double calcWeight(RoutingCHEdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
            if (edgeState.isShortcut())
                return expandEdge(edgeState);

            if (super.calcWeight(edgeState, reverse, prevOrNextEdgeId) >= Double.MAX_VALUE)
                return Double.POSITIVE_INFINITY;

            return 1;
        }

        private int expandEdge(RoutingCHEdgeIteratorState mainEdgeState) {
            if (!mainEdgeState.isShortcut())
                return 1;

            int skippedEdge1 = mainEdgeState.getSkippedEdge1();
            int skippedEdge2 = mainEdgeState.getSkippedEdge2();
            int from = mainEdgeState.getBaseNode();
            int to = mainEdgeState.getAdjNode();

            RoutingCHEdgeIteratorState iter1, iter2;
            iter1 = core.getEdgeIteratorState(skippedEdge1, from);
            if (iter1 == null) {
                iter1 = core.getEdgeIteratorState(skippedEdge2, from);
                iter2 = core.getEdgeIteratorState(skippedEdge1, to);
            }
            else {
                iter2 = core.getEdgeIteratorState(skippedEdge2, to);
            }

            return expandEdge(iter1) + expandEdge(iter2);
        }

    }
}
