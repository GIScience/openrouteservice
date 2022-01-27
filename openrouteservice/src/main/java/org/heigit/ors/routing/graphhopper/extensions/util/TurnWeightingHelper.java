package org.heigit.ors.routing.graphhopper.extensions.util;

import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.weighting.TurnWeighting;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.graphhopper.extensions.storages.MultiTreeSPEntryItem;

public class TurnWeightingHelper {

    public static void configureTurnWeighting(boolean hasTurnWeighting, TurnWeighting turnWeighting, EdgeIteratorState iter, MultiTreeSPEntryItem currEdgeItem) {
        if(hasTurnWeighting && !isInORS(iter, currEdgeItem))
            turnWeighting.setInORS(false);
    }

    public static void resetTurnWeighting(boolean hasTurnWeighting, TurnWeighting turnWeighting) {
        if(hasTurnWeighting)
            turnWeighting.setInORS(true);
    }

    /**
     * Check whether the turnWeighting should be in the inORS mode. If one of the edges is a virtual one, we need the original edge to get the turn restriction.
     * If the two edges are actually virtual edges on the same original edge, we want to disable inORS mode so that they are not regarded as u turn,
     * because the same edge id left and right of a virtual node results in a u turn
     * @param iter
     * @param currEdgeItem
     * @return
     */
    private static boolean isInORS(EdgeIteratorState iter, MultiTreeSPEntryItem currEdgeItem) {
        return currEdgeItem.getEdge() == iter.getEdge()
                || currEdgeItem.getOriginalEdge() != EdgeIteratorStateHelper.getOriginalEdge(iter);
    }
}
