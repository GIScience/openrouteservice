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

import com.graphhopper.routing.profiles.DecimalEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

public class OptimizedPriorityWeighting extends FastestWeighting {
	private static final Double THRESHOLD_AVOID_IF_POSSIBLE = PriorityCode.AVOID_IF_POSSIBLE.getValue() / (double)PriorityCode.BEST.getValue();
	private  static final Double THRESHOLD_REACH_DEST = PriorityCode.REACH_DEST.getValue() / (double)PriorityCode.BEST.getValue();
	private final DecimalEncodedValue priorityEncoder;

	public OptimizedPriorityWeighting(FlagEncoder encoder, PMap map) {
		super(encoder, map);
		priorityEncoder = encoder.getDecimalEncodedValue(EncodingManager.getKey(encoder, "priority"));
	}

	@Override
	public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
		double weight = super.calcWeight(edgeState, reverse, prevOrNextEdgeId);
		if (Double.isInfinite(weight))
			return Double.POSITIVE_INFINITY;

		double priority = priorityEncoder.getDecimal(reverse, edgeState.getFlags());

		if (priority <= THRESHOLD_REACH_DEST)
			weight *= 1.25;
		else if (priority <= THRESHOLD_AVOID_IF_POSSIBLE)
			weight *= 2;

		return weight / (0.5 + priority);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final OptimizedPriorityWeighting other = (OptimizedPriorityWeighting) obj;
		return toString().equals(other.toString());
	}

	@Override
	public int hashCode() {
		return ("OptimizedPriorityWeighting" + toString()).hashCode();
	}
}
