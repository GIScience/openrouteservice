/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
 */
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
	private float _minimumConfidence = 0.0f;
	
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

	public float getMinimumConfidence() {
		return _minimumConfidence;
	}

	public void setMinimumConfidence(float minimumConfidence) {
		_minimumConfidence = minimumConfidence;
	}
}
