package org.heigit.ors.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.common.DataAccessEnum;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.config.profile.defaults.DefaultElevationProperties;
import org.heigit.ors.config.profile.defaults.DefaultProfileProperties;
import org.heigit.ors.config.profile.defaults.DefaultProfiles;
import org.heigit.ors.config.utils.PathDeserializer;
import org.heigit.ors.config.utils.PathSerializer;
import org.heigit.ors.config.utils.PropertyUtils;
import org.heigit.ors.routing.configuration.RouteProfileConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Getter
@Setter(AccessLevel.PROTECTED)
@EqualsAndHashCode
public class EngineProperties {

    @JsonIgnore
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private boolean initialized = false;

    @JsonProperty("source_file")
    @JsonDeserialize(using = PathDeserializer.class)
    @JsonSerialize(using = PathSerializer.class)
    private Path sourceFile;
    @JsonProperty("init_threads")
    private Integer initThreads;
    @JsonProperty("preparation_mode")
    private Boolean preparationMode;
    @JsonProperty("config_output_mode")
    private Boolean configOutputMode;
    @JsonProperty("graphs_root_path")
    @JsonDeserialize(using = PathDeserializer.class)
    @JsonSerialize(using = PathSerializer.class)
    private Path graphsRootPath;
    @JsonProperty("graphs_data_access")
    private DataAccessEnum graphsDataAccess;

    @JsonProperty("elevation")
    private ElevationProperties elevation;
    @JsonProperty("profile_default")
    private ProfileProperties profileDefault;
    @JsonProperty("profiles")
    private Map<String, ProfileProperties> profiles;

    public EngineProperties() {
        this(false);
    }

    public EngineProperties(Boolean setDefaults) {
        setProfiles(new LinkedHashMap<>());
        setProfileDefault(new DefaultProfileProperties(setDefaults));
        setElevation(new DefaultElevationProperties(setDefaults));
        if (setDefaults) {
            setSourceFile(Paths.get(""));
            setInitThreads(1);
            setPreparationMode(false);
            setConfigOutputMode(false);
            setGraphsRootPath(Paths.get("./graphs"));
            setGraphsDataAccess(DataAccessEnum.RAM_STORE);
        }
    }

    @JsonIgnore
    public void combineProperties() {
        if (isInitialized()) {
            return;
        }
        // Merge default profiles with custom profiles
        // First: Top priority have properties from Map<String, ProfileProperties> profiles;
        // Second: Next priority are user set global properties from profileDefault
        // Third: If properties are not set in profiles and profileDefault, use the default_profiles with their specific properties and their defaults
        // Fourth: If properties are not set in profiles, profileDefault and default_profiles, use the default properties from DefaultProfileProperties
        // Initialize defult profiles

        // Correct the default profiles that haven't been set by the user
        // Make a copy Set<String>
        Set<String> raw_user_profile_names = new HashSet<>(this.getProfiles().keySet());
        ProfileProperties raw_user_default_profile_settings = this.getProfileDefault();
        DefaultProfiles system_default_profile_settings = new DefaultProfiles(true);
        DefaultProfileProperties system_default_profile_defaults_properties = new DefaultProfileProperties(true);

        for (String profileEntry : system_default_profile_settings.getProfiles().keySet()) {
            ProfileProperties profile = system_default_profile_settings.getProfiles().get(profileEntry);
            if (this.getProfiles().containsKey(profileEntry)) {
                // Todo Still needed or just overwrite the defaults in the end?
                continue;
            }
            // Second step
            PropertyUtils.deepCopyObjectsProperties(raw_user_default_profile_settings, profile, true, false);
            // Third step
            PropertyUtils.deepCopyObjectsProperties(system_default_profile_settings.getProfiles().get(profile.getEncoderName().name), profile, false, false);
            // Fourth step
            PropertyUtils.deepCopyObjectsProperties(system_default_profile_defaults_properties, profile, false, false);
            this.profiles.put(profileEntry, profile);
        }

        EngineProperties default_engine_properties = new EngineProperties(true);

        // Enrich null or missing properties with default values
        PropertyUtils.deepCopyObjectsProperties(default_engine_properties, this, false, false);

        // Correct the raw user profiles
        for (String profileEntryName : raw_user_profile_names) {
            // First step
            ProfileProperties profile = this.getProfiles().get(profileEntryName);
            // Second step
            PropertyUtils.deepCopyObjectsProperties(raw_user_default_profile_settings, profile, false, false);
            // Third step
            PropertyUtils.deepCopyObjectsProperties(system_default_profile_settings.getProfiles().get(profileEntryName), profile, false, false);
            // Fourth step
            PropertyUtils.deepCopyObjectsProperties(system_default_profile_defaults_properties, profile, false, false);
        }
        setInitialized(true);
    }


