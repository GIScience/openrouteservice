package org.heigit.ors.api;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "engine")
public class EngineProperties {
    private int initThreads;
    private boolean preparationMode;
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
}
