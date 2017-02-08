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

import java.util.ArrayList;
import java.util.List;

import com.graphhopper.routing.util.RouteSplit;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.PointList;

import heigit.ors.routing.RouteExtraInfo;

public class SteepnessExtraInfoBuilder extends RouteExtraInfoBuilder {
	//private List<RouteSplit> _splits;
	private DistanceCalc _distCalc;
	private double _prevElevation;
	private double _splitLength;
	
    public SteepnessExtraInfoBuilder(RouteExtraInfo extraInfo) {
		super(extraInfo);
		
	//	_splits = new ArrayList<RouteSplit>();
		_distCalc = new DistanceCalcEarth();
	}

	public void addSegment(double value, int valueIndex, PointList geom, double dist, boolean lastEdge)
    {
		int nPoints = geom.getSize() - 1;
		
		
		_splitLength = dist;
		
		/*if ((_prevValue != Double.MAX_VALUE && value != _prevValue) || (lastEdge))
		{
			RouteSegmentItem item = null;
			if (lastEdge)
				item = new RouteSegmentItem(_prevIndex, _prevIndex + _segmentLength + nPoints, valueIndex, _segmentDist + dist);
			else
			{
				item = new RouteSegmentItem(_prevIndex, _prevIndex + _segmentLength, _prevValueIndex, _segmentDist);
				_prevIndex +=_segmentLength;
				_segmentDist = dist;
				_segmentLength = nPoints;
			}
			
			_extraInfo.add(item);
		}
		else
		{
			_segmentLength += nPoints;
			_segmentDist += dist;
		}

		_prevValue = value;
		_prevValueIndex = valueIndex;*/
    }
	
	public void finish()
	{
		/*for(RouteSplit split : _splits)
		{
			RouteSegmentItem item = new RouteSegmentItem(_prevIndex, _prevIndex + _segmentLength + nPoints, valueIndex, _segmentDist + dist); 
		}*/
	}
}
