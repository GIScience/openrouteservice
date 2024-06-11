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

package org.heigit.ors.api.requests.routing;

import com.fasterxml.jackson.annotation.*;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.heigit.ors.api.requests.common.APIRequest;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.api.APIEnums;
import org.heigit.ors.routing.RouteRequestParameterNames;
import org.heigit.ors.routing.RoutingErrorCodes;
import org.heigit.ors.routing.RoutingProfileType;
import org.locationtech.jts.geom.Coordinate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.heigit.ors.api.services.ApiService.convertRouteProfileType;

@Schema(title = "Directions Service", name = "directionsService", description = "The JSON body request sent to the routing service which defines options and parameters regarding the route to generate.")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RouteRequest extends APIRequest implements RouteRequestParameterNames {

    @Schema(name = PARAM_COORDINATES, description = "The waypoints to use for the route as an array of `longitude/latitude` pairs in WGS 84 (EPSG:4326)",
            example = "[[8.681495,49.41461],[8.686507,49.41943],[8.687872,49.420318]]",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty(PARAM_COORDINATES)
    private List<List<Double>> coordinates;

    //TODO (GTFS): We might need to make a bunch of these parameters only valid if profile pt is selected.

    @Schema(name = PARAM_PREFERENCE,
            description = "Specifies the route preference.",
            defaultValue = "recommended")
    @JsonProperty(value = PARAM_PREFERENCE)
    private APIEnums.RoutePreference routePreference;
    @JsonIgnore
    private boolean hasRoutePreference = false;

    @Schema(name = PARAM_FORMAT, hidden = true)
    @JsonProperty(PARAM_FORMAT)
    private APIEnums.RouteResponseType responseType = APIEnums.RouteResponseType.JSON;

    @Schema(name = PARAM_UNITS,
            description = "Specifies the distance unit.",
            defaultValue = "m")
    @JsonProperty(value = PARAM_UNITS)
    private APIEnums.Units units;
    @JsonIgnore
    private boolean hasUnits = false;

    @Schema(name = PARAM_LANGUAGE,
            description = "Language for the route instructions.",
            defaultValue = "en")
    @JsonProperty(value = PARAM_LANGUAGE)
    private APIEnums.Languages language;
    @JsonIgnore
    private boolean hasLanguage = false;

    @Schema(name = PARAM_GEOMETRY,
            description = "Specifies whether to return geometry. ",
            extensions = {@Extension(name = "validWhen", properties = {
                    @ExtensionProperty(name = "ref", value = "format"),
                    @ExtensionProperty(name = "value", value = "[\"json\"]", parseValue = true)}
            )},
            defaultValue = "true")
    @JsonProperty(value = PARAM_GEOMETRY)
    private boolean includeGeometry;
    @JsonIgnore
    private boolean hasIncludeGeometry = false;

    @Schema(name = PARAM_INSTRUCTIONS,
            description = "Specifies whether to return instructions.",
            defaultValue = "true")
    @JsonProperty(value = PARAM_INSTRUCTIONS)
    private boolean includeInstructionsInResponse;
    @JsonIgnore
    private boolean hasIncludeInstructions = false;

    @Schema(name = PARAM_INSTRUCTIONS_FORMAT,
            description = "Select html for more verbose instructions.",
            defaultValue = "text")
    @JsonProperty(value = PARAM_INSTRUCTIONS_FORMAT)
    private APIEnums.InstructionsFormat instructionsFormat;
    @JsonIgnore
    private boolean hasInstructionsFormat = false;

    @Schema(name = PARAM_ROUNDABOUT_EXITS,
            description = "Provides bearings of the entrance and all passed roundabout exits. Adds the `exit_bearings` array to the step object in the response. ",
            defaultValue = "false")
    @JsonProperty(value = PARAM_ROUNDABOUT_EXITS)
    private boolean includeRoundaboutExitInfo;
    @JsonIgnore
    private boolean hasIncludeRoundaboutExitInfo = false;

    @Schema(name = PARAM_ATTRIBUTES,
            description = "List of route attributes",
            example = "[\"avgspeed\",\"percentage\"]")
    @JsonProperty(PARAM_ATTRIBUTES)
    private APIEnums.Attributes[] attributes;
    @JsonIgnore
    private boolean hasAttributes = false;

    @Schema(name = PARAM_MANEUVERS, description = "Specifies whether the maneuver object is included into the step object or not. ",
            defaultValue = "false")
    @JsonProperty(value = PARAM_MANEUVERS)
    private boolean includeManeuvers;
    @JsonIgnore
    private boolean hasIncludeManeuvers = false;

    @Schema(name = PARAM_RADII, description = """
            A list of maximum distances (measured in metres) that limit the search of nearby road segments to every given waypoint. \
            The values must be greater than 0, the value of -1 specifies using the maximum possible search radius. The number of radiuses correspond to the number of waypoints. If only a single value is given, it will be applied to all waypoints.\
            """,
            example = "[200, -1, 30]")
    @JsonProperty(PARAM_RADII)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private Double[] maximumSearchRadii;
    @JsonIgnore
    private boolean hasMaximumSearchRadii = false;

    @Schema(name = PARAM_BEARINGS, description = """
            Specifies a list of pairs (bearings and deviations) to filter the segments of the road network a waypoint can snap to.
            "For example `bearings=[[45,10],[120,20]]`.
            "Each pair is a comma-separated list that can consist of one or two float values, where the first value is the bearing and the second one is the allowed deviation from the bearing.
            "The bearing can take values between `0` and `360` clockwise from true north. If the deviation is not set, then the default value of `100` degrees is used.
            "The number of pairs must correspond to the number of waypoints.
            "The number of bearings corresponds to the length of waypoints-1 or waypoints. If the bearing information for the last waypoint is given, then this will control the sector from which the destination waypoint may be reached.
            "You can skip a bearing for a certain waypoint by passing an empty value for an array, e.g. `[30,20],[],[40,20]`.""",
            example = "[[30, 20], [], [40, 20]]",
            extensions = {@Extension(name = "validWhen", properties = {
                    @ExtensionProperty(name = "ref", value = "profile"),
                    @ExtensionProperty(name = "value", value = "cycling-*")}
            )}
    )
    @JsonProperty(PARAM_BEARINGS)
    private Double[][] bearings;
    @JsonIgnore
    private boolean hasBearings = false;

    @Schema(name = PARAM_CONTINUE_STRAIGHT,
            description = "Forces the route to keep going straight at waypoints restricting uturns there even if it would be faster.",
            defaultValue = "false")
    @JsonProperty(value = PARAM_CONTINUE_STRAIGHT)
    private boolean continueStraightAtWaypoints;
    @JsonIgnore
    private boolean hasContinueStraightAtWaypoints = false;

    @Schema(name = PARAM_ELEVATION,
            description = "Specifies whether to return elevation values for points. Please note that elevation also gets encoded for json response encoded polyline.",
            example = "false")
    @JsonProperty(value = PARAM_ELEVATION)
    private boolean useElevation;
    @JsonIgnore
    private boolean hasUseElevation = false;

    @Schema(name = PARAM_EXTRA_INFO,
            description = "The extra info items to include in the response",
            example = "[\"waytype\",\"surface\"]")
    @JsonProperty(PARAM_EXTRA_INFO)
    private APIEnums.ExtraInfo[] extraInfo;
    @JsonIgnore
    private boolean hasExtraInfo = false;

    @Schema(name = PARAM_OPTIMIZED, description = "Uses contraction hierarchies if available (false). ",
            defaultValue = "true",
            hidden = true)
    @JsonProperty(value = PARAM_OPTIMIZED)
    private boolean useContractionHierarchies;
    @JsonIgnore
    private boolean hasUseContractionHierarchies = false;

    @Schema(name = PARAM_OPTIONS,
            description = "For advanced options formatted as json object. For structure refer to the [these examples](https://giscience.github.io/openrouteservice/api-reference/endpoints/directions/routing-options#examples).",
            example = "{\"avoid_borders\":\"controlled\"}")
    @JsonProperty(PARAM_OPTIONS)
    private RouteRequestOptions routeOptions;
    @JsonIgnore
    private boolean hasRouteOptions = false;

    @Schema(name = PARAM_SUPPRESS_WARNINGS,
            description = "Suppress warning messages in the response",
            example = "false")
    @JsonProperty(PARAM_SUPPRESS_WARNINGS)
    private boolean suppressWarnings;
    @JsonIgnore
    private boolean hasSuppressWarnings = false;

    @Schema(name = PARAM_SIMPLIFY_GEOMETRY, description = """
            Specifies whether to simplify the geometry. \
            Simplify geometry cannot be applied to routes with more than **one segment** and when `extra_info` is required.\
            """,
            defaultValue = "false")
    @JsonProperty(value = PARAM_SIMPLIFY_GEOMETRY)
    private boolean simplifyGeometry;
    @JsonIgnore
    private boolean hasSimplifyGeometry = false;

    @Schema(name = PARAM_SKIP_SEGMENTS, description = """
            Specifies the segments that should be skipped in the route calculation. \
            A segment is the connection between two given coordinates and the counting starts with 1 for the connection between the first and second coordinate.\
            """,
            example = "[2,4]")
    @JsonProperty(PARAM_SKIP_SEGMENTS)
    private List<Integer> skipSegments;
    @JsonIgnore
    private boolean hasSkipSegments = false;

    @Schema(name = PARAM_ALTERNATIVE_ROUTES,
            description = "Specifies whether alternative routes are computed, and parameters for the algorithm determining suitable alternatives.",
            example = "{\"target_count\":2,\"weight_factor\":1.6}")
    @JsonProperty(PARAM_ALTERNATIVE_ROUTES)
    private RouteRequestAlternativeRoutes alternativeRoutes;
    @JsonIgnore
    private boolean hasAlternativeRoutes = false;

    @Schema(name = PARAM_DEPARTURE, description = "Departure date and time provided in local time zone",
            extensions = {@Extension(name = "validWhen", properties = {
                    @ExtensionProperty(name = "ref", value = "format"),
                    @ExtensionProperty(name = "valueNot", value = "[\"*\"]", parseValue = true)}
            )},
            example = "2020-01-31T12:45:00", hidden = true)
    @JsonProperty(PARAM_DEPARTURE)
    private LocalDateTime departure;
    @JsonIgnore
    private boolean hasDeparture = false;

    @Schema(name = PARAM_ARRIVAL, description = "Arrival date and time provided in local time zone",
            extensions = {@Extension(name = "validWhen", properties = {
                    @ExtensionProperty(name = "ref", value = "format"),
                    @ExtensionProperty(name = "valueNot", value = "[\"*\"]", parseValue = true)}
            )},
            example = "2020-01-31T13:15:00", hidden = true)
    @JsonProperty(PARAM_ARRIVAL)
    private LocalDateTime arrival;
    @JsonIgnore
    private boolean hasArrival = false;

    @Schema(name = PARAM_MAXIMUM_SPEED, description = "The maximum speed specified by user.",
            extensions = {@Extension(name = "validWhen", properties = {
                    @ExtensionProperty(name = "ref", value = "profile"),
                    @ExtensionProperty(name = "value", value = "driving-*")}
            )},
            example = "90")
    @JsonProperty(PARAM_MAXIMUM_SPEED)
    private double maximumSpeed;
    @JsonIgnore
    private boolean hasMaximumSpeed = false;

    /*
     * The following parameters are specific to public transport.
     * Other parameters public-transport accepts are coordinates and language.
     */
    @Schema(name = PARAM_SCHEDULE, description = "If true, return a public transport schedule starting at <departure> for the next <schedule_duration> minutes.",
            extensions = {@Extension(name = "validWhen", properties = {
                    @ExtensionProperty(name = "ref", value = "profile"),
                    @ExtensionProperty(name = "value", value = "public-transport")}
            )},
            defaultValue = "false",
            example = "true")
    @JsonProperty(PARAM_SCHEDULE)
    private boolean schedule;
    @JsonIgnore
    private boolean hasSchedule = false;

    @Schema(name = PARAM_SCHEDULE_DURATION, description = "The time window when requesting a public transport schedule." +
            " The format is passed as ISO 8601 duration: https://en.wikipedia.org/wiki/ISO_8601#Durations",
            extensions = {@Extension(name = "validWhen", properties = {
                    @ExtensionProperty(name = "ref", value = "schedule"),
                    @ExtensionProperty(name = "value", value = "true", parseValue = true)}
            )},
            example = "PT30M",
            type = "String")
    @JsonProperty(PARAM_SCHEDULE_DURATION)
    private Duration scheduleDuration;
    @JsonIgnore
    private boolean hasScheduleDuration = false;

    @Schema(name = PARAM_SCHEDULE_ROWS, description = "The maximum amount of entries that should be returned when requesting a schedule.",
            extensions = {@Extension(name = "validWhen", properties = {
                    @ExtensionProperty(name = "ref", value = "schedule"),
                    @ExtensionProperty(name = "value", value = "true", parseValue = true)}
            )},
            example = "3")
    @JsonProperty(PARAM_SCHEDULE_ROWS)
    private int scheduleRows;
    @JsonIgnore
    private boolean hasScheduleRows = false;

    @Schema(name = PARAM_WALKING_TIME, description = "Maximum duration for walking access and egress of public transport." +
            " The value is passed in ISO 8601 duration format: https://en.wikipedia.org/wiki/ISO_8601#Durations",
            extensions = {@Extension(name = "validWhen", properties = {
                    @ExtensionProperty(name = "ref", value = "profile"),
                    @ExtensionProperty(name = "value", value = "public-transport")}
            )},
            defaultValue = "PT15M",
            example = "PT30M",
            type = "String")
    @JsonProperty(PARAM_WALKING_TIME)
    private Duration walkingTime;
    @JsonIgnore
    private boolean hasWalkingTime = false;

    @Schema(name = PARAM_IGNORE_TRANSFERS, description = "Specifies if transfers as criterion should be ignored.",
            extensions = {@Extension(name = "validWhen", properties = {
                    @ExtensionProperty(name = "ref", value = "profile"),
                    @ExtensionProperty(name = "value", value = "public-transport")}
            )},
            defaultValue = "false",
            example = "true")
    @JsonProperty(PARAM_IGNORE_TRANSFERS)
    private boolean ignoreTransfers;
    @JsonIgnore
    private boolean hasIgnoreTransfers = false;

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
        if (start == null) {
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, "start");
        }
        if (end == null) {
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

    public List<List<Double>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<List<Double>> coordinates) {
        this.coordinates = coordinates;
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

    public boolean getIncludeGeometry() {
        return includeGeometry;
    }

    public void setIncludeGeometry(boolean includeGeometry) {
        this.includeGeometry = includeGeometry;
        this.hasIncludeGeometry = true;
    }

    public boolean getIncludeInstructionsInResponse() {
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

    public boolean getIncludeRoundaboutExitInfo() {
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

    public boolean getIncludeManeuvers() {
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

    public boolean getContinueStraightAtWaypoints() {
        return continueStraightAtWaypoints;
    }

    public void setContinueStraightAtWaypoints(Boolean continueStraightAtWaypoints) {
        this.continueStraightAtWaypoints = continueStraightAtWaypoints;
        hasContinueStraightAtWaypoints = true;
    }

    public boolean getUseElevation() {
        return useElevation;
    }

    public void setUseElevation(Boolean useElevation) {
        this.useElevation = useElevation;
        hasUseElevation = true;
    }

    public boolean getUseContractionHierarchies() {
        return useContractionHierarchies;
    }

    public void setUseContractionHierarchies(Boolean useContractionHierarchies) {
        this.useContractionHierarchies = useContractionHierarchies;
        hasUseContractionHierarchies = true;
    }

    public boolean getSuppressWarnings() {
        return suppressWarnings;
    }

    public void setSuppressWarnings(boolean suppressWarnings) {
        this.suppressWarnings = suppressWarnings;
        hasSuppressWarnings = true;
    }

    public boolean getSimplifyGeometry() {
        return simplifyGeometry;
    }

    public void setSimplifyGeometry(boolean simplifyGeometry) {
        this.simplifyGeometry = simplifyGeometry;
        this.hasSimplifyGeometry = true;
    }

    public List<Integer> getSkipSegments() {
        return this.skipSegments;
    }

    public void setSkipSegments(List<Integer> skipSegments) {
        this.skipSegments = skipSegments;
        hasSkipSegments = true;
    }

    public RouteRequestAlternativeRoutes getAlternativeRoutes() {
        return alternativeRoutes;
    }

    public void setAlternativeRoutes(RouteRequestAlternativeRoutes alternativeRoutes) {
        this.alternativeRoutes = alternativeRoutes;
        hasAlternativeRoutes = true;
    }

    public LocalDateTime getDeparture() {
        return departure;
    }

    public void setDeparture(LocalDateTime departure) {
        this.departure = departure;
        hasDeparture = true;
    }

    public LocalDateTime getArrival() {
        return arrival;
    }

    public void setArrival(LocalDateTime arrival) {
        this.arrival = arrival;
        hasArrival = true;
    }

    public void setMaximumSpeed(Double maximumSpeed) {
        this.maximumSpeed = maximumSpeed;
        hasMaximumSpeed = true;
    }

    public double getMaximumSpeed() {
        return maximumSpeed;
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

    public boolean hasSkipSegments() {
        return hasSkipSegments;
    }

    public boolean hasAlternativeRoutes() {
        return hasAlternativeRoutes;
    }

    public boolean hasDeparture() {
        return hasDeparture;
    }

    public boolean hasArrival() {
        return hasArrival;
    }

    public boolean hasMaximumSpeed() {
        return hasMaximumSpeed;
    }

    public boolean getSchedule() {
        return schedule;
    }

    public void setSchedule(boolean schedule) {
        this.schedule = schedule;
        this.hasSchedule = true;
    }

    public boolean hasSchedule() {
        return hasSchedule;
    }

    public Duration getScheduleDuration() {
        return scheduleDuration;
    }

    public void setScheduleDuration(Duration scheduleDuration) {
        this.scheduleDuration = scheduleDuration;
        this.hasScheduleDuration = true;
    }

    public boolean hasScheduleDuration() {
        return hasScheduleDuration;
    }

    public int getScheduleRows() {
        return scheduleRows;
    }

    public void setScheduleRows(int scheduleRows) {
        this.scheduleRows = scheduleRows;
        this.hasScheduleRows = true;
    }

    public boolean hasScheduleRows() {
        return hasScheduleRows;
    }

    public Duration getWalkingTime() {
        return walkingTime;
    }

    public void setWalkingTime(Duration walkingTime) {
        this.walkingTime = walkingTime;
        this.hasWalkingTime = true;
    }

    public boolean hasWalkingTime() {
        return hasWalkingTime;
    }


    public boolean isIgnoreTransfers() {
        return ignoreTransfers;
    }

    public void setIgnoreTransfers(boolean ignoreTransfers) {
        this.ignoreTransfers = ignoreTransfers;
        this.hasIgnoreTransfers = true;
    }

    public boolean hasIgnoreTransfers() {
        return hasIgnoreTransfers;
    }

    @JsonIgnore
    public boolean isPtRequest() {
        return convertRouteProfileType(profile) == RoutingProfileType.PUBLIC_TRANSPORT;
    }

}
