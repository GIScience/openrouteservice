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
package heigit.ors.locations;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import heigit.ors.services.ServiceRequest;
import heigit.ors.services.locations.LocationsServiceSettings;

public class LocationsRequest extends ServiceRequest
{
	private LocationsSearchFilter _filter = new LocationsSearchFilter();
	private int _limit = 20;
	private String _language;
	private Envelope _bbox;
	private Geometry _geometry;
	private double _radius;
	private LocationRequestType _type = LocationRequestType.POIS;
	private int _details = LocationDetailsType.NONE;
	private LocationsResultSortType _sortType = LocationsResultSortType.NONE;

	public LocationsRequest()
	{

	}

	public LocationsSearchFilter getSearchFilter() {
		return _filter;
	}

	public Geometry getGeometry()
	{
		return _geometry;
	}

	public void setGeometry(Geometry geom)
	{
		_geometry =  geom;
	}

	public int getLimit() {
		return _limit;
	}

	public void setLimit(int limit) {
		_limit = Math.min(limit, LocationsServiceSettings.getResponseLimit());
	}

	public String getLanguage() {
		return _language;
	}

	public void setLanguage(String language) {
		this._language = language;
	}

	public boolean isValid() {
		return _type == LocationRequestType.CATEGORY_LIST || (!(_geometry == null && _bbox == null));
	}

	public Envelope getBBox() {
		return _bbox;
	}

	public void setBBox(Envelope bbox) {
		this._bbox = bbox;
	}

	public double getRadius() {
		return _radius;
	}

	public void setRadius(double radius) {
		_radius = radius;
	}

	public LocationRequestType getType() {
		return _type;
	}

	public void setType(LocationRequestType type) {
		_type = type;
	}

	public LocationsResultSortType getSortType() {
		return _sortType;
	}

	public void setSortType(LocationsResultSortType value) {
		this._sortType = value;
	}

	public int getDetails() {
		return _details;
	}

	public void setDetails(int value) {
		_details = value;
	}
	
	public LocationsRequest clone()
	{
		LocationsRequest req = new LocationsRequest();
		req._bbox = _bbox;
		req._filter = _filter.clone();
		req._geometry = _geometry;
		req._language = _language;
		req._limit = _limit;
		req._radius = _radius;
		req._sortType = _sortType;
		req._type = _type;
		req._details = _details;
		
		return req;
	}
}

