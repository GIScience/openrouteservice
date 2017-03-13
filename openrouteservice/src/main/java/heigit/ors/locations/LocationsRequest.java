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

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import heigit.ors.services.ServiceRequest;
import heigit.ors.services.locations.LocationsServiceSettings;

public class LocationsRequest extends ServiceRequest
{
	private String _query;
	private int _limit = 20;
	private String _language;
    private Envelope _bbox;
    private Geometry _geometry;
    private double _radius;
   
   public LocationsRequest()
   {
	   
   }
   
   public String getQuery() {
		return _query;
	}

	public void setQuery(String query) {
		_query = query;
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
	
	public boolean isValid()
	{
		return !Helper.isEmpty(_query) || _geometry != null;
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
}
