package org.heigit.ors.config;

public class EngineConfig {
// Migration guide: 1. add field and getter, assign in constructor

    private final int initializationThreads;

    public int getInitializationThreads() {
        return initializationThreads;
    }

    public EngineConfig(EngineConfigBuilder builder) {
        this.initializationThreads = builder.initializationThreads;
    }

    public static class EngineConfigBuilder {
// Migration guide: 2. add corresponding field (without final)
        private int initializationThreads = 1;

        public static EngineConfigBuilder init() {
            return new EngineConfigBuilder();
        }

        public static EngineConfigBuilder initFromAppConfig() {
            AppConfig deprecatedAppConfig = AppConfig.getGlobal();
            final String SERVICE_NAME_ROUTING = "routing";

// Migration guide: 3. add fetching from old AppConfig
            int initializationThreads = 1;
            String value = deprecatedAppConfig.getServiceParameter(SERVICE_NAME_ROUTING, "init_threads");
            if (value != null)
                initializationThreads = Integer.parseInt(value);

            return new EngineConfigBuilder()
                    .setInitializationThreads(initializationThreads);
        }

// Migration guide: 4. add chainable setter
        public EngineConfigBuilder setInitializationThreads(int initializationThreads) {
            this.initializationThreads = initializationThreads;
            return this;
        }

        public EngineConfig build() {
            return new EngineConfig(this);
        }
    }
}
