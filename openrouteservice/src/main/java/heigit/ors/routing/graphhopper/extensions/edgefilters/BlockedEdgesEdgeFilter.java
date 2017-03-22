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
package heigit.ors.routing.graphhopper.extensions.edgefilters;

import java.util.List;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.util.EdgeIteratorState;

import heigit.ors.routing.graphhopper.extensions.flagencoders.HeavyVehicleFlagEncoder;

public class BlockedEdgesEdgeFilter implements EdgeFilter {

	private final boolean in;
	private final boolean out;
	private FlagEncoder encoder;
	private List<Integer> blockedEdges;
	private List<Integer> blockedEdges_hv;

	/**
	 * edges (blockedEdges) for cars and heavy vehicles
	 * edges_hv only for heavy vehicles 
	 **/
	public BlockedEdgesEdgeFilter(FlagEncoder encoder, List<Integer> edges, List<Integer> edges_hv) {
		
		this(encoder, true, true, edges, edges_hv);
	}
	/**
	 * Creates an edges filter which accepts both direction of the specified
	 * vehicle.
	 */
	public BlockedEdgesEdgeFilter(FlagEncoder encoder, boolean in, boolean out, List<Integer> edges, List<Integer> edges_hv) {
		
		this.encoder = encoder;
		this.in = in;
		this.out = out;
		this.blockedEdges = edges;
		this.blockedEdges_hv = edges_hv;
	}

	@Override
	public boolean accept(EdgeIteratorState iter) {
		if (out && iter.isForward(encoder) || in && iter.isBackward(encoder)) {
            if (blockedEdges != null)
            {
            	if (blockedEdges.contains(iter.getOriginalEdge()))
            		return false;
            }
            
            if ((blockedEdges_hv.size()!=0) && ( encoder instanceof HeavyVehicleFlagEncoder))
            {
         
            	if (blockedEdges_hv.contains(iter.getOriginalEdge()))
            		return false;
            }
            
            return true;
		}

		return false;
	}

	@Override
	public String toString() {
		return encoder.toString() + ", in:" + in + ", out:" + out;
	}
}