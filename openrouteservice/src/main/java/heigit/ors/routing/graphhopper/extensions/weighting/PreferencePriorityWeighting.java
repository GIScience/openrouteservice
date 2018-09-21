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

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

public class PreferencePriorityWeighting extends FastestWeighting
{
	private static final Double THRESHOLD_AVOID_IF_POSSIBLE = (double) (PriorityCode.AVOID_IF_POSSIBLE.getValue() / (double)PriorityCode.BEST
			.getValue());
	
	private static final Double THRESHOLD_REACH_DEST = (double) (PriorityCode.REACH_DEST.getValue() / (double)PriorityCode.BEST
			.getValue());
	
	private Double THRESHOLD_PREFER = (double) (PriorityCode.PREFER.getValue() / (double)PriorityCode.BEST
			.getValue());

	private Double THRESHOLD_VERY_NICE = (double) (PriorityCode.VERY_NICE.getValue() / (double)PriorityCode.BEST
			.getValue());
	
    /**
     * For now used only in BikeCommonFlagEncoder and MotorcycleFlagEncoder
     */
    public static final int KEY = 101;

    public PreferencePriorityWeighting(FlagEncoder encoder, PMap map)
    {
        super(encoder, map);
    }

    @Override
    public double calcWeight( EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    {
    	double weight = super.calcWeight(edgeState, reverse, prevOrNextEdgeId);
		if (Double.isInfinite(weight))
			weight = 0.0; 

    	double priority = getFlagEncoder().getDouble(edgeState.getFlags(), KEY);

		if (priority <= THRESHOLD_REACH_DEST)
			priority /= 1.5;
		else if (priority <= THRESHOLD_AVOID_IF_POSSIBLE)
			priority /= 1.25;
		else if (priority == THRESHOLD_PREFER)
			priority *= 1.5;
		else if (priority >= THRESHOLD_VERY_NICE)
			priority *= 2.2;
		
		 return weight / (0.5 + priority);
    }
    
    @Override
    public String getName() {
        return "priority";
    } 
}
