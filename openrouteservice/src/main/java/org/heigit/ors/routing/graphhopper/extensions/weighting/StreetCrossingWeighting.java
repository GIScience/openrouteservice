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

	public static GraphStorage staticStorage;
	private StreetCrossingGraphStorage gsStreetCrossIndex;

	private static final double[] PENALTY_FACTOR = {1.2, 1.3, 1.4, 1.5, 1.7, 1.8, 2.0, 2.2, 2.4, 2.6, 2.8, 3.2, 3.5, 3.7, 3.9, 4.2};

    public StreetCrossingWeighting(FlagEncoder encoder, PMap map, GraphStorage graphStorage) {
        super(encoder, map);
        if(graphStorage != null) {
			gsStreetCrossIndex = GraphStorageUtils.getGraphExtension(graphStorage, StreetCrossingGraphStorage.class);
		}
    }

    @Override
    public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
		double fastestWeight = super.calcWeight(edgeState, reverse, prevOrNextEdgeId);
		if(gsStreetCrossIndex == null && staticStorage != null){
			gsStreetCrossIndex = GraphStorageUtils.getGraphExtension(staticStorage, StreetCrossingGraphStorage.class);
		}
		if (gsStreetCrossIndex != null) {
    		int[] lightsAndCrossings = gsStreetCrossIndex.getTrafficLightsAndCrossings(edgeState.getEdge());
    		int sum = lightsAndCrossings[0] + lightsAndCrossings[1];
    		if(sum > 0) {
				if (sum < 15) {
					return fastestWeight / PENALTY_FACTOR[sum];
				} else {
					return fastestWeight / 10;
				}
			}
    	}
    	return fastestWeight;
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
}