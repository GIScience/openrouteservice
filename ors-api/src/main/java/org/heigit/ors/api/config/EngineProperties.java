package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.util.Helper;
import org.apache.commons.lang3.StringUtils;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.configuration.RouteProfileConfiguration;
import org.heigit.ors.util.ProfileTools;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Configuration
@ConfigurationProperties(prefix = "ors.engine")
public class EngineProperties {

    @JsonProperty("source_file")
    private String sourceFile = "ors-api/src/test/files/heidelberg.osm.gz";
    @JsonProperty("init_threads")
    private int initThreads = 1;
    @JsonProperty("preparation_mode")
    private boolean preparationMode = false;
    @JsonProperty("config_output_mode")
    private boolean configOutputMode = false;
    @JsonProperty("graphs_root_path")
    private String graphsRootPath = "./graphs";
    @JsonProperty("graphs_data_access")
    private String graphsDataAccess = "RAM_STORE";

    @JsonProperty("elevation")
    private ElevationProperties elevation;
    @JsonProperty("profile_default")
    private ProfileProperties profileDefault;
    @JsonProperty("profiles")
    private Map<String, ProfileProperties> profiles;

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        if (StringUtils.isNotBlank(sourceFile))
            this.sourceFile = Paths.get(sourceFile).toAbsolutePath().toString();
        else this.sourceFile = sourceFile;
    }

    public int getInitThreads() {
        return initThreads;
    }

    public void setInitThreads(int initThreads) {
        this.initThreads = initThreads;
    }

    public boolean isPreparationMode() {
        return preparationMode;
    }

    public void setPreparationMode(boolean preparationMode) {
        this.preparationMode = preparationMode;
    }

    public boolean isConfigOutputMode() {
        return configOutputMode;
    }

    public void setConfigOutputMode(boolean configOutputMode) {
        this.configOutputMode = configOutputMode;
    }

    public String getGraphsRootPath() {
        return graphsRootPath;
    }

    public void setGraphsRootPath(String graphsRootPath) {
        if (StringUtils.isNotBlank(graphsRootPath))
            this.graphsRootPath = Paths.get(graphsRootPath).toAbsolutePath().toString();
        else this.graphsRootPath = graphsRootPath;
    }

    public String getGraphsDataAccess() {
        return graphsDataAccess;
    }

    public void setGraphsDataAccess(String graphsDataAccess) {
        this.graphsDataAccess = graphsDataAccess;
    }

    public ElevationProperties getElevation() {
        return elevation;
    }

    public void setElevation(ElevationProperties elevation) {
        this.elevation = elevation;
    }

    public ProfileProperties getProfileDefault() {
        return profileDefault;
    }

    public void setProfileDefault(ProfileProperties profileDefault) {
        this.profileDefault = profileDefault;
    }

    public Map<String, ProfileProperties> getProfiles() {
        return profiles;
    }

    public void setProfiles(Map<String, ProfileProperties> profiles) {
        this.profiles = profiles;
    }

    @JsonIgnore
    public RouteProfileConfiguration[] getConvertedProfiles() {
        List<RouteProfileConfiguration> convertedProfiles = new ArrayList<>();
        if (profiles != null) {
            for (Map.Entry<String, ProfileProperties> profileEntry : profiles.entrySet()) {
                ProfileProperties profile = profileEntry.getValue();
                boolean enabled = profile.enabled != null ? profile.enabled : profileDefault.getEnabled();
                if (!enabled) {
                    continue;
                }
                RouteProfileConfiguration convertedProfile = new RouteProfileConfiguration();
                convertedProfile.setName(profileEntry.getKey());
                convertedProfile.setEnabled(enabled);
                convertedProfile.setProfiles(profile.getEncoderName());
                String graphPath = profile.getGraphPath();
                String rootGraphsPath = getGraphsRootPath();
                if (!Helper.isEmpty(rootGraphsPath)) {
                    if (Helper.isEmpty(graphPath))
                        graphPath = Paths.get(rootGraphsPath, profileEntry.getKey()).toString();
                }
                convertedProfile.setGraphPath(graphPath);
                convertedProfile.setEncoderOptions(profile.getEncoderOptionsString());
                convertedProfile.setOptimize(profile.optimize != null ? profile.optimize : profileDefault.getOptimize());
                convertedProfile.setEncoderFlagsSize(profile.encoderFlagsSize != null ? profile.encoderFlagsSize : profileDefault.getEncoderFlagsSize());
                convertedProfile.setInstructions(profile.instructions != null ? profile.instructions : profileDefault.getInstructions());
                convertedProfile.setMaximumDistance(profile.maximumDistance != null ? profile.maximumDistance : profileDefault.getMaximumDistance());
                convertedProfile.setMaximumDistanceDynamicWeights(profile.maximumDistanceDynamicWeights != null ? profile.maximumDistanceDynamicWeights : profileDefault.getMaximumDistanceDynamicWeights());
                convertedProfile.setMaximumDistanceAvoidAreas(profile.maximumDistanceAvoidAreas != null ? profile.maximumDistanceAvoidAreas : profileDefault.getMaximumDistanceAvoidAreas());
                convertedProfile.setMaximumDistanceAlternativeRoutes(profile.maximumDistanceAlternativeRoutes != null ? profile.maximumDistanceAlternativeRoutes : profileDefault.getMaximumDistanceAlternativeRoutes());
                convertedProfile.setMaximumDistanceRoundTripRoutes(profile.maximumDistanceRoundTripRoutes != null ? profile.maximumDistanceRoundTripRoutes : profileDefault.getMaximumDistanceRoundTripRoutes());
                convertedProfile.setMaximumSpeedLowerBound(profile.maximumSpeedLowerBound != null ? profile.maximumSpeedLowerBound : profileDefault.getMaximumSpeedLowerBound());
                convertedProfile.setMaximumWayPoints(profile.maximumWayPoints != null ? profile.maximumWayPoints : profileDefault.getMaximumWayPoints());
                convertedProfile.setMaximumSnappingRadius(profile.maximumSnappingRadius != null ? profile.maximumSnappingRadius : profileDefault.getMaximumSnappingRadius());
                convertedProfile.setLocationIndexResolution(profile.locationIndexResolution != null ? profile.locationIndexResolution : profileDefault.getLocationIndexResolution());
                convertedProfile.setLocationIndexSearchIterations(profile.locationIndexSearchIterations != null ? profile.locationIndexSearchIterations : profileDefault.getLocationIndexSearchIterations());
                convertedProfile.setEnforceTurnCosts(profile.forceTurnCosts != null ? profile.forceTurnCosts : profileDefault.getForceTurnCosts());
                convertedProfile.setGtfsFile(profile.gtfsFile != null ? profile.gtfsFile : "");
                convertedProfile.setMaximumVisitedNodesPT(profile.maximumVisitedNodes != null ? profile.maximumVisitedNodes : profileDefault.getMaximumVisitedNodes());
                if (profile.elevation != null && profile.elevation || profileDefault.getElevation()) {
                    convertedProfile.setElevationProvider(elevation.getProvider());
                    convertedProfile.setElevationCachePath(elevation.getCachePath());
                    convertedProfile.setElevationDataAccess(elevation.getDataAccess());
                    convertedProfile.setElevationCacheClear(elevation.isCacheClear());
                    convertedProfile.setElevationSmoothing(profile.elevationSmoothing != null ? profile.elevationSmoothing : profileDefault.getElevationSmoothing());
                    convertedProfile.setInterpolateBridgesAndTunnels(profile.interpolateBridgesAndTunnels != null ? profile.interpolateBridgesAndTunnels : profileDefault.getInterpolateBridgesAndTunnels());
                }
                // TODO (WIP): these options need to be used differently
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
                if (profile.getExtStorages() != null) {
                    for (Map<String, String> storageParams : profile.getExtStorages().values()) {
                        storageParams.put("gh_profile", ProfileTools.makeProfileName(RoutingProfileType.getEncoderName(RoutingProfileType.getFromString(convertedProfile.getProfiles())), "fastest", RouteProfileConfiguration.hasTurnCosts(convertedProfile.getEncoderOptions())));
                        storageParams.remove("");
                    }
                    convertedProfile.getExtStorages().putAll(profile.getExtStorages());
                }
                convertedProfiles.add(convertedProfile);
            }
        }
        return convertedProfiles.toArray(new RouteProfileConfiguration[0]);
    }

    public static class ElevationProperties {
        private boolean preprocessed = false;
        @JsonProperty("data_access")
        private String dataAccess = "MMAP";
        @JsonProperty("cache_clear")
        private boolean cacheClear = false;
        @JsonProperty("provider")
        private String provider = "multi";
        @JsonProperty("cache_path")
        private String cachePath = "./elevation_cache";

        public boolean isPreprocessed() {
            return preprocessed;
        }

        public void setPreprocessed(boolean preprocessed) {
            this.preprocessed = preprocessed;
        }

        public String getDataAccess() {
            return dataAccess;
        }

        public void setDataAccess(String dataAccess) {
            this.dataAccess = dataAccess;
        }

        public boolean isCacheClear() {
            return cacheClear;
        }

        public void setCacheClear(boolean cacheClear) {
            this.cacheClear = cacheClear;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getCachePath() {
            return cachePath;
        }

        public void setCachePath(String cachePath) {
            if (StringUtils.isNotBlank(cachePath))
                this.cachePath = Paths.get(cachePath).toAbsolutePath().toString();
            else this.cachePath = cachePath;
        }

    }

    @JsonInclude(NON_NULL)
    public static class ProfileProperties {
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
        @JsonProperty("graph_paths")
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
        private Map<String, Map<String, String>> extStorages;

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

        public Map<String, Map<String, String>> getExtStorages() {
            return extStorages;
        }

        public void setExtStorages(Map<String, Map<String, String>> extStorages) {
            // Todo write individual storage config classes
            // Iterate over each storage in the extStorages and overwrite all paths variables with absolute paths#
            for (Map.Entry<String, Map<String, String>> storage : extStorages.entrySet()) {
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

        @JsonInclude(NON_NULL)
        public static class EncoderOptionsProperties {
            @JsonProperty("block_fords")
            private Boolean blockFords;
            @JsonProperty("consider_elevation")
            private Boolean considerElevation;
            @JsonProperty("turn_costs")
            private Boolean turnCosts;
            @JsonProperty("use_acceleration")
            private Boolean useAcceleration;
            @JsonProperty("maximum_grade_level")
            private Integer maximumGradeLevel;
            @JsonProperty("preferred_speed_factor")
            private Double preferredSpeedFactor;
            @JsonProperty("problematic_speed_factor")
            private Double problematicSpeedFactor;

            public Boolean getBlockFords() {
                return blockFords;
            }

            public void setBlockFords(Boolean blockFords) {
                this.blockFords = blockFords;
            }

            public Boolean getConsiderElevation() {
                return considerElevation;
            }

            public void setConsiderElevation(Boolean considerElevation) {
                this.considerElevation = considerElevation;
            }

            public Boolean getTurnCosts() {
                return turnCosts;
            }

            public void setTurnCosts(Boolean turnCosts) {
                this.turnCosts = turnCosts;
            }

            public Boolean getUseAcceleration() {
                return useAcceleration;
            }

            public void setUseAcceleration(Boolean useAcceleration) {
                this.useAcceleration = useAcceleration;
            }

            public Integer getMaximumGradeLevel() {
                return maximumGradeLevel;
            }

            public void setMaximumGradeLevel(Integer maximumGradeLevel) {
                this.maximumGradeLevel = maximumGradeLevel;
            }

            public Double getPreferredSpeedFactor() {
                return preferredSpeedFactor;
            }

            public void setPreferredSpeedFactor(Double preferredSpeedFactor) {
                this.preferredSpeedFactor = preferredSpeedFactor;
            }

            public Double getProblematicSpeedFactor() {
                return problematicSpeedFactor;
            }

            public void setProblematicSpeedFactor(Double problematicSpeedFactor) {
                this.problematicSpeedFactor = problematicSpeedFactor;
            }

            public String toString() {
                StringBuilder output = new StringBuilder();
                for (Field entry : this.getClass().getDeclaredFields()) {
                    try {
                        Object value = entry.get(this);
                        if (value != null) {
                            if (!output.isEmpty()) {
                                output.append("|");
                            }
                            output.append(value);
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
                return output.toString();

            }
        }

        @JsonInclude(NON_NULL)
        public static class PreparationProperties {
            @JsonProperty("min_network_size")
            private Integer minNetworkSize;
            @JsonProperty("min_one_way_network_size")
            private Integer minOneWayNetworkSize;
            @JsonProperty("methods")
            private MethodsProperties methods;

            public Integer getMinNetworkSize() {
                return minNetworkSize;
            }

            public void setMinNetworkSize(Integer minNetworkSize) {
                this.minNetworkSize = minNetworkSize;
            }

            public Integer getMinOneWayNetworkSize() {
                return minOneWayNetworkSize;
            }

            public void setMinOneWayNetworkSize(Integer minOneWayNetworkSize) {
                this.minOneWayNetworkSize = minOneWayNetworkSize;
            }

            public MethodsProperties getMethods() {
                return methods;
            }

            public PreparationProperties setMethods(MethodsProperties methods) {
                this.methods = methods;
                return this;
            }

            @JsonInclude(NON_NULL)
            public static class MethodsProperties {
                private CHProperties ch;
                private LMProperties lm;
                private CoreProperties core;
                private FastIsochroneProperties fastisochrones;

                public CHProperties getCh() {
                    return ch;
                }

                public void setCh(CHProperties ch) {
                    this.ch = ch;
                }

                public LMProperties getLm() {
                    return lm;
                }

                public void setLm(LMProperties lm) {
                    this.lm = lm;
                }

                public CoreProperties getCore() {
                    return core;
                }

                public void setCore(CoreProperties core) {
                    this.core = core;
                }

                public FastIsochroneProperties getFastisochrones() {
                    return fastisochrones;
                }

                public void setFastisochrones(FastIsochroneProperties fastisochrones) {
                    this.fastisochrones = fastisochrones;
                }

                @JsonInclude(NON_NULL)
                public static class CHProperties {
                    private Boolean enabled;
                    private Integer threads;
                    private String weightings;

                    public Boolean getEnabled() {
                        return enabled;
                    }

                    public void setEnabled(Boolean enabled) {
                        this.enabled = enabled;
                    }

                    public Integer getThreads() {
                        return threads;
                    }

                    public void setThreads(Integer threads) {
                        this.threads = threads;
                    }

                    public String getWeightings() {
                        return weightings;
                    }

                    public void setWeightings(String weightings) {
                        this.weightings = weightings;
                    }
                }

                @JsonInclude(NON_NULL)
                public static class LMProperties {
                    private Boolean enabled;
                    private Integer threads;
                    private String weightings;
                    private Integer landmarks;

                    public Boolean getEnabled() {
                        return enabled;
                    }

                    public void setEnabled(Boolean enabled) {
                        this.enabled = enabled;
                    }

                    public Integer getThreads() {
                        return threads;
                    }

                    public void setThreads(Integer threads) {
                        this.threads = threads;
                    }

                    public String getWeightings() {
                        return weightings;
                    }

                    public void setWeightings(String weightings) {
                        this.weightings = weightings;
                    }

                    public Integer getLandmarks() {
                        return landmarks;
                    }

                    public void setLandmarks(Integer landmarks) {
                        this.landmarks = landmarks;
                    }
                }

                @JsonInclude(NON_NULL)
                public static class CoreProperties {
                    private Boolean enabled;
                    private Integer threads;
                    private String weightings;
                    private Integer landmarks;
                    private String lmsets;

                    public Boolean getEnabled() {
                        return enabled;
                    }

                    public void setEnabled(Boolean enabled) {
                        this.enabled = enabled;
                    }

                    public Integer getThreads() {
                        return threads;
                    }

                    public void setThreads(Integer threads) {
                        this.threads = threads;
                    }

                    public String getWeightings() {
                        return weightings;
                    }

                    public void setWeightings(String weightings) {
                        this.weightings = weightings;
                    }

                    public Integer getLandmarks() {
                        return landmarks;
                    }

                    public void setLandmarks(Integer landmarks) {
                        this.landmarks = landmarks;
                    }

                    public String getLmsets() {
                        return lmsets;
                    }

                    public void setLmsets(String lmsets) {
                        this.lmsets = lmsets;
                    }
                }

                @JsonInclude(NON_NULL)
                public static class FastIsochroneProperties {
                    private Boolean enabled;
                    private Integer threads;
                    private String weightings;
                    private Integer maxcellnodes;

                    public Boolean isEnabled() {
                        return enabled;
                    }

                    public void setEnabled(Boolean enabled) {
                        this.enabled = enabled;
                    }

                    public Integer getThreads() {
                        return threads;
                    }

                    public void setThreads(Integer threads) {
                        this.threads = threads;
                    }

                    public String getWeightings() {
                        return weightings;
                    }

                    public void setWeightings(String weightings) {
                        this.weightings = weightings;
                    }

                    public Integer getMaxcellnodes() {
                        return maxcellnodes;
                    }

                    public void setMaxcellnodes(Integer maxcellnodes) {
                        this.maxcellnodes = maxcellnodes;
                    }
                }
            }
        }

        @JsonInclude(NON_NULL)
        public static class ExecutionProperties {
            private MethodsProperties methods;

            public MethodsProperties getMethods() {
                return methods;
            }

            public void setMethods(MethodsProperties methods) {
                this.methods = methods;
            }

            @JsonInclude(NON_NULL)
            public static class MethodsProperties {
                private AStarProperties astar;
                private LMProperties lm;
                private CoreProperties core;

                public AStarProperties getCh() {
                    return astar;
                }

                public void setCh(AStarProperties ch) {
                    this.astar = ch;
                }

                public LMProperties getLm() {
                    return lm;
                }

                public void setLm(LMProperties lm) {
                    this.lm = lm;
                }

                public CoreProperties getCore() {
                    return core;
                }

                public void setCore(CoreProperties core) {
                    this.core = core;
                }

                @JsonInclude(NON_NULL)
                public static class AStarProperties {
                    private String approximation;
                    private Integer epsilon;

                    public String getApproximation() {
                        return approximation;
                    }

                    public void setApproximation(String approximation) {
                        this.approximation = approximation;
                    }

                    public Integer getEpsilon() {
                        return epsilon;
                    }

                    public void setEpsilon(Integer epsilon) {
                        this.epsilon = epsilon;
                    }
                }

                @JsonInclude(NON_NULL)
                public static class LMProperties {
                    @JsonProperty("active_landmarks")
                    private Integer activeLandmarks;

                    public Integer getActiveLandmarks() {
                        return activeLandmarks;
                    }

                    public void setActiveLandmarks(Integer activeLandmarks) {
                        this.activeLandmarks = activeLandmarks;
                    }
                }

                @JsonInclude(NON_NULL)
                public static class CoreProperties {
                    @JsonProperty("active_landmarks")
                    private Integer activeLandmarks;

                    public Integer getActiveLandmarks() {
                        return activeLandmarks;
                    }

                    public void setActiveLandmarks(Integer activeLandmarks) {
                        this.activeLandmarks = activeLandmarks;
                    }
                }
            }
        }
    }
}
