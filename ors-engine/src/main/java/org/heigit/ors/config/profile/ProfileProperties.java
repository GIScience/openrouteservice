package org.heigit.ors.config.profile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.defaults.DefaultExtendedStoragesProperties;
import org.heigit.ors.config.profile.storages.ExtendedStorage;
import org.heigit.ors.config.profile.storages.ExtendedStorageName;
import org.heigit.ors.config.utils.NonEmptyMapFilter;
import org.heigit.ors.config.utils.PathSerializer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

@Getter
@Setter
@JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyMapFilter.class)
public class ProfileProperties {
    @JsonProperty("enabled")
    private Boolean enabled;
    @JsonProperty("encoder_name")
    private EncoderNameEnum encoderName;
    @JsonProperty("source_file")
    @JsonSerialize(using = PathSerializer.class)
    private Path sourceFile;
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
    @JsonSerialize(using = PathSerializer.class)
    @Setter(AccessLevel.PUBLIC)
    private Path graphPath;
    @JsonProperty("location_index_resolution")
    private Integer locationIndexResolution;
    @JsonProperty("location_index_search_iterations")
    private Integer locationIndexSearchIterations;
    @JsonProperty("gtfs_file")
    @JsonSerialize(using = PathSerializer.class)
    private Path gtfsFile;

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

    @JsonProperty("encoder_options")
    private EncoderOptionsProperties encoderOptions = new EncoderOptionsProperties();
    @JsonProperty("preparation")
    private PreparationProperties preparation = new PreparationProperties();
    @JsonProperty("execution")
    private ExecutionProperties execution = new ExecutionProperties();
    @JsonProperty("ext_storages")
    private Map<String, ExtendedStorage> extStorages;

    @JsonIgnore
    public static ProfileProperties getProfileInstance(EncoderNameEnum encoderName) {
        ProfileProperties profile = new ProfileProperties();
        profile.setEnabled(false);
        profile.setEncoderName(encoderName);
        profile.setEncoderOptions(EncoderOptionsProperties.getEncoderOptionsProperties(encoderName));
        profile.setPreparation(PreparationProperties.getPreparationProperties(encoderName));
        profile.setExecution(ExecutionProperties.getExecutionProperties(encoderName));
        DefaultExtendedStoragesProperties defaultExtendedStoragesProperties = new DefaultExtendedStoragesProperties(encoderName);
        profile.setExtStorages(defaultExtendedStoragesProperties.getExtStorages());
        switch (encoderName) {
            case PUBLIC_TRANSPORT -> {
                profile.setElevation(true);
                profile.setMaximumVisitedNodes(1000000);
                profile.setGtfsFile(Path.of(""));
            }
            case WHEELCHAIR -> {
                profile.setMaximumSnappingRadius(50);
            }
            case DEFAULT -> {
                profile.setEncoderName(null);
                profile.setElevation(true);
                profile.setElevationSmoothing(true);
                profile.setEncoderFlagsSize(8);
                profile.setInstructions(true);
                profile.setOptimize(false);
                profile.setTraffic(false);
                profile.setInterpolateBridgesAndTunnels(true);
                profile.setForceTurnCosts(false);
                profile.setLocationIndexResolution(500);
                profile.setLocationIndexSearchIterations(4);
                profile.setMaximumDistance(100000d);
                profile.setMaximumDistanceDynamicWeights(100000d);
                profile.setMaximumDistanceAvoidAreas(100000d);
                profile.setMaximumDistanceAlternativeRoutes(100000d);
                profile.setMaximumDistanceRoundTripRoutes(100000d);
                profile.setMaximumSpeedLowerBound(80d);
                profile.setMaximumWayPoints(50);
                profile.setMaximumSnappingRadius(400);
                profile.setMaximumVisitedNodes(1000000);
            }
            default -> {
            }
        }
        return profile;
    }

    @JsonIgnore
    public String getEncoderOptionsString() {
        if (encoderOptions == null) return "";
        return encoderOptions.toString();
    }

