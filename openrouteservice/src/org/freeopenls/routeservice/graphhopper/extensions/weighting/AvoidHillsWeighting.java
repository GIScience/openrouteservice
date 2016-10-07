package org.freeopenls.routeservice.graphhopper.extensions.weighting;

import org.freeopenls.routeservice.graphhopper.extensions.storages.GraphStorageUtils;
import org.freeopenls.routeservice.graphhopper.extensions.storages.HillIndexGraphStorage;

import com.graphhopper.routing.util.FastestWeighting;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.storage.GraphExtension.ExtendedStorageSequence;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Special weighting for down/uphills
 * <p>
 * @author Maxim Rylov
 */
public class AvoidHillsWeighting extends FastestWeighting
{
    private Weighting superWeighting;
	private HillIndexGraphStorage gsHillIndex;
	private byte[] buffer;
	private double maxSteepness = -1;
	                                         //0     1   2    3    4    5    6    7    8    9   10    11   12   13    14    15
	private static double[] PENALTY_FACTOR = {1.0, 1.0, 1.1, 1.5, 1.7, 1.8, 2.0, 2.2, 2.4, 2.6, 2.8, 3.2, 3.5, 3.7, 3.9, 4.2};
	//private static double[] PENALTY_FACTOR = {1.0, 1.0, 1.1, 1.5, 2.0, 2.1, 2.3, 2.4, 2.5, 2.7, 2.9, 3.1, 3.3, 3.6, 3.8, 4.5};

    public AvoidHillsWeighting(Weighting superWeighting, FlagEncoder encoder, GraphStorage graphStorage, double maxSteepness)
    {
        super(-1, encoder);
        
        this.superWeighting = superWeighting;
        buffer = new byte[1];
        this.maxSteepness = maxSteepness;

        gsHillIndex = GraphStorageUtils.getGraphExtension(graphStorage, HillIndexGraphStorage.class);
    }
    
    @Override
    public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId )
    {
    	double weight = superWeighting.calcWeight(edgeState, reverse, prevOrNextEdgeId);

    	if (gsHillIndex != null)
    	{
    		boolean revert = edgeState.getBaseNode() < edgeState.getAdjNode();
    		int hillIndex = gsHillIndex.getEdgeValue(edgeState.getEdge(), revert, buffer);
    		
    		if (maxSteepness > 0 && hillIndex > maxSteepness)
    			return weight * 100;
    		
    		weight *= PENALTY_FACTOR[hillIndex];
    	}

    	return weight;
    }
}
