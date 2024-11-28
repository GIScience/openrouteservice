package org.heigit.ors.config;

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
        // This constructor is used to create an empty object for the purpose of ignoring it in the JSON serialization.
    }

    public Path getCachePath() {
        return cachePath != null ? cachePath.toAbsolutePath() : null;
    }
}