/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/

// Authors: M. Rylov 

package heigit.ors.routing.graphhopper.extensions.edgefilters;

import heigit.ors.routing.graphhopper.extensions.storages.*;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;

public class AvoidSteepnessEdgeFilter implements EdgeFilter {

	private final boolean in;
	private final boolean out;
	protected final FlagEncoder encoder;
	private byte[] buffer;
    private double maximumSteepness;
    private HillIndexGraphStorage gsHillIndex;
    
	public AvoidSteepnessEdgeFilter(FlagEncoder encoder, GraphStorage graphStorage, double maxSteepness) {
		this(encoder, true, true, graphStorage, maxSteepness);
	}

	public AvoidSteepnessEdgeFilter(FlagEncoder encoder, boolean in, boolean out, GraphStorage graphStorage, double maxSteepness) {
		this.in = in;
		this.out = out;

		this.encoder = encoder;
		this.maximumSteepness = maxSteepness;
        this.buffer = new byte[1];
        
        gsHillIndex = GraphStorageUtils.getGraphExtension(graphStorage, HillIndexGraphStorage.class);
	}

	@Override
	public final boolean accept(EdgeIteratorState iter) {

		if (out && iter.isForward(encoder) || in && iter.isBackward(encoder)) {
			
			if (gsHillIndex != null)
	    	{
	    		boolean revert = iter.getBaseNode() < iter.getAdjNode();
	    		int hillIndex = gsHillIndex.getEdgeValue(iter.getOriginalEdge(), revert, buffer);
	    		
	    		if (hillIndex > maximumSteepness)
	    			return false;
	    	}
			
			return true;
		}
		
		return false;
	}

	@Override
	public String toString() {
		return "AVOIDSTEEPNESS|" + encoder;
	}
}
