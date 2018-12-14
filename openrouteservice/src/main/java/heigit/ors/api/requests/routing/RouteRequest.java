/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package heigit.ors.api.requests.routing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.routing.RoutingErrorCodes;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApiModel(value = "RouteRequest", description = "The JSON body request sent to the routing service which defines options and parameters regarding the route to generate.")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RouteRequest {
    @ApiModelProperty(value = "Arbitrary identification string of the request reflected in the meta information.")
    private String id;
    private boolean hasId = false;

    @ApiModelProperty(name = "coordinates", value = "The waypoints to use for the route as an array of longitude/latitude pairs", example = "[[8.681495,49.41461],[8.686507,49.41943],[8.687872,49.420318]]")
    @JsonProperty("coordinates")
    private List<List<Double>> coordinates;

    @ApiModelProperty(hidden = true)
    private APIEnums.Profile profile;

    @ApiModelProperty(name = "preference",
            value = "Specifies the route preference.\n" +
                    "Default: fastest.")
    @JsonProperty(value = "preference", defaultValue = "fastest")
    private APIEnums.RoutePreference routePreference = APIEnums.RoutePreference.FASTEST;

    @ApiModelProperty(hidden = true)
    private APIEnums.RouteResponseType responseType = APIEnums.RouteResponseType.JSON;

    @ApiModelProperty(name = "units",
            value = "Specifies the distance unit.\n" +
                    "Default: m.")
    @JsonProperty(value = "units", defaultValue = "m")
    private APIEnums.Units units = APIEnums.Units.METRES;

    @ApiModelProperty(name = "language",
            value = "Language for the route instructions.\n" +
                    "Default: en.")
    @JsonProperty(value = "language", defaultValue = "en")
    private APIEnums.Languages language = APIEnums.Languages.EN;

    @ApiModelProperty(name = "geometry",
            value = "Specifies whether to return geometry.\n" +
                    "Default: true.[{response!=[geo+json]}]")
    @JsonProperty(value = "geometry", defaultValue = "true")
    private Boolean includeGeometry = true;

    @ApiModelProperty(name = "instructions",
            value = "Specifies whether to return instructions.\n" +
                    "Default: true.")
    @JsonProperty(value = "instructions", defaultValue = "true")
    private Boolean includeInstructionsInResponse = true;

    @ApiModelProperty(name = "instructions_format",
            value = "Select html for more verbose instructions.\n" +
                    "Default: text.")
    @JsonProperty(value = "instructions_format", defaultValue = "text")
    private APIEnums.InstructionsFormat instructionsFormat = APIEnums.InstructionsFormat.TEXT;

    @ApiModelProperty(name = "roundabout_exits",
            value = "Provides bearings of the entrance and all passed roundabout exits. Adds the exit_bearings array to the step object in the response.")
    @JsonProperty("roundabout_exits")
    private Boolean includeRoundaboutExitInfo;
    @JsonIgnore
    private boolean hasIncludeRoundaboutExitInfo = false;

    @ApiModelProperty(name = "attributes", value = "List of route attributes")
    @JsonProperty("attributes")
    private APIEnums.Attributes[] attributes;
    @JsonIgnore
    private boolean hasAttributes = false;

    @ApiModelProperty(name = "maneuvers", value = "Specifies whether the maneuver object is included into the step object or not. Default: false.")
    @JsonProperty(value = "maneuvers", defaultValue = "false")
    private Boolean incĺudeManeuvers = false;

    @ApiModelProperty(name = "radiuses", value = "A pipe list of maximum distances (measured in metres) that limit the search of nearby road segments to every given waypoint. " +
            "The values must be greater than 0, the value of -1 specifies no limit in the search. The number of radiuses correspond to the number of waypoints.", example = "[ 200, -1, 30 ]")
    @JsonProperty("radiuses")
    private Double[] maximumSearchRadii;
    @JsonIgnore
    private boolean hasMaximumSearchRadii = false;

    @ApiModelProperty(name = "bearings", value = "Specifies a list of pairs (bearings and deviations) to filter the segments of the road network a waypoint can snap to. " +
            "For example bearings=45,10|120,20. Each pair is a comma-separated list that can consist of one or two float values, where the first value is the bearing and the second one is the allowed deviation from the bearing. " +
            "The bearing can take values between 0 and 360 clockwise from true north. If the deviation is not set, then the default value of 100 degrees is used. " +
            "The number of pairs must correspond to the number of waypoints. Setting optimized=false is mandatory for this feature to work for all profiles. " +
            "The number of bearings corresponds to the length of waypoints-1 or waypoints. If the bearing information for the last waypoint is given, then this will control the sector from which the destination waypoint may be reached. " +
            "You can skip a bearing for a certain waypoint by passing an empty value for an array, e.g. [30,20],[],[40,20].",
            example = "[ [ 30, 20 ], [ ], [ 40, 20 ] ]"
    )
    @JsonProperty("bearings")
    private Double[][] bearings;
    @JsonIgnore
    private boolean hasBearings = false;

    @ApiModelProperty(name = "continue_straight", value = "Forces the route to keep going straight at waypoints restricting uturns there even if it would be faster. This setting will work for all profiles except for driving-*. " +
            "In this case you will have to set optimized=false for it to work. Default: false. [{profile!=[car,hgv]}]")
    @JsonProperty(value = "continue_straight", defaultValue = "false")
    private Boolean continueStraightAtWaypoints = false;

    @ApiModelProperty(name = "elevation", value = "Specifies whether to return elevation values for points. Please note that elevation also gets encoded for json response encoded polyline.")
    @JsonProperty("elevation")
    private Boolean useElevation;
    @JsonIgnore
    private boolean hasUseElevation = false;

    @ApiModelProperty(name = "extra_info",
            value = "The extra info items to include in the response")
    @JsonProperty("extra_info")
    private APIEnums.ExtraInfo[] extraInfo;
    @JsonIgnore
    private boolean hasExtraInfo = false;

    @ApiModelProperty(name = "optimized", value = "Uses contraction hierarchies if available (false).\n" +
            "Default: true.")
    @JsonProperty(value = "optimized", defaultValue = "true")
    private Boolean useContractionHierarchies = true;
    @JsonIgnore
    private boolean hasUseContractionHierarchies = false;

    @ApiModelProperty(name = "options",
            value = "Additional options for the route request")
    @JsonProperty("options")
    private RouteRequestOptions routeOptions;
    @JsonIgnore
    private boolean hasRouteOptions = false;

    @ApiModelProperty(name="suppress_warnings",
            value = "Suppress warning messages in the response")
    @JsonProperty("suppress_warnings")
    private Boolean suppressWarnings = false;
    @JsonIgnore
    private boolean hasSuppressWarnings = false;

    @JsonCreator
    public RouteRequest(@JsonProperty(value = "coordinates", required = true) List<List<Double>> coordinates) {
        this.coordinates = coordinates;
    }

    public RouteRequest(Double[][] coordinates) throws ParameterValueException {
        if (coordinates.length < 2)
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, "coordinates");

        this.coordinates = new ArrayList<>();
        for (Double[] coordPair : coordinates) {
            if (coordPair.length != 2)
                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, "coordinates");
            List<Double> coordPairList = new ArrayList<>();
            coordPairList.add(coordPair[0]);
            coordPairList.add(coordPair[1]);
            this.coordinates.add(coordPairList);
        }
    }

    public RouteRequest(Coordinate start, Coordinate end) throws ParameterValueException {
        if(start == null) {
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, "start");
        }
        if(end == null) {
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, "end");
        }

        this.coordinates = new ArrayList<>();
        this.coordinates.add(
                new ArrayList<>(Arrays.asList(start.x, start.y))
        );
        this.coordinates.add(
                new ArrayList<>(Arrays.asList(end.x, end.y))
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.hasId = true;
    }

    public boolean hasId() {
        return hasId;
    }

    public List<List<Double>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<List<Double>> coordinates) {
        this.coordinates = coordinates;
    }

    public APIEnums.Profile getProfile() {
        return profile;
    }

    public void setProfile(APIEnums.Profile profile) {
        this.profile = profile;
    }

    public APIEnums.Units getUnits() {
        return units;
    }

    public void setUnits(APIEnums.Units units) {
        this.units = units;
    }

    public APIEnums.Languages getLanguage() {
        return language;
    }

    public void setLanguage(APIEnums.Languages language) {
        this.language = language;
    }

    public APIEnums.RoutePreference getRoutePreference() {
        return routePreference;
    }

    public void setRoutePreference(APIEnums.RoutePreference routePreference) {
        this.routePreference = routePreference;
    }

    public APIEnums.RouteResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(APIEnums.RouteResponseType responseType) {
        this.responseType = responseType;
    }

    public APIEnums.ExtraInfo[] getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(APIEnums.ExtraInfo[] extraInfo) {
        this.extraInfo = extraInfo;
        this.hasExtraInfo = true;
    }

    public RouteRequestOptions getRouteOptions() {
        return routeOptions;
    }

    public void setRouteOptions(RouteRequestOptions routeOptions) {
        this.routeOptions = routeOptions;
        hasRouteOptions = true;
    }

    public Boolean getIncludeGeometry() {
        return includeGeometry;
    }

    public void setIncludeGeometry(Boolean includeGeometry) {
        this.includeGeometry = includeGeometry;
    }

    public Boolean getIncludeInstructionsInResponse() {
        return includeInstructionsInResponse;
    }

    public void setIncludeInstructionsInResponse(Boolean includeInstructionsInResponse) {
        this.includeInstructionsInResponse = includeInstructionsInResponse;
    }

    public APIEnums.InstructionsFormat getInstructionsFormat() {
        return instructionsFormat;
    }

    public void setInstructionsFormat(APIEnums.InstructionsFormat instructionsFormat) {
        this.instructionsFormat = instructionsFormat;
    }

    public Boolean getIncludeRoundaboutExitInfo() {
        return includeRoundaboutExitInfo;
    }

    public void setIncludeRoundaboutExitInfo(Boolean includeRoundaboutExitInfo) {
        this.includeRoundaboutExitInfo = includeRoundaboutExitInfo;
        hasIncludeRoundaboutExitInfo = true;
    }

    public APIEnums.Attributes[] getAttributes() {
        return attributes;
    }

    public void setAttributes(APIEnums.Attributes[] attributes) {
        this.attributes = attributes;
        this.hasAttributes = true;
    }

    public Boolean getIncĺudeManeuvers() {
        return incĺudeManeuvers;
    }

    public void setIncĺudeManeuvers(Boolean incĺudeManeuvers) {
        this.incĺudeManeuvers = incĺudeManeuvers;
    }

    public Double[] getMaximumSearchRadii() {
        return maximumSearchRadii;
    }

    public void setMaximumSearchRadii(Double[] maximumSearchRadii) {
        this.maximumSearchRadii = maximumSearchRadii;
        hasMaximumSearchRadii = true;
    }

    public Double[][] getBearings() {
        return bearings;
    }

    public void setBearings(Double[][] bearings) {
        this.bearings = bearings;
        hasBearings = true;
    }

    public Boolean getContinueStraightAtWaypoints() {
        return continueStraightAtWaypoints;
    }

    public void setContinueStraightAtWaypoints(Boolean continueStraightAtWaypoints) {
        this.continueStraightAtWaypoints = continueStraightAtWaypoints;
    }

    public Boolean getUseElevation() {
        return useElevation;
    }

    public void setUseElevation(Boolean useElevation) {
        this.useElevation = useElevation;
        hasUseElevation = true;
    }

    public Boolean getUseContractionHierarchies() {
        return useContractionHierarchies;
    }

    public void setUseContractionHierarchies(Boolean useContractionHierarchies) {
        this.useContractionHierarchies = useContractionHierarchies;
        hasUseContractionHierarchies = true;
    }

    public Boolean getSuppressWarnings() {
        return suppressWarnings;
    }

    public void setSuppressWarnings(boolean suppressWarnings) {
        this.suppressWarnings = suppressWarnings;
        hasSuppressWarnings = true;
    }

    public boolean hasIncludeRoundaboutExitInfo() {
        return hasIncludeRoundaboutExitInfo;
    }

    public boolean hasAttributes() {
        return hasAttributes;
    }

    public boolean hasMaximumSearchRadii() {
        return hasMaximumSearchRadii;
    }

    public boolean hasBearings() {
        return hasBearings;
    }

    public boolean hasUseElevation() {
        return hasUseElevation;
    }

    public boolean hasRouteOptions() {
        return hasRouteOptions;
    }

    public boolean hasUseContractionHierarchies() {
        return hasUseContractionHierarchies;
    }

    public boolean hasExtraInfo() {
        return hasExtraInfo;
    }

    public boolean hasSuppressWarnings() {
        return hasSuppressWarnings;
    }
}
