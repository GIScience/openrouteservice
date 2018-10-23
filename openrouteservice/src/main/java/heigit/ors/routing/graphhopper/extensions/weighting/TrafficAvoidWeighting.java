/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
 */
package heigit.ors.routing.graphhopper.extensions.weighting;


import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.AbstractWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import heigit.ors.routing.graphhopper.extensions.flagencoders.CarFlagEncoder;
import heigit.ors.routing.graphhopper.extensions.flagencoders.tomove.exghoverwrite.ExGhORSCarFlagEncoder;
import heigit.ors.routing.traffic.AvoidEdgeInfo;
import heigit.ors.routing.traffic.TmcEventCodesTable;
import heigit.ors.routing.traffic.TmcMode;
import heigit.ors.routing.traffic.TrafficEventInfo;

import java.util.HashMap;

public class TrafficAvoidWeighting extends AbstractWeighting {

    /**
     * Converting to seconds is not necessary but makes adding other penalities easier (e.g. turn
     * costs or traffic light costs etc)
     */
    protected final static double SPEED_CONV = 1;
    private double maxSpeed;
	private HashMap<Integer, AvoidEdgeInfo> forbiddenEdges;

    public TrafficAvoidWeighting( FlagEncoder encoder, PMap map)
    {
    	super(encoder);
    	
        if (!encoder.isRegistered())
            throw new IllegalStateException("Make sure you add the FlagEncoder " + encoder + " to an EncodingManager before using it elsewhere");

        maxSpeed = encoder.getMaxSpeed() / SPEED_CONV;
    }

    public TrafficAvoidWeighting( FlagEncoder encoder )
    {
        this(encoder, new PMap(0));
    }


    public TrafficAvoidWeighting(Weighting defultWeighting, FlagEncoder encoder, HashMap<Integer, AvoidEdgeInfo> forbiddenEdges)
    {
        this(encoder, new PMap(0));
		this.forbiddenEdges = forbiddenEdges;     
    }

    
    @Override
    public double getMinWeight( double distance )
    {
        return distance / maxSpeed;
    }

    @Override
    public double calcWeight( EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId )
    {
        double normal_speed = reverse ? getFlagEncoder().getReverseSpeed(edge.getFlags()) : getFlagEncoder().getSpeed(edge.getFlags());
        if (normal_speed == 0)
            return Double.POSITIVE_INFINITY;

    
	    AvoidEdgeInfo ei = forbiddenEdges.get(edge.getEdge());
		if (ei!= null){

			short[] codes = ei.getCodes();
			TrafficEventInfo tec = null;
			double givenSpeed = Double.MAX_VALUE;
			double speedFactor = 1;
			double givenDelay = -1;
			for (int i = 0; i < codes.length; i++) {
				int code = codes[i];
				tec = TmcEventCodesTable.getEventInfo(code);
				
				if ((tec.getTmcMode() == TmcMode.HEAVY_VEHICLE) && isCarFlagEncoder(flagEncoder))
					continue; 				
				
				if (tec!=null) {
					if (tec.isDelay()) {
                        // use the max delay in the routing 
						givenDelay = Math.max(givenDelay, tec.getDelay());
					}  

					if(tec.getSpeedFactor()>1){

						givenSpeed = Math.min(givenSpeed, tec.getSpeedFactor());

					} else {
   
						speedFactor = Math.min(speedFactor, tec.getSpeedFactor()); 

					}

				} // end for tec null 
			} // end for codes
			

		
			if (givenDelay > 0 ){
				
	            return givenDelay * 60 + calcTravelTimeInSec(edge.getDistance(), normal_speed); 			
	       
			} else if (givenSpeed < Double.MAX_VALUE){
			
				return calcTravelTimeInSec(edge.getDistance(), givenSpeed); 		
					
			} else if (speedFactor <= 1){
				
				return calcTravelTimeInSec(edge.getDistance(), speedFactor * normal_speed); 	

			} else {
				
				 System.err.println("traffic weighting method didn't give the weight");
				 throw new IllegalStateException("edge " + EdgeIteratorStateHelper.getOriginalEdge(edge) +
						   "has no considered event codes " + ei.getCodesAsString());
			}
		}
		
		// if AovidFeatureInfo is null
		double weight  = calcTravelTimeInSec(edge.getDistance(), normal_speed);
		return weight; 
		
    }

    private boolean isCarFlagEncoder(FlagEncoder encoder){
		return encoder instanceof ExGhORSCarFlagEncoder || encoder instanceof CarFlagEncoder;
	}
    
    private double calcTravelTimeInSec (double distance, double speed) {
    	return distance *3600 / (1000 * speed);
    }

	@Override
	public String getName() {
		return "traffic_avoiding_weighting";
	}
}