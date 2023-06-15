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
package org.heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.TurnCostProvider;
import org.heigit.ors.routing.graphhopper.extensions.util.PriorityCode;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderKeys;

/**
 * Special weighting for (motor)bike
 * <p>
 * 
 * @author Peter Karich
 */
public class FastestSafeWeighting extends FastestWeighting {
	private final Double priorityThreshold = PriorityCode.REACH_DEST.getValue() / (double)PriorityCode.BEST.getValue();
	
	public FastestSafeWeighting(FlagEncoder encoder, PMap map, TurnCostProvider turnCostProvider) {
		super(encoder, map, turnCostProvider);
	}

	@Override
	public double calcEdgeWeight(EdgeIteratorState edgeState, boolean reverse, long edgeEnterTime) {
		double weight = super.calcEdgeWeight(edgeState, reverse, edgeEnterTime);
		if (Double.isInfinite(weight))
			return Double.POSITIVE_INFINITY;

		double priority = getFlagEncoder().getDecimalEncodedValue(FlagEncoderKeys.PRIORITY_KEY).getDecimal(reverse, edgeState.getFlags());

		if (priority <= priorityThreshold)
			weight *= 2;

		return weight;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final FastestSafeWeighting other = (FastestSafeWeighting) obj;
		return toString().equals(other.toString());
	}

	@Override
	public int hashCode() {
		return ("FastestSafeWeighting" + this).hashCode();
	}
}
