package org.heigit.ors.config;

import org.apache.commons.lang3.StringUtils;
import org.heigit.ors.routing.configuration.RouteProfileConfiguration;
import org.heigit.ors.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class EngineConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(EngineConfig.class);
    public static final String GRAPH_VERSION;

    static {
        Properties prop = new Properties();
        String graphVersion = "0";
        try (InputStream in = new ClassPathResource("engine.properties").getInputStream()) {
            prop.load(in);
            graphVersion = prop.getProperty("graphVersion", "0");
        } catch (Exception e) {
            LOGGER.error("Initialization ERROR: cannot read engineVersion. {}", e.getMessage());
        }
        GRAPH_VERSION = graphVersion;
    }

    // Migration guide: 1. add field and getter, assign in constructor
    private final int initializationThreads;
    private final boolean preparationMode;
    private final String sourceFile;
    private final int maxNumberOfGraphBackups;
    private final String graphVersion;
    private final String graphsRootPath;
    private final String graphsRepoName;
    private final String graphsRepoUrl;
    private final String graphsRepoPath;
    private final String graphsExtent;
    private final String graphsProfileGroup;
    private final String graphsDataAccess;
    private final boolean elevationPreprocessed;
    private final RouteProfileConfiguration[] profiles;

    public String getGraphsRepoPath() {
        return graphsRepoPath;
    }

    public int getInitializationThreads() {
        return initializationThreads;
    }

    public boolean isPreparationMode() {
        return preparationMode;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public int getMaxNumberOfGraphBackups() {
        return maxNumberOfGraphBackups;
    }

    public String getGraphVersion() {
        return graphVersion;
    }

    public String getGraphsRootPath() {
        return graphsRootPath;
    }

    public String getGraphsDataAccess() {
        return graphsDataAccess;
    }

    public String getGraphsRepoName() {
        return graphsRepoName;
    }

    public String getGraphsRepoUrl() {
        return graphsRepoUrl;
    }

    public String getGraphsExtent() {
        return graphsExtent;
    }

    public String getGraphsProfileGroup() {
        return graphsProfileGroup;
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
        this.maxNumberOfGraphBackups = builder.maxNumberOfGraphBackups;
        this.elevationPreprocessed = builder.elevationPreprocessed;
        this.graphVersion = builder.graphVersion;
        this.graphsRootPath = builder.graphsRootPath;
        this.graphsRepoUrl = builder.graphsRepoUrl;
        this.graphsRepoPath = builder.graphsRepoPath;
        this.graphsRepoName = builder.graphsRepoName;
        this.graphsExtent = builder.graphsExtent;
        this.graphsProfileGroup = builder.graphsProfileGroup;
        this.profiles = builder.profiles;
        this.graphsDataAccess = builder.graphsDataAccess;
    }

    public static class EngineConfigBuilder {
        // Migration guide: 2. add corresponding field (without final)
        private int initializationThreads = 1;
        private boolean preparationMode;
        private String sourceFile;
        private int maxNumberOfGraphBackups;
        private String graphVersion;
        private String graphsRootPath;
        private String graphsRepoUrl;
        private String graphsRepoPath;
        private String graphsRepoName;
        private String graphsExtent;
        private String graphsProfileGroup;
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
            if (StringUtils.isNotBlank(sourceFile))
                this.sourceFile = Paths.get(sourceFile).toAbsolutePath().toString();
            else this.sourceFile = sourceFile;
            return this;
        }

        public EngineConfigBuilder setMaxNumberOfGraphBackups(int maxNumberOfGraphBackups) {
            this.maxNumberOfGraphBackups = maxNumberOfGraphBackups;
            return this;
        }

        public EngineConfigBuilder setGraphVersion(String graphVersion) {
            this.graphVersion = graphVersion;
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

        public EngineConfigBuilder setGraphsRepoName(String repoName) {
            this.graphsRepoName = repoName;
            return this;
        }

        public EngineConfigBuilder setGraphsRepoPath(String repoPath) {
            this.graphsRepoPath = repoPath;
            return this;
        }

        public EngineConfigBuilder setGraphsRepoUrl(String url) {
            this.graphsRepoUrl = url;
            return this;
        }

        public EngineConfigBuilder setGraphsExtent(String extent) {
            this.graphsExtent = extent;
            return this;
        }

        public EngineConfigBuilder setGraphsProfileGroup(String profileGroup) {
            this.graphsProfileGroup = profileGroup;
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
