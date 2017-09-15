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
package heigit.ors.accessibility;

import java.util.ArrayList;
import java.util.List;

import heigit.ors.common.NamedLocation;
import heigit.ors.common.TravelRangeType;
import heigit.ors.common.TravellerInfo;
import heigit.ors.isochrones.IsochroneSearchParameters;
import heigit.ors.locations.LocationsRequest;
import heigit.ors.services.ServiceRequest;
import heigit.ors.services.accessibility.AccessibilityServiceSettings;

public class AccessibilityRequest extends ServiceRequest
{
	private List<TravellerInfo> _travellers;

    // Destination points specified either by a user defined locations or by POI search filter
	private LocationsRequest _locationsRequest;
	private NamedLocation[] _userLocations;
    // common parameters for all locations
	private String _routesFormat = "detailed";
	private String _units = "m";
	private int _limit = 5;
	private boolean _includeElevation = false;
	private boolean _includeGeometry = false;
	private String _geometryFormat = "encodedpolyline";

	public AccessibilityRequest()
	{
		_travellers = new ArrayList<TravellerInfo>();
	}
	
	public LocationsRequest getLocationsRequest()
	{
		return _locationsRequest;
	}

	public void setLocationsRequest(LocationsRequest locRequest)
	{
		_locationsRequest = locRequest;
	}

	public int getLimit() {
		return _limit;
	}

	public void setLimit(int limit) {
		_limit = Math.min(limit, AccessibilityServiceSettings.getResponseLimit());
		
		if (_locationsRequest != null)
			_locationsRequest.setLimit(_limit);
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

	public String getUnits() {
		return _units;
	}

	public void setUnits(String units) {
		_units = units;
	}

	public List<TravellerInfo> getTravellers() {
		return _travellers;
	}

	public void addTraveller(TravellerInfo travellerInfo) {
		_travellers.add(travellerInfo);
	}

	public boolean getIncludeElevation() {
		return _includeElevation;
	}

	public void setIncludeElevation(boolean includeElevation) {
		_includeElevation = includeElevation;
	}

	public boolean getIncludeGeometry() {
		return _includeGeometry;
	}

	public void setIncludeGeometry(boolean includeGeometry) {
		_includeGeometry = includeGeometry;
	}

	public String getGeometryFormat() {
		return _geometryFormat;
	}

	public void setGeometryFormat(String geometryFormat) {
		_geometryFormat = geometryFormat;
	}
	
	public IsochroneSearchParameters getIsochroneSearchParameters(int travellerIndex)
	{
		TravellerInfo traveller = _travellers.get(travellerIndex);
		double[] ranges = traveller.getRanges();

		// convert ranges in units to meters or seconds
		if (!(_units == null || "m".equalsIgnoreCase(_units)))
		{
			double scale = 1.0;
			if (traveller.getRangeType() == TravelRangeType.Distance)
			{
				switch(_units)
				{
				case "m":
					break;
				case "km":
					scale = 1000;
					break;
				case "mi":
					scale = 1609.34;
					break;
				}
			}

			if (scale != 1.0)
			{
				for (int i = 0; i < ranges.length; i++)
					ranges[i] = ranges[i]*scale;
			}
		}

		IsochroneSearchParameters parameters = new IsochroneSearchParameters(travellerIndex, traveller.getLocation(), ranges);
		parameters.setLocation(traveller.getLocation());
		parameters.setRangeType(traveller.getRangeType() );
		parameters.setCalcMethod("default");
		parameters.setRouteParameters(traveller.getRouteSearchParameters());
		if ("destination".equalsIgnoreCase(traveller.getLocationType()))
			parameters.setReverseDirection(true);

		return parameters;
	}
}
