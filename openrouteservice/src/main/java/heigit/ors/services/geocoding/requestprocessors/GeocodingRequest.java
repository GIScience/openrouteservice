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
import com.vividsolutions.jts.geom.Envelope;

import heigit.ors.services.ServiceRequest;

public class GeocodingRequest extends ServiceRequest
{
	private String _query;
	private int _limit = 5;
	private String _language;
	private Envelope _bbox;
	
	// reverse geocoding parameter
	private Coordinate _location;
	
	public GeocodingRequest()
	{
		
	}

	public String getQuery() {
		return _query;
	}

	public void setQuery(String query) {
		_query = query;
	}

	public int getLimit() {
		return _limit;
	}

	public void setLimit(int limit) {
		_limit = Math.max(limit, 20);
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
		return !Helper.isEmpty(_query) || _location != null;
	}

	public Envelope getBBox() {
		return _bbox;
	}

	public void setBBox(Envelope bbox) {
		this._bbox = bbox;
	}
}
