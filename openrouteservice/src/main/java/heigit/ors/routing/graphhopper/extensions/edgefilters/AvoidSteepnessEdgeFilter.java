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
package heigit.ors.routing.graphhopper.extensions.edgefilters;

import heigit.ors.routing.graphhopper.extensions.storages.*;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;

public class AvoidSteepnessEdgeFilter implements EdgeFilter {

	private final boolean in;
	private final boolean out;
	protected final FlagEncoder encoder;
	private byte[] buffer;
    private double maximumSteepness;
    private HillIndexGraphStorage gsHillIndex;
    
	public AvoidSteepnessEdgeFilter(FlagEncoder encoder, GraphStorage graphStorage, double maxSteepness) {
		this(encoder, true, true, graphStorage, maxSteepness);
	}

	public AvoidSteepnessEdgeFilter(FlagEncoder encoder, boolean in, boolean out, GraphStorage graphStorage, double maxSteepness) {
		this.in = in;
		this.out = out;

		this.encoder = encoder;
		this.maximumSteepness = maxSteepness;
        this.buffer = new byte[1];
        
        gsHillIndex = GraphStorageUtils.getGraphExtension(graphStorage, HillIndexGraphStorage.class);
	}

	@Override
	public final boolean accept(EdgeIteratorState iter) {

		if (out && iter.isForward(encoder) || in && iter.isBackward(encoder)) {
			
			if (gsHillIndex != null)
	    	{
	    		boolean revert = iter.getBaseNode() < iter.getAdjNode();
	    		int hillIndex = gsHillIndex.getEdgeValue(iter.getOriginalEdge(), revert, buffer);
	    		
	    		if (hillIndex > maximumSteepness)
	    			return false;
	    	}
			
			return true;
		}
		
		return false;
	}

	@Override
	public String toString() {
		return "AVOIDSTEEPNESS|" + encoder;
	}
}
