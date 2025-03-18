package org.heigit.ors.routing.graphhopper.extensions;

import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.profile.ProfileProperties;
import org.junit.jupiter.api.Test;
import org.heigit.ors.common.DataAccessEnum;
import org.heigit.ors.common.EncoderNameEnum;

import java.nio.file.Path;


class ORSGraphHopperConfigTest {
    @Test
    void createGHSettingsWithoutElevation() {
        ProfileProperties profile = new ProfileProperties();
        profile.setEncoderName(EncoderNameEnum.DRIVING_CAR);
        profile.getBuild().setElevation(false);
        profile.setProfileName("bobby-car");

        EngineProperties engineConfig = new EngineProperties();
        engineConfig.setGraphsDataAccess(DataAccessEnum.MMAP);
        engineConfig.getElevation().setCachePath(Path.of("/elevation_cache"));
        engineConfig.getElevation().setDataAccess(DataAccessEnum.MMAP);

        String graphLocation = "";

        ORSGraphHopperConfig config = ORSGraphHopperConfig.createGHSettings(profile, engineConfig, graphLocation);

        assert !config.toString().contains("graph.elevation.provider");
    }

    @Test
    void createGHSettingsWithElevation() {
        ProfileProperties profile = new ProfileProperties();
        profile.setEncoderName(EncoderNameEnum.DRIVING_CAR);
        profile.getBuild().setElevation(true);
        profile.setProfileName("bobby-car");

        EngineProperties engineConfig = new EngineProperties();
        engineConfig.setGraphsDataAccess(DataAccessEnum.MMAP);
        engineConfig.getElevation().setCachePath(Path.of("/elevation_cache"));
        engineConfig.getElevation().setProvider("srtm");
        engineConfig.getElevation().setDataAccess(DataAccessEnum.MMAP);

        String graphLocation = "";

        ORSGraphHopperConfig config = ORSGraphHopperConfig.createGHSettings(profile, engineConfig, graphLocation);

        assert config.toString().contains("graph.elevation.provider");
    }

    @Test
    void createGHSettingsWithElevationNoCachePath() {
        ProfileProperties profile = new ProfileProperties();
        profile.setEncoderName(EncoderNameEnum.DRIVING_CAR);
        profile.getBuild().setElevation(true);
        profile.setProfileName("bobby-car");

        EngineProperties engineConfig = new EngineProperties();
        engineConfig.setGraphsDataAccess(DataAccessEnum.MMAP);
        engineConfig.getElevation().setCachePath(null);
        engineConfig.getElevation().setProvider("srtm");
        engineConfig.getElevation().setDataAccess(DataAccessEnum.MMAP);

        String graphLocation = "";

        ORSGraphHopperConfig config = ORSGraphHopperConfig.createGHSettings(profile, engineConfig, graphLocation);

        assert !config.toString().contains("graph.elevation.provider");
        }

    @Test
    void createGHSettingsWithElevationNoProvider() {
        ProfileProperties profile = new ProfileProperties();
        profile.setEncoderName(EncoderNameEnum.DRIVING_CAR);
        profile.getBuild().setElevation(true);
        profile.setProfileName("bobby-car");

        EngineProperties engineConfig = new EngineProperties();
        engineConfig.setGraphsDataAccess(DataAccessEnum.MMAP);
        engineConfig.getElevation().setCachePath(Path.of("/elevation_cache"));
        engineConfig.getElevation().setProvider(null);
        engineConfig.getElevation().setDataAccess(DataAccessEnum.MMAP);

        String graphLocation = "";

        ORSGraphHopperConfig config = ORSGraphHopperConfig.createGHSettings(profile, engineConfig, graphLocation);

        assert !config.toString().contains("graph.elevation.provider");
    }
}
