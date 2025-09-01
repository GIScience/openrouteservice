package org.heigit.ors.config.profile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.common.EncoderNameEnum;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ProfilePropertiesTest {

    static ObjectMapper mapper;

    @BeforeAll
    static void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    void testWithEmptyExtendedStorages() throws JsonProcessingException {
        String json = "{\"encoder_name\":\"driving-car\",\"build\":{\"ext_storages\":{}}}";
        ProfileProperties foo = mapper.readValue(json, ProfileProperties.class);
        assertEquals(EncoderNameEnum.DRIVING_CAR, foo.getEncoderName());
        assertInstanceOf(ProfileProperties.class, foo);
        assertEquals(0, foo.getBuild().getExtStorages().size());
    }

    @Test
    void testGetEncoderOptionsString() {
        ProfileProperties profile = new ProfileProperties();
        profile.getBuild().getEncoderOptions().setMaximumGradeLevel(4);
        profile.getBuild().getEncoderOptions().setPreferredSpeedFactor(0.8);
        profile.getBuild().getEncoderOptions().setProblematicSpeedFactor(0.5);
        profile.getBuild().getEncoderOptions().setBlockFords(false);
        profile.getBuild().getEncoderOptions().setConsiderElevation(false);
        profile.getBuild().getEncoderOptions().setTurnCosts(true);
        profile.getBuild().getEncoderOptions().setUseAcceleration(false);
        profile.getBuild().getEncoderOptions().setConditionalAccess(true);
        profile.getBuild().getEncoderOptions().setConditionalSpeed(true);

        String result = profile.getBuild().getEncoderOptionsString();
        assertEquals("consider_elevation=false|turn_costs=true|block_fords=false|use_acceleration=false|maximum_grade_level=4|preferred_speed_factor=0.8|problematic_speed_factor=0.5|conditional_access=true|conditional_speed=true", result);
        // Variance of the parameter values
        profile.getBuild().getEncoderOptions().setMaximumGradeLevel(4);
        profile.getBuild().getEncoderOptions().setBlockFords(null);
        profile.getBuild().getEncoderOptions().setTurnCosts(null);
        profile.getBuild().getEncoderOptions().setConsiderElevation(null);
        profile.getBuild().getEncoderOptions().setUseAcceleration(null);
        profile.getBuild().getEncoderOptions().setConditionalAccess(null);
        profile.getBuild().getEncoderOptions().setPreferredSpeedFactor(null);
        profile.getBuild().getEncoderOptions().setProblematicSpeedFactor(null);
        profile.getBuild().getEncoderOptions().setConditionalSpeed(null);
        result = profile.getBuild().getEncoderOptionsString();
        assertEquals("maximum_grade_level=4", result);

        // Set all to null
        profile.getBuild().getEncoderOptions().setMaximumGradeLevel(null);
        assertEquals("", profile.getBuild().getEncoderOptionsString());

        // Null encoder Options
        profile.getBuild().setEncoderOptions(null);
        result = profile.getBuild().getEncoderOptionsString();
        assertEquals("", result);

    }

    @Test
    void testMergeDefaults() {
        ProfileProperties profile = new ProfileProperties();
        profile.getBuild().setElevation(true);
        profile.getService().setMaximumDistance(100.0);
        profile.getBuild().getEncoderOptions().setMaximumGradeLevel(1);
        profile.getBuild().getEncoderOptions().setPreferredSpeedFactor(0.8);
        profile.getBuild().getPreparation().getMethods().getCore().setEnabled(true);
        profile.getBuild().getExtStorages().put("WayCategory", new ExtendedStorageProperties());
        profile.getBuild().getEncodedValues().setWayType(true);

        ProfileProperties defaultProfile = new ProfileProperties();
        defaultProfile.setGraphPath(Path.of("/path/to/graphs/cannot/be/null"));
        defaultProfile.getBuild().setSourceFile(Path.of("/path/to/source/cannot/be/null"));
        defaultProfile.getBuild().setElevation(false);
        defaultProfile.getService().setMaximumDistanceAvoidAreas(100.0);
        defaultProfile.getBuild().getEncoderOptions().setMaximumGradeLevel(2);
        defaultProfile.getBuild().getEncoderOptions().setProblematicSpeedFactor(9.9);
        defaultProfile.getService().getExecution().getMethods().getAstar().setApproximation("Beeline");
        defaultProfile.getBuild().getPreparation().getMethods().getLm().setEnabled(true);
        defaultProfile.getBuild().getExtStorages().put("HeavyVehicle", new ExtendedStorageProperties());
        profile.getBuild().getEncodedValues().setWaySurface(true);

        profile.mergeDefaults(defaultProfile, "profName");

        assertEquals("profName", profile.getProfileName(), "Profile name should be set");

        assertTrue(profile.getBuild().getElevation(), "Elevation should not be overwritten");
        assertEquals(100.0, profile.getService().getMaximumDistance(), "Maximum distance should not be overwritten");
        assertEquals(100.0, profile.getService().getMaximumDistanceAvoidAreas(), "Maximum distance avoid areas should not be written");

        assertEquals(1, profile.getBuild().getEncoderOptions().getMaximumGradeLevel(), "Maximum grade level should not be overwritten");
        assertEquals(0.8, profile.getBuild().getEncoderOptions().getPreferredSpeedFactor(), "Preferred speed factor should be left alone");
        assertEquals(9.9, profile.getBuild().getEncoderOptions().getProblematicSpeedFactor(), "Problematic speed factor should be set");
        assertNull(profile.getBuild().getEncoderOptions().getBlockFords(), "Block fords should be null");

        assertEquals("Beeline", profile.getService().getExecution().getMethods().getAstar().getApproximation(), "Execution options should be set by default");
        assertTrue(profile.getBuild().getPreparation().getMethods().getCore().getEnabled(), "Core should be enabled");
        assertTrue(profile.getBuild().getPreparation().getMethods().getLm().getEnabled(), "LM should be enabled by default");

        assertEquals(2, profile.getBuild().getExtStorages().size(), "extStrorages should be merged");
        assertTrue(profile.getBuild().getExtStorages().containsKey("WayCategory"), "extStrorages should be merged");
        assertTrue(profile.getBuild().getExtStorages().containsKey("HeavyVehicle"), "extStrorages should be merged");

        assertTrue(profile.getBuild().getEncodedValues().getWayType(), "way_type should not be overwritten");
        assertTrue(profile.getBuild().getEncodedValues().getWaySurface(), "way_surface should be merged");
    }

    @Test
    void testMergeLoaded() {
        ProfileProperties profile = new ProfileProperties();
        profile.getBuild().setElevation(true);
        profile.getService().setMaximumDistance(100.0);
        profile.getBuild().getEncoderOptions().setMaximumGradeLevel(1);
        profile.getBuild().getEncoderOptions().setPreferredSpeedFactor(0.8);
        profile.getBuild().getEncoderOptions().setBlockFords(true);
        profile.getBuild().getEncoderOptions().setTurnCosts(true);
        profile.getService().getExecution().getMethods().getAstar().setApproximation("Beeline");
        profile.getBuild().getPreparation().getMethods().getLm().setEnabled(true);
        profile.getBuild().getPreparation().getMethods().getCore().setEnabled(true);
        profile.getBuild().getExtStorages().put("WayCategory", new ExtendedStorageProperties());
        profile.getBuild().getEncodedValues().setWayType(true);

        ProfileProperties loadedProfile = new ProfileProperties();
        loadedProfile.setEncoderName(EncoderNameEnum.DRIVING_CAR);
        loadedProfile.getBuild().setElevation(false);
        loadedProfile.getBuild().getEncoderOptions().setMaximumGradeLevel(99);
        loadedProfile.getBuild().getEncoderOptions().setProblematicSpeedFactor(9.9);
        loadedProfile.getBuild().getEncoderOptions().setBlockFords(false);
        loadedProfile.getService().getExecution().getMethods().getAstar().setApproximation("should not be here");
        loadedProfile.getBuild().getPreparation().getMethods().getCh().setEnabled(true);
        loadedProfile.getBuild().getPreparation().getMethods().getLm().setEnabled(false);
        loadedProfile.getBuild().getExtStorages().put("HeavyVehicle", new ExtendedStorageProperties());
        loadedProfile.getBuild().getEncodedValues().setWaySurface(true);

        profile.mergeLoaded(loadedProfile);

        assertFalse(profile.getBuild().getElevation(), "Elevation should be overwritten");
        assertEquals(100.0, profile.getService().getMaximumDistance(), "Maximum distance should not be overwritten");
        assertEquals(99, profile.getBuild().getEncoderOptions().getMaximumGradeLevel(), "Maximum grade level should be overwritten");
        assertNull(profile.getBuild().getEncoderOptions().getPreferredSpeedFactor(), "Preferred speed factor should be null");
        assertEquals(9.9, profile.getBuild().getEncoderOptions().getProblematicSpeedFactor(), "Problematic speed factor should be set");
        assertFalse(profile.getBuild().getEncoderOptions().getBlockFords(), "Block fords should be overwritten");
        assertNull(profile.getBuild().getEncoderOptions().getTurnCosts(), "Turn costs should be null");
        assertEquals("Beeline", profile.getService().getExecution().getMethods().getAstar().getApproximation(), "Execution options should not be overwritten");
        assertEquals(true, profile.getBuild().getPreparation().getMethods().getCh().getEnabled(), "CH should be set");
        assertEquals(false, profile.getBuild().getPreparation().getMethods().getLm().getEnabled(), "LM should be overwritten");
        assertNull(profile.getBuild().getPreparation().getMethods().getCore().getEnabled(), "Core should be null");
        assertTrue(profile.getBuild().getExtStorages().size() == 1 && profile.getBuild().getExtStorages().containsKey("HeavyVehicle"), "extStrorages should be replaced");
        assertNull(profile.getBuild().getEncodedValues().getWayType(), "way_type should be null");
        assertTrue(profile.getBuild().getEncodedValues().getWaySurface(), "way_surface should be overwritten");
    }
}