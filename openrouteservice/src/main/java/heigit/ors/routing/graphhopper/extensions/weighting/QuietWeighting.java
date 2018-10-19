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

import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.NoiseIndexGraphStorage;

public class QuietWeighting extends FastestWeighting {
    private NoiseIndexGraphStorage _gsNoiseIndex;
    private byte[] _buffer;
    private double _weightingFactor = 1;

    public QuietWeighting(FlagEncoder encoder, PMap map, GraphStorage graphStorage) {
        super(encoder, map);
        _buffer = new byte[1];
        _gsNoiseIndex = GraphStorageUtils.getGraphExtension(graphStorage, NoiseIndexGraphStorage.class);
        _weightingFactor = map.getDouble("factor", 1);
    }

    private double calcNoiseWeightFactor(int level) {
    	if ( level == 0)
    		return 1;
        else if ( level <=1 )
        	return 1 + _weightingFactor * 10;
        else if ( level <=2 )
        	return 1 + _weightingFactor * _weightingFactor * 200;  // drop factor for noise level 2 and 3 dramatically, but still larger then the factor for noise level 1 
        else if (level <=3 )
        	return 1 + _weightingFactor * _weightingFactor * 400;
        else
        	throw new AssertionError("The noise level "+  level + " is not supported!");
    }

    @Override
    public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
        if (_gsNoiseIndex != null) {
            int noiseLevel = _gsNoiseIndex.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edgeState), _buffer);
            return calcNoiseWeightFactor(noiseLevel);
        }

        return 1.0;
    }
}