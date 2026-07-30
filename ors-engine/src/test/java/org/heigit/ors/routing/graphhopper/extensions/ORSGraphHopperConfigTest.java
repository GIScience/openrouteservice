package org.heigit.ors.routing.graphhopper.extensions;

import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.common.DataAccessEnum;
import org.heigit.ors.common.EncoderNameEnum;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.nio.file.Path;
import java.util.Arrays;


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
        if (cachePath != null) {
            engineConfig.getElevation().setProvider(expectedProvider);
        }
        engineConfig.getElevation().setDataAccess(DataAccessEnum.MMAP);

        String graphLocation = "";

        ORSGraphHopperConfig config = ORSGraphHopperConfig.createGHSettings(profile, engineConfig, graphLocation);

        assertEquals(setElevation, config.toString().contains("graph.elevation.provider"));
    }

    @ParameterizedTest
    @CsvSource({
            "DRIVING_CAR, sac_scale, false",
            "DRIVING_CAR, mtb_scale, false",
            "DRIVING_CAR, hill_index, false",
            "DRIVING_CAR, max_height, true",
            "DRIVING_CAR, road_environment, true",
            "FOOT_HIKING, sac_scale, true",
            "FOOT_HIKING, max_height, false",
            "FOOT_HIKING, max_width, false",
            "CYCLING_MOUNTAIN, sac_scale, true",
            "CYCLING_MOUNTAIN, max_height, true",
            "WHEELCHAIR, sac_scale, false",
            "WHEELCHAIR, mtb_scale, false",
            "WHEELCHAIR, max_height, false"
    })
    void testEncodedValuesLeakagePrevention(EncoderNameEnum encoderName, String ev, boolean shouldContain) {
        ProfileProperties profile = new ProfileProperties();
        profile.setEncoderName(encoderName);
        profile.getBuild().getEncodedValues().setSacScale(true);
        profile.getBuild().getEncodedValues().setMtbScale(true);
        profile.getBuild().getEncodedValues().setHillIndex(true);
        profile.getBuild().getEncodedValues().setMaxHeight(true);
        profile.getBuild().getEncodedValues().setMaxWidth(true);
        profile.getBuild().getEncodedValues().setRoadEnvironment(true);
        profile.setProfileName(encoderName.toString());

        EngineProperties engineConfig = new EngineProperties();
        engineConfig.setGraphsDataAccess(DataAccessEnum.MMAP);
        engineConfig.getElevation().setCachePath(Path.of(""));
        
        ORSGraphHopperConfig config = ORSGraphHopperConfig.createGHSettings(profile, engineConfig, "");
        
        String encodedValues = config.getString("graph.encoded_values", "");
        assertEquals(shouldContain, Arrays.asList(encodedValues.split(",")).contains(ev));
    }
}
