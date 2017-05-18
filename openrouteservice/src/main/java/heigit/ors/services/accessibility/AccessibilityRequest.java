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
package heigit.ors.services.accessibility;

import heigit.ors.common.TravelRangeType;
import heigit.ors.locations.LocationsRequest;
import heigit.ors.routing.RouteSearchParameters;
import heigit.ors.services.ServiceRequest;

public class AccessibilityRequest extends ServiceRequest
{
	private LocationsRequest _locationsRequest;
	private String _locationType = "start"; // either start or destination
	private RouteSearchParameters _routeParameters;   
	private double _range;
	private TravelRangeType _rangeType = TravelRangeType.Time;
	private int _limit = 5;
	
	public AccessibilityRequest()
	{
		_locationsRequest = new LocationsRequest();
	}
	
	public LocationsRequest getLocationsRequest()
	{
		return _locationsRequest;
	}
	
	public void setLocationsRequest(LocationsRequest req)
	{
		_locationsRequest = req;
	}
	
	public int getLimit() {
		return _limit;
	}

	public void setLimit(int limit) {
		_limit = Math.min(limit, AccessibilityServiceSettings.getResponseLimit());
		_locationsRequest.setLimit(_limit);
	}
}
