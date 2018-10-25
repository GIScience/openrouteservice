/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.routing.graphhopper.extensions.core;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.predicates.IntObjectPredicate;
import com.carrotsearch.hppc.procedures.IntObjectProcedure;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.coll.MapEntry;
import com.graphhopper.routing.DijkstraBidirectionRef;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.lm.LandmarkStorage;
import com.graphhopper.routing.lm.LandmarkSuggestion;
import com.graphhopper.routing.subnetwork.SubnetworkStorage;
import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.util.spatialrules.SpatialRule;
import com.graphhopper.routing.util.spatialrules.SpatialRuleLookup;
import com.graphhopper.routing.weighting.AbstractWeighting;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import com.graphhopper.util.*;
import com.graphhopper.util.exceptions.ConnectionNotFoundException;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint;
import heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;



/**
 * This class stores the landmark nodes and the weights from and to all other nodes in every
 * subnetwork. This data is created to apply a speed-up for path calculation but at the same times
 * stays flexible to per-request changes. The class is safe for usage from multiple reading threads
 * across algorithms.
 *
 * @author Peter Karich
 */
public class ProxyNodeStorage implements Storable<ProxyNodeStorage>{
    private static class VirtEntry {
        private int node;
        private int weight;

        @Override
        public String toString() {
            return node + ", " + weight;
        }
    }
    private final DataAccess proxyNodes;
    private final CHGraphImpl core;
    private final GraphHopperStorage graph;
    private final Weighting weighting;



    private int PROXYBYTES = 16;
    private int PROXY_OFFSET = 4;

    /**
     * 'to' and 'from' fit into 32 bit => 16 bit for each of them => 65536
     */

    public ProxyNodeStorage(GraphHopperStorage graph, Directory dir, final Weighting weighting) {

        this.graph = graph;
        this.core = graph.getCoreGraph(weighting);
        this.weighting = weighting;

        //TODO Add specificier for landmark definition
//        makeVirtualNodeMap(core.getNodes(),);
        this.proxyNodes = dir.find("proxy_nodes_" + AbstractWeighting.weightingToFileName(weighting));
        // 4 byte for the proxy id and 4 byte for the to distance to it and 4 byte for the from distance
    }


    /**
     * Calculate the proxy node (closest node in core with specified weighting) for all non-core nodes in the graph.
     * Use a Dijkstra for each node
     */
    public void generateProxies() {
        int nodes = graph.getNodes();
        this.proxyNodes.create(1000);
        int coreNodeLevel = nodes + 1;
        SPTEntry proxyNode;
        int countProxy = 0;
        int countNot = 0;
        for (int node = 0; node < nodes; node++) {
            if(core.getLevel(node) == coreNodeLevel){
                setProxyNode(node, node, 0, false);
                setProxyNode(node, node, 0, true);
                countNot++;
                continue;
            }
            //fwd
            ProxyNodeDijkstra proxyNodeDijkstra = new ProxyNodeDijkstra(graph, weighting, TraversalMode.NODE_BASED);
            proxyNode = proxyNodeDijkstra.getProxyNode(node, false);
            //cast to integer approximates weight but that should not be a problem
            if (proxyNode == null)
                setProxyNode(node, -1, -1, false);
            else{
                countProxy++;
                setProxyNode(node, proxyNode.adjNode, (int)proxyNode.getWeightOfVisitedPath(), false);

            }

            //bwd
            proxyNodeDijkstra = new ProxyNodeDijkstra(graph, weighting, TraversalMode.NODE_BASED);
            proxyNode = proxyNodeDijkstra.getProxyNode(node, true);
            //cast to integer approximates weight but that should not be a problem
            if (proxyNode == null)
                setProxyNode(node, -1, -1, true);
            else
                setProxyNode(node, proxyNode.adjNode, (int)proxyNode.getWeightOfVisitedPath(), true);
        }
        int test = 0;
    }

    @Override
    public boolean loadExisting() {
        if (proxyNodes.loadExisting()) {
            return true;
        }
        return false;
    }


    @Override
    public ProxyNodeStorage create(long byteCount) {
        throw new IllegalStateException("Do not call LandmarkStore.create directly");
    }

    @Override
    public void flush() {
        proxyNodes.flush();
    }

    @Override
    public void close() {
        proxyNodes.close();

    }

//    public void makeVirtualNodeMap(int maxBaseNodes,){
//        int idxVirtNode = maxBaseNodes;
//        virtNodeMap = new GHIntObjectHashMap(graph.getNodes() - idxVirtNode, 0.5f);
//        // virtual nodes handling: calculate the minium weight for the virt. nodes, i.e. pick the correct neighbouring node
//        if (graph instanceof QueryGraph) {
//            QueryGraph qGraph = (QueryGraph) graph;
//            // there are at least two virtual nodes (start & destination)
//            for (; idxVirtNode < qGraph.getNodes(); idxVirtNode++) {
//                // we need the real underlying edge as neighboring nodes could be virtual too
//                EdgeIteratorState edge = qGraph.getOriginalEdgeFromVirtNode(idxVirtNode);
//
//                int weight = lms.calcWeight(edge, reverse);
//                int reverseWeight = lms.calcWeight(edge, !reverse);
//                CoreLMApproximator.VirtEntry virtEntry = new CoreLMApproximator.VirtEntry();
//                if (weight < Integer.MAX_VALUE && (reverseWeight >= Integer.MAX_VALUE || weight < reverseWeight)) {
//                    virtEntry.weight = weight;
//                    virtEntry.node = reverse ? edge.getBaseNode() : edge.getAdjNode();
//                } else {
//                    virtEntry.weight = reverseWeight;
//                    if (reverseWeight >= Integer.MAX_VALUE)
//                        throw new IllegalStateException("At least one direction of edge (" + edge + ") should be accessible but wasn't!");
//
//                    virtEntry.node = reverse ? edge.getAdjNode() : edge.getBaseNode();
//                }
//
//                virtNodeMap.put(idxVirtNode, virtEntry);
//            }
//        }
//    }

    @Override
    public boolean isClosed() {
        return proxyNodes.isClosed();
    }

    public long getCapacity() {
        return proxyNodes.getCapacity() + proxyNodes.getCapacity();
    }

    private void setProxyNode(int nodeId, int proxyNodeId, int weight, boolean bwd) {
        long tmp = (long)nodeId * PROXYBYTES;
        proxyNodes.ensureCapacity(tmp + PROXYBYTES);
        if(!bwd) {
            proxyNodes.setInt(tmp, proxyNodeId);
            proxyNodes.setInt(tmp + PROXY_OFFSET, weight);
        }
        else {
            proxyNodes.setInt(tmp + 2 * PROXY_OFFSET, proxyNodeId);
            proxyNodes.setInt(tmp + 3 * PROXY_OFFSET, weight);
        }
    }

    public int[] getProxyNodeAndWeight(int nodeId, boolean bwd) {
        long tmp = (long)nodeId * PROXYBYTES;
        proxyNodes.ensureCapacity(tmp + PROXYBYTES);
        int[] value = new int[2];
        if(!bwd) {
            value[0] = proxyNodes.getInt(tmp);
            value[1] = proxyNodes.getInt(tmp + PROXY_OFFSET);
        }
        else {
            value[0] = proxyNodes.getInt(tmp + 2 * PROXY_OFFSET);
            value[1] = proxyNodes.getInt(tmp + 3 * PROXY_OFFSET);
        }
        return value;
    }


}
