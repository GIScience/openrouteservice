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

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.CHEdgeIteratorState;
import com.graphhopper.util.EdgeIteratorState;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.HeavyVehicleAttributesGraphStorage;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Only certain nodes are accepted and therefor the others are ignored.
 * <p>
 *
 * @author Peter Karich
 */
public class CoreDijkstraFilter implements EdgeFilter {
    private final CHGraph graph;
    private final int maxNodes;
    private final int coreNodeLevel;
    static boolean[] isCoreNode;
    EdgeFilter restrictions;

    boolean inCore = false;

    public void setInCore(boolean inCore) {
        this.inCore = inCore;
    }

    /**
     *
     * @param g
     */
    public CoreDijkstraFilter(CHGraph g) {
        graph = g;
        maxNodes = g.getNodes();
        coreNodeLevel = maxNodes + 1;

        isCoreNode = new boolean[maxNodes];
        for (int node = 0; node < maxNodes; node++)
            isCoreNode[node] = graph.getLevel(node) == coreNodeLevel;
    }

    /**
     *
     * @param edgeIterState iterator pointing to a given edge
     * @return true iff the edge is virtual or is a shortcut or the level of the base node is greater/equal than
     * the level of the adjacent node
     */
    @Override
    
    public boolean accept(EdgeIteratorState edgeIterState) {
        int base = edgeIterState.getBaseNode();
        int adj = edgeIterState.getAdjNode();

        // always accept virtual edges, see #288
        if (base >= maxNodes || adj >= maxNodes)
            return true;

        if (!inCore) {
            // minor performance improvement: shortcuts in wrong direction are already disconnected, so no need to check them
            if (((CHEdgeIteratorState) edgeIterState).isShortcut())
                return true;
            else
                return graph.getLevel(base) <= graph.getLevel(adj);
        }
        else {
            // minor performance improvement: shortcuts in wrong direction are already disconnected, so no need to check them
            if (((CHEdgeIteratorState) edgeIterState).isShortcut())
                return true;

            // stay within core
            if (!isCoreNode[adj])
                return false;
            else
                // if edge is in the core check for restrictions
                return restrictions.accept(edgeIterState);
        }
    }

    public void addRestrictionFilter (EdgeFilter restrictions) {
        this.restrictions = restrictions;
    }
}
