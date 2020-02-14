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

import com.graphhopper.routing.profiles.IntEncodedValue;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.CarFlagEncoder;

public class StreetCrossingWeighting extends FastestWeighting {

	protected final IntEncodedValue trafficLightCountEnc;
	protected final IntEncodedValue crossingCountEnc;

    public StreetCrossingWeighting(FlagEncoder encoder, PMap map) {
        super(encoder, map);
        if(encoder instanceof CarFlagEncoder) {
			trafficLightCountEnc = ((CarFlagEncoder) encoder).getTrafficLightCountEnc();
			crossingCountEnc = ((CarFlagEncoder) encoder).getCrossingCountEnc();
		}else{
			trafficLightCountEnc = null;
			crossingCountEnc = null;
		}
    }

	@Override
	public long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
    	long time = super.calcMillis(edgeState, reverse, prevOrNextEdgeId);

    	if(trafficLightCountEnc != null && crossingCountEnc != null) {
			int tLights = edgeState.get(trafficLightCountEnc);
			int crossings = edgeState.get(crossingCountEnc);
			// 30sec penalty for a traffic light
			// 2sec penalty for a pedestrian crossing
			return time + tLights * 30000 + crossings * 2000;
		}else{
    		return time;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final StreetCrossingWeighting other = (StreetCrossingWeighting) obj;
		return toString().equals(other.toString());
	}

	@Override
	public int hashCode() {
		return ("StreetCrossingWeighting" + toString()).hashCode();
	}

	@Override
	public String getName() {
		return "fastestwp";
	}
}