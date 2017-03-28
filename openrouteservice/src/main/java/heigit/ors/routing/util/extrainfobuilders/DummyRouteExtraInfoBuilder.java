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

public class DummyRouteExtraInfoBuilder extends RouteExtraInfoBuilder {
	private int _prevIndex = 0;
	
    public DummyRouteExtraInfoBuilder(RouteExtraInfo extraInfo) {
		super(extraInfo);
	}

	public void addSegment(double value, int valueIndex, PointList geom, double dist, boolean lastEdge)
    {
		int nPoints = geom.getSize() - 1;

		RouteSegmentItem item = new RouteSegmentItem(_prevIndex, _prevIndex + nPoints, 0, dist);
		_extraInfo.add(item);

		_prevIndex += nPoints;
    }
	
	public void finish()
	{
		
	}
}