    @JsonIgnore
    public RouteProfileConfiguration[] getConvertedProfiles() {
        List<RouteProfileConfiguration> convertedProfiles = new ArrayList<>();
//        if (profiles != null) {
//            for (Map.Entry<String, ProfileProperties> profileEntry : profiles.entrySet()) {
//                ProfileProperties profile = profileEntry.getValue();
//                boolean enabled = profile.getEnabled() != null ? profile.getEnabled() : profileDefault.getEnabled();
//                if (!enabled) {
//                    continue;
//                }
//                RouteProfileConfiguration convertedProfile = new RouteProfileConfiguration();
//                convertedProfile.setName(profileEntry.getKey());
//                convertedProfile.setEnabled(enabled);
//                convertedProfile.setProfiles(profile.getEncoderName());
//                String graphPath = profile.getGraphPath();
//                String rootGraphsPath = getGraphsRootPath();
//                if (!Helper.isEmpty(rootGraphsPath)) {
//                    if (Helper.isEmpty(graphPath))
//                        graphPath = Paths.get(rootGraphsPath, profileEntry.getKey()).toString();
//                }
//                convertedProfile.setGraphPath(graphPath);
//                convertedProfile.setEncoderOptions(profile.getEncoderOptionsString());
//                convertedProfile.setOptimize(profile.getOptimize() != null ? profile.getOptimize() : profileDefault.getOptimize());
//                convertedProfile.setEncoderFlagsSize(profile.getEncoderFlagsSize() != null ? profile.getEncoderFlagsSize() : profileDefault.getEncoderFlagsSize());
//                convertedProfile.setInstructions(profile.getInstructions() != null ? profile.getInstructions() : profileDefault.getInstructions());
//                convertedProfile.setMaximumDistance(profile.getMaximumDistance() != null ? profile.getMaximumDistance() : profileDefault.getMaximumDistance());
//                convertedProfile.setMaximumDistanceDynamicWeights(profile.getMaximumDistanceDynamicWeights() != null ? profile.getMaximumDistanceDynamicWeights() : profileDefault.getMaximumDistanceDynamicWeights());
//                convertedProfile.setMaximumDistanceAvoidAreas(profile.getMaximumDistanceAvoidAreas() != null ? profile.getMaximumDistanceAvoidAreas() : profileDefault.getMaximumDistanceAvoidAreas());
//                convertedProfile.setMaximumDistanceAlternativeRoutes(profile.getMaximumDistanceAlternativeRoutes() != null ? profile.getMaximumDistanceAlternativeRoutes() : profileDefault.getMaximumDistanceAlternativeRoutes());
//                convertedProfile.setMaximumDistanceRoundTripRoutes(profile.getMaximumDistanceRoundTripRoutes() != null ? profile.getMaximumDistance() : profileDefault.getMaximumDistanceRoundTripRoutes());
//                convertedProfile.setMaximumSpeedLowerBound(profile.getMaximumSpeedLowerBound() != null ? profile.getMaximumSpeedLowerBound() : profileDefault.getMaximumSpeedLowerBound());
//                convertedProfile.setMaximumWayPoints(profile.getMaximumWayPoints() != null ? profile.getMaximumWayPoints() : profileDefault.getMaximumWayPoints());
//                convertedProfile.setMaximumSnappingRadius(profile.getMaximumSnappingRadius() != null ? profile.getMaximumSnappingRadius() : profileDefault.getMaximumSnappingRadius());
//                convertedProfile.setLocationIndexResolution(profile.getLocationIndexResolution() != null ? profile.getLocationIndexResolution() : profileDefault.getLocationIndexResolution());
//                convertedProfile.setLocationIndexSearchIterations(profile.getLocationIndexSearchIterations() != null ? profile.getLocationIndexSearchIterations() : profileDefault.getLocationIndexSearchIterations());
//                convertedProfile.setEnforceTurnCosts(profile.getForceTurnCosts() != null ? profile.getForceTurnCosts() : profileDefault.getForceTurnCosts());
//                convertedProfile.setGtfsFile(profile.getGtfsFile() != null ? profile.getGtfsFile() : "");
//                convertedProfile.setMaximumVisitedNodesPT(profile.getMaximumVisitedNodes() != null ? profile.getMaximumVisitedNodes() : profileDefault.getMaximumVisitedNodes());
//                if (profile.getElevation() != null && profile.getElevation() || profileDefault.getElevation()) {
//                    convertedProfile.setElevationProvider(elevation.getProvider());
//                    convertedProfile.setElevationCachePath(elevation.getCachePath());
//                    convertedProfile.setElevationDataAccess(elevation.getDataAccess());
//                    convertedProfile.setElevationCacheClear(elevation.isCacheClear());
//                    convertedProfile.setElevationSmoothing(profile.getElevationSmoothing() != null ? profile.getElevationSmoothing() : profileDefault.getElevationSmoothing());
//                    convertedProfile.setInterpolateBridgesAndTunnels(profile.getInterpolateBridgesAndTunnels() != null ? profile.getInterpolateBridgesAndTunnels() : profileDefault.getInterpolateBridgesAndTunnels());
//                }
//                Map<String, Object> preparation = profile.preparation != null ? profile.preparation : profileDefault.getPreparation();
//                if (preparation != null) {
//                    convertedProfile.setPreparationOpts(ConfigFactory.parseMap(preparation));
//                    String methodsKey = "methods";
//                    if (preparation.containsKey(methodsKey) && preparation.get(methodsKey) != null && ((Map<String, Object>) preparation.get(methodsKey)).containsKey("fastisochrones")) {
//                        convertedProfile.setIsochronePreparationOpts(ConfigFactory.parseMap((Map<String, Object>) ((Map<String, Object>) preparation.get(methodsKey)).get("fastisochrones")));
//                    }
//                }
//                Map<String, Object> execution = profile.execution != null ? profile.execution : profileDefault.getExecution();
//                if (execution != null) {
//                    convertedProfile.setExecutionOpts(ConfigFactory.parseMap(execution));
//                }
//                if (profile.getExtStorages() != null) {
//                    for (Map<String, String> storageParams : profile.getExtStorages().values()) {
//                        storageParams.put("gh_profile", ProfileTools.makeProfileName(RoutingProfileType.getEncoderName(RoutingProfileType.getFromString(convertedProfile.getProfiles())), "fastest", RouteProfileConfiguration.hasTurnCosts(convertedProfile.getEncoderOptions())));
//                        storageParams.remove("");
//                    }
//                    convertedProfile.getExtStorages().putAll(profile.getExtStorages());
//                }
//                convertedProfiles.add(convertedProfile);
//            }
//        }
        return convertedProfiles.toArray(new RouteProfileConfiguration[0]);
    }
}
