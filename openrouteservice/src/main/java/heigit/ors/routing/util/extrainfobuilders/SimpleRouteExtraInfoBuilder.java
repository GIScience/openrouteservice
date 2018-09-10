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
