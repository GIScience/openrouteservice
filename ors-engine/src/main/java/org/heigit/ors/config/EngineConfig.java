package org.heigit.ors.config;

public class EngineConfig {
// Migration guide: 1. add field and getter, assign in constructor

    private final int initializationThreads;
    private final boolean preparationMode;
    public int getInitializationThreads() {
        return initializationThreads;
    }

    public boolean isPreparationMode () {
        return preparationMode;
    }

    public EngineConfig(EngineConfigBuilder builder) {
        this.initializationThreads = builder.initializationThreads;
        this.preparationMode = builder.preparationMode;
    }

    public static class EngineConfigBuilder {
// Migration guide: 2. add corresponding field (without final)
        private int initializationThreads = 1;
        private boolean preparationMode;
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

            return new EngineConfig(this);
        }
    }
}
