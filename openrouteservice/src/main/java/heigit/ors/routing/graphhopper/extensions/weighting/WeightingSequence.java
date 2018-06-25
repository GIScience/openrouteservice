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
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;

import java.util.ArrayList;

public class WeightingSequence implements Weighting {
	private ArrayList<Weighting> weightings;
	private int weightingsCount;
	private Weighting weighting1, weighting2, weighting3;

	public WeightingSequence(ArrayList<Weighting> weightings) {
		this.weightings = weightings;
		this.weightingsCount = weightings.size();
		
		if (weightingsCount == 2)
		{
			weighting1 = weightings.get(0);
			weighting2 = weightings.get(1);
		} else if (weightingsCount == 3)
		{
			weighting1 = weightings.get(0);
			weighting2 = weightings.get(1);
			weighting3 = weightings.get(2);
		}
	}

	public void addWeighting(Weighting w) {
		weightings.add(w);
		weightingsCount++;
	}
	
	@Override
	public double getMinWeight(double distance) {
		
		double minValue = Double.MAX_VALUE;
		
		for (int i = 0; i < weightingsCount; i++) {
			double w = weightings.get(i).getMinWeight(distance);
			if (w < minValue)
				minValue = w;
		}
 
		return minValue;
	}

	@Override
	public String toString() {
		return "WEIGHTINGSEQUENCE";
	}

	@Override
	public double calcWeight(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId ) {
		if (weightingsCount == 2)
		{
			return weighting1.calcWeight(edge, reverse, prevOrNextEdgeId) + weighting2.calcWeight(edge, reverse, prevOrNextEdgeId);
		}
		else if (weightingsCount == 3)
		{
			return weighting1.calcWeight(edge, reverse, prevOrNextEdgeId) + weighting2.calcWeight(edge, reverse, prevOrNextEdgeId) + weighting3.calcWeight(edge, reverse, prevOrNextEdgeId);
		}
		
        double result = 0;
		
		for (int i = 0; i < weightingsCount; i++) {
			result += weightings.get(i).calcWeight(edge, reverse, prevOrNextEdgeId);
		}
 
		return result;
	}

	@Override
	public FlagEncoder getFlagEncoder() {
		return weightings.get(0).getFlagEncoder();
	}

	@Override
	public long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean matches(HintsMap map) {
		// TODO Auto-generated method stub
		return false;
	}
}