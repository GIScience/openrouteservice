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
package org.heigit.ors.routing;

import com.graphhopper.util.Helper;
import org.locationtech.jts.geom.Coordinate;

import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.common.ServiceRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class RoutingRequest extends ServiceRequest {
    public static final String ATTR_DETOURFACTOR = "detourfactor";

    private Coordinate[] coordinates;
	private RouteSearchParameters searchParameters;
	private DistanceUnit units = DistanceUnit.METERS;
	private String language = "en";
	private String geometryFormat = "encodedpolyline";
	private boolean geometrySimplify = false;
	private RouteInstructionsFormat instructionsFormat = RouteInstructionsFormat.TEXT;
	private boolean includeInstructions = true;
	private boolean includeElevation = false;
	private boolean includeGeometry = true;
	private boolean includeManeuvers = false;
    private boolean includeRoundaboutExits = false;
	private String[] attributes = null;
    private int extraInfo;
    private int locationIndex = -1;
    private boolean continueStraight = false;
	private List<Integer> skipSegments = new ArrayList<>();
	private boolean includeCountryInfo = false;
	private double maximumSpeed;

	private String responseFormat = "json";
	// Fields specific to GraphHopper GTFS
	private boolean schedule;
	private Duration walkingTime;
	private int scheduleRows;
	private boolean ignoreTransfers;
	private Duration scheduleDuration;

	public RoutingRequest()
	{
		searchParameters = new RouteSearchParameters();
	}

	public Coordinate[] getCoordinates() {
		return coordinates;
	}
	
	public Coordinate getDestination()
	{
		return coordinates[coordinates.length - 1];
	}

	public void setCoordinates(Coordinate[] coordinates) {
		this.coordinates = coordinates;
	}

	public RouteSearchParameters getSearchParameters() {
		return searchParameters;
	}

	public void setSearchParameters(RouteSearchParameters searchParameters) {
		this.searchParameters = searchParameters;
	}

	public boolean getIncludeInstructions() {
		return includeInstructions;
	}

	public void setIncludeInstructions(boolean includeInstructions) {
		this.includeInstructions = includeInstructions;
	}

	public DistanceUnit getUnits() {
		return units;
	}

	public void setUnits(DistanceUnit units) {
		this.units = units;
	}

	public String getGeometryFormat() {
		return geometryFormat;
	}

	public void setGeometryFormat(String geometryFormat) {
		this.geometryFormat = geometryFormat;
	}

	public boolean getGeometrySimplify() { return geometrySimplify; }

	public void setGeometrySimplify(boolean geometrySimplify) { this.geometrySimplify = geometrySimplify; }

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public RouteInstructionsFormat getInstructionsFormat() {
		return instructionsFormat;
	}

	public void setInstructionsFormat(RouteInstructionsFormat format) {
		instructionsFormat = format;
	}

	public int getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(int extraInfo) {
		this.extraInfo = extraInfo;
	}

	public boolean getIncludeElevation() {
		return includeElevation;
	}

	public void setIncludeElevation(boolean includeElevation) {
		this.includeElevation = includeElevation;
	}

	public boolean getIncludeGeometry() {
		return includeGeometry;
	}

	public void setIncludeGeometry(boolean includeGeometry) {
		this.includeGeometry = includeGeometry;
	}

	public String[] getAttributes() {
		return attributes;
	}

	public void setAttributes(String[] attributes) {
		this.attributes = attributes;
	}
	
	public boolean hasAttribute(String attr) {
		if (attributes == null || attr == null)
			return false;

		for (String attribute : attributes)
			if (attr.equalsIgnoreCase(attribute))
				return true;

		return false;
	}

	public int getLocationIndex() {
		return locationIndex;
	}

	public void setLocationIndex(int locationIndex) {
		this.locationIndex = locationIndex;
	}

	public boolean getIncludeManeuvers() {
		return includeManeuvers;
	}

	public void setIncludeManeuvers(boolean includeManeuvers) {
		this.includeManeuvers = includeManeuvers;
	}

	public boolean getContinueStraight() {
		return continueStraight;
	}

	public void setContinueStraight(boolean continueStraight) {
		this.continueStraight = continueStraight;
	}

	public boolean getIncludeRoundaboutExits() {
		return includeRoundaboutExits;
	}

	public void setIncludeRoundaboutExits(boolean includeRoundaboutExits) {
		this.includeRoundaboutExits = includeRoundaboutExits;
	}

	public boolean isValid() {
		return coordinates != null;
	}

	public List<Integer> getSkipSegments() {
		return skipSegments;
	}

	public void setSkipSegments(List<Integer> skipSegments) {
		this.skipSegments = skipSegments;
	}

	public boolean getIncludeCountryInfo() {
		return includeCountryInfo;
	}

	public void setIncludeCountryInfo(boolean includeCountryInfo) {
		this.includeCountryInfo = includeCountryInfo;
	}

	public void setMaximumSpeed(double maximumSpeed){
		this.maximumSpeed = maximumSpeed;
	}

	public double getMaximumSpeed(){
		return maximumSpeed;
	}

	public void setResponseFormat(String responseFormat) {
		if (!Helper.isEmpty(responseFormat)) {
			this.responseFormat = responseFormat;
		}
	}

	public String getResponseFormat() {
		return this.responseFormat;
	}

	public boolean isRoundTripRequest() {
		return this.coordinates.length == 1 && this.searchParameters.getRoundTripLength() > 0;
	}

	public void setSchedule(boolean schedule) {
		this.schedule = schedule;
	}

	public void setWalkingTime(Duration walkingTime) {
		this.walkingTime = walkingTime;
	}

	public void setScheduleRows(int scheduleRows) {
		this.scheduleRows = scheduleRows;
	}

	public void setIgnoreTransfers(boolean ignoreTransfers) {
		this.ignoreTransfers = ignoreTransfers;
	}

	public void setScheduleDuaration(Duration scheduleDuration) {
		this.scheduleDuration = scheduleDuration;
	}
}
