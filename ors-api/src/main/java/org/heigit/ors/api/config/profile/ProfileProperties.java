package org.heigit.ors.api.config.profile;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.StringUtils;
import org.heigit.ors.api.config.EncoderOptionsProperties;
import org.heigit.ors.api.config.ExecutionProperties;
import org.heigit.ors.api.config.PreparationProperties;

import java.nio.file.Paths;
import java.util.HashMap;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "encoder_name", defaultImpl = DefaultProfileProperties.class)
@JsonSubTypes({
        @JsonSubTypes.Type(name = "default", value = DefaultProfileProperties.class),
        @JsonSubTypes.Type(name = "driving-car", value = CarProfileProperties.class),
        @JsonSubTypes.Type(name = "driving-hgv", value = HgvProfileProperties.class),
        @JsonSubTypes.Type(name = "cycling-regular", value = BikeRegularProfileProperties.class),
        @JsonSubTypes.Type(name = "cycling-electric", value = BikeElectricProfileProperties.class),
        @JsonSubTypes.Type(name = "cycling-mountain", value = BikeMountainProfileProperties.class),
        @JsonSubTypes.Type(name = "cycling-road", value = BikeRoadProfileProperties.class),
        @JsonSubTypes.Type(name = "foot-walking", value = WalkingProfileProperties.class),
        @JsonSubTypes.Type(name = "foot-hiking", value = HikingProfileProperties.class),
        @JsonSubTypes.Type(name = "wheelchair", value = WheelchairProfileProperties.class),
        @JsonSubTypes.Type(name = "public-transport", value = PublicTransportProfileProperties.class),
})
public abstract class ProfileProperties {
    @JsonProperty("enabled")
    private Boolean enabled;
    @JsonProperty("encoder_name")
    private String encoderName;
    @JsonProperty("elevation")
    private Boolean elevation;
    @JsonProperty("elevation_smoothing")
    private Boolean elevationSmoothing;
    @JsonProperty("encoder_flags_size")
    private Integer encoderFlagsSize;
    @JsonProperty("instructions")
    private Boolean instructions;
    @JsonProperty("optimize")
    private Boolean optimize;
    @JsonProperty("traffic")
    private Boolean traffic;
    @JsonProperty("interpolate_bridges_and_tunnels")
    private Boolean interpolateBridgesAndTunnels;
    @JsonProperty("force_turn_costs")
    private Boolean forceTurnCosts;
    @JsonProperty("graph_path")
    private String graphPath;
    @JsonProperty("location_index_resolution")
    private Integer locationIndexResolution;
    @JsonProperty("location_index_search_iterations")
    private Integer locationIndexSearchIterations;
    @JsonProperty("gtfs_file")
    private String gtfsFile;

    @JsonProperty("maximum_distance")
    private Double maximumDistance;
    @JsonProperty("maximum_distance_dynamic_weights")
    private Double maximumDistanceDynamicWeights;
    @JsonProperty("maximum_distance_avoid_areas")
    private Double maximumDistanceAvoidAreas;
    @JsonProperty("maximum_distance_alternative_routes")
    private Double maximumDistanceAlternativeRoutes;
    @JsonProperty("maximum_distance_round_trip_routes")
    private Double maximumDistanceRoundTripRoutes;
    @JsonProperty("maximum_speed_lower_bound")
    private Double maximumSpeedLowerBound;
    @JsonProperty("maximum_way_points")
    private Integer maximumWayPoints;
    @JsonProperty("maximum_snapping_radius")
    private Integer maximumSnappingRadius;
    @JsonProperty("maximum_visited_nodes")
    private Integer maximumVisitedNodes;
    @JsonProperty("maximum_visited_nodes_pt")
    private Integer maximumVisitedNodesPT;

    @JsonProperty("encoder_options")
    private EncoderOptionsProperties encoderOptions;
    @JsonProperty("preparation")
    private PreparationProperties preparation;
    @JsonProperty("execution")
    private ExecutionProperties execution;
    @JsonProperty("ext_storages")
    private HashMap<String, HashMap<String, String>> extStorages;

