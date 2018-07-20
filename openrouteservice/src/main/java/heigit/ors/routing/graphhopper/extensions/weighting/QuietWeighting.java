/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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