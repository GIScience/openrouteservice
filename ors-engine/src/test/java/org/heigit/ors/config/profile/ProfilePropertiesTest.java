package org.heigit.ors.config.profile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.config.profile.defaults.DefaultProfilePropertiesCar;
import org.heigit.ors.config.profile.storages.*;
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
    void testDeserializeExtendedStorages() throws JsonProcessingException {
        //       car:
        //        encoder_name: driving-car
        //        ext_storages:
        //          WayCategory:
        //          HeavyVehicle:
        //            restrictions: true
        //          GreenIndex:
        //            filepath: /path/to/file.csv
        String json = "{\"encoder_name\":\"driving-car\",\"ext_storages\":" +
                "{\"WayCategory\":{}," +
                "\"HeavyVehicle\":{\"restrictions\":true}, " +
                "\"GreenIndex\":{\"filepath\":\"/path/to/file.csv\"}}}";
        ProfileProperties foo = mapper.readValue(json, ProfileProperties.class);
        assertEquals("driving-car", foo.getEncoderName().getName());
        assertInstanceOf(DefaultProfilePropertiesCar.class, foo);
        assertEquals(6, foo.getExtStorages().size());
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
                case "Tollways" -> {
                    assertInstanceOf(ExtendedStorageTollways.class, value);
                    assertTrue(value.getEnabled());
                }
                case "WaySurfaceType" -> {
                    assertInstanceOf(ExtendedStorageWaySurfaceType.class, value);
                    assertTrue(value.getEnabled());
                }
                case "RoadAccessRestrictions" -> {
                    assertInstanceOf(ExtendedStorageRoadAccessRestrictions.class, value);
                    assertTrue(value.getEnabled());
                }
                default -> fail("Unexpected key: " + key);
            }
        });


    }

}