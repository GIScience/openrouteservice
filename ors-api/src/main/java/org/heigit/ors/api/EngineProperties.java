package org.heigit.ors.api;

import com.graphhopper.util.Helper;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.configuration.RouteProfileConfiguration;
import org.heigit.ors.util.ProfileTools;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "ors.engine")
public class EngineProperties {

    private int initThreads;
    private boolean preparationMode;
    private String sourceFile;
    private String graphsRootPath;
    private String graphsDataAccess;
    private ElevationProperties elevation;
    private ProfileProperties profileDefault;
    private Map<String, ProfileProperties> profiles;

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

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        if (StringUtils.isNotBlank(sourceFile))
            this.sourceFile = Paths.get(sourceFile).toAbsolutePath().toString();
        else this.sourceFile = sourceFile;
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

    public RouteProfileConfiguration[] getConvertedProfiles() {
        List<RouteProfileConfiguration> convertedProfiles = new ArrayList<>();
        if (profiles != null) {
            for (Map.Entry<String, ProfileProperties> profileEntry : profiles.entrySet()) {
                ProfileProperties profile = profileEntry.getValue();
                boolean enabled = profile.enabled != null ? profile.enabled : profileDefault.isEnabled();
                if (!enabled) {
                    continue;
                }
                RouteProfileConfiguration convertedProfile = new RouteProfileConfiguration();
                convertedProfile.setName(profileEntry.getKey());
                convertedProfile.setEnabled(enabled);
                convertedProfile.setProfiles(profile.getProfile());
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
                convertedProfile.setGtfsFile(profile.gtfsFile != null ? profile.gtfsFile : profile.getGtfsFile());
                convertedProfile.setMaximumVisitedNodesPT(profile.maximumVisitedNodes != null ? profile.maximumVisitedNodes : profileDefault.getMaximumVisitedNodes());
                if (profile.elevation != null && profile.elevation || profileDefault.isElevation()) {
                    convertedProfile.setElevationProvider(elevation.getProvider());
                    convertedProfile.setElevationCachePath(elevation.getCachePath());
                    convertedProfile.setElevationDataAccess(elevation.getDataAccess());
                    convertedProfile.setElevationCacheClear(elevation.isCacheClear());
                    convertedProfile.setElevationSmoothing(profile.elevationSmoothing != null ? profile.elevationSmoothing : profileDefault.getElevationSmoothing());
                    convertedProfile.setInterpolateBridgesAndTunnels(profile.interpolateBridgesAndTunnels != null ? profile.interpolateBridgesAndTunnels : profileDefault.getInterpolateBridgesAndTunnels());
                }
                Map<String, Object> preparation = profile.preparation != null ? profile.preparation : profileDefault.getPreparation();
                if (preparation != null) {
                    convertedProfile.setPreparationOpts(ConfigFactory.parseMap(preparation));
                    String methodsKey = "methods";
                    if (preparation.containsKey(methodsKey) && preparation.get(methodsKey) != null && ((Map<String, Object>) preparation.get(methodsKey)).containsKey("fastisochrones")) {
                        convertedProfile.setIsochronePreparationOpts(ConfigFactory.parseMap((Map<String, Object>) ((Map<String, Object>) preparation.get(methodsKey)).get("fastisochrones")));
                    }
                }
                Map<String, Object> execution = profile.execution != null ? profile.execution : profileDefault.getExecution();
                if (execution != null) {
                    convertedProfile.setExecutionOpts(ConfigFactory.parseMap(execution));
                }
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
        private boolean preprocessed;
        private boolean cacheClear;
        private String provider;
        private String cachePath;
        private String dataAccess;

        public boolean isPreprocessed() {
            return preprocessed;
        }

        public void setPreprocessed(boolean preprocessed) {
            this.preprocessed = preprocessed;
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

        public String getDataAccess() {
            return dataAccess;
        }

        public void setDataAccess(String dataAccess) {
            this.dataAccess = dataAccess;
        }
    }

    public static class ProfileProperties {
        private String profile;
        private Boolean enabled;
        private Boolean elevation;
        private Boolean elevationSmoothing;
        private Boolean traffic;
        private Boolean interpolateBridgesAndTunnels;
        private Boolean instructions;
        private Boolean optimize;
        private String graphPath;
        private Map<String, String> encoderOptions;

        //TODO: For later use when refactoring RoutingManagerConfiguration
//        private PreparationProperties preparation;
//        private ExecutionProperties execution;
        private Map<String, Object> preparation;
        private Map<String, Object> execution;
        private Map<String, Map<String, String>> extStorages;
        private Double maximumDistance;
        private Double maximumDistanceDynamicWeights;
        private Double maximumDistanceAvoidAreas;
        private Double maximumDistanceAlternativeRoutes;
        private Double maximumDistanceRoundTripRoutes;
        private Double maximumSpeedLowerBound;
        private Integer maximumWayPoints;
        private Integer maximumSnappingRadius;
        private Integer maximumVisitedNodes;
        private Integer encoderFlagsSize;
        private Integer locationIndexResolution = 500;
        private Integer locationIndexSearchIterations = 4;
        private Boolean forceTurnCosts;
        private String gtfsFile;

        public String getProfile() {
            return profile;
        }

        public void setProfile(String profile) {
            this.profile = profile;
        }

        public boolean isEnabled() {
            return enabled != null && enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isElevation() {
            return elevation != null && elevation;
        }

        public void setElevation(boolean elevation) {
            this.elevation = elevation;
        }

        public Boolean getElevationSmoothing() {
            return elevationSmoothing != null && elevationSmoothing;
        }

        public void setElevationSmoothing(Boolean elevationSmoothing) {
            this.elevationSmoothing = elevationSmoothing;
        }

        public boolean isTraffic() {
            return traffic != null && traffic;
        }

        public void setTraffic(boolean traffic) {
            this.traffic = traffic;
        }

        public boolean getInterpolateBridgesAndTunnels() {
            return interpolateBridgesAndTunnels != null && interpolateBridgesAndTunnels;
        }

        public void setInterpolateBridgesAndTunnels(Boolean interpolateBridgesAndTunnels) {
            this.interpolateBridgesAndTunnels = interpolateBridgesAndTunnels;
        }

        public boolean getInstructions() {
            return instructions != null ? instructions : true;
        }

        public void setInstructions(Boolean instructions) {
            this.instructions = instructions;
        }

        public boolean getOptimize() {
            return optimize != null && optimize;
        }

        public void setOptimize(Boolean optimize) {
            this.optimize = optimize;
        }

        public String getGraphPath() {
            return graphPath;
        }

        public void setGraphPath(String graphPath) {
            if (StringUtils.isNotBlank(graphPath))
                this.graphPath = Paths.get(graphPath).toAbsolutePath().toString();
            else this.graphPath = graphPath;
        }

        public Map<String, String> getEncoderOptions() {
            return encoderOptions;
        }

        public String getEncoderOptionsString() {
            if (encoderOptions == null || encoderOptions.isEmpty())
                return "";
            StringBuilder output = new StringBuilder();
            for (Map.Entry<String, String> entry : encoderOptions.entrySet()) {
                if (!output.isEmpty()) {
                    output.append("|");
                }
                output.append(entry.getKey()).append("=").append(entry.getValue());
            }
            return output.toString();
        }

        public void setEncoderOptions(Map<String, String> encoderOptions) {
            this.encoderOptions = encoderOptions;
        }

//        For later use when refactoring RoutingManagerConfiguration
//        public PreparationProperties getPreparation() {
//            return preparation;
//        }
//
//        public void setPreparation(PreparationProperties preparation) {
//            this.preparation = preparation;
//        }
//
//        public ExecutionProperties getExecution() {
//            return execution;
//        }
//
//        public void setExecution(ExecutionProperties execution) {
//            this.execution = execution;
//        }

        public Map<String, Object> getPreparation() {
            return preparation;
        }

        public void setPreparation(Map<String, Object> preparation) {
            this.preparation = preparation;
        }

        public Map<String, Object> getExecution() {
            return execution;
        }

        public void setExecution(Map<String, Object> execution) {
            this.execution = execution;
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

        public double getMaximumDistance() {
            return maximumDistance != null ? maximumDistance : 0;
        }

        public void setMaximumDistance(double maximumDistance) {
            this.maximumDistance = maximumDistance;
        }

        public double getMaximumDistanceDynamicWeights() {
            return maximumDistanceDynamicWeights != null ? maximumDistanceDynamicWeights : 0;
        }

        public void setMaximumDistanceDynamicWeights(double maximumDistanceDynamicWeights) {
            this.maximumDistanceDynamicWeights = maximumDistanceDynamicWeights;
        }

        public double getMaximumDistanceAvoidAreas() {
            return maximumDistanceAvoidAreas != null ? maximumDistanceAvoidAreas : 0;
        }

        public void setMaximumDistanceAvoidAreas(double maximumDistanceAvoidAreas) {
            this.maximumDistanceAvoidAreas = maximumDistanceAvoidAreas;
        }

        public double getMaximumDistanceAlternativeRoutes() {
            return maximumDistanceAlternativeRoutes != null ? maximumDistanceAlternativeRoutes : 0;
        }

        public void setMaximumDistanceAlternativeRoutes(double maximumDistanceAlternativeRoutes) {
            this.maximumDistanceAlternativeRoutes = maximumDistanceAlternativeRoutes;
        }

        public double getMaximumDistanceRoundTripRoutes() {
            return maximumDistanceRoundTripRoutes != null ? maximumDistanceRoundTripRoutes : 0;
        }

        public void setMaximumDistanceRoundTripRoutes(double maximumDistanceRoundTripRoutes) {
            this.maximumDistanceRoundTripRoutes = maximumDistanceRoundTripRoutes;
        }

        public double getMaximumSpeedLowerBound() {
            return maximumSpeedLowerBound != null ? maximumSpeedLowerBound : 0;
        }

        public void setMaximumSpeedLowerBound(Double maximumSpeedLowerBound) {
            this.maximumSpeedLowerBound = maximumSpeedLowerBound;
        }

        public int getMaximumWayPoints() {
            return maximumWayPoints != null ? maximumWayPoints : 0;
        }

        public void setMaximumWayPoints(int maximumWayPoints) {
            this.maximumWayPoints = maximumWayPoints;
        }

        public int getMaximumSnappingRadius() {
            return maximumSnappingRadius != null ? maximumSnappingRadius : 0;
        }

        public void setMaximumSnappingRadius(int maximumSnappingRadius) {
            this.maximumSnappingRadius = maximumSnappingRadius;
        }

        public int getMaximumVisitedNodes() {
            return maximumVisitedNodes != null ? maximumVisitedNodes : 0;
        }

        public void setMaximumVisitedNodes(Integer maximumVisitedNodes) {
            this.maximumVisitedNodes = maximumVisitedNodes;
        }

        public int getEncoderFlagsSize() {
            return encoderFlagsSize != null ? encoderFlagsSize : 0;
        }

        public void setEncoderFlagsSize(Integer encoderFlagsSize) {
            this.encoderFlagsSize = encoderFlagsSize;
        }

        public Integer getLocationIndexResolution() {
            return locationIndexResolution != null ? locationIndexResolution : 0;
        }

        public void setLocationIndexResolution(Integer locationIndexResolution) {
            this.locationIndexResolution = locationIndexResolution;
        }

        public Integer getLocationIndexSearchIterations() {
            return locationIndexSearchIterations != null ? locationIndexSearchIterations : 0;
        }

        public void setLocationIndexSearchIterations(Integer locationIndexSearchIterations) {
            this.locationIndexSearchIterations = locationIndexSearchIterations;
        }

        public boolean getForceTurnCosts() {
            return forceTurnCosts != null && forceTurnCosts;
        }

        public void setForceTurnCosts(Boolean forceTurnCosts) {
            this.forceTurnCosts = forceTurnCosts;
        }

        public String getGtfsFile() {
            return gtfsFile != null ? gtfsFile : "";
        }

        public void setGtfsFile(String gtfsFile) {
            if (StringUtils.isNotBlank(gtfsFile))
                this.gtfsFile = Paths.get(gtfsFile).toAbsolutePath().toString();
            else this.gtfsFile = gtfsFile;
        }

//        For later use when refactoring RoutingManagerConfiguration
//        public static class PreparationProperties {
//            private int minNetworkSize;
//            private int minOneWayNetworkSize;
//
//            public MethodsProperties getMethods() {
//                return methods;
//            }
//
//            public PreparationProperties setMethods(MethodsProperties methods) {
//                this.methods = methods;
//                return this;
//            }
//
//            private MethodsProperties methods;
//
//            public int getMinNetworkSize() {
//                return minNetworkSize;
//            }
//
//            public void setMinNetworkSize(int minNetworkSize) {
//                this.minNetworkSize = minNetworkSize;
//            }
//
//            public int getMinOneWayNetworkSize() {
//                return minOneWayNetworkSize;
//            }
//
//            public void setMinOneWayNetworkSize(int minOneWayNetworkSize) {
//                this.minOneWayNetworkSize = minOneWayNetworkSize;
//            }
//        }
//
//        public static class MethodsProperties {
//            private CHProperties ch;
//            private LMProperties lm;
//            private CoreProperties core;
//            private FastIsochroneProperties fastisochrones;
//
//            public CHProperties getCh() {
//                return ch;
//            }
//
//            public void setCh(CHProperties ch) {
//                this.ch = ch;
//            }
//
//            public LMProperties getLm() {
//                return lm;
//            }
//
//            public void setLm(LMProperties lm) {
//                this.lm = lm;
//            }
//
//            public CoreProperties getCore() {
//                return core;
//            }
//
//            public void setCore(CoreProperties core) {
//                this.core = core;
//            }
//
//            public FastIsochroneProperties getFastisochrones() {
//                return fastisochrones;
//            }
//
//            public void setFastisochrones(FastIsochroneProperties fastisochrones) {
//                this.fastisochrones = fastisochrones;
//            }
//
//        }
//
//        public static class CHProperties {
//            //TBD
//        }
//
//        public static class LMProperties {
//            //TBD
//        }
//
//        public static class CoreProperties {
//            //TBD
//        }
//
//        public static class FastIsochroneProperties {
//            private boolean enabled;
//            private int threads;
//            private String weightings;
//            private int maxcellnodes;
//
//            public boolean isEnabled() {
//                return enabled;
//            }
//
//            public void setEnabled(boolean enabled) {
//                this.enabled = enabled;
//            }
//
//            public int getThreads() {
//                return threads;
//            }
//
//            public void setThreads(int threads) {
//                this.threads = threads;
//            }
//
//            public String getWeightings() {
//                return weightings;
//            }
//
//            public void setWeightings(String weightings) {
//                this.weightings = weightings;
//            }
//
//            public int getMaxcellnodes() {
//                return maxcellnodes;
//            }
//
//            public void setMaxcellnodes(int maxcellnodes) {
//                this.maxcellnodes = maxcellnodes;
//            }
//        }
//
//        public static class ExecutionProperties {
//            private Map<String, Map<String, String>> methods;
//
//            public Map<String, Map<String, String>> getMethods() {
//                return methods;
//            }
//
//            public void setMethods(Map<String, Map<String, String>> methods) {
//                this.methods = methods;
//            }
//        }
    }
}
