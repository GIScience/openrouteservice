package org.heigit.ors.config.profile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.defaults.DefaultEncoderOptionsProperties;
import org.heigit.ors.config.defaults.DefaultProfileProperties;
import org.heigit.ors.config.defaults.DefaultProfilePropertiesCar;
import org.heigit.ors.config.profile.storages.ExtendedStorage;
import org.heigit.ors.config.profile.storages.ExtendedStorageGreenIndex;
import org.heigit.ors.config.profile.storages.ExtendedStorageHeavyVehicle;
import org.heigit.ors.config.profile.storages.ExtendedStorageWayCategory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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
        String json = "{\"encoder_name\":\"driving-car\",\"ext_storages\":" + "{\"WayCategory\":{}," + "\"HeavyVehicle\":{\"restrictions\":true}, " + "\"GreenIndex\":{\"filepath\":\"/path/to/file.csv\"}}}";
        ProfileProperties foo = mapper.readValue(json, ProfileProperties.class);
        assertEquals("driving-car", foo.getEncoderName().getName());
        assertInstanceOf(DefaultProfilePropertiesCar.class, foo);
        assertEquals(3, foo.getExtStorages().size());
        assertTrue(foo.getExtStorages().containsKey("WayCategory"));
        assertTrue(foo.getExtStorages().containsKey("HeavyVehicle"));
        assertTrue(foo.getExtStorages().containsKey("GreenIndex"));

        foo.getExtStorages().forEach((key, value) -> {
            switch (key) {
                case "WayCategory" -> {
                    assertInstanceOf(ExtendedStorageWayCategory.class, value);
                    assertTrue(value.getEnabled());
                }
                case "HeavyVehicle" -> {
                    assertInstanceOf(ExtendedStorageHeavyVehicle.class, value);
                    assertTrue(value.getEnabled());
                    assertTrue(((ExtendedStorageHeavyVehicle) value).getRestrictions());
                }
                case "GreenIndex" -> {
                    assertTrue(value.getEnabled());
                    assertEquals(Path.of("/path/to/file.csv"), ((ExtendedStorageGreenIndex) value).getFilepath());
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
        assertInstanceOf(DefaultProfilePropertiesCar.class, foo);
        assertEquals(0, foo.getExtStorages().size());
    }

    @Test
    void testGetEncoderOptionsString() {
        ProfileProperties profile = new DefaultProfilePropertiesCar(true);
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

    @Test
    void testCopyProperties() {
        ProfileProperties source = new DefaultProfileProperties(true);
        source.setEncoderOptions(new DefaultEncoderOptionsProperties(true));
        source.getEncoderOptions().setMaximumGradeLevel(9);

        ProfileProperties target = new DefaultProfileProperties(true);
        target.setEncoderOptions(new DefaultEncoderOptionsProperties(true));
        target.getEncoderOptions().setMaximumGradeLevel(5);

        assertNotEquals(source, target, "Source and target should not be equal before copying properties");
        target.copyProperties(source, false);

        assertNotEquals(source, target, "All properties should not be copied when overwrite is false");

        target.copyProperties(source, true);

        assertEquals(source, target, "All properties should be copied when overwrite is true");
        assertEquals(9, target.getEncoderOptions().getMaximumGradeLevel(), "MaximumGradeLevel should be copied");
    }

    @Test
    void testCopyPropertiesWithNullSource() {
        ProfileProperties target = new DefaultProfileProperties(true);
        target.getEncoderOptions().setMaximumGradeLevel(5);

        target.copyProperties(null, true);

        assertEquals(5, target.getEncoderOptions().getMaximumGradeLevel(), "MaximumGradeLevel should remain unchanged when source is null");
    }

    @Test
    void testCopyPropertiesWithEmptySource() {
        ProfileProperties source = new DefaultProfileProperties();
        ProfileProperties target = new DefaultProfileProperties(true);

        target.getEncoderOptions().setMaximumGradeLevel(5);
        target.getEncoderOptions().setPreferredSpeedFactor(0.9);
        target.getEncoderOptions().setProblematicSpeedFactor(0.6);

        assertNotEquals(source, target, "Source and target should not be equal before copying properties");
        target.copyProperties(source, true);

        assertNotEquals(source, target, "Source and target should not be equal after copying properties from an empty source with overwrite");
        assertEquals(5, target.getEncoderOptions().getMaximumGradeLevel(), "MaximumGradeLevel should remain unchanged when source is empty");
        assertEquals(0.9, target.getEncoderOptions().getPreferredSpeedFactor(), "PreferredSpeedFactor should remain unchanged when source is empty");
        assertEquals(0.6, target.getEncoderOptions().getProblematicSpeedFactor(), "ProblematicSpeedFactor should remain unchanged when source is empty");
    }

    @Test
    void testCopyPropertiesWithEmptyTarget() {
        DefaultProfileProperties target = new DefaultProfileProperties();
        target.setEncoderOptions(null);
        target.setPreparation(null);
        target.setExecution(null);
        target.setExtStorages(null);
        DefaultProfileProperties source = new DefaultProfileProperties(true);
        source.getEncoderOptions().setMaximumGradeLevel(4);
        source.getEncoderOptions().setPreferredSpeedFactor(0.8);
        source.getEncoderOptions().setProblematicSpeedFactor(0.5);

        assertNotEquals(source, target, "Source and target should not be equal before copying properties");
        target.copyProperties(source, true);
        assertEquals(4, target.getEncoderOptions().getMaximumGradeLevel(), "MaximumGradeLevel should not be copied when overwrite is false");
        assertEquals(0.8, target.getEncoderOptions().getPreferredSpeedFactor(), "PreferredSpeedFactor should not be copied when overwrite is false");
        assertEquals(0.5, target.getEncoderOptions().getProblematicSpeedFactor(), "ProblematicSpeedFactor should not be copied when overwrite is false");
        assertEquals(source.getEncoderOptions(), target.getEncoderOptions(), "EncoderOptions should not be copied when overwrite is false");
        assertEquals(source.getPreparation(), target.getPreparation(), "Preparation should not be copied when overwrite is false");
        assertEquals(source.getExecution(), target.getExecution(), "Execution should not be copied when overwrite is false");
        assertEquals(source.getExtStorages(), target.getExtStorages(), "ExtStorages should not be copied when overwrite is false");
    }

    @Test
    void testCopyPropertiesWithDefaultTarget() {
        DefaultProfileProperties target = new DefaultProfileProperties();
        DefaultProfileProperties source = new DefaultProfileProperties(true);
        source.getEncoderOptions().setMaximumGradeLevel(4);
        source.getEncoderOptions().setPreferredSpeedFactor(0.8);
        source.getEncoderOptions().setProblematicSpeedFactor(0.5);

        assertNotEquals(source, target, "Source and target should not be equal before copying properties");
        target.copyProperties(source, true);
        assertEquals(4, target.getEncoderOptions().getMaximumGradeLevel(), "MaximumGradeLevel should not be copied when overwrite is false");
        assertEquals(0.8, target.getEncoderOptions().getPreferredSpeedFactor(), "PreferredSpeedFactor should not be copied when overwrite is false");
        assertEquals(0.5, target.getEncoderOptions().getProblematicSpeedFactor(), "ProblematicSpeedFactor should not be copied when overwrite is false");
        assertEquals(source.getEncoderOptions(), target.getEncoderOptions(), "EncoderOptions should not be copied when overwrite is false");
        assertEquals(source.getPreparation(), target.getPreparation(), "Preparation should not be copied when overwrite is false");
        assertEquals(source.getExecution(), target.getExecution(), "Execution should not be copied when overwrite is false");
        assertEquals(source.getExtStorages(), target.getExtStorages(), "ExtStorages should not be copied when overwrite is false");
    }

    @Test
    void testCopyPropertiesWithExtendedStoragesInSource() {
        DefaultProfileProperties source = new DefaultProfileProperties(true);
        Map<String, ExtendedStorage> extendedStorages = new HashMap<>();
        extendedStorages.put("WayCategory", new ExtendedStorageWayCategory());
        extendedStorages.put("HeavyVehicle", new ExtendedStorageHeavyVehicle(true));
        source.setExtStorages(extendedStorages);
        DefaultProfileProperties target = new DefaultProfileProperties(true);
        target.setExtStorages(null);

        assertNotEquals(source, target, "Source and target should not be equal before copying properties");
        target.copyProperties(source, true);
        assertEquals(source, target, "All properties should be copied when overwrite is true");
    }

    @Test
    void testCopyPropertiesWithExtendedStoragesInBoth() {
        DefaultProfileProperties source = new DefaultProfileProperties(true);
        Map<String, ExtendedStorage> extendedStorages = new HashMap<>();
        extendedStorages.put("WayCategory", new ExtendedStorageWayCategory());
        extendedStorages.put("HeavyVehicle", new ExtendedStorageHeavyVehicle(true));
        source.setExtStorages(extendedStorages);
        DefaultProfileProperties target = new DefaultProfileProperties(true);
        Map<String, ExtendedStorage> extendedStoragesTarget = new HashMap<>();
        extendedStoragesTarget.put("WayCategory", new ExtendedStorageWayCategory());
        target.setExtStorages(extendedStoragesTarget);

        assertNotEquals(source, target, "Source and target should not be equal before copying properties");
        target.copyProperties(source, true);
        assertEquals(source, target, "All properties should be copied when overwrite is true");
    }
}