    public ProfileProperties() {
        preparation = new PreparationProperties();
        execution = new ExecutionProperties();
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getEncoderName() {
        return encoderName;
    }

    public void setEncoderName(String encoderName) {
        this.encoderName = encoderName;
    }

    public Boolean getElevation() {
        return elevation;
    }

    public void setElevation(Boolean elevation) {
        this.elevation = elevation;
    }

    public Boolean getElevationSmoothing() {
        return elevationSmoothing;
    }

    public void setElevationSmoothing(Boolean elevationSmoothing) {
        this.elevationSmoothing = elevationSmoothing;
    }

    public Integer getEncoderFlagsSize() {
        return encoderFlagsSize;
    }

    public void setEncoderFlagsSize(Integer encoderFlagsSize) {
        this.encoderFlagsSize = encoderFlagsSize;
    }

    public Boolean getInstructions() {
        return instructions;
    }

    public void setInstructions(Boolean instructions) {
        this.instructions = instructions;
    }

    public Boolean getOptimize() {
        return optimize;
    }

    public void setOptimize(Boolean optimize) {
        this.optimize = optimize;
    }

    public Boolean getTraffic() {
        return traffic;
    }

    public void setTraffic(Boolean traffic) {
        this.traffic = traffic;
    }

    public Boolean getInterpolateBridgesAndTunnels() {
        return interpolateBridgesAndTunnels;
    }

    public void setInterpolateBridgesAndTunnels(Boolean interpolateBridgesAndTunnels) {
        this.interpolateBridgesAndTunnels = interpolateBridgesAndTunnels;
    }

    public String getGraphPath() {
        return graphPath;
    }

    public void setGraphPath(String graphPath) {
        if (StringUtils.isNotBlank(graphPath))
            this.graphPath = Paths.get(graphPath).toAbsolutePath().toString();
        else this.graphPath = graphPath;
    }

    public Boolean getForceTurnCosts() {
        return forceTurnCosts;
    }

    public void setForceTurnCosts(Boolean forceTurnCosts) {
        this.forceTurnCosts = forceTurnCosts;
    }

    public Integer getLocationIndexResolution() {
        return locationIndexResolution;
    }

    public void setLocationIndexResolution(Integer locationIndexResolution) {
        this.locationIndexResolution = locationIndexResolution;
    }

    public Integer getLocationIndexSearchIterations() {
        return locationIndexSearchIterations;
    }

    public void setLocationIndexSearchIterations(Integer locationIndexSearchIterations) {
        this.locationIndexSearchIterations = locationIndexSearchIterations;
    }

    public String getGtfsFile() {
        return gtfsFile;
    }

    public void setGtfsFile(String gtfsFile) {
        if (StringUtils.isNotBlank(gtfsFile))
            this.gtfsFile = Paths.get(gtfsFile).toAbsolutePath().toString();
        else this.gtfsFile = gtfsFile;
    }

    public EncoderOptionsProperties getEncoderOptions() {
        return encoderOptions;
    }

    public void setEncoderOptions(EncoderOptionsProperties encoderOptions) {
        this.encoderOptions = encoderOptions;
    }

    @JsonIgnore
    public String getEncoderOptionsString() {
        if (encoderOptions == null)
            return "";
        return encoderName.toString();
    }


    public ExecutionProperties getExecution() {
        return execution;
    }

    public void setExecution(ExecutionProperties execution) {
        this.execution = execution;
    }

    public PreparationProperties getPreparation() {
        return preparation;
    }

    public void setPreparation(PreparationProperties preparation) {
        this.preparation = preparation;
    }

    public HashMap<String, HashMap<String, String>> getExtStorages() {
        return extStorages;
    }

    public void setExtStorages(HashMap<String, HashMap<String, String>> extStorages) {
        // Todo write individual storage config classes
        // Iterate over each storage in the extStorages and overwrite all paths variables with absolute paths#
        for (HashMap.Entry<String, HashMap<String, String>> storage : extStorages.entrySet()) {
            if (storage.getKey().equals("HereTraffic")) {
                // Replace streets, ref_pattern pattern_15min and log_location with absolute paths
                String hereTrafficPath = storage.getValue().get("streets");
                if (StringUtils.isNotBlank(hereTrafficPath)) {
                    storage.getValue().put("streets", Paths.get(hereTrafficPath).toAbsolutePath().toString());
                }
                String hereTrafficRefPattern = storage.getValue().get("ref_pattern");
                if (StringUtils.isNotBlank(hereTrafficRefPattern)) {
                    storage.getValue().put("ref_pattern", Paths.get(hereTrafficRefPattern).toAbsolutePath().toString());
                }
                String hereTrafficPattern15min = storage.getValue().get("pattern_15min");
                if (StringUtils.isNotBlank(hereTrafficPattern15min)) {
                    storage.getValue().put("pattern_15min", Paths.get(hereTrafficPattern15min).toAbsolutePath().toString());
                }
                String hereTrafficLogLocation = storage.getValue().get("log_location");
                if (StringUtils.isNotBlank(hereTrafficLogLocation)) {
                    storage.getValue().put("log_location", Paths.get(hereTrafficLogLocation).toAbsolutePath().toString());
                }
            }
            if (storage.getKey().equals("Borders")) {
                // Replace boundaries, ids and openborders with absolute paths
                String bordersBoundaries = storage.getValue().get("boundaries");
                if (StringUtils.isNotBlank(bordersBoundaries)) {
                    storage.getValue().put("boundaries", Paths.get(bordersBoundaries).toAbsolutePath().toString());
                }
                String bordersIds = storage.getValue().get("ids");
                if (StringUtils.isNotBlank(bordersIds)) {
                    storage.getValue().put("ids", Paths.get(bordersIds).toAbsolutePath().toString());
                }
                String openBorders = storage.getValue().get("openborders");
                if (StringUtils.isNotBlank(openBorders)) {
                    storage.getValue().put("openborders", Paths.get(openBorders).toAbsolutePath().toString());
                }
            }

            if (storage.getKey().equals("GreenIndex") || storage.getKey().equals("NoiseIndex") || storage.getKey().equals("csv") || storage.getKey().equals("ShadowIndex")) {
                // replace filepath
                String indexFilePath = storage.getValue().get("filepath");
                if (indexFilePath != null) {
                    storage.getValue().put("filepath", Paths.get(indexFilePath).toAbsolutePath().toString());
                }
            }
        }
        this.extStorages = extStorages;
    }

    public Double getMaximumDistance() {
        return maximumDistance;
    }

    public void setMaximumDistance(Double maximumDistance) {
        this.maximumDistance = maximumDistance;
    }

    public Double getMaximumDistanceDynamicWeights() {
        return maximumDistanceDynamicWeights;
    }

    public void setMaximumDistanceDynamicWeights(Double maximumDistanceDynamicWeights) {
        this.maximumDistanceDynamicWeights = maximumDistanceDynamicWeights;
    }

    public Double getMaximumDistanceAvoidAreas() {
        return maximumDistanceAvoidAreas;
    }

    public void setMaximumDistanceAvoidAreas(Double maximumDistanceAvoidAreas) {
        this.maximumDistanceAvoidAreas = maximumDistanceAvoidAreas;
    }

    public Double getMaximumDistanceAlternativeRoutes() {
        return maximumDistanceAlternativeRoutes;
    }

    public void setMaximumDistanceAlternativeRoutes(Double maximumDistanceAlternativeRoutes) {
        this.maximumDistanceAlternativeRoutes = maximumDistanceAlternativeRoutes;
    }

    public Double getMaximumDistanceRoundTripRoutes() {
        return maximumDistanceRoundTripRoutes;
    }

    public void setMaximumDistanceRoundTripRoutes(Double maximumDistanceRoundTripRoutes) {
        this.maximumDistanceRoundTripRoutes = maximumDistanceRoundTripRoutes;
    }

    public Double getMaximumSpeedLowerBound() {
        return maximumSpeedLowerBound;
    }

    public void setMaximumSpeedLowerBound(Double maximumSpeedLowerBound) {
        this.maximumSpeedLowerBound = maximumSpeedLowerBound;
    }

    public Integer getMaximumWayPoints() {
        return maximumWayPoints;
    }

    public void setMaximumWayPoints(Integer maximumWayPoints) {
        this.maximumWayPoints = maximumWayPoints;
    }

    public Integer getMaximumSnappingRadius() {
        return maximumSnappingRadius;
    }

    public void setMaximumSnappingRadius(Integer maximumSnappingRadius) {
        this.maximumSnappingRadius = maximumSnappingRadius;
    }

    public Integer getMaximumVisitedNodes() {
        return maximumVisitedNodes;
    }

    public void setMaximumVisitedNodes(Integer maximumVisitedNodes) {
        this.maximumVisitedNodes = maximumVisitedNodes;
    }

    public Integer getMaximumVisitedNodesPT() {
        return maximumVisitedNodesPT;
    }

    public void setMaximumVisitedNodesPT(Integer maximumVisitedNodesPT) {
        this.maximumVisitedNodesPT = maximumVisitedNodesPT;
    }
}
