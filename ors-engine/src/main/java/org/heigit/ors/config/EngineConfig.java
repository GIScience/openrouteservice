package org.heigit.ors.config;

import org.heigit.ors.routing.configuration.RouteProfileConfiguration;

public record EngineConfig(int initializationThreads, boolean preparationMode, String sourceFile,
                              String graphsRootPath, String graphsDataAccess, boolean elevationPreprocessed,
                              RouteProfileConfiguration[] profiles) {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int initializationThreads;
        private boolean preparationMode;
        private String sourceFile;
        private String graphsRootPath;
        private String graphsDataAccess;
        private boolean elevationPreprocessed;
        private RouteProfileConfiguration[] profiles;

        public Builder initializationThreads(int initializationThreads) {
            this.initializationThreads = initializationThreads;
            return this;
        }

        public Builder preparationMode(boolean preparationMode) {
            this.preparationMode = preparationMode;
            return this;
        }

        public Builder sourceFile(String sourceFile) {
            this.sourceFile = sourceFile;
            return this;
        }

        public Builder graphsRootPath(String graphsRootPath) {
            this.graphsRootPath = graphsRootPath;
            return this;
        }

        public Builder graphsDataAccess(String graphsDataAccess) {
            this.graphsDataAccess = graphsDataAccess;
            return this;
        }

        public Builder elevationPreprocessed(boolean elevationPreprocessed) {
            this.elevationPreprocessed = elevationPreprocessed;
            return this;
        }

        public Builder profiles(RouteProfileConfiguration[] profiles) {
            this.profiles = profiles;
            return this;
        }

        public EngineConfig build() {
            return new EngineConfig(initializationThreads, preparationMode, sourceFile, graphsRootPath, graphsDataAccess, elevationPreprocessed, profiles);
        }
    }
}