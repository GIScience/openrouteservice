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
package heigit.ors.routing;

import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.common.DistanceUnit;
import heigit.ors.services.ServiceRequest;

import java.util.ArrayList;
import java.util.List;

public class RoutingRequest extends ServiceRequest
{
    public static final String ATTR_DETOURFACTOR = "detourfactor";

    private Coordinate[] _coordinates;
	private RouteSearchParameters _searchParameters;
	private DistanceUnit _units = DistanceUnit.Meters;
	private String _language = "en";
	private String _geometryFormat = "encodedpolyline";
	private Boolean _geometrySimplify = false;
	private RouteInstructionsFormat _instructionsFormat = RouteInstructionsFormat.TEXT;
	private Boolean _includeInstructions = true;
	private Boolean _includeElevation = false;
	private Boolean _includeGeometry = true;
	private Boolean _includeManeuvers = false;
    private boolean _includeRoundaboutExits = false;
	private String[] _attributes = null;
    private int _extraInfo;
    private int _locationIndex = -1;
    private boolean _continueStraight = false;
    private Boolean _suppressWarnings = false;
	private List<Integer> _skipSegments = new ArrayList<>();

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

	public boolean getGeometrySimplify() { return _geometrySimplify; }

	public void setGeometrySimplify(boolean geometrySimplify) { _geometrySimplify = geometrySimplify; }

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

	public boolean getSuppressWarnings() {
		return _suppressWarnings;
	}

	public void setSuppressWarnings(boolean suppressWarnings) {
		_suppressWarnings = suppressWarnings;
	}

	public boolean isValid() {
		return !(_coordinates == null);
	}

	public List<Integer> getSkipSegments() {
		return _skipSegments;
	}

	public void setSkipSegments(List<Integer> skipSegments) {
		_skipSegments = skipSegments;
	}
}
