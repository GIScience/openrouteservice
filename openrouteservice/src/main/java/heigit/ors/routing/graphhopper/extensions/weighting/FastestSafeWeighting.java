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

import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

/**
 * Special weighting for (motor)bike
 * <p>
 * 
 * @author Peter Karich
 */
public class FastestSafeWeighting extends FastestWeighting {
	private Double THRESHOLD_AVOID_AT_ALL_COSTS = (double) (PriorityCode.AVOID_AT_ALL_COSTS.getValue() / (double)PriorityCode.BEST
			.getValue());
	
	/**
	 * For now used only in BikeCommonFlagEncoder and MotorcycleFlagEncoder
	 */
	public static final int KEY = 101;

	public FastestSafeWeighting(FlagEncoder encoder, PMap map) {
		super(encoder, map);

	}

	@Override
	public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
		double weight = super.calcWeight(edgeState, reverse, prevOrNextEdgeId);
		if (Double.isInfinite(weight))
			return Double.POSITIVE_INFINITY;

		double priority = flagEncoder.getDouble(edgeState.getFlags(), KEY);

		if (priority <= THRESHOLD_AVOID_AT_ALL_COSTS)
			weight *= 2;

		return weight;
	}
}
