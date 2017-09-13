/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.optimization;

import heigit.ors.routing.RouteResult;

public class RouteOptimizationResult {
	private RouteResult _routeResult;
	private int[] _waypoints;

	public RouteOptimizationResult()
	{

	}

	public RouteResult getRouteResult() {
		return _routeResult;
	}

	public void setRouteResult(RouteResult routeResult) {
		_routeResult = routeResult;
	}

	public int[] getWayPoints() {
		return _waypoints;
	}

	public void setWayPoints(int[] waypoints) {
		_waypoints = waypoints;
	}
}
