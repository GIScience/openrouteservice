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
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.routing.weighting.AbstractWeighting;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

public class AdditionWeighting extends AbstractWeighting {
	private Weighting _superWeighting;
    private WeightCalc _weightCalc;

    public AdditionWeighting(Weighting[] weightings, Weighting superWeighting, FlagEncoder encoder, PMap map, GraphStorage graphStorage) {
        super(encoder);
        _superWeighting = superWeighting;
        
        int count = weightings.length;
        if (count == 1)
           _weightCalc = new OneWeightCalc(weightings);
        else if (count == 2)
            _weightCalc = new TwoWeightCalc(weightings);
        else if (count == 3)
            _weightCalc = new ThreeWeightCalc(weightings);
        else if (count == 4)
            _weightCalc = new FourWeightCalc(weightings);
        else if (count == 5)
            _weightCalc = new FiveWeightCalc(weightings);
    }
    
    public abstract class WeightCalc
    {
    	public abstract double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId);
    	
    	public abstract long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId);
    }
    
    public class OneWeightCalc extends WeightCalc
    {
    	private Weighting _weighting;
    	
    	public OneWeightCalc(Weighting[] weightings)
    	{
    		_weighting = weightings[0];
    	}
    	
    	public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    	{
    		return _weighting.calcWeight(edgeState, reverse, prevOrNextEdgeId);
    	}
    	
    	public long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    	{
    		return _weighting.calcMillis(edgeState, reverse, prevOrNextEdgeId);
    	}
    }
    
    public class TwoWeightCalc extends OneWeightCalc
    {
    	private Weighting _weighting;
    	
    	public TwoWeightCalc(Weighting[] weightings)
    	{
    		super(weightings);
    		_weighting = weightings[1];
    	}
    	
    	public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    	{
    		return super.calcWeight(edgeState, reverse, prevOrNextEdgeId) + _weighting.calcWeight(edgeState, reverse, prevOrNextEdgeId);
    	}
    	
    	public long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    	{
    		return super.calcMillis(edgeState, reverse, prevOrNextEdgeId) + _weighting.calcMillis(edgeState, reverse, prevOrNextEdgeId);
    	}
    }
    
    public class ThreeWeightCalc extends TwoWeightCalc
    {
    	private Weighting _weighting;
    	
    	public ThreeWeightCalc(Weighting[] weightings)
    	{
    		super(weightings);
    		_weighting = weightings[2];
    	}
    	
    	public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    	{
    		return super.calcWeight(edgeState, reverse, prevOrNextEdgeId) + _weighting.calcWeight(edgeState, reverse, prevOrNextEdgeId);
    	}
    	
    	public long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    	{
    		return super.calcMillis(edgeState, reverse, prevOrNextEdgeId) + _weighting.calcMillis(edgeState, reverse, prevOrNextEdgeId);
    	}
    }
    
    public class FourWeightCalc extends ThreeWeightCalc
    {
    	private Weighting _weighting;
    	
    	public FourWeightCalc(Weighting[] weightings)
    	{
    		super(weightings);
    		_weighting = weightings[3];
    	}
    	
    	public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    	{
    		return super.calcWeight(edgeState, reverse, prevOrNextEdgeId) + _weighting.calcWeight(edgeState, reverse, prevOrNextEdgeId);
    	}
    	
    	public long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    	{
    		return super.calcMillis(edgeState, reverse, prevOrNextEdgeId) + _weighting.calcMillis(edgeState, reverse, prevOrNextEdgeId);
    	}
    }
    
    public class FiveWeightCalc extends FourWeightCalc
    {
    	private Weighting _weighting;
    	
    	public FiveWeightCalc(Weighting[] weightings)
    	{
    		super(weightings);
    		_weighting = weightings[4];
    	}
    	
    	public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    	{
    		return super.calcWeight(edgeState, reverse, prevOrNextEdgeId) + _weighting.calcWeight(edgeState, reverse, prevOrNextEdgeId);
    	}
    	
    	public long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    	{
    		return super.calcMillis(edgeState, reverse, prevOrNextEdgeId) + _weighting.calcMillis(edgeState, reverse, prevOrNextEdgeId);
    	}
    }

    @Override
    public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
        
    	return _superWeighting.calcWeight(edgeState, reverse, prevOrNextEdgeId) * _weightCalc.calcWeight(edgeState, reverse, prevOrNextEdgeId);
    }

	@Override
	public double getMinWeight(double distance) {
		return 0;
	}
	
	@Override
	public long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
		return _superWeighting.calcMillis(edgeState, reverse, prevOrNextEdgeId) + _weightCalc.calcMillis(edgeState, reverse, prevOrNextEdgeId);
	}

	@Override
	public String getName() {
		return "addition";
	}
}
