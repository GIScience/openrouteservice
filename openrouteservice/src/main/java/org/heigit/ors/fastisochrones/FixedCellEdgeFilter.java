package org.heigit.ors.fastisochrones;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.partitioning.IsochroneNodeStorage;

/**
 * EdgeFilter to stay within cell.
 * <p>
 *
 * @author Hendrik Leuschner
 */

public class FixedCellEdgeFilter implements EdgeFilter {
    private IsochroneNodeStorage isochroneNodeStorage;
    private int cellId;
    private final int maxNodes;


    /* Edge is within a specified Cell */
    public FixedCellEdgeFilter(IsochroneNodeStorage isochroneNodeStorage, int cellId, int maxNodes) {
        this.isochroneNodeStorage = isochroneNodeStorage;
        this.cellId = cellId;
        this.maxNodes = maxNodes - 1;
    }

    public void setCellId(int cellId){
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
