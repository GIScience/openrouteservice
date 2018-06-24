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

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

public class PreferencePriorityWeighting extends FastestWeighting
{
	private static final Double THRESHOLD_AVOID_IF_POSSIBLE = (double) (PriorityCode.AVOID_IF_POSSIBLE.getValue() / (double)PriorityCode.BEST
			.getValue());
	
	private static final Double THRESHOLD_REACH_DEST = (double) (PriorityCode.REACH_DEST.getValue() / (double)PriorityCode.BEST
			.getValue());
	
	private Double THRESHOLD_PREFER = (double) (PriorityCode.PREFER.getValue() / (double)PriorityCode.BEST
			.getValue());

	private Double THRESHOLD_VERY_NICE = (double) (PriorityCode.VERY_NICE.getValue() / (double)PriorityCode.BEST
			.getValue());
	
    /**
     * For now used only in BikeCommonFlagEncoder and MotorcycleFlagEncoder
     */
    public static final int KEY = 101;

    public PreferencePriorityWeighting(FlagEncoder encoder, PMap map)
    {
        super(encoder, map);
    }

    @Override
    public double calcWeight( EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    {
    	double weight = super.calcWeight(edgeState, reverse, prevOrNextEdgeId);
		if (Double.isInfinite(weight))
			weight = 0.0; 

    	double priority = getFlagEncoder().getDouble(edgeState.getFlags(), KEY);

		if (priority <= THRESHOLD_REACH_DEST)
			priority /= 1.5;
		else if (priority <= THRESHOLD_AVOID_IF_POSSIBLE)
			priority /= 1.25;
		else if (priority == THRESHOLD_PREFER)
			priority *= 1.5;
		else if (priority >= THRESHOLD_VERY_NICE)
			priority *= 2.2;
		
		 return weight / (0.5 + priority);
    }
    
    @Override
    public String getName() {
        return "priority";
    } 
}
