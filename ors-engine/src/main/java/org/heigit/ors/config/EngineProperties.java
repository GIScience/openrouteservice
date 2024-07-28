package org.heigit.ors.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.common.DataAccessEnum;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.config.profile.defaults.*;
import org.heigit.ors.config.utils.PathDeserializer;
import org.heigit.ors.config.utils.PathSerializer;
import org.heigit.ors.routing.configuration.RouteProfileConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter(AccessLevel.PACKAGE)
public class EngineProperties {

    private static final Map<String, ProfileProperties> DEFAULT_PROFILES = new LinkedHashMap<>();
    static {
        DEFAULT_PROFILES.put("car", new DefaultProfilePropertiesCar());
        DEFAULT_PROFILES.put("hgv", new DefaultProfilePropertiesHgv());
        DEFAULT_PROFILES.put("bike-regular", new DefaultProfilePropertiesBikeRegular());
        DEFAULT_PROFILES.put("bike-electric", new DefaultProfilePropertiesBikeElectric());
        DEFAULT_PROFILES.put("bike-mountain", new DefaultProfilePropertiesBikeMountain());
        DEFAULT_PROFILES.put("bike-road", new DefaultProfilePropertiesBikeRoad());
        DEFAULT_PROFILES.put("walking", new DefaultProfilePropertiesWalking());
        DEFAULT_PROFILES.put("hiking", new DefaultProfilePropertiesHiking());
        DEFAULT_PROFILES.put("wheelchair", new DefaultProfilePropertiesWheelchair());
        DEFAULT_PROFILES.put("public-transport", new DefaultProfilePropertiesPublicTransport());
    }

    @JsonProperty("source_file")
    @JsonDeserialize(using = PathDeserializer.class)
    @JsonSerialize(using = PathSerializer.class)
    private Path sourceFile = Paths.get("");
    @JsonProperty("init_threads")
    private Integer initThreads = 1;
    @JsonProperty("preparation_mode")
    private Boolean preparationMode = false;
    @JsonProperty("config_output_mode")
    private Boolean configOutputMode = false;
    @JsonProperty("graphs_root_path")
    @JsonDeserialize(using = PathDeserializer.class)
    @JsonSerialize(using = PathSerializer.class)
    private Path graphsRootPath = Paths.get("./graphs");
    @JsonProperty("graphs_data_access")
    private DataAccessEnum graphsDataAccess = DataAccessEnum.RAM_STORE;

    @JsonProperty("elevation")
    private ElevationProperties elevation = new ElevationProperties();
    @JsonProperty("profile_default")
    private ProfileProperties profileDefault = new DefaultProfileProperties();
    @JsonProperty("profiles")
    private Map<String, ProfileProperties> profiles = DEFAULT_PROFILES;

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
