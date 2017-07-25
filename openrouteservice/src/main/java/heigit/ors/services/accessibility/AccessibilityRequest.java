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
package heigit.ors.services.accessibility;

import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.common.NamedLocation;
import heigit.ors.common.TravelRangeType;
import heigit.ors.locations.LocationsRequest;
import heigit.ors.services.ServiceRequest;
import heigit.ors.services.routing.RoutingRequest;

public class AccessibilityRequest extends ServiceRequest
{
	private RoutingRequest _routingRequest;
    // Destination points specified either by a user defined locations or by POI search filter
	private LocationsRequest _locationsRequest;
	private NamedLocation[] _userLocations;
	// Starting points of accessibility analysis
	private Coordinate[] _locations;
	private String _locationType = "start"; // either start or destination
	private double _range;
	private TravelRangeType _rangeType = TravelRangeType.Time;
	private String _routesFormat = "detailed";
	private int _limit = 5;
	
	public AccessibilityRequest()
	{
		_locationsRequest = new LocationsRequest();
		_routingRequest = new RoutingRequest();
	}
	
	public RoutingRequest getRoutingRequest()
	{
		return _routingRequest;
	}

	public LocationsRequest getLocationsRequest()
	{
		return _locationsRequest;
	}

	public int getLimit() {
		return _limit;
	}

	public void setLimit(int limit) {
		_limit = Math.min(limit, AccessibilityServiceSettings.getResponseLimit());
		_locationsRequest.setLimit(_limit);
	}

	public String getLocationType() {
		return _locationType;
	}

	public void setLocationType(String locationType) {
		_locationType = locationType;
	}

	public double getRange() {
		return _range;
	}

	public void setRange(double range) {
		_range = range;
	}

	public TravelRangeType getRangeType() {
		return _rangeType;
	}

	public void setRangeType(TravelRangeType rangeType) {
		_rangeType = rangeType;
	}

	public Coordinate[] getLocations() {
		return _locations;
	}

	public void setLocations(Coordinate[] locations) {
		_locations = locations;
		_routingRequest.setCoordinates(locations);
	}

	public String getRoutesFormat() {
		return _routesFormat;
	}

	public void setRoutesFormat(String routesFormat) {
		_routesFormat = routesFormat;
	}

	public NamedLocation[] getUserLocations() {
		return _userLocations;
	}

	public void setUserLocations(NamedLocation[] userLocations) {
		_userLocations = userLocations;
	}
}
