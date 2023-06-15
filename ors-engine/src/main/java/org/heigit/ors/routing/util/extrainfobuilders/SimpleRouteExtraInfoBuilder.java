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
package org.heigit.ors.routing.util.extrainfobuilders;

import com.graphhopper.util.PointList;
import org.heigit.ors.routing.RouteExtraInfo;
import org.heigit.ors.routing.RouteSegmentItem;

public class SimpleRouteExtraInfoBuilder extends RouteExtraInfoBuilder {
	private int prevIndex = 0;
	private int segmentLength = 0;
	private long prevValueIndex = -1;
	private double prevValue = Double.MAX_VALUE;
	private double segmentDist = 0;
	
    public SimpleRouteExtraInfoBuilder(RouteExtraInfo extraInfo) {
		super(extraInfo);
	}

	public void addSegment(double value, long valueIndex, PointList geom, double dist, boolean lastEdge) {
		int nPoints = geom.size() - 1;
		if ((prevValue != Double.MAX_VALUE && value != prevValue) || (lastEdge)) {
			RouteSegmentItem item = null;
			if (lastEdge) {
				if (value != prevValue) {
					if (prevValueIndex != -1) {
						item = new RouteSegmentItem(prevIndex, prevIndex + segmentLength, prevValueIndex, segmentDist);
						extraInfo.add(item);
					}
					item = new RouteSegmentItem(prevIndex + segmentLength, prevIndex + segmentLength + nPoints, valueIndex, dist);
					extraInfo.add(item);
				} else {
					item = new RouteSegmentItem(prevIndex, prevIndex + segmentLength + nPoints, valueIndex, segmentDist + dist);
					extraInfo.add(item);
				}
			} else {
				item = new RouteSegmentItem(prevIndex, prevIndex + segmentLength, prevValueIndex, segmentDist);
				prevIndex += segmentLength;
				segmentDist = dist;
				segmentLength = nPoints;
				extraInfo.add(item);
			}
		} else {
			segmentLength += nPoints;
			segmentDist += dist;
		}
		prevValue = value;
		prevValueIndex = valueIndex;
    }

	public void addSegment(double value, long valueIndex, PointList geom, double dist) {
    	throw new UnsupportedOperationException("SimpleRouteExtraInfoBuilder does not support method addSegment without lastEdge flag.");
	}
	
	public void finish() {
		throw new UnsupportedOperationException("SimpleRouteExtraInfoBuilder does not support method finish.");
	}
}
