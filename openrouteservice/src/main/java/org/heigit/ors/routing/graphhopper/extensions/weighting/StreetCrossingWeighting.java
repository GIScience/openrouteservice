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
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.StreetCrossingGraphStorage;

public class StreetCrossingWeighting extends FastestWeighting {

	private StreetCrossingGraphStorage gsStreetCrossIndex = null;
	public static GraphStorage storage = null;

    public StreetCrossingWeighting(FlagEncoder encoder, PMap map) {
        super(encoder, map);
    }

	@Override
	public long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
    	long time = super.calcMillis(edgeState, reverse, prevOrNextEdgeId);

		// MARQ24: HOW the heck we can get rid of this EXTREM ugly storage INIT?!
    	if(gsStreetCrossIndex == null && storage != null){
			gsStreetCrossIndex = GraphStorageUtils.getGraphExtension(storage, StreetCrossingGraphStorage.class);
		}
		if(gsStreetCrossIndex != null){
			int[] lightsAndCrossings = gsStreetCrossIndex.getTrafficLightsAndCrossings(edgeState.getEdge());
			// in order to make debugging easier we just apply the penalty if one of the values
			// is > 0
			if(lightsAndCrossings[0]+lightsAndCrossings[1]>0) {
				// 30sec penalty for a traffic light
				// 2sec penalty for a pedestrian crossing
				return time + lightsAndCrossings[0] * 30000 + lightsAndCrossings[1] * 2000;
			}
		}
		return time;
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