/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/

package org.freeopenls.routeservice.mapmatching;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EdgeFilter;

public abstract class AbstractMapMatcher implements MapMatcher {
	protected double mSearchRadius = 50;
	protected EdgeFilter mEdgeFilter;
	protected GraphHopper mGraphHopper;
	
	public void setSearchRadius(double radius)
	{
		mSearchRadius = radius;
	}
	
	public void setEdgeFilter(EdgeFilter edgeFilter)
	{
		mEdgeFilter = edgeFilter;
	}
	
	public void setGraphHopper(GraphHopper gh)
	{
		mGraphHopper = gh;
	}
	
	public RouteSegmentInfo match(double lat0, double lon0, double lat1, double lon1)
	{
		return null;
	}
}
