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
package heigit.ors.routing.util.extrainfobuilders;

import com.graphhopper.util.PointList;

import heigit.ors.routing.RouteExtraInfo;
import heigit.ors.routing.RouteSegmentItem;

public class SimpleRouteExtraInfoBuilder extends RouteExtraInfoBuilder {
	private int _prevIndex = 0;
	private int _segmentLength = 0;
	private long _prevValueIndex = -1;
	private double _prevValue = Double.MAX_VALUE;
	private double _segmentDist = 0;
	
    public SimpleRouteExtraInfoBuilder(RouteExtraInfo extraInfo) {
		super(extraInfo);
	}

	public void addSegment(double value, long valueIndex, PointList geom, double dist, boolean lastEdge)
    {
		int nPoints = geom.getSize() - 1;

		if ((_prevValue != Double.MAX_VALUE && value != _prevValue) || (lastEdge))
		{
			RouteSegmentItem item = null;
			if (lastEdge)
			{
				if (value != _prevValue)
				{
					if (_prevValueIndex != -1)
					{
						item = new RouteSegmentItem(_prevIndex, _prevIndex + _segmentLength, _prevValueIndex, _segmentDist);
						_extraInfo.add(item);
					}
					
					item = new RouteSegmentItem(_prevIndex + _segmentLength, _prevIndex + _segmentLength + nPoints, valueIndex, dist);
					_extraInfo.add(item);
				}
				else
				{
					item = new RouteSegmentItem(_prevIndex, _prevIndex + _segmentLength + nPoints, valueIndex, _segmentDist + dist);
					_extraInfo.add(item);
				}
			}
			else
			{
				item = new RouteSegmentItem(_prevIndex, _prevIndex + _segmentLength, _prevValueIndex, _segmentDist);
				_prevIndex +=_segmentLength;
				_segmentDist = dist;
				_segmentLength = nPoints;
				
				_extraInfo.add(item);
			}
		}
		else
		{
			_segmentLength += nPoints;
			_segmentDist += dist;
		}

		_prevValue = value;
		_prevValueIndex = valueIndex;
    }
	
	public void finish()
	{
		
	}
}
