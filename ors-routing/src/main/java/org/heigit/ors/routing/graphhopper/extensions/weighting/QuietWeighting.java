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
import org.heigit.ors.routing.graphhopper.extensions.storages.NoiseIndexGraphStorage;

public class QuietWeighting extends FastestWeighting {
    private final NoiseIndexGraphStorage gsNoiseIndex;
    private final byte[] buffer;
    private double weightingFactor = 1;

    public QuietWeighting(FlagEncoder encoder, PMap map, GraphHopperStorage graphStorage) {
        super(encoder, map);
        buffer = new byte[1];
        gsNoiseIndex = GraphStorageUtils.getGraphExtension(graphStorage, NoiseIndexGraphStorage.class);
        weightingFactor = map.getDouble("factor", 1);
    }

    private double calcNoiseWeightFactor(int level) {
    	if ( level == 0)
    		return 1;
        else if ( level <=1 )
        	return 1 + weightingFactor * 10;
        else if ( level <=2 )
        	return 1 + weightingFactor * weightingFactor * 200;  // drop factor for noise level 2 and 3 dramatically, but still larger then the factor for noise level 1
        else if (level <=3 )
        	return 1 + weightingFactor * weightingFactor * 400;
        else
        	throw new AssertionError("The noise level "+  level + " is not supported!");
    }

    @Override
    public double calcEdgeWeight(EdgeIteratorState edgeState, boolean reverse) {
        if (gsNoiseIndex != null) {
            int noiseLevel = gsNoiseIndex.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edgeState), buffer);
            return calcNoiseWeightFactor(noiseLevel);
        }

        return 1.0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final QuietWeighting other = (QuietWeighting) obj;
        return toString().equals(other.toString());
    }

    @Override
    public int hashCode() {
        return ("QuietWeighting" + this).hashCode();
    }
}