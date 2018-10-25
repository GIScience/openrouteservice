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

import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.AbstractWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;



/**
 * This class stores the proxy node for each node in the graph for a given weighting. It is used in core routing algorithms.
 *
 * @author Hendrik Leuschner
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


    public ProxyNodeStorage(GraphHopperStorage graph, Directory dir, final Weighting weighting) {

        this.graph = graph;
        this.core = graph.getCoreGraph(weighting);
        this.weighting = weighting;

        //TODO Add specificier for landmark definition
        this.proxyNodes = dir.find("proxy_nodes_" + AbstractWeighting.weightingToFileName(weighting));
        // 4 byte for the proxy id and 4 byte for the to distance for two directions -> 16B
    }


    /**
     * Calculate the proxy node (closest node in core with specified weighting) for all nodes in the graph.
     * Use a Dijkstra for each node.
     * If the node is a core node, set itself as proxy. If no proxy is found, set -1,-1.
     */
    public void generateProxies() {
        int nodes = graph.getNodes();
        this.proxyNodes.create(1000);
        int coreNodeLevel = nodes + 1;
        SPTEntry proxyNode;

        for (int node = 0; node < nodes; node++) {
            if (core.getLevel(node) == coreNodeLevel) {
                setProxyNode(node, node, 0, false);
                setProxyNode(node, node, 0, true);
                continue;
            }
            //fwd
            ProxyNodeDijkstra proxyNodeDijkstra = new ProxyNodeDijkstra(graph, weighting, TraversalMode.NODE_BASED);
            proxyNode = proxyNodeDijkstra.getProxyNode(node, false);
            // instead of truncating the weight towards 0 always take its upper integer bound to avoid underestimation
            if (proxyNode == null)
                setProxyNode(node, -1, -1, false);
            else
                setProxyNode(node, proxyNode.adjNode, (int) Math.floor(proxyNode.getWeightOfVisitedPath()), false);
            //bwd
            proxyNodeDijkstra = new ProxyNodeDijkstra(graph, weighting, TraversalMode.NODE_BASED);
            proxyNode = proxyNodeDijkstra.getProxyNode(node, true);
            //cast to integer approximates weight but that should not be a problem
            if (proxyNode == null)
                setProxyNode(node, -1, -1, true);
            else
                setProxyNode(node, proxyNode.adjNode, (int) Math.floor(proxyNode.getWeightOfVisitedPath()), true);
        }
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
        throw new IllegalStateException("Do not call ProxyNodeStorage.create directly");
    }

    @Override
    public void flush() {
        proxyNodes.flush();
    }

    @Override
    public void close() {
        proxyNodes.close();

    }


    @Override
    public boolean isClosed() {
        return proxyNodes.isClosed();
    }

    public long getCapacity() {
        return proxyNodes.getCapacity() + proxyNodes.getCapacity();
    }

    /**
     * Set the proxy node in the storage
     * @param nodeId node
     * @param proxyNodeId proxynode
     * @param weight weight from node to proxy
     * @param bwd is backwards?
     */
    private void setProxyNode(int nodeId, int proxyNodeId, int weight, boolean bwd) {
        // Layout in Storage at position nodeId * PROXYBYTES:
        // proxyFWD, proxyWeightFWD, proxyBWD, proxyWeightBWD
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
