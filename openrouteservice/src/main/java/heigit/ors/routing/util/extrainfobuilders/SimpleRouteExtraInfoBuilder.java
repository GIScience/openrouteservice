/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2016
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.routing.util.extrainfobuilders;

import com.graphhopper.util.PointList;

import heigit.ors.routing.RouteExtraInfo;
import heigit.ors.routing.RouteSegmentItem;

public class SimpleRouteExtraInfoBuilder extends RouteExtraInfoBuilder {
	private int _prevIndex = 0;
	private int _segmentLength = 0;
	private int _prevValueIndex = -1;
	private double _prevValue = Double.MAX_VALUE;
	private double _segmentDist = 0;
	
    public SimpleRouteExtraInfoBuilder(RouteExtraInfo extraInfo) {
		super(extraInfo);
	}

	public void addSegment(double value, int valueIndex, PointList geom, double dist, boolean lastEdge)
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
