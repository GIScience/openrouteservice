package org.heigit.ors.config;

import com.typesafe.config.Config;
import org.apache.commons.lang3.StringUtils;
import org.heigit.ors.routing.configuration.RouteProfileConfiguration;
import org.locationtech.jts.geom.Envelope;

import java.nio.file.Paths;
import java.util.Map;

public class ProfileConfig {

    // TODO this is supposed to replace RouteProfileConfiguration, also checking all params if still needed.
    private String name = "";
    private boolean enabled = true;
    private String profiles = ""; // comma separated
    private String graphPath;
    private Map<String, Map<String, String>> extStorages;
    private Map<String, Map<String, String>> graphBuilders;
    private Double maximumDistance = 0.0;
    private Double maximumDistanceDynamicWeights = 0.0;
    private Double maximumDistanceAvoidAreas = 0.0;
    private Double maximumDistanceAlternativeRoutes = 0.0;
    private Double maximumDistanceRoundTripRoutes = 0.0;
    private Integer maximumWayPoints = 0;
    private boolean instructions = true;
    private boolean optimize = false;

    private int encoderFlagsSize = 4;
    private String encoderOptions = "";
    private String gtfsFile = "";
    private Config isochronePreparationOpts;
    private Config preparationOpts;
    private Config executionOpts;

    private String elevationProvider = null;
    private String elevationCachePath = null;
    private String elevationDataAccess = "MMAP";
    private boolean elevationCacheClear = true;
    private boolean elevationSmoothing = true;
    private boolean interpolateBridgesAndTunnels = true;
    private int maximumSnappingRadius = 350;

    private Envelope extent;
    private boolean hasMaximumSnappingRadius = false;

    private int locationIndexResolution = 500;
    private int locationIndexSearchIterations = 4;

    private double maximumSpeedLowerBound = 80;

    private final int trafficExpirationMin = 15;

    private int maximumVisitedNodesPT = 1000000;

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
}
