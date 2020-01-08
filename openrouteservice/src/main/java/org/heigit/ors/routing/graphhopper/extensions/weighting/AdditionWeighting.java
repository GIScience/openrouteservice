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
import com.graphhopper.routing.weighting.AbstractWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;

public class AdditionWeighting extends AbstractWeighting {
	private Weighting superWeighting;
    private WeightCalc weightCalc;

    public AdditionWeighting(Weighting[] weightings, Weighting superWeighting, FlagEncoder encoder) {
        super(encoder);
        this.superWeighting = superWeighting;
        
        int count = weightings.length;
        if (count == 1)
           weightCalc = new OneWeightCalc(weightings);
        else if (count == 2)
            weightCalc = new TwoWeightCalc(weightings);
        else if (count == 3)
            weightCalc = new ThreeWeightCalc(weightings);
        else if (count == 4)
            weightCalc = new FourWeightCalc(weightings);
        else if (count == 5)
            weightCalc = new FiveWeightCalc(weightings);
    }
    
    public interface WeightCalc  {
		 double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId);
		 long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId);
    }
    
    public static class OneWeightCalc implements WeightCalc
    {
    	private Weighting weighting;
    	
    	public OneWeightCalc(Weighting[] weightings)
    	{
    		weighting = weightings[0];
    	}
    	
    	public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    	{
    		return weighting.calcWeight(edgeState, reverse, prevOrNextEdgeId);
    	}
    	
    	public long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    	{
    		return weighting.calcMillis(edgeState, reverse, prevOrNextEdgeId);
    	}
    }
    
    public class TwoWeightCalc extends OneWeightCalc
    {
    	private Weighting weighting;
    	
    	public TwoWeightCalc(Weighting[] weightings)
    	{
    		super(weightings);
    		weighting = weightings[1];
    	}

		@Override
    	public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    	{
    		return super.calcWeight(edgeState, reverse, prevOrNextEdgeId) + weighting.calcWeight(edgeState, reverse, prevOrNextEdgeId);
    	}

    	@Override
    	public long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    	{
    		return super.calcMillis(edgeState, reverse, prevOrNextEdgeId) + weighting.calcMillis(edgeState, reverse, prevOrNextEdgeId);
    	}
    }
    
    public class ThreeWeightCalc extends TwoWeightCalc
    {
    	private Weighting weighting;
    	
    	public ThreeWeightCalc(Weighting[] weightings)
    	{
    		super(weightings);
    		weighting = weightings[2];
    	}

		@Override
    	public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    	{
    		return super.calcWeight(edgeState, reverse, prevOrNextEdgeId) + weighting.calcWeight(edgeState, reverse, prevOrNextEdgeId);
    	}

		@Override
    	public long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    	{
    		return super.calcMillis(edgeState, reverse, prevOrNextEdgeId) + weighting.calcMillis(edgeState, reverse, prevOrNextEdgeId);
    	}
    }
    
    public class FourWeightCalc extends ThreeWeightCalc
    {
    	private Weighting weighting;
    	
    	public FourWeightCalc(Weighting[] weightings)
    	{
    		super(weightings);
    		weighting = weightings[3];
    	}

		@Override
    	public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    	{
    		return super.calcWeight(edgeState, reverse, prevOrNextEdgeId) + weighting.calcWeight(edgeState, reverse, prevOrNextEdgeId);
    	}

		@Override
    	public long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    	{
    		return super.calcMillis(edgeState, reverse, prevOrNextEdgeId) + weighting.calcMillis(edgeState, reverse, prevOrNextEdgeId);
    	}
    }
    
    public class FiveWeightCalc extends FourWeightCalc
    {
    	private Weighting weighting;
    	
    	public FiveWeightCalc(Weighting[] weightings)
    	{
    		super(weightings);
    		weighting = weightings[4];
    	}

		@Override
    	public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    	{
    		return super.calcWeight(edgeState, reverse, prevOrNextEdgeId) + weighting.calcWeight(edgeState, reverse, prevOrNextEdgeId);
    	}

		@Override
    	public long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    	{
    		return super.calcMillis(edgeState, reverse, prevOrNextEdgeId) + weighting.calcMillis(edgeState, reverse, prevOrNextEdgeId);
    	}
    }

    @Override
    public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
        
    	return superWeighting.calcWeight(edgeState, reverse, prevOrNextEdgeId) * weightCalc.calcWeight(edgeState, reverse, prevOrNextEdgeId);
    }

	@Override
	public double getMinWeight(double distance) {
		return 0;
	}
	
	@Override
	public long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
		return superWeighting.calcMillis(edgeState, reverse, prevOrNextEdgeId) + weightCalc.calcMillis(edgeState, reverse, prevOrNextEdgeId);
	}

	@Override
	public String getName() {
		return "addition";
	}

	@Override
	public int hashCode() {
		return ("AddWeighting" + toString()).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final AdditionWeighting other = (AdditionWeighting) obj;
		return toString().equals(other.toString());
	}
}
