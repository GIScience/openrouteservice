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
import com.graphhopper.util.CHEdgeIteratorState;
import com.graphhopper.util.EdgeIteratorState;

import java.util.HashMap;
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
    EdgeFilter restrictions;

    boolean inCore = false;

    public void setInCore(boolean inCore) {
        this.inCore = inCore;
    }

    /**
     *
     * @param graph
     */
    public CoreDijkstraFilter(CHGraph graph) {
        this.graph = graph;
        maxNodes = graph.getNodes();
        coreNodeLevel = maxNodes + 1;
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

        if (!inCore) {
            // always accept virtual edges, see #288
            if (base >= maxNodes || adj >= maxNodes)
                return true;
            // minor performance improvement: shortcuts in wrong direction are already disconnected, so no need to check them
            if (((CHEdgeIteratorState) edgeIterState).isShortcut())
                return true;
            else
                return graph.getLevel(base) <= graph.getLevel(adj);
        }
        else {
            if (adj >= maxNodes)
                return false;
            // minor performance improvement: shortcuts in wrong direction are already disconnected, so no need to check them
            if (((CHEdgeIteratorState) edgeIterState).isShortcut())
                return true;

            // do not follow virtual edges, and stay within core
            if (isCoreNode(adj))
                // if edge is in the core check for restrictions
                return restrictions.accept(edgeIterState);
            else
                return false;
        }
    }

    private boolean isCoreNode(int node) {
        return graph.getLevel(node) == coreNodeLevel;
    }

    public void addRestrictionFilter (EdgeFilter restrictions) {
        this.restrictions = restrictions;
    }
}
