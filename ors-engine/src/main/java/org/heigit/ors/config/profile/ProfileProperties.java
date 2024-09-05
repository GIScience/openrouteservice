package org.heigit.ors.config.profile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.common.EncoderNameEnum;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;

@Getter
@Setter
public class ProfileProperties {
    @JsonIgnore
    private String profileName;

    @JsonProperty("enabled")
    private Boolean enabled;
    @JsonProperty("encoder_name")
    private EncoderNameEnum encoderName;
    @JsonProperty("graph_path")
    private Path graphPath;
    @JsonProperty("source_file")
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

    public ProfileProperties mergeDefaults(ProfileProperties profileDefault, String key) {
        // set the profile name to the key
        profileName = ofNullable(this.profileName).orElse(key);

        // set values from profileDefault if they are not set
        enabled = ofNullable(this.enabled).orElse(profileDefault.enabled);
        elevation = ofNullable(this.elevation).orElse(profileDefault.elevation);
        elevationSmoothing = ofNullable(this.elevationSmoothing).orElse(profileDefault.elevationSmoothing);
        encoderFlagsSize = ofNullable(this.encoderFlagsSize).orElse(profileDefault.encoderFlagsSize);
        instructions = ofNullable(this.instructions).orElse(profileDefault.instructions);
        optimize = ofNullable(this.optimize).orElse(profileDefault.optimize);
        traffic = ofNullable(this.traffic).orElse(profileDefault.traffic);
        interpolateBridgesAndTunnels = ofNullable(this.interpolateBridgesAndTunnels).orElse(profileDefault.interpolateBridgesAndTunnels);
        forceTurnCosts = ofNullable(this.forceTurnCosts).orElse(profileDefault.forceTurnCosts);
        locationIndexResolution = ofNullable(this.locationIndexResolution).orElse(profileDefault.locationIndexResolution);
        locationIndexSearchIterations = ofNullable(this.locationIndexSearchIterations).orElse(profileDefault.locationIndexSearchIterations);
        maximumDistance = ofNullable(this.maximumDistance).orElse(profileDefault.maximumDistance);
        maximumDistanceDynamicWeights = ofNullable(this.maximumDistanceDynamicWeights).orElse(profileDefault.maximumDistanceDynamicWeights);
        maximumDistanceAvoidAreas = ofNullable(this.maximumDistanceAvoidAreas).orElse(profileDefault.maximumDistanceAvoidAreas);
        maximumDistanceAlternativeRoutes = ofNullable(this.maximumDistanceAlternativeRoutes).orElse(profileDefault.maximumDistanceAlternativeRoutes);
        maximumDistanceRoundTripRoutes = ofNullable(this.maximumDistanceRoundTripRoutes).orElse(profileDefault.maximumDistanceRoundTripRoutes);
        maximumSpeedLowerBound = ofNullable(this.maximumSpeedLowerBound).orElse(profileDefault.maximumSpeedLowerBound);
        maximumWayPoints = ofNullable(this.maximumWayPoints).orElse(profileDefault.maximumWayPoints);
        maximumSnappingRadius = ofNullable(this.maximumSnappingRadius).orElse(profileDefault.maximumSnappingRadius);
        maximumVisitedNodes = ofNullable(this.maximumVisitedNodes).orElse(profileDefault.maximumVisitedNodes);

        // deep merge from profileDefault
        encoderOptions.merge(profileDefault.encoderOptions);
        preparation.merge(profileDefault.preparation);
        execution.merge(profileDefault.execution);
        for (Map.Entry<String, ExtendedStorage> entry : profileDefault.extStorages.entrySet()) {
            if (extStorages.containsKey(entry.getKey())) {
                extStorages.get(entry.getKey()).merge(entry.getValue());
            } else {
                extStorages.put(entry.getKey(), entry.getValue());
            }
        }

        // Fix paths
        graphPath = ofNullable(graphPath).orElse(Paths.get(profileDefault.graphPath.toString(), key)).toAbsolutePath();
        sourceFile = ofNullable(sourceFile).orElse(profileDefault.sourceFile).toAbsolutePath();
        gtfsFile = ofNullable(gtfsFile).orElse(profileDefault.gtfsFile);
        if (gtfsFile != null) {
            gtfsFile = gtfsFile.toAbsolutePath();
        }
        return this;
    }
}


