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

import com.graphhopper.routing.weighting.AbstractAdjustedWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;

import java.util.Collection;

// TODO (cleanup): The name is misleading as the class does not only
//                 perform addition. Rename into SoftWeighting
public class AdditionWeighting extends AbstractAdjustedWeighting {
	private final Weighting[] weightings;

	/*
	 * @deprecated This constructor reveals too much of the implementation
	 * details. Use {@link AdditionWeighting(Collection<Weighting> weightings, Weighting superWeighting)}
	 */
	@Deprecated
    public AdditionWeighting(Weighting[] weightings, Weighting superWeighting) {
        super(superWeighting);
        this.weightings = weightings.clone();
    }

	public AdditionWeighting(Collection<Weighting> weightings, Weighting superWeighting) {
		super(superWeighting);
		this.weightings = weightings.toArray(new Weighting[0]);
	}

    @Override
    public double calcEdgeWeight(EdgeIteratorState edgeState, boolean reverse, long edgeEnterTime) {
        double sumOfWeights = 0;
		for (Weighting weighting:weightings) {
			sumOfWeights += weighting.calcEdgeWeight(edgeState, reverse);
		}
    	return superWeighting.calcEdgeWeight(edgeState, reverse, edgeEnterTime) * sumOfWeights;
    }

	@Override
	public String getName() {
		return "addition";
	}

	@Override
	public int hashCode() {
		return ("AddWeighting" + this).hashCode();
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
