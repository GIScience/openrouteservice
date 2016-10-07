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

package org.freeopenls.routeservice.graphhopper.extensions.weighting;

import java.util.HashMap;

import org.freeopenls.routeservice.traffic.AvoidEdgeInfo;
import org.freeopenls.routeservice.traffic.EdgeInfo;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.util.EdgeIteratorState;

public class BlockingWeighting implements Weighting {
	private final FlagEncoder encoder;
	private final double maxSpeed;
	private HashMap<Integer, AvoidEdgeInfo> forbiddenEdges;

	public BlockingWeighting(FlagEncoder encoder, HashMap<Integer, AvoidEdgeInfo> forbiddenEdges) {
		this.encoder = encoder;
		this.maxSpeed = encoder.getMaxSpeed();
		this.forbiddenEdges = forbiddenEdges;
	}

	@Override
	public double getMinWeight(double distance) {
		return distance / maxSpeed;
	}

	@Override
	public String toString() {
		return "BLOCKING";
	}

	@Override
	public double calcWeight(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId ) {
		double speed = 0.0;
		
		AvoidEdgeInfo ei = forbiddenEdges != null ? forbiddenEdges.get(edge.getEdge()) : null;
		
		speed = reverse ? encoder.getReverseSpeed(edge.getFlags()) : encoder.getSpeed(edge.getFlags());
		
		if (ei != null)
		    speed *= ei.getSpeedFactor();
		
		if (speed == 0)
			return Double.POSITIVE_INFINITY;
		return edge.getDistance() / speed;
	}
	
	@Override
	public FlagEncoder getFlagEncoder() {
		return this.encoder;
	}
}