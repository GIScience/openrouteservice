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

import heigit.ors.locations.LocationsRequest;
import heigit.ors.services.isochrones.IsochroneRequest;

public class AccessibilityRequest 
{
	private LocationsRequest _locationsRequest;
	private IsochroneRequest _isochroneRequest; 
	private int _limit = 5;
	
	public AccessibilityRequest()
	{
		_locationsRequest = new LocationsRequest();
		_isochroneRequest = new IsochroneRequest();
	}
	
	public LocationsRequest getLocationsRequest()
	{
		return _locationsRequest;
	}
	
	public IsochroneRequest getIsochroneRequest()
	{
		return _isochroneRequest;
	}
	
	public int getLimit() {
		return _limit;
	}

	public void setLimit(int limit) {
		_limit = Math.min(limit, AccessibilityServiceSettings.getResponseLimit());
		_locationsRequest.setLimit(_limit);
	}
}
