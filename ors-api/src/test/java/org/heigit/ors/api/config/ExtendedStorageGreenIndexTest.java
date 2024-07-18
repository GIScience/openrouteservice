package org.heigit.ors.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedStorageGreenIndexTest {
    @Test
    void testDefaultConstructor() {
        ExtendedStorageGreenIndex storage = new ExtendedStorageGreenIndex();
        assertNotNull(storage, "Default constructor should initialize the object");
        assertTrue(storage.getEnabled(), "Default constructor should initialize 'enabled' to true");
        assertEquals("", storage.getFilepath().toString(), "Default constructor should initialize 'filepath' to an empty string");
    }

    @Test
    void testDeSerializationCorrectJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\"filepath\":\"src/test/resources/index.csv\",\"enabled\":true}";
        ExtendedStorageGreenIndex storage = objectMapper.readValue(json, ExtendedStorageGreenIndex.class);
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
        assertEquals(Paths.get("src/test/resources/index.csv").toAbsolutePath(), storage.getFilepath(), "Deserialized object should have 'filepath' set to an absolute path");
    }

    @Test
    void testSerializationCorrectJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ExtendedStorageGreenIndex storage = new ExtendedStorageGreenIndex();
        String path = "src/test/resources/index.csv";
        storage.setFilepath(Paths.get(path));
        String json = objectMapper.writeValueAsString(storage);
        assertEquals("{\"enabled\":true,\"filepath\":\"" + Paths.get(path).toAbsolutePath() + "\"}", json, "Serialized object should have 'filepath' set to an absolute path");
    }
//
//    @ParameterizedTest
//    @CsvSource({
//            ", ''", // Represents the default path for null input
//            "'', ''", // Represents the empty string input
//            "'src/test/resources', 'src/test/resources'"
//    })
//    void testSetFilepath(String input, String expected) {
//        ExtendedStorage storage = new ExtendedStorage();
//        if (input != null)
//            storage.setFilepath(Paths.get(input));
//        else
//            storage.setFilepath(null);
//
//        if (input == null || input.isEmpty()) {
//            assertEquals(Path.of(""), storage.getFilepath(), "setFilepath(null) should result in an empty path");
//        } else {
//            assertEquals(Paths.get(expected).toAbsolutePath(), storage.getFilepath(), "setFilepath('src/test/resources') should result in an absolute path");
//        }
//    }
}