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

import com.graphhopper.routing.querygraph.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.HillIndexGraphStorage;

public class SteepnessDifficultyWeighting extends FastestWeighting {
    
	private final HillIndexGraphStorage gsHillIndex;
	private final byte[] buffer;
	private double[] difficultyWeights;

    private static final double[][] BIKE_DIFFICULTY_MATRIX = { // [4][20]
        {0.5, 0.5, 0.5, 0.7, 0.9, 1.5, 3, 3.5, 4, 5, 11, 11.5, 12, 12.5, 13, 13.5, 14, 14.5, 15, 15.5},
        {0.7, 0.6, 0.6, 0.5, 0.5, 0.8, 1.0, 2, 3, 4, 5, 11.5, 12, 12.5, 13, 13.5, 14, 14.5, 15, 15.5},
        {1.6, 1.6, 1.5, 1.5, 0.7, 0.5, 0.5, 0.5, 1, 2, 2.5, 2.5, 3, 4, 5, 7.5, 7.6, 7.7, 7.8, 7.9},
        {1.6, 1.6, 1.5, 1.5, 0.9, 0.7, 0.5, 0.5, 0.6, 0.7, 0.9, 1.2, 2, 3, 5, 6, 7.7, 7.8, 7.9, 8.0}
      };

    public SteepnessDifficultyWeighting(FlagEncoder encoder, PMap map, GraphHopperStorage graphStorage) {
        super(encoder, map);
        buffer = new byte[1];
	    int difficultyLevel = map.getInt("level", -1);
        gsHillIndex = GraphStorageUtils.getGraphExtension(graphStorage, HillIndexGraphStorage.class);
        // TODO: Check for upper bound of difficultyLevel. What is the right behavior here?
		// TODO: Ignoring as for negative values, or throwing a dedicated exception?
        if (gsHillIndex != null && difficultyLevel >= 0) {
				difficultyWeights = BIKE_DIFFICULTY_MATRIX[difficultyLevel];
        }
    }
    
    @Override
    public double calcEdgeWeight(EdgeIteratorState edgeState, boolean reverse) {
    	if (gsHillIndex != null) {
    		boolean revert = edgeState.getBaseNode() < edgeState.getAdjNode();
    		int hillIndex = gsHillIndex.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edgeState), revert, buffer);

    		if (difficultyWeights != null) {
				// TODO: Clarify whether hillIndex should be checked for out of bounds.
    			return difficultyWeights[hillIndex];
			}
    	}
   		return 1.0;
    }

	@Override
	public boolean equals(Object obj) {
    	// TODO: Clarify whether equals should depend on difficulty level.
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SteepnessDifficultyWeighting other = (SteepnessDifficultyWeighting) obj;
		return toString().equals(other.toString());
	}

	@Override
	public int hashCode() {
		// TODO: Clarify whether hashCode should depend on difficulty level.
		return ("SteepnessDifficultyWeighting" + this).hashCode();
	}
}
