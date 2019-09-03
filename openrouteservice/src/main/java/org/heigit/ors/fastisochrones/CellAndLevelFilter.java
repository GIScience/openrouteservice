package org.heigit.ors.fastisochrones;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.util.CHEdgeIteratorState;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.partitioning.IsochroneNodeStorage;


public class CellAndLevelFilter implements EdgeFilter {
    private IsochroneNodeStorage isochroneNodeStorage;
    private CHGraph graph;
    private int cellId;
    private final int maxNodes;


    /* Edge is within a specified Cell */
    public CellAndLevelFilter(IsochroneNodeStorage isochroneNodeStorage, int cellId, int maxNodes, CHGraph g) {
        this.isochroneNodeStorage = isochroneNodeStorage;
        graph = g;
        this.cellId = cellId;
        this.maxNodes = maxNodes - 1;
    }

    public void setCellId(int cellId){
        this.cellId = cellId;
    }

    @Override
    public final boolean accept(EdgeIteratorState iter) {
        int base = iter.getBaseNode();
        int adj = iter.getAdjNode();
        if (base >= maxNodes || adj >= maxNodes)
            return true;

        // minor performance improvement: shortcuts in wrong direction are disconnected, so no need to exclude them
        if (((CHEdgeIteratorState) iter).isShortcut())
            return true;

        if(graph.getLevel(base) <= graph.getLevel(adj))
            return true;

        return isochroneNodeStorage.getCellId(iter.getBaseNode()) == cellId
                && isochroneNodeStorage.getCellId(iter.getAdjNode()) == cellId;
    }
}
