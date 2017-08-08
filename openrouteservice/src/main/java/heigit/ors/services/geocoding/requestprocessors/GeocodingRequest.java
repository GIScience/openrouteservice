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
package heigit.ors.services.geocoding.requestprocessors;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.geocoding.geocoders.Address;
import heigit.ors.geocoding.geocoders.SearchBoundary;
import heigit.ors.services.ServiceRequest;
import heigit.ors.services.geocoding.GeocodingServiceSettings;

public class GeocodingRequest extends ServiceRequest
{
	private int _limit = 5;
	private String _language;
	private SearchBoundary _boundary;
	private String _queryString;
	private Address _queryAddress;
	
	// reverse geocoding parameter
	private Coordinate _location;
	
	public GeocodingRequest()
	{
		
	}

	public String getQueryString() {
		return _queryString;
	}

	public void setQueryString(String query) {
		_queryString = query;
	}
	
	public Address getQueryAddress() {
		return _queryAddress;
	}

	public void setQueryAddress(Address query) {
		_queryAddress = query;
	}

	public int getLimit() {
		return _limit;
	}

	public void setLimit(int limit) {
		_limit = Math.min(limit, GeocodingServiceSettings.getResponseLimit());
	}

	public String getLanguage() {
		return _language;
	}

	public void setLanguage(String language) {
		this._language = language;
	}

	public Coordinate getLocation() {
		return _location;
	}

	public void setLocation(Coordinate location) {
		this._location = location;
	}
	
	public boolean isValid()
	{
		return !Helper.isEmpty(_queryString) || _queryAddress != null || _location != null;
	}

	public SearchBoundary getBoundary() {
		return _boundary;
	}

	public void setBoundary(SearchBoundary boundary) {
		this._boundary = boundary;
	}
}
