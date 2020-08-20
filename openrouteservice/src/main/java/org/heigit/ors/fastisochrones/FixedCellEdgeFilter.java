package org.heigit.ors.fastisochrones;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;

/**
 * EdgeFilter to stay within cell.
 * <p>
 *
 * @author Hendrik Leuschner
 */
public class FixedCellEdgeFilter implements EdgeFilter {
    //Any node with an id higher than this is virtual
    private final int maxNodes;
    private IsochroneNodeStorage isochroneNodeStorage;
    private int cellId;

    /* Edge is within a specified Cell */
    public FixedCellEdgeFilter(IsochroneNodeStorage isochroneNodeStorage, int cellId, int maxNodes) {
        this.isochroneNodeStorage = isochroneNodeStorage;
        this.cellId = cellId;
        this.maxNodes = maxNodes;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    @Override
    public final boolean accept(EdgeIteratorState iter) {
        if (iter.getBaseNode() >= maxNodes || iter.getAdjNode() >= maxNodes)
            return true;
        return isochroneNodeStorage.getCellId(iter.getBaseNode()) == cellId
                && isochroneNodeStorage.getCellId(iter.getAdjNode()) == cellId;
    }
}
