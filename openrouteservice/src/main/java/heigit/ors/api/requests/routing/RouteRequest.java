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

@ApiModel(value = "Directions Service", description = "The JSON body request sent to the routing service which defines options and parameters regarding the route to generate.")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RouteRequest {
    public static final String PARAM_ID = "id";
    public static final String PARAM_COORDINATES = "coordinates";
    public static final String PARAM_PROFILE = "profile";
    public static final String PARAM_PREFERENCE = "preference";
    public static final String PARAM_FORMAT = "format";
    public static final String PARAM_UNITS = "units";
    public static final String PARAM_LANGUAGE = "language";
    public static final String PARAM_GEOMETRY = "geometry";
    public static final String PARAM_INSTRUCTIONS = "instructions";
    public static final String PARAM_INSTRUCTIONS_FORMAT = "instructions_format";
    public static final String PARAM_ROUNDABOUT_EXITS = "roundabout_exits";
    public static final String PARAM_ATTRIBUTES = "attributes";
    public static final String PARAM_MANEUVERS = "maneuvers";
    public static final String PARAM_RADII = "radiuses";
    public static final String PARAM_BEARINGS = "bearings";
    public static final String PARAM_CONTINUE_STRAIGHT = "continue_straight";
    public static final String PARAM_ELEVATION  = "elevation";
    public static final String PARAM_EXTRA_INFO = "extra_info";
    public static final String PARAM_OPTIMIZED = "optimized";
    public static final String PARAM_OPTIONS = "options";
    public static final String PARAM_SUPPRESS_WARNINGS = "suppress_warnings";
    public static final String PARAM_SIMPLIFY_GEOMETRY = "geometry_simplify";
    public static final String PARAM_SKIP_SEGMENTS = "skip_segments";


    @ApiModelProperty(name = PARAM_ID, value = "Arbitrary identification string of the request reflected in the meta information.",
            example = "routing_request")
    @JsonProperty(PARAM_ID)
    private String id;
    @JsonIgnore
    private boolean hasId = false;

    @ApiModelProperty(name = PARAM_COORDINATES, value = "The waypoints to use for the route as an array of `longitude/latitude` pairs",
            example = "[[8.681495,49.41461],[8.686507,49.41943],[8.687872,49.420318]]",
            required = true)
    @JsonProperty(PARAM_COORDINATES)
    private List<List<Double>> coordinates;

    @ApiModelProperty(name = PARAM_PROFILE, hidden = true)
    private APIEnums.Profile profile;

    @ApiModelProperty(name = PARAM_PREFERENCE,
            value = "Specifies the route preference. CUSTOM_KEYS:{'apiDefault':'fastest'}",
            example = "fastest")
    @JsonProperty(value = PARAM_PREFERENCE)
    private APIEnums.RoutePreference routePreference;
    @JsonIgnore
    private boolean hasRoutePreference = false;

    @ApiModelProperty(name = PARAM_FORMAT, hidden = true)
    @JsonProperty(PARAM_FORMAT)
    private APIEnums.RouteResponseType responseType = APIEnums.RouteResponseType.JSON;

    @ApiModelProperty(name = PARAM_UNITS,
            value = "Specifies the distance unit. CUSTOM_KEYS:{'apiDefault':'m'}",
            example = "m")
    @JsonProperty(value = PARAM_UNITS)
    private APIEnums.Units units;
    @JsonIgnore
    private boolean hasUnits = false;

    @ApiModelProperty(name = PARAM_LANGUAGE,
            value = "Language for the route instructions. CUSTOM_KEYS:{'apiDefault':'en'}",
            example = "en")
    @JsonProperty(value = PARAM_LANGUAGE)
    private APIEnums.Languages language;
    @JsonIgnore
    private boolean hasLanguage = false;

    @ApiModelProperty(name = PARAM_GEOMETRY,
            value = "Specifies whether to return geometry. " +
                    "CUSTOM_KEYS:{'apiDefault':true, 'validWhen':{'ref':'format','value':['json']}}",
                    example = "true")
    @JsonProperty(value = PARAM_GEOMETRY)
    private Boolean includeGeometry;
    @JsonIgnore
    private boolean hasIncludeGeometry = false;

    @ApiModelProperty(name = PARAM_INSTRUCTIONS,
            value = "Specifies whether to return instructions. CUSTOM_KEYS:{'apiDefault':true}",
            example = "true")
    @JsonProperty(value = PARAM_INSTRUCTIONS)
    private Boolean includeInstructionsInResponse;
    @JsonIgnore
    private boolean hasIncludeInstructions = false;

    @ApiModelProperty(name = PARAM_INSTRUCTIONS_FORMAT,
            value = "Select html for more verbose instructions. CUSTOM_KEYS:{'apiDefault':'text'}",
            example = "text")
    @JsonProperty(value = PARAM_INSTRUCTIONS_FORMAT)
    private APIEnums.InstructionsFormat instructionsFormat;
    @JsonIgnore
    private boolean hasInstructionsFormat = false;

    @ApiModelProperty(name = PARAM_ROUNDABOUT_EXITS,
            value = "Provides bearings of the entrance and all passed roundabout exits. Adds the `exit_bearings` array to the step object in the response. " +
                    "CUSTOM_KEYS:{'apiDefault':false}",
            example = "false")
    @JsonProperty(value = PARAM_ROUNDABOUT_EXITS)
    private Boolean includeRoundaboutExitInfo;
    @JsonIgnore
    private boolean hasIncludeRoundaboutExitInfo = false;

    @ApiModelProperty(name = PARAM_ATTRIBUTES,
            value = "List of route attributes",
            example = "[\"avgspeed\",\"percentage\"]")
    @JsonProperty(PARAM_ATTRIBUTES)
    private APIEnums.Attributes[] attributes;
    @JsonIgnore
    private boolean hasAttributes = false;

    @ApiModelProperty(name = PARAM_MANEUVERS, value = "Specifies whether the maneuver object is included into the step object or not. " +
            "CUSTOM_KEYS:{'apiDefault':false}",
            example = "false")
    @JsonProperty(value = PARAM_MANEUVERS)
    private Boolean includeManeuvers;
    @JsonIgnore
    private boolean hasIncludeManeuvers = false;

    @ApiModelProperty(name = PARAM_RADII, value = "A pipe list of maximum distances (measured in metres) that limit the search of nearby road segments to every given waypoint. " +
            "The values must be greater than 0, the value of -1 specifies no limit in the search. The number of radiuses correspond to the number of waypoints.",
            example = "[200, -1, 30]")
    @JsonProperty(PARAM_RADII)
    private Double[] maximumSearchRadii;
    @JsonIgnore
    private boolean hasMaximumSearchRadii = false;

    @ApiModelProperty(name = PARAM_BEARINGS, value = "Specifies a list of pairs (bearings and deviations) to filter the segments of the road network a waypoint can snap to. " +
            "For example `bearings=[[45,10],[120,20]]`. \n" +
            "Each pair is a comma-separated list that can consist of one or two float values, where the first value is the bearing and the second one is the allowed deviation from the bearing. " +
            "The bearing can take values between `0` and `360` clockwise from true north. If the deviation is not set, then the default value of `100` degrees is used. " +
            "The number of pairs must correspond to the number of waypoints.\n" +
            "The number of bearings corresponds to the length of waypoints-1 or waypoints. If the bearing information for the last waypoint is given, then this will control the sector from which the destination waypoint may be reached. " +
            "You can skip a bearing for a certain waypoint by passing an empty value for an array, e.g. `[30,20],[],[40,20]`. CUSTOM_KEYS:{'validWhen':{'ref':'profile','value':'cycling-*'}}",
            example = "[[30, 20], [], [40, 20]]"
    )
    @JsonProperty(PARAM_BEARINGS)
    private Double[][] bearings;
    @JsonIgnore
    private boolean hasBearings = false;

    @ApiModelProperty(name = PARAM_CONTINUE_STRAIGHT,
            value = "Forces the route to keep going straight at waypoints restricting uturns there even if it would be faster. This setting will work for all profiles except for `driving-*`. " +
            "CUSTOM_KEYS:{'apiDefault':'true','validWhen':{'ref':'profile','valueNot':['driving-*']}}",
            example = "false")
    @JsonProperty(value = PARAM_CONTINUE_STRAIGHT)
    private Boolean continueStraightAtWaypoints;
    @JsonIgnore
    private boolean hasContinueStraightAtWaypoints = false;

    @ApiModelProperty(name = PARAM_ELEVATION,
            value = "Specifies whether to return elevation values for points. Please note that elevation also gets encoded for json response encoded polyline.",
            example = "false")
    @JsonProperty(value = PARAM_ELEVATION)
    private Boolean useElevation;
    @JsonIgnore
    private boolean hasUseElevation = false;

    @ApiModelProperty(name = PARAM_EXTRA_INFO,
            value = "The extra info items to include in the response",
            example = "[\"waytype\",\"surface\"]")
    @JsonProperty(PARAM_EXTRA_INFO)
    private APIEnums.ExtraInfo[] extraInfo;
    @JsonIgnore
    private boolean hasExtraInfo = false;

    @ApiModelProperty(name = PARAM_OPTIMIZED, value = "Uses contraction hierarchies if available (false). " +
            "CUSTOM_KEYS:{'apiDefault':true}", hidden = true)
    @JsonProperty(value = PARAM_OPTIMIZED)
    private Boolean useContractionHierarchies;
    @JsonIgnore
    private boolean hasUseContractionHierarchies = false;

    @ApiModelProperty(name = PARAM_OPTIONS,
            value = "For advanced options formatted as json object. For structure refer to the [these examples](https://github.com/GIScience/openrouteservice-docs#examples).",
            example = "{\"maximum_speed\": 100}")
    @JsonProperty(PARAM_OPTIONS)
    private RouteRequestOptions routeOptions;
    @JsonIgnore
    private boolean hasRouteOptions = false;

    @ApiModelProperty(name = PARAM_SUPPRESS_WARNINGS,
            value = "Suppress warning messages in the response",
            example = "false")
    @JsonProperty(PARAM_SUPPRESS_WARNINGS)
    private Boolean suppressWarnings;
    @JsonIgnore
    private boolean hasSuppressWarnings = false;

    @ApiModelProperty(name = PARAM_SIMPLIFY_GEOMETRY, value = "Specifies whether to simplify the geometry. " +
            "Simplify geometry cannot be applied to routes with more than **one segment** and when `extra_info` is required." +
            "CUSTOM_KEYS:{'apiDefault':false}",
            example = "false")
    @JsonProperty(value = PARAM_SIMPLIFY_GEOMETRY)
    private Boolean simplifyGeometry;
    @JsonIgnore
    private boolean hasSimplifyGeometry = false;

    @ApiModelProperty(name = PARAM_SKIP_SEGMENTS, value = "Specifies the segments that should be skipped in the route calculation. " +
            "A segment is the connection between two given coordinates and the counting starts with 1 for the connection between the first and second coordinate.",
            example = "[2,4]")
    @JsonProperty(PARAM_SKIP_SEGMENTS)
    private List<Integer> skipSegments;
    @JsonIgnore
    private boolean hasSkipSegments = false;

    @JsonCreator
    public RouteRequest(@JsonProperty(value = PARAM_COORDINATES, required = true) List<List<Double>> coordinates) {
        this.coordinates = coordinates;
    }

    public RouteRequest(Double[][] coordinates) throws ParameterValueException {
        if (coordinates.length < 2)
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, PARAM_COORDINATES);

        this.coordinates = new ArrayList<>();
        for (Double[] coordPair : coordinates) {
            if (coordPair.length != 2)
                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, PARAM_COORDINATES);
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
        hasUnits = true;
    }

    public APIEnums.Languages getLanguage() {
        return language;
    }

    public void setLanguage(APIEnums.Languages language) {
        this.language = language;
        hasLanguage = true;
    }

    public APIEnums.RoutePreference getRoutePreference() {
        return routePreference;
    }

    public void setRoutePreference(APIEnums.RoutePreference routePreference) {
        this.routePreference = routePreference;
        this.hasRoutePreference = true;
    }

    public boolean hasRoutePreference() {
        return hasRoutePreference;
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
        this.hasIncludeGeometry = true;
    }

    public Boolean getIncludeInstructionsInResponse() {
        return includeInstructionsInResponse;
    }

    public void setIncludeInstructionsInResponse(Boolean includeInstructionsInResponse) {
        this.includeInstructionsInResponse = includeInstructionsInResponse;
        hasIncludeInstructions = true;
    }

    public APIEnums.InstructionsFormat getInstructionsFormat() {
        return instructionsFormat;
    }

    public void setInstructionsFormat(APIEnums.InstructionsFormat instructionsFormat) {
        this.instructionsFormat = instructionsFormat;
        hasInstructionsFormat = true;
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

    public Boolean getIncludeManeuvers() {
        return includeManeuvers;
    }

    public void setIncludeManeuvers(Boolean includeManeuvers) {
        this.includeManeuvers = includeManeuvers;
        hasIncludeManeuvers = true;
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
        hasContinueStraightAtWaypoints = true;
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

    public Boolean getSimplifyGeometry() {
        return simplifyGeometry;
    }

    public void setSimplifyGeometry(boolean simplifyGeometry) {
        this.simplifyGeometry = simplifyGeometry;
        this.hasSimplifyGeometry = true;
    }

    public List<Integer> getSkipSegments(){
        return this.skipSegments;
    }

    public void setSkipSegments(List<Integer> skipSegments){
        this.skipSegments = skipSegments;
        hasSkipSegments = true;
    }

    public boolean hasIncludeRoundaboutExitInfo() {
        return hasIncludeRoundaboutExitInfo;
    }

    public boolean hasAttributes() {
        return hasAttributes;
    }

    public boolean hasIncludeManeuvers() {
        return hasIncludeManeuvers;
    }

    public boolean hasMaximumSearchRadii() {
        return hasMaximumSearchRadii;
    }

    public boolean hasBearings() {
        return hasBearings;
    }

    public boolean hasContinueStraightAtWaypoints() {
        return hasContinueStraightAtWaypoints;
    }

    public boolean hasIncludeInstructions() {
        return hasIncludeInstructions;
    }

    public boolean hasIncludeGeometry() {
        return hasIncludeGeometry;
    }

    public boolean hasLanguage() {
        return hasLanguage;
    }

    public boolean hasInstructionsFormat() {
        return hasInstructionsFormat;
    }

    public boolean hasUnits() {
        return hasUnits;
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

    public boolean hasSimplifyGeometry() {
        return hasSimplifyGeometry;
    }

    public boolean hasSkipSegments() { return hasSkipSegments;}
}
