package org.heigit.ors.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.common.DataAccessEnum;

import java.nio.file.Path;

@Getter
@Setter
public class ElevationProperties {
    private Boolean preprocessed;
    private DataAccessEnum dataAccess;
    private Boolean cacheClear;
    private String provider;
    private Path cachePath;

    public ElevationProperties() {
    }

    public ElevationProperties(String ignored) {
    }

    @JsonIgnore
    public Path getCachePath() {
        return cachePath != null ? cachePath.toAbsolutePath() : null;
    }
}