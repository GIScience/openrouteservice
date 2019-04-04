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
package heigit.ors.routing.graphhopper.extensions.core;

import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.weighting.BeelineWeightApproximator;
import com.graphhopper.routing.weighting.WeightApproximator;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIteratorState;

import java.util.Arrays;

/**
 * This class is a weight approximation based on precalculated landmarks for core.
 *
 * This code is based on that from GraphHopper GmbH.
 *
 * @author Peter Karich
 * @author Hendrik Leuschner
 */
public class CoreLMApproximator implements WeightApproximator {
    private static class VirtEntry {
        private int node;
        private int weight;

        @Override
        public String toString() {
            return node + ", " + weight;
        }
    }

    private final CoreLandmarkStorage lms;

    // store node ids
    private int[] activeLandmarks;
    // store weights as int
    private int[] activeFromIntWeights;
    private int[] activeToIntWeights;
    private double epsilon = 1;
    private int to = -1;
    private double proxyWeight = 0;
    // do activate landmark recalculation
    private boolean doALMRecalc = true;
    private final double factor;
    private final boolean reverse;
    private final int maxBaseNodes;
    private final Graph graph;
    private final WeightApproximator fallBackApproximation;
    private boolean fallback = false;
    private final GHIntObjectHashMap<VirtEntry> virtNodeMap;

    public CoreLMApproximator(Graph graph, int maxBaseNodes, CoreLandmarkStorage lms, int activeCount,
                              double factor, boolean reverse) {
        this.reverse = reverse;
        this.lms = lms;
        this.factor = factor;
        if (activeCount > lms.getLandmarkCount())
            throw new IllegalArgumentException("Active landmarks " + activeCount
                    + " should be lower or equals to landmark count " + lms.getLandmarkCount());

        activeLandmarks = new int[activeCount];
        Arrays.fill(activeLandmarks, -1);
        activeFromIntWeights = new int[activeCount];
        activeToIntWeights = new int[activeCount];

        this.graph = graph;
        this.fallBackApproximation = new BeelineWeightApproximator(graph.getNodeAccess(), lms.getWeighting());
        this.maxBaseNodes = maxBaseNodes;
        int idxVirtNode = maxBaseNodes;
        virtNodeMap = new GHIntObjectHashMap(graph.getNodes() - idxVirtNode, 0.5f);
        // virtual nodes handling: calculate the minium weight for the virt. nodes, i.e. pick the correct neighbouring node
        if (graph instanceof QueryGraph) {
            QueryGraph qGraph = (QueryGraph) graph;
            // there are at least two virtual nodes (start & destination)
            for (; idxVirtNode < qGraph.getNodes(); idxVirtNode++) {
                // we need the real underlying edge as neighboring nodes could be virtual too
                EdgeIteratorState edge = qGraph.getOriginalEdgeFromVirtNode(idxVirtNode);

                int weight = lms.calcWeight(edge, reverse);
                int reverseWeight = lms.calcWeight(edge, !reverse);
                VirtEntry virtEntry = new VirtEntry();
                if (weight < Integer.MAX_VALUE && (reverseWeight >= Integer.MAX_VALUE || weight < reverseWeight)) {
                    virtEntry.weight = weight;
                    virtEntry.node = reverse ? edge.getBaseNode() : edge.getAdjNode();
                } else {
                    virtEntry.weight = reverseWeight;
                    if (reverseWeight >= Integer.MAX_VALUE)
                        throw new IllegalStateException("At least one direction of edge (" + edge + ") should be accessible but wasn't!");

                    virtEntry.node = reverse ? edge.getAdjNode() : edge.getBaseNode();
                }

                virtNodeMap.put(idxVirtNode, virtEntry);
            }
        }
    }

    /**
     * Increase approximation with higher epsilon
     */
    public CoreLMApproximator setEpsilon(double epsilon) {
        this.epsilon = epsilon;
        return this;
    }

