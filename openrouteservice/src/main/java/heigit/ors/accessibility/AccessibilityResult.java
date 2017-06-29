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
package heigit.ors.accessibility;

import java.util.List;

import heigit.ors.locations.LocationsResult;
import heigit.ors.routing.RouteResult;

public class AccessibilityResult {
    private List<RouteResult> _routes;
    private List<LocationsResult> _locations;
    
    public AccessibilityResult()
    {
    	
    }
    
    public void setRoutes(List<RouteResult> routes)
    {
    	_routes = routes;
    }
    
    public List<RouteResult> getRoutes()
    {
    	return _routes;
    }

	public List<LocationsResult> getLocations() {
		return _locations;
	}

	public void setLocations(List<LocationsResult> locations) {
		_locations = locations;
	}
}
