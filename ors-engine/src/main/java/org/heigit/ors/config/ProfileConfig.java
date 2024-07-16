package org.heigit.ors.config;

import com.typesafe.config.Config;
import org.apache.commons.lang3.StringUtils;
import org.heigit.ors.routing.configuration.RouteProfileConfiguration;

import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.Map;

public class ProfileConfig {

    private String name;
    private String encoderName;
    private String graphPath;
    private String gtfsFile;

    private boolean enabled;
    private boolean elevation;
    private boolean elevationSmoothing;
    private boolean instructions;
    private boolean optimize;
    private boolean traffic;
    private boolean interpolateBridgesAndTunnels;
    private boolean forceTurnCosts;
    private int encoderFlagsSize;
    private int locationIndexResolution;
    private int locationIndexSearchIterations;

    private double maximumDistance;
    private double maximumDistanceDynamicWeights;
    private double maximumDistanceAvoidAreas;
    private double maximumDistanceAlternativeRoutes;
    private double maximumDistanceRoundTripRoutes;
    private double maximumSpeedLowerBound;
    private int maximumWayPoints;
    private int maximumSnappingRadius;
    private int maximumVisitedNodes;
    private int maximumVisitedNodesPT;

    private String elevationProvider = null;
    private String elevationCachePath = null;
    private String elevationDataAccess = "MMAP";
    private boolean elevationCacheClear = true;

    private String encoderOptions;

//        private PreparationProperties preparation;
    private Map<String, Object> preparation;
//        private ExecutionProperties execution;
    private Map<String, Object> execution;
    private Map<String, Map<String, String>> extStorages;
    private Map<String, Map<String, String>> graphBuilders;

    private Config isochronePreparationOpts;
    private Config preparationOpts;
    private Config executionOpts;





    private boolean turnCostEnabled = false;//FIXME: even though the field is read by external methods, its setter is never called.
    private boolean enforceTurnCosts = false;
    private String graphDataAccess = "";



    public ProfileConfig(EngineConfigBuilder builder) {
    }

    public static class EngineConfigBuilder {
        private int initializationThreads = 1;
        private boolean preparationMode;
        private String sourceFile;
        private String graphsRootPath;
        private String graphsDataAccess;
        private boolean elevationPreprocessed;
        private RouteProfileConfiguration[] profiles;

        public static EngineConfigBuilder init() {
            return new EngineConfigBuilder();
        }

        public EngineConfigBuilder setInitializationThreads(int initializationThreads) {
            this.initializationThreads = initializationThreads;
            return this;
        }

        public EngineConfigBuilder setPreparationMode(boolean preparationMode) {
            this.preparationMode = preparationMode;
            return this;
        }

        public EngineConfigBuilder setSourceFile(String sourceFile) {
            if (StringUtils.isNotBlank(sourceFile))
                this.sourceFile = Paths.get(sourceFile).toAbsolutePath().toString();
            else this.sourceFile = sourceFile;
            return this;
        }

        public EngineConfigBuilder setGraphsRootPath(String graphsRootPath) {
            if (StringUtils.isNotBlank(graphsRootPath))
                this.graphsRootPath = Paths.get(graphsRootPath).toAbsolutePath().toString();
            else this.graphsRootPath = graphsRootPath;
            return this;
        }

        public EngineConfigBuilder setGraphsDataAccess(String graphsDataAccess) {
            this.graphsDataAccess = graphsDataAccess;
            return this;
        }

        public EngineConfigBuilder setElevationPreprocessed(boolean elevationPreprocessed) {
            this.elevationPreprocessed = elevationPreprocessed;
            return this;
        }

        public EngineConfigBuilder setProfiles(RouteProfileConfiguration[] profiles) {
            this.profiles = profiles;
            return this;
        }

        public ProfileConfig build() {
            return new ProfileConfig(this);
        }
    }

    public static class EncoderOptionsConfig {
        private boolean blockFords;
        private boolean considerElevation;
        private boolean turnCosts;
        private boolean useAcceleration;
        private int maximumGradeLevel;
        private double preferredSpeedFactor;
        private double problematicSpeedFactor;

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
}
