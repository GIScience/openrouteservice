package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.profile.BuildProperties;
import org.heigit.ors.config.profile.ExtendedStorageName;
import org.heigit.ors.config.profile.ExtendedStorageProperties;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.routing.graphhopper.extensions.GraphProcessContext;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BorderParserTest {

    private ORSGraphHopper initializeGraphHopper(ExtendedStorageProperties parameters) throws Exception {
        parameters.setIds(Path.of("../ors-api/src/test/files/borders/ids.csv"));
        parameters.setOpenborders(Path.of("../ors-api/src/test/files/borders/openborders.csv"));

        BuildProperties buildProperties = new BuildProperties();
        buildProperties.setExtStorages(Map.of(ExtendedStorageName.BORDERS.getName(), parameters));

        ProfileProperties profileProperties = new ProfileProperties();
        profileProperties.setBuild(buildProperties);
        profileProperties.setEncoderName(EncoderNameEnum.DRIVING_CAR);

        return new ORSGraphHopper(new GraphProcessContext(profileProperties), new EngineProperties(), profileProperties);
    }

    /*
     * Test that the builder successfully initializes with a valid boundaries file and throws an exception
     * if the boundaries file is invalid.
     */
    @Test
    void TestInit() throws Exception {
        ExtendedStorageProperties parameters = new ExtendedStorageProperties();
        ORSGraphHopper gh = initializeGraphHopper(parameters);

        parameters.setBoundaries(Path.of("foo.bar"));
        assertThrows(RuntimeException.class, () -> {
            new BorderParser(gh);
        });

        parameters.setBoundaries(Path.of("../ors-api/src/test/files/borders/borders.geojson"));
        assertDoesNotThrow(() -> {
            new BorderParser(gh);
        });
    }

    /*
     * Test that the builder successfully initializes for preprocessed OSM data in case of invalid boundaries file,
     * as it is not needed for already annotated data.
     */
    @Test
    void TestInitPreprocessed() throws Exception {
        ExtendedStorageProperties parameters = new ExtendedStorageProperties();
        ORSGraphHopper gh = initializeGraphHopper(parameters);

        parameters.setPreprocessed(true);
        parameters.setBoundaries(Path.of("foo.bar"));
        assertDoesNotThrow(() -> {
            new BorderParser(gh);
        });
    }
}
