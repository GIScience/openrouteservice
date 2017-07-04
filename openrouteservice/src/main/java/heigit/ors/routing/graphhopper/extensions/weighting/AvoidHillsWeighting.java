/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.routing.graphhopper.extensions.weighting;

import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.HillIndexGraphStorage;

import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

/**
 * Special weighting for down/uphills
 * <p>
 * @author Maxim Rylov
 */
public class AvoidHillsWeighting extends FastestWeighting
{
	private HillIndexGraphStorage gsHillIndex;
	private byte[] buffer;
	private double maxSteepness = -1;
	                                         //0     1   2    3    4    5    6    7    8    9   10    11   12   13    14    15
	private static double[] PENALTY_FACTOR = {1.0, 1.0, 1.1, 1.5, 1.7, 1.8, 2.0, 2.2, 2.4, 2.6, 2.8, 3.2, 3.5, 3.7, 3.9, 4.2};
	//private static double[] PENALTY_FACTOR = {1.0, 1.0, 1.1, 1.5, 2.0, 2.1, 2.3, 2.4, 2.5, 2.7, 2.9, 3.1, 3.3, 3.6, 3.8, 4.5};

    public AvoidHillsWeighting(FlagEncoder encoder, PMap map, GraphStorage graphStorage)
    {
        super(encoder, map);
        
        buffer = new byte[1];
        this.maxSteepness = map.getDouble("steepness_maximum", -1);
        
        gsHillIndex = GraphStorageUtils.getGraphExtension(graphStorage, HillIndexGraphStorage.class);
    }
    
    @Override
    public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId )
    {
    	if (gsHillIndex != null)
    	{
    		boolean revert = edgeState.getBaseNode() < edgeState.getAdjNode();
    		int hillIndex = gsHillIndex.getEdgeValue(edgeState.getEdge(), revert, buffer);
    		
    		if (maxSteepness > 0 && hillIndex > maxSteepness)
    			return 100;
    		
    		return PENALTY_FACTOR[hillIndex];
    	}

    	return 1;
    }
}
