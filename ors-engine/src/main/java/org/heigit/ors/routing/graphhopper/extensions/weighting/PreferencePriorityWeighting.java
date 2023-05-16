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

import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.FlagEncoder;
import org.heigit.ors.routing.graphhopper.extensions.util.PriorityCode;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.TurnCostProvider;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderKeys;

import static com.graphhopper.routing.util.EncodingManager.getKey;

public class PreferencePriorityWeighting extends FastestWeighting {
	private static final double THRESHOLD_VERY_BAD = PriorityCode.AVOID_IF_POSSIBLE.getValue() / (double)PriorityCode.BEST.getValue();
	private static final double THRESHOLD_REACH_DEST = PriorityCode.REACH_DEST.getValue() / (double)PriorityCode.BEST.getValue();
	private static final double THRESHOLD_PREFER = PriorityCode.PREFER.getValue() / (double)PriorityCode.BEST.getValue();
	private static final double THRESHOLD_VERY_NICE = PriorityCode.VERY_NICE.getValue() / (double)PriorityCode.BEST.getValue();
	private final DecimalEncodedValue priorityEncoder;

	public PreferencePriorityWeighting(FlagEncoder encoder, PMap map) {
		super(encoder, map);
		priorityEncoder = encoder.getDecimalEncodedValue(getKey(encoder, FlagEncoderKeys.PRIORITY_KEY));
	}

	public PreferencePriorityWeighting(FlagEncoder encoder, PMap map, TurnCostProvider tcp) {
		super(encoder, map, tcp);
		priorityEncoder = encoder.getDecimalEncodedValue(getKey(encoder, FlagEncoderKeys.PRIORITY_KEY));
	}
	@Override
	public double calcEdgeWeight( EdgeIteratorState edgeState, boolean reverse) {
		double weight = super.calcEdgeWeight(edgeState, reverse);
		if (Double.isInfinite(weight))
			weight = 0.0;

		double priority = priorityEncoder.getDecimal(reverse, edgeState.getFlags());

		if (priority <= THRESHOLD_REACH_DEST)
			priority /= 1.5;
		else if (priority <= THRESHOLD_VERY_BAD)
			priority /= 1.25;
		else if (priority == THRESHOLD_PREFER)
			priority *= 1.5;
		else if (priority >= THRESHOLD_VERY_NICE)
			priority *= 2.2;

		return weight / (0.5 + priority);
	}

	@Override
	public String getName() {
		return "recommended";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final PreferencePriorityWeighting other = (PreferencePriorityWeighting) obj;
		return toString().equals(other.toString());
	}

	@Override
	public int hashCode() {
		return ("PreferencePriorityWeighting" + this).hashCode();
	}
}
