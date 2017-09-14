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
	private double _radius = 0.0;
	private LocationRequestType _type = LocationRequestType.UNKNOWN;
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

