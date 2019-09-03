package org.heigit.ors.fastisochrones;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.partitioning.IsochroneNodeStorage;


public class CellCoreEdgeFilter implements EdgeFilter {
    private IsochroneNodeStorage isochroneNodeStorage;

    public CellCoreEdgeFilter(IsochroneNodeStorage isochroneNodeStorage){
        this.isochroneNodeStorage = isochroneNodeStorage;
    }

    //Put edges into the core that connect cells
    @Override
    public final boolean accept(EdgeIteratorState iter) {
        return isochroneNodeStorage.getCellId(iter.getBaseNode()) == isochroneNodeStorage.getCellId(iter.getAdjNode());
    }
}
