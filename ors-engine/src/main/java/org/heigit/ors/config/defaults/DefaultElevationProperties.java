package org.heigit.ors.config.defaults;

import lombok.EqualsAndHashCode;
import org.heigit.ors.common.DataAccessEnum;
import org.heigit.ors.config.ElevationProperties;

import java.nio.file.Paths;

@EqualsAndHashCode(callSuper = true)
public class DefaultElevationProperties extends ElevationProperties {

    public DefaultElevationProperties() {
        this(false);
    }

    public DefaultElevationProperties(Boolean setDefaults) {
        if (setDefaults) {
            setPreprocessed(false);
            setDataAccess(DataAccessEnum.MMAP);
            setCacheClear(false);
            setProvider("multi");
            setCachePath(Paths.get("./elevation_cache").toAbsolutePath());
        }
    }
}
