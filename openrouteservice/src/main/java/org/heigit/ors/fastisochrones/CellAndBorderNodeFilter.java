package org.heigit.ors.fastisochrones;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;

/**
 * Allows all edges within a given cell AND all edges to bordernodes
 * <p>
 *
 * @author Hendrik Leuschner
 */
public class CellAndBorderNodeFilter implements EdgeFilter {
    private final int maxNodes;
    private final IsochroneNodeStorage isochroneNodeStorage;
    private int cellId;

    /* Edge is within a specified Cell */
    public CellAndBorderNodeFilter(IsochroneNodeStorage isochroneNodeStorage, int cellId, int maxNodes) {
        this.isochroneNodeStorage = isochroneNodeStorage;
        this.cellId = cellId;
        this.maxNodes = maxNodes - 1;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    @Override
    public final boolean accept(EdgeIteratorState iter) {
        int base = iter.getBaseNode();
        int adj = iter.getAdjNode();
        if (base >= maxNodes || adj >= maxNodes)
            return true;
        if (isochroneNodeStorage.getCellId(base) == cellId
                && isochroneNodeStorage.getCellId(adj) == cellId) {
            return true;
        }
        return isochroneNodeStorage.getBorderness(adj);
    }
}
