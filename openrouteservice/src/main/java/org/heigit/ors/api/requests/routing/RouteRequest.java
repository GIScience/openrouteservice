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
import com.vividsolutions.jts.geom.Coordinate;
import org.heigit.ors.api.requests.common.APIEnums;
import org.heigit.ors.api.requests.common.APIRequest;
import org.heigit.ors.common.StatusCode;
import org.heigit.ors.config.AppConfig;
import org.heigit.ors.exceptions.*;
import org.heigit.ors.localization.LocalizationManager;
import org.heigit.ors.routing.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.heigit.ors.util.StringUtility;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@ApiModel(value = "Directions Service", description = "The JSON body request sent to the routing service which defines options and parameters regarding the route to generate.")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RouteRequest extends APIRequest {
    public static final String PARAM_COORDINATES = "coordinates";
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
    public static final String PARAM_ALTERNATIVE_ROUTES = "alternative_routes";
    public static final String PARAM_DEPARTURE = "departure";
    public static final String PARAM_ARRIVAL = "arrival";
    public static final String PARAM_MAXIMUM_SPEED = "maximum_speed";

    @ApiModelProperty(name = PARAM_COORDINATES, value = "The waypoints to use for the route as an array of `longitude/latitude` pairs",
            example = "[[8.681495,49.41461],[8.686507,49.41943],[8.687872,49.420318]]",
            required = true)
    @JsonProperty(PARAM_COORDINATES)
    private List<List<Double>> coordinates;

    @ApiModelProperty(name = PARAM_PREFERENCE,
            value = "Specifies the route preference. CUSTOM_KEYS:{'apiDefault':'recommended'}",
            example = "recommended")
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
    private boolean includeGeometry;
    @JsonIgnore
    private boolean hasIncludeGeometry = false;

    @ApiModelProperty(name = PARAM_INSTRUCTIONS,
            value = "Specifies whether to return instructions. CUSTOM_KEYS:{'apiDefault':true}",
            example = "true")
    @JsonProperty(value = PARAM_INSTRUCTIONS)
    private boolean includeInstructionsInResponse;
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
    private boolean includeRoundaboutExitInfo;
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
    private boolean includeManeuvers;
    @JsonIgnore
    private boolean hasIncludeManeuvers = false;

    @ApiModelProperty(name = PARAM_RADII, value = "A list of maximum distances (measured in metres) that limit the search of nearby road segments to every given waypoint. " +
            "The values must be greater than 0, the value of -1 specifies using the maximum possible search radius. The number of radiuses correspond to the number of waypoints. If only a single value is given, it will be applied to all waypoints.",
            example = "[200, -1, 30]")
    @JsonProperty(PARAM_RADII)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
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
            value = "Forces the route to keep going straight at waypoints restricting uturns there even if it would be faster." +
            "CUSTOM_KEYS:{'apiDefault':'false'}",
            example = "false")
    @JsonProperty(value = PARAM_CONTINUE_STRAIGHT)
    private boolean continueStraightAtWaypoints;
    @JsonIgnore
    private boolean hasContinueStraightAtWaypoints = false;

    @ApiModelProperty(name = PARAM_ELEVATION,
            value = "Specifies whether to return elevation values for points. Please note that elevation also gets encoded for json response encoded polyline.",
            example = "false")
    @JsonProperty(value = PARAM_ELEVATION)
    private boolean useElevation;
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
    private boolean useContractionHierarchies;
    @JsonIgnore
    private boolean hasUseContractionHierarchies = false;

    @ApiModelProperty(name = PARAM_OPTIONS,
            value = "For advanced options formatted as json object. For structure refer to the [these examples](https://GIScience.github.io/openrouteservice/documentation/routing-options/Examples.html).",
            example = "{\"avoid_borders\":\"controlled\"}")
    @JsonProperty(PARAM_OPTIONS)
    private RouteRequestOptions routeOptions;
    @JsonIgnore
    private boolean hasRouteOptions = false;

    @ApiModelProperty(name = PARAM_SUPPRESS_WARNINGS,
            value = "Suppress warning messages in the response",
            example = "false")
    @JsonProperty(PARAM_SUPPRESS_WARNINGS)
    private boolean suppressWarnings;
    @JsonIgnore
    private boolean hasSuppressWarnings = false;

    @ApiModelProperty(name = PARAM_SIMPLIFY_GEOMETRY, value = "Specifies whether to simplify the geometry. " +
            "Simplify geometry cannot be applied to routes with more than **one segment** and when `extra_info` is required." +
            "CUSTOM_KEYS:{'apiDefault':false}",
            example = "false")
    @JsonProperty(value = PARAM_SIMPLIFY_GEOMETRY)
    private boolean simplifyGeometry;
    @JsonIgnore
    private boolean hasSimplifyGeometry = false;

    @ApiModelProperty(name = PARAM_SKIP_SEGMENTS, value = "Specifies the segments that should be skipped in the route calculation. " +
            "A segment is the connection between two given coordinates and the counting starts with 1 for the connection between the first and second coordinate.",
            example = "[2,4]")
    @JsonProperty(PARAM_SKIP_SEGMENTS)
    private List<Integer> skipSegments;
    @JsonIgnore
    private boolean hasSkipSegments = false;

    @ApiModelProperty(name = PARAM_ALTERNATIVE_ROUTES,
            value = "Specifies whether alternative routes are computed, and parameters for the algorithm determining suitable alternatives.",
            example = "{\"target_count\":2,\"weight_factor\":1.6}")
    @JsonProperty(PARAM_ALTERNATIVE_ROUTES)
    private RouteRequestAlternativeRoutes alternativeRoutes;
    @JsonIgnore
    private boolean hasAlternativeRoutes = false;

    @ApiModelProperty(name = PARAM_DEPARTURE, value = "Departure date and time provided in local time zone" +
            "CUSTOM_KEYS:{'validWhen':{'ref':'arrival','valueNot':['*']}}",
            example = "2020-01-31T12:45:00",  hidden = true)
    @JsonProperty(PARAM_DEPARTURE)
    private LocalDateTime departure;
    @JsonIgnore
    private boolean hasDeparture = false;

    @ApiModelProperty(name = PARAM_ARRIVAL, value = "Arrival date and time provided in local time zone" +
            "CUSTOM_KEYS:{'validWhen':{'ref':'departure','valueNot':['*']}}",
            example = "2020-01-31T13:15:00",  hidden = true)
    @JsonProperty(PARAM_ARRIVAL)
    private LocalDateTime arrival;
    @JsonIgnore
    private boolean hasArrival = false;

