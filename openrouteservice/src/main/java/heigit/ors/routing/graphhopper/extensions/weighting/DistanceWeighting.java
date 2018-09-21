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
import com.graphhopper.routing.weighting.AbstractWeighting;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

public class DistanceWeighting extends AbstractWeighting
{
    protected final FlagEncoder flagEncoder;

    public DistanceWeighting( FlagEncoder encoder, PMap pMap )
    {
        super(encoder);

        this.flagEncoder = encoder;
    }

    public DistanceWeighting( FlagEncoder encoder )
    {
        this(encoder, new PMap(0));
    }

    public DistanceWeighting(double userMaxSpeed, FlagEncoder encoder)
    {
    	this(encoder);
    }
    
    @Override
    public double calcWeight(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId)
    {
        double speed = reverse ? flagEncoder.getReverseSpeed(edge.getFlags()) : flagEncoder.getSpeed(edge.getFlags());
        if (speed == 0)
            return Double.POSITIVE_INFINITY;

       return edge.getDistance();
    }

	@Override
	public double getMinWeight(double distance) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		return "distance";
	}
}
