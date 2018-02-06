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
package heigit.ors.routing;

import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.common.DistanceUnit;
import heigit.ors.services.ServiceRequest;

public class RoutingRequest extends ServiceRequest
{
	private Coordinate[] _coordinates;
	private RouteSearchParameters _searchParameters;
	private DistanceUnit _units = DistanceUnit.Meters;
	private String _language = "en";
	private String _geometryFormat = "encodedpolyline";
	private RouteInstructionsFormat _instructionsFormat = RouteInstructionsFormat.TEXT;
	private Boolean _includeInstructions = true;
	private Boolean _includeElevation = false;
	private Boolean _includeGeometry = true;
	private Boolean _includeManeuvers = false;
    private boolean _includeRoundaboutExits = false;
	private Boolean _simplifyGeometry = false;
	private String[] _attributes = null;
    private int _extraInfo;
    private int _locationIndex = -1;
    private boolean _continueStraight = false;
	
	public RoutingRequest()
	{
		_searchParameters = new RouteSearchParameters();
	}

	public Coordinate[] getCoordinates() {
		return _coordinates;
	}
	
	public Coordinate getDestination()
	{
		return _coordinates[_coordinates.length - 1];
	}

	public void setCoordinates(Coordinate[] _coordinates) {
		this._coordinates = _coordinates;
	}

	public RouteSearchParameters getSearchParameters() {
		return _searchParameters;
	}

	public void setSearchParameters(RouteSearchParameters _searchParameters) {
		this._searchParameters = _searchParameters;
	}

	public boolean getIncludeInstructions() {
		return _includeInstructions;
	}

	public void setIncludeInstructions(boolean includeInstructions) {
		_includeInstructions = includeInstructions;
	}

	public DistanceUnit getUnits() {
		return _units;
	}

	public void setUnits(DistanceUnit units) {
		_units = units;
	}

	public String getGeometryFormat() {
		return _geometryFormat;
	}

	public void setGeometryFormat(String geometryFormat) {
		_geometryFormat = geometryFormat;
	}

	public String getLanguage() {
		return _language;
	}

	public void setLanguage(String language) {
		_language = language;
	}

	public RouteInstructionsFormat getInstructionsFormat() {
		return _instructionsFormat;
	}

	public void setInstructionsFormat(RouteInstructionsFormat format) {
		_instructionsFormat = format;
	}

	public int getExtraInfo() {
		return _extraInfo;
	}

	public void setExtraInfo(int extraInfo) {
		_extraInfo = extraInfo;
	}

	public Boolean getIncludeElevation() {
		return _includeElevation;
	}

	public void setIncludeElevation(Boolean includeElevation) {
		this._includeElevation = includeElevation;
	}

	public Boolean getIncludeGeometry() {
		return _includeGeometry;
	}

	public void setIncludeGeometry(Boolean includeGeometry) {
		this._includeGeometry = includeGeometry;
	}

	public String[] getAttributes() {
		return _attributes;
	}

	public void setAttributes(String[] attributes) {
		_attributes = attributes;
	}
	
	public boolean hasAttribute(String attr) {
		if (_attributes == null || attr == null)
			return false;

		for (int i = 0; i< _attributes.length; i++)
			if (attr.equalsIgnoreCase(_attributes[i]))
				return true;

		return false;
	}

	public Boolean getSimplifyGeometry() {
		return _simplifyGeometry;
	}

	public void setSimplifyGeometry(Boolean simplifyGeometry) {
		_simplifyGeometry = simplifyGeometry;
	}
	
	public boolean getConsiderTraffic(){
		return this._searchParameters.getConsiderTraffic();
		
	}

	public int getLocationIndex() {
		return _locationIndex;
	}

	public void setLocationIndex(int locationIndex) {
		_locationIndex = locationIndex;
	}

	public Boolean getIncludeManeuvers() {
		return _includeManeuvers;
	}

	public void setIncludeManeuvers(Boolean includeManeuvers) {
		_includeManeuvers = includeManeuvers;
	}

	public boolean getContinueStraight() {
		return _continueStraight;
	}

	public void setContinueStraight(boolean continueStraight) {
		_continueStraight = continueStraight;
	}

	public boolean getIncludeRoundaboutExits() {
		return _includeRoundaboutExits;
	}

	public void setIncludeRoundaboutExits(boolean includeRoundaboutExits) {
		_includeRoundaboutExits = includeRoundaboutExits;
	}

	public boolean isValid() {
		return !(_coordinates == null);
	}
}
