package org.heigit.ors.config.profile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.common.EncoderNameEnum;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfilePropertiesTest {

    static ObjectMapper mapper;

    @BeforeAll
    static void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    void testWithEmptyExtendedStorages() throws JsonProcessingException {
        // This will initialize custom storages to make sure only them are deserialized without adding any other default storages.
        // Example JSON:
        //       car:
        //        encoder_name: driving-car
        //        ext_storages: {}
        String json = "{\"encoder_name\":\"driving-car\",\"ext_storages\":{}}";
        ProfileProperties foo = mapper.readValue(json, ProfileProperties.class);
        assertEquals(EncoderNameEnum.DRIVING_CAR, foo.getEncoderName());
        assertInstanceOf(ProfileProperties.class, foo);
        assertEquals(0, foo.getExtStorages().size());
    }

    @Test
    void testGetEncoderOptionsString() {
        ProfileProperties profile = new ProfileProperties();
        profile.getEncoderOptions().setMaximumGradeLevel(4);
        profile.getEncoderOptions().setPreferredSpeedFactor(0.8);
        profile.getEncoderOptions().setProblematicSpeedFactor(0.5);
        profile.getEncoderOptions().setBlockFords(false);
        profile.getEncoderOptions().setConsiderElevation(false);
        profile.getEncoderOptions().setTurnCosts(true);
        profile.getEncoderOptions().setUseAcceleration(false);
        profile.getEncoderOptions().setConditionalAccess(true);
        profile.getEncoderOptions().setConditionalSpeed(true);

        String result = profile.getEncoderOptionsString();
        assertEquals("consider_elevation=false|turn_costs=true|block_fords=false|use_acceleration=false|maximum_grade_level=4|preferred_speed_factor=0.8|problematic_speed_factor=0.5|conditional_access=true|conditional_speed=true", result);
        // Variance of the parameter values
        profile.getEncoderOptions().setMaximumGradeLevel(4);
        profile.getEncoderOptions().setBlockFords(null);
        profile.getEncoderOptions().setTurnCosts(null);
        profile.getEncoderOptions().setConsiderElevation(null);
        profile.getEncoderOptions().setUseAcceleration(null);
        profile.getEncoderOptions().setConditionalAccess(null);
        profile.getEncoderOptions().setPreferredSpeedFactor(null);
        profile.getEncoderOptions().setProblematicSpeedFactor(null);
        profile.getEncoderOptions().setConditionalSpeed(null);
        result = profile.getEncoderOptionsString();
        assertEquals("maximum_grade_level=4", result);

        // Set all to null
        profile.getEncoderOptions().setMaximumGradeLevel(null);
        assertEquals("", profile.getEncoderOptionsString());

        // Null encoder Options
        profile.setEncoderOptions(null);
        result = profile.getEncoderOptionsString();
        assertEquals("", result);

    }

    @Test
    void mergeLoaded() {
        ProfileProperties profile = new ProfileProperties();
        profile.setElevation(true);
        profile.setMaximumDistance(100.0);
        profile.getEncoderOptions().setMaximumGradeLevel(1);
        profile.getEncoderOptions().setPreferredSpeedFactor(0.8);
        profile.getEncoderOptions().setBlockFords(true);
        profile.getEncoderOptions().setTurnCosts(true);
        profile.getExecution().getMethods().getAstar().setApproximation("Beeline");
        profile.getPreparation().getMethods().getLm().setEnabled(true);
        profile.getPreparation().getMethods().getCore().setEnabled(true);
        profile.getExtStorages().put("WayCategory", new ExtendedStorage());

        ProfileProperties loadedProfile = new ProfileProperties();
        loadedProfile.setElevation(false);
        loadedProfile.getEncoderOptions().setMaximumGradeLevel(99);
        loadedProfile.getEncoderOptions().setProblematicSpeedFactor(9.9);
        loadedProfile.getEncoderOptions().setBlockFords(false);
        loadedProfile.getExecution().getMethods().getAstar().setApproximation("should not be here");
        loadedProfile.getPreparation().getMethods().getCh().setEnabled(true);
        loadedProfile.getPreparation().getMethods().getLm().setEnabled(false);
        loadedProfile.getExtStorages().put("HeavyVehicle", new ExtendedStorage());

        profile.mergeLoaded(loadedProfile);

        assertFalse(profile.getElevation(), "Elevation should be overwritten");
        assertEquals(100.0, profile.getMaximumDistance(), "Maximum distance should not be overwritten");
        assertEquals(99, profile.getEncoderOptions().getMaximumGradeLevel(), "Maximum grade level should be overwritten");
        assertNull(profile.getEncoderOptions().getPreferredSpeedFactor(), "Preferred speed factor should be null");
        assertEquals(9.9, profile.getEncoderOptions().getProblematicSpeedFactor(), "Problematic speed factor should be set");
        assertFalse(profile.getEncoderOptions().getBlockFords(), "Block fords should be overwritten");
        assertNull(profile.getEncoderOptions().getTurnCosts(), "Turn costs should be null");
        assertEquals("Beeline", profile.getExecution().getMethods().getAstar().getApproximation(), "Execution options should not be overwritten");
        assertEquals(true, profile.getPreparation().getMethods().getCh().getEnabled(), "CH should be set");
        assertEquals(false, profile.getPreparation().getMethods().getLm().getEnabled(), "LM should be overwritten");
        assertNull(profile.getPreparation().getMethods().getCore().getEnabled(), "Core should be null");
        assertTrue(profile.getExtStorages().size() == 1 && profile.getExtStorages().containsKey("HeavyVehicle"), "extStrorages should be replaced");
    }
}