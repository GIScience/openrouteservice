package heigit.ors.controllersOld.DataTransferObjects2;

import com.fasterxml.jackson.annotation.*;
import heigit.ors.routing.RoutingProfileType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RouteRequestDTO{
    @JsonProperty(value = "coordinates")
    private RouteRequestCoordinates coordinates;

    @JsonProperty(value="preference", defaultValue = "fastest")
    private RoutingPreference routingPreference = RoutingPreference.FASTEST;

    @JsonProperty(value="format", defaultValue = "json")
    private RouteResponseType responseType = RouteResponseType.JSON;

    @JsonProperty(value = "geometry_format", defaultValue = "geojson")
    private RouteGeometryFormat geometryFormat = RouteGeometryFormat.GEOJSON;

    @JsonProperty(value="profile")
    private String routingProfileName;

    private RoutingUnits units;

    private Language language;

    @JsonProperty(value = "geometry", defaultValue = "true")
    private boolean includeGeometry = true;

    @JsonProperty(value = "simplify_geometry", defaultValue = "false")
    private boolean simplifyGeometry = false;

    @JsonProperty(defaultValue = "true")
    private boolean instructions = true;

    @JsonProperty(value = "instructions_format", defaultValue = "html")
    private InstructionFormat instructionFormat = InstructionFormat.HTML;

    @JsonProperty(value = "roundabout_exits", defaultValue = "false")
    private boolean useRoundaboutExitNumbers = false;

    @JsonProperty(value = "manouvers", defaultValue = "true")
    private boolean includeManouvers = true;

    private float[][] bearings;

    private float[][] radiuses;

    @JsonProperty(value = "continue_straight", defaultValue = "false")
    private boolean continueStraightAtWaypoints = false;

    @JsonProperty(value = "elevation", defaultValue = "false")
    private boolean useElevation = false;

    @JsonProperty("extra_info")
    private ExtraInfo[] extraInfos;

    @JsonProperty(value = "optimized", defaultValue = "true")
    private boolean optimizeRouteCalculation = true;

    private RouteRequestOptionsDTO options;

    @JsonIgnore
    private int profileId;

    @JsonCreator
    public RouteRequestDTO() {
    }

    public void setOptions(RouteRequestOptionsDTO options) {
        this.options = options;
    }

    public RouteRequestOptionsDTO getOptions() {
        return this.options;
    }

    @JsonSetter("profile")
    public void setRoutingProfile(String routingProfileName) {
        this.routingProfileName = routingProfileName;

        this.profileId = RoutingProfileType.getFromString(routingProfileName);
    }

    public RouteRequestCoordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(RouteRequestCoordinates coordinates) {
        this.coordinates = coordinates;
    }

    public String getRoutingProfileName() {
        return  this.routingProfileName;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public int getProfileId() {
        return this.profileId;
    }

    public void setRoutingPreference(RoutingPreference routingPreference) {
        this.routingPreference = routingPreference;
    }

    public RoutingPreference getRoutingPreference() {
        return this.routingPreference;
    }

    public void setResponseType(RouteResponseType type) {
        this.responseType = type;
    }

    public RouteResponseType getResponseType() {
        return this.responseType;
    }

    public RouteResponseType getType() {
        return this.responseType;
    }

    public void setGeometryFormat(RouteGeometryFormat geometryFormat) {
        this.geometryFormat = geometryFormat;
    }

    public RouteGeometryFormat getGeometryFormat() {
        return geometryFormat;
    }

    public void setUseElevation(boolean useElevation) {
        this.useElevation = useElevation;
    }

    public boolean getUseElevation() {
        return this.useElevation;
    }

    public RoutingUnits getUnits() {
        return units;
    }

    public void setUnits(RoutingUnits units) {
        this.units = units;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public boolean isIncludeGeometry() {
        return includeGeometry;
    }

    public void setIncludeGeometry(boolean includeGeometry) {
        this.includeGeometry = includeGeometry;
    }

    public boolean isSimplifyGeometry() {
        return simplifyGeometry;
    }

    public void setSimplifyGeometry(boolean simplifyGeometry) {
        this.simplifyGeometry = simplifyGeometry;
    }

    public boolean isInstructions() {
        return instructions;
    }

    public void setInstructions(boolean instructions) {
        this.instructions = instructions;
    }

    public InstructionFormat getInstructionFormat() {
        return instructionFormat;
    }

    public void setInstructionFormat(InstructionFormat instructionFormat) {
        this.instructionFormat = instructionFormat;
    }

    public boolean isUseRoundaboutExitNumbers() {
        return useRoundaboutExitNumbers;
    }

    public void setUseRoundaboutExitNumbers(boolean useRoundaboutExitNumbers) {
        this.useRoundaboutExitNumbers = useRoundaboutExitNumbers;
    }

    public boolean isIncludeManouvers() {
        return includeManouvers;
    }

    public void setIncludeManouvers(boolean includeManouvers) {
        this.includeManouvers = includeManouvers;
    }

    public float[][] getBearings() {
        return bearings;
    }

    public void setBearings(float[][] bearings) {
        this.bearings = bearings;
    }

    public float[][] getRadiuses() {
        return radiuses;
    }

    public void setRadiuses(float[][] radiuses) {
        this.radiuses = radiuses;
    }

    public boolean isContinueStraightAtWaypoints() {
        return continueStraightAtWaypoints;
    }

    public void setContinueStraightAtWaypoints(boolean continueStraightAtWaypoints) {
        this.continueStraightAtWaypoints = continueStraightAtWaypoints;
    }

    public ExtraInfo[] getExtraInfos() {
        return extraInfos;
    }

    public void setExtraInfos(ExtraInfo[] extraInfos) {
        this.extraInfos = extraInfos;
    }

    public boolean isOptimizeRouteCalculation() {
        return optimizeRouteCalculation;
    }

    public void setOptimizeRouteCalculation(boolean optimizeRouteCalculation) {
        this.optimizeRouteCalculation = optimizeRouteCalculation;
    }
}


