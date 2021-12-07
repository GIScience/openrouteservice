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
import com.graphhopper.util.EdgeIteratorState;

public class DistanceWeighting extends AbstractWeighting {
    public DistanceWeighting(FlagEncoder encoder) {
        super(encoder);
    }

    @Override
    public double calcEdgeWeight(EdgeIteratorState edge, boolean reverse) {
        double speed = flagEncoder.getAverageSpeedEnc().getDecimal(reverse, edge.getFlags());
        if (speed == 0)
            return Double.POSITIVE_INFINITY;

       return edge.getDistance();
    }

	@Override
	public double getMinWeight(double distance) {
		return 0;
	}

	@Override
	public String getName() {
		return "distance";
	}

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final DistanceWeighting other = (DistanceWeighting) obj;
        return toString().equals(other.toString());
    }

    @Override
    public int hashCode() {
        return ("DistanceWeighting" + this).hashCode();
    }
}