@ApiModelProperty(name = PARAM_MAXIMUM_SPEED, value = "The maximum speed specified by user." +
            "CUSTOM_KEYS:{'validWhen':{'ref':'profile','value':['driving-*']}}",
            example = "90")
    @JsonProperty(PARAM_MAXIMUM_SPEED)
    private double maximumSpeed;
    @JsonIgnore
    private boolean hasMaximumSpeed = false;

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

    public List<Integer> getSkipSegments(){
        return this.skipSegments;
    }

    public void setSkipSegments(List<Integer> skipSegments){
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

    public boolean hasSkipSegments() { return hasSkipSegments;}

    public boolean hasAlternativeRoutes() { return hasAlternativeRoutes; }

    public boolean hasDeparture() { return hasDeparture; }

    public boolean hasArrival() { return hasArrival; }

    public boolean hasMaximumSpeed() {
        return hasMaximumSpeed;
    }

    public RouteResult[] generateRouteFromRequest() throws StatusCodeException {
        RoutingRequest routingRequest = this.convertRouteRequest();

        try {
            return RoutingProfileManager.getInstance().computeRoute(routingRequest);
        } catch (StatusCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new StatusCodeException(StatusCode.INTERNAL_SERVER_ERROR, RoutingErrorCodes.UNKNOWN);
        }
    }

    public RoutingRequest convertRouteRequest() throws StatusCodeException {
        RoutingRequest routingRequest = new RoutingRequest();
        boolean isRoundTrip = this.hasRouteOptions() && routeOptions.hasRoundTripOptions();
        routingRequest.setCoordinates(convertCoordinates(coordinates, isRoundTrip));
        routingRequest.setGeometryFormat(convertGeometryFormat(responseType));

        if (this.hasUseElevation())
            routingRequest.setIncludeElevation(useElevation);

        if (this.hasContinueStraightAtWaypoints())
            routingRequest.setContinueStraight(continueStraightAtWaypoints);

        if (this.hasIncludeGeometry())
            routingRequest.setIncludeGeometry(this.convertIncludeGeometry());

        if (this.hasIncludeManeuvers())
            routingRequest.setIncludeManeuvers(includeManeuvers);

        if (this.hasIncludeInstructions())
            routingRequest.setIncludeInstructions(includeInstructionsInResponse);

        if (this.hasIncludeRoundaboutExitInfo())
            routingRequest.setIncludeRoundaboutExits(includeRoundaboutExitInfo);

        if (this.hasAttributes())
            routingRequest.setAttributes(convertAttributes(attributes));

        if (this.hasExtraInfo()) {
            routingRequest.setExtraInfo(convertExtraInfo(extraInfo));
            for (APIEnums.ExtraInfo extra : extraInfo) {
                if (extra.compareTo(APIEnums.ExtraInfo.COUNTRY_INFO) == 0) {
                    routingRequest.setIncludeCountryInfo(true);
                }
            }
        }
        if (this.hasLanguage())
            routingRequest.setLanguage(convertLanguage(language));

        if (this.hasInstructionsFormat())
            routingRequest.setInstructionsFormat(convertInstructionsFormat(instructionsFormat));

        if (this.hasUnits())
            routingRequest.setUnits(convertUnits(units));

        if (this.hasSimplifyGeometry()) {
            routingRequest.setGeometrySimplify(simplifyGeometry);
            if (this.hasExtraInfo() && simplifyGeometry) {
                throw new IncompatibleParameterException(RoutingErrorCodes.INCOMPATIBLE_PARAMETERS, RouteRequest.PARAM_SIMPLIFY_GEOMETRY, "true", RouteRequest.PARAM_EXTRA_INFO, "*");
            }
        }

        if (this.hasSkipSegments()) {
            routingRequest.setSkipSegments(this.processSkipSegments());
        }

        if (this.hasId())
            routingRequest.setId(id);

        if (this.hasMaximumSpeed()) {
            routingRequest.setMaximumSpeed(maximumSpeed);
        }

        int profileType = -1;

        int coordinatesLength = coordinates.size();

        RouteSearchParameters params = new RouteSearchParameters();

        if (this.hasExtraInfo()) {
            routingRequest.setExtraInfo(convertExtraInfo(extraInfo));
            params.setExtraInfo(convertExtraInfo(extraInfo));
        }

        if (this.hasSuppressWarnings())
            params.setSuppressWarnings(suppressWarnings);

        try {
            profileType = convertRouteProfileType(profile);
            params.setProfileType(profileType);
        } catch (Exception e) {
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_PROFILE);
        }

        if (this.hasRoutePreference())
            params.setWeightingMethod(convertWeightingMethod(routePreference));

        if (this.hasBearings())
            params.setBearings(convertBearings(bearings, coordinatesLength));

        if (this.hasContinueStraightAtWaypoints())
            params.setContinueStraight(continueStraightAtWaypoints);

        if (this.hasMaximumSearchRadii())
            params.setMaximumRadiuses(convertMaxRadii(maximumSearchRadii, coordinatesLength, profileType));

        if (this.hasUseContractionHierarchies()) {
            params.setFlexibleMode(convertSetFlexibleMode(useContractionHierarchies));
            params.setOptimized(useContractionHierarchies);
        }

        if (this.hasRouteOptions()) {
            params = this.processRouteRequestOptions(params);
        }

        if (this.hasAlternativeRoutes()) {
            if (coordinates.size() > 2) {
                throw new IncompatibleParameterException(RoutingErrorCodes.INCOMPATIBLE_PARAMETERS, RouteRequest.PARAM_ALTERNATIVE_ROUTES, "(number of waypoints > 2)");
            }
            if (alternativeRoutes.hasTargetCount()) {
                params.setAlternativeRoutesCount(alternativeRoutes.getTargetCount());
                String paramMaxAlternativeRoutesCount = AppConfig.getGlobal().getRoutingProfileParameter(profile.toString(), "maximum_alternative_routes");
                int countLimit = StringUtility.isNullOrEmpty(paramMaxAlternativeRoutesCount) ? 0 : Integer.parseInt(paramMaxAlternativeRoutesCount);
                if (countLimit > 0 && alternativeRoutes.getTargetCount() > countLimit) {
                    throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_ALTERNATIVE_ROUTES, Integer.toString(alternativeRoutes.getTargetCount()), "The target alternative routes count has to be equal to or less than " + paramMaxAlternativeRoutesCount);
                }
            }
            if (alternativeRoutes.hasWeightFactor())
                params.setAlternativeRoutesWeightFactor(alternativeRoutes.getWeightFactor());
            if (alternativeRoutes.hasShareFactor())
                params.setAlternativeRoutesShareFactor(alternativeRoutes.getShareFactor());
        }

        if (this.hasDeparture() && this.hasArrival())
            throw new IncompatibleParameterException(RoutingErrorCodes.INCOMPATIBLE_PARAMETERS, RouteRequest.PARAM_DEPARTURE, RouteRequest.PARAM_ARRIVAL);
        else if (this.hasDeparture())
            params.setDeparture(departure);
        else if (this.hasArrival())
            params.setArrival(arrival);

        if (this.hasMaximumSpeed()) {
            params.setMaximumSpeed(maximumSpeed);
        }

        params.setConsiderTurnRestrictions(false);

        routingRequest.setSearchParameters(params);

        return routingRequest;
    }

    private List<Integer> processSkipSegments() throws ParameterOutOfRangeException, ParameterValueException, EmptyElementException {
        for (Integer skipSegment : skipSegments) {
            if (skipSegment >= coordinates.size()) {
                throw new ParameterOutOfRangeException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_SKIP_SEGMENTS, skipSegment.toString(), String.valueOf(coordinates.size() - 1));
            }
            if (skipSegment <= 0) {
                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_SKIP_SEGMENTS, skipSegments.toString(), "The individual skip_segments values have to be greater than 0.");
            }

        }
        if (skipSegments.size() > coordinates.size() - 1) {
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_SKIP_SEGMENTS, skipSegments.toString(), "The amount of segments to skip shouldn't be more than segments in the coordinates.");
        }
        if (skipSegments.isEmpty()) {
            throw new EmptyElementException(RoutingErrorCodes.EMPTY_ELEMENT, RouteRequest.PARAM_SKIP_SEGMENTS);
        }
        return skipSegments;
    }

    private RouteSearchParameters processRouteRequestOptions(RouteSearchParameters params) throws StatusCodeException {
        params = processRequestOptions(routeOptions, params);
        if (routeOptions.hasProfileParams())
            params.setProfileParams(convertParameters(routeOptions, params.getProfileType()));

        if (routeOptions.hasVehicleType())
            params.setVehicleType(convertVehicleType(routeOptions.getVehicleType(), params.getProfileType()));

        if (routeOptions.hasRoundTripOptions()) {
            RouteRequestRoundTripOptions roundTripOptions = routeOptions.getRoundTripOptions();
            if (roundTripOptions.hasLength()) {
                params.setRoundTripLength(roundTripOptions.getLength());
            } else {
                throw new MissingParameterException(RoutingErrorCodes.MISSING_PARAMETER, RouteRequestRoundTripOptions.PARAM_LENGTH);
            }
            if (roundTripOptions.hasPoints()) {
                params.setRoundTripPoints(roundTripOptions.getPoints());
            }
            if (roundTripOptions.hasSeed()) {
                params.setRoundTripSeed(roundTripOptions.getSeed());
            }
        }
        return params;
    }

    // TODO: can this be merged with processRequestOptions in MatrixRequestHandler?

    private boolean convertIncludeGeometry() throws IncompatibleParameterException {
        if (!includeGeometry && responseType != APIEnums.RouteResponseType.JSON) {
            throw new IncompatibleParameterException(RoutingErrorCodes.INVALID_PARAMETER_VALUE,
                    RouteRequest.PARAM_GEOMETRY, "false",
                    RouteRequest.PARAM_FORMAT, APIEnums.RouteResponseType.GEOJSON + "/" + APIEnums.RouteResponseType.GPX);
        }
        return includeGeometry;
    }

    private String convertGeometryFormat(APIEnums.RouteResponseType responseType) throws ParameterValueException {
        switch (responseType) {
            case GEOJSON:
                return "geojson";
            case JSON:
                return "encodedpolyline";
            case GPX:
                return "gpx";
            default:
                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_FORMAT);
        }
    }

    private Coordinate[] convertCoordinates(List<List<Double>> coordinates, boolean allowSingleCoordinate) throws ParameterValueException {
        if (!allowSingleCoordinate && coordinates.size() < 2)
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_COORDINATES);

        if (allowSingleCoordinate && coordinates.size() > 1)
            throw new ParameterValueException(
                    RoutingErrorCodes.INVALID_PARAMETER_VALUE,
                    RouteRequest.PARAM_COORDINATES,
                    "Length = " + coordinates.size(),
                    "Only one coordinate pair is allowed");

        ArrayList<Coordinate> coords = new ArrayList<>();

        for (List<Double> coord : coordinates) {
            coords.add(convertSingleCoordinate(coord));
        }

        return coords.toArray(new Coordinate[coords.size()]);
    }

    private Coordinate convertSingleCoordinate(List<Double> coordinate) throws ParameterValueException {
        if (coordinate.size() != 2)
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_COORDINATES);

        return new Coordinate(coordinate.get(0), coordinate.get(1));
    }

    private WayPointBearing[] convertBearings(Double[][] bearingsIn, int coordinatesLength) throws ParameterValueException {
        if (bearingsIn == null || bearingsIn.length == 0)
            return new WayPointBearing[0];

        if (bearingsIn.length != coordinatesLength && bearingsIn.length != coordinatesLength - 1)
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_BEARINGS, Arrays.toString(bearingsIn), "The number of bearings must be equal to the number of waypoints on the route.");

        WayPointBearing[] bearingsList = new WayPointBearing[coordinatesLength];
        for (int i = 0; i < bearingsIn.length; i++) {
            Double[] singleBearingIn = bearingsIn[i];

            if (singleBearingIn.length == 0) {
                bearingsList[i] = new WayPointBearing(Double.NaN, Double.NaN);
            } else if (singleBearingIn.length == 1) {
                bearingsList[i] = new WayPointBearing(singleBearingIn[0], Double.NaN);
            } else {
                bearingsList[i] = new WayPointBearing(singleBearingIn[0], singleBearingIn[1]);
            }
        }

        return bearingsList;
    }

    private double[] convertMaxRadii(Double[] radiiIn, int coordinatesLength, int profileType) throws ParameterValueException {
        if (radiiIn != null) {
            if (radiiIn.length == 1) {
                double[] maxRadii = new double[coordinatesLength];
                Arrays.fill(maxRadii, radiiIn[0]);
                return maxRadii;
            }
            if (radiiIn.length != coordinatesLength)
                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_RADII, Arrays.toString(radiiIn), "The number of specified radiuses must be one or equal to the number of specified waypoints.");
            return Stream.of(radiiIn).mapToDouble(Double::doubleValue).toArray();
        } else if (profileType == RoutingProfileType.WHEELCHAIR) {
            // As there are generally less ways that can be used as pedestrian ways, we need to restrict search
            // radii else we end up with starting and ending ways really far from the actual points. This is
            // especially a problem for wheelchair users as the restrictions are stricter
            double[] maxRadii = new double[coordinatesLength];
            Arrays.fill(maxRadii, 50);
            return maxRadii;
        } else {
            return new double[0];
        }
    }

    private static String[] convertAttributes(APIEnums.Attributes[] attributes) {
        return convertAPIEnumListToStrings(attributes);
    }

    private static int convertExtraInfo(APIEnums.ExtraInfo[] extraInfos) {
        String[] extraInfosStrings = convertAPIEnumListToStrings(extraInfos);

        String extraInfoPiped = String.join("|", extraInfosStrings);

        return RouteExtraInfoFlag.getFromString(extraInfoPiped);
    }

    private String convertLanguage(APIEnums.Languages languageIn) throws StatusCodeException {
        boolean isLanguageSupported;
        String languageString = languageIn.toString();

        try {
            isLanguageSupported = LocalizationManager.getInstance().isLanguageSupported(languageString);
        } catch (Exception e) {
            throw new InternalServerException(RoutingErrorCodes.UNKNOWN, "Could not access Localization Manager");
        }

        if (!isLanguageSupported)
            throw new StatusCodeException(StatusCode.BAD_REQUEST, RoutingErrorCodes.INVALID_PARAMETER_VALUE, "Specified language '" + languageIn + "' is not supported.");

        return languageString;
    }

    private RouteInstructionsFormat convertInstructionsFormat(APIEnums.InstructionsFormat formatIn) throws UnknownParameterValueException {
        RouteInstructionsFormat instrFormat = RouteInstructionsFormat.fromString(formatIn.toString());
        if (instrFormat == RouteInstructionsFormat.UNKNOWN)
            throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_INSTRUCTIONS_FORMAT, formatIn.toString());

        return instrFormat;
    }

    private int convertWeightingMethod(APIEnums.RoutePreference preferenceIn) throws UnknownParameterValueException {
        int weightingMethod = WeightingMethod.getFromString(preferenceIn.toString());
        if (weightingMethod == WeightingMethod.UNKNOWN)
            throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_PREFERENCE, preferenceIn.toString());

        return weightingMethod;
    }

    private boolean convertSetFlexibleMode(boolean useContractionHierarchies) throws ParameterValueException {
        if (useContractionHierarchies)
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, RouteRequest.PARAM_OPTIMIZED);

        return true;
    }
}
