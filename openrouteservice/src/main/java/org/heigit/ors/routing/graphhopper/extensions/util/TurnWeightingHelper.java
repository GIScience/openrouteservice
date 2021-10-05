package org.heigit.ors.routing.graphhopper.extensions.util;

import com.graphhopper.routing.querygraph.EdgeIteratorStateHelper;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.GHUtility;
import org.heigit.ors.routing.graphhopper.extensions.storages.MultiTreeSPEntryItem;

public class TurnWeightingHelper {

    public static void configureTurnWeighting(boolean hasTurnWeighting, EdgeIteratorState iter, MultiTreeSPEntryItem currEdgeItem) {
        if(hasTurnWeighting && !isInORS(iter, currEdgeItem))
            GHUtility.setInORS(false);
    }

    public static void resetTurnWeighting(boolean hasTurnWeighting) {
        if(hasTurnWeighting)
            GHUtility.setInORS(true);
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
