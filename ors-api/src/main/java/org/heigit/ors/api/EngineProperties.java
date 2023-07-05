package org.heigit.ors.api;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "engine")
public class EngineProperties {
    private int initThreads;
    private boolean preparationMode;
    private String sourceFile;
    private String graphsRootPath;
    private ElevationProperties elevation;
    private ProfileProperties profileDefault;
    private List<ProfileProperties> profiles;

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
        this.sourceFile = sourceFile;
    }

    public String getGraphsRootPath() {
        return graphsRootPath;
    }

    public void setGraphsRootPath(String graphsRootPath) {
        this.graphsRootPath = graphsRootPath;
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

    public List<ProfileProperties> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<ProfileProperties> profiles) {
        this.profiles = profiles;
    }

    public static class ElevationProperties {
        private boolean preprocessed;
        private boolean cacheClear;
        private boolean smoothing;
        private String provider;
        private String cachePath;

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

        public boolean isSmoothing() {
            return smoothing;
        }

        public void setSmoothing(boolean smoothing) {
            this.smoothing = smoothing;
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
            this.cachePath = cachePath;
        }
    }

    public static class ProfileProperties {
        private String type;
        private boolean elevation;
        private boolean traffic;
        private Map<String, String> encoderOptions;
        private PreparationProperties preparation;
        private ExecutionProperties execution;
        private Map<String, Map<String, String>> extStorages;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isElevation() {
            return elevation;
        }

        public void setElevation(boolean elevation) {
            this.elevation = elevation;
        }

        public boolean isTraffic() {
            return traffic;
        }

        public void setTraffic(boolean traffic) {
            this.traffic = traffic;
        }

        public Map<String, String> getEncoderOptions() {
            return encoderOptions;
        }

        public void setEncoderOptions(Map<String, String> encoderOptions) {
            this.encoderOptions = encoderOptions;
        }

        public PreparationProperties getPreparation() {
            return preparation;
        }

        public void setPreparation(PreparationProperties preparation) {
            this.preparation = preparation;
        }

        public ExecutionProperties getExecution() {
            return execution;
        }

        public void setExecution(ExecutionProperties execution) {
            this.execution = execution;
        }

        public Map<String, Map<String, String>> getExtStorages() {
            return extStorages;
        }

        public void setExtStorages(Map<String, Map<String, String>> extStorages) {
            this.extStorages = extStorages;
        }

        public static class PreparationProperties {
            private int minNetworkSize;
            private int minOneWayNetworkSize;
            private Map<String, Map<String, String>> methods;

            public int getMinNetworkSize() {
                return minNetworkSize;
            }

            public void setMinNetworkSize(int minNetworkSize) {
                this.minNetworkSize = minNetworkSize;
            }

            public int getMinOneWayNetworkSize() {
                return minOneWayNetworkSize;
            }

            public void setMinOneWayNetworkSize(int minOneWayNetworkSize) {
                this.minOneWayNetworkSize = minOneWayNetworkSize;
            }

            public Map<String, Map<String, String>> getMethods() {
                return methods;
            }

            public void setMethods(Map<String, Map<String, String>> methods) {
                this.methods = methods;
            }
        }

        public static class ExecutionProperties {
            private Map<String, Map<String, String>> methods;

            public Map<String, Map<String, String>> getMethods() {
                return methods;
            }

            public void setMethods(Map<String, Map<String, String>> methods) {
                this.methods = methods;
            }
        }
    }
}
