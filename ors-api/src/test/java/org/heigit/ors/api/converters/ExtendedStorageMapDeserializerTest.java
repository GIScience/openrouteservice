package org.heigit.ors.api.converters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.heigit.ors.api.config.profile.storages.ExtendedStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedStorageMapDeserializerTest {
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Object.class, new ExtendedStorageMapDeserializer());
        mapper.registerModule(module);
    }

    @ParameterizedTest
    @CsvSource({"{\"WayCategory\":{}}, WayCategory, true", "{\"WayCategory\":{\"enabled\":false}}, WayCategory, false", "{\"HeavyVehicle\":{}}, HeavyVehicle, true", "{\"HeavyVehicle\":null}, HeavyVehicle, true", "{\"HeavyVehicle\":\"\"}, HeavyVehicle, true", "{\"HeavyVehicle\":\"null\"}, HeavyVehicle, true",})
    void testDeserializeExtendedStorage(String jsonInput, String expectedKey, boolean expectedEnabled) throws IOException {
        HelperClass foo = mapper.readValue("{\"ext_storages\":" + jsonInput + "}", HelperClass.class);
        Map<String, ExtendedStorage> extendedStorage = foo.getExtendedStorage();
        assertTrue(extendedStorage.containsKey(expectedKey));
        assertEquals(expectedEnabled, extendedStorage.get(expectedKey).getEnabled());
    }

    // Write a test to fail
    @Test
    void testDeserializeExtendedStorageFail() throws JsonProcessingException {
        // Expect ExtendedStorageDeserializationException to be thrown
        assertThrows(
                JsonMappingException.class, () ->
                        mapper.readValue("{\"ext_storages\":{\"Foo\":null}}", HelperClass.class));
    }

    /**
     * Helper class to test the deserialization of a Path object.
     * The class has a single field of type Path.
     * The field setFilepath is annotated with @JsonDeserialize to use the PathDeserializer.
     */
    @JsonTypeName("HelperClass")
    private static class HelperClass {

        private Map<String, ExtendedStorage> extendedStorage;

        @JsonProperty("ext_storages")
        public Map<String, ExtendedStorage> getExtendedStorage() {
            return this.extendedStorage;
        }

        @JsonSetter("ext_storages")
        @JsonDeserialize(using = ExtendedStorageMapDeserializer.class)
        public void setExtendedStorage(Map<String, ExtendedStorage> extendedStorage) {
            this.extendedStorage = extendedStorage;
        }
    }
}


