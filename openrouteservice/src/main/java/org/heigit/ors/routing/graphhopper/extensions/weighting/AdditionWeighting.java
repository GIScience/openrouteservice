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
	private Weighting[] weightings;

    public AdditionWeighting(Weighting[] weightings, Weighting superWeighting, FlagEncoder encoder) {
        super(encoder);
        this.superWeighting = superWeighting;
        this.weightings = weightings.clone();
    }
    
    @Override
    public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
        double sumOfWeights = 0;
		for (Weighting w:weightings) {
			sumOfWeights += w.calcWeight(edgeState, reverse, prevOrNextEdgeId);
		}
    	return superWeighting.calcWeight(edgeState, reverse, prevOrNextEdgeId) * sumOfWeights;
    }

	@Override
	public double getMinWeight(double distance) {
		return 0;
	}
	
	@Override
	public long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
    	return superWeighting.calcMillis(edgeState, reverse, prevOrNextEdgeId);
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
