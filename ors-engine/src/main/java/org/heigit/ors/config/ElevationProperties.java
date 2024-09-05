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

    public Path getCachePath() {
        return cachePath.toAbsolutePath();
    }
}