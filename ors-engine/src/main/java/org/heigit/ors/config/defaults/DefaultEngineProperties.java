package org.heigit.ors.config.defaults;

import org.heigit.ors.common.DataAccessEnum;
import org.heigit.ors.config.EngineProperties;

import java.nio.file.Path;
import java.util.LinkedHashMap;

public class DefaultEngineProperties extends EngineProperties {
    public DefaultEngineProperties() {
        this(false);
    }

    public DefaultEngineProperties(Boolean setDefaults) {
        super();
        if (setDefaults) {
            setSourceFile(Path.of(""));
            setInitThreads(2);
            setPreparationMode(false);
            setConfigOutputMode(false);
            setGraphsRootPath(Path.of("./graphs").toAbsolutePath());
            setGraphsDataAccess(DataAccessEnum.RAM_STORE);
            setElevation(new DefaultElevationProperties(true));
            setProfileDefault(new DefaultProfileProperties(true));
            setProfiles(new LinkedHashMap<>());
        }
    }
}