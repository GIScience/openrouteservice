package heigit.ors.api.requests.routing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;
import io.swagger.annotations.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;

@ApiModel(value = "Route Request", description = "The JSON body request sent to the routing service which defines options and parameters regarding the route to generate.")
@ApiImplicitParams({
        @ApiImplicitParam(name="start", required=true, value = "The starting location of the route")
})
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RouteRequest {
    @ApiModelProperty(value = "The start location of the route", required = true, position = 0, example = "[ 8.34234, 48.23424 ]")
    private Double[] start;
    @ApiModelProperty(value = "The destination location of the route", required = true, position = 1, example = "[ 8.34423, 48.26424 ]")
    private Double[] end;

    @ApiModelProperty(value = "Via points to use in the route", position = 2, example = "[ [ 8.3435, 48.24645 ], [ 8.3487, 48.27451 ] ]")
    private Double[][] via;

    @ApiModelProperty(hidden = true)
    private APIRoutingEnums.RoutingProfile profile;

    @ApiModelProperty(name = "preference",
            value = "Specifies the route preference.\n" +
                    "Default: fastest.")
    @JsonProperty(value = "preference", defaultValue = "fastest")
    private APIRoutingEnums.RoutePreference routePreference = APIRoutingEnums.RoutePreference.FASTEST;

    @ApiModelProperty(name = "format",
            value = "Specifies the response format. The GPX schema the response is validated against can be found here.\n" +
                    "Default: json.")
    @JsonProperty(value = "format", defaultValue = "json")
    private APIRoutingEnums.RouteResponseType responseType = APIRoutingEnums.RouteResponseType.JSON;

    @ApiModelProperty(name = "units",
            value = "Specifies the distance unit.\n" +
                    "Default: m.")
    @JsonProperty(value = "units", defaultValue = "m")
    private APIRoutingEnums.Units units = APIRoutingEnums.Units.METRES;

    @ApiModelProperty(name = "language",
            value = "Language for the route instructions.\n" +
                    "Default: en.")
    @JsonProperty(value = "language", defaultValue = "en")
    private APIRoutingEnums.Languages language = APIRoutingEnums.Languages.EN;

    @ApiModelProperty(name = "geometry",
            value = "Specifies whether to return geometry.\n" +
                    "Default: true.")
    @JsonProperty(value = "geometry", defaultValue = "true")
    private Boolean includeGeometry = true;

    @ApiModelProperty(name = "geometry_format",
            value = "Sets the format of the returned geometry. Note that for elevation=true encodedpolyline also encodes the height information of each point. " +
                    "If format=geojson, this parameter has no effect. To decode, please use a suitable library (for example graphhopper).\n" +
                    "Default: encodedpolyline.")
    @JsonProperty(value = "geometry_format", defaultValue = "encodedpolyline")
    private APIRoutingEnums.RouteResponseGeometryType geometryType = APIRoutingEnums.RouteResponseGeometryType.ENCODED_POLYLINE;

    @ApiModelProperty(name = "geometry_simplify",
            value = "Specifies whether to simplify the geometry. true will automatically be set to false if extra_info parameter is specified."
    )
    @JsonProperty(value = "geometry_simplify")
    private Boolean simplifyGeometry;
    @JsonIgnore
    private boolean hasSimplifyGeography = false;

    @ApiModelProperty(name = "instructions",
            value = "Specifies whether to return instructions.\n" +
                    "Default: true.")
    @JsonProperty(value = "instructions", defaultValue = "true")
    private Boolean includeInstructionsInResponse = true;

    @ApiModelProperty(name = "instructions_format",
            value = "Select html for more verbose instructions.\n" +
                    "Default: text.")
    @JsonProperty(value = "instructions_format", defaultValue = "text")
    private APIRoutingEnums.InstructionsFormat instructionsFormat = APIRoutingEnums.InstructionsFormat.TEXT;

    @ApiModelProperty(name = "roundabout_exits",
            value = "Provides bearings of the entrance and all passed roundabout exits. Adds the exit_bearings array to the step object in the response.")
    @JsonProperty("roundabout_exits")
    private Boolean includeRoundaboutExitInfo;
    @JsonIgnore
    private boolean hasIncludeRoundaboutExitInfo = false;

    @ApiModelProperty(name = "attributes", value = "List of route attributes")
    @JsonProperty("attributes")
    private APIRoutingEnums.Attributes[] attributes;
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
            "You can skip a bearing for a certain waypoint by passing an empty value for a pair, e.g. 30,20||40,20.",
            example = "[ [ 30, 20 ], [ -999, -999 ], [ 40, 20 ] ]"
    )
    @JsonProperty("bearings")
    private Double[][] bearings;
    @JsonIgnore
    private boolean hasBearings = false;

    @ApiModelProperty(name = "continue_straight", value = "Forces the route to keep going straight at waypoints restricting uturns there even if it would be faster. This setting will work for all profiles except for driving-*. " +
            "In this case you will have to set optimized=false for it to work. Default: false.")
    @JsonProperty(value = "continue_straight", defaultValue = "false")
    private Boolean continueStraightAtWaypoints = false;

    @ApiModelProperty(name = "elevation", value = "Specifies whether to return elevation values for points. Please note that elevation also gets encoded for geometry_format=encodedpolyline.")
    @JsonProperty("elevation")
    private Boolean returnElevationForPoints;
    @JsonIgnore
    private boolean hasReturnElevationForPoints = false;

    @ApiModelProperty(name = "extra_info",
            value = "The extra info items to include in the response")
    @JsonProperty("extra_info")
    private APIRoutingEnums.ExtraInfo[] extraInfo;
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

    @JsonCreator
    public RouteRequest(
            @JsonProperty(value = "start", required = true) Double[] start,
            @JsonProperty(value = "end", required = true) Double[] end) {
        this.start = start;
        this.end = end;
    }

    public Double[] getStart() {
        return start;
    }

    public void setStart(Double[] start) {
        this.start = start;
    }

    public Double[] getEnd() {
        return end;
    }

    public void setEnd(Double[] end) {
        this.end = end;
    }

    public Double[][] getVia() {
        return via;
    }

    public void setVia(Double[][] via) {
        this.via = via;
    }

    public APIRoutingEnums.RoutingProfile getProfile() {
        return profile;
    }

    public void setProfile(APIRoutingEnums.RoutingProfile profile) {
        this.profile = profile;
    }

    public APIRoutingEnums.Units getUnits() {
        return units;
    }

    public void setUnits(APIRoutingEnums.Units units) {
        this.units = units;
    }

    public APIRoutingEnums.Languages getLanguage() {
        return language;
    }

    public void setLanguage(APIRoutingEnums.Languages language) {
        this.language = language;
    }

    public APIRoutingEnums.RoutePreference getRoutePreference() {
        return routePreference;
    }

    public void setRoutePreference(APIRoutingEnums.RoutePreference routePreference) {
        this.routePreference = routePreference;
    }

    public APIRoutingEnums.RouteResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(APIRoutingEnums.RouteResponseType responseType) {
        this.responseType = responseType;
    }

    public APIRoutingEnums.RouteResponseGeometryType getGeometryType() {
        return geometryType;
    }

    public void setGeometryType(APIRoutingEnums.RouteResponseGeometryType geometryType) {
        this.geometryType = geometryType;
    }

    public APIRoutingEnums.ExtraInfo[] getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(APIRoutingEnums.ExtraInfo[] extraInfo) {
        this.extraInfo = extraInfo;
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

    public Boolean getSimplifyGeometry() {
        return simplifyGeometry;
    }

    public void setSimplifyGeometry(Boolean simplifyGeometry) {
        this.simplifyGeometry = simplifyGeometry;
        hasSimplifyGeography = true;
    }

    public Boolean getIncludeInstructionsInResponse() {
        return includeInstructionsInResponse;
    }

    public void setIncludeInstructionsInResponse(Boolean includeInstructionsInResponse) {
        this.includeInstructionsInResponse = includeInstructionsInResponse;
    }

    public APIRoutingEnums.InstructionsFormat getInstructionsFormat() {
        return instructionsFormat;
    }

    public void setInstructionsFormat(APIRoutingEnums.InstructionsFormat instructionsFormat) {
        this.instructionsFormat = instructionsFormat;
    }

    public Boolean getIncludeRoundaboutExitInfo() {
        return includeRoundaboutExitInfo;
    }

    public void setIncludeRoundaboutExitInfo(Boolean includeRoundaboutExitInfo) {
        this.includeRoundaboutExitInfo = includeRoundaboutExitInfo;
        hasIncludeRoundaboutExitInfo = true;
    }

    public APIRoutingEnums.Attributes[] getAttributes() {
        return attributes;
    }

    public void setAttributes(APIRoutingEnums.Attributes[] attributes) {
        this.attributes = attributes;
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

    public Boolean getReturnElevationForPoints() {
        return returnElevationForPoints;
    }

    public void setReturnElevationForPoints(Boolean returnElevationForPoints) {
        this.returnElevationForPoints = returnElevationForPoints;
        hasReturnElevationForPoints = true;
    }

    public Boolean getUseContractionHierarchies() {
        return useContractionHierarchies;
    }

    public void setUseContractionHierarchies(Boolean useContractionHierarchies) {
        this.useContractionHierarchies = useContractionHierarchies;
        hasUseContractionHierarchies = true;
    }

    public boolean hasSimplifyGeography() {
        return hasSimplifyGeography;
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

    public boolean hasReturnElevationForPoints() {
        return hasReturnElevationForPoints;
    }

    public boolean hasRouteOptions() {
        return hasRouteOptions;
    }

    public boolean hasUseContractionHierarchies() {
        return hasUseContractionHierarchies;
    }


    @ApiIgnore
    @ApiModelProperty(hidden = true)
    @JsonIgnore
    public Coordinate[] getCoordinates() {
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(new Coordinate(start[0], start[1]));
        coordinates.add(new Coordinate(end[0], end[1]));

        return coordinates.toArray(new Coordinate[coordinates.size()]);
    }
}
