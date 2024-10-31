package org.heigit.ors.config.profile;

import org.heigit.ors.common.EncoderNameEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileDefaultPropertiesTest {

    ProfileDefaultProperties profileDefaultProperties;
    ProfileProperties carProfile;

    @BeforeEach
    void setUp() {
        profileDefaultProperties = new ProfileDefaultProperties();
        profileDefaultProperties.setEnabled(true);
        profileDefaultProperties.setGraphPath(Path.of("/path/to/graphs"));
        profileDefaultProperties.getBuild().setSourceFile(Path.of("/path/to/source/file"));
        profileDefaultProperties.getBuild().getPreparation().setMinNetworkSize(300);
        profileDefaultProperties.getBuild().getPreparation().getMethods().getLm().setEnabled(false);
        profileDefaultProperties.getBuild().getPreparation().getMethods().getLm().setWeightings("shortest");
        profileDefaultProperties.getBuild().getPreparation().getMethods().getLm().setLandmarks(2);
        profileDefaultProperties.getService().getExecution().getMethods().getLm().setActiveLandmarks(2);

        carProfile = new ProfileProperties();
        carProfile.setEnabled(true);
        carProfile.setEncoderName(EncoderNameEnum.DRIVING_CAR);
        carProfile.getBuild().getPreparation().setMinNetworkSize(300);
        carProfile.getBuild().getPreparation().getMethods().getLm().setEnabled(true);
        carProfile.getBuild().getPreparation().getMethods().getLm().setThreads(5);
        carProfile.getService().getExecution().getMethods().getLm().setActiveLandmarks(2);
    }


    @Test
    void testProfileDefaultSerializesEnabled() throws IOException {
        // Write to yaml
        ObjectMapper mapper = new ObjectMapper();
        String yaml = mapper.writeValueAsString(profileDefaultProperties);

        // Read from yaml
        ProfileDefaultProperties profileDefaultPropertiesDeserialized = mapper.readValue(yaml, ProfileDefaultProperties.class);
        assertTrue(profileDefaultPropertiesDeserialized.getEnabled());
    }

    @Test
    void testProfileDoesNotSerializeEnabled() throws IOException {
        // Write to yaml
        ObjectMapper mapper = new ObjectMapper();
        String yaml = mapper.writeValueAsString(carProfile);

        // Read from yaml
        ProfileProperties carProfileDeserialized = mapper.readValue(yaml, ProfileProperties.class);
        assertNull(carProfileDeserialized.getEnabled());
    }
}