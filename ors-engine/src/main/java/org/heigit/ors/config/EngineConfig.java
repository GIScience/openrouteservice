package org.heigit.ors.config;

import org.heigit.ors.routing.configuration.RouteProfileConfiguration;
import org.heigit.ors.util.StringUtility;

import java.util.List;
import java.util.Map;

public class EngineConfig {
    // Migration guide: 1. add field and getter, assign in constructor
    private final int initializationThreads;
    private final boolean preparationMode;
    private final String sourceFile;
    private final String graphsRootPath;
    private final String graphsDataAccess;
    private final boolean elevationPreprocessed;
    private final RouteProfileConfiguration[] profiles;

    public int getInitializationThreads() {
        return initializationThreads;
    }

    public boolean isPreparationMode() {
        return preparationMode;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public String getGraphsRootPath() {
        return graphsRootPath;
    }

    public String getGraphsDataAccess() {
        return graphsDataAccess;
    }

    public boolean isElevationPreprocessed() {
        return elevationPreprocessed;
    }

    public RouteProfileConfiguration[] getProfiles() {
        return profiles;
    }

    public EngineConfig(EngineConfigBuilder builder) {
        this.initializationThreads = builder.initializationThreads;
        this.preparationMode = builder.preparationMode;
        this.sourceFile = builder.sourceFile;
        this.elevationPreprocessed = builder.elevationPreprocessed;
        this.graphsRootPath = builder.graphsRootPath;
        this.profiles = builder.profiles;
        this.graphsDataAccess = builder.graphsDataAccess;
    }

    public static class EngineConfigBuilder {
        // Migration guide: 2. add corresponding field (without final)
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

        // Migration guide: 3. add chainable setter
        public EngineConfigBuilder setInitializationThreads(int initializationThreads) {
            this.initializationThreads = initializationThreads;
            return this;
        }

        public EngineConfigBuilder setPreparationMode(boolean preparationMode) {
            this.preparationMode = preparationMode;
            return this;
        }

        public EngineConfigBuilder setSourceFile(String sourceFile) {
            this.sourceFile = sourceFile;
            return this;
        }

        public EngineConfigBuilder setGraphsRootPath(String graphsRootPath) {
            this.graphsRootPath = graphsRootPath;
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

        public EngineConfig build() {
            return new EngineConfig(this);
        }

        public EngineConfig buildWithAppConfigOverride() {
            AppConfig deprecatedAppConfig = AppConfig.getGlobal();
            final String SERVICE_NAME_ROUTING = "routing";

// Migration guide: 4. add fetching from old AppConfig
            String value = deprecatedAppConfig.getServiceParameter(SERVICE_NAME_ROUTING, "init_threads");
            if (value != null)
                this.initializationThreads = Integer.parseInt(value);

            value = deprecatedAppConfig.getServiceParameter(SERVICE_NAME_ROUTING, "mode");
            if (value != null)
                this.preparationMode = "preparation".equalsIgnoreCase(value);

            List<String> sources = deprecatedAppConfig.getServiceParametersList(SERVICE_NAME_ROUTING, "sources");
            if (!sources.isEmpty())
                this.sourceFile = sources.get(0);

            value = deprecatedAppConfig.getServiceParameter(SERVICE_NAME_ROUTING, "elevation_preprocessed");
            if (value != null)
                elevationPreprocessed = "true".equalsIgnoreCase(value);

            Map<String, Object> defaultParams = deprecatedAppConfig.getServiceParametersMap(SERVICE_NAME_ROUTING, "profiles.default_params", true);
            if (defaultParams != null && defaultParams.containsKey("graphs_root_path"))
                graphsRootPath = StringUtility.trim(defaultParams.get("graphs_root_path").toString(), '"');

            return new EngineConfig(this);
        }
    }
}
