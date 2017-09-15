/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