    @JsonIgnore
    public Integer[] getProfilesTypes() {
        ArrayList<Integer> list = new ArrayList<>();
        // TODO check why this originally tries to split the encoderName. Can we add more than one?
        if (encoderName != null && encoderName != EncoderNameEnum.DEFAULT) {
            list.add(encoderName.getValue());
        }
        return list.toArray(new Integer[0]);
    }

    @JsonSetter("ext_storages")
    public void setExtStorages(Map<String, ExtendedStorage> extStorages) {
        if (extStorages != null) {
            this.extStorages = new HashMap<>();
            extStorages.forEach((key, storage) -> {
                if (storage != null) {
                    storage.initialize(ExtendedStorageName.getEnum(key));
                    this.extStorages.put(key, storage);
                }
            });
        }
    }

    public ProfileProperties mergeDefaults(ProfileProperties profileDefault) {
        enabled = enabled == null ? profileDefault.enabled : enabled;
        encoderName = encoderName == null ? profileDefault.encoderName : encoderName;
        sourceFile = sourceFile ==null ? profileDefault.sourceFile : sourceFile;
        elevation = elevation == null ? profileDefault.elevation : elevation;
        elevationSmoothing = elevationSmoothing == null ? profileDefault.elevationSmoothing : elevationSmoothing;
        encoderFlagsSize = encoderFlagsSize == null ? profileDefault.encoderFlagsSize : encoderFlagsSize;
        instructions = instructions == null ? profileDefault.instructions : instructions;
        optimize = optimize == null ? profileDefault.optimize : optimize;
        traffic = traffic == null ? profileDefault.traffic : traffic;
        interpolateBridgesAndTunnels = interpolateBridgesAndTunnels == null ? profileDefault.interpolateBridgesAndTunnels : interpolateBridgesAndTunnels;
        forceTurnCosts = forceTurnCosts == null ? profileDefault.forceTurnCosts : forceTurnCosts;
        graphPath = graphPath == null ? profileDefault.graphPath: graphPath;
        locationIndexResolution = locationIndexResolution == null ? profileDefault.locationIndexResolution : locationIndexResolution;
        locationIndexSearchIterations = locationIndexSearchIterations == null ? profileDefault.locationIndexSearchIterations : locationIndexSearchIterations;
        gtfsFile = gtfsFile == null ? profileDefault.gtfsFile : gtfsFile;

        maximumDistance = maximumDistance == null ? profileDefault.maximumDistance : maximumDistance;
        maximumDistanceDynamicWeights = maximumDistanceDynamicWeights == null ? profileDefault.maximumDistanceDynamicWeights : maximumDistanceDynamicWeights;
        maximumDistanceAvoidAreas = maximumDistanceAvoidAreas == null ? profileDefault.maximumDistanceAvoidAreas : maximumDistanceAvoidAreas;
        maximumDistanceAlternativeRoutes = maximumDistanceAlternativeRoutes == null ? profileDefault.maximumDistanceAlternativeRoutes : maximumDistanceAlternativeRoutes;
        maximumDistanceRoundTripRoutes = maximumDistanceRoundTripRoutes == null ? profileDefault.maximumDistanceRoundTripRoutes : maximumDistanceRoundTripRoutes;
        maximumSpeedLowerBound = maximumSpeedLowerBound == null ? profileDefault.maximumSpeedLowerBound : maximumSpeedLowerBound;
        maximumWayPoints = maximumWayPoints == null ? profileDefault.maximumWayPoints : maximumWayPoints;
        maximumSnappingRadius = maximumSnappingRadius == null ? profileDefault.maximumSnappingRadius : maximumSnappingRadius;
        maximumVisitedNodes = maximumVisitedNodes == null ? profileDefault.maximumVisitedNodes : maximumVisitedNodes;

        encoderOptions = encoderOptions.isEmpty() ? profileDefault.encoderOptions : encoderOptions;
        preparation = preparation.isEmpty() ? profileDefault.preparation : preparation;
        execution = execution.isEmpty() ? profileDefault.execution : execution;
        extStorages = extStorages.isEmpty() ? profileDefault.extStorages : extStorages;
        return this;
    }
}
