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