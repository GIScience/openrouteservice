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
    void testDeserializeExtendedStoragesWithNonDefaultStorages() throws JsonProcessingException {
        // This will initialize custom storages to make sure only them are deserialized without adding any other default storages.
        // Example JSON:
        //       car:
        //        encoder_name: driving-car
        //        ext_storages:
        //          WayCategory:
        //          HeavyVehicle:
        //            restrictions: true
        //          GreenIndex:
        //            filepath: /path/to/file.csv
        String json = "{\"encoder_name\":\"driving-car\",\"ext_storages\":" + "{\"WayCategory\":{ \"enabled\": false },\"HeavyVehicle\":{ \"enabled\": true, \"restrictions\": true },\"GreenIndex\":{ \"enabled\": true, \"filepath\": \"/path/to/file.csv\" }}}";
        ProfileProperties foo = mapper.readValue(json, ProfileProperties.class);
        assertEquals("driving-car", foo.getEncoderName().getName());
        assertInstanceOf(ProfileProperties.class, foo);
        assertEquals(3, foo.getExtStorages().size());
        assertTrue(foo.getExtStorages().containsKey("WayCategory"));
        assertTrue(foo.getExtStorages().containsKey("HeavyVehicle"));
        assertTrue(foo.getExtStorages().containsKey("GreenIndex"));

        foo.getExtStorages().forEach((key, value) -> {
            switch (key) {
                case "WayCategory" -> {
                    assertInstanceOf(ExtendedStorage.class, value);
                    assertFalse(value.getEnabled());
                }
                case "HeavyVehicle" -> {
                    assertInstanceOf(ExtendedStorage.class, value);
                    assertTrue(value.getEnabled());
                    assertTrue(value.getRestrictions());
                }
                case "GreenIndex" -> {
                    assertInstanceOf(ExtendedStorage.class, value);
                    assertTrue(value.getEnabled());
                    assertEquals(Path.of("/path/to/file.csv"), (value).getFilepath());
                }
                default -> fail("Unexpected key: " + key);
            }
        });
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
        assertEquals("block_fords=false|consider_elevation=false|turn_costs=true|use_acceleration=false|maximum_grade_level=4|preferred_speed_factor=0.8|problematic_speed_factor=0.5|conditional_access=true|conditional_speed=true", result);
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

}