package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;

public class ElevationProperties {
    private boolean preprocessed = false;
    @JsonProperty("data_access")
    private String dataAccess = "MMAP";
    @JsonProperty("cache_clear")
    private boolean cacheClear = false;
    @JsonProperty("provider")
    private String provider = "multi";
    @JsonProperty("cache_path")
    private String cachePath = "./elevation_cache";

    public boolean isPreprocessed() {
        return preprocessed;
    }

    public void setPreprocessed(boolean preprocessed) {
        this.preprocessed = preprocessed;
    }

    public String getDataAccess() {
        return dataAccess;
    }

    public void setDataAccess(String dataAccess) {
        this.dataAccess = dataAccess;
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
}