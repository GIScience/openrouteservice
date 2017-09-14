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
	public String getName() {
		return "addition";
	}
}
