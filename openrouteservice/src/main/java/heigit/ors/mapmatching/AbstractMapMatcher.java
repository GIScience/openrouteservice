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

package heigit.ors.mapmatching;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EdgeFilter;

public abstract class AbstractMapMatcher implements MapMatcher {
	protected double _searchRadius = 50;
	protected EdgeFilter _edgeFilter;
	protected GraphHopper _graphHopper;
	
	public void setSearchRadius(double radius)
	{
		_searchRadius = radius;
	}
	
	public void setEdgeFilter(EdgeFilter edgeFilter)
	{
		_edgeFilter = edgeFilter;
	}
	
	public void setGraphHopper(GraphHopper gh)
	{
		_graphHopper = gh;
	}
	
	public RouteSegmentInfo match(double lat0, double lon0, double lat1, double lon1)
	{
		return null;
	}
}