    @Override
    public double approximate(final int queryNode) {
        if (!doALMRecalc && fallback || lms.isEmpty())
            return fallBackApproximation.approximate(queryNode);

        int node = queryNode;
        int virtEdgeWeightInt = 0;
        if (queryNode >= maxBaseNodes) {
            // handle virtual node
            VirtEntry virtEntry = virtNodeMap.get(queryNode);
            node = virtEntry.node;
            virtEdgeWeightInt = virtEntry.weight;
        }

        // select better active landmarks, LATER: use 'success' statistics about last active landmark
        // we have to update the priority queues and the maps if done in the middle of the search http://cstheory.stackexchange.com/q/36355/13229
        if (doALMRecalc) {
            doALMRecalc = false;
            if (!initActiveLandmarks(node))
                return fallBackApproximation.approximate(queryNode);
        }

        int maxWeightInt = getMaxWeight(node, virtEdgeWeightInt, activeLandmarks, activeFromIntWeights, activeToIntWeights);

        double weightDouble = maxWeightInt * factor * epsilon - proxyWeight;

        return weightDouble;
    }

    int getMaxWeight(int node, int virtEdgeWeightInt, int[] activeLandmarks, int[] activeFromIntWeights, int[] activeToIntWeights) {
        int maxWeightInt = -1;
        for (int activeLMIdx = 0; activeLMIdx < activeLandmarks.length; activeLMIdx++) {
            int landmarkIndex = activeLandmarks[activeLMIdx];

            // 1. assume route from a to b: a--->v--->b and a landmark LM.
            //    From this we get two inequality formulas where v is the start (or current node) and b is the 'to' node:
            //    LMv + vb >= LMb therefor vb >= LMb - LMv => 'getFromWeight'
            //    vb + bLM >= vLM therefor vb >= vLM - bLM => 'getToWeight'
            // 2. for the case a->v the sign is reverse as we need to know the vector av not va => if(reverse) "-weight"
            // 3. as weight is the full edge weight for now (and not the precise weight to the virt node) we can only add it to the subtrahend
            //    to avoid overestimating (keep the result strictly lower)

            int fromWeightInt = activeFromIntWeights[activeLMIdx] - lms.getFromWeight(landmarkIndex, node);
            int toWeightInt = lms.getToWeight(landmarkIndex, node) - activeToIntWeights[activeLMIdx];

            int tmpMaxWeightInt = reverse ? Math.max(-fromWeightInt, -toWeightInt) : Math.max(fromWeightInt, toWeightInt);

            if (tmpMaxWeightInt > maxWeightInt)
                maxWeightInt = tmpMaxWeightInt;
        }
        return maxWeightInt;
    }

    final int getNode(int node) {
        return node >= maxBaseNodes ? virtNodeMap.get(node).node : node;
    }

    /*
     * This method initializes the to/from nodes in the forward/reverse approximators
     */
    @Override
    public void setTo(int to) {
        this.to = getNode(to);
    }

    @Override
    public CoreLMApproximator reverse() {
        return new CoreLMApproximator(graph, maxBaseNodes, lms, activeLandmarks.length, factor, !reverse);
    }

    public void setProxyWeight(double proxyDistance){
        proxyWeight = proxyDistance;
    }

    public boolean initActiveLandmarks(int from) {
        doALMRecalc = false;
        boolean res = lms.initActiveLandmarks(from, to, activeLandmarks, activeFromIntWeights, activeToIntWeights, reverse);
        if (!res)
            // note: fallback==true means forever true!
            fallback = true;
        return res;
    }

    public int[] getActiveLandmarks() {
        return activeLandmarks;
    }

    public void setActiveLandmarks(int[] activeLandmarks) {
        doALMRecalc = false;
        this.activeLandmarks = activeLandmarks;
        lms.initActiveLandmarkWeights(to, activeLandmarks, activeFromIntWeights, activeToIntWeights);
    }

    public double getfFactor() {
        return factor;
    }

    /**
     * This method forces a lazy recalculation of the active landmark set e.g. necessary after the 'to' node changed.
     */
    public void triggerActiveLandmarkRecalculation() {
        doALMRecalc = true;
    }

    @Override
    public String toString() {
        return "landmarks";
    }
}
