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
package heigit.ors.routing.pathprocessors;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.PathProcessor;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;
import heigit.ors.routing.util.ElevationSmoother;

public class ElevationSmoothPathProcessor extends PathProcessor {
	public ElevationSmoothPathProcessor()
	{

	}

	@Override
	public void init(FlagEncoder enc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSegmentIndex(int index, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processEdge(EdgeIteratorState edge, boolean isLastEdge, PointList geom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

	@Override
	public PointList processPoints(PointList points) {
		return ElevationSmoother.smooth(points);
	}
}
