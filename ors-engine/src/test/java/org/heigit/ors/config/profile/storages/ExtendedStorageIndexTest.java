package org.heigit.ors.config.profile.storages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;


class ExtendedStorageIndexTest {

    ObjectMapper objectMapper = new ObjectMapper();
    ExtendedStorageIndex source;
    ExtendedStorageIndex target;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        String json1 = "{\"GreenIndex\":{\"enabled\":false,\"filepath\": \"" + Paths.get("src/test/resources/index.csv") + "\"}}";
        String json2 = "{\"GreenIndex\":{\"enabled\":true,\"filepath\": \"" + Paths.get("other/path/index.csv") + "\"}}";
        source = objectMapper.readValue(json1, ExtendedStorageIndex.class);
        target = objectMapper.readValue(json2, ExtendedStorageIndex.class);
    }

    @Test
    void defaultConstructorInitializesObject() {
        ExtendedStorageIndex storage = new HelperClass();
        assertNotNull(storage, "Default constructor should initialize the object");
        assertTrue(storage.getEnabled(), "Default constructor should initialize 'enabled' to true");
        assertEquals("", storage.getFilepath().toString(), "Default constructor should initialize 'filepath' to an empty string");
    }

    @Test
    void deserializationCorrectJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\"GreenIndex\":{\"enabled\":false,\"filepath\":\"" + Paths.get("src/test/resources/index.csv") + "\"}}";
        ExtendedStorageIndex storage = objectMapper.readValue(json, ExtendedStorageIndex.class);
        assertFalse(storage.getEnabled(), "Deserialized object should have 'enabled' set to false");
        assertEquals(Paths.get("src/test/resources/index.csv").toAbsolutePath(), storage.getFilepath(), "Deserialized object should have 'filepath' set to an absolute path");
    }

    @Test
    void deserializeNullPath() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\"GreenIndex\":{\"enabled\":false,\"filepath\":null}}";
        ExtendedStorageIndex storage = objectMapper.readValue(json, ExtendedStorageIndex.class);
        assertFalse(storage.getEnabled(), "Deserialized object should have 'enabled' set to false");
        assertEquals(Paths.get(""), storage.getFilepath(), "Deserialized object should have 'filepath' set to an empty string");
    }

    @Test
    void copyPropertiesWithoutOverwrite() {
        assertNotEquals(source, target);
        target.copyProperties(source, false);
        assertNotEquals(source.getEnabled(), target.getEnabled(), "Enabled should not be copied when overwrite is false");
    }

    @Test
    void copyPropertiesWithOverwrite() {
        assertNotEquals(source, target);
        target.copyProperties(source, true);
        assertEquals(source.getEnabled(), target.getEnabled(), "Enabled should be copied when overwrite is true");
    }

    @Test
    void copyPropertiesWithNullSource() {
        target.copyProperties(null, true);
        assertTrue(target.getEnabled(), "Enabled should remain unchanged when source is null");
    }

    @Test
    void copyPropertiesWithEmptySource() throws JsonProcessingException {
        String emptyJson = "{\"GreenIndex\":\"\"}";
        ExtendedStorageIndex source = objectMapper.readValue(emptyJson, ExtendedStorageIndex.class);
        assertNotEquals(source, target);
        target.copyProperties(source, true);
        assertEquals(source.getEnabled(), target.getEnabled(), "Enabled should be copied from an empty source when overwrite is true");
    }

    @Test
    void copyPropertiesWithEmptyTarget() throws JsonProcessingException {
        String emptyJson = "{\"GreenIndex\":\"\"}";
        ExtendedStorageIndex target = objectMapper.readValue(emptyJson, ExtendedStorageIndex.class);
        target.copyProperties(source, true);
        assertEquals(source.getEnabled(), target.getEnabled(), "Enabled should be copied from an empty source when overwrite is true");
    }

    class HelperClass extends ExtendedStorageIndex {
        public HelperClass() {
            super();
        }
    }
}