package org.heigit.ors.config.profile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.common.EncoderNameEnum;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;

@Getter
@Setter
public class ProfileProperties {
    @JsonIgnore
    private String profileName;
    @JsonIgnore
    private Boolean enabled;
    @JsonProperty("encoder_name")
    private EncoderNameEnum encoderName;
    @JsonIgnore
    private Path graphPath;
    @JsonIgnore
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
    @JsonIgnore
    private Path gtfsFile;

    @JsonIgnore
    private Double maximumDistance;
    @JsonIgnore
    private Double maximumDistanceDynamicWeights;
    @JsonIgnore
    private Double maximumDistanceAvoidAreas;
    @JsonIgnore
    private Double maximumDistanceAlternativeRoutes;
    @JsonIgnore
    private Double maximumDistanceRoundTripRoutes;
    @JsonIgnore
    private Double maximumSpeedLowerBound;
    @JsonIgnore
    private Integer maximumWayPoints;
    @JsonIgnore
    private Integer maximumSnappingRadius;
    @JsonIgnore
    private Integer maximumVisitedNodes;

    @JsonIgnore
    private RepoProperties repo = new RepoProperties();
    @JsonProperty("encoder_options")
    private EncoderOptionsProperties encoderOptions = new EncoderOptionsProperties();
    @JsonProperty("preparation")
    private PreparationProperties preparation = new PreparationProperties();
    @JsonIgnore
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
        profileName = ofNullable(profileName).orElse(key);

        // set values from profileDefault if they are not set
        enabled = ofNullable(enabled).orElse(profileDefault.enabled);
        elevation = ofNullable(elevation).orElse(profileDefault.elevation);
        elevationSmoothing = ofNullable(elevationSmoothing).orElse(profileDefault.elevationSmoothing);
        encoderFlagsSize = ofNullable(encoderFlagsSize).orElse(profileDefault.encoderFlagsSize);
        instructions = ofNullable(instructions).orElse(profileDefault.instructions);
        optimize = ofNullable(optimize).orElse(profileDefault.optimize);
        traffic = ofNullable(traffic).orElse(profileDefault.traffic);
        interpolateBridgesAndTunnels = ofNullable(interpolateBridgesAndTunnels).orElse(profileDefault.interpolateBridgesAndTunnels);
        forceTurnCosts = ofNullable(forceTurnCosts).orElse(profileDefault.forceTurnCosts);
        locationIndexResolution = ofNullable(locationIndexResolution).orElse(profileDefault.locationIndexResolution);
        locationIndexSearchIterations = ofNullable(locationIndexSearchIterations).orElse(profileDefault.locationIndexSearchIterations);
        maximumDistance = ofNullable(maximumDistance).orElse(profileDefault.maximumDistance);
        maximumDistanceDynamicWeights = ofNullable(maximumDistanceDynamicWeights).orElse(profileDefault.maximumDistanceDynamicWeights);
        maximumDistanceAvoidAreas = ofNullable(maximumDistanceAvoidAreas).orElse(profileDefault.maximumDistanceAvoidAreas);
        maximumDistanceAlternativeRoutes = ofNullable(maximumDistanceAlternativeRoutes).orElse(profileDefault.maximumDistanceAlternativeRoutes);
        maximumDistanceRoundTripRoutes = ofNullable(maximumDistanceRoundTripRoutes).orElse(profileDefault.maximumDistanceRoundTripRoutes);
        maximumSpeedLowerBound = ofNullable(maximumSpeedLowerBound).orElse(profileDefault.maximumSpeedLowerBound);
        maximumWayPoints = ofNullable(maximumWayPoints).orElse(profileDefault.maximumWayPoints);
        maximumSnappingRadius = ofNullable(maximumSnappingRadius).orElse(profileDefault.maximumSnappingRadius);
        maximumVisitedNodes = ofNullable(maximumVisitedNodes).orElse(profileDefault.maximumVisitedNodes);

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
        graphPath = ofNullable(graphPath).orElse(profileDefault.graphPath).toAbsolutePath();
        sourceFile = ofNullable(sourceFile).orElse(profileDefault.sourceFile).toAbsolutePath();
        gtfsFile = ofNullable(gtfsFile).orElse(profileDefault.gtfsFile);
        if (gtfsFile != null) {
            gtfsFile = gtfsFile.toAbsolutePath();
        }
        return this;
    }

    public void mergeLoaded(ProfileProperties loadedProfile) {
        // Copy only relevant values from loadedProfile if they are set
        elevation = ofNullable(loadedProfile.elevation).orElse(elevation);
        elevationSmoothing = ofNullable(loadedProfile.elevationSmoothing).orElse(elevationSmoothing);
        encoderFlagsSize = ofNullable(loadedProfile.encoderFlagsSize).orElse(encoderFlagsSize);
        instructions = ofNullable(loadedProfile.instructions).orElse(instructions);
        optimize = ofNullable(loadedProfile.optimize).orElse(optimize);
        traffic = ofNullable(loadedProfile.traffic).orElse(traffic);
        interpolateBridgesAndTunnels = ofNullable(loadedProfile.interpolateBridgesAndTunnels).orElse(interpolateBridgesAndTunnels);
        forceTurnCosts = ofNullable(loadedProfile.forceTurnCosts).orElse(forceTurnCosts);
        locationIndexResolution = ofNullable(loadedProfile.locationIndexResolution).orElse(locationIndexResolution);
        locationIndexSearchIterations = ofNullable(loadedProfile.locationIndexSearchIterations).orElse(locationIndexSearchIterations);
        // replace object params
        encoderOptions = ofNullable(loadedProfile.encoderOptions).orElse(encoderOptions);
        preparation = ofNullable(loadedProfile.preparation).orElse(preparation);
        extStorages = ofNullable(loadedProfile.extStorages).orElse(extStorages);
    }
}


