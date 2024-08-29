package org.heigit.ors.config.profile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.utils.NonEmptyMapFilter;
import org.heigit.ors.config.utils.PathSerializer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;

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
    private Map<String, ExtendedStorage> extStorages = new LinkedHashMap<>();

    @JsonIgnore
    public static ProfileProperties getProfileInstance(EncoderNameEnum encoderName) {
        ProfileProperties profile = new ProfileProperties();
//        profile.setEnabled(false);
        profile.setEncoderName(encoderName);
        profile.setEncoderOptions(EncoderOptionsProperties.getEncoderOptionsProperties(encoderName));
        profile.setPreparation(PreparationProperties.getPreparationProperties(encoderName));
        profile.setExecution(ExecutionProperties.getExecutionProperties(encoderName));
        profile.setExtStorages(ExtendedStorage.getDefaultExtStoragesMap(encoderName));
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
            this.extStorages = new LinkedHashMap<>();
            extStorages.forEach((key, storage) -> {
                if (storage != null) {
                    storage.initialize(ExtendedStorageName.getEnum(key));
                    this.extStorages.put(key, storage);
                }
            });
        }
    }

    public ProfileProperties mergeDefaults(ProfileProperties other, Boolean overwrite) {
        enabled = overwrite ? ofNullable(other.enabled).orElse(this.enabled) : ofNullable(this.enabled).orElse(other.enabled);
        encoderName = overwrite ? ofNullable(other.encoderName).orElse(this.encoderName) : ofNullable(this.encoderName).orElse(other.encoderName);
        sourceFile = overwrite ? ofNullable(other.sourceFile).orElse(this.sourceFile) : ofNullable(this.sourceFile).orElse(other.sourceFile);
        elevation = overwrite ? ofNullable(other.elevation).orElse(this.elevation) : ofNullable(this.elevation).orElse(other.elevation);
        elevationSmoothing = overwrite ? ofNullable(other.elevationSmoothing).orElse(this.elevationSmoothing) : ofNullable(this.elevationSmoothing).orElse(other.elevationSmoothing);
        encoderFlagsSize = overwrite ? ofNullable(other.encoderFlagsSize).orElse(this.encoderFlagsSize) : ofNullable(this.encoderFlagsSize).orElse(other.encoderFlagsSize);
        instructions = overwrite ? ofNullable(other.instructions).orElse(this.instructions) : ofNullable(this.instructions).orElse(other.instructions);
        optimize = overwrite ? ofNullable(other.optimize).orElse(this.optimize) : ofNullable(this.optimize).orElse(other.optimize);
        traffic = overwrite ? ofNullable(other.traffic).orElse(this.traffic) : ofNullable(this.traffic).orElse(other.traffic);
        interpolateBridgesAndTunnels = overwrite ? ofNullable(other.interpolateBridgesAndTunnels).orElse(this.interpolateBridgesAndTunnels) : ofNullable(this.interpolateBridgesAndTunnels).orElse(other.interpolateBridgesAndTunnels);
        forceTurnCosts = overwrite ? ofNullable(other.forceTurnCosts).orElse(this.forceTurnCosts) : ofNullable(this.forceTurnCosts).orElse(other.forceTurnCosts);
        locationIndexResolution = overwrite ? ofNullable(other.locationIndexResolution).orElse(this.locationIndexResolution) : ofNullable(this.locationIndexResolution).orElse(other.locationIndexResolution);
        locationIndexSearchIterations = overwrite ? ofNullable(other.locationIndexSearchIterations).orElse(this.locationIndexSearchIterations) : ofNullable(this.locationIndexSearchIterations).orElse(other.locationIndexSearchIterations);
        gtfsFile = overwrite ? ofNullable(other.gtfsFile).orElse(this.gtfsFile) : ofNullable(this.gtfsFile).orElse(other.gtfsFile);
        maximumDistance = overwrite ? ofNullable(other.maximumDistance).orElse(this.maximumDistance) : ofNullable(this.maximumDistance).orElse(other.maximumDistance);
        maximumDistanceDynamicWeights = overwrite ? ofNullable(other.maximumDistanceDynamicWeights).orElse(this.maximumDistanceDynamicWeights) : ofNullable(this.maximumDistanceDynamicWeights).orElse(other.maximumDistanceDynamicWeights);
        maximumDistanceAvoidAreas = overwrite ? ofNullable(other.maximumDistanceAvoidAreas).orElse(this.maximumDistanceAvoidAreas) : ofNullable(this.maximumDistanceAvoidAreas).orElse(other.maximumDistanceAvoidAreas);
        maximumDistanceAlternativeRoutes = overwrite ? ofNullable(other.maximumDistanceAlternativeRoutes).orElse(this.maximumDistanceAlternativeRoutes) : ofNullable(this.maximumDistanceAlternativeRoutes).orElse(other.maximumDistanceAlternativeRoutes);
        maximumDistanceRoundTripRoutes = overwrite ? ofNullable(other.maximumDistanceRoundTripRoutes).orElse(this.maximumDistanceRoundTripRoutes) : ofNullable(this.maximumDistanceRoundTripRoutes).orElse(other.maximumDistanceRoundTripRoutes);
        maximumSpeedLowerBound = overwrite ? ofNullable(other.maximumSpeedLowerBound).orElse(this.maximumSpeedLowerBound) : ofNullable(this.maximumSpeedLowerBound).orElse(other.maximumSpeedLowerBound);
        maximumWayPoints = overwrite ? ofNullable(other.maximumWayPoints).orElse(this.maximumWayPoints) : ofNullable(this.maximumWayPoints).orElse(other.maximumWayPoints);
        maximumSnappingRadius = overwrite ? ofNullable(other.maximumSnappingRadius).orElse(this.maximumSnappingRadius) : ofNullable(this.maximumSnappingRadius).orElse(other.maximumSnappingRadius);
        maximumVisitedNodes = overwrite ? ofNullable(other.maximumVisitedNodes).orElse(this.maximumVisitedNodes) : ofNullable(this.maximumVisitedNodes).orElse(other.maximumVisitedNodes);

        for (Map.Entry<String, ExtendedStorage> entry : other.extStorages.entrySet()) {
            if (extStorages.containsKey(entry.getKey())) {
                extStorages.get(entry.getKey()).merge(entry.getValue(), overwrite);
            } else {
                extStorages.put(entry.getKey(), entry.getValue());
            }
        }

        encoderOptions.merge(other.encoderOptions, overwrite);
        preparation.merge(other.preparation, overwrite);
        execution.merge(other.execution, overwrite);

        return this;
    }
}


