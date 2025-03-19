package org.heigit.ors.routing.graphhopper.extensions;

import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.common.DataAccessEnum;
import org.heigit.ors.common.EncoderNameEnum;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.nio.file.Path;


class ORSGraphHopperConfigTest {
    @ParameterizedTest
    @CsvSource({"true,/elevation_cache,srtm", "false,/elevation_cache,null", "true,null,srtm"})
    void createGHSettings(Boolean setElevation, String cachePath, String expectedProvider) {
        ProfileProperties profile = new ProfileProperties();
        profile.setEncoderName(EncoderNameEnum.DRIVING_CAR);
        profile.getBuild().setElevation(setElevation);
        profile.setProfileName("bobby-car");

        EngineProperties engineConfig = new EngineProperties();
        engineConfig.setGraphsDataAccess(DataAccessEnum.MMAP);
        engineConfig.getElevation().setCachePath(Path.of(cachePath));
        engineConfig.getElevation().setProvider(expectedProvider);
        engineConfig.getElevation().setDataAccess(DataAccessEnum.MMAP);

        String graphLocation = "";

        ORSGraphHopperConfig config = ORSGraphHopperConfig.createGHSettings(profile, engineConfig, graphLocation);

        assertEquals(setElevation, config.toString().contains("graph.elevation.provider"));
    }
}